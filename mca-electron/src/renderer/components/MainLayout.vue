<template>
  <div class="h-screen flex flex-col bg-slate-950">
    <!-- Header -->
    <header class="h-14 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-4 flex-shrink-0">
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
          <span class="text-white font-bold text-sm">M</span>
        </div>
        <span class="text-white font-semibold">MCA</span>
        <Badge variant="secondary" class="bg-slate-800 text-slate-300">v1.0.0</Badge>
      </div>

      <div class="flex items-center gap-4">
        <div v-if="currentRoom?.isConnected" class="flex items-center gap-2">
          <Radio class="w-4 h-4 text-green-400 animate-pulse" />
          <span class="text-slate-300 text-sm">@{{ currentRoom.tiktokId }}</span>
        </div>

        <div class="flex items-center gap-2">
          <Globe class="w-4 h-4 text-slate-400" />
          <select
            :value="locale"
            class="bg-slate-800 text-slate-300 text-sm rounded px-2 py-1 border border-slate-700"
            @change="onChangeLanguage"
          >
            <option value="zh">中文</option>
            <option value="en">English</option>
            <option value="vi">Tiếng Việt</option>
            <option value="th">ไทย</option>
          </select>
        </div>

        <Button
          variant="ghost"
          size="sm"
          class="text-slate-400 hover:text-red-400 hover:bg-red-500/10"
          @click="isSwitchDialogOpen = true"
        >
          <Power class="w-4 h-4 mr-1" />
          {{ t('switchRoom') }}
        </Button>

        <Button
          variant="ghost"
          size="sm"
          class="text-slate-400 hover:text-amber-400 hover:bg-amber-500/10"
          @click="handleLogout"
        >
          <LogOut class="w-4 h-4 mr-1" />
          {{ t('logout') }}
        </Button>
      </div>
    </header>

    <!-- Main Content -->
    <div class="flex-1 flex overflow-hidden">
      <!-- Sidebar -->
      <aside class="w-56 bg-slate-900 border-r border-slate-800 flex-shrink-0">
        <ScrollArea class="h-full">
          <nav class="p-2 space-y-1">
            <button
              v-for="item in navItems"
              :key="item.id"
              @click="activeTab = item.id"
              :class="[
                'w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors',
                activeTab === item.id
                  ? 'bg-blue-500/20 text-blue-400'
                  : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
              ]"
            >
              <component :is="item.icon" class="w-4 h-4" />
              {{ item.label }}
            </button>
          </nav>

          <div class="p-4 mt-auto space-y-3">
            <!-- User Info -->
            <div v-if="store.user" class="bg-slate-800/50 rounded-lg p-3">
              <p class="text-slate-400 text-xs mb-1">{{ t('user') }}</p>
              <p class="text-slate-200 text-sm font-medium">{{ store.user.nickname || store.user.email }}</p>
              <p class="text-slate-500 text-xs mt-2">
                Subscription: {{ subscriptionType }} · {{ isSubscriptionActive ? 'Active' : 'Inactive' }}
              </p>
              <p class="text-slate-500 text-xs mt-2">
                {{ t('validUntil') }}: {{ subscriptionExpiresAt ? new Date(subscriptionExpiresAt).toLocaleDateString() : 'N/A' }}
              </p>
            </div>
            <!-- Device Info -->
            <div class="bg-slate-800/50 rounded-lg p-3">
              <p class="text-slate-400 text-xs mb-1">{{ t('deviceName') }}</p>
              <p class="text-slate-200 text-sm font-medium">{{ store.activation.deviceName || 'Unknown' }}</p>
            </div>
          </div>
        </ScrollArea>
      </aside>

      <!-- Content Area -->
      <main class="flex-1 overflow-hidden bg-slate-950">
        <ScrollArea class="h-full">
          <div class="p-6">
            <Dashboard v-if="activeTab === 'dashboard'" />
            <StickerMode v-if="activeTab === 'sticker'" />
            <PKMode v-if="activeTab === 'pk'" />
            <FreeMode v-if="activeTab === 'free'" />
            <Widgets v-if="activeTab === 'widgets'" />
            <Reports v-if="activeTab === 'reports'" />
            <Settings v-if="activeTab === 'settings'" />
          </div>
        </ScrollArea>
      </main>
    </div>

    <Dialog :open="isSwitchDialogOpen" @update:open="isSwitchDialogOpen = $event">
      <DialogContent class="bg-slate-900 border-slate-800 text-white">
        <DialogHeader>
          <DialogTitle class="text-white">{{ t('switchRoom') }}</DialogTitle>
          <DialogDescription class="text-slate-400">
            {{ t('enterRoomId') }}
          </DialogDescription>
        </DialogHeader>

        <div class="space-y-4 mt-4">
          <div class="flex items-center gap-3">
            <Input
              v-model="roomInput"
              :placeholder="t('enterRoomId')"
              class="flex-1 bg-slate-950 border-slate-700 text-white"
              @keydown.enter="handleSwitchRoom(roomInput)"
            />
            <Button
              :disabled="isSwitching || !roomInput"
              class="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              @click="handleSwitchRoom(roomInput)"
            >
              <Loader2 v-if="isSwitching" class="mr-2 h-4 w-4 animate-spin" />
              {{ isSwitching ? t('loading') : t('connect') }}
            </Button>
          </div>

          <div v-if="store.recentRooms.length > 0" class="space-y-2">
            <div class="flex items-center gap-2 text-slate-400 text-sm">
              <History class="w-4 h-4" />
              {{ t('recentRooms') }}
            </div>
            <div class="space-y-2">
              <button
                v-for="room in store.recentRooms"
                :key="room.id"
                class="w-full flex items-center justify-between p-3 bg-slate-950/60 rounded-lg hover:bg-slate-950 transition-colors"
                @click="handleSwitchRoom(room.tiktokId)"
              >
                <div class="flex items-center gap-3">
                  <div class="w-8 h-8 bg-gradient-to-br from-pink-500 to-rose-600 rounded-full flex items-center justify-center">
                    <span class="text-white font-bold text-xs">@{{ room.tiktokId[0].toUpperCase() }}</span>
                  </div>
                  <div class="text-left">
                    <p class="text-white text-sm font-medium">@{{ room.tiktokId }}</p>
                    <p class="text-slate-400 text-xs">
                      {{ room.connectedAt ? new Date(room.connectedAt).toLocaleDateString() : 'Unknown date' }}
                    </p>
                  </div>
                </div>
                <Badge
                  :class="room.isConnected ? 'bg-green-500' : ''"
                  :variant="room.isConnected ? 'default' : 'secondary'"
                >
                  <template v-if="room.isConnected">
                    <CheckCircle2 class="w-3 h-3 mr-1" />
                    {{ t('connected') }}
                  </template>
                  <template v-else>
                    <XCircle class="w-3 h-3 mr-1" />
                    {{ t('disconnected') }}
                  </template>
                </Badge>
              </button>
            </div>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { listUserAnchors } from '@/lib/anchorsApi'
import { getDefaultPreset, listPresets } from '@/lib/presetsApi'
import { mapApiPreset, mapApiUserAnchor } from '@/lib/mappers'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Input from '@/components/ui/Input.vue'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import ScrollArea from '@/components/ui/ScrollArea.vue'
import Dashboard from '@/pages/Dashboard.vue'
import StickerMode from '@/pages/StickerMode.vue'
import PKMode from '@/pages/PKMode.vue'
import FreeMode from '@/pages/FreeMode.vue'
import Widgets from '@/pages/Widgets.vue'
import Reports from '@/pages/Reports.vue'
import Settings from '@/pages/Settings.vue'
import {
  LayoutDashboard,
  Sticker,
  Swords,
  Users,
  LayoutTemplate,
  BarChart3,
  Settings2,
  Radio,
  Power,
  Globe,
  LogOut,
  History,
  CheckCircle2,
  XCircle,
  Loader2,
} from 'lucide-vue-next'

const { t, locale } = useI18n()
const { toast } = useToast()
const store = useStore()

const activeTab = computed({
  get: () => store.activeTab,
  set: (value) => store.setActiveTab(value),
})
const isSwitchDialogOpen = ref(false)
const roomInput = ref('')
const isSwitching = ref(false)

const currentRoom = computed(() => store.currentRoom)

const subscriptionExpiresAt = computed(() => store.user?.subscriptionExpiresAt)
const subscriptionType = computed(() => store.user?.subscriptionType ?? 'FREE')
const isSubscriptionActive = computed(() => {
  if (subscriptionType.value === 'FREE') return false
  return subscriptionExpiresAt.value ? Date.now() < subscriptionExpiresAt.value : false
})

const navItems = computed(() => [
  { id: 'dashboard', label: t('dashboard'), icon: LayoutDashboard },
  { id: 'sticker', label: t('stickerMode'), icon: Sticker },
  { id: 'pk', label: t('pkMode'), icon: Swords },
  { id: 'free', label: t('freeMode'), icon: Users },
  { id: 'widgets', label: t('widgets'), icon: LayoutTemplate },
  { id: 'reports', label: t('reports'), icon: BarChart3 },
  { id: 'settings', label: t('settings'), icon: Settings2 },
])

const extractRoomId = (input: string): string | null => {
  const urlMatch = input.match(/@([^/\s]+)/)
  if (urlMatch) {
    return urlMatch[1]
  }
  if (/^[a-zA-Z0-9_.]+$/.test(input)) {
    return input
  }
  return null
}

const handleSwitchRoom = async (input: string) => {
  if (!isSubscriptionActive.value) {
    toast({
      title: t('error'),
      description: 'Your subscription has expired. Please renew to continue.',
      variant: 'destructive',
    })
    return
  }

  const roomId = extractRoomId(input)
  if (!roomId) {
    toast({
      title: t('error'),
      description: 'Invalid TikTok room ID or URL',
      variant: 'destructive',
    })
    return
  }

  isSwitching.value = true
  try {
    if (currentRoom.value?.isConnected) {
      await window.electronAPI.monitor.close()
    }

    const result = await window.electronAPI.monitor.open(roomId)
    if (result.success) {
      const room = {
        id: `room-${Date.now()}`,
        tiktokId: roomId,
        isConnected: true,
        connectedAt: Date.now(),
      }
      store.setCurrentRoom(room)
      store.addRecentRoom(room)
      isSwitchDialogOpen.value = false
      roomInput.value = ''

      toast({
        title: t('success'),
        description: `Connected to @${roomId}`,
      })
    }
  } catch {
    toast({
      title: t('error'),
      description: 'Failed to connect to room',
      variant: 'destructive',
    })
  } finally {
    isSwitching.value = false
  }
}

const loadPresetData = async () => {
  if (!store.user?.accessToken || !isSubscriptionActive.value) return

  try {
    const [anchorsResponse, presetsResponse] = await Promise.all([
      listUserAnchors(),
      listPresets(),
    ])

    if (anchorsResponse?.success) {
      const anchors = (anchorsResponse.data ?? []).map(mapApiUserAnchor)
      store.setUserAnchors(anchors)
    } else {
      store.setUserAnchors([])
    }

    if (presetsResponse?.success) {
      const presets = (presetsResponse.data ?? []).map(mapApiPreset)
      store.setPresets(presets)

      const resolveSelection = (mode: 'sticker' | 'pk' | 'free') => {
        const existingId = store.presetSelections[mode]
        const existing = existingId ? presets.find((item) => item.id === existingId) : null
        if (existing) return existing
        const candidates = presets.filter((item) => item.mode === mode)
        return candidates.find((item) => item.isDefault) ?? candidates[0] ?? null
      }

      const stickerPreset = resolveSelection('sticker')
      const pkPreset = resolveSelection('pk')
      const freePreset = resolveSelection('free')

      store.setPresetSelection('sticker', stickerPreset?.id ?? null)
      store.setPresetSelection('pk', pkPreset?.id ?? null)
      store.setPresetSelection('free', freePreset?.id ?? null)

      const modeTab = store.activeTab === 'sticker' || store.activeTab === 'pk' || store.activeTab === 'free'
        ? store.activeTab
        : null
      if (modeTab) {
        store.selectPresetForMode(modeTab, resolveSelection(modeTab))
      } else {
        const defaultResponse = await getDefaultPreset()
        if (defaultResponse?.success && defaultResponse.data) {
          store.setCurrentPreset(mapApiPreset(defaultResponse.data))
        } else {
          store.setCurrentPreset(stickerPreset ?? pkPreset ?? freePreset ?? presets[0] ?? null)
        }
      }
    } else {
      store.setPresets([])
      store.setCurrentPreset(null)
    }
  } catch (err) {
    console.error('Failed to load presets/anchors', err)
    toast({
      title: t('error'),
      description: t('networkError'),
      variant: 'destructive',
    })
  }
}

watch(
  () => [store.user?.accessToken, isSubscriptionActive.value],
  () => {
    loadPresetData()
  },
  { immediate: true }
)

watch(
  () => store.activeTab,
  (tab) => {
    if (tab === 'sticker' || tab === 'pk' || tab === 'free') {
      store.applyPresetSelection(tab)
    }
  }
)

const handleLogout = async () => {
  await window.electronAPI.monitor.close()
  store.setCurrentRoom(null)
  store.logout()
}

const onChangeLanguage = (event: Event) => {
  const value = (event.target as HTMLSelectElement).value
  locale.value = value
  store.setSettings({ language: value })
}
</script>
