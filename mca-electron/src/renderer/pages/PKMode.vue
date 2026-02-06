<template>
  <ModeConfigLayout
    :title="t('pkMode')"
    :icon="Swords"
    icon-class="text-red-400"
    :configs="configs"
    :active-config-id="activeConfigId"
    :anchors="anchors"
    :user-anchors="store.userAnchors"
    :is-running="isRunning"
    :show-end-round="true"
    :start-button-text="'开始PK'"
    :start-button-class="'bg-red-600 hover:bg-red-700'"
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
      <!-- PK Progress Bar -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardContent class="p-6">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-blue-500/20 rounded-full flex items-center justify-center">
                <Shield class="w-6 h-6 text-blue-400" />
              </div>
              <div>
                <p class="text-white font-bold text-lg">{{ totalDefenderScore.toLocaleString() }}</p>
                <p class="text-blue-400 text-sm">守擂方</p>
              </div>
            </div>

            <div class="text-center">
              <Badge variant="secondary" class="text-lg px-4 py-1">
                第 {{ currentRound }} 回合
              </Badge>
              <p v-if="config.countdownEnabled && isRunning" class="text-amber-400 text-sm mt-1">
                <Clock class="w-4 h-4 inline mr-1" />
                {{ Math.floor(config.countdownDuration / 60) }}:{{ String(config.countdownDuration % 60).padStart(2, '0') }}
              </p>
            </div>

            <div class="flex items-center gap-3 text-right">
              <div>
                <p class="text-white font-bold text-lg">{{ totalAttackerScore.toLocaleString() }}</p>
                <p class="text-red-400 text-sm">攻擂方</p>
              </div>
              <div class="w-12 h-12 bg-red-500/20 rounded-full flex items-center justify-center">
                <Sword class="w-6 h-6 text-red-400" />
              </div>
            </div>
          </div>

          <!-- Progress Bar -->
          <div class="relative h-8 bg-slate-800 rounded-full overflow-hidden">
            <div
              class="absolute left-0 top-0 h-full bg-gradient-to-r from-blue-500 to-blue-600 transition-all duration-500"
              :style="{ width: `${defenderPercent}%` }"
            />
            <div
              class="absolute right-0 top-0 h-full bg-gradient-to-l from-red-500 to-red-600 transition-all duration-500"
              :style="{ width: `${100 - defenderPercent}%` }"
            />
            <div class="absolute inset-0 flex items-center justify-center">
              <span class="text-white font-bold text-sm drop-shadow-lg">
                {{ defenderPercent.toFixed(1) }}% - {{ (100 - defenderPercent).toFixed(1) }}%
              </span>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Team Assignment -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Users class="w-5 h-5 text-amber-400" />
            队伍分配
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div class="grid grid-cols-2 gap-4">
            <!-- Defenders -->
            <div class="bg-blue-500/10 border border-blue-500/30 rounded-lg p-4">
              <div class="flex items-center gap-2 mb-3">
                <Shield class="w-5 h-5 text-blue-400" />
                <span class="text-white font-medium">守擂方</span>
              </div>
              <div class="space-y-2">
                <div
                  v-for="anchor in defenders"
                  :key="anchor.id"
                  class="flex items-center gap-2 p-2 bg-slate-800 rounded-lg"
                >
                  <Avatar class="w-8 h-8">
                    <AvatarImage :src="anchor.avatar || ''" />
                    <AvatarFallback class="bg-blue-500 text-white text-xs">
                      {{ anchor.name.charAt(0) }}
                    </AvatarFallback>
                  </Avatar>
                  <span class="text-white flex-1 text-sm">{{ anchor.name }}</span>
                  <button
                    class="w-5 h-5 rounded-full bg-slate-700 text-slate-400 hover:bg-red-500/20 hover:text-red-400 flex items-center justify-center"
                    @click="removeFromTeam(anchor.id, 'defender')"
                  >
                    <X class="w-3 h-3" />
                  </button>
                </div>
                <select
                  class="w-full bg-slate-800 border-slate-700 text-white rounded px-3 py-2 text-sm"
                  @change="(e) => addToTeamById((e.target as HTMLSelectElement).value, 'defender')"
                >
                  <option value="">添加守擂主播...</option>
                  <option
                    v-for="anchor in availableForDefender"
                    :key="anchor.id"
                    :value="anchor.id"
                  >
                    {{ anchor.name }}
                  </option>
                </select>
              </div>
            </div>

            <!-- Attackers -->
            <div class="bg-red-500/10 border border-red-500/30 rounded-lg p-4">
              <div class="flex items-center gap-2 mb-3">
                <Sword class="w-5 h-5 text-red-400" />
                <span class="text-white font-medium">攻擂方</span>
              </div>
              <div class="space-y-2">
                <div
                  v-for="anchor in attackers"
                  :key="anchor.id"
                  class="flex items-center gap-2 p-2 bg-slate-800 rounded-lg"
                >
                  <Avatar class="w-8 h-8">
                    <AvatarImage :src="anchor.avatar || ''" />
                    <AvatarFallback class="bg-red-500 text-white text-xs">
                      {{ anchor.name.charAt(0) }}
                    </AvatarFallback>
                  </Avatar>
                  <span class="text-white flex-1 text-sm">{{ anchor.name }}</span>
                  <button
                    class="w-5 h-5 rounded-full bg-slate-700 text-slate-400 hover:bg-red-500/20 hover:text-red-400 flex items-center justify-center"
                    @click="removeFromTeam(anchor.id, 'attacker')"
                  >
                    <X class="w-3 h-3" />
                  </button>
                </div>
                <select
                  class="w-full bg-slate-800 border-slate-700 text-white rounded px-3 py-2 text-sm"
                  @change="(e) => addToTeamById((e.target as HTMLSelectElement).value, 'attacker')"
                >
                  <option value="">添加攻擂主播...</option>
                  <option
                    v-for="anchor in availableForAttacker"
                    :key="anchor.id"
                    :value="anchor.id"
                  >
                    {{ anchor.name }}
                  </option>
                </select>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- PK Settings -->
      <Card class="bg-slate-900 border-slate-800 mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Settings2 class="w-5 h-5 text-amber-400" />
            PK设置
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-4">
          <!-- Advantage -->
          <div>
            <label class="text-sm font-medium text-slate-300 mb-2 block">优势设置</label>
            <Select v-model="config.advantage">
              <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="defender">守擂有利（平局=守擂胜）</SelectItem>
                <SelectItem value="attacker">攻擂有利（平局=攻擂胜）</SelectItem>
                <SelectItem value="draw">允许平局（进入下一回合）</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- Scoring Method -->
          <div>
            <label class="text-sm font-medium text-slate-300 mb-2 block">计分方式</label>
            <Select v-model="config.scoringMethod">
              <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="individual">个人计分（各自独立）</SelectItem>
                <SelectItem value="split">平分分数（平均分配）</SelectItem>
                <SelectItem value="defender">全给守擂（即使输了）</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- Countdown -->
          <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
            <div class="flex items-center gap-3">
              <Clock class="w-5 h-5 text-amber-400" />
              <div>
                <p class="text-white font-medium">倒计时</p>
                <p class="text-slate-400 text-sm">时间到自动结束回合</p>
              </div>
            </div>
            <div class="flex items-center gap-4">
              <Input
                v-if="config.countdownEnabled"
                v-model.number="config.countdownDuration"
                type="number"
                class="w-24 bg-slate-900 border-slate-700 text-white"
                placeholder="秒"
              />
              <Switch v-model="config.countdownEnabled" />
            </div>
          </div>

          <!-- Single Gift Bind -->
          <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
            <div class="flex items-center gap-3">
              <Gift class="w-5 h-5 text-green-400" />
              <div>
                <p class="text-white font-medium">专属礼物跟随</p>
                <p class="text-slate-400 text-sm">每队一个专属礼物，用户送礼即绑定</p>
              </div>
            </div>
            <Switch v-model="config.singleGiftBindEnabled" />
          </div>

          <!-- Freeze Effect -->
          <div class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Snowflake class="w-5 h-5 text-cyan-400" />
                <div>
                  <p class="text-white font-medium">冰冻效果</p>
                  <p class="text-slate-400 text-sm">落后时显示冰冻特效</p>
                </div>
              </div>
              <Switch v-model="config.freezeEffectEnabled" />
            </div>

            <div v-if="config.freezeEffectEnabled" class="grid grid-cols-3 gap-4">
              <div>
                <label class="text-xs text-slate-400 mb-1 block">低阈值 (%)</label>
                <Input
                  v-model.number="config.freezeThresholds.low"
                  type="number"
                  class="bg-slate-800 border-slate-700 text-white"
                />
              </div>
              <div>
                <label class="text-xs text-slate-400 mb-1 block">中阈值 (%)</label>
                <Input
                  v-model.number="config.freezeThresholds.medium"
                  type="number"
                  class="bg-slate-800 border-slate-700 text-white"
                />
              </div>
              <div>
                <label class="text-xs text-slate-400 mb-1 block">高阈值 (%)</label>
                <Input
                  v-model.number="config.freezeThresholds.high"
                  type="number"
                  class="bg-slate-800 border-slate-700 text-white"
                />
              </div>
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
import type { PKModeConfig, TargetGiftsConfig, TargetGift, Preset, GameMode, Anchor } from '@/types'
import {
  Swords,
  Shield,
  Sword,
  Users,
  Clock,
  Snowflake,
  Settings2,
  Gift,
  Plus,
  X,
  ExternalLink,
  Copy,
  Save,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const defaultConfig: PKModeConfig = {
  advantage: 'defender',
  scoringMethod: 'individual',
  countdownEnabled: true,
  countdownDuration: 180,
  singleGiftBindEnabled: false,
  freezeEffectEnabled: false,
  freezeThresholds: {
    low: 20,
    medium: 40,
    high: 60,
  },
}

const defaultTargetGifts: TargetGiftsConfig = {
  targetGifts: [],
}

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T

// Config management
const modePresets = computed(() => store.presets.filter((preset) => preset.mode === 'pk'))
const configs = computed(() =>
  modePresets.value.map((p) => ({ id: p.id, name: p.name }))
)

const activeConfigId = computed({
  get: () => store.presetSelections.pk ?? '',
  set: (value: string) => {
    const preset = store.presets.find((item) => item.id === value) ?? null
    store.selectPresetForMode('pk', preset)
  },
})

const currentPreset = computed<Preset | null>(() => {
  if (store.currentPreset?.mode === 'pk') return store.currentPreset
  const presetId = store.presetSelections.pk
  return presetId ? store.presets.find((item) => item.id === presetId) ?? null : null
})

// Local state
const config = ref<PKModeConfig>(clone(defaultConfig))
const targetGifts = ref<TargetGiftsConfig>(clone(defaultTargetGifts))
const isRunning = ref(false)
const currentRound = ref(1)
const defenderScore = ref(0)
const attackerScore = ref(0)
const defenders = ref<Anchor[]>([])
const attackers = ref<Anchor[]>([])
const isTikTokConnecting = ref(false)
const isGiftSelectorOpen = ref(false)

const anchors = computed(() => currentPreset.value?.anchors ?? [])

const widgetPreviewUrl = computed(() =>
  currentPreset.value?.widgetToken
    ? `${getServerBaseUrl()}/widget/${currentPreset.value.widgetToken}?mode=preset`
    : null
)

// Team management
const availableForDefender = computed(() =>
  anchors.value.filter((a) => !defenders.value.find((d) => d.id === a.id) && !attackers.value.find((at) => at.id === a.id))
)

const availableForAttacker = computed(() =>
  anchors.value.filter((a) => !defenders.value.find((d) => d.id === a.id) && !attackers.value.find((at) => at.id === a.id))
)

const totalDefenderScore = computed(() =>
  defenders.value.reduce((sum, a) => sum + a.totalScore, 0) + defenderScore.value
)

const totalAttackerScore = computed(() =>
  attackers.value.reduce((sum, a) => sum + a.totalScore, 0) + attackerScore.value
)

const defenderPercent = computed(() => {
  const total = totalDefenderScore.value + totalAttackerScore.value
  return total > 0 ? (totalDefenderScore.value / total) * 100 : 50
})

// Watch for preset changes
watch(
  () => currentPreset.value,
  (preset) => {
    const nextConfig = clone(preset?.config ?? defaultConfig) as PKModeConfig
    config.value = {
      ...defaultConfig,
      ...nextConfig,
      freezeThresholds: {
        ...defaultConfig.freezeThresholds,
        ...(nextConfig.freezeThresholds ?? {}),
      },
    }
    targetGifts.value = clone(preset?.targetGifts ?? defaultTargetGifts)
    defenders.value = []
    attackers.value = []
    currentRound.value = 1
    defenderScore.value = 0
    attackerScore.value = 0
  },
  { immediate: true }
)

// Ensure default config exists
const ensureDefaultConfig = async () => {
  if (modePresets.value.length === 0) {
    try {
      const response = await createPreset({
        name: 'Config1',
        mode: 'pk' as GameMode,
        config: defaultConfig,
        targetGifts: defaultTargetGifts,
      })
      if (response?.success && response.data) {
        const preset = mapApiPreset(response.data)
        store.addPreset(preset)
        store.selectPresetForMode('pk', preset)
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
      mode: 'pk' as GameMode,
      config: defaultConfig,
      targetGifts: defaultTargetGifts,
    })
    if (response?.success && response.data) {
      const preset = mapApiPreset(response.data)
      store.addPreset(preset)
      store.selectPresetForMode('pk', preset)
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
        const remaining = store.presets.filter((p) => p.mode === 'pk')
        if (remaining.length > 0) {
          store.selectPresetForMode('pk', remaining[0])
        } else {
          store.selectPresetForMode('pk', null)
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
    store.selectPresetForMode('pk', updated)
  }
}

const handleAnchorRemove = async (anchorId: string) => {
  if (!currentPreset.value) return
  const response = await detachAnchorFromPreset(currentPreset.value.id, anchorId)
  if (response?.success && response.data) {
    const updated = mapApiPreset(response.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('pk', updated)
  }
}

const handleApplyToAll = () => {
  toast({ title: '提示', description: '应用到全部功能开发中' })
}

// Team handlers
const addToTeamById = (id: string, team: 'defender' | 'attacker') => {
  if (!id) return
  const anchor = anchors.value.find((a) => a.id === id)
  if (anchor) {
    if (team === 'defender') {
      defenders.value.push(anchor)
    } else {
      attackers.value.push(anchor)
    }
  }
}

const removeFromTeam = (anchorId: string, team: 'defender' | 'attacker') => {
  if (team === 'defender') {
    defenders.value = defenders.value.filter((a) => a.id !== anchorId)
  } else {
    attackers.value = attackers.value.filter((a) => a.id !== anchorId)
  }
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
  if (defenders.value.length === 0 || attackers.value.length === 0) {
    toast({ title: '错误', description: '请配置守擂方和攻擂方', variant: 'destructive' })
    return
  }
  const connected = await connectTikTok()
  if (!connected) return
  isRunning.value = true
  toast({ title: '开始', description: `第 ${currentRound.value} 回合 - 守擂方 vs 攻擂方` })
}

const handlePause = () => {
  isRunning.value = false
  toast({ title: '暂停', description: 'PK已暂停' })
}

const handleEndRound = () => {
  isRunning.value = false
  const winner =
    defenderScore.value > attackerScore.value
      ? '守擂方'
      : attackerScore.value > defenderScore.value
        ? '攻擂方'
        : '平局'
  toast({
    title: '回合结束',
    description: `获胜: ${winner}`,
  })
  currentRound.value++
  defenderScore.value = 0
  attackerScore.value = 0
}

const handleReset = () => {
  isRunning.value = false
  currentRound.value = 1
  defenderScore.value = 0
  attackerScore.value = 0
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
    pk: config.value,
    targetGifts: targetGifts.value.targetGifts,
  })

  if (configResponse?.success && configResponse.data) {
    const updated = mapApiPreset(configResponse.data)
    store.updatePreset(updated.id, updated)
    store.selectPresetForMode('pk', updated)
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
