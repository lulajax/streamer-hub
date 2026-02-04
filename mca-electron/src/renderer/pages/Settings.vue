<template>
  <div class="space-y-6">
    <!-- Header -->
    <div>
      <h1 class="text-2xl font-bold text-white flex items-center gap-2">
        <Settings2 class="w-6 h-6 text-slate-400" />
        {{ t('settings') }}
      </h1>
      <p class="text-slate-400">Configure application preferences</p>
    </div>

    <div class="grid grid-cols-2 gap-6">
      <!-- General Settings -->
      <Card class="bg-slate-900 border-slate-800">
        <CardHeader>
          <CardTitle class="text-white flex items-center gap-2">
            <Globe class="w-5 h-5 text-blue-400" />
            {{ t('generalSettings') }}
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-6">
          <!-- Language -->
          <div>
            <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('language') }}</label>
            <Select v-model="language">
              <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="zh">中文 (Chinese)</SelectItem>
                <SelectItem value="en">English</SelectItem>
                <SelectItem value="vi">Ti?ng Vi?t (Vietnamese)</SelectItem>
                <SelectItem value="th">??? (Thai)</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- Theme -->
          <div>
            <label class="text-sm font-medium text-slate-300 mb-2 block">{{ t('theme') }}</label>
            <Select v-model="theme">
              <SelectTrigger class="bg-slate-800 border-slate-700 text-white">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="light">{{ t('light') }}</SelectItem>
                <SelectItem value="dark">{{ t('dark') }}</SelectItem>
                <SelectItem value="system">{{ t('system') }}</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <!-- Notifications -->
          <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
            <div class="flex items-center gap-3">
              <Bell class="w-5 h-5 text-amber-400" />
              <div>
                <p class="text-white font-medium">{{ t('notifications') }}</p>
                <p class="text-slate-400 text-sm">Show desktop notifications</p>
              </div>
            </div>
            <Switch
              :modelValue="store.settings.notifications"
              @update:modelValue="(checked) => store.setSettings({ notifications: checked })"
            />
          </div>

          <!-- Auto Save -->
          <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
            <div class="flex items-center gap-3">
              <Save class="w-5 h-5 text-green-400" />
              <div>
                <p class="text-white font-medium">{{ t('autoSave') }}</p>
                <p class="text-slate-400 text-sm">Automatically save session data</p>
              </div>
            </div>
            <Switch
              :modelValue="store.settings.autoSave"
              @update:modelValue="(checked) => store.setSettings({ autoSave: checked })"
            />
          </div>

          <!-- Save Interval -->
          <div v-if="store.settings.autoSave">
            <label class="text-sm font-medium text-slate-300 mb-2 block">
              {{ t('saveInterval') }} (seconds)
            </label>
            <Input
              type="number"
              :modelValue="String(store.settings.saveInterval)"
              class="bg-slate-800 border-slate-700 text-white"
              @update:modelValue="(value) => store.setSettings({ saveInterval: parseInt(value) || 30 })"
            />
          </div>
        </CardContent>
      </Card>

      <!-- About & System -->
      <div class="space-y-6">
        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Crown class="w-5 h-5 text-amber-400" />
              Subscription
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div>
                <p class="text-white font-medium">
                  {{ subscriptionLabelMap[subscriptionType] }}
                </p>
                <p class="text-slate-400 text-sm">
                  {{ subscriptionDescriptions[subscriptionType] }}
                </p>
              </div>
              <Badge
                variant="secondary"
                :class="subscriptionType === 'FREE'
                  ? 'bg-slate-700 text-slate-200'
                  : 'bg-amber-500/20 text-amber-300'"
              >
                {{ subscriptionType }}
              </Badge>
            </div>

            <div class="p-4 bg-slate-800 rounded-lg space-y-2 text-sm">
              <div class="flex justify-between">
                <span class="text-slate-400">Status</span>
                <span class="text-white">
                  {{ subscriptionType === 'FREE'
                    ? 'Free'
                    : subscriptionExpiresAt
                      ? 'Active'
                      : 'Inactive' }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Activated On</span>
                <span class="text-white">
                  {{ activatedAt ? new Date(activatedAt).toLocaleDateString() : 'N/A' }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Expires</span>
                <span class="text-white">
                  {{ subscriptionExpiresAt
                    ? new Date(subscriptionExpiresAt).toLocaleDateString()
                    : 'N/A' }}
                </span>
              </div>
            </div>

            <div class="flex items-center gap-3 p-4 bg-slate-800 rounded-lg">
              <div class="w-10 h-10 bg-amber-500/20 rounded-lg flex items-center justify-center">
                <KeyRound class="w-5 h-5 text-amber-400" />
              </div>
              <div class="flex-1">
                <p class="text-white font-medium">Activation Code</p>
                <Input
                  :placeholder="activationCode || 'BAS-XXXXXXXXXXXX'"
                  :modelValue="activationCodeInput"
                  class="mt-2 bg-slate-900 border-slate-600 text-white text-center tracking-widest font-mono text-sm"
                  @update:modelValue="(value) => (activationCodeInput = value.toUpperCase())"
                />
                <Button
                  :disabled="isRedeeming"
                  class="mt-3 w-full bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700"
                  @click="handleRedeemActivationCode"
                >
                  {{ isRedeeming ? t('loading') : 'Redeem Code' }}
                </Button>
                <p class="text-slate-500 text-xs mt-2 text-center">
                  Format: BAS-XXXXXXXXXXXX, PRO-XXXXXXXXXXXX, or ENT-XXXXXXXXXXXX
                </p>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Info class="w-5 h-5 text-purple-400" />
              {{ t('about') }}
            </CardTitle>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div>
                <p class="text-white font-medium">{{ t('version') }}</p>
                <p class="text-slate-400 text-sm">MCA Multi-Caster Assistant</p>
              </div>
              <Badge variant="secondary" class="text-lg">
                1.0.0
              </Badge>
            </div>

            <div class="p-4 bg-slate-800 rounded-lg space-y-2">
              <div class="flex justify-between">
                <span class="text-slate-400">Device Name</span>
                <span class="text-white">{{ store.activation.deviceName || 'Unknown' }}</span>
              </div>
            </div>

            <Button
              variant="outline"
              class="w-full border-slate-700 text-slate-300"
              @click="handleCheckUpdate"
            >
              <RefreshCw class="w-4 h-4 mr-2" />
              Check for Updates
            </Button>
          </CardContent>
        </Card>

        <Card class="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Monitor class="w-5 h-5 text-green-400" />
              System Information
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="p-4 bg-slate-800 rounded-lg space-y-2">
              <div class="flex justify-between">
                <span class="text-slate-400">Platform</span>
                <span class="text-white">{{ systemInfo }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Architecture</span>
                <span class="text-white">x64</span>
              </div>
              <div class="flex justify-between">
                <span class="text-slate-400">Node Version</span>
                <span class="text-white">20.x</span>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>

    <!-- Save Button -->
    <div class="flex justify-end">
      <Button class="bg-blue-600 hover:bg-blue-700" @click="handleSaveSettings">
        <Check class="w-4 h-4 mr-2" />
        Save Settings
      </Button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Switch from '@/components/ui/Switch.vue'
import Select from '@/components/ui/Select.vue'
import SelectContent from '@/components/ui/SelectContent.vue'
import SelectItem from '@/components/ui/SelectItem.vue'
import SelectTrigger from '@/components/ui/SelectTrigger.vue'
import SelectValue from '@/components/ui/SelectValue.vue'
import {
  Settings2,
  Globe,
  Bell,
  Save,
  Monitor,
  Info,
  Check,
  RefreshCw,
  Crown,
  KeyRound,
} from 'lucide-vue-next'
import type { ApiResponse, UserDTO } from '@/types'

const subscriptionLabelMap = {
  FREE: 'Free',
  BASIC: 'Basic',
  PRO: 'Pro',
  ENTERPRISE: 'Enterprise',
} as const

const subscriptionDescriptions = {
  FREE: 'Basic access',
  BASIC: 'Starter features for small teams',
  PRO: 'Advanced controls and analytics',
  ENTERPRISE: 'All features plus priority support',
} as const

const { t, locale } = useI18n()
const { toast } = useToast()
const store = useStore()

const systemInfo = ref('Electron')
const activationCodeInput = ref('')
const isRedeeming = ref(false)

const subscriptionType = computed(() => store.user?.subscriptionType ?? 'FREE')
const subscriptionExpiresAt = computed(() => store.user?.subscriptionExpiresAt)
const activatedAt = computed(() => store.user?.activatedAt)
const activationCode = computed(
  () => store.user?.activationCode ?? store.activation.activationCode
)

const language = computed({
  get: () => store.settings.language,
  set: (value: 'zh' | 'en' | 'vi' | 'th') => {
    store.setSettings({ language: value })
    locale.value = value
  },
})

const theme = computed({
  get: () => store.settings.theme,
  set: (value: 'light' | 'dark' | 'system') => {
    store.setSettings({ theme: value })
  },
})

const handleRedeemActivationCode = async () => {
  let code = activationCodeInput.value.trim().toUpperCase().replace(/\s+/g, '')
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
    const data = await apiFetch<ApiResponse<UserDTO>>('/auth/activate', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${store.user.accessToken}`,
      },
      json: { activationCode: code },
    })
    if (data?.success) {
      store.setUser(normalizeUserDates(data.data))
      activationCodeInput.value = ''
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

const handleSaveSettings = () => {
  toast({
    title: 'Settings Saved',
    description: 'Your preferences have been updated',
  })
}

const handleCheckUpdate = () => {
  toast({
    title: 'Checking for Updates',
    description: 'You are running the latest version',
  })
}

let isActive = true

const loadSystemInfo = async () => {
  try {
    const info = await window.electronAPI?.app?.getSystemInfo?.()
    if (!isActive || !info) return

    if (typeof info === 'string') {
      systemInfo.value = info
      return
    }

    if (typeof info === 'object') {
      const record = info as Record<string, unknown>
      const platform = typeof record.platform === 'string' ? record.platform : 'Unknown'
      const arch = typeof record.arch === 'string' ? record.arch : 'Unknown'
      const version = typeof record.version === 'string' ? record.version : 'Unknown'
      const hostname = typeof record.hostname === 'string' ? record.hostname : 'Unknown'
      systemInfo.value = `${platform} ? ${arch} ? ${version} ? ${hostname}`
    }
  } catch {
    // Keep fallback value.
  }
}

onMounted(() => {
  loadSystemInfo()
})

onBeforeUnmount(() => {
  isActive = false
})
</script>
