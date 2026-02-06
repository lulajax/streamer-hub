<template>
  <ModeConfigLayout
    :title="t('freeMode')"
    :icon="Users"
    icon-class="text-green-400"
    :configs="configs"
    :active-config-id="activeConfigId"
    :anchors="anchors"
    :user-anchors="store.userAnchors"
    :is-running="isRunning"
    :show-end-round="true"
    :start-button-text="'开始轮次'"
    :start-button-class="'bg-green-600 hover:bg-green-700'"
    @config-select="handleConfigSelect"
    @config-add="handleConfigAdd"
    @config-remove="handleConfigRemove"
    @anchor-add="handleAnchorAdd"
    @anchor-remove="handleAnchorRemove"
    @apply-to-all="handleApplyToAll"
    @start="handleStart"
    @pause="handlePause"
    @reset="handleReset"
    @end-round="handleEndRound"
  >
    <template #config>
      <!-- Current Round Info -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardContent class="p-6">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-6">
              <div v-if="config.showRoundNumber" class="text-center">
                <p class="text-slate-400 text-sm">回合</p>
                <p class="text-3xl font-bold text-white">{{ currentRound }}</p>
              </div>

              <div v-if="currentAnchor" class="flex items-center gap-4">
                <Avatar class="w-16 h-16">
                  <AvatarImage :src="currentAnchor.avatar || ''" />
                  <AvatarFallback class="bg-gradient-to-br from-green-500 to-emerald-600 text-white text-xl">
                    {{ currentAnchor.name.charAt(0) }}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <p class="text-slate-400 text-sm">当前主播</p>
                  <p class="text-2xl font-bold text-white">
                    {{ showNames ? currentAnchor.name : '???' }}
                  </p>
                </div>
              </div>
            </div>

            <div class="flex items-center gap-6">
              <div class="text-center">
                <p class="text-slate-400 text-sm">当前分数</p>
                <p class="text-3xl font-bold text-green-400">
                  {{ currentAnchor?.totalScore?.toLocaleString() || 0 }}
                </p>
              </div>

              <div class="text-center">
                <p class="text-slate-400 text-sm">目标</p>
                <p class="text-3xl font-bold text-amber-400">
                  {{ config.targetScore.toLocaleString() }}
                </p>
              </div>

              <Badge v-if="currentAnchor && currentAnchor.totalScore >= config.targetScore" class="bg-green-500 text-white px-4 py-2">
                <Trophy class="w-4 h-4 mr-1" />
                目标达成！
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

      <!-- Anchor Queue -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader class="flex flex-row items-center justify-between">
          <CardTitle class="text-white flex items-center gap-2">
            <ListOrdered class="w-5 h-5 text-blue-400" />
            主播队列
          </CardTitle>
          <Button
            variant="ghost"
            size="sm"
            class="text-slate-400"
            @click="showNames = !showNames"
          >
            <Eye v-if="showNames" class="w-4 h-4 mr-1" />
            <EyeOff v-else class="w-4 h-4 mr-1" />
            {{ showNames ? '隐藏名称' : '显示名称' }}
          </Button>
        </CardHeader>
        <CardContent>
          <div class="space-y-2">
            <div
              v-for="(anchor, index) in anchors"
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
                <AvatarImage :src="anchor.avatar || ''" />
                <AvatarFallback class="bg-slate-700 text-white">
                  {{ anchor.name.charAt(0) }}
                </AvatarFallback>
              </Avatar>

              <div class="flex-1">
                <p :class="index === currentAnchorIndex ? 'text-green-400' : 'text-white'" class="font-medium">
                  {{ showNames ? anchor.name : '???' }}
                </p>
                <p class="text-slate-400 text-sm">
                  {{ anchor.exclusiveGifts?.length || 0 }} 个专属礼物
                </p>
              </div>

              <div class="text-right">
                <p class="text-white font-bold">{{ anchor.totalScore?.toLocaleString() || 0 }}</p>
                <p class="text-slate-400 text-xs">分</p>
              </div>

              <Badge v-if="index === currentAnchorIndex" class="bg-green-500 text-white">
                <Clock class="w-3 h-3 mr-1" />
                当前
              </Badge>
            </div>

            <div v-if="anchors.length === 0" class="text-center py-8 text-slate-500">
              <Users class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>暂无主播</p>
              <p class="text-sm">请从左侧添加主播</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Round History -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <History class="w-5 h-5 text-amber-400" />
            回合历史
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-2 max-h-64 overflow-y-auto">
            <div
              v-for="(record, index) in roundHistory"
              :key="index"
              class="flex items-center gap-4 p-4 bg-slate-800 rounded-lg"
            >
              <div class="w-8 text-center">
                <span class="text-slate-500 font-medium">{{ record.round }}</span>
              </div>

              <Avatar class="w-10 h-10">
                <AvatarImage :src="getAnchorById(record.anchorId)?.avatar || ''" />
                <AvatarFallback class="bg-slate-700 text-white">
                  {{ getAnchorById(record.anchorId)?.name?.charAt(0) || '?' }}
                </AvatarFallback>
              </Avatar>

              <div class="flex-1">
                <p class="text-white font-medium">{{ getAnchorById(record.anchorId)?.name || '未知' }}</p>
                <p class="text-slate-400 text-sm">{{ new Date().toLocaleDateString() }}</p>
              </div>

              <div class="text-right">
                <p class="text-white font-bold">{{ record.score.toLocaleString() }}</p>
                <p class="text-slate-400 text-xs">分</p>
              </div>

              <Badge v-if="record.targetReached" class="bg-green-500 text-white">
                <Trophy class="w-3 h-3 mr-1" />
                达成目标
              </Badge>
              <Badge v-else variant="secondary" class="text-slate-400">未达成</Badge>
            </div>

            <div v-if="roundHistory.length === 0" class="text-center py-8 text-slate-500">
              <Clock class="w-12 h-12 mx-auto mb-3 opacity-50" />
              <p>暂无历史记录</p>
              <p class="text-sm">完成回合后将显示历史</p>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Free Mode Settings -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Settings2 class="w-5 h-5 text-amber-400" />
            自由模式设置
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <!-- Round Duration -->
          <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
            <div class="flex items-center gap-3">
              <Clock class="w-5 h-5 text-blue-400" />
              <div>
                <p class="text-white font-medium">回合时长</p>
                <p class="text-slate-400 text-sm">每位主播的时间限制</p>
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
                <p class="text-white font-medium">目标分数</p>
                <p class="text-slate-400 text-sm">达到目标所需的分数</p>
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
                <p class="text-white font-medium">显示回合数</p>
                <p class="text-slate-400 text-sm">在挂件中显示当前回合</p>
              </div>
            </div>
            <Switch v-model="config.showRoundNumber" />
          </div>

          <!-- Display Range -->
          <div class="p-4 bg-slate-800 rounded-lg">
            <p class="text-white font-medium mb-3">排行榜显示范围</p>
            <div class="flex items-center gap-4">
              <Input
                v-model.number="config.displayRange[0]"
                type="number"
                class="w-20 bg-slate-900 border-slate-700 text-white"
              />
              <span class="text-slate-400">到</span>
              <Input
                v-model.number="config.displayRange[1]"
                type="number"
                class="w-20 bg-slate-900 border-slate-700 text-white"
              />
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
          <div class="space-y-3">
            <div
              v-for="(gift, index) in targetGifts.targetGifts"
              :key="index"
              class="grid grid-cols-6 gap-3 items-center bg-slate-800/60 p-3 rounded-lg"
            >
              <div class="flex items-center gap-2">
                <img v-if="gift.giftIcon" :src="gift.giftIcon" class="w-8 h-8 object-contain" />
                <Input v-model="gift.giftId" class="bg-slate-900 border-slate-700 text-white flex-1" placeholder="礼物ID" readonly />
              </div>
              <Input v-model="gift.giftName" class="bg-slate-900 border-slate-700 text-white" placeholder="礼物名称" readonly />
              <Input v-model.number="gift.diamondCost" type="number" class="bg-slate-900 border-slate-700 text-white" placeholder="钻石值" />
              <Input v-model.number="gift.points" type="number" class="bg-slate-900 border-slate-700 text-white" placeholder="积分" />
              <div class="flex items-center gap-2">
                <Switch v-model="gift.isTarget" />
                <span class="text-xs text-slate-400">目标</span>
              </div>
              <Button
                variant="outline"
                class="border-red-500/40 text-red-400 hover:text-red-300"
                @click="removeTargetGift(index)"
              >
                删除
              </Button>
            </div>

            <div v-if="targetGifts.targetGifts.length === 0" class="text-slate-500 text-center py-6">
              暂无目标礼物
            </div>
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
import Badge from '@/components/ui/Badge.vue'
import Avatar from '@/components/ui/Avatar.vue'
import AvatarImage from '@/components/ui/AvatarImage.vue'
import AvatarFallback from '@/components/ui/AvatarFallback.vue'
import Switch from '@/components/ui/Switch.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import type { FreeModeConfig, TargetGiftsConfig, TargetGift, Preset, GameMode } from '@/types'
import {
  Users,
  Play,
  Clock,
  Target,
  Eye,
  EyeOff,
  Trophy,
  ListOrdered,
  History,
  Settings2,
  Gift,
  Plus,
  ExternalLink,
  Copy,
  Save,
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
}

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T

// Config management
const modePresets = computed(() => store.presets.filter((preset) => preset.mode === 'free'))
const configs = computed(() =>
  modePresets.value.map((p) => ({ id: p.id, name: p.name }))
)

const activeConfigId = computed({
  get: () => store.presetSelections.free ?? '',
  set: (value: string) => {
    const preset = store.presets.find((item) => item.id === value) ?? null
    store.selectPresetForMode('free', preset)
  },
})

const currentPreset = computed<Preset | null>(() => {
  if (store.currentPreset?.mode === 'free') return store.currentPreset
  const presetId = store.presetSelections.free
  return presetId ? store.presets.find((item) => item.id === presetId) ?? null : null
})

// Local state
const config = ref<FreeModeConfig>(clone(defaultConfig))
const targetGifts = ref<TargetGiftsConfig>(clone(defaultTargetGifts))
const isRunning = ref(false)
const currentRound = ref(1)
const currentAnchorIndex = ref(0)
const showNames = ref(true)
const isTikTokConnecting = ref(false)
const isGiftSelectorOpen = ref(false)

const roundHistory = ref<
  { round: number; anchorId: string; score: number; targetReached: boolean }[]
>([])

const anchors = computed(() => currentPreset.value?.anchors ?? [])

const currentAnchor = computed(() => anchors.value[currentAnchorIndex.value])

const getAnchorById = (id: string) => anchors.value.find((a) => a.id === id)

const widgetPreviewUrl = computed(() =>
  currentPreset.value?.widgetToken
    ? `${getServerBaseUrl()}/widget/${currentPreset.value.widgetToken}?mode=preset`
    : null
)

// Watch for preset changes
watch(
  () => currentPreset.value,
  (preset) => {
    const nextConfig = clone(preset?.config ?? defaultConfig) as FreeModeConfig
    config.value = {
      ...defaultConfig,
      ...nextConfig,
      displayRange: nextConfig.displayRange ?? defaultConfig.displayRange,
    }
    targetGifts.value = clone(preset?.targetGifts ?? defaultTargetGifts)
    currentRound.value = 1
    currentAnchorIndex.value = 0
    roundHistory.value = []
  },
  { immediate: true }
)

// Ensure default config exists
const ensureDefaultConfig = async () => {
  if (modePresets.value.length === 0) {
    try {
      const response = await createPreset({
        name: 'Config1',
        mode: 'free' as GameMode,
        config: defaultConfig,
        targetGifts: defaultTargetGifts,
      })
      if (response?.success && response.data) {
        const preset = mapApiPreset(response.data)
        store.addPreset(preset)
        store.selectPresetForMode('free', preset)
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
      mode: 'free' as GameMode,
      config: defaultConfig,
      targetGifts: defaultTargetGifts,
    })
    if (response?.success && response.data) {
      const preset = mapApiPreset(response.data)
      store.addPreset(preset)
      store.selectPresetForMode('free', preset)
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
      if (isCurrentSelected) {
        const remaining = store.presets.filter((p) => p.mode === 'free')
        if (remaining.length > 0) {
          store.selectPresetForMode('free', remaining[0])
        } else {
          store.selectPresetForMode('free', null)
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
    store.selectPresetForMode('free', updated)
  }
}

const handleAnchorRemove = async (anchorId: string) => {
  if (!currentPreset.value) return
  const response = await detachAnchorFromPreset(currentPreset.value.id, anchorId)
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('free', updated)
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
  if (anchors.value.length === 0) {
    toast({ title: '错误', description: '请至少添加一位主播', variant: 'destructive' })
    return
  }
  const connected = await connectTikTok()
  if (!connected) return
  isRunning.value = true
  toast({ title: '开始', description: `第 ${currentRound.value} 回合 - ${anchors.value[currentAnchorIndex.value]?.name}` })
}

const handlePause = () => {
  isRunning.value = false
  toast({ title: '暂停', description: '自由模式已暂停' })
}

const handleEndRound = () => {
  if (anchors.value.length === 0) return
  const anchor = anchors.value[currentAnchorIndex.value]
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
  currentAnchorIndex.value = (currentAnchorIndex.value + 1) % anchors.value.length

  toast({
    title: '回合结束',
    description: `下一位: ${anchors.value[currentAnchorIndex.value]?.name}`,
  })
}

const handleReset = () => {
  isRunning.value = false
  currentRound.value = 1
  currentAnchorIndex.value = 0
  roundHistory.value = []
  toast({
    title: '重置',
    description: '自由模式已重置',
  })
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

  const configResponse = await updateGameConfig(currentPreset.value.id, {
    free: config.value,
    targetGifts: targetGifts.value.targetGifts,
  })

  if (configResponse?.success && configResponse.data) {
    const updated = mapApiPreset(configResponse.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('free', updated)
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
