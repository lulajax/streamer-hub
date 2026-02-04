import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from '@/components/ui/dialog'
import { useToast } from '@/hooks/use-toast'
import { Key, Monitor, Globe, Sparkles } from 'lucide-react'

export function ActivationDialog() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { setActivation } = useStore()
  const [activationCode, setActivationCode] = useState('')
  const [isActivating, setIsActivating] = useState(false)

  const handleActivate = async () => {
    if (activationCode.length !== 16) {
      toast({
        title: t('error'),
        description: 'Please enter a valid 16-digit activation code',
        variant: 'destructive',
      })
      return
    }

    setIsActivating(true)

    // Simulate activation API call
    setTimeout(() => {
      // In production, this would call the backend API
      const mockActivation = {
        isActivated: true,
        deviceName: `Device-${Math.random().toString(36).substr(2, 6).toUpperCase()}`,
        activatedAt: Date.now(),
        expiresAt: Date.now() + 365 * 24 * 60 * 60 * 1000, // 1 year
        activationCode,
      }

      setActivation(mockActivation)
      setIsActivating(false)

      toast({
        title: t('activationSuccess'),
        description: `${t('deviceName')}: ${mockActivation.deviceName}`,
      })
    }, 1500)
  }

  const formatActivationCode = (value: string) => {
    // Remove non-alphanumeric characters
    const cleaned = value.replace(/[^a-zA-Z0-9]/g, '').toUpperCase()
    // Add dashes every 4 characters
    const formatted = cleaned.match(/.{1,4}/g)?.join('-') || cleaned
    return formatted.slice(0, 19) // Max 16 chars + 3 dashes
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-4xl">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-4">
            <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <Sparkles className="w-8 h-8 text-white" />
            </div>
          </div>
          <h1 className="text-4xl font-bold text-white mb-2">MCA</h1>
          <p className="text-slate-400 text-lg">Multi-Caster Assistant for TikTok Live</p>
        </div>

        <div className="grid md:grid-cols-2 gap-6">
          <Card className="bg-slate-800/50 border-slate-700 backdrop-blur">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Key className="w-5 h-5 text-blue-400" />
                {t('activation')}
              </CardTitle>
              <CardDescription className="text-slate-400">
                Enter your 16-digit activation code to unlock MCA
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">
                  {t('activationCode')}
                </label>
                <Input
                  placeholder="XXXX-XXXX-XXXX-XXXX"
                  value={activationCode}
                  onChange={(e) => setActivationCode(formatActivationCode(e.target.value))}
                  className="bg-slate-900 border-slate-600 text-white text-center tracking-widest font-mono text-lg"
                  maxLength={19}
                />
              </div>
              <Button
                onClick={handleActivate}
                disabled={isActivating || activationCode.length !== 19}
                className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              >
                {isActivating ? t('loading') : t('activateNow')}
              </Button>
            </CardContent>
          </Card>

          <div className="space-y-4">
            <Card className="bg-slate-800/50 border-slate-700 backdrop-blur">
              <CardContent className="p-4">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Monitor className="w-5 h-5 text-blue-400" />
                  </div>
                  <div>
                    <h3 className="text-white font-medium mb-1">Real-time Monitoring</h3>
                    <p className="text-slate-400 text-sm">Monitor TikTok live streams and capture gift data in real-time</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-slate-800/50 border-slate-700 backdrop-blur">
              <CardContent className="p-4">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-purple-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Globe className="w-5 h-5 text-purple-400" />
                  </div>
                  <div>
                    <h3 className="text-white font-medium mb-1">Multiple Game Modes</h3>
                    <p className="text-slate-400 text-sm">Support Sticker Dance, PK Battle, and Free Mode</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-slate-800/50 border-slate-700 backdrop-blur">
              <CardContent className="p-4">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                    <Sparkles className="w-5 h-5 text-green-400" />
                  </div>
                  <div>
                    <h3 className="text-white font-medium mb-1">OBS Integration</h3>
                    <p className="text-slate-400 text-sm">Seamless widget integration with OBS and LiveStudio</p>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>

        <div className="text-center mt-8 text-slate-500 text-sm">
          <p>Contact customer service to get your activation code</p>
          <p className="mt-1">Support: Windows 7+ | macOS 10.14+</p>
        </div>
      </div>
    </div>
  )
}
