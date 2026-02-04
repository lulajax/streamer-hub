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
        <!-- Subscription Card -->
        <Card class="bg-slate-800/50 border-slate-700 backdrop-blur">
          <CardHeader>
            <CardTitle class="text-white flex items-center gap-2">
              <Crown class="w-5 h-5 text-amber-400" />
              {{ t('subscription') }}
            </CardTitle>
            <CardDescription class="text-slate-400">
              Subscribe to unlock all features
            </CardDescription>
          </CardHeader>
          <CardContent class="space-y-4">
            <div class="p-4 bg-slate-900 rounded-lg">
              <p class="text-slate-400 text-sm mb-1">Device Name</p>
              <p class="text-white font-medium">{{ deviceName }}</p>
            </div>
            <div class="p-4 bg-slate-900 rounded-lg">
              <p class="text-slate-400 text-sm mb-1">Subscription Status</p>
              <p class="text-amber-400 font-medium">No Active Subscription</p>
            </div>
            <Button
              class="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              @click="handleSubscribe"
            >
              {{ t('subscribeNow') }}
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
        <p>Contact customer service to subscribe</p>
        <p class="mt-1">Support: Windows 7+ | macOS 10.14+</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useStore } from '@/stores/useStore'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardContent from '@/components/ui/CardContent.vue'
import { useToast } from '@/hooks/use-toast'
import { getDeviceInfo } from '@/lib/device'
import { Crown, Monitor, Globe, Sparkles } from 'lucide-vue-next'

const { t } = useI18n()
const { toast } = useToast()
const store = useStore()

const deviceName = ref('Unknown')

onMounted(async () => {
  try {
    const deviceInfo = await getDeviceInfo()
    deviceName.value = deviceInfo.deviceName
  } catch (err) {
    console.error('Failed to get device info:', err)
  }
})

const handleSubscribe = () => {
  toast({
    title: 'Coming Soon',
    description: 'Please contact customer service to subscribe.',
  })
}
</script>
