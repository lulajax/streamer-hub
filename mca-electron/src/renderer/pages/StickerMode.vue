<template>
  <div class="space-y-6 p-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <Sticker class="w-6 h-6 text-pink-400" />
          {{ t('stickerMode') }}
        </h1>
        <p class="text-slate-400">Sticker dance battle setup</p>
      </div>
      <Button class="bg-pink-600 hover:bg-pink-700" @click="handleStart">
        <Play class="w-4 h-4 mr-2" />
        {{ t('startRound') }}
      </Button>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Settings2 class="w-5 h-5 text-slate-300" />
          {{ t('mode') }} Settings
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <label class="text-sm text-slate-300">{{ t('countdown') }} (seconds)</label>
            <Input v-model="config.countdown" type="number" />
          </div>
          <div class="space-y-2">
            <label class="text-sm text-slate-300">{{ t('decay') }} (%)</label>
            <Input v-model="config.decay" type="number" />
          </div>
        </div>
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('autoFlip') }}</p>
            <p class="text-xs text-slate-500">Auto flip sticker cards.</p>
          </div>
          <Switch v-model="config.autoFlip" />
        </div>
      </CardContent>
    </Card>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Users class="w-5 h-5 text-blue-400" />
          {{ t('anchors') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div v-if="anchors.length === 0" class="text-slate-400 text-sm">
          No anchors added yet.
        </div>
        <div v-else class="grid grid-cols-2 gap-4">
          <div
            v-for="anchor in anchors"
            :key="anchor.id"
            class="flex items-center gap-3 rounded-lg bg-slate-800 p-3"
          >
            <Avatar class="w-10 h-10">
              <AvatarImage :src="anchor.avatar" />
              <AvatarFallback class="bg-gradient-to-br from-pink-500 to-rose-600 text-white">
                {{ anchor.name[0] }}
              </AvatarFallback>
            </Avatar>
            <div>
              <p class="text-white font-medium">{{ anchor.name }}</p>
              <p class="text-slate-400 text-xs">{{ anchor.totalScore }} points</p>
            </div>
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
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Sticker, Play, Settings2, Users } from 'lucide-vue-next'
import type { StickerModeConfig } from '@/types'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const anchors = computed(() => store.anchors)

const config = ref<StickerModeConfig>({
  countdown: 60,
  decay: 10,
  autoFlip: true,
  flipInterval: 10,
})

const handleStart = () => {
  if (anchors.value.length === 0) {
    toast({
      title: 'Error',
      description: 'Please add at least one anchor',
      variant: 'destructive',
    })
    return
  }
  toast({
    title: 'Sticker Mode Started',
    description: `Anchors: ${anchors.value.length}`,
  })
}
</script>
