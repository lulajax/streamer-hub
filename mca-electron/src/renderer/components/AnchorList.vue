<template>
  <div class="w-48 bg-slate-900 border-r border-slate-800 flex flex-col h-full">
    <!-- Header -->
    <div class="p-3 border-b border-slate-800">
      <h3 class="text-white font-medium text-sm flex items-center gap-2">
        <Users class="w-4 h-4 text-blue-400" />
        主播配置
      </h3>
    </div>

    <!-- Anchor List -->
    <ScrollArea class="flex-1">
      <div class="p-2 space-y-1">
        <div
          v-for="(anchor, index) in anchors"
          :key="anchor.id"
          class="flex items-center gap-2 p-2 rounded-lg bg-slate-800/60 hover:bg-slate-800 transition-colors group"
        >
          <span class="text-xs text-slate-500 w-4">{{ index + 1 }}</span>
          <Avatar class="w-8 h-8 flex-shrink-0">
            <AvatarImage :src="anchor.avatar || ''" />
            <AvatarFallback class="bg-slate-700 text-white text-xs">
              {{ anchor.name.charAt(0) }}
            </AvatarFallback>
          </Avatar>
          <div class="flex-1 min-w-0">
            <p class="text-white text-sm truncate">{{ anchor.name }}</p>
          </div>
          <button
            class="w-5 h-5 rounded-full bg-slate-700 text-slate-400 hover:bg-red-500/20 hover:text-red-400 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
            @click="handleRemove(anchor.id)"
          >
            <X class="w-3 h-3" />
          </button>
        </div>

        <!-- Empty Slot -->
        <button
          v-for="slot in emptySlots"
          :key="slot"
          class="w-full flex items-center gap-2 p-2 rounded-lg border border-dashed border-slate-700 hover:border-slate-600 hover:bg-slate-800/30 transition-colors text-slate-500"
          @click="handleAddClick"
        >
          <span class="text-xs w-4">{{ anchors.length + slot }}</span>
          <div class="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center">
            <Plus class="w-4 h-4" />
          </div>
          <span class="text-sm">添加</span>
        </button>
      </div>
    </ScrollArea>

    <!-- Add Button -->
    <div class="p-3 border-t border-slate-800">
      <Button
        variant="outline"
        class="w-full border-slate-700 text-slate-300 hover:bg-slate-800"
        @click="handleAddClick"
      >
        <Plus class="w-4 h-4 mr-1" />
        添加主播
      </Button>
    </div>

    <!-- Apply Button -->
    <div class="p-3 border-t border-slate-800">
      <Button
        class="w-full bg-blue-600 hover:bg-blue-700"
        @click="handleApplyToAll"
      >
        应用到全部
      </Button>
    </div>

    <!-- Add Anchor Dialog -->
    <Dialog :open="isDialogOpen" @update:open="isDialogOpen = $event">
      <DialogContent class="bg-slate-900 border-slate-800 text-white">
        <DialogHeader>
          <DialogTitle>添加主播</DialogTitle>
          <DialogDescription class="text-slate-400">
            选择要添加的主播
          </DialogDescription>
        </DialogHeader>

        <div class="space-y-2 mt-4 max-h-64 overflow-y-auto">
          <div
            v-for="anchor in availableAnchors"
            :key="anchor.id"
            class="flex items-center gap-3 p-3 rounded-lg bg-slate-800 hover:bg-slate-700 cursor-pointer transition-colors"
            @click="handleSelectAnchor(anchor)"
          >
            <Avatar class="w-10 h-10">
              <AvatarImage :src="anchor.avatarUrl || ''" />
              <AvatarFallback class="bg-slate-700 text-white">
                {{ anchor.name.charAt(0) }}
              </AvatarFallback>
            </Avatar>
            <div>
              <p class="text-white font-medium">{{ anchor.name }}</p>
              <p class="text-slate-400 text-sm">{{ anchor.tiktokId || '-' }}</p>
            </div>
          </div>

          <div v-if="availableAnchors.length === 0" class="text-center py-8 text-slate-500">
            <Users class="w-12 h-12 mx-auto mb-3 opacity-50" />
            <p>没有可用的主播</p>
            <p class="text-sm">请先在 Dashboard 中添加主播</p>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Users, Plus, X } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'
import ScrollArea from '@/components/ui/ScrollArea.vue'
import Avatar from '@/components/ui/Avatar.vue'
import AvatarImage from '@/components/ui/AvatarImage.vue'
import AvatarFallback from '@/components/ui/AvatarFallback.vue'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import type { Anchor, UserAnchor } from '@/types'

interface Props {
  anchors: Anchor[]
  userAnchors: UserAnchor[]
  maxSlots?: number
}

interface Emits {
  (e: 'add', anchorId: string): void
  (e: 'remove', anchorId: string): void
  (e: 'apply-to-all'): void
}

const props = withDefaults(defineProps<Props>(), {
  maxSlots: 8
})

const emit = defineEmits<Emits>()

const isDialogOpen = ref(false)

const emptySlots = computed(() => {
  const remaining = Math.max(0, props.maxSlots - props.anchors.length)
  return Math.min(remaining, 4)
})

const availableAnchors = computed(() => {
  const linkedIds = new Set(props.anchors.map(a => a.id))
  return props.userAnchors.filter(ua => !linkedIds.has(ua.id))
})

const handleAddClick = () => {
  if (availableAnchors.value.length === 0) {
    return
  }
  isDialogOpen.value = true
}

const handleSelectAnchor = (anchor: UserAnchor) => {
  emit('add', anchor.id)
  isDialogOpen.value = false
}

const handleRemove = (anchorId: string) => {
  emit('remove', anchorId)
}

const handleApplyToAll = () => {
  emit('apply-to-all')
}
</script>
