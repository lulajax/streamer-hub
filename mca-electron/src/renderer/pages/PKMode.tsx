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
import { Slider } from '@/components/ui/slider'
import { useToast } from '@/hooks/use-toast'
import {
  Swords,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Shield,
  Sword,
  Users,
  Clock,
  Snowflake,
  Trophy,
  GripVertical,
  Plus,
  Trash2,
} from 'lucide-react'
import type { PKModeConfig, Anchor } from '@/types'

export function PKMode() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { anchors } = useStore()
  const [config, setConfig] = useState<PKModeConfig>({
    advantage: 'defender',
    scoringMethod: 'individual',
    countdownEnabled: true,
    countdownDuration: 180,
    singleGiftBindEnabled: false,
    freezeEffectEnabled: false,
    freezeThresholds: {
      low: 20,
      medium: 40,
      high: 60,
    },
  })
  const [isRunning, setIsRunning] = useState(false)
  const [currentRound, setCurrentRound] = useState(1)
  const [defenderScore, setDefenderScore] = useState(0)
  const [attackerScore, setAttackerScore] = useState(0)
  const [defenders, setDefenders] = useState<Anchor[]>([])
  const [attackers, setAttackers] = useState<Anchor[]>([])

  const handleStart = () => {
    if (defenders.length === 0 || attackers.length === 0) {
      toast({
        title: 'Error',
        description: 'Please configure both defender and attacker teams',
        variant: 'destructive',
      })
      return
    }
    setIsRunning(true)
    toast({
      title: 'PK Started',
      description: `Round ${currentRound} - Defenders vs Attackers`,
    })
  }

  const handlePause = () => {
    setIsRunning(false)
    toast({
      title: 'PK Paused',
    })
  }

  const handleEndRound = () => {
    setIsRunning(false)
    const winner = defenderScore > attackerScore ? 'defender' : attackerScore > defenderScore ? 'attacker' : 'draw'
    toast({
      title: 'Round Ended',
      description: `Winner: ${winner === 'draw' ? 'Draw' : winner === 'defender' ? 'Defenders' : 'Attackers'}`,
    })
    setCurrentRound((prev) => prev + 1)
    setDefenderScore(0)
    setAttackerScore(0)
  }

  const addToTeam = (anchor: Anchor, team: 'defender' | 'attacker') => {
    if (team === 'defender') {
      setDefenders([...defenders, anchor])
    } else {
      setAttackers([...attackers, anchor])
    }
  }

  const removeFromTeam = (anchorId: string, team: 'defender' | 'attacker') => {
    if (team === 'defender') {
      setDefenders(defenders.filter((a) => a.id !== anchorId))
    } else {
      setAttackers(attackers.filter((a) => a.id !== anchorId))
    }
  }

  const totalDefenderScore = defenders.reduce((sum, a) => sum + a.totalScore, 0) + defenderScore
  const totalAttackerScore = attackers.reduce((sum, a) => sum + a.totalScore, 0) + attackerScore
  const maxScore = Math.max(totalDefenderScore, totalAttackerScore, 1)
  const defenderPercent = (totalDefenderScore / (totalDefenderScore + totalAttackerScore || 1)) * 100

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Swords className="w-6 h-6 text-red-400" />
            {t('pkMode')}
          </h1>
          <p className="text-slate-400">Team PK battle mode configuration</p>
        </div>
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            onClick={() => {
              setIsRunning(false)
              setCurrentRound(1)
              setDefenderScore(0)
              setAttackerScore(0)
            }}
            className="border-slate-700 text-slate-300"
          >
            <RotateCcw className="w-4 h-4 mr-2" />
            Reset
          </Button>
          {isRunning ? (
            <>
              <Button onClick={handlePause} variant="secondary">
                <Pause className="w-4 h-4 mr-2" />
                Pause
              </Button>
              <Button onClick={handleEndRound} variant="destructive">
                <Trophy className="w-4 h-4 mr-2" />
                End Round
              </Button>
            </>
          ) : (
            <Button onClick={handleStart} className="bg-red-600 hover:bg-red-700">
              <Play className="w-4 h-4 mr-2" />
              Start PK
            </Button>
          )}
        </div>
      </div>

      {/* PK Progress Bar */}
      <Card className="bg-slate-900 border-slate-800">
        <CardContent className="p-6">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-blue-500/20 rounded-full flex items-center justify-center">
                <Shield className="w-6 h-6 text-blue-400" />
              </div>
              <div>
                <p className="text-white font-bold text-lg">{totalDefenderScore.toLocaleString()}</p>
                <p className="text-blue-400 text-sm">Defenders</p>
              </div>
            </div>

            <div className="text-center">
              <Badge variant="secondary" className="text-lg px-4 py-1">
                Round {currentRound}
              </Badge>
              {config.countdownEnabled && isRunning && (
                <p className="text-amber-400 text-sm mt-1">
                  <Clock className="w-4 h-4 inline mr-1" />
                  {Math.floor(config.countdownDuration / 60)}:
                  {(config.countdownDuration % 60).toString().padStart(2, '0')}
                </p>
              )}
            </div>

            <div className="flex items-center gap-3 text-right">
              <div>
                <p className="text-white font-bold text-lg">{totalAttackerScore.toLocaleString()}</p>
                <p className="text-red-400 text-sm">Attackers</p>
              </div>
              <div className="w-12 h-12 bg-red-500/20 rounded-full flex items-center justify-center">
                <Sword className="w-6 h-6 text-red-400" />
              </div>
            </div>
          </div>

          {/* Progress Bar */}
          <div className="relative h-8 bg-slate-800 rounded-full overflow-hidden">
            <div
              className="absolute left-0 top-0 h-full bg-gradient-to-r from-blue-500 to-blue-600 transition-all duration-500"
              style={{ width: `${defenderPercent}%` }}
            />
            <div
              className="absolute right-0 top-0 h-full bg-gradient-to-l from-red-500 to-red-600 transition-all duration-500"
              style={{ width: `${100 - defenderPercent}%` }}
            />
            <div className="absolute inset-0 flex items-center justify-center">
              <span className="text-white font-bold text-sm drop-shadow-lg">
                {defenderPercent.toFixed(1)}% - {(100 - defenderPercent).toFixed(1)}%
              </span>
            </div>
          </div>

          {/* Freeze Effect Indicator */}
          {config.freezeEffectEnabled && (
            <div className="mt-4 flex items-center justify-center gap-4">
              <div className="flex items-center gap-2 text-cyan-400">
                <Snowflake className="w-4 h-4" />
                <span className="text-sm">Freeze Effect Active</span>
              </div>
              <div className="flex gap-2">
                <Badge variant="outline" className="border-cyan-500/50 text-cyan-400">
                  Low: {config.freezeThresholds.low}%
                </Badge>
                <Badge variant="outline" className="border-cyan-500/50 text-cyan-400">
                  Med: {config.freezeThresholds.medium}%
                </Badge>
                <Badge variant="outline" className="border-cyan-500/50 text-cyan-400">
                  High: {config.freezeThresholds.high}%
                </Badge>
              </div>
            </div>
          )}
        </CardContent>
      </Card>

      <Tabs defaultValue="teams" className="w-full">
        <TabsList className="bg-slate-900 border border-slate-800">
          <TabsTrigger value="teams">Team Setup</TabsTrigger>
          <TabsTrigger value="config">Configuration</TabsTrigger>
          <TabsTrigger value="preview">Preview</TabsTrigger>
        </TabsList>

        <TabsContent value="teams" className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            {/* Defenders */}
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader className="bg-blue-500/10">
                <CardTitle className="text-white flex items-center gap-2">
                  <Shield className="w-5 h-5 text-blue-400" />
                  Defenders
                </CardTitle>
              </CardHeader>
              <CardContent className="p-4">
                <ScrollArea className="h-64">
                  <div className="space-y-2">
                    {defenders.map((anchor) => (
                      <div
                        key={anchor.id}
                        className="flex items-center gap-3 p-3 bg-slate-800 rounded-lg"
                      >
                        <GripVertical className="w-4 h-4 text-slate-500" />
                        <Avatar className="w-8 h-8">
                          <AvatarImage src={anchor.avatar} />
                          <AvatarFallback className="bg-blue-500 text-white text-xs">
                            {anchor.name[0]}
                          </AvatarFallback>
                        </Avatar>
                        <span className="text-white flex-1">{anchor.name}</span>
                        <span className="text-blue-400 font-medium">{anchor.totalScore}</span>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="w-6 h-6 text-red-400"
                          onClick={() => removeFromTeam(anchor.id, 'defender')}
                        >
                          <Trash2 className="w-3 h-3" />
                        </Button>
                      </div>
                    ))}

                    {defenders.length === 0 && (
                      <div className="text-center py-8 text-slate-500">
                        <Shield className="w-12 h-12 mx-auto mb-3 opacity-50" />
                        <p>No defenders assigned</p>
                      </div>
                    )}
                  </div>
                </ScrollArea>

                <Select
                  onValueChange={(value) => {
                    const anchor = anchors.find((a) => a.id === value)
                    if (anchor) addToTeam(anchor, 'defender')
                  }}
                >
                  <SelectTrigger className="mt-4 bg-slate-800 border-slate-700 text-white">
                    <SelectValue placeholder="Add defender..." />
                  </SelectTrigger>
                  <SelectContent>
                    {anchors
                      .filter((a) => !defenders.find((d) => d.id === a.id) && !attackers.find((at) => at.id === a.id))
                      .map((anchor) => (
                        <SelectItem key={anchor.id} value={anchor.id}>
                          {anchor.name}
                        </SelectItem>
                      ))}
                  </SelectContent>
                </Select>
              </CardContent>
            </Card>

            {/* Attackers */}
            <Card className="bg-slate-900 border-slate-800">
              <CardHeader className="bg-red-500/10">
                <CardTitle className="text-white flex items-center gap-2">
                  <Sword className="w-5 h-5 text-red-400" />
                  Attackers
                </CardTitle>
              </CardHeader>
              <CardContent className="p-4">
                <ScrollArea className="h-64">
                  <div className="space-y-2">
                    {attackers.map((anchor) => (
                      <div
                        key={anchor.id}
                        className="flex items-center gap-3 p-3 bg-slate-800 rounded-lg"
                      >
                        <GripVertical className="w-4 h-4 text-slate-500" />
                        <Avatar className="w-8 h-8">
                          <AvatarImage src={anchor.avatar} />
                          <AvatarFallback className="bg-red-500 text-white text-xs">
                            {anchor.name[0]}
                          </AvatarFallback>
                        </Avatar>
                        <span className="text-white flex-1">{anchor.name}</span>
                        <span className="text-red-400 font-medium">{anchor.totalScore}</span>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="w-6 h-6 text-red-400"
                          onClick={() => removeFromTeam(anchor.id, 'attacker')}
                        >
                          <Trash2 className="w-3 h-3" />
                        </Button>
                      </div>
                    ))}

                    {attackers.length === 0 && (
                      <div className="text-center py-8 text-slate-500">
                        <Sword className="w-12 h-12 mx-auto mb-3 opacity-50" />
                        <p>No attackers assigned</p>
                      </div>
                    )}
                  </div>
                </ScrollArea>

                <Select
                  onValueChange={(value) => {
                    const anchor = anchors.find((a) => a.id === value)
                    if (anchor) addToTeam(anchor, 'attacker')
                  }}
                >
                  <SelectTrigger className="mt-4 bg-slate-800 border-slate-700 text-white">
                    <SelectValue placeholder="Add attacker..." />
                  </SelectTrigger>
                  <SelectContent>
                    {anchors
                      .filter((a) => !defenders.find((d) => d.id === a.id) && !attackers.find((at) => at.id === a.id))
                      .map((anchor) => (
                        <SelectItem key={anchor.id} value={anchor.id}>
                          {anchor.name}
                        </SelectItem>
                      ))}
                  </SelectContent>
                </Select>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="config" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Settings2 className="w-5 h-5 text-amber-400" />
                PK Settings
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Advantage */}
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">Advantage Setting</label>
                <Select
                  value={config.advantage}
                  onValueChange={(value: 'defender' | 'attacker' | 'draw') =>
                    setConfig({ ...config, advantage: value })
                  }
                >
                  <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="defender">Defender Advantage (Draw = Defender Wins)</SelectItem>
                    <SelectItem value="attacker">Attacker Advantage (Draw = Attacker Wins)</SelectItem>
                    <SelectItem value="draw">Allow Draw (Continue to Next Round)</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Scoring Method */}
              <div>
                <label className="text-sm font-medium text-slate-300 mb-2 block">Scoring Method</label>
                <Select
                  value={config.scoringMethod}
                  onValueChange={(value: 'individual' | 'split' | 'defender') =>
                    setConfig({ ...config, scoringMethod: value })
                  }
                >
                  <SelectTrigger className="bg-slate-800 border-slate-700 text-white">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="individual">Individual Score (Each gets their own)</SelectItem>
                    <SelectItem value="split">Split Score (Divide equally)</SelectItem>
                    <SelectItem value="defender">All to Defender (Even if they lose)</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Countdown */}
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div className="flex items-center gap-3">
                  <Clock className="w-5 h-5 text-amber-400" />
                  <div>
                    <p className="text-white font-medium">Countdown Timer</p>
                    <p className="text-slate-400 text-sm">Auto-end round when time expires</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  {config.countdownEnabled && (
                    <Input
                      type="number"
                      value={config.countdownDuration}
                      onChange={(e) =>
                        setConfig({ ...config, countdownDuration: parseInt(e.target.value) || 180 })
                      }
                      className="w-24 bg-slate-900 border-slate-700 text-white"
                      placeholder="Seconds"
                    />
                  )}
                  <Switch
                    checked={config.countdownEnabled}
                    onCheckedChange={(checked) => setConfig({ ...config, countdownEnabled: checked })}
                  />
                </div>
              </div>

              {/* Single Gift Bind */}
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div className="flex items-center gap-3">
                  <Users className="w-5 h-5 text-green-400" />
                  <div>
                    <p className="text-white font-medium">Single Gift Bind Mode</p>
                    <p className="text-slate-400 text-sm">One exclusive gift per team, user binds by sending</p>
                  </div>
                </div>
                <Switch
                  checked={config.singleGiftBindEnabled}
                  onCheckedChange={(checked) => setConfig({ ...config, singleGiftBindEnabled: checked })}
                />
              </div>

              {/* Freeze Effect */}
              <div className="space-y-4">
                <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                  <div className="flex items-center gap-3">
                    <Snowflake className="w-5 h-5 text-cyan-400" />
                    <div>
                      <p className="text-white font-medium">Freeze Effect</p>
                      <p className="text-slate-400 text-sm">Visual effect when team is behind</p>
                    </div>
                  </div>
                  <Switch
                    checked={config.freezeEffectEnabled}
                    onCheckedChange={(checked) => setConfig({ ...config, freezeEffectEnabled: checked })}
                  />
                </div>

                {config.freezeEffectEnabled && (
                  <div className="grid grid-cols-3 gap-4">
                    <div>
                      <label className="text-xs text-slate-400 mb-1 block">Low Threshold (%)</label>
                      <Input
                        type="number"
                        value={config.freezeThresholds.low}
                        onChange={(e) =>
                          setConfig({
                            ...config,
                            freezeThresholds: {
                              ...config.freezeThresholds,
                              low: parseInt(e.target.value) || 20,
                            },
                          })
                        }
                        className="bg-slate-800 border-slate-700 text-white"
                      />
                    </div>
                    <div>
                      <label className="text-xs text-slate-400 mb-1 block">Medium Threshold (%)</label>
                      <Input
                        type="number"
                        value={config.freezeThresholds.medium}
                        onChange={(e) =>
                          setConfig({
                            ...config,
                            freezeThresholds: {
                              ...config.freezeThresholds,
                              medium: parseInt(e.target.value) || 40,
                            },
                          })
                        }
                        className="bg-slate-800 border-slate-700 text-white"
                      />
                    </div>
                    <div>
                      <label className="text-xs text-slate-400 mb-1 block">High Threshold (%)</label>
                      <Input
                        type="number"
                        value={config.freezeThresholds.high}
                        onChange={(e) =>
                          setConfig({
                            ...config,
                            freezeThresholds: {
                              ...config.freezeThresholds,
                              high: parseInt(e.target.value) || 60,
                            },
                          })
                        }
                        className="bg-slate-800 border-slate-700 text-white"
                      />
                    </div>
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
                  <Swords className="w-16 h-16 mx-auto mb-4 opacity-50" />
                  <p>PK widget preview will appear here</p>
                  <p className="text-sm">Configure teams and click Start PK to see preview</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
