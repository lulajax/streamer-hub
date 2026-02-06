<template>
  <div class="h-full flex flex-col">
    <!-- Header with Config Tabs -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-4">
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <component :is="icon" class="w-6 h-6" :class="iconClass" />
          {{ title }}
        </h1>
      </div>
      <div class="flex items-center gap-3">
        <Button
          variant="outline"
          class="border-slate-700 text-slate-300"
          @click="handleReset"
        >
          <RotateCcw class="w-4 h-4 mr-2" />
          重置
        </Button>
        <template v-if="isRunning">
          <Button variant="secondary" @click="handlePause">
            <Pause class="w-4 h-4 mr-2" />
            暂停
          </Button>
          <Button v-if="showEndRound" variant="destructive" @click="handleEndRound">
            <ChevronRight class="w-4 h-4 mr-2" />
            结束回合
          </Button>
        </template>
        <Button v-else :class="startButtonClass" @click="handleStart">
          <Play class="w-4 h-4 mr-2" />
          {{ startButtonText }}
        </Button>
      </div>
    </div>

    <!-- Config Tabs -->
    <ModeConfigTabs
      :configs="configs"
      :active-config-id="activeConfigId"
      @select="handleConfigSelect"
      @add="handleConfigAdd"
      @remove="handleConfigRemove"
    />

    <!-- Main Content Area -->
    <div class="flex-1 flex overflow-hidden border border-slate-800 rounded-lg bg-slate-900/50">
      <!-- Left: Anchor List -->
      <AnchorList
        :anchors="anchors"
        :user-anchors="userAnchors"
        @add="handleAnchorAdd"
        @remove="handleAnchorRemove"
        @apply-to-all="handleApplyToAll"
      />

      <!-- Right: Config Area -->
      <div class="flex-1 overflow-hidden">
        <ScrollArea class="h-full">
          <div class="p-6">
            <slot name="config" />
          </div>
        </ScrollArea>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Play, Pause, RotateCcw, ChevronRight } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'
import ScrollArea from '@/components/ui/ScrollArea.vue'
import ModeConfigTabs, { type ConfigTab } from './ModeConfigTabs.vue'
import AnchorList from './AnchorList.vue'
import type { Anchor, UserAnchor } from '@/types'
import type { Component } from 'vue'

interface Props {
  title: string
  icon: Component
  iconClass?: string
  configs: ConfigTab[]
  activeConfigId: string | null
  anchors: Anchor[]
  userAnchors: UserAnchor[]
  isRunning: boolean
  showEndRound?: boolean
  startButtonText?: string
  startButtonClass?: string
}

interface Emits {
  (e: 'config-select', configId: string): void
  (e: 'config-add'): void
  (e: 'config-remove', configId: string): void
  (e: 'anchor-add', anchorId: string): void
  (e: 'anchor-remove', anchorId: string): void
  (e: 'apply-to-all'): void
  (e: 'start'): void
  (e: 'pause'): void
  (e: 'reset'): void
  (e: 'end-round'): void
}

const props = withDefaults(defineProps<Props>(), {
  iconClass: 'text-blue-400',
  showEndRound: false,
  startButtonText: '开始',
  startButtonClass: 'bg-green-600 hover:bg-green-700'
})

const emit = defineEmits<Emits>()

const handleConfigSelect = (configId: string) => {
  emit('config-select', configId)
}

const handleConfigAdd = () => {
  emit('config-add')
}

const handleConfigRemove = (configId: string) => {
  emit('config-remove', configId)
}

const handleAnchorAdd = (anchorId: string) => {
  emit('anchor-add', anchorId)
}

const handleAnchorRemove = (anchorId: string) => {
  emit('anchor-remove', anchorId)
}

const handleApplyToAll = () => {
  emit('apply-to-all')
}

const handleStart = () => {
  emit('start')
}

const handlePause = () => {
  emit('pause')
}

const handleReset = () => {
  emit('reset')
}

const handleEndRound = () => {
  emit('end-round')
}
</script>
