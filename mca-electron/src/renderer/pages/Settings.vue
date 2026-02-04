<template>
  <div class="space-y-6 p-6">
    <div>
      <h1 class="text-2xl font-bold text-white flex items-center gap-2">
        <Settings2 class="w-6 h-6 text-slate-300" />
        {{ t('settings') }}
      </h1>
      <p class="text-slate-400">Manage general preferences.</p>
    </div>

    <Card class="bg-slate-900 border-slate-800">
      <CardHeader>
        <CardTitle class="text-white flex items-center gap-2">
          <Sliders class="w-5 h-5 text-blue-400" />
          {{ t('generalSettings') }}
        </CardTitle>
      </CardHeader>
      <CardContent class="space-y-4">
        <div class="space-y-2">
          <label class="text-sm text-slate-300">{{ t('language') }}</label>
          <select
            v-model="localSettings.language"
            class="w-full rounded border border-slate-700 bg-slate-900 p-2 text-sm text-slate-200"
          >
            <option value="zh">中文</option>
            <option value="en">English</option>
            <option value="vi">Tiếng Việt</option>
            <option value="th">ไทย</option>
          </select>
        </div>
        <div class="space-y-2">
          <label class="text-sm text-slate-300">{{ t('theme') }}</label>
          <select
            v-model="localSettings.theme"
            class="w-full rounded border border-slate-700 bg-slate-900 p-2 text-sm text-slate-200"
          >
            <option value="light">{{ t('light') }}</option>
            <option value="dark">{{ t('dark') }}</option>
            <option value="system">{{ t('system') }}</option>
          </select>
        </div>
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('notifications') }}</p>
            <p class="text-xs text-slate-500">Enable desktop notifications.</p>
          </div>
          <Switch v-model="localSettings.notifications" />
        </div>
        <div class="flex items-center justify-between">
          <div>
            <p class="text-sm text-slate-200">{{ t('autoSave') }}</p>
            <p class="text-xs text-slate-500">Auto-save game data.</p>
          </div>
          <Switch v-model="localSettings.autoSave" />
        </div>
        <div class="space-y-2">
          <label class="text-sm text-slate-300">{{ t('saveInterval') }} (sec)</label>
          <Input v-model="localSettings.saveInterval" type="number" />
        </div>
        <Button class="w-full" @click="handleSave">
          <Save class="w-4 h-4 mr-2" />
          {{ t('save') }}
        </Button>
      </CardContent>
    </Card>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Settings2, Sliders, Save } from 'lucide-vue-next'

const { t, locale } = useI18n()
const { toast } = useToast()
const store = useStore()

const localSettings = reactive({ ...store.settings })

const handleSave = () => {
  store.setSettings({ ...localSettings })
  locale.value = localSettings.language
  toast({
    title: 'Saved',
    description: 'Settings updated successfully.',
  })
}
</script>
