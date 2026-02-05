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
                <Users class="w-16 h-16 mx-auto mb-4 opacity-50" />
                <p>Free mode widget preview will appear here</p>
                <p class="text-sm">Configure settings and click Start to see preview</p>
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
import type { FreeModeConfig } from '@/types'
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
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const config = ref<FreeModeConfig>({
  roundDuration: 300,
  targetScore: 10000,
  showRoundNumber: true,
  displayRange: [1, 10],
})

const isRunning = ref(false)
const currentRound = ref(1)
const currentAnchorIndex = ref(0)
const showNames = ref(true)
const activeTab = ref('anchors')
const isTikTokConnecting = ref(false)

const roundHistory = ref<
  { round: number; anchorId: string; score: number; targetReached: boolean }[]
>([])

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
</script>
