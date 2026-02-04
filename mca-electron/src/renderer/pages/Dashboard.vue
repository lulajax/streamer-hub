<template>
  <div class="space-y-6 p-6">
    <div class="grid grid-cols-4 gap-4">
      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Total Gifts</p>
              <p class="text-2xl font-bold text-white">{{ totalGifts }}</p>
            </div>
            <div class="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
              <Gift class="w-5 h-5 text-blue-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Total Diamonds</p>
              <p class="text-2xl font-bold text-white">{{ totalDiamonds.toLocaleString() }}</p>
            </div>
            <div class="w-10 h-10 bg-purple-500/20 rounded-lg flex items-center justify-center">
              <Diamond class="w-5 h-5 text-purple-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Unique Users</p>
              <p class="text-2xl font-bold text-white">{{ uniqueUsers }}</p>
            </div>
            <div class="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center">
              <Users class="w-5 h-5 text-green-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Active Anchors</p>
              <p class="text-2xl font-bold text-white">
                {{ activeAnchors }}/{{ anchors.length }}
              </p>
            </div>
            <div class="w-10 h-10 bg-amber-500/20 rounded-lg flex items-center justify-center">
              <Trophy class="w-5 h-5 text-amber-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <div class="grid grid-cols-2 gap-6">
      <Card class="bg-slate-900 border-slate-800">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Trophy class="w-5 h-5 text-amber-400" />
            Anchor Leaderboard
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ScrollArea class="h-80">
            <div class="space-y-2">
              <div
                v-for="(anchor, index) in sortedAnchors"
                :key="anchor.id"
                :class="[
                  'flex items-center gap-3 p-3 rounded-lg',
                  anchor.isEliminated ? 'bg-slate-800/50 opacity-50' : 'bg-slate-800',
                ]"
              >
                <div class="w-6 text-center">
                  <span v-if="index === 0" class="text-amber-400 font-bold">1</span>
                  <span v-else-if="index === 1" class="text-slate-300 font-bold">2</span>
                  <span v-else-if="index === 2" class="text-amber-600 font-bold">3</span>
                  <span v-else class="text-slate-500">{{ index + 1 }}</span>
                </div>

                <Avatar class="w-10 h-10">
                  <AvatarImage :src="anchor.avatar" />
                  <AvatarFallback class="bg-gradient-to-br from-pink-500 to-rose-600 text-white">
                    {{ anchor.name[0] }}
                  </AvatarFallback>
                </Avatar>

                <div class="flex-1">
                  <p class="text-white font-medium">{{ anchor.name }}</p>
                  <p class="text-slate-400 text-sm">
                    {{ anchor.exclusiveGifts.length }} exclusive gifts
                  </p>
                </div>

                <div class="text-right">
                  <p class="text-white font-bold">{{ anchor.totalScore.toLocaleString() }}</p>
                  <p class="text-slate-400 text-xs">points</p>
                </div>

                <div class="flex items-center gap-1">
                  <Button
                    variant="ghost"
                    size="icon"
                    class="w-7 h-7 text-green-400 hover:text-green-300 hover:bg-green-500/20"
                    @click="handleAddScore(anchor.id, 100)"
                  >
                    <Plus class="w-4 h-4" />
                  </Button>
                  <Button
                    variant="ghost"
                    size="icon"
                    class="w-7 h-7 text-red-400 hover:text-red-300 hover:bg-red-500/20"
                    @click="handleAddScore(anchor.id, -100)"
                  >
                    <Minus class="w-4 h-4" />
                  </Button>
                </div>

                <Button
                  variant="ghost"
                  size="sm"
                  :class="anchor.isEliminated ? 'text-green-400 hover:text-green-300' : 'text-red-400 hover:text-red-300'"
                  @click="handleEliminate(anchor.id)"
                >
                  {{ anchor.isEliminated ? 'Revive' : 'Eliminate' }}
                </Button>
              </div>
            </div>
          </ScrollArea>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Gift class="w-5 h-5 text-pink-400" />
            Recent Gifts
          </CardTitle>
        </CardHeader>
        <CardContent>
          <ScrollArea class="h-80">
            <div class="space-y-2">
              <div
                v-for="record in giftRecords.slice(0, 20)"
                :key="record.id"
                class="flex items-center gap-3 p-2 rounded-lg bg-slate-800/50"
              >
                <Avatar class="w-8 h-8">
                  <AvatarImage :src="record.userAvatar" />
                  <AvatarFallback class="bg-slate-700 text-slate-300 text-xs">
                    {{ record.userName[0] }}
                  </AvatarFallback>
                </Avatar>

                <div class="flex-1 min-w-0">
                  <p class="text-white text-sm truncate">{{ record.userName }}</p>
                  <p class="text-slate-400 text-xs">
                    Sent {{ record.giftCount }}x {{ record.giftName }}
                  </p>
                </div>

                <div class="text-right">
                  <p class="text-white text-sm">{{ record.totalCost }}</p>
                  <p class="text-slate-400 text-xs">diamonds</p>
                </div>
              </div>
            </div>
          </ScrollArea>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Gift, Users, Trophy, Diamond, Minus, Plus } from 'lucide-vue-next'

const store = useStore()

const anchors = computed(() => store.anchors)
const giftRecords = computed(() => store.giftRecords)

const totalGifts = computed(() => giftRecords.value.length)
const totalDiamonds = computed(() => giftRecords.value.reduce((sum, r) => sum + r.totalCost, 0))
const uniqueUsers = computed(() => new Set(giftRecords.value.map((r) => r.userId)).size)
const activeAnchors = computed(() => anchors.value.filter((anchor) => !anchor.isEliminated).length)
const sortedAnchors = computed(() => [...anchors.value].sort((a, b) => b.totalScore - a.totalScore))

const handleAddScore = (anchorId: string, amount: number) => {
  // Implement score adjustment
}

const handleEliminate = (anchorId: string) => {
  // Implement eliminate/revive
}
</script>
