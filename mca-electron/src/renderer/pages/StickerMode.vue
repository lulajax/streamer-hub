<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <Sticker class="w-6 h-6 text-pink-400" />
          {{ t('stickerMode') }}
        </h1>
        <p class="text-slate-400">Configure sticker dance mode settings</p>
      </div>
      <div class="flex items-center gap-3">
        <Button
          variant="outline"
          class="border-slate-700 text-slate-300"
          @click="handleReset"
        >
          <RotateCcw class="w-4 h-4 mr-2" />
          Reset
        </Button>
        <Button v-if="isRunning" variant="secondary" @click="handlePause">
          <Pause class="w-4 h-4 mr-2" />
          Pause
        </Button>
        <Button v-else class="bg-green-600 hover:bg-green-700" @click="handleStart">
          <Play class="w-4 h-4 mr-2" />
          Start
        </Button>
      </div>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardContent class="p-4 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <div>
          <p class="text-slate-400 text-sm">{{ t('presetForMode') }}</p>
          <div v-if="modePresets.length > 0" class="mt-2">
            <Select v-model="selectedPresetId">
              <SelectTrigger class="w-64 bg-slate-800 border-slate-700 text-white">
                <SelectValue :placeholder="t('selectPreset')" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem v-for="preset in modePresets" :key="preset.id" :value="preset.id">
                  {{ preset.name }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
          <p v-else class="text-slate-500 mt-2">{{ t('noPresetsForMode') }}</p>
        </div>
        <Button variant="outline" class="border-slate-700 text-slate-300" @click="goToPresets">
          {{ t('managePresets') }}
        </Button>
      </CardContent>
    </Card>

    <Tabs v-model="activeTab" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="config">Configuration</TabsTrigger>
        <TabsTrigger value="anchors">Anchors</TabsTrigger>
        <TabsTrigger value="target-gifts">{{ t('targetGifts') }}</TabsTrigger>
        <TabsTrigger value="gameplay">Gameplay Gifts</TabsTrigger>
        <TabsTrigger value="preview">Preview</TabsTrigger>
      </TabsList>

      <TabsContent value="config" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Settings2 class="w-5 h-5 text-blue-400" />
              Mode Settings
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-6">
            <!-- Mode Type -->
            <div>
              <label class="text-sm font-medium text-slate-300 mb-2 block">Mode Type</label>
              <div class="flex gap-4">
                <button
                  class="flex-1 p-4 rounded-lg border transition-colors"
                  :class="config.type === 'normal' ? 'border-blue-500 bg-blue-500/10' : 'border-slate-700 bg-slate-800'"
                  @click="config.type = 'normal'"
                >
                  <Sticker class="w-6 h-6 text-blue-400 mb-2" />
                  <p class="text-white font-medium">Normal Mode</p>
                  <p class="text-slate-400 text-sm">Show stickers without counting</p>
                </button>
                <button
                  class="flex-1 p-4 rounded-lg border transition-colors"
                  :class="config.type === 'scoring' ? 'border-purple-500 bg-purple-500/10' : 'border-slate-700 bg-slate-800'"
                  @click="config.type = 'scoring'"
                >
                  <Sparkles class="w-6 h-6 text-purple-400 mb-2" />
                  <p class="text-white font-medium">Scoring Mode</p>
                  <p class="text-slate-400 text-sm">Count gifts with timer/decay</p>
                </button>
              </div>
            </div>

            <template v-if="config.type === 'scoring'">
              <!-- Countdown -->
              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <Clock class="w-5 h-5 text-amber-400" />
                  <div>
                    <p class="text-white font-medium">Countdown Timer</p>
                    <p class="text-slate-400 text-sm">Reset scores after countdown</p>
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <Input
                    v-if="config.countdownEnabled"
                    v-model.number="config.countdownDuration"
                    type="number"
                    class="w-20 bg-slate-900 border-slate-700 text-white"
                  />
                  <Switch v-model="config.countdownEnabled" />
                </div>
              </div>

              <!-- Decay -->
              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <Timer class="w-5 h-5 text-red-400" />
                  <div>
                    <p class="text-white font-medium">Score Decay</p>
                    <p class="text-slate-400 text-sm">Clear scores if no gifts received</p>
                  </div>
                </div>
                <div class="flex items-center gap-4">
                  <Input
                    v-if="config.decayEnabled"
                    v-model.number="config.decayDuration"
                    type="number"
                    class="w-20 bg-slate-900 border-slate-700 text-white"
                  />
                  <Switch v-model="config.decayEnabled" />
                </div>
              </div>
            </template>

            <!-- Auto Flip -->
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <FlipHorizontal class="w-5 h-5 text-green-400" />
                <div>
                  <p class="text-white font-medium">Auto Page Flip</p>
                  <p class="text-slate-400 text-sm">Automatically switch between pages</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <Input
                  v-if="config.autoFlipEnabled"
                  v-model.number="config.flipInterval"
                  type="number"
                  class="w-20 bg-slate-900 border-slate-700 text-white"
                  placeholder="Seconds"
                />
                <Switch v-model="config.autoFlipEnabled" />
              </div>
            </div>

            <Button class="bg-blue-600 hover:bg-blue-700 w-fit" @click="handleSaveConfig">
              {{ t('savePresetConfig') }}
            </Button>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="anchors" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">{{ t('linkedAnchors') }}</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-2">
              <div
                v-for="anchor in store.userAnchors"
                :key="anchor.id"
                class="flex items-center justify-between p-3 rounded-lg bg-slate-800/60"
              >
                <div>
                  <p class="text-white font-medium">{{ anchor.name }}</p>
                  <p class="text-slate-400 text-xs">{{ anchor.tiktokId || '-' }}</p>
                </div>
                <Switch
                  :modelValue="isAnchorLinked(anchor.id)"
                  @update:modelValue="(value) => handleToggleAnchor(anchor.id, value)"
                />
              </div>

              <div v-if="store.userAnchors.length === 0" class="text-slate-500 text-center py-6">
                {{ t('noAnchors') }}
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="target-gifts" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">{{ t('targetGifts') }}</CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="space-y-3">
              <div
                v-for="(gift, index) in targetGifts.targetGifts"
                :key="index"
                class="grid grid-cols-6 gap-3 items-center bg-slate-800/60 p-3 rounded-lg"
              >
                <Input v-model="gift.giftId" class="bg-slate-900 border-slate-700 text-white" :placeholder="t('giftId')" />
                <Input v-model="gift.giftName" class="bg-slate-900 border-slate-700 text-white" :placeholder="t('giftName')" />
                <Input v-model.number="gift.diamondCost" type="number" class="bg-slate-900 border-slate-700 text-white" :placeholder="t('diamondCost')" />
                <Input v-model.number="gift.points" type="number" class="bg-slate-900 border-slate-700 text-white" :placeholder="t('points')" />
                <div class="flex items-center gap-2">
                  <Switch v-model="gift.isTarget" />
                  <span class="text-xs text-slate-400">{{ t('isTarget') }}</span>
                </div>
                <Button
                  variant="outline"
                  class="border-red-500/40 text-red-400 hover:text-red-300"
                  @click="removeTargetGift(index)"
                >
                  {{ t('remove') }}
                </Button>
              </div>

              <div v-if="targetGifts.targetGifts.length === 0" class="text-slate-500 text-center py-6">
                {{ t('noTargetGifts') }}
              </div>
            </div>

            <div class="flex gap-3">
              <Button variant="outline" class="border-slate-700 text-slate-300" @click="addTargetGift">
                {{ t('addTargetGift') }}
              </Button>
            </div>

            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('scoringMode') }}</label>
                <Select v-model="targetGifts.scoringRules.mode">
                  <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="DIAMOND">DIAMOND</SelectItem>
                    <SelectItem value="COUNT">COUNT</SelectItem>
                    <SelectItem value="POINTS">POINTS</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div>
                <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('multiplier') }}</label>
                <Input v-model.number="targetGifts.scoringRules.multiplier" type="number" class="bg-slate-900 border-slate-700 text-white" />
              </div>
            </div>

            <Button class="bg-blue-600 hover:bg-blue-700" @click="handleSaveTargetGifts">
              {{ t('saveTargetGifts') }}
            </Button>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="gameplay" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center justify-between">
              <span>Gameplay Gifts</span>
              <Button size="sm" @click="addGameplayGift">
                <Plus class="w-4 h-4 mr-2" />
                Add Gift
              </Button>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-3">
              <div
                v-for="(gift, index) in config.gameplayGifts"
                :key="index"
                class="flex items-center gap-4 p-4 bg-slate-800 rounded-lg"
              >
                <select
                  v-model="gift.giftId"
                  class="w-48 bg-slate-900 border-slate-700 text-white rounded px-3 py-2"
                >
                  <option value="">Select gift</option>
                  <option value="gift1">Rose</option>
                  <option value="gift2">TikTok</option>
                  <option value="gift3">Heart</option>
                  <option value="gift4">Crown</option>
                </select>

                <select
                  v-model="gift.effect"
                  class="w-40 bg-slate-900 border-slate-700 text-white rounded px-3 py-2"
                >
                  <option value="bounce">Bounce</option>
                  <option value="shake">Shake</option>
                  <option value="glow">Glow</option>
                </select>

                <Button
                  variant="ghost"
                  size="icon"
                  class="text-red-400 hover:text-red-300 ml-auto"
                  @click="removeGameplayGift(index)"
                >
                  <Trash2 class="w-4 h-4" />
                </Button>
              </div>

              <div v-if="config.gameplayGifts.length === 0" class="text-center py-8 text-slate-500">
                <Sparkles class="w-12 h-12 mx-auto mb-3 opacity-50" />
                <p>No gameplay gifts configured</p>
                <p class="text-sm">Add gifts with special effects</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="preview" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">Widget Preview</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="space-y-4">
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
                <Button
                  variant="outline"
                  size="icon"
                  class="border-slate-700"
                  :disabled="!widgetPreviewUrl"
                  @click="openWidgetPreview"
                >
                  <ExternalLink class="w-4 h-4" />
                </Button>
              </div>

              <div class="aspect-video bg-slate-800 rounded-lg flex items-center justify-center">
                <div class="text-center text-slate-500">
                  <Sticker class="w-16 h-16 mx-auto mb-4 opacity-50" />
                  <p>{{ widgetPreviewUrl ? t('widgetPreviewHint') : t('noPresetSelected') }}</p>
                  <p class="text-sm">{{ t('widgetPreviewTip') }}</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import { useToast } from '@/hooks/use-toast'
import { connectTikTokLiveForRoom } from '@/lib/tiktokLive'
import { getServerBaseUrl } from '@/lib/api'
import { mapApiPreset } from '@/lib/mappers'
import { attachAnchorToPreset, detachAnchorFromPreset, updateGameConfig, updateTargetGifts } from '@/lib/presetsApi'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import Switch from '@/components/ui/Switch.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import type { StickerModeConfig, GameplayGift, TargetGiftsConfig, TargetGift, Preset } from '@/types'
import {
  Sticker,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Clock,
  Timer,
  FlipHorizontal,
  Sparkles,
  Copy,
  ExternalLink,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const defaultConfig: StickerModeConfig = {
  type: 'normal',
  countdownEnabled: false,
  countdownDuration: 60,
  decayEnabled: false,
  decayDuration: 30,
  autoFlipEnabled: false,
  flipInterval: 10,
  maxPages: 2,
  gameplayGifts: [],
}

const defaultTargetGifts: TargetGiftsConfig = {
  targetGifts: [],
  scoringRules: {
    mode: 'POINTS',
    multiplier: 1,
  },
}

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T

const modePresets = computed(() => store.presets.filter((preset) => preset.mode === 'sticker'))

const selectedPresetId = computed({
  get: () => store.presetSelections.sticker ?? '',
  set: (value: string) => {
    const preset = store.presets.find((item) => item.id === value) ?? null
    store.selectPresetForMode('sticker', preset)
  },
})

const selectedPreset = computed<Preset | null>(() => {
  if (store.currentPreset?.mode === 'sticker') return store.currentPreset
  const presetId = store.presetSelections.sticker
  return presetId ? store.presets.find((item) => item.id === presetId) ?? null : null
})

const config = ref<StickerModeConfig>(clone(selectedPreset.value?.config ?? defaultConfig))
const targetGifts = ref<TargetGiftsConfig>(clone(selectedPreset.value?.targetGifts ?? defaultTargetGifts))

const isRunning = ref(false)
const activeTab = ref('config')
const isTikTokConnecting = ref(false)

const widgetPreviewUrl = computed(() =>
  selectedPreset.value?.widgetToken
    ? `${getServerBaseUrl()}/widget/${selectedPreset.value.widgetToken}?mode=preset`
    : null
)

watch(
  () => selectedPreset.value,
  (preset) => {
    const nextConfig = clone(preset?.config ?? defaultConfig)
    config.value = {
      ...defaultConfig,
      ...nextConfig,
      gameplayGifts: nextConfig.gameplayGifts ?? [],
    }
    targetGifts.value = clone(preset?.targetGifts ?? defaultTargetGifts)
    if (!targetGifts.value.scoringRules) {
      targetGifts.value.scoringRules = { mode: 'POINTS', multiplier: 1 }
    }
  },
  { immediate: true }
)

const connectTikTok = async () => {
  if (isTikTokConnecting.value) return false

  const roomId = store.currentRoom?.tiktokId
  if (!roomId) {
    toast({
      title: 'Error',
      description: 'Please connect a TikTok room first',
      variant: 'destructive',
    })
    return false
  }

  isTikTokConnecting.value = true
  const result = await connectTikTokLiveForRoom(roomId)
  isTikTokConnecting.value = false

  if (!result.success) {
    toast({
      title: 'Error',
      description: result.error || 'Failed to connect to TikTok Live',
      variant: 'destructive',
    })
    return false
  }

  toast({
    title: 'TikTok Live Connected',
    description: `Listening to @${roomId}`,
  })

  return true
}

const handleStart = async () => {
  const connected = await connectTikTok()
  if (!connected) return
  isRunning.value = true
  toast({
    title: 'Sticker Mode Started',
    description: `Mode: ${config.value.type === 'normal' ? 'Normal' : 'Scoring'}`,
  })
}

const handlePause = () => {
  isRunning.value = false
  toast({
    title: 'Sticker Mode Paused',
  })
}

const handleReset = () => {
  isRunning.value = false
  toast({
    title: 'Sticker Mode Reset',
  })
}

const handleSaveConfig = async () => {
  if (!selectedPreset.value) return
  const response = await updateGameConfig(selectedPreset.value.id, {
    sticker: config.value,
  })
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('sticker', updated)
    toast({ title: t('success'), description: t('presetUpdated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const addTargetGift = () => {
  const next: TargetGift = { giftId: '', giftName: '', diamondCost: 0, points: 0, isTarget: true }
  targetGifts.value.targetGifts.push(next)
}

const removeTargetGift = (index: number) => {
  targetGifts.value.targetGifts.splice(index, 1)
}

const handleSaveTargetGifts = async () => {
  if (!selectedPreset.value) return
  const response = await updateTargetGifts(selectedPreset.value.id, {
    targetGifts: targetGifts.value.targetGifts,
    scoringRules: targetGifts.value.scoringRules,
  })
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('sticker', updated)
    toast({ title: t('success'), description: t('presetUpdated') })
  } else {
    toast({
      title: t('error'),
      description: response?.error || t('networkError'),
      variant: 'destructive',
    })
  }
}

const isAnchorLinked = (anchorId: string) =>
  selectedPreset.value?.anchors.some((anchor) => anchor.id === anchorId) ?? false

const handleToggleAnchor = async (anchorId: string, value: boolean) => {
  if (!selectedPreset.value) return
  if (value) {
    const response = await attachAnchorToPreset(selectedPreset.value.id, {
      anchorId,
      displayOrder: selectedPreset.value.anchors.length,
    })
    if (response?.success && response.data) {
      const updated = mapApiPreset(response.data)
      store.updatePreset(updated.id, updated)
      store.selectPresetForMode('sticker', updated)
    } else {
      toast({
        title: t('error'),
        description: response?.error || t('networkError'),
        variant: 'destructive',
      })
    }
  } else {
    const response = await detachAnchorFromPreset(selectedPreset.value.id, anchorId)
    if (response?.success && response.data) {
      const updated = mapApiPreset(response.data)
      store.updatePreset(updated.id, updated)
      store.selectPresetForMode('sticker', updated)
    } else {
      toast({
        title: t('error'),
        description: response?.error || t('networkError'),
        variant: 'destructive',
      })
    }
  }
}

const copyWidgetUrl = () => {
  if (!widgetPreviewUrl.value) return
  navigator.clipboard.writeText(widgetPreviewUrl.value)
  toast({ title: t('success'), description: t('widgetLinkCopied') })
}

const openWidgetPreview = () => {
  if (!widgetPreviewUrl.value || !selectedPreset.value) return
  window.electronAPI.widget.open(widgetPreviewUrl.value, selectedPreset.value.name)
}

const goToPresets = () => {
  store.setActiveTab('presets')
}

const addGameplayGift = () => {
  const newGift: GameplayGift = {
    giftId: '',
    effect: 'bounce',
  }
  config.value.gameplayGifts.push(newGift)
}

const removeGameplayGift = (index: number) => {
  config.value.gameplayGifts.splice(index, 1)
}
</script>
