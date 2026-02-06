<template>
  <ModeConfigLayout
    :title="t('stickerMode')"
    :icon="Sticker"
    icon-class="text-pink-400"
    :configs="configs"
    :active-config-id="activeConfigId"
    :anchors="anchors"
    :user-anchors="store.userAnchors"
    :is-running="isRunning"
    :start-button-text="'开始'"
    :start-button-class="'bg-pink-600 hover:bg-pink-700'"
    @config-select="handleConfigSelect"
    @config-add="handleConfigAdd"
    @config-remove="handleConfigRemove"
    @anchor-add="handleAnchorAdd"
    @anchor-remove="handleAnchorRemove"
    @apply-to-all="handleApplyToAll"
    @start="handleStart"
    @pause="handlePause"
    @reset="handleReset"
  >
    <template #config>
      <!-- Mode Type Selection -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Settings2 class="w-5 h-5 text-blue-400" />
            模式类型
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex gap-4">
            <button
              class="flex-1 p-4 rounded-lg border transition-colors text-left"
              :class="config.type === 'normal' ? 'border-blue-500 bg-blue-500/10' : 'border-slate-700 bg-slate-800'"
              @click="config.type = 'normal'"
            >
              <Sticker class="w-6 h-6 text-blue-400 mb-2" />
              <p class="text-white font-medium">普通模式</p>
              <p class="text-slate-400 text-sm">仅显示贴纸，不计分</p>
            </button>
            <button
              class="flex-1 p-4 rounded-lg border transition-colors text-left"
              :class="config.type === 'scoring' ? 'border-purple-500 bg-purple-500/10' : 'border-slate-700 bg-slate-800'"
              @click="config.type = 'scoring'"
            >
              <Sparkles class="w-6 h-6 text-purple-400 mb-2" />
              <p class="text-white font-medium">计分模式</p>
              <p class="text-slate-400 text-sm">礼物计分，支持倒计时/衰减</p>
            </button>
          </div>
        </CardContent>
      </Card>

      <!-- Scoring Mode Settings -->
      <template v-if="config.type === 'scoring'">
        <Card class="bg-slate-900 border-slate-800 mb-6">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Clock class="w-5 h-5 text-amber-400" />
              倒计时设置
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Timer class="w-5 h-5 text-amber-400" />
                <div>
                  <p class="text-white font-medium">倒计时（秒）</p>
                  <p class="text-slate-400 text-sm">倒计时结束后重置分数</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <Input
                  v-if="config.countdownEnabled"
                  v-model.number="config.countdownDuration"
                  type="number"
                  class="w-24 bg-slate-900 border-slate-700 text-white"
                />
                <Switch v-model="config.countdownEnabled" />
              </div>
            </div>

            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Timer class="w-5 h-5 text-red-400" />
                <div>
                  <p class="text-white font-medium">分数衰减</p>
                  <p class="text-slate-400 text-sm">无礼物时自动衰减分数</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <Input
                  v-if="config.decayEnabled"
                  v-model.number="config.decayDuration"
                  type="number"
                  class="w-24 bg-slate-900 border-slate-700 text-white"
                  placeholder="秒"
                />
                <Switch v-model="config.decayEnabled" />
              </div>
            </div>
          </CardContent>
        </Card>
      </template>

      <!-- Auto Flip Settings -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <FlipHorizontal class="w-5 h-5 text-green-400" />
            自动翻页
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
            <div class="flex items-center gap-3">
              <FlipHorizontal class="w-5 h-5 text-green-400" />
              <div>
                <p class="text-white font-medium">自动翻页</p>
                <p class="text-slate-400 text-sm">自动切换不同页面</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <Input
                v-if="config.autoFlipEnabled"
                v-model.number="config.flipInterval"
                type="number"
                class="w-24 bg-slate-900 border-slate-700 text-white"
                placeholder="秒"
              />
              <Switch v-model="config.autoFlipEnabled" />
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Target Gifts -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader class="flex flex-row items-center justify-between">
          <CardTitle class="text-white flex items-center gap-2">
            <Gift class="w-5 h-5 text-purple-400" />
            目标礼物
          </CardTitle>
          <Button size="sm" @click="openGiftSelector">
            <Plus class="w-4 h-4 mr-1" />
            添加
          </Button>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="grid grid-cols-6 gap-3">
            <div
              v-for="(gift, index) in targetGifts.targetGifts"
              :key="index"
              class="bg-slate-800 rounded-lg p-3 flex flex-col gap-2"
            >
              <div class="flex items-center justify-between">
                <span class="text-xs text-slate-400">位置 {{ index + 1 }}</span>
                <button
                  class="w-5 h-5 rounded-full bg-slate-700 text-slate-400 hover:bg-red-500/20 hover:text-red-400 flex items-center justify-center"
                  @click="removeTargetGift(index)"
                >
                  <X class="w-3 h-3" />
                </button>
              </div>
              <div class="w-full aspect-square bg-slate-700 rounded-lg flex items-center justify-center overflow-hidden">
                <img
                  v-if="gift.giftIcon"
                  :src="gift.giftIcon"
                  :alt="gift.giftName"
                  class="w-12 h-12 object-contain"
                />
                <Gift v-else class="w-6 h-6 text-slate-500" />
              </div>
              <Input
                v-model="gift.giftName"
                class="bg-slate-900 border-slate-700 text-white text-sm"
                placeholder="礼物名称"
                readonly
              />
            </div>

            <!-- Empty slots -->
            <button
              v-for="slot in emptyGiftSlots"
              :key="'empty-' + slot"
              class="bg-slate-800/50 rounded-lg p-3 flex flex-col gap-2 border border-dashed border-slate-700 hover:border-slate-600 transition-colors"
              @click="openGiftSelector"
            >
              <span class="text-xs text-slate-500">位置 {{ targetGifts.targetGifts.length + slot }}</span>
              <div class="w-full aspect-square bg-slate-800 rounded-lg flex items-center justify-center">
                <Plus class="w-6 h-6 text-slate-500" />
              </div>
              <div class="h-8" />
            </button>
          </div>

        </CardContent>
      </Card>

      <!-- Widget Preview -->
      <Card class="bg-slate-900 border-slate-800">
        <CardHeader class="flex flex-row items-center justify-between">
          <CardTitle class="text-white flex items-center gap-2">
            <ExternalLink class="w-5 h-5 text-slate-400" />
            挂件预览
          </CardTitle>
          <Button
            variant="outline"
            size="sm"
            class="border-slate-700 text-slate-300"
            :disabled="!widgetPreviewUrl"
            @click="openWidgetPreview"
          >
            <ExternalLink class="w-4 h-4 mr-1" />
            打开预览
          </Button>
        </CardHeader>
        <CardContent>
          <div class="flex items-center gap-2">
            <Input
              :modelValue="widgetPreviewUrl || ''"
              readonly
              class="flex-1 bg-slate-900 border-slate-700 text-slate-300 text-sm"
            />
            <Button
              variant="outline"
              size="icon"
              class="border-slate-700"
              :disabled="!widgetPreviewUrl"
              @click="copyWidgetUrl"
            >
              <Copy class="w-4 h-4" />
            </Button>
          </div>
        </CardContent>
      </Card>

      <!-- Save Button -->
      <div class="flex justify-end mt-6">
        <Button class="bg-blue-600 hover:bg-blue-700" @click="handleSave">
          <Save class="w-4 h-4 mr-2" />
          保存配置
        </Button>
      </div>

      <!-- Gift Selector -->
      <GiftSelector
        v-model:open="isGiftSelectorOpen"
        :room-id="store.currentRoom?.tiktokId"
        @select="handleGiftSelect"
      />
    </template>
  </ModeConfigLayout>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import { useToast } from '@/hooks/use-toast'
import { connectTikTokLiveForRoom } from '@/lib/tiktokLive'
import { getServerBaseUrl } from '@/lib/api'
import { mapApiPreset } from '@/lib/mappers'
import { attachAnchorToPreset, detachAnchorFromPreset, updateGameConfig, createPreset, deletePreset } from '@/lib/presetsApi'
import ModeConfigLayout from '@/components/ModeConfigLayout.vue'
import GiftSelector from '@/components/GiftSelector.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Switch from '@/components/ui/Switch.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import type { StickerModeConfig, TargetGiftsConfig, TargetGift, Preset, GameMode } from '@/types'
import {
  Sticker,
  Settings2,
  Clock,
  Timer,
  FlipHorizontal,
  Sparkles,
  Plus,
  X,
  Gift,
  ExternalLink,
  Copy,
  Save,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const defaultConfig: StickerModeConfig = {
  type: 'normal',
  countdownEnabled: false,
  countdownDuration: 120,
  decayEnabled: false,
  decayDuration: 30,
  autoFlipEnabled: false,
  flipInterval: 5,
  maxPages: 2,
  gameplayGifts: [],
}

const defaultTargetGifts: TargetGiftsConfig = {
  targetGifts: [],
}

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T

// Config management
const modePresets = computed(() => store.presets.filter((preset) => preset.mode === 'sticker'))
const configs = computed(() =>
  modePresets.value.map((p) => ({ id: p.id, name: p.name }))
)

const activeConfigId = computed({
  get: () => store.presetSelections.sticker ?? '',
  set: (value: string) => {
    const preset = store.presets.find((item) => item.id === value) ?? null
    store.selectPresetForMode('sticker', preset)
  },
})

const currentPreset = computed<Preset | null>(() => {
  if (store.currentPreset?.mode === 'sticker') return store.currentPreset
  const presetId = store.presetSelections.sticker
  return presetId ? store.presets.find((item) => item.id === presetId) ?? null : null
})

// Local state
const config = ref<StickerModeConfig>(clone(defaultConfig))
const targetGifts = ref<TargetGiftsConfig>(clone(defaultTargetGifts))
const isRunning = ref(false)
const isTikTokConnecting = ref(false)
const isGiftSelectorOpen = ref(false)

const anchors = computed(() => currentPreset.value?.anchors ?? [])

const emptyGiftSlots = computed(() => {
  const total = 6
  const filled = targetGifts.value.targetGifts.length
  return Math.max(0, total - filled)
})

const widgetPreviewUrl = computed(() =>
  currentPreset.value?.widgetToken
    ? `${getServerBaseUrl()}/widget/${currentPreset.value.widgetToken}?mode=preset`
    : null
)

// Watch for preset changes
watch(
  () => currentPreset.value,
  (preset) => {
    const nextConfig = clone(preset?.config ?? defaultConfig) as StickerModeConfig
    config.value = {
      ...defaultConfig,
      ...nextConfig,
      gameplayGifts: (nextConfig as StickerModeConfig).gameplayGifts ?? [],
    }
    targetGifts.value = clone(preset?.targetGifts ?? defaultTargetGifts)
  },
  { immediate: true }
)

// Ensure default config exists
const ensureDefaultConfig = async () => {
  if (modePresets.value.length === 0) {
    try {
      const response = await createPreset({
        name: 'Config1',
        mode: 'sticker' as GameMode,
        config: defaultConfig,
        targetGifts: defaultTargetGifts,
      })
      if (response?.success && response.data) {
        const preset = mapApiPreset(response.data)
        store.addPreset(preset)
        store.selectPresetForMode('sticker', preset)
      }
    } catch (err) {
      console.error('Failed to create default config:', err)
    }
  }
}

ensureDefaultConfig()

// Config handlers
const handleConfigSelect = (configId: string) => {
  activeConfigId.value = configId
}

const handleConfigAdd = async () => {
  const nextNum = modePresets.value.length + 1
  const name = `Config${nextNum}`
  try {
    const response = await createPreset({
      name,
      mode: 'sticker' as GameMode,
      config: defaultConfig,
      targetGifts: defaultTargetGifts,
    })
    if (response?.success && response.data) {
      const preset = mapApiPreset(response.data)
      store.addPreset(preset)
      store.selectPresetForMode('sticker', preset)
      toast({ title: '成功', description: `已创建 ${name}` })
    }
  } catch (err) {
    toast({ title: '错误', description: '创建配置失败', variant: 'destructive' })
  }
}

const handleConfigRemove = async (configId: string) => {
  const preset = store.presets.find((p) => p.id === configId)
  if (!preset) return

  try {
    const response = await deletePreset(configId)
    if (response?.success) {
      const isCurrentSelected = activeConfigId.value === configId
      store.deletePreset(configId)
      // Select another preset if the current one was deleted
      if (isCurrentSelected) {
        const remaining = store.presets.filter((p) => p.mode === 'sticker')
        if (remaining.length > 0) {
          store.selectPresetForMode('sticker', remaining[0])
        } else {
          store.selectPresetForMode('sticker', null)
        }
      }
      toast({ title: '成功', description: '配置已删除' })
    }
  } catch (err) {
    toast({ title: '错误', description: '删除配置失败', variant: 'destructive' })
  }
}

// Anchor handlers
const handleAnchorAdd = async (anchorId: string) => {
  if (!currentPreset.value) return
  const response = await attachAnchorToPreset(currentPreset.value.id, {
    anchorId,
    displayOrder: currentPreset.value.anchors.length,
  })
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('sticker', updated)
  }
}

const handleAnchorRemove = async (anchorId: string) => {
  if (!currentPreset.value) return
  const response = await detachAnchorFromPreset(currentPreset.value.id, anchorId)
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('sticker', updated)
  }
}

const handleApplyToAll = () => {
  toast({ title: '提示', description: '应用到全部功能开发中' })
}

// Game handlers
const connectTikTok = async () => {
  if (isTikTokConnecting.value) return false
  const roomId = store.currentRoom?.tiktokId
  if (!roomId) {
    toast({ title: '错误', description: '请先连接直播间', variant: 'destructive' })
    return false
  }
  isTikTokConnecting.value = true
  const result = await connectTikTokLiveForRoom(roomId)
  isTikTokConnecting.value = false
  if (!result.success) {
    toast({ title: '错误', description: result.error || '连接失败', variant: 'destructive' })
    return false
  }
  return true
}

const handleStart = async () => {
  const connected = await connectTikTok()
  if (!connected) return
  isRunning.value = true
  toast({ title: '开始', description: '贴纸舞模式已启动' })
}

const handlePause = () => {
  isRunning.value = false
  toast({ title: '暂停', description: '贴纸舞模式已暂停' })
}

const handleReset = () => {
  isRunning.value = false
  toast({ title: '重置', description: '贴纸舞模式已重置' })
}

// Gift handlers
const openGiftSelector = () => {
  if (!store.currentRoom?.tiktokId) {
    toast({ title: '错误', description: '请先连接直播间', variant: 'destructive' })
    return
  }
  isGiftSelectorOpen.value = true
}

const handleGiftSelect = (gift: { giftId: string; giftName: string; giftIcon: string; diamondCost: number }) => {
  const next: TargetGift = {
    giftId: gift.giftId,
    giftName: gift.giftName,
    giftIcon: gift.giftIcon,
    diamondCost: gift.diamondCost,
    points: gift.diamondCost,
    isTarget: true
  }
  targetGifts.value.targetGifts.push(next)
}

const removeTargetGift = (index: number) => {
  targetGifts.value.targetGifts.splice(index, 1)
}

// Save handler
const handleSave = async () => {
  if (!currentPreset.value) return

  // Save config and target gifts in one request
  const configResponse = await updateGameConfig(currentPreset.value.id, {
    sticker: config.value,
    targetGifts: targetGifts.value.targetGifts,
  })

  if (configResponse?.success && configResponse.data) {
    const updated = mapApiPreset(configResponse.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('sticker', updated)
    toast({ title: '成功', description: '配置已保存' })
  } else {
    toast({ title: '错误', description: '保存失败', variant: 'destructive' })
  }
}

const copyWidgetUrl = () => {
  if (!widgetPreviewUrl.value) return
  navigator.clipboard.writeText(widgetPreviewUrl.value)
  toast({ title: '成功', description: '链接已复制' })
}

const openWidgetPreview = () => {
  if (!widgetPreviewUrl.value || !currentPreset.value) return
  window.electronAPI.widget.open(widgetPreviewUrl.value, currentPreset.value.name)
}
</script>
