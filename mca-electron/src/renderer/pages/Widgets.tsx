import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useToast } from '@/hooks/use-toast'
import {
  LayoutTemplate,
  Copy,
  ExternalLink,
  Monitor,
  Smartphone,
  Snowflake,
  UserCircle,
  BarChart3,
  Check,
  Eye,
} from 'lucide-react'
import type { WidgetSettings } from '@/types'

export function Widgets() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { widgets, currentPreset } = useStore()
  const [copiedId, setCopiedId] = useState<string | null>(null)
  const [settings, setSettings] = useState<WidgetSettings>({
    showAnchorName: true,
    showGiftCount: true,
    showTotalScore: true,
    showProgressBar: true,
    showCountdown: true,
    theme: 'default',
    layout: 'horizontal',
  })

  const handleCopyLink = (url: string, id: string) => {
    navigator.clipboard.writeText(url)
    setCopiedId(id)
    toast({
      title: 'Link Copied',
      description: 'Widget URL copied to clipboard',
    })
    setTimeout(() => setCopiedId(null), 2000)
  }

  const handleOpenWidget = (url: string, title: string) => {
    window.electronAPI.widget.open(url, title)
    toast({
      title: 'Widget Opened',
      description: `${title} window opened`,
    })
  }

  const generateWidgetUrl = (type: string, params: Record<string, string>) => {
    const baseUrl = 'http://localhost:8080/widget'
    const queryParams = new URLSearchParams({ type, ...params })
    return `${baseUrl}?${queryParams.toString()}`
  }

  const widgetConfigs = [
    {
      id: 'main-sticker',
      type: 'main',
      name: 'Sticker Mode Main Widget',
      description: 'Main display for sticker dance mode',
      url: generateWidgetUrl('sticker-main', { preset: currentPreset?.id || 'default' }),
      recommendedSize: '800x600',
      icon: LayoutTemplate,
    },
    {
      id: 'main-pk',
      type: 'main',
      name: 'PK Mode Main Widget',
      description: 'PK progress bar and team scores',
      url: generateWidgetUrl('pk-main', { preset: currentPreset?.id || 'default' }),
      recommendedSize: '1000x200',
      icon: BarChart3,
    },
    {
      id: 'main-free',
      type: 'main',
      name: 'Free Mode Main Widget',
      description: 'Current anchor and score display',
      url: generateWidgetUrl('free-main', { preset: currentPreset?.id || 'default' }),
      recommendedSize: '600x400',
      icon: UserCircle,
    },
    {
      id: 'leaderboard-h',
      type: 'leaderboard',
      name: 'Leaderboard (Horizontal)',
      description: 'Horizontal anchor ranking display',
      url: generateWidgetUrl('leaderboard', { layout: 'horizontal', preset: currentPreset?.id || 'default' }),
      recommendedSize: '1200x150',
      icon: Monitor,
    },
    {
      id: 'leaderboard-v',
      type: 'leaderboard',
      name: 'Leaderboard (Vertical)',
      description: 'Vertical anchor ranking display',
      url: generateWidgetUrl('leaderboard', { layout: 'vertical', preset: currentPreset?.id || 'default' }),
      recommendedSize: '300x800',
      icon: Smartphone,
    },
    {
      id: 'freeze-effect',
      type: 'freeze-effect',
      name: 'Freeze Effect Widget',
      description: 'Visual freeze effect for PK mode',
      url: generateWidgetUrl('freeze', { preset: currentPreset?.id || 'default' }),
      recommendedSize: '500x1109',
      icon: Snowflake,
    },
    {
      id: 'avatar-frame',
      type: 'avatar-frame',
      name: 'Avatar Frame Widget',
      description: 'Dynamic avatar frame with effects',
      url: generateWidgetUrl('avatar-frame', { preset: currentPreset?.id || 'default' }),
      recommendedSize: '400x400',
      icon: UserCircle,
    },
  ]

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-white flex items-center gap-2">
          <LayoutTemplate className="w-6 h-6 text-purple-400" />
          {t('widgets')}
        </h1>
        <p className="text-slate-400">Configure and manage OBS widgets</p>
      </div>

      <Tabs defaultValue="links" className="w-full">
        <TabsList className="bg-slate-900 border border-slate-800">
          <TabsTrigger value="links">Widget Links</TabsTrigger>
          <TabsTrigger value="settings">Widget Settings</TabsTrigger>
          <TabsTrigger value="guide">OBS Setup Guide</TabsTrigger>
        </TabsList>

        <TabsContent value="links" className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {widgetConfigs.map((widget) => {
              const Icon = widget.icon
              return (
                <Card key={widget.id} className="bg-slate-900 border-slate-800">
                  <CardContent className="p-4">
                    <div className="flex items-start gap-4">
                      <div className="w-12 h-12 bg-purple-500/20 rounded-lg flex items-center justify-center flex-shrink-0">
                        <Icon className="w-6 h-6 text-purple-400" />
                      </div>

                      <div className="flex-1 min-w-0">
                        <h3 className="text-white font-medium">{widget.name}</h3>
                        <p className="text-slate-400 text-sm">{widget.description}</p>

                        <div className="flex items-center gap-2 mt-2">
                          <Badge variant="secondary" className="text-xs">
                            {widget.recommendedSize}
                          </Badge>
                          <Badge variant="outline" className="text-xs text-slate-400">
                            {widget.type}
                          </Badge>
                        </div>

                        <div className="flex items-center gap-2 mt-3">
                          <Input
                            value={widget.url}
                            readOnly
                            className="flex-1 bg-slate-800 border-slate-700 text-slate-400 text-sm"
                          />
                          <Button
                            variant="outline"
                            size="icon"
                            onClick={() => handleCopyLink(widget.url, widget.id)}
                            className="border-slate-700"
                          >
                            {copiedId === widget.id ? (
                              <Check className="w-4 h-4 text-green-400" />
                            ) : (
                              <Copy className="w-4 h-4" />
                            )}
                          </Button>
                          <Button
                            variant="outline"
                            size="icon"
                            onClick={() => handleOpenWidget(widget.url, widget.name)}
                            className="border-slate-700"
                          >
                            <ExternalLink className="w-4 h-4" />
                          </Button>
                        </div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        </TabsContent>

        <TabsContent value="settings" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">Widget Display Settings</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Display Options */}
              <div className="space-y-4">
                <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                  <div className="flex items-center gap-3">
                    <Eye className="w-5 h-5 text-blue-400" />
                    <div>
                      <p className="text-white font-medium">Show Anchor Name</p>
                      <p className="text-slate-400 text-sm">Display anchor names in widget</p>
                    </div>
                  </div>
                  <Switch
                    checked={settings.showAnchorName}
                    onCheckedChange={(checked) =>
                      setSettings({ ...settings, showAnchorName: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                  <div className="flex items-center gap-3">
                    <LayoutTemplate className="w-5 h-5 text-green-400" />
                    <div>
                      <p className="text-white font-medium">Show Gift Count</p>
                      <p className="text-slate-400 text-sm">Display received gift counts</p>
                    </div>
                  </div>
                  <Switch
                    checked={settings.showGiftCount}
                    onCheckedChange={(checked) =>
                      setSettings({ ...settings, showGiftCount: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                  <div className="flex items-center gap-3">
                    <BarChart3 className="w-5 h-5 text-amber-400" />
                    <div>
                      <p className="text-white font-medium">Show Total Score</p>
                      <p className="text-slate-400 text-sm">Display total score in widget</p>
                    </div>
                  </div>
                  <Switch
                    checked={settings.showTotalScore}
                    onCheckedChange={(checked) =>
                      setSettings({ ...settings, showTotalScore: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                  <div className="flex items-center gap-3">
                    <Monitor className="w-5 h-5 text-purple-400" />
                    <div>
                      <p className="text-white font-medium">Show Progress Bar</p>
                      <p className="text-slate-400 text-sm">Display score progress bar</p>
                    </div>
                  </div>
                  <Switch
                    checked={settings.showProgressBar}
                    onCheckedChange={(checked) =>
                      setSettings({ ...settings, showProgressBar: checked })
                    }
                  />
                </div>

                <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                  <div className="flex items-center gap-3">
                    <LayoutTemplate className="w-5 h-5 text-pink-400" />
                    <div>
                      <p className="text-white font-medium">Show Countdown</p>
                      <p className="text-slate-400 text-sm">Display countdown timer</p>
                    </div>
                  </div>
                  <Switch
                    checked={settings.showCountdown}
                    onCheckedChange={(checked) =>
                      setSettings({ ...settings, showCountdown: checked })
                    }
                  />
                </div>
              </div>

              {/* Theme */}
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">Widget Theme</label>
                <Select
                  value={settings.theme}
                  onValueChange={(value: 'default' | 'neon' | 'minimal') =>
                    setSettings({ ...settings, theme: value })
                  }
                >
                  <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="default">Default</SelectItem>
                    <SelectItem value="neon">Neon</SelectItem>
                    <SelectItem value="minimal">Minimal</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Layout */}
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">Default Layout</label>
                <div className="flex gap-4">
                  <button
                    onClick={() => setSettings({ ...settings, layout: 'horizontal' })}
                    className={`flex-1 p-4 rounded-lg border transition-colors ${
                      settings.layout === 'horizontal'
                        ? 'border-blue-500 bg-blue-500/10'
                        : 'border-slate-700 bg-slate-800'
                    }`}
                  >
                    <Monitor className="w-6 h-6 text-blue-400 mx-auto mb-2" />
                    <p className="text-white text-center">Horizontal</p>
                  </button>
                  <button
                    onClick={() => setSettings({ ...settings, layout: 'vertical' })}
                    className={`flex-1 p-4 rounded-lg border transition-colors ${
                      settings.layout === 'vertical'
                        ? 'border-purple-500 bg-purple-500/10'
                        : 'border-slate-700 bg-slate-800'
                    }`}
                  >
                    <Smartphone className="w-6 h-6 text-purple-400 mx-auto mb-2" />
                    <p className="text-white text-center">Vertical</p>
                  </button>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="guide" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">OBS Studio Setup</CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              <div className="space-y-4">
                <div className="p-4 bg-slate-800 rounded-lg">
                  <h3 className="text-white font-medium mb-2">Step 1: Copy Widget URL</h3>
                  <p className="text-slate-400 text-sm">
                    Go to the Widget Links tab and copy the URL for the widget you want to add.
                  </p>
                </div>

                <div className="p-4 bg-slate-800 rounded-lg">
                  <h3 className="text-white font-medium mb-2">Step 2: Add Browser Source</h3>
                  <p className="text-slate-400 text-sm">
                    In OBS Studio, click the + button in Sources and select "Browser".
                  </p>
                </div>

                <div className="p-4 bg-slate-800 rounded-lg">
                  <h3 className="text-white font-medium mb-2">Step 3: Configure Source</h3>
                  <ul className="text-slate-400 text-sm space-y-1 list-disc list-inside">
                    <li>Name your source (e.g., "MCA Sticker Widget")</li>
                    <li>Paste the widget URL in the URL field</li>
                    <li>Set width and height to the recommended size</li>
                    <li>Check "Shutdown source when not visible"</li>
                    <li>Click OK</li>
                  </ul>
                </div>

                <div className="p-4 bg-slate-800 rounded-lg">
                  <h3 className="text-white font-medium mb-2">Step 4: Position Widget</h3>
                  <p className="text-slate-400 text-sm">
                    Drag and resize the widget in your OBS preview to position it where you want.
                    Right-click the source and select Transform {'>'} Fit to Screen for best results.
                  </p>
                </div>
              </div>

              <div className="p-4 bg-amber-500/10 border border-amber-500/30 rounded-lg">
                <p className="text-amber-200 text-sm">
                  <strong>Tip:</strong> For transparent backgrounds, make sure your widget has a transparent
                  background enabled. The widget will blend seamlessly with your stream overlay.
                </p>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">TikTok LIVE Studio Setup</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="p-4 bg-slate-800 rounded-lg">
                <h3 className="text-white font-medium mb-2">Using Widget in LIVE Studio</h3>
                <ul className="text-slate-400 text-sm space-y-1 list-disc list-inside">
                  <li>Open TikTok LIVE Studio</li>
                  <li>Click "Add Source" and select "Web"</li>
                  <li>Enter the widget URL</li>
                  <li>Set custom resolution as recommended</li>
                  <li>Adjust position and size as needed</li>
                </ul>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
