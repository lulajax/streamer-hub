<template>
  <div class="space-y-6">
    <!-- Header -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <BarChart3 class="w-6 h-6 text-amber-400" />
          {{ t('reports') }}
        </h1>
        <p class="text-slate-400">View and export session data</p>
      </div>
      <div class="flex items-center gap-3">
        <div class="relative">
          <Calendar class="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
          <Input
            v-model="dateFilter"
            type="date"
            class="pl-10 bg-slate-800 border-slate-700 text-white w-40"
          />
        </div>
        <Button variant="outline" class="border-slate-700 text-slate-300">
          <Filter class="w-4 h-4 mr-2" />
          Filter
        </Button>
      </div>
    </div>

    <!-- Stats Overview -->
    <div class="grid grid-cols-4 gap-4">
      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Total Sessions</p>
              <p class="text-2xl font-bold text-white">{{ totalStats.totalSessions }}</p>
            </div>
            <div class="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
              <BarChart3 class="w-5 h-5 text-blue-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Total Gifts</p>
              <p class="text-2xl font-bold text-white">{{ totalStats.totalGifts.toLocaleString() }}</p>
            </div>
            <div class="w-10 h-10 bg-pink-500/20 rounded-lg flex items-center justify-center">
              <Gift class="w-5 h-5 text-pink-400" />
            </div>
          </div>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardContent class="p-4">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-slate-400 text-sm">Total Diamonds</p>
              <p class="text-2xl font-bold text-white">
                {{ totalStats.totalDiamonds.toLocaleString() }}
              </p>
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
              <p class="text-slate-400 text-sm">Avg Diamonds/Session</p>
              <p class="text-2xl font-bold text-white">
                {{ Math.round(totalStats.totalDiamonds / (totalStats.totalSessions || 1)).toLocaleString() }}
              </p>
            </div>
            <div class="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center">
              <TrendingUp class="w-5 h-5 text-green-400" />
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <Tabs defaultValue="sessions" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="sessions">Sessions</TabsTrigger>
        <TabsTrigger value="details">Session Details</TabsTrigger>
      </TabsList>

      <TabsContent value="sessions" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">Session History</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow class="border-slate-800">
                  <TableHead class="text-slate-400">Date</TableHead>
                  <TableHead class="text-slate-400">Duration</TableHead>
                  <TableHead class="text-slate-400">Gifts</TableHead>
                  <TableHead class="text-slate-400">Diamonds</TableHead>
                  <TableHead class="text-slate-400">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                <TableRow
                  v-for="session in sessions"
                  :key="session.sessionId"
                  class="border-slate-800"
                >
                  <TableCell class="text-white">
                    {{ new Date(session.startTime).toLocaleDateString() }}
                  </TableCell>
                  <TableCell class="text-slate-300">{{ getSessionDuration(session) }} min</TableCell>
                  <TableCell class="text-slate-300">
                    {{ session.totalGifts.toLocaleString() }}
                  </TableCell>
                  <TableCell class="text-purple-400 font-medium">
                    {{ session.totalDiamonds.toLocaleString() }}
                  </TableCell>
                  <TableCell>
                    <div class="flex items-center gap-2">
                      <Button
                        variant="ghost"
                        size="sm"
                        class="text-blue-400 hover:text-blue-300"
                        @click="selectedSession = session"
                      >
                        View
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        class="text-green-400 hover:text-green-300"
                        @click="handleExportExcel(session)"
                      >
                        <FileSpreadsheet class="w-4 h-4 mr-1" />
                        Export
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        class="text-red-400 hover:text-red-300"
                        @click="handleDeleteSession(session.sessionId)"
                      >
                        <Trash2 class="w-4 h-4" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>

                <TableRow v-if="sessions.length === 0">
                  <TableCell colspan="5" class="text-center py-8 text-slate-500">
                    <BarChart3 class="w-12 h-12 mx-auto mb-3 opacity-50" />
                    <p>No session data available</p>
                  </TableCell>
                </TableRow>
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="details" class="space-y-4">
        <template v-if="selectedSession">
          <!-- Anchor Stats -->
          <Card class="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle class="text-white flex items-center gap-2">
                <Users class="w-5 h-5 text-blue-400" />
                Anchor Statistics
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow class="border-slate-800">
                    <TableHead class="text-slate-400">Anchor</TableHead>
                    <TableHead class="text-slate-400">Total Score</TableHead>
                    <TableHead class="text-slate-400">Gift Count</TableHead>
                    <TableHead class="text-slate-400">Unique Users</TableHead>
                    <TableHead class="text-slate-400">Rounds Won</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow
                    v-for="stat in selectedSession.anchorStats"
                    :key="stat.anchorId"
                    class="border-slate-800"
                  >
                    <TableCell class="text-white font-medium">{{ stat.anchorName }}</TableCell>
                    <TableCell class="text-purple-400">{{ stat.totalScore.toLocaleString() }}</TableCell>
                    <TableCell class="text-slate-300">{{ stat.giftCount }}</TableCell>
                    <TableCell class="text-slate-300">{{ stat.uniqueUsers }}</TableCell>
                    <TableCell class="text-amber-400">{{ stat.roundsWon }}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <!-- Gift Stats -->
          <Card class="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle class="text-white flex items-center gap-2">
                <Gift class="w-5 h-5 text-pink-400" />
                Gift Statistics
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow class="border-slate-800">
                    <TableHead class="text-slate-400">Gift</TableHead>
                    <TableHead class="text-slate-400">Total Count</TableHead>
                    <TableHead class="text-slate-400">Total Value</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow
                    v-for="stat in selectedSession.giftStats"
                    :key="stat.giftId"
                    class="border-slate-800"
                  >
                    <TableCell class="text-white font-medium">{{ stat.giftName }}</TableCell>
                    <TableCell class="text-slate-300">{{ stat.totalCount }}</TableCell>
                    <TableCell class="text-purple-400">{{ stat.totalValue.toLocaleString() }}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          <!-- Top Users -->
          <Card class="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle class="text-white flex items-center gap-2">
                <Diamond class="w-5 h-5 text-amber-400" />
                Top Contributors
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow class="border-slate-800">
                    <TableHead class="text-slate-400">User</TableHead>
                    <TableHead class="text-slate-400">Total Spent</TableHead>
                    <TableHead class="text-slate-400">Gift Count</TableHead>
                    <TableHead class="text-slate-400">Favorite Anchor</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  <TableRow
                    v-for="stat in selectedSession.userStats"
                    :key="stat.userId"
                    class="border-slate-800"
                  >
                    <TableCell class="text-white font-medium">{{ stat.userName }}</TableCell>
                    <TableCell class="text-purple-400">{{ stat.totalSpent.toLocaleString() }}</TableCell>
                    <TableCell class="text-slate-300">{{ stat.giftCount }}</TableCell>
                    <TableCell class="text-blue-400">{{ stat.favoriteAnchor }}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </template>
        <Card v-else class="bg-slate-900 border-slate-800">
          <CardContent class="p-8 text-center">
            <BarChart3 class="w-16 h-16 mx-auto mb-4 text-slate-600" />
            <p class="text-slate-400">Select a session from the Sessions tab to view details</p>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  BarChart3,
  Calendar,
  Trash2,
  FileSpreadsheet,
  Users,
  Gift,
  Diamond,
  TrendingUp,
  Filter,
} from 'lucide-vue-next'
import type { ReportData } from '@/types'

const { t } = useI18n()
const { toast } = useToast()

const mockSessions: ReportData[] = [
  {
    sessionId: 'session-1',
    roomId: 'room-1',
    startTime: Date.now() - 86400000 * 2,
    endTime: Date.now() - 86400000 * 2 + 3600000,
    totalGifts: 1250,
    totalDiamonds: 45600,
    anchorStats: [
      { anchorId: 'a1', anchorName: 'Alice', totalScore: 15000, giftCount: 450, uniqueUsers: 89, roundsWon: 3 },
      { anchorId: 'a2', anchorName: 'Bob', totalScore: 12000, giftCount: 380, uniqueUsers: 76, roundsWon: 2 },
      { anchorId: 'a3', anchorName: 'Carol', totalScore: 9800, giftCount: 320, uniqueUsers: 65, roundsWon: 1 },
    ],
    userStats: [
      { userId: 'u1', userName: 'Fan1', totalSpent: 5000, giftCount: 50, favoriteAnchor: 'Alice' },
      { userId: 'u2', userName: 'Fan2', totalSpent: 3200, giftCount: 35, favoriteAnchor: 'Bob' },
    ],
    giftStats: [
      { giftId: 'g1', giftName: 'Rose', totalCount: 500, totalValue: 5000 },
      { giftId: 'g2', giftName: 'TikTok', totalCount: 200, totalValue: 20000 },
    ],
  },
  {
    sessionId: 'session-2',
    roomId: 'room-1',
    startTime: Date.now() - 86400000,
    endTime: Date.now() - 86400000 + 7200000,
    totalGifts: 2100,
    totalDiamonds: 78900,
    anchorStats: [
      { anchorId: 'a1', anchorName: 'Alice', totalScore: 25000, giftCount: 700, uniqueUsers: 120, roundsWon: 5 },
      { anchorId: 'a2', anchorName: 'Bob', totalScore: 18000, giftCount: 550, uniqueUsers: 95, roundsWon: 3 },
    ],
    userStats: [
      { userId: 'u3', userName: 'Fan3', totalSpent: 8000, giftCount: 80, favoriteAnchor: 'Alice' },
    ],
    giftStats: [
      { giftId: 'g1', giftName: 'Rose', totalCount: 800, totalValue: 8000 },
      { giftId: 'g3', giftName: 'Crown', totalCount: 50, totalValue: 25000 },
    ],
  },
]

const sessions = ref<ReportData[]>(mockSessions)
const selectedSession = ref<ReportData | null>(null)
const dateFilter = ref('')

const totalStats = computed(() =>
  sessions.value.reduce(
    (acc, session) => ({
      totalGifts: acc.totalGifts + session.totalGifts,
      totalDiamonds: acc.totalDiamonds + session.totalDiamonds,
      totalSessions: acc.totalSessions + 1,
    }),
    { totalGifts: 0, totalDiamonds: 0, totalSessions: 0 }
  )
)

const handleExportExcel = async (session: ReportData) => {
  toast({
    title: 'Exporting...',
    description: 'Excel report is being generated',
  })

  setTimeout(() => {
    toast({
      title: 'Export Complete',
      description: `Session report_${session.sessionId}.xlsx downloaded`,
    })
  }, 1500)
}

const handleDeleteSession = (sessionId: string) => {
  sessions.value = sessions.value.filter((session) => session.sessionId !== sessionId)
  if (selectedSession.value?.sessionId === sessionId) {
    selectedSession.value = null
  }
  toast({
    title: 'Session Deleted',
    description: 'Session data has been removed',
  })
}

const getSessionDuration = (session: ReportData) =>
  Math.round((session.endTime - session.startTime) / 60000)
</script>
