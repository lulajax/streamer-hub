<template>
  <div class="min-h-screen bg-slate-950 flex items-center justify-center p-6">
    <div class="w-full max-w-4xl grid grid-cols-2 gap-8">
      <Card class="bg-slate-900 border-slate-800">
        <CardHeader>
          <CardTitle class="text-white">{{ t('appName') }}</CardTitle>
          <CardDescription>{{ t('activation') }}</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs v-model="activeTab">
            <TabsList class="mb-6">
              <TabsTrigger value="login">Login</TabsTrigger>
              <TabsTrigger value="register">Register</TabsTrigger>
            </TabsList>

            <TabsContent value="login">
              <div class="space-y-4">
                <div class="space-y-2">
                  <label class="text-sm text-slate-300">Email</label>
                  <div class="relative">
                    <Mail class="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                    <Input v-model="loginEmail" type="email" class="pl-9" placeholder="you@example.com" />
                  </div>
                </div>
                <div class="space-y-2">
                  <label class="text-sm text-slate-300">Password</label>
                  <div class="relative">
                    <Lock class="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                    <Input
                      v-model="loginPassword"
                      :type="showPassword ? 'text' : 'password'"
                      class="pl-9 pr-10"
                      placeholder="••••••••"
                    />
                    <button
                      class="absolute right-3 top-3 text-slate-400"
                      type="button"
                      @click="showPassword = !showPassword"
                    >
                      <component :is="showPassword ? EyeOff : Eye" class="h-4 w-4" />
                    </button>
                  </div>
                </div>
                <Button class="w-full" :disabled="isLoading" @click="handleLogin">
                  <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
                  Login
                </Button>
              </div>
            </TabsContent>

            <TabsContent value="register">
              <div class="space-y-4">
                <div class="space-y-2">
                  <label class="text-sm text-slate-300">Nickname</label>
                  <div class="relative">
                    <User class="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                    <Input v-model="registerNickname" class="pl-9" placeholder="Streamer" />
                  </div>
                </div>
                <div class="space-y-2">
                  <label class="text-sm text-slate-300">Email</label>
                  <div class="relative">
                    <Mail class="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                    <Input v-model="registerEmail" type="email" class="pl-9" placeholder="you@example.com" />
                  </div>
                </div>
                <div class="space-y-2">
                  <label class="text-sm text-slate-300">Password</label>
                  <div class="relative">
                    <Lock class="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                    <Input v-model="registerPassword" type="password" class="pl-9" placeholder="••••••••" />
                  </div>
                </div>
                <div class="space-y-2">
                  <label class="text-sm text-slate-300">Confirm Password</label>
                  <div class="relative">
                    <Lock class="absolute left-3 top-3 h-4 w-4 text-slate-400" />
                    <Input
                      v-model="registerConfirmPassword"
                      type="password"
                      class="pl-9"
                      placeholder="••••••••"
                    />
                  </div>
                </div>
                <Button class="w-full" :disabled="isLoading" @click="handleRegister">
                  <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
                  Register
                </Button>
              </div>
            </TabsContent>
          </Tabs>
        </CardContent>
      </Card>

      <Card class="bg-slate-900 border-slate-800">
        <CardHeader>
          <CardTitle class="text-white">Activation</CardTitle>
          <CardDescription>Activate your premium access.</CardDescription>
        </CardHeader>
        <CardContent>
          <div class="space-y-4">
            <div class="rounded-lg border border-slate-800 bg-slate-950 p-4">
              <div class="flex items-center gap-3">
                <Sparkles class="h-6 w-6 text-amber-400" />
                <div>
                  <p class="text-white font-semibold">Premium Features</p>
                  <p class="text-sm text-slate-400">Unlock advanced modes and widgets.</p>
                </div>
              </div>
            </div>

            <div v-if="showActivation" class="space-y-3">
              <label class="text-sm text-slate-300">Activation Code</label>
              <Input v-model="activationCode" placeholder="XXXX-XXXX-XXXX-XXXX" />
              <Button class="w-full" :disabled="isLoading" @click="handleActivate">
                <Loader2 v-if="isLoading" class="mr-2 h-4 w-4 animate-spin" />
                Activate
              </Button>
            </div>
            <Button v-else class="w-full" variant="secondary" @click="showActivation = true">
              Activate Now
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/hooks/use-toast'
import { useStore } from '@/stores/useStore'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
import type { ApiResponse, UserDTO } from '@/types'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Mail, Lock, User, Eye, EyeOff, Sparkles, Loader2 } from 'lucide-vue-next'

const emit = defineEmits<{ (event: 'auth-success'): void }>()

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const activeTab = ref('login')
const isLoading = ref(false)
const showPassword = ref(false)

const loginEmail = ref('')
const loginPassword = ref('')

const registerEmail = ref('')
const registerPassword = ref('')
const registerConfirmPassword = ref('')
const registerNickname = ref('')

const activationCode = ref('')
const showActivation = ref(false)

const handleLogin = async () => {
  if (!loginEmail.value || !loginPassword.value) {
    toast({
      title: 'Error',
      description: 'Please enter email and password',
      variant: 'destructive',
    })
    return
  }

  isLoading.value = true

  try {
    const data = await apiFetch<ApiResponse<UserDTO>>('/auth/login', {
      method: 'POST',
      json: {
        email: loginEmail.value,
        password: loginPassword.value,
      },
    })

    if (data.success) {
      store.setUser(normalizeUserDates(data.data))

      if (!data.data.isActivated) {
        showActivation.value = true
        isLoading.value = false
        return
      }

      toast({
        title: 'Success',
        description: 'Login successful!',
      })
      emit('auth-success')
    } else {
      toast({
        title: 'Error',
        description: data.error || 'Login failed',
        variant: 'destructive',
      })
    }
  } catch {
    toast({
      title: 'Error',
      description: 'Network error. Please try again.',
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
}

const handleRegister = async () => {
  if (!registerEmail.value || !registerPassword.value || !registerConfirmPassword.value) {
    toast({
      title: 'Error',
      description: 'Please fill in all required fields',
      variant: 'destructive',
    })
    return
  }

  if (registerPassword.value !== registerConfirmPassword.value) {
    toast({
      title: 'Error',
      description: 'Passwords do not match',
      variant: 'destructive',
    })
    return
  }

  if (registerPassword.value.length < 8) {
    toast({
      title: 'Error',
      description: 'Password must be at least 8 characters',
      variant: 'destructive',
    })
    return
  }

  isLoading.value = true

  try {
    const data = await apiFetch<ApiResponse<UserDTO>>('/auth/register', {
      method: 'POST',
      json: {
        email: registerEmail.value,
        password: registerPassword.value,
        confirmPassword: registerConfirmPassword.value,
        nickname: registerNickname.value || registerEmail.value.split('@')[0],
      },
    })

    if (data.success) {
      toast({
        title: 'Success',
        description: 'Registration successful! Please check your email to verify your account.',
      })
      activeTab.value = 'login'
      loginEmail.value = registerEmail.value
    } else {
      toast({
        title: 'Error',
        description: data.error || 'Registration failed',
        variant: 'destructive',
      })
    }
  } catch {
    toast({
      title: 'Error',
      description: 'Network error. Please try again.',
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
}

const handleActivate = async () => {
  if (!activationCode.value) {
    toast({
      title: 'Error',
      description: 'Please enter activation code',
      variant: 'destructive',
    })
    return
  }

  isLoading.value = true

  try {
    const data = await apiFetch<ApiResponse<UserDTO>>('/auth/activate', {
      method: 'POST',
      headers: {
        Authorization: `Bearer ${store.user?.accessToken}`,
      },
      json: {
        activationCode: activationCode.value.toUpperCase(),
      },
    })

    if (data.success) {
      store.setUser(normalizeUserDates(data.data))
      toast({
        title: 'Success',
        description: 'Premium activated successfully!',
      })
      showActivation.value = false
      emit('auth-success')
    } else {
      toast({
        title: 'Error',
        description: data.error || 'Activation failed',
        variant: 'destructive',
      })
    }
  } catch {
    toast({
      title: 'Error',
      description: 'Network error. Please try again.',
      variant: 'destructive',
    })
  } finally {
    isLoading.value = false
  }
}
</script>
