<template>
  <div class="space-y-6 p-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <Users class="w-6 h-6 text-green-400" />
          {{ t('freeMode') }}
        </h1>
        <p class="text-slate-400">Free rotation mode for talent showcase</p>
      </div>
      <div class="flex items-center gap-3">
        <Button variant="outline" class="border-slate-700 text-slate-300" @click="handleReset">
          <RotateCcw class="w-4 h-4 mr-2" />
          Reset
        </Button>
        <Button v-if="isRunning" variant="secondary" @click="handlePause">
          <Pause class="w-4 h-4 mr-2" />
          Pause
        </Button>
        <Button v-else class="bg-green-600 hover:bg-green-700" @click="handleStart">
          <Play class="w-4 h-4 mr-2" />
          Start Round
        </Button>
      </div>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Settings2 class="w-5 h-5 text-slate-300" />
          {{ t('freeSettings') }}
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <label class="text-sm text-slate-300">{{ t('roundDuration') }}</label>
            <Input v-model="config.roundDuration" type="number" />
          </div>
          <div class="space-y-2">
            <label class="text-sm text-slate-300">{{ t('targetScore') }}</label>
            <Input v-model="config.targetScore" type="number" />
          </div>
        </div>
        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <p class="text-sm text-slate-200">{{ t('showRoundNumber') }}</p>
            <p class="text-xs text-slate-500">Display current round number.</p>
          </div>
          <Switch v-model="config.showRoundNumber" />
        </div>
        <div class="flex items-center justify-between">
          <div class="space-y-1">
            <p class="text-sm text-slate-200">Show anchor names</p>
            <p class="text-xs text-slate-500">Toggle name visibility in widgets.</p>
          </div>
          <Switch v-model="showNames" />
        </div>
      </CardContent>
    </Card>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Trophy class="w-5 h-5 text-amber-400" />
          Current Anchor
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="flex items-center gap-4">
          <Avatar class="w-14 h-14">
            <AvatarImage :src="currentAnchor?.avatar" />
            <AvatarFallback class="bg-gradient-to-br from-green-500 to-emerald-600 text-white text-xl">
              {{ currentAnchor?.name?.[0] || '?' }}
            </AvatarFallback>
          </Avatar>
          <div>
            <p class="text-slate-400 text-sm">Current Anchor</p>
            <p class="text-xl font-bold text-white">
              {{ showNames ? currentAnchor?.name || 'N/A' : '???' }}
            </p>
          </div>
          <div class="ml-auto text-right">
            <p class="text-slate-400 text-sm">Score</p>
            <p class="text-2xl font-bold text-green-400">
              {{ currentAnchor?.totalScore?.toLocaleString() || 0 }}
            </p>
          </div>
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Users, Play, Pause, RotateCcw, Settings2, Trophy } from 'lucide-vue-next'
import type { FreeModeConfig } from '@/types'

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
const currentAnchorIndex = ref(0)
const showNames = ref(true)

const anchors = computed(() => store.anchors)
const currentAnchor = computed(() => anchors.value[currentAnchorIndex.value])

const handleStart = () => {
  if (anchors.value.length === 0) {
    toast({
      title: 'Error',
      description: 'Please add at least one anchor',
      variant: 'destructive',
    })
    return
  }
  isRunning.value = true
  toast({
    title: 'Free Mode Started',
    description: `Anchor: ${currentAnchor.value?.name ?? 'N/A'}`,
  })
}

const handlePause = () => {
  isRunning.value = false
  toast({
    title: 'Free Mode Paused',
  })
}

const handleReset = () => {
  isRunning.value = false
  currentAnchorIndex.value = 0
  toast({
    title: 'Free Mode Reset',
  })
}
</script>
