<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <Swords class="w-6 h-6 text-red-400" />
          {{ t('pkMode') }}
        </h1>
        <p class="text-slate-400">Team PK battle mode configuration</p>
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
            <Trophy class="w-4 h-4 mr-2" />
            End Round
          </Button>
        </template>
        <Button v-else class="bg-red-600 hover:bg-red-700" @click="handleStart">
          <Play class="w-4 h-4 mr-2" />
          Start PK
        </Button>
      </div>
    </div>

    <!-- PK Progress Bar -->
    <Card class="bg-slate-900 border-slate-800">
      <CardContent class="p-6">
        <div class="flex items-center justify-between mb-4">
          <div class="flex items-center gap-3">
            <div class="w-12 h-12 bg-blue-500/20 rounded-full flex items-center justify-center">
              <Shield class="w-6 h-6 text-blue-400" />
            </div>
            <div>
              <p class="text-white font-bold text-lg">{{ totalDefenderScore.toLocaleString() }}</p>
              <p class="text-blue-400 text-sm">Defenders</p>
            </div>
          </div>

          <div class="text-center">
            <Badge variant="secondary" class="text-lg px-4 py-1">
              Round {{ currentRound }}
            </Badge>
            <p v-if="config.countdownEnabled && isRunning" class="text-amber-400 text-sm mt-1">
              <Clock class="w-4 h-4 inline mr-1" />
              {{ Math.floor(config.countdownDuration / 60) }}:
              {{ String(config.countdownDuration % 60).padStart(2, '0') }}
            </p>
          </div>

          <div class="flex items-center gap-3 text-right">
            <div>
              <p class="text-white font-bold text-lg">{{ totalAttackerScore.toLocaleString() }}</p>
              <p class="text-red-400 text-sm">Attackers</p>
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

        <!-- Freeze Effect Indicator -->
        <div v-if="config.freezeEffectEnabled" class="mt-4 flex items-center justify-center gap-4">
          <div class="flex items-center gap-2 text-cyan-400">
            <Snowflake class="w-4 h-4" />
            <span class="text-sm">Freeze Effect Active</span>
          </div>
          <div class="flex gap-2">
            <Badge variant="outline" class="border-cyan-500/50 text-cyan-400">
              Low: {{ config.freezeThresholds.low }}%
            </Badge>
            <Badge variant="outline" class="border-cyan-500/50 text-cyan-400">
              Med: {{ config.freezeThresholds.medium }}%
            </Badge>
            <Badge variant="outline" class="border-cyan-500/50 text-cyan-400">
              High: {{ config.freezeThresholds.high }}%
            </Badge>
          </div>
        </div>
      </CardContent>
    </Card>

    <Tabs v-model="activeTab" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="teams">Team Setup</TabsTrigger>
        <TabsTrigger value="config">Configuration</TabsTrigger>
        <TabsTrigger value="preview">Preview</TabsTrigger>
      </TabsList>

      <TabsContent value="teams" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <!-- Defenders -->
          <Card class="bg-slate-900 border-slate-800">
            <CardHeader class="bg-blue-500/10">
              <CardTitle class="text-white flex items-center gap-2">
                <Shield class="w-5 h-5 text-blue-400" />
                Defenders
              </CardTitle>
            </CardHeader>
            <CardContent class="p-4">
              <ScrollArea class="h-64">
                <div class="space-y-2">
                  <div
                    v-for="anchor in defenders"
                    :key="anchor.id"
                    class="flex items-center gap-3 p-3 bg-slate-800 rounded-lg"
                  >
                    <GripVertical class="w-4 h-4 text-slate-500" />
                    <Avatar class="w-8 h-8">
                      <AvatarImage :src="anchor.avatar" />
                      <AvatarFallback class="bg-blue-500 text-white text-xs">
                        {{ anchor.name[0] }}
                      </AvatarFallback>
                    </Avatar>
                    <span class="text-white flex-1">{{ anchor.name }}</span>
                    <span class="text-blue-400 font-medium">{{ anchor.totalScore }}</span>
                    <Button
                      variant="ghost"
                      size="icon"
                      class="w-6 h-6 text-red-400"
                      @click="removeFromTeam(anchor.id, 'defender')"
                    >
                      <Trash2 class="w-3 h-3" />
                    </Button>
                  </div>

                  <div v-if="defenders.length === 0" class="text-center py-8 text-slate-500">
                    <Shield class="w-12 h-12 mx-auto mb-3 opacity-50" />
                    <p>No defenders assigned</p>
                  </div>
                </div>
              </ScrollArea>

              <select
                class="mt-4 w-full bg-slate-800 border-slate-700 text-white rounded px-3 py-2"
                @change="(e) => addToTeamById((e.target as HTMLSelectElement).value, 'defender')"
              >
                <option value="">Add defender...</option>
                <option
                  v-for="anchor in availableAnchors"
                  :key="anchor.id"
                  :value="anchor.id"
                >
                  {{ anchor.name }}
                </option>
              </select>
            </CardContent>
          </Card>

          <!-- Attackers -->
          <Card class="bg-slate-900 border-slate-800">
            <CardHeader class="bg-red-500/10">
              <CardTitle class="text-white flex items-center gap-2">
                <Sword class="w-5 h-5 text-red-400" />
                Attackers
              </CardTitle>
            </CardHeader>
            <CardContent class="p-4">
              <ScrollArea class="h-64">
                <div class="space-y-2">
                  <div
                    v-for="anchor in attackers"
                    :key="anchor.id"
                    class="flex items-center gap-3 p-3 bg-slate-800 rounded-lg"
                  >
                    <GripVertical class="w-4 h-4 text-slate-500" />
                    <Avatar class="w-8 h-8">
                      <AvatarImage :src="anchor.avatar" />
                      <AvatarFallback class="bg-red-500 text-white text-xs">
                        {{ anchor.name[0] }}
                      </AvatarFallback>
                    </Avatar>
                    <span class="text-white flex-1">{{ anchor.name }}</span>
                    <span class="text-red-400 font-medium">{{ anchor.totalScore }}</span>
                    <Button
                      variant="ghost"
                      size="icon"
                      class="w-6 h-6 text-red-400"
                      @click="removeFromTeam(anchor.id, 'attacker')"
                    >
                      <Trash2 class="w-3 h-3" />
                    </Button>
                  </div>

                  <div v-if="attackers.length === 0" class="text-center py-8 text-slate-500">
                    <Sword class="w-12 h-12 mx-auto mb-3 opacity-50" />
                    <p>No attackers assigned</p>
                  </div>
                </div>
              </ScrollArea>

              <select
                class="mt-4 w-full bg-slate-800 border-slate-700 text-white rounded px-3 py-2"
                @change="(e) => addToTeamById((e.target as HTMLSelectElement).value, 'attacker')"
              >
                <option value="">Add attacker...</option>
                <option
                  v-for="anchor in availableAnchors"
                  :key="anchor.id"
                  :value="anchor.id"
                >
                  {{ anchor.name }}
                </option>
              </select>
            </CardContent>
          </Card>
        </div>
      </TabsContent>

      <TabsContent value="config" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Settings2 class="w-5 h-5 text-amber-400" />
              PK Settings
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-6">
            <!-- Advantage -->
            <div>
              <label class="text-sm font-medium text-slate-300 mb-2 block">Advantage Setting</label>
              <select
                v-model="config.advantage"
                class="w-full bg-slate-800 border-slate-700 text-white rounded px-3 py-2"
              >
                <option value="defender">Defender Advantage (Draw = Defender Wins)</option>
                <option value="attacker">Attacker Advantage (Draw = Attacker Wins)</option>
                <option value="draw">Allow Draw (Continue to Next Round)</option>
              </select>
            </div>

            <!-- Scoring Method -->
            <div>
              <label class="text-sm font-medium text-slate-300 mb-2 block">Scoring Method</label>
              <select
                v-model="config.scoringMethod"
                class="w-full bg-slate-800 border-slate-700 text-white rounded px-3 py-2"
              >
                <option value="individual">Individual Score (Each gets their own)</option>
                <option value="split">Split Score (Divide equally)</option>
                <option value="defender">All to Defender (Even if they lose)</option>
              </select>
            </div>

            <!-- Countdown -->
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Clock class="w-5 h-5 text-amber-400" />
                <div>
                  <p class="text-white font-medium">Countdown Timer</p>
                  <p class="text-slate-400 text-sm">Auto-end round when time expires</p>
                </div>
              </div>
              <div class="flex items-center gap-4">
                <Input
                  v-if="config.countdownEnabled"
                  v-model.number="config.countdownDuration"
                  type="number"
                  class="w-24 bg-slate-900 border-slate-700 text-white"
                  placeholder="Seconds"
                />
                <Switch v-model="config.countdownEnabled" />
              </div>
            </div>

            <!-- Single Gift Bind -->
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div class="flex items-center gap-3">
                <Users class="w-5 h-5 text-green-400" />
                <div>
                  <p class="text-white font-medium">Single Gift Bind Mode</p>
                  <p class="text-slate-400 text-sm">One exclusive gift per team, user binds by sending</p>
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
                    <p class="text-white font-medium">Freeze Effect</p>
                    <p class="text-slate-400 text-sm">Visual effect when team is behind</p>
                  </div>
                </div>
                <Switch v-model="config.freezeEffectEnabled" />
              </div>

              <div v-if="config.freezeEffectEnabled" class="grid grid-cols-3 gap-4">
                <div>
                  <label class="text-xs text-slate-400 mb-1 block">Low Threshold (%)</label>
                  <Input
                    v-model.number="config.freezeThresholds.low"
                    type="number"
                    class="bg-slate-800 border-slate-700 text-white"
                  />
                </div>
                <div>
                  <label class="text-xs text-slate-400 mb-1 block">Medium Threshold (%)</label>
                  <Input
                    v-model.number="config.freezeThresholds.medium"
                    type="number"
                    class="bg-slate-800 border-slate-700 text-white"
                  />
                </div>
                <div>
                  <label class="text-xs text-slate-400 mb-1 block">High Threshold (%)</label>
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
      </TabsContent>

      <TabsContent value="preview" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">Widget Preview</CardTitle>
          </CardHeader>
          <CardContent>
            <div class="aspect-video bg-slate-800 rounded-lg flex items-center justify-center">
              <div class="text-center text-slate-500">
                <Swords class="w-16 h-16 mx-auto mb-4 opacity-50" />
                <p>PK widget preview will appear here</p>
                <p class="text-sm">Configure teams and click Start PK to see preview</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import { useToast } from '@/hooks/use-toast'
import { connectTikTokLiveForRoom } from '@/lib/tiktokLive'
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
import type { PKModeConfig, Anchor } from '@/types'
import {
  Swords,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Shield,
  Sword,
  Users,
  Clock,
  Snowflake,
  Trophy,
  GripVertical,
  Trash2,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const config = ref<PKModeConfig>({
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
})

const isRunning = ref(false)
const currentRound = ref(1)
const defenderScore = ref(0)
const attackerScore = ref(0)
const defenders = ref<Anchor[]>([])
const attackers = ref<Anchor[]>([])
const activeTab = ref('teams')
const isTikTokConnecting = ref(false)

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

const availableAnchors = computed(() =>
  store.anchors.filter(
    (a) => !defenders.value.find((d) => d.id === a.id) && !attackers.value.find((at) => at.id === a.id)
  )
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
  if (defenders.value.length === 0 || attackers.value.length === 0) {
    toast({
      title: 'Error',
      description: 'Please configure both defender and attacker teams',
      variant: 'destructive',
    })
    return
  }
  const connected = await connectTikTok()
  if (!connected) return
  isRunning.value = true
  toast({
    title: 'PK Started',
    description: `Round ${currentRound.value} - Defenders vs Attackers`,
  })
}

const handlePause = () => {
  isRunning.value = false
  toast({
    title: 'PK Paused',
  })
}

const handleEndRound = () => {
  isRunning.value = false
  const winner =
    defenderScore.value > attackerScore.value
      ? 'defender'
      : attackerScore.value > defenderScore.value
        ? 'attacker'
        : 'draw'
  toast({
    title: 'Round Ended',
    description: `Winner: ${winner === 'draw' ? 'Draw' : winner === 'defender' ? 'Defenders' : 'Attackers'}`,
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

const addToTeamById = (id: string, team: 'defender' | 'attacker') => {
  if (!id) return
  const anchor = store.anchors.find((a) => a.id === id)
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
</script>
