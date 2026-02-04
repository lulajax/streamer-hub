<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-white flex items-center gap-2">
        <LayoutTemplate class="w-6 h-6 text-purple-400" />
        {{ t('widgets') }}
      </h1>
      <p class="text-slate-400">Configure and manage OBS widgets</p>
    </div>

    <Tabs defaultValue="links" class="w-full">
      <TabsList class="bg-slate-900 border border-slate-800">
        <TabsTrigger value="links">Widget Links</TabsTrigger>
        <TabsTrigger value="settings">Widget Settings</TabsTrigger>
        <TabsTrigger value="guide">OBS Setup Guide</TabsTrigger>
      </TabsList>

      <TabsContent value="links" class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <Card
            v-for="widget in widgetConfigs"
            :key="widget.id"
            class="bg-slate-900 border-slate-800"
          >
            <CardContent class="p-4">
              <div class="flex items-start gap-4">
                <div class="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                  <component :is="widget.icon" class="w-6 h-6 text-purple-400" />
                </div>

                <div class="flex-1 min-w-0">
                  <h3 class="text-white font-medium">{{ widget.name }}</h3>
                  <p class="text-slate-400 text-sm">{{ widget.description }}</p>

                  <div class="flex items-center gap-2 mt-2">
                    <Badge variant="secondary" class="text-xs">
                      {{ widget.recommendedSize }}
                    </Badge>
                    <Badge variant="outline" class="text-xs text-slate-400">
                      {{ widget.type }}
                    </Badge>
                  </div>

                  <div class="flex items-center gap-2 mt-3">
                    <Input
                      :modelValue="widget.url"
                      readonly
                      class="flex-1 bg-slate-800 border-slate-700 text-slate-400 text-sm"
                    />
                    <Button
                      variant="outline"
                      size="icon"
                      class="border-slate-700"
                      @click="handleCopyLink(widget.url, widget.id)"
                    >
                      <Check v-if="copiedId === widget.id" class="w-4 h-4 text-green-400" />
                      <Copy v-else class="w-4 h-4" />
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      class="border-slate-700"
                      @click="handleOpenWidget(widget.url, widget.name)"
                    >
                      <ExternalLink class="w-4 h-4" />
                    </Button>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </TabsContent>

      <TabsContent value="settings" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">Widget Display Settings</CardTitle>
          </CardHeader>
          <CardContent class="space-y-6">
            <!-- Display Options -->
            <div class="space-y-4">
              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <Eye class="w-5 h-5 text-blue-400" />
                  <div>
                    <p class="text-white font-medium">Show Anchor Name</p>
                    <p class="text-slate-400 text-sm">Display anchor names in widget</p>
                  </div>
                </div>
                <Switch v-model="settings.showAnchorName" />
              </div>

              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <LayoutTemplate class="w-5 h-5 text-green-400" />
                  <div>
                    <p class="text-white font-medium">Show Gift Count</p>
                    <p class="text-slate-400 text-sm">Display received gift counts</p>
                  </div>
                </div>
                <Switch v-model="settings.showGiftCount" />
              </div>

              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <BarChart3 class="w-5 h-5 text-amber-400" />
                  <div>
                    <p class="text-white font-medium">Show Total Score</p>
                    <p class="text-slate-400 text-sm">Display total score in widget</p>
                  </div>
                </div>
                <Switch v-model="settings.showTotalScore" />
              </div>

              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <Monitor class="w-5 h-5 text-purple-400" />
                  <div>
                    <p class="text-white font-medium">Show Progress Bar</p>
                    <p class="text-slate-400 text-sm">Display score progress bar</p>
                  </div>
                </div>
                <Switch v-model="settings.showProgressBar" />
              </div>

              <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div class="flex items-center gap-3">
                  <LayoutTemplate class="w-5 h-5 text-pink-400" />
                  <div>
                    <p class="text-white font-medium">Show Countdown</p>
                    <p class="text-slate-400 text-sm">Display countdown timer</p>
                  </div>
                </div>
                <Switch v-model="settings.showCountdown" />
              </div>
            </div>

            <!-- Theme -->
            <div>
              <label class="text-sm font-medium text-slate-300 mb-2 block">Widget Theme</label>
              <Select v-model="settings.theme">
                <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="default">Default</SelectItem>
                  <SelectItem value="neon">Neon</SelectItem>
                  <SelectItem value="minimal">Minimal</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <!-- Layout -->
            <div>
              <label class="text-sm font-medium text-slate-300 mb-2 block">Default Layout</label>
              <div class="flex gap-4">
                <button
                  class="flex-1 p-4 rounded-lg border transition-colors"
                  :class="settings.layout === 'horizontal'
                    ? 'border-blue-500 bg-blue-500/10'
                    : 'border-slate-700 bg-slate-800'"
                  @click="settings.layout = 'horizontal'"
                >
                  <Monitor class="w-6 h-6 text-blue-400 mx-auto mb-2" />
                  <p class="text-white text-center">Horizontal</p>
                </button>
                <button
                  class="flex-1 p-4 rounded-lg border transition-colors"
                  :class="settings.layout === 'vertical'
                    ? 'border-purple-500 bg-purple-500/10'
                    : 'border-slate-700 bg-slate-800'"
                  @click="settings.layout = 'vertical'"
                >
                  <Smartphone class="w-6 h-6 text-purple-400 mx-auto mb-2" />
                  <p class="text-white text-center">Vertical</p>
                </button>
              </div>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="guide" class="space-y-4">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">OBS Studio Setup</CardTitle>
          </CardHeader>
          <CardContent class="space-y-6">
            <div class="space-y-4">
              <div class="p-4 bg-slate-800 rounded-lg">
                <h3 class="text-white font-medium mb-2">Step 1: Copy Widget URL</h3>
                <p class="text-slate-400 text-sm">
                  Go to the Widget Links tab and copy the URL for the widget you want to add.
                </p>
              </div>

              <div class="p-4 bg-slate-800 rounded-lg">
                <h3 class="text-white font-medium mb-2">Step 2: Add Browser Source</h3>
                <p class="text-slate-400 text-sm">
                  In OBS Studio, click the + button in Sources and select "Browser".
                </p>
              </div>

              <div class="p-4 bg-slate-800 rounded-lg">
                <h3 class="text-white font-medium mb-2">Step 3: Configure Source</h3>
                <ul class="text-slate-400 text-sm space-y-1 list-disc list-inside">
                  <li>Name your source (e.g., "MCA Sticker Widget")</li>
                  <li>Paste the widget URL in the URL field</li>
                  <li>Set width and height to the recommended size</li>
                  <li>Check "Shutdown source when not visible"</li>
                  <li>Click OK</li>
                </ul>
              </div>

              <div class="p-4 bg-slate-800 rounded-lg">
                <h3 class="text-white font-medium mb-2">Step 4: Position Widget</h3>
                <p class="text-slate-400 text-sm">
                  Drag and resize the widget in your OBS preview to position it where you want.
                  Right-click the source and select Transform > Fit to Screen for best results.
                </p>
              </div>
            </div>

            <div class="p-4 bg-amber-500/10 border border-amber-500/30 rounded-lg">
              <p class="text-amber-200 text-sm">
                <strong>Tip:</strong> For transparent backgrounds, make sure your widget has a transparent
                background enabled. The widget will blend seamlessly with your stream overlay.
              </p>
            </div>
          </CardContent>
        </Card>

        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white">TikTok LIVE Studio Setup</CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="p-4 bg-slate-800 rounded-lg">
              <h3 class="text-white font-medium mb-2">Using Widget in LIVE Studio</h3>
              <ul class="text-slate-400 text-sm space-y-1 list-disc list-inside">
                <li>Open TikTok LIVE Studio</li>
                <li>Click "Add Source" and select "Web"</li>
                <li>Enter the widget URL</li>
                <li>Set custom resolution as recommended</li>
                <li>Adjust position and size as needed</li>
              </ul>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import { useToast } from '@/hooks/use-toast'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Switch from '@/components/ui/Switch.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import {
  LayoutTemplate,
  Copy,
  ExternalLink,
  Monitor,
  Smartphone,
  Snowflake,
  UserCircle,
  BarChart3,
  Check,
  Eye,
} from 'lucide-vue-next'
import type { WidgetSettings } from '@/types'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const copiedId = ref<string | null>(null)
const settings = ref<WidgetSettings>({
  showAnchorName: true,
  showGiftCount: true,
  showTotalScore: true,
  showProgressBar: true,
  showCountdown: true,
  theme: 'default',
  layout: 'horizontal',
})

const handleCopyLink = (url: string, id: string) => {
  navigator.clipboard.writeText(url)
  copiedId.value = id
  toast({
    title: 'Link Copied',
    description: 'Widget URL copied to clipboard',
  })
  setTimeout(() => {
    copiedId.value = null
  }, 2000)
}

const handleOpenWidget = (url: string, title: string) => {
  window.electronAPI.widget.open(url, title)
  toast({
    title: 'Widget Opened',
    description: `${title} window opened`,
  })
}

const generateWidgetUrl = (type: string, params: Record<string, string>) => {
  const baseUrl = 'http://localhost:8080/widget'
  const queryParams = new URLSearchParams({ type, ...params })
  return `${baseUrl}?${queryParams.toString()}`
}

const widgetConfigs = computed(() => [
  {
    id: 'main-sticker',
    type: 'main',
    name: 'Sticker Mode Main Widget',
    description: 'Main display for sticker dance mode',
    url: generateWidgetUrl('sticker-main', { preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '800x600',
    icon: LayoutTemplate,
  },
  {
    id: 'main-pk',
    type: 'main',
    name: 'PK Mode Main Widget',
    description: 'PK progress bar and team scores',
    url: generateWidgetUrl('pk-main', { preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '1000x200',
    icon: BarChart3,
  },
  {
    id: 'main-free',
    type: 'main',
    name: 'Free Mode Main Widget',
    description: 'Current anchor and score display',
    url: generateWidgetUrl('free-main', { preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '600x400',
    icon: UserCircle,
  },
  {
    id: 'leaderboard-h',
    type: 'leaderboard',
    name: 'Leaderboard (Horizontal)',
    description: 'Horizontal anchor ranking display',
    url: generateWidgetUrl('leaderboard', { layout: 'horizontal', preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '1200x150',
    icon: Monitor,
  },
  {
    id: 'leaderboard-v',
    type: 'leaderboard',
    name: 'Leaderboard (Vertical)',
    description: 'Vertical anchor ranking display',
    url: generateWidgetUrl('leaderboard', { layout: 'vertical', preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '300x800',
    icon: Smartphone,
  },
  {
    id: 'freeze-effect',
    type: 'freeze-effect',
    name: 'Freeze Effect Widget',
    description: 'Visual freeze effect for PK mode',
    url: generateWidgetUrl('freeze', { preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '500x1109',
    icon: Snowflake,
  },
  {
    id: 'avatar-frame',
    type: 'avatar-frame',
    name: 'Avatar Frame Widget',
    description: 'Dynamic avatar frame with effects',
    url: generateWidgetUrl('avatar-frame', { preset: store.currentPreset?.id || 'default' }),
    recommendedSize: '400x400',
    icon: UserCircle,
  },
])
</script>
