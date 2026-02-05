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

    <Tabs v-model="activeTab" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="config">Configuration</TabsTrigger>
        <TabsTrigger value="anchors">Anchors</TabsTrigger>
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
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="anchors" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">Anchor Configuration</CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea class="h-96">
              <div class="space-y-3">
                <div
                  v-for="(anchor, index) in store.anchors"
                  :key="anchor.id"
                  class="flex items-center gap-4 p-4 bg-slate-800 rounded-lg"
                >
                  <div class="w-8 h-8 bg-slate-700 rounded-full flex items-center justify-center text-slate-400 font-medium">
                    {{ index + 1 }}
                  </div>

                  <Avatar class="w-12 h-12">
                    <AvatarImage :src="anchor.avatar" />
                    <AvatarFallback class="bg-gradient-to-br from-pink-500 to-rose-600 text-white">
                      {{ anchor.name[0] }}
                    </AvatarFallback>
                  </Avatar>

                  <div class="flex-1">
                    <Input
                      v-model="anchor.name"
                      class="bg-slate-900 border-slate-700 text-white mb-2"
                      placeholder="Anchor Name"
                    />
                    <Input
                      v-model="anchor.tiktokId"
                      class="bg-slate-900 border-slate-700 text-white text-sm"
                      placeholder="TikTok ID (optional)"
                    />
                  </div>

                  <Button variant="ghost" size="icon" class="text-red-400 hover:text-red-300">
                    <Trash2 class="w-4 h-4" />
                  </Button>
                </div>

                <Button
                  variant="outline"
                  class="w-full border-dashed border-slate-700 text-slate-400"
                >
                  <Plus class="w-4 h-4 mr-2" />
                  Add Anchor
                </Button>
              </div>
            </ScrollArea>
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
            <div class="aspect-video bg-slate-800 rounded-lg flex items-center justify-center">
              <div class="text-center text-slate-500">
                <Sticker class="w-16 h-16 mx-auto mb-4 opacity-50" />
                <p>Widget preview will appear here</p>
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
import { ref } from 'vue'
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
import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import ScrollArea from '@/components/ui/ScrollArea.vue'
import Avatar from '@/components/ui/Avatar.vue'
import AvatarImage from '@/components/ui/AvatarImage.vue'
import AvatarFallback from '@/components/ui/AvatarFallback.vue'
import Switch from '@/components/ui/Switch.vue'
import type { StickerModeConfig, GameplayGift } from '@/types'
import {
  Sticker,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Plus,
  Trash2,
  Clock,
  Timer,
  FlipHorizontal,
  Sparkles,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const config = ref<StickerModeConfig>({
  type: 'normal',
  countdownEnabled: false,
  countdownDuration: 60,
  decayEnabled: false,
  decayDuration: 30,
  autoFlipEnabled: false,
  flipInterval: 10,
  maxPages: 2,
  gameplayGifts: [],
})

const isRunning = ref(false)
const activeTab = ref('config')
const isTikTokConnecting = ref(false)

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
