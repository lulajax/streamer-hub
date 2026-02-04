<template>
  <div class="h-screen flex items-center justify-center bg-slate-950">
    <div class="w-full max-w-md p-8 space-y-6 bg-slate-900 rounded-xl border border-slate-800">
      <!-- Login Form -->
      <template v-if="!showRegister">
        <div class="text-center space-y-2">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center mx-auto">
            <span class="text-white font-bold text-2xl">M</span>
          </div>
          <h1 class="text-2xl font-bold text-white">{{ t('appName') }}</h1>
          <p class="text-slate-400">{{ t('loginSubtitle') }}</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleSubmit">
          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-300">{{ t('email') }}</label>
            <Input
              v-model="email"
              type="email"
              :placeholder="t('enterEmail')"
              class="bg-slate-950 border-slate-700 text-white"
              required
            />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-300">{{ t('password') }}</label>
            <Input
              v-model="password"
              type="password"
              :placeholder="t('enterPassword')"
              class="bg-slate-950 border-slate-700 text-white"
              required
            />
          </div>

          <Button
            type="submit"
            class="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
            :disabled="isLoading"
          >
            <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
            {{ isLoading ? t('loading') : t('login') }}
          </Button>
        </form>

        <div class="text-center">
          <p class="text-slate-500 text-sm">
            {{ t('noAccount') }}
            <a href="#" class="text-blue-400 hover:text-blue-300" @click.prevent="showRegister = true">
              {{ t('register') }}
            </a>
          </p>
        </div>
      </template>

      <!-- Register Form -->
      <template v-else>
        <div class="text-center space-y-2">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-xl flex items-center justify-center mx-auto">
            <span class="text-white font-bold text-2xl">M</span>
          </div>
          <h1 class="text-2xl font-bold text-white">{{ t('appName') }}</h1>
          <p class="text-slate-400">{{ t('registerSubtitle') }}</p>
        </div>

        <form class="space-y-4" @submit.prevent="handleRegister">
          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-300">{{ t('email') }}</label>
            <Input
              v-model="email"
              type="email"
              :placeholder="t('enterEmail')"
              class="bg-slate-950 border-slate-700 text-white"
              required
            />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-300">{{ t('password') }}</label>
            <Input
              v-model="password"
              type="password"
              :placeholder="t('enterPassword')"
              class="bg-slate-950 border-slate-700 text-white"
              required
            />
          </div>

          <div class="space-y-2">
            <label class="text-sm font-medium text-slate-300">{{ t('confirmPassword') }}</label>
            <Input
              v-model="confirmPassword"
              type="password"
              :placeholder="t('enterConfirmPassword')"
              class="bg-slate-950 border-slate-700 text-white"
              required
            />
          </div>

          <Button
            type="submit"
            class="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
            :disabled="isLoading"
          >
            <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
            {{ isLoading ? t('loading') : t('register') }}
          </Button>
        </form>

        <div class="text-center">
          <p class="text-slate-500 text-sm">
            {{ t('hasAccount') }}
            <a href="#" class="text-blue-400 hover:text-blue-300" @click.prevent="showRegister = false">
              {{ t('login') }}
            </a>
          </p>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Loader2 } from 'lucide-vue-next'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
import type { ApiResponse, UserDTO } from '@/types'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const emit = defineEmits<{
  (e: 'auth-success'): void
}>()

const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)
const showRegister = ref(false)

const handleSubmit = async () => {
  if (!email.value || !password.value) {
    toast({
      title: t('error'),
      description: t('enterEmailAndPassword'),
      variant: 'destructive',
    })
    return
  }

  isLoading.value = true
  try {
    const response = await apiFetch<ApiResponse<UserDTO>>('/auth/login', {
      method: 'POST',
      json: {
        email: email.value,
        password: password.value,
      },
    })

    if (response.success && response.data) {
      store.setUser(normalizeUserDates(response.data))
      toast({
        title: t('success'),
        description: t('loginSuccess'),
      })
      emit('auth-success')
    } else {
      toast({
        title: t('error'),
        description: response.error || t('loginFailed'),
        variant: 'destructive',
      })
    }
  } catch (error) {
    toast({
      title: t('error'),
      description: t('networkError'),
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
}

const handleRegister = async () => {
  if (!email.value || !password.value || !confirmPassword.value) {
    toast({
      title: t('error'),
      description: t('enterAllFields'),
      variant: 'destructive',
    })
    return
  }

  if (password.value !== confirmPassword.value) {
    toast({
      title: t('error'),
      description: t('passwordNotMatch'),
      variant: 'destructive',
    })
    return
  }

  isLoading.value = true
  try {
    const response = await apiFetch<ApiResponse<UserDTO>>('/auth/register', {
      method: 'POST',
      json: {
        email: email.value,
        password: password.value,
      },
    })

    if (response.success && response.data) {
      store.setUser(normalizeUserDates(response.data))
      toast({
        title: t('success'),
        description: t('registerSuccess'),
      })
      emit('auth-success')
    } else {
      toast({
        title: t('error'),
        description: response.error || t('registerFailed'),
        variant: 'destructive',
      })
    }
  } catch (error) {
    toast({
      title: t('error'),
      description: t('networkError'),
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
}
</script>
