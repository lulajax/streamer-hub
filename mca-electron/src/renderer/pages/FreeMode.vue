<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <Users class="w-6 h-6 text-green-400" />
          {{ t('freeMode') }}
        </h1>
        <p class="text-slate-400">Free rotation mode for talent showcase</p>
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
        <template v-if="isRunning">
          <Button variant="secondary" @click="handlePause">
            <Pause class="w-4 h-4 mr-2" />
            Pause
          </Button>
          <Button variant="destructive" @click="handleEndRound">
            <ChevronRight class="w-4 h-4 mr-2" />
            End Round
          </Button>
        </template>
        <Button v-else class="bg-green-600 hover:bg-green-700" @click="handleStart">
          <Play class="w-4 h-4 mr-2" />
          Start Round
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

    <!-- Current Round Info -->
    <Card class="bg-slate-900 border-slate-800">
      <CardContent class="p-6">
        <div class="flex items-center justify-between">
          <div class="flex items-center gap-6">
            <div v-if="config.showRoundNumber" class="text-center">
              <p class="text-slate-400 text-sm">Round</p>
              <p class="text-3xl font-bold text-white">{{ currentRound }}</p>
            </div>

            <div v-if="currentAnchor" class="flex items-center gap-4">
              <Avatar class="w-16 h-16">
                <AvatarImage :src="currentAnchor.avatar" />
                <AvatarFallback class="bg-gradient-to-br from-green-500 to-emerald-600 text-white text-xl">
                  {{ currentAnchor.name[0] }}
                </AvatarFallback>
              </Avatar>
              <div>
                <p class="text-slate-400 text-sm">Current Anchor</p>
                <p class="text-2xl font-bold text-white">
                  {{ showNames ? currentAnchor.name : '???' }}
                </p>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-6">
            <div class="text-center">
              <p class="text-slate-400 text-sm">Current Score</p>
              <p class="text-3xl font-bold text-green-400">
                {{ currentAnchor?.totalScore?.toLocaleString() || 0 }}
              </p>
            </div>

            <div class="text-center">
              <p class="text-slate-400 text-sm">Target</p>
              <p class="text-3xl font-bold text-amber-400">
                {{ config.targetScore.toLocaleString() }}
              </p>
            </div>

            <Badge v-if="currentAnchor && currentAnchor.totalScore >= config.targetScore" class="bg-green-500 text-white px-4 py-2">
              <Trophy class="w-4 h-4 mr-1" />
              Target Reached!
            </Badge>
          </div>
        </div>

        <!-- Progress Bar -->
        <div class="mt-6">
          <div class="h-4 bg-slate-800 rounded-full overflow-hidden">
            <div
              class="h-full bg-gradient-to-r from-green-500 to-emerald-500 transition-all duration-500"
              :style="{ width: `${Math.min(((currentAnchor?.totalScore || 0) / config.targetScore) * 100, 100)}%` }"
            />
          </div>
          <div class="flex justify-between mt-2 text-sm text-slate-400">
            <span>0</span>
            <span>{{ config.targetScore.toLocaleString() }}</span>
          </div>
        </div>
      </CardContent>
    </Card>

    <Tabs v-model="activeTab" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="anchors">Anchor Queue</TabsTrigger>
        <TabsTrigger value="history">Round History</TabsTrigger>
        <TabsTrigger value="config">Configuration</TabsTrigger>
        <TabsTrigger value="target-gifts">{{ t('targetGifts') }}</TabsTrigger>
        <TabsTrigger value="preview">Preview</TabsTrigger>
      </TabsList>

      <TabsContent value="anchors" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader class="flex flex-row items-center justify-between">
            <CardTitle class="text-white">Anchor Rotation Queue</CardTitle>
            <Button
              variant="ghost"
              size="sm"
              class="text-slate-400"
              @click="showNames = !showNames"
            >
              <Eye v-if="showNames" class="w-4 h-4 mr-1" />
              <EyeOff v-else class="w-4 h-4 mr-1" />
              {{ showNames ? 'Hide Names' : 'Show Names' }}
            </Button>
          </CardHeader>
          <CardContent>
            <div class="mb-4 space-y-2">
              <p class="text-sm text-slate-300">{{ t('linkedAnchors') }}</p>
              <div class="grid grid-cols-2 gap-2">
                <div
                  v-for="anchor in store.userAnchors"
                  :key="anchor.id"
                  class="flex items-center justify-between p-2 rounded-lg bg-slate-800/60"
                >
                  <div>
                    <p class="text-white text-sm font-medium">{{ anchor.name }}</p>
                    <p class="text-slate-500 text-xs">{{ anchor.tiktokId || '-' }}</p>
                  </div>
                  <Switch
                    :modelValue="isAnchorLinked(anchor.id)"
                    @update:modelValue="(value) => handleToggleAnchor(anchor.id, value)"
                  />
                </div>
              </div>
              <div v-if="store.userAnchors.length === 0" class="text-slate-500 text-center py-4">
                {{ t('noAnchors') }}
              </div>
            </div>
            <ScrollArea class="h-96">
              <div class="space-y-2">
                <div
                  v-for="(anchor, index) in store.anchors"
                  :key="anchor.id"
                  class="flex items-center gap-4 p-4 rounded-lg"
                  :class="{
                    'bg-green-500/20 border border-green-500/50': index === currentAnchorIndex,
                    'bg-slate-800/50 opacity-50': index < currentAnchorIndex,
                    'bg-slate-800': index > currentAnchorIndex
                  }"
                >
                  <div class="w-8 text-center">
                    <Play v-if="index === currentAnchorIndex" class="w-5 h-5 text-green-400 mx-auto" />
                    <span v-else class="text-slate-500 font-medium">{{ index + 1 }}</span>
                  </div>

                  <Avatar class="w-10 h-10">
                    <AvatarImage :src="anchor.avatar" />
                    <AvatarFallback class="bg-slate-700 text-white">
                      {{ anchor.name[0] }}
                    </AvatarFallback>
                  </Avatar>

                  <div class="flex-1">
                    <p :class="index === currentAnchorIndex ? 'text-green-400' : 'text-white'" class="font-medium">
                      {{ showNames ? anchor.name : '???' }}
                    </p>
                    <p class="text-slate-400 text-sm">
                      {{ anchor.exclusiveGifts?.length || 0 }} exclusive gifts
                    </p>
                  </div>

                  <div class="text-right">
                    <p class="text-white font-bold">{{ anchor.totalScore?.toLocaleString() || 0 }}</p>
                    <p class="text-slate-400 text-xs">points</p>
                  </div>

                  <Badge v-if="index === currentAnchorIndex" class="bg-green-500 text-white">
                    <Clock class="w-3 h-3 mr-1" />
                    Current
                  </Badge>
                </div>

                <div v-if="store.anchors.length === 0" class="text-center py-8 text-slate-500">
                  <Users class="w-12 h-12 mx-auto mb-3 opacity-50" />
                  <p>No anchors configured</p>
                  <p class="text-sm">Add anchors in the Dashboard</p>
                </div>
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="history" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">Round History</CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea class="h-96">
              <div class="space-y-2">
                <div
                  v-for="(record, index) in roundHistory"
                  :key="index"
                  class="flex items-center gap-4 p-4 bg-slate-800 rounded-lg"
                >
                  <div class="w-8 text-center">
                    <span class="text-slate-500 font-medium">{{ record.round }}</span>
                  </div>

                  <Avatar class="w-10 h-10">
                    <AvatarImage :src="getAnchorById(record.anchorId)?.avatar" />
                    <AvatarFallback class="bg-slate-700 text-white">
                      {{ getAnchorById(record.anchorId)?.name?.[0] || '?' }}
                    </AvatarFallback>
                  </Avatar>

                  <div class="flex-1">
                    <p class="text-white font-medium">{{ getAnchorById(record.anchorId)?.name || 'Unknown' }}</p>
                    <p class="text-slate-400 text-sm">{{ new Date().toLocaleDateString() }}</p>
                  </div>

                  <div class="text-right">
                    <p class="text-white font-bold">{{ record.score.toLocaleString() }}</p>
                    <p class="text-slate-400 text-xs">points</p>
                  </div>

                  <Badge v-if="record.targetReached" class="bg-green-500 text-white">
                    <Trophy class="w-3 h-3 mr-1" />
                    Target Met
                  </Badge>
                  <Badge v-else variant="secondary" class="text-slate-400">Not Met</Badge>
                </div>

                <div v-if="roundHistory.length === 0" class="text-center py-8 text-slate-500">
                  <Clock class="w-12 h-12 mx-auto mb-3 opacity-50" />
                  <p>No round history yet</p>
                  <p class="text-sm">Complete rounds to see history</p>
                </div>
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="config" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Settings2 class="w-5 h-5 text-amber-400" />
              Free Mode Settings
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-6">
            <!-- Round Duration -->
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Clock class="w-5 h-5 text-blue-400" />
                <div>
                  <p class="text-white font-medium">Round Duration</p>
                  <p class="text-slate-400 text-sm">Time limit for each anchor</p>
                </div>
              </div>
              <Input
                v-model.number="config.roundDuration"
                type="number"
                class="w-24 bg-slate-900 border-slate-700 text-white"
              />
            </div>

            <!-- Target Score -->
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Target class="w-5 h-5 text-green-400" />
                <div>
                  <p class="text-white font-medium">Target Score</p>
                  <p class="text-slate-400 text-sm">Score needed to reach target</p>
                </div>
              </div>
              <Input
                v-model.number="config.targetScore"
                type="number"
                class="w-32 bg-slate-900 border-slate-700 text-white"
              />
            </div>

            <!-- Show Round Number -->
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Trophy class="w-5 h-5 text-amber-400" />
                <div>
                  <p class="text-white font-medium">Show Round Number</p>
                  <p class="text-slate-400 text-sm">Display current round in widget</p>
                </div>
              </div>
              <Switch v-model="config.showRoundNumber" />
            </div>

            <!-- Display Range -->
            <div class="p-4 bg-slate-800 rounded-lg">
              <p class="text-white font-medium mb-3">Leaderboard Display Range</p>
              <div class="flex items-center gap-4">
                <Input
                  v-model.number="config.displayRange[0]"
                  type="number"
                  class="w-20 bg-slate-900 border-slate-700 text-white"
                />
                <span class="text-slate-400">to</span>
                <Input
                  v-model.number="config.displayRange[1]"
                  type="number"
                  class="w-20 bg-slate-900 border-slate-700 text-white"
                />
              </div>
            </div>

            <Button class="bg-blue-600 hover:bg-blue-700 w-fit" @click="handleSaveConfig">
              {{ t('savePresetConfig') }}
            </Button>
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
                  <Users class="w-16 h-16 mx-auto mb-4 opacity-50" />
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
import Badge from '@/components/ui/Badge.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import ScrollArea from '@/components/ui/ScrollArea.vue'
import Avatar from '@/components/ui/Avatar.vue'
import AvatarImage from '@/components/ui/AvatarImage.vue'
import AvatarFallback from '@/components/ui/AvatarFallback.vue'
import Switch from '@/components/ui/Switch.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import type { FreeModeConfig, TargetGiftsConfig, TargetGift, Preset } from '@/types'
import {
  Users,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Clock,
  Target,
  Eye,
  EyeOff,
  ChevronRight,
  Trophy,
  Copy,
  ExternalLink,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const defaultConfig: FreeModeConfig = {
  roundDuration: 300,
  targetScore: 10000,
  showRoundNumber: true,
  displayRange: [1, 10],
}

const defaultTargetGifts: TargetGiftsConfig = {
  targetGifts: [],
  scoringRules: {
    mode: 'POINTS',
    multiplier: 1,
  },
}

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T

const modePresets = computed(() => store.presets.filter((preset) => preset.mode === 'free'))

const selectedPresetId = computed({
  get: () => store.presetSelections.free ?? '',
  set: (value: string) => {
    const preset = store.presets.find((item) => item.id === value) ?? null
    store.selectPresetForMode('free', preset)
  },
})

const selectedPreset = computed<Preset | null>(() => {
  if (store.currentPreset?.mode === 'free') return store.currentPreset
  const presetId = store.presetSelections.free
  return presetId ? store.presets.find((item) => item.id === presetId) ?? null : null
})

const config = ref<FreeModeConfig>(clone(selectedPreset.value?.config ?? defaultConfig))
const targetGifts = ref<TargetGiftsConfig>(clone(selectedPreset.value?.targetGifts ?? defaultTargetGifts))

const isRunning = ref(false)
const currentRound = ref(1)
const currentAnchorIndex = ref(0)
const showNames = ref(true)
const activeTab = ref('anchors')
const isTikTokConnecting = ref(false)

const roundHistory = ref<
  { round: number; anchorId: string; score: number; targetReached: boolean }[]
>([])

const widgetPreviewUrl = computed(() =>
  selectedPreset.value?.widgetToken
    ? `${getServerBaseUrl()}/widget/${selectedPreset.value.widgetToken}?mode=preset`
    : null
)

watch(
  () => selectedPreset.value,
  (preset) => {
    const nextConfig = clone(preset?.config ?? defaultConfig) as FreeModeConfig
    config.value = {
      ...defaultConfig,
      ...nextConfig,
      displayRange: nextConfig.displayRange ?? defaultConfig.displayRange,
    }
    targetGifts.value = clone(preset?.targetGifts ?? defaultTargetGifts)
    if (!targetGifts.value.scoringRules) {
      targetGifts.value.scoringRules = { mode: 'POINTS', multiplier: 1 }
    }
    currentRound.value = 1
    currentAnchorIndex.value = 0
    roundHistory.value = []
  },
  { immediate: true }
)

const currentAnchor = computed(() => store.anchors[currentAnchorIndex.value])

const getAnchorById = (id: string) => store.anchors.find((a) => a.id === id)

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
  if (store.anchors.length === 0) {
    toast({
      title: 'Error',
      description: 'Please add at least one anchor',
      variant: 'destructive',
    })
    return
  }
  const connected = await connectTikTok()
  if (!connected) return
  isRunning.value = true
  toast({
    title: 'Free Mode Started',
    description: `Round ${currentRound.value} - ${store.anchors[currentAnchorIndex.value]?.name}`,
  })
}

const handlePause = () => {
  isRunning.value = false
  toast({
    title: 'Free Mode Paused',
  })
}

const handleEndRound = () => {
  if (store.anchors.length === 0) return
  const anchor = store.anchors[currentAnchorIndex.value]
  if (anchor) {
    roundHistory.value.push({
      round: currentRound.value,
      anchorId: anchor.id,
      score: anchor.totalScore,
      targetReached: anchor.totalScore >= config.value.targetScore,
    })
  }

  isRunning.value = false
  currentRound.value++
  currentAnchorIndex.value = (currentAnchorIndex.value + 1) % store.anchors.length

  toast({
    title: 'Round Ended',
    description: `Next: ${store.anchors[currentAnchorIndex.value]?.name}`,
  })
}

const handleReset = () => {
  isRunning.value = false
  currentRound.value = 1
  currentAnchorIndex.value = 0
  roundHistory.value = []
  toast({
    title: 'Free Mode Reset',
  })
}

const handleSaveConfig = async () => {
  if (!selectedPreset.value) return
  const response = await updateGameConfig(selectedPreset.value.id, {
    free: config.value,
  })
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('free', updated)
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
    store.selectPresetForMode('free', updated)
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
      store.selectPresetForMode('free', updated)
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
      store.selectPresetForMode('free', updated)
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
</script>
