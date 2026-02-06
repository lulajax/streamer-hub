<template>
  <Dialog :open="isOpen" @update:open="handleOpenChange">
    <DialogContent class="bg-slate-900 border-slate-800 text-white max-w-2xl">
      <DialogHeader>
        <DialogTitle class="flex items-center gap-2">
          <Gift class="w-5 h-5 text-pink-400" />
          选择礼物
        </DialogTitle>
        <DialogDescription class="text-slate-400">
          从 TikTok 礼物列表中选择
        </DialogDescription>
      </DialogHeader>

      <!-- Search -->
      <div class="relative">
        <Search class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <Input
          v-model="searchQuery"
          placeholder="搜索礼物..."
          class="pl-9 bg-slate-800 border-slate-700 text-white"
        />
      </div>

      <!-- Gift Grid -->
      <ScrollArea class="h-80">
        <div v-if="isLoading" class="flex items-center justify-center h-full">
          <Loader2 class="w-8 h-8 animate-spin text-blue-400" />
          <span class="ml-2 text-slate-400">加载礼物列表...</span>
        </div>

        <div v-else-if="error" class="flex flex-col items-center justify-center h-full text-slate-500">
          <AlertCircle class="w-12 h-12 mb-3" />
          <p>{{ error }}</p>
          <Button variant="outline" class="mt-4" @click="fetchGifts">
            重试
          </Button>
        </div>

        <div v-else-if="filteredGifts.length === 0" class="flex flex-col items-center justify-center h-full text-slate-500">
          <Gift class="w-12 h-12 mb-3 opacity-50" />
          <p>暂无礼物数据</p>
          <p class="text-sm">请确保已连接直播间</p>
        </div>

        <div v-else class="grid grid-cols-4 gap-3 p-1">
          <button
            v-for="gift in filteredGifts"
            :key="gift.giftId"
            class="flex flex-col items-center p-3 rounded-lg bg-slate-800 hover:bg-slate-700 transition-colors border border-transparent hover:border-slate-600"
            @click="selectGift(gift)"
          >
            <img
              :src="gift.giftIcon"
              :alt="gift.giftName"
              class="w-12 h-12 object-contain mb-2"
              @error="handleImageError"
            />
            <span class="text-xs text-white text-center truncate w-full">{{ gift.giftName }}</span>
            <span class="text-xs text-pink-400">{{ gift.diamondCost }}💎</span>
          </button>
        </div>
      </ScrollArea>

      <DialogFooter>
        <Button variant="outline" @click="close">
          取消
        </Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Gift, Search, Loader2, AlertCircle } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import ScrollArea from '@/components/ui/ScrollArea.vue'
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { useToast } from '@/hooks/use-toast'

export interface TikTokGift {
  giftId: string
  giftName: string
  giftIcon: string
  diamondCost: number
  quantity: number
  totalCost: number
}

interface Props {
  open: boolean
  roomId?: string
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'select', gift: TikTokGift): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const { toast } = useToast()

const isOpen = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const searchQuery = ref('')
const gifts = ref<TikTokGift[]>([])
const isLoading = ref(false)
const error = ref('')

const filteredGifts = computed(() => {
  if (!searchQuery.value) return gifts.value
  const query = searchQuery.value.toLowerCase()
  return gifts.value.filter(gift =>
    gift.giftName.toLowerCase().includes(query)
  )
})

const fetchGifts = async () => {
  if (!props.roomId) {
    error.value = '请先连接直播间'
    return
  }

  isLoading.value = true
  error.value = ''

  try {
    const bridge = window.electronAPI?.tiktokLive
    if (!bridge) {
      throw new Error('TikTok Live bridge is not available')
    }

    const result = await bridge.getAvailableGifts(props.roomId)
    gifts.value = result || []
  } catch (err) {
    console.error('Failed to fetch gifts:', err)
    error.value = '获取礼物列表失败'
  } finally {
    isLoading.value = false
  }
}

const selectGift = (gift: TikTokGift) => {
  emit('select', gift)
  close()
}

const close = () => {
  isOpen.value = false
}

const handleOpenChange = (open: boolean) => {
  isOpen.value = open
  if (open) {
    fetchGifts()
  }
}

const handleImageError = (e: Event) => {
  const img = e.target as HTMLImageElement
  img.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"%3E%3Cpath d="M20 12v10H4V12"/%3E%3Cpath d="M2 7h20v5H2z"/%3E%3Cpath d="M12 22V7"/%3E%3Cpath d="M12 7H7.5a2.5 2.5 0 0 1 0-5C11 2 12 7 12 7z"/%3E%3Cpath d="M12 7h4.5a2.5 2.5 0 0 0 0-5C13 2 12 7 12 7z"/%3E%3C/svg%3E'
}

// Fetch gifts when dialog opens
watch(() => props.open, (open) => {
  if (open) {
    fetchGifts()
  }
})
</script>
