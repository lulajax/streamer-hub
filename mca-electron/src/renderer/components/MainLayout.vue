<template>
  <div class="h-screen flex flex-col bg-slate-950">
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

    <div class="flex flex-1 overflow-hidden">
      <aside class="w-56 bg-slate-900 border-r border-slate-800 p-4 flex-shrink-0">
        <Tabs v-model="activeTab">
          <TabsList class="flex flex-col gap-2 bg-transparent p-0">
            <TabsTrigger
              v-for="item in navItems"
              :key="item.id"
              :value="item.id"
              class="w-full justify-start gap-2"
            >
              <component :is="item.icon" class="w-4 h-4" />
              {{ item.label }}
            </TabsTrigger>
          </TabsList>
        </Tabs>

        <div class="mt-6 space-y-2">
          <div class="flex items-center justify-between text-xs text-slate-500">
            <span>Subscription</span>
            <span v-if="isSubscriptionActive" class="text-green-400 flex items-center gap-1">
              <CheckCircle2 class="w-3 h-3" /> Active
            </span>
            <span v-else class="text-red-400 flex items-center gap-1">
              <XCircle class="w-3 h-3" /> Inactive
            </span>
          </div>
        </div>
      </aside>

      <main class="flex-1 overflow-hidden">
        <Tabs v-model="activeTab">
          <TabsContent value="dashboard" class="h-full">
            <Dashboard />
          </TabsContent>
          <TabsContent value="sticker" class="h-full">
            <StickerMode />
          </TabsContent>
          <TabsContent value="pk" class="h-full">
            <PKMode />
          </TabsContent>
          <TabsContent value="free" class="h-full">
            <FreeMode />
          </TabsContent>
          <TabsContent value="widgets" class="h-full">
            <Widgets />
          </TabsContent>
          <TabsContent value="reports" class="h-full">
            <Reports />
          </TabsContent>
          <TabsContent value="settings" class="h-full">
            <Settings />
          </TabsContent>
        </Tabs>
      </main>
    </div>
  </div>

  <Dialog v-model:open="isSwitchDialogOpen">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{{ t('switchRoom') }}</DialogTitle>
        <DialogDescription>Connect to a TikTok live room.</DialogDescription>
      </DialogHeader>

      <div class="space-y-4 mt-4">
        <Input v-model="roomInput" :placeholder="t('enterRoomId')" />
        <div class="flex gap-2 justify-end">
          <Button variant="secondary" @click="isSwitchDialogOpen = false">{{ t('cancel') }}</Button>
          <Button :disabled="isSwitching" @click="handleSwitchRoom(roomInput)">
            <Loader2 v-if="isSwitching" class="mr-2 h-4 w-4 animate-spin" />
            {{ t('connect') }}
          </Button>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
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
  CheckCircle2,
  XCircle,
  Loader2,
} from 'lucide-vue-next'

const { t, locale } = useI18n()
const { toast } = useToast()
const store = useStore()

const activeTab = ref('dashboard')
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
  if (!store.activation.isActivated) {
    toast({
      title: t('error'),
      description: 'Please activate your subscription before connecting a room',
      variant: 'destructive',
    })
    return
  }

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
