<template>
  <div class="space-y-6 p-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-white flex items-center gap-2">
          <LayoutTemplate class="w-6 h-6 text-purple-400" />
          {{ t('widgets') }}
        </h1>
        <p class="text-slate-400">Manage widget links and settings.</p>
      </div>
      <Button variant="secondary" @click="handleGenerate">
        <Link2 class="w-4 h-4 mr-2" />
        Generate Links
      </Button>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Link2 class="w-5 h-5 text-slate-300" />
          {{ t('widgetLinks') }}
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-3">
        <div v-if="widgets.length === 0" class="text-slate-400 text-sm">
          No widget links generated yet.
        </div>
        <div v-for="widget in widgets" :key="widget.id" class="flex items-center gap-3">
          <Input v-model="widget.url" class="flex-1" />
          <Button variant="outline" class="border-slate-700" @click="copyLink(widget.url)">
            <Copy class="w-4 h-4 mr-2" />
            {{ t('copyLink') }}
          </Button>
        </div>
      </CardContent>
    </Card>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Settings2 class="w-5 h-5 text-slate-300" />
          {{ t('widgetSettings') }}
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('showAnchorName') }}</p>
            <p class="text-xs text-slate-500">Display anchor names on widgets.</p>
          </div>
          <Switch v-model="settings.showAnchorName" />
        </div>
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('showGiftCount') }}</p>
            <p class="text-xs text-slate-500">Show gift counts in widgets.</p>
          </div>
          <Switch v-model="settings.showGiftCount" />
        </div>
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('showTotalScore') }}</p>
            <p class="text-xs text-slate-500">Show total scores.</p>
          </div>
          <Switch v-model="settings.showTotalScore" />
        </div>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { LayoutTemplate, Link2, Copy, Settings2 } from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const widgets = computed(() => store.widgets)

const settings = reactive({
  showAnchorName: true,
  showGiftCount: true,
  showTotalScore: true,
})

const handleGenerate = () => {
  toast({
    title: 'Generated',
    description: 'Widget links generated for current room.',
  })
}

const copyLink = async (link: string) => {
  await navigator.clipboard.writeText(link)
  toast({
    title: 'Copied',
    description: 'Link copied to clipboard.',
  })
}
</script>
