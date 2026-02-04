<template>
  <div class="space-y-6 p-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <Swords class="w-6 h-6 text-red-400" />
          {{ t('pkMode') }}
        </h1>
        <p class="text-slate-400">Attack & defense showdown</p>
      </div>
      <Button class="bg-red-600 hover:bg-red-700" @click="handleStart">
        <Play class="w-4 h-4 mr-2" />
        {{ t('startPK') }}
      </Button>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Settings2 class="w-5 h-5 text-slate-300" />
          {{ t('pkSettings') }}
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <label class="text-sm text-slate-300">{{ t('freezeThresholds') }}</label>
            <Slider v-model="config.freezeThresholds" :min="0" :max="100" />
          </div>
          <div class="space-y-2">
            <label class="text-sm text-slate-300">{{ t('scoringMethod') }}</label>
            <select v-model="config.scoringMethod" class="w-full rounded border border-slate-700 bg-slate-900 p-2 text-sm">
              <option value="individual">{{ t('individualScore') }}</option>
              <option value="split">{{ t('splitScore') }}</option>
              <option value="defender">{{ t('defenderWins') }}</option>
            </select>
          </div>
        </div>
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('freezeEffect') }}</p>
            <p class="text-xs text-slate-500">Enable freeze effect at threshold.</p>
          </div>
          <Switch v-model="config.freezeEffect" />
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
              <AvatarFallback class="bg-gradient-to-br from-blue-500 to-indigo-600 text-white">
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
import { Slider } from '@/components/ui/slider'
import { Switch } from '@/components/ui/switch'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Swords, Play, Settings2, Users } from 'lucide-vue-next'
import type { PKModeConfig } from '@/types'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const anchors = computed(() => store.anchors)

const config = ref<PKModeConfig>({
  defenderAdvantage: true,
  allowDraw: false,
  scoringMethod: 'individual',
  freezeEffect: true,
  freezeThresholds: 50,
})

const handleStart = () => {
  if (anchors.value.length < 2) {
    toast({
      title: 'Error',
      description: 'Please add at least two anchors',
      variant: 'destructive',
    })
    return
  }
  toast({
    title: 'PK Mode Started',
    description: `Anchors: ${anchors.value.length}`,
  })
}
</script>
