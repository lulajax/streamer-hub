<template>
  <div class="space-y-6 p-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <BarChart3 class="w-6 h-6 text-blue-400" />
          {{ t('reports') }}
        </h1>
        <p class="text-slate-400">Session summary and analytics.</p>
      </div>
      <Button variant="secondary" @click="handleExport">
        <Download class="w-4 h-4 mr-2" />
        {{ t('exportReport') }}
      </Button>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Gift class="w-5 h-5 text-pink-400" />
          {{ t('giftStats') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Gift</TableHead>
              <TableHead>User</TableHead>
              <TableHead>Count</TableHead>
              <TableHead>Diamonds</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="record in giftRecords" :key="record.id">
              <TableCell>{{ record.giftName }}</TableCell>
              <TableCell>{{ record.userName }}</TableCell>
              <TableCell>{{ record.giftCount }}</TableCell>
              <TableCell>{{ record.totalCost }}</TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Users class="w-5 h-5 text-green-400" />
          {{ t('anchorStats') }}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Anchor</TableHead>
              <TableHead>Total Score</TableHead>
              <TableHead>Status</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            <TableRow v-for="anchor in anchors" :key="anchor.id">
              <TableCell>{{ anchor.name }}</TableCell>
              <TableCell>{{ anchor.totalScore }}</TableCell>
              <TableCell>
                <span :class="anchor.isEliminated ? 'text-red-400' : 'text-green-400'">
                  {{ anchor.isEliminated ? 'Eliminated' : 'Active' }}
                </span>
              </TableCell>
            </TableRow>
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { BarChart3, Download, Gift, Users } from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const giftRecords = computed(() => store.giftRecords.slice(0, 10))
const anchors = computed(() => store.anchors)

const handleExport = () => {
  toast({
    title: 'Exported',
    description: 'Report export started.',
  })
}
</script>
