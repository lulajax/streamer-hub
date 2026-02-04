<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
    <div class="w-full max-w-2xl">
      <!-- Header -->
      <div class="flex items-start justify-between mb-8">
        <div class="flex items-center justify-center gap-3 mb-4">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
            <Radio class="w-8 h-8 text-white" />
          </div>
        </div>
        <div class="flex-1 text-center">
          <h1 class="text-3xl font-bold text-white mb-2">{{ t('connectRoom') }}</h1>
          <p class="text-slate-400">Enter TikTok live room ID to start</p>
        </div>
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

      <!-- Room Input Card -->
      <Card class="bg-slate-800/50 border-slate-700 backdrop-blur mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Link2 class="w-5 h-5 text-blue-400" />
            {{ t('roomId') }}
          </CardTitle>
          <CardDescription class="text-slate-400">
            Enter TikTok username or full live URL
          </CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <div class="flex gap-3">
            <Input
              v-model="roomInput"
              placeholder="@username or https://www.tiktok.com/@username/live"
              class="flex-1 bg-slate-900 border-slate-600 text-white"
              @keydown.enter="handleConnect"
            />
            <Button
              :disabled="isConnecting || !roomInput"
              class="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              @click="handleConnect"
            >
              {{ isConnecting ? t('loading') : t('connect') }}
            </Button>
          </div>

          <div class="flex items-center gap-2 text-sm text-slate-400">
            <Sparkles class="w-4 h-4" />
            <span>Examples: @username, username, or full URL</span>
          </div>
        </CardContent>
      </Card>

      <!-- Activation Code Card -->
      <Card v-if="!isSubscriptionActive" class="bg-slate-800/50 border-slate-700 backdrop-blur mb-6">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <KeyRound class="w-5 h-5 text-amber-400" />
            Redeem Activation Code
          </CardTitle>
          <CardDescription class="text-slate-400">
            Enter your code to renew your subscription
          </CardDescription>
        </CardHeader>
        <CardContent class="space-y-3">
          <Input
            v-model="activationCode"
            placeholder="BAS-XXXXXXXXXXXX"
            maxlength="16"
            class="bg-slate-900 border-slate-600 text-white text-center tracking-widest font-mono text-lg"
          />
          <Button
            :disabled="isRedeeming"
            class="w-full bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700"
            @click="handleRedeem"
          >
            {{ isRedeeming ? t('loading') : 'Redeem Code' }}
          </Button>
          <p class="text-slate-500 text-xs text-center">
            Format: BAS-XXXXXXXXXXXX, PRO-XXXXXXXXXXXX, or ENT-XXXXXXXXXXXX
          </p>
        </CardContent>
      </Card>

      <!-- Recent Rooms -->
      <Card v-if="store.recentRooms.length > 0" class="bg-slate-800/50 border-slate-700 backdrop-blur">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2 text-lg">
            <History class="w-5 h-5 text-purple-400" />
            {{ t('recentRooms') }}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div class="space-y-2">
            <div
              v-for="room in store.recentRooms"
              :key="room.id"
              class="flex items-center justify-between p-3 bg-slate-900/50 rounded-lg hover:bg-slate-900 transition-colors cursor-pointer"
              @click="handleRecentConnect(room)"
            >
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 bg-gradient-to-br from-pink-500 to-rose-600 rounded-full flex items-center justify-center">
                  <span class="text-white font-bold text-sm">
                    @{{ room.tiktokId[0].toUpperCase() }}
                  </span>
                </div>
                <div>
                  <p class="text-white font-medium">@{{ room.tiktokId }}</p>
                  <p class="text-slate-400 text-sm">
                    {{ room.connectedAt ? new Date(room.connectedAt).toLocaleDateString() : 'Unknown date' }}
                  </p>
                </div>
              </div>
              <Badge
                :variant="room.isConnected ? 'default' : 'secondary'"
                :class="room.isConnected ? 'bg-green-500' : ''"
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
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- Monitor Notice -->
      <div class="mt-6 p-4 bg-amber-500/10 border border-amber-500/30 rounded-lg">
        <div class="flex items-start gap-3">
          <ExternalLink class="w-5 h-5 text-amber-400 flex-shrink-0 mt-0.5" />
          <div>
            <p class="text-amber-200 font-medium mb-1">Monitor Window Notice</p>
            <p class="text-amber-200/70 text-sm">
              A monitor window will open to capture live data. Please do not close or interact with it.
              If login is required, use a separate TikTok account (not your streaming account).
            </p>
          </div>
        </div>
      </div>

      <!-- Footer Info -->
      <div class="text-center mt-6 text-slate-500 text-sm space-y-1">
        <p>
          Subscription: {{ subscriptionType }} · {{ isSubscriptionActive ? 'Active' : 'Inactive' }}
        </p>
        <p>Device: {{ store.activation.deviceName }}</p>
        <p class="mt-1">
          Valid until: {{ store.activation.expiresAt ? new Date(store.activation.expiresAt).toLocaleDateString() : 'N/A' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import Badge from '@/components/ui/Badge.vue'
import { useToast } from '@/hooks/use-toast'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
import type { Room, UserDTO } from '@/types'
import {
  Radio,
  History,
  Link2,
  ExternalLink,
  CheckCircle2,
  XCircle,
  Sparkles,
  KeyRound,
  LogOut,
} from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const roomInput = ref('')
const isConnecting = ref(false)
const activationCode = ref('')
const isRedeeming = ref(false)

const subscriptionExpiresAt = computed(() => store.user?.subscriptionExpiresAt)
const subscriptionType = computed(() => store.user?.subscriptionType ?? 'FREE')
const isSubscriptionActive = computed(() => {
  return subscriptionType.value !== 'FREE' &&
    (subscriptionExpiresAt.value ? Date.now() < subscriptionExpiresAt.value : false)
})

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

const handleRedeem = async () => {
  let code = activationCode.value.trim().toUpperCase().replace(/\s+/g, '')
  if (!code) {
    toast({
      title: t('error'),
      description: 'Please enter activation code',
      variant: 'destructive',
    })
    return
  }
  if (/^(BAS|PRO|ENT)[A-Z0-9]{12}$/.test(code)) {
    code = `${code.slice(0, 3)}-${code.slice(3)}`
  }
  if (!/^(BAS|PRO|ENT)-[A-Z0-9]{12}$/.test(code)) {
    toast({
      title: t('error'),
      description: 'Invalid activation code format',
      variant: 'destructive',
    })
    return
  }
  if (!store.user?.accessToken) {
    toast({
      title: t('error'),
      description: 'Please login again',
      variant: 'destructive',
    })
    return
  }

  isRedeeming.value = true
  try {
    const data = await apiFetch<{ success: boolean; data?: UserDTO; error?: string }>('/auth/activate', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${store.user.accessToken}`,
      },
      json: { activationCode: code },
    })
    if (data?.success) {
      store.setUser(normalizeUserDates(data.data!))
      activationCode.value = ''
      toast({
        title: t('success'),
        description: 'Premium activated successfully!',
      })
    } else {
      toast({
        title: t('error'),
        description: data?.error || 'Activation failed',
        variant: 'destructive',
      })
    }
  } catch {
    toast({
      title: t('error'),
      description: 'Network error. Please try again.',
      variant: 'destructive',
    })
  } finally {
    isRedeeming.value = false
  }
}

const handleConnect = async () => {
  if (!store.activation?.isActivated) {
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

  const roomId = extractRoomId(roomInput.value)
  if (!roomId) {
    toast({
      title: t('error'),
      description: 'Invalid TikTok room ID or URL',
      variant: 'destructive',
    })
    return
  }

  isConnecting.value = true

  try {
    const result = await window.electronAPI.monitor.open(roomId)

    if (result.success) {
      const room: Room = {
        id: `room-${Date.now()}`,
        tiktokId: roomId,
        isConnected: true,
        connectedAt: Date.now(),
      }

      store.setCurrentRoom(room)
      store.addRecentRoom(room)

      toast({
        title: t('success'),
        description: `Connected to @${roomId}`,
      })
    }
  } catch (error) {
    toast({
      title: t('error'),
      description: 'Failed to connect to room',
      variant: 'destructive',
    })
  } finally {
    isConnecting.value = false
  }
}

const handleRecentConnect = async (room: Room) => {
  roomInput.value = room.tiktokId
  await handleConnect()
}

const handleLogout = () => {
  store.logout()
}
</script>
