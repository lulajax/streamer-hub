import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Badge } from '@/components/ui/badge'
import { useToast } from '@/hooks/use-toast'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
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
} from 'lucide-react'
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

export function Settings() {
  const { t, i18n } = useTranslation()
  const { toast } = useToast()
  const { settings, setSettings, activation, user, setUser } = useStore()
  const [systemInfo, setSystemInfo] = useState('Electron')
  const [activationCodeInput, setActivationCodeInput] = useState('')
  const [isRedeeming, setIsRedeeming] = useState(false)

  const subscriptionType = user?.subscriptionType ?? 'FREE'
  const subscriptionExpiresAt = user?.subscriptionExpiresAt
  const activatedAt = user?.activatedAt
  const activationCode = user?.activationCode ?? activation.activationCode

  useEffect(() => {
    let isActive = true
    const loadSystemInfo = async () => {
      try {
        const info = await window.electronAPI?.app?.getSystemInfo?.()
        if (!isActive || !info) return

        if (typeof info === 'string') {
          setSystemInfo(info)
          return
        }

        if (typeof info === 'object') {
          const record = info as Record<string, unknown>
          const platform = typeof record.platform === 'string' ? record.platform : 'Unknown'
          const arch = typeof record.arch === 'string' ? record.arch : 'Unknown'
          const version = typeof record.version === 'string' ? record.version : 'Unknown'
          const hostname = typeof record.hostname === 'string' ? record.hostname : 'Unknown'
          setSystemInfo(`${platform} • ${arch} • ${version} • ${hostname}`)
        }
      } catch {
        // Keep fallback value
      }
    }

    loadSystemInfo()
    return () => {
      isActive = false
    }
  }, [])

  const handleRedeemActivationCode = async () => {
    let code = activationCodeInput.trim().toUpperCase().replace(/\s+/g, '')
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
    if (!user?.accessToken) {
      toast({
        title: t('error'),
        description: 'Please login again',
        variant: 'destructive',
      })
      return
    }

    setIsRedeeming(true)
    try {
      const data = await apiFetch<ApiResponse<UserDTO>>('/auth/activate', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${user.accessToken}`,
        },
        json: { activationCode: code },
      })
      if (data?.success) {
        setUser(normalizeUserDates(data.data))
        setActivationCodeInput('')
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
      setIsRedeeming(false)
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

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <Settings2 className="w-6 h-6 text-slate-400" />
          {t('settings')}
        </h1>
        <p className="text-slate-400">Configure application preferences</p>
      </div>

      <div className="grid grid-cols-2 gap-6">
        {/* General Settings */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white flex items-center gap-2">
              <Globe className="w-5 h-5 text-blue-400" />
              {t('generalSettings')}
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            {/* Language */}
            <div>
              <label className="text-sm font-medium text-slate-300 mb-2 block">{t('language')}</label>
              <Select
                value={settings.language}
                onValueChange={(value: 'zh' | 'en' | 'vi' | 'th') => {
                  setSettings({ language: value })
                  i18n.changeLanguage(value)
                }}
              >
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="zh">中文 (Chinese)</SelectItem>
                  <SelectItem value="en">English</SelectItem>
                  <SelectItem value="vi">Tiếng Việt (Vietnamese)</SelectItem>
                  <SelectItem value="th">ไทย (Thai)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Theme */}
            <div>
              <label className="text-sm font-medium text-slate-300 mb-2 block">{t('theme')}</label>
              <Select
                value={settings.theme}
                onValueChange={(value: 'light' | 'dark' | 'system') =>
                  setSettings({ theme: value })
                }
              >
                <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="light">{t('light')}</SelectItem>
                  <SelectItem value="dark">{t('dark')}</SelectItem>
                  <SelectItem value="system">{t('system')}</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Notifications */}
            <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div className="flex items-center gap-3">
                <Bell className="w-5 h-5 text-amber-400" />
                <div>
                  <p className="text-white font-medium">{t('notifications')}</p>
                  <p className="text-slate-400 text-sm">Show desktop notifications</p>
                </div>
              </div>
              <Switch
                checked={settings.notifications}
                onCheckedChange={(checked) => setSettings({ notifications: checked })}
              />
            </div>

            {/* Auto Save */}
            <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
              <div className="flex items-center gap-3">
                <Save className="w-5 h-5 text-green-400" />
                <div>
                  <p className="text-white font-medium">{t('autoSave')}</p>
                  <p className="text-slate-400 text-sm">Automatically save session data</p>
                </div>
              </div>
              <Switch
                checked={settings.autoSave}
                onCheckedChange={(checked) => setSettings({ autoSave: checked })}
              />
            </div>

            {/* Save Interval */}
            {settings.autoSave && (
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">
                  {t('saveInterval')} (seconds)
                </label>
                <Input
                  type="number"
                  value={settings.saveInterval}
                  onChange={(e) =>
                    setSettings({ saveInterval: parseInt(e.target.value) || 30 })
                  }
                  className="bg-slate-800 border-slate-700 text-white"
                />
              </div>
            )}
          </CardContent>
        </Card>

        {/* About & System */}
        <div className="space-y-6">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Crown className="w-5 h-5 text-amber-400" />
                Subscription
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div>
                  <p className="text-white font-medium">
                    {subscriptionLabelMap[subscriptionType]}
                  </p>
                  <p className="text-slate-400 text-sm">
                    {subscriptionDescriptions[subscriptionType]}
                  </p>
                </div>
                <Badge
                  variant="secondary"
                  className={
                    subscriptionType === 'FREE'
                      ? 'bg-slate-700 text-slate-200'
                      : 'bg-amber-500/20 text-amber-300'
                  }
                >
                  {subscriptionType}
                </Badge>
              </div>

              <div className="p-4 bg-slate-800 rounded-lg space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-slate-400">Status</span>
                  <span className="text-white">
                    {subscriptionType === 'FREE'
                      ? 'Free'
                      : subscriptionExpiresAt
                        ? 'Active'
                        : 'Inactive'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Activated On</span>
                  <span className="text-white">
                    {activatedAt ? new Date(activatedAt).toLocaleDateString() : 'N/A'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Expires</span>
                  <span className="text-white">
                    {subscriptionExpiresAt
                      ? new Date(subscriptionExpiresAt).toLocaleDateString()
                      : 'N/A'}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3 p-4 bg-slate-800 rounded-lg">
                <div className="w-10 h-10 bg-amber-500/20 rounded-lg flex items-center justify-center">
                  <KeyRound className="w-5 h-5 text-amber-400" />
                </div>
                <div className="flex-1">
                  <p className="text-white font-medium">Activation Code</p>
                  <Input
                    placeholder={activationCode || 'BAS-XXXXXXXXXXXX'}
                    value={activationCodeInput}
                    onChange={(e) => setActivationCodeInput(e.target.value.toUpperCase())}
                    className="mt-2 bg-slate-900 border-slate-600 text-white text-center tracking-widest font-mono text-sm"
                  />
                  <Button
                    onClick={handleRedeemActivationCode}
                    disabled={isRedeeming}
                    className="mt-3 w-full bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700"
                  >
                    {isRedeeming ? t('loading') : 'Redeem Code'}
                  </Button>
                  <p className="text-slate-500 text-xs mt-2 text-center">
                    Format: BAS-XXXXXXXXXXXX, PRO-XXXXXXXXXXXX, or ENT-XXXXXXXXXXXX
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Info className="w-5 h-5 text-purple-400" />
                {t('about')}
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div>
                  <p className="text-white font-medium">{t('version')}</p>
                  <p className="text-slate-400 text-sm">MCA Multi-Caster Assistant</p>
                </div>
                <Badge variant="secondary" className="text-lg">
                  1.0.0
                </Badge>
              </div>

              <div className="p-4 bg-slate-800 rounded-lg space-y-2">
                <div className="flex justify-between">
                  <span className="text-slate-400">Device Name</span>
                  <span className="text-white">{activation.deviceName}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Activated On</span>
                  <span className="text-white">
                    {activation.activatedAt
                      ? new Date(activation.activatedAt).toLocaleDateString()
                      : 'N/A'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Valid Until</span>
                  <span className="text-white">
                    {activation.expiresAt
                      ? new Date(activation.expiresAt).toLocaleDateString()
                      : 'N/A'}
                  </span>
                </div>
              </div>

              <Button
                variant="outline"
                className="w-full border-slate-700 text-slate-300"
                onClick={handleCheckUpdate}
              >
                <RefreshCw className="w-4 h-4 mr-2" />
                Check for Updates
              </Button>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Monitor className="w-5 h-5 text-green-400" />
                System Information
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="p-4 bg-slate-800 rounded-lg space-y-2">
                <div className="flex justify-between">
                  <span className="text-slate-400">Platform</span>
                  <span className="text-white">{systemInfo}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Architecture</span>
                  <span className="text-white">x64</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-400">Node Version</span>
                  <span className="text-white">20.x</span>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Save Button */}
      <div className="flex justify-end">
        <Button
          onClick={handleSaveSettings}
          className="bg-blue-600 hover:bg-blue-700"
        >
          <Check className="w-4 h-4 mr-2" />
          Save Settings
        </Button>
      </div>
    </div>
  )
}
