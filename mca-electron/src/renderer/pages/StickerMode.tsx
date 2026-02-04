import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Switch } from '@/components/ui/switch'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useToast } from '@/hooks/use-toast'
import {
  Sticker,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Plus,
  Trash2,
  Clock,
  Timer,
  FlipHorizontal,
  Sparkles,
} from 'lucide-react'
import type { StickerModeConfig, Anchor, GameplayGift } from '@/types'

export function StickerMode() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { anchors, currentPreset, setCurrentPreset } = useStore()
  const [config, setConfig] = useState<StickerModeConfig>({
    type: 'normal',
    countdownEnabled: false,
    countdownDuration: 60,
    decayEnabled: false,
    decayDuration: 30,
    autoFlipEnabled: false,
    flipInterval: 10,
    maxPages: 2,
    gameplayGifts: [],
  })
  const [isRunning, setIsRunning] = useState(false)
  const [currentPage, setCurrentPage] = useState(1)

  const handleStart = () => {
    setIsRunning(true)
    toast({
      title: 'Sticker Mode Started',
      description: `Mode: ${config.type === 'normal' ? 'Normal' : 'Scoring'}`,
    })
  }

  const handlePause = () => {
    setIsRunning(false)
    toast({
      title: 'Sticker Mode Paused',
    })
  }

  const handleReset = () => {
    setIsRunning(false)
    setCurrentPage(1)
    toast({
      title: 'Sticker Mode Reset',
    })
  }

  const addGameplayGift = () => {
    const newGift: GameplayGift = {
      giftId: '',
      effect: 'bounce',
    }
    setConfig({
      ...config,
      gameplayGifts: [...config.gameplayGifts, newGift],
    })
  }

  const removeGameplayGift = (index: number) => {
    setConfig({
      ...config,
      gameplayGifts: config.gameplayGifts.filter((_, i) => i !== index),
    })
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Sticker className="w-6 h-6 text-pink-400" />
            {t('stickerMode')}
          </h1>
          <p className="text-slate-400">Configure sticker dance mode settings</p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={handleReset}
            className="border-slate-700 text-slate-300"
          >
            <RotateCcw className="w-4 h-4 mr-2" />
            Reset
          </Button>
          {isRunning ? (
            <Button onClick={handlePause} variant="secondary">
              <Pause className="w-4 h-4 mr-2" />
              Pause
            </Button>
          ) : (
            <Button onClick={handleStart} className="bg-green-600 hover:bg-green-700">
              <Play className="w-4 h-4 mr-2" />
              Start
            </Button>
          )}
        </div>
      </div>

      <Tabs defaultValue="config" className="w-full">
        <TabsList className="bg-slate-900 border border-slate-800">
          <TabsTrigger value="config">Configuration</TabsTrigger>
          <TabsTrigger value="anchors">Anchors</TabsTrigger>
          <TabsTrigger value="gameplay">Gameplay Gifts</TabsTrigger>
          <TabsTrigger value="preview">Preview</TabsTrigger>
        </TabsList>

        <TabsContent value="config" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Settings2 className="w-5 h-5 text-blue-400" />
                Mode Settings
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Mode Type */}
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">Mode Type</label>
                <div className="flex gap-4">
                  <button
                    onClick={() => setConfig({ ...config, type: 'normal' })}
                    className={`flex-1 p-4 rounded-lg border transition-colors ${
                      config.type === 'normal'
                        ? 'border-blue-500 bg-blue-500/10'
                        : 'border-slate-700 bg-slate-800'
                    }`}
                  >
                    <Sticker className="w-6 h-6 text-blue-400 mb-2" />
                    <p className="text-white font-medium">Normal Mode</p>
                    <p className="text-slate-400 text-sm">Show stickers without counting</p>
                  </button>
                  <button
                    onClick={() => setConfig({ ...config, type: 'scoring' })}
                    className={`flex-1 p-4 rounded-lg border transition-colors ${
                      config.type === 'scoring'
                        ? 'border-purple-500 bg-purple-500/10'
                        : 'border-slate-700 bg-slate-800'
                    }`}
                  >
                    <Sparkles className="w-6 h-6 text-purple-400 mb-2" />
                    <p className="text-white font-medium">Scoring Mode</p>
                    <p className="text-slate-400 text-sm">Count gifts with timer/decay</p>
                  </button>
                </div>
              </div>

              {config.type === 'scoring' && (
                <>
                  {/* Countdown */}
                  <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                    <div className="flex items-center gap-3">
                      <Clock className="w-5 h-5 text-amber-400" />
                      <div>
                        <p className="text-white font-medium">Countdown Timer</p>
                        <p className="text-slate-400 text-sm">Reset scores after countdown</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      {config.countdownEnabled && (
                        <Input
                          type="number"
                          value={config.countdownDuration}
                          onChange={(e) =>
                            setConfig({ ...config, countdownDuration: parseInt(e.target.value) || 60 })
                          }
                          className="w-20 bg-slate-900 border-slate-700 text-white"
                        />
                      )}
                      <Switch
                        checked={config.countdownEnabled}
                        onCheckedChange={(checked) =>
                          setConfig({ ...config, countdownEnabled: checked, decayEnabled: false })
                        }
                      />
                    </div>
                  </div>

                  {/* Decay */}
                  <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                    <div className="flex items-center gap-3">
                      <Timer className="w-5 h-5 text-red-400" />
                      <div>
                        <p className="text-white font-medium">Score Decay</p>
                        <p className="text-slate-400 text-sm">Clear scores if no gifts received</p>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      {config.decayEnabled && (
                        <Input
                          type="number"
                          value={config.decayDuration}
                          onChange={(e) =>
                            setConfig({ ...config, decayDuration: parseInt(e.target.value) || 30 })
                          }
                          className="w-20 bg-slate-900 border-slate-700 text-white"
                        />
                      )}
                      <Switch
                        checked={config.decayEnabled}
                        onCheckedChange={(checked) =>
                          setConfig({ ...config, decayEnabled: checked, countdownEnabled: false })
                        }
                      />
                    </div>
                  </div>
                </>
              )}

              {/* Auto Flip */}
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div className="flex items-center gap-3">
                  <FlipHorizontal className="w-5 h-5 text-green-400" />
                  <div>
                    <p className="text-white font-medium">Auto Page Flip</p>
                    <p className="text-slate-400 text-sm">Automatically switch between pages</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  {config.autoFlipEnabled && (
                    <Input
                      type="number"
                      value={config.flipInterval}
                      onChange={(e) =>
                        setConfig({ ...config, flipInterval: parseInt(e.target.value) || 10 })
                      }
                      className="w-20 bg-slate-900 border-slate-700 text-white"
                      placeholder="Seconds"
                    />
                  )}
                  <Switch
                    checked={config.autoFlipEnabled}
                    onCheckedChange={(checked) => setConfig({ ...config, autoFlipEnabled: checked })}
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="anchors" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">Anchor Configuration</CardTitle>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-96">
                <div className="space-y-3">
                  {anchors.map((anchor, index) => (
                    <div
                      key={anchor.id}
                      className="flex items-center gap-4 p-4 bg-slate-800 rounded-lg"
                    >
                      <div className="w-8 h-8 bg-slate-700 rounded-full flex items-center justify-center text-slate-400 font-medium">
                        {index + 1}
                      </div>

                      <Avatar className="w-12 h-12">
                        <AvatarImage src={anchor.avatar} />
                        <AvatarFallback className="bg-gradient-to-br from-pink-500 to-rose-600 text-white">
                          {anchor.name[0]}
                        </AvatarFallback>
                      </Avatar>

                      <div className="flex-1">
                        <Input
                          value={anchor.name}
                          onChange={(e) => {
                            // Update anchor name
                          }}
                          className="bg-slate-900 border-slate-700 text-white mb-2"
                          placeholder="Anchor Name"
                        />
                        <Input
                          value={anchor.tiktokId || ''}
                          onChange={(e) => {
                            // Update TikTok ID
                          }}
                          className="bg-slate-900 border-slate-700 text-white text-sm"
                          placeholder="TikTok ID (optional)"
                        />
                      </div>

                      <div className="w-48">
                        <Select>
                          <SelectTrigger className="bg-slate-900 border-slate-700 text-white">
                            <SelectValue placeholder="Select exclusive gifts" />
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="gift1">Rose</SelectItem>
                            <SelectItem value="gift2">TikTok</SelectItem>
                            <SelectItem value="gift3">Heart</SelectItem>
                          </SelectContent>
                        </Select>
                      </div>

                      <Button variant="ghost" size="icon" className="text-red-400 hover:text-red-300">
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  ))}

                  <Button
                    variant="outline"
                    className="w-full border-dashed border-slate-700 text-slate-400"
                    onClick={() => {
                      // Add new anchor
                    }}
                  >
                    <Plus className="w-4 h-4 mr-2" />
                    Add Anchor
                  </Button>
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="gameplay" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center justify-between">
                <span>Gameplay Gifts</span>
                <Button onClick={addGameplayGift} size="sm">
                  <Plus className="w-4 h-4 mr-2" />
                  Add Gift
                </Button>
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-3">
                {config.gameplayGifts.map((gift, index) => (
                  <div key={index} className="flex items-center gap-4 p-4 bg-slate-800 rounded-lg">
                    <Select
                      value={gift.giftId}
                      onValueChange={(value) => {
                        const newGifts = [...config.gameplayGifts]
                        newGifts[index].giftId = value
                        setConfig({ ...config, gameplayGifts: newGifts })
                      }}
                    >
                      <SelectTrigger className="w-48 bg-slate-900 border-slate-700 text-white">
                        <SelectValue placeholder="Select gift" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="gift1">Rose</SelectItem>
                        <SelectItem value="gift2">TikTok</SelectItem>
                        <SelectItem value="gift3">Heart</SelectItem>
                        <SelectItem value="gift4">Crown</SelectItem>
                      </SelectContent>
                    </Select>

                    <Select
                      value={gift.effect}
                      onValueChange={(value: 'bounce' | 'shake' | 'glow') => {
                        const newGifts = [...config.gameplayGifts]
                        newGifts[index].effect = value
                        setConfig({ ...config, gameplayGifts: newGifts })
                      }}
                    >
                      <SelectTrigger className="w-40 bg-slate-900 border-slate-700 text-white">
                        <SelectValue placeholder="Effect" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="bounce">Bounce</SelectItem>
                        <SelectItem value="shake">Shake</SelectItem>
                        <SelectItem value="glow">Glow</SelectItem>
                      </SelectContent>
                    </Select>

                    <Button
                      variant="ghost"
                      size="icon"
                      className="text-red-400 hover:text-red-300 ml-auto"
                      onClick={() => removeGameplayGift(index)}
                    >
                      <Trash2 className="w-4 h-4" />
                    </Button>
                  </div>
                ))}

                {config.gameplayGifts.length === 0 && (
                  <div className="text-center py-8 text-slate-500">
                    <Sparkles className="w-12 h-12 mx-auto mb-3 opacity-50" />
                    <p>No gameplay gifts configured</p>
                    <p className="text-sm">Add gifts with special effects</p>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="preview" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">Widget Preview</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="aspect-video bg-slate-800 rounded-lg flex items-center justify-center">
                <div className="text-center text-slate-500">
                  <Sticker className="w-16 h-16 mx-auto mb-4 opacity-50" />
                  <p>Widget preview will appear here</p>
                  <p className="text-sm">Configure settings and click Start to see preview</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
