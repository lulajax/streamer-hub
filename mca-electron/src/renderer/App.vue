<template>
  <AuthLayout v-if="!isAuthenticated" @auth-success="handleAuthSuccess" />
  <ActivationDialog v-else-if="!hasValidSubscription" />
  <MainLayout v-else />
  <Toaster />
</template>

<script setup lang="ts">
import { computed, onMounted, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import AuthLayout from '@/components/AuthLayout.vue'
import MainLayout from '@/components/MainLayout.vue'
import ActivationDialog from '@/components/ActivationDialog.vue'
import Toaster from '@/components/ui/Toaster.vue'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
import { getDeviceInfo } from '@/lib/device'
import type { ApiResponse, UserDTO } from '@/types'

const store = useStore()
const { toast } = useToast()
const { locale } = useI18n()

const isAuthenticated = computed(() => store.isAuthenticated)

const hasValidSubscription = computed(() => {
  const user = store.user
  if (!user) return false
  const subscriptionType = user.subscriptionType ?? 'FREE'
  const expiresAt = user.subscriptionExpiresAt
  if (subscriptionType === 'FREE') return false
  if (!expiresAt) return false
  return Date.now() < expiresAt
})

watch(
  () => store.settings.language,
  (language) => {
    if (language) {
      locale.value = language
    }
  },
  { immediate: true }
)

const handleAuthSuccess = () => {
  // Placeholder for future post-login handling.
}

// Initialize device info and sync with store
const initializeDeviceInfo = async () => {
  try {
    const deviceInfo = await getDeviceInfo()
    // Update store activation with device info
    const currentActivation = store.activation
    store.setActivation({
      ...currentActivation,
      deviceName: deviceInfo.deviceName,
    })
  } catch (err) {
    console.error('Failed to initialize device info:', err)
  }
}

onMounted(() => {
  // Initialize device info
  initializeDeviceInfo()

  const { pathname, search } = window.location
  if (pathname !== '/verify-email' && pathname !== '/verify-email/') return

  const params = new URLSearchParams(search)
  const token = params.get('token')
  if (!token) {
    toast({
      title: 'Error',
      description: 'Missing verification token',
      variant: 'destructive',
    })
    window.history.replaceState({}, '', '/')
    return
  }

  apiFetch<ApiResponse<UserDTO>>(`/auth/verify-email?token=${encodeURIComponent(token)}`)
    .then((data) => {
      if (data?.success) {
        store.setUser(normalizeUserDates(data.data))
        toast({
          title: 'Success',
          description: 'Email verified successfully!',
        })
      } else {
        toast({
          title: 'Error',
          description: data?.error || 'Email verification failed',
          variant: 'destructive',
        })
      }
    })
    .catch(() => {
      toast({
        title: 'Error',
        description: 'Network error. Please try again.',
        variant: 'destructive',
      })
    })
    .finally(() => {
      window.history.replaceState({}, '', '/')
    })
})
</script>
