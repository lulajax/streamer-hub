<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
    <div class="w-full max-w-4xl">
      <!-- Header -->
      <div class="text-center mb-8">
        <div class="flex items-center justify-center gap-3 mb-4">
          <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
            <Sparkles class="w-8 h-8 text-white" />
          </div>
        </div>
        <h1 class="text-4xl font-bold text-white mb-2">MCA</h1>
        <p class="text-slate-400 text-lg">Multi-Caster Assistant for TikTok Live</p>
      </div>

      <div class="grid md:grid-cols-2 gap-6">
        <!-- Activation Card -->
        <Card class="bg-slate-800/50 border-slate-700 backdrop-blur">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Key class="w-5 h-5 text-blue-400" />
              {{ t('activation') }}
            </CardTitle>
            <CardDescription class="text-slate-400">
              Enter your 16-digit activation code to unlock MCA
            </CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <div>
              <label class="text-sm font-medium text-slate-300 mb-2 block">
                {{ t('activationCode') }}
              </label>
              <Input
                :modelValue="activationCode"
                placeholder="XXXX-XXXX-XXXX-XXXX"
                maxlength="19"
                class="bg-slate-900 border-slate-600 text-white text-center tracking-widest font-mono text-lg"
                @update:modelValue="activationCode = formatActivationCode($event)"
              />
            </div>
            <Button
              :disabled="isActivating || activationCode.length !== 19"
              class="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              @click="handleActivate"
            >
              {{ isActivating ? t('loading') : t('activateNow') }}
            </Button>
          </CardContent>
        </Card>

        <!-- Features -->
        <div class="space-y-4">
          <Card class="bg-slate-800/50 border-slate-700 backdrop-blur">
            <CardContent class="p-4">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Monitor class="w-5 h-5 text-blue-400" />
                </div>
                <div>
                  <h3 class="text-white font-medium mb-1">Real-time Monitoring</h3>
                  <p class="text-slate-400 text-sm">Monitor TikTok live streams and capture gift data in real-time</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card class="bg-slate-800/50 border-slate-700 backdrop-blur">
            <CardContent class="p-4">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 bg-purple-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Globe class="w-5 h-5 text-purple-400" />
                </div>
                <div>
                  <h3 class="text-white font-medium mb-1">Multiple Game Modes</h3>
                  <p class="text-slate-400 text-sm">Support Sticker Dance, PK Battle, and Free Mode</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card class="bg-slate-800/50 border-slate-700 backdrop-blur">
            <CardContent class="p-4">
              <div class="flex items-start gap-3">
                <div class="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Sparkles class="w-5 h-5 text-green-400" />
                </div>
                <div>
                  <h3 class="text-white font-medium mb-1">OBS Integration</h3>
                  <p class="text-slate-400 text-sm">Seamless widget integration with OBS and LiveStudio</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <!-- Footer -->
      <div class="text-center mt-8 text-slate-500 text-sm">
        <p>Contact customer service to get your activation code</p>
        <p class="mt-1">Support: Windows 7+ | macOS 10.14+</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import { useToast } from '@/hooks/use-toast'
import { Key, Monitor, Globe, Sparkles } from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const activationCode = ref('')
const isActivating = ref(false)

const formatActivationCode = (value: string) => {
  const cleaned = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()
  const formatted = cleaned.match(/.{1,4}/g)?.join('-') || cleaned
  return formatted.slice(0, 19)
}

const handleActivate = async () => {
  if (activationCode.value.length !== 16) {
    toast({
      title: t('error'),
      description: 'Please enter a valid 16-digit activation code',
      variant: 'destructive',
    })
    return
  }

  isActivating.value = true

  setTimeout(() => {
    const mockActivation = {
      isActivated: true,
      deviceName: `Device-${Math.random().toString(36).substr(2, 6).toUpperCase()}`,
      activatedAt: Date.now(),
      expiresAt: Date.now() + 365 * 24 * 60 * 60 * 1000,
      activationCode: activationCode.value,
    }

    store.setActivation(mockActivation)
    isActivating.value = false

    toast({
      title: t('activationSuccess'),
      description: `${t('deviceName')}: ${mockActivation.deviceName}`,
    })
  }, 1500)
}
</script>
