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
import { useToast } from '@/hooks/use-toast'
import {
  Users,
  Play,
  Pause,
  RotateCcw,
  Settings2,
  Clock,
  Target,
  Eye,
  EyeOff,
  ChevronRight,
  Trophy,
} from 'lucide-react'
import type { FreeModeConfig, Anchor } from '@/types'

export function FreeMode() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { anchors } = useStore()
  const [config, setConfig] = useState<FreeModeConfig>({
    roundDuration: 300,
    targetScore: 10000,
    showRoundNumber: true,
    displayRange: [1, 10],
  })
  const [isRunning, setIsRunning] = useState(false)
  const [currentRound, setCurrentRound] = useState(1)
  const [currentAnchorIndex, setCurrentAnchorIndex] = useState(0)
  const [showNames, setShowNames] = useState(true)
  const [roundHistory, setRoundHistory] = useState<
    { round: number; anchorId: string; score: number; targetReached: boolean }[]
  >([])

  const handleStart = () => {
    if (anchors.length === 0) {
      toast({
        title: 'Error',
        description: 'Please add at least one anchor',
        variant: 'destructive',
      })
      return
    }
    setIsRunning(true)
    toast({
      title: 'Free Mode Started',
      description: `Round ${currentRound} - ${anchors[currentAnchorIndex]?.name}`,
    })
  }

  const handlePause = () => {
    setIsRunning(false)
    toast({
      title: 'Free Mode Paused',
    })
  }

  const handleEndRound = () => {
    const currentAnchor = anchors[currentAnchorIndex]
    if (currentAnchor) {
      setRoundHistory([
        ...roundHistory,
        {
          round: currentRound,
          anchorId: currentAnchor.id,
          score: currentAnchor.totalScore,
          targetReached: currentAnchor.totalScore >= config.targetScore,
        },
      ])
    }

    setIsRunning(false)
    setCurrentRound((prev) => prev + 1)
    setCurrentAnchorIndex((prev) => (prev + 1) % anchors.length)

    toast({
      title: 'Round Ended',
      description: `Next: ${anchors[(currentAnchorIndex + 1) % anchors.length]?.name}`,
    })
  }

  const handleReset = () => {
    setIsRunning(false)
    setCurrentRound(1)
    setCurrentAnchorIndex(0)
    setRoundHistory([])
    toast({
      title: 'Free Mode Reset',
    })
  }

  const currentAnchor = anchors[currentAnchorIndex]
  const sortedAnchors = [...anchors].sort((a, b) => b.totalScore - a.totalScore)

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <Users className="w-6 h-6 text-green-400" />
            {t('freeMode')}
          </h1>
          <p className="text-slate-400">Free rotation mode for talent showcase</p>
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
            <>
              <Button onClick={handlePause} variant="secondary">
                <Pause className="w-4 h-4 mr-2" />
                Pause
              </Button>
              <Button onClick={handleEndRound} variant="destructive">
                <ChevronRight className="w-4 h-4 mr-2" />
                End Round
              </Button>
            </>
          ) : (
            <Button onClick={handleStart} className="bg-green-600 hover:bg-green-700">
              <Play className="w-4 h-4 mr-2" />
              Start Round
            </Button>
          )}
        </div>
      </div>

      {/* Current Round Info */}
      <Card className="bg-slate-900 border-slate-800">
        <CardContent className="p-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-6">
              {config.showRoundNumber && (
                <div className="text-center">
                  <p className="text-slate-400 text-sm">Round</p>
                  <p className="text-3xl font-bold text-white">{currentRound}</p>
                </div>
              )}

              {currentAnchor && (
                <div className="flex items-center gap-4">
                  <Avatar className="w-16 h-16">
                    <AvatarImage src={currentAnchor.avatar} />
                    <AvatarFallback className="bg-gradient-to-br from-green-500 to-emerald-600 text-white text-xl">
                      {currentAnchor.name[0]}
                    </AvatarFallback>
                  </Avatar>
                  <div>
                    <p className="text-slate-400 text-sm">Current Anchor</p>
                    <p className="text-2xl font-bold text-white">
                      {showNames ? currentAnchor.name : '???'}
                    </p>
                  </div>
                </div>
              )}
            </div>

            <div className="flex items-center gap-6">
              <div className="text-center">
                <p className="text-slate-400 text-sm">Current Score</p>
                <p className="text-3xl font-bold text-green-400">
                  {currentAnchor?.totalScore.toLocaleString() || 0}
                </p>
              </div>

              <div className="text-center">
                <p className="text-slate-400 text-sm">Target</p>
                <p className="text-3xl font-bold text-amber-400">
                  {config.targetScore.toLocaleString()}
                </p>
              </div>

              {currentAnchor && currentAnchor.totalScore >= config.targetScore && (
                <Badge className="bg-green-500 text-white px-4 py-2">
                  <Trophy className="w-4 h-4 mr-1" />
                  Target Reached!
                </Badge>
              )}
            </div>
          </div>

          {/* Progress Bar */}
          <div className="mt-6">
            <div className="h-4 bg-slate-800 rounded-full overflow-hidden">
              <div
                className="h-full bg-gradient-to-r from-green-500 to-emerald-500 transition-all duration-500"
                style={{
                  width: `${Math.min(
                    ((currentAnchor?.totalScore || 0) / config.targetScore) * 100,
                    100
                  )}%`,
                }}
              />
            </div>
            <div className="flex justify-between mt-2 text-sm text-slate-400">
              <span>0</span>
              <span>{config.targetScore.toLocaleString()}</span>
            </div>
          </div>
        </CardContent>
      </Card>

      <Tabs defaultValue="anchors" className="w-full">
        <TabsList className="bg-slate-900 border border-slate-800">
          <TabsTrigger value="anchors">Anchor Queue</TabsTrigger>
          <TabsTrigger value="history">Round History</TabsTrigger>
          <TabsTrigger value="config">Configuration</TabsTrigger>
          <TabsTrigger value="preview">Preview</TabsTrigger>
        </TabsList>

        <TabsContent value="anchors" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader className="flex flex-row items-center justify-between">
              <CardTitle className="text-white">Anchor Rotation Queue</CardTitle>
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setShowNames(!showNames)}
                className="text-slate-400"
              >
                {showNames ? <Eye className="w-4 h-4 mr-1" /> : <EyeOff className="w-4 h-4 mr-1" />}
                {showNames ? 'Hide Names' : 'Show Names'}
              </Button>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-96">
                <div className="space-y-2">
                  {anchors.map((anchor, index) => {
                    const isCurrent = index === currentAnchorIndex
                    const isPast = index < currentAnchorIndex

                    return (
                      <div
                        key={anchor.id}
                        className={`flex items-center gap-4 p-4 rounded-lg ${
                          isCurrent
                            ? 'bg-green-500/20 border border-green-500/50'
                            : isPast
                            ? 'bg-slate-800/50 opacity-50'
                            : 'bg-slate-800'
                        }`}
                      >
                        <div className="w-8 text-center">
                          {isCurrent ? (
                            <Play className="w-5 h-5 text-green-400 mx-auto" />
                          ) : (
                            <span className="text-slate-500 font-medium">{index + 1}</span>
                          )}
                        </div>

                        <Avatar className="w-10 h-10">
                          <AvatarImage src={anchor.avatar} />
                          <AvatarFallback className="bg-slate-700 text-white">
                            {anchor.name[0]}
                          </AvatarFallback>
                        </Avatar>

                        <div className="flex-1">
                          <p className={`font-medium ${isCurrent ? 'text-green-400' : 'text-white'}`}>
                            {showNames ? anchor.name : '???'}
                          </p>
                          <p className="text-slate-400 text-sm">
                            {anchor.exclusiveGifts.length} exclusive gifts
                          </p>
                        </div>

                        <div className="text-right">
                          <p className="text-white font-bold">{anchor.totalScore.toLocaleString()}</p>
                          <p className="text-slate-400 text-xs">points</p>
                        </div>

                        {isCurrent && (
                          <Badge className="bg-green-500 text-white">
                            <Clock className="w-3 h-3 mr-1" />
                            Current
                          </Badge>
                        )}
                      </div>
                    )
                  })}

                  {anchors.length === 0 && (
                    <div className="text-center py-8 text-slate-500">
                      <Users className="w-12 h-12 mx-auto mb-3 opacity-50" />
                      <p>No anchors configured</p>
                      <p className="text-sm">Add anchors in the Dashboard</p>
                    </div>
                  )}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="history" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">Round History</CardTitle>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-96">
                <div className="space-y-2">
                  {roundHistory.map((record, index) => {
                    const anchor = anchors.find((a) => a.id === record.anchorId)
                    return (
                      <div
                        key={index}
                        className="flex items-center gap-4 p-4 bg-slate-800 rounded-lg"
                      >
                        <div className="w-8 text-center">
                          <span className="text-slate-500 font-medium">{record.round}</span>
                        </div>

                        <Avatar className="w-10 h-10">
                          <AvatarImage src={anchor?.avatar} />
                          <AvatarFallback className="bg-slate-700 text-white">
                            {anchor?.name[0] || '?'}
                          </AvatarFallback>
                        </Avatar>

                        <div className="flex-1">
                          <p className="text-white font-medium">{anchor?.name || 'Unknown'}</p>
                          <p className="text-slate-400 text-sm">
                            {new Date().toLocaleDateString()}
                          </p>
                        </div>

                        <div className="text-right">
                          <p className="text-white font-bold">{record.score.toLocaleString()}</p>
                          <p className="text-slate-400 text-xs">points</p>
                        </div>

                        {record.targetReached ? (
                          <Badge className="bg-green-500 text-white">
                            <Trophy className="w-3 h-3 mr-1" />
                            Target Met
                          </Badge>
                        ) : (
                          <Badge variant="secondary" className="text-slate-400">
                            Not Met
                          </Badge>
                        )}
                      </div>
                    )
                  })}

                  {roundHistory.length === 0 && (
                    <div className="text-center py-8 text-slate-500">
                      <Clock className="w-12 h-12 mx-auto mb-3 opacity-50" />
                      <p>No round history yet</p>
                      <p className="text-sm">Complete rounds to see history</p>
                    </div>
                  )}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="config" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2">
                <Settings2 className="w-5 h-5 text-amber-400" />
                Free Mode Settings
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-6">
              {/* Round Duration */}
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div className="flex items-center gap-3">
                  <Clock className="w-5 h-5 text-blue-400" />
                  <div>
                    <p className="text-white font-medium">Round Duration</p>
                    <p className="text-slate-400 text-sm">Time limit for each anchor</p>
                  </div>
                </div>
                <Input
                  type="number"
                  value={config.roundDuration}
                  onChange={(e) =>
                    setConfig({ ...config, roundDuration: parseInt(e.target.value) || 300 })
                  }
                  className="w-24 bg-slate-900 border-slate-700 text-white"
                />
              </div>

              {/* Target Score */}
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div className="flex items-center gap-3">
                  <Target className="w-5 h-5 text-green-400" />
                  <div>
                    <p className="text-white font-medium">Target Score</p>
                    <p className="text-slate-400 text-sm">Score needed to reach target</p>
                  </div>
                </div>
                <Input
                  type="number"
                  value={config.targetScore}
                  onChange={(e) =>
                    setConfig({ ...config, targetScore: parseInt(e.target.value) || 10000 })
                  }
                  className="w-32 bg-slate-900 border-slate-700 text-white"
                />
              </div>

              {/* Show Round Number */}
              <div className="flex items-center justify-between p-4 bg-slate-800 rounded-lg">
                <div className="flex items-center gap-3">
                  <Trophy className="w-5 h-5 text-amber-400" />
                  <div>
                    <p className="text-white font-medium">Show Round Number</p>
                    <p className="text-slate-400 text-sm">Display current round in widget</p>
                  </div>
                </div>
                <Switch
                  checked={config.showRoundNumber}
                  onCheckedChange={(checked) => setConfig({ ...config, showRoundNumber: checked })}
                />
              </div>

              {/* Display Range */}
              <div className="p-4 bg-slate-800 rounded-lg">
                <p className="text-white font-medium mb-3">Leaderboard Display Range</p>
                <div className="flex items-center gap-4">
                  <Input
                    type="number"
                    value={config.displayRange[0]}
                    onChange={(e) =>
                      setConfig({
                        ...config,
                        displayRange: [parseInt(e.target.value) || 1, config.displayRange[1]],
                      })
                    }
                    className="w-20 bg-slate-900 border-slate-700 text-white"
                  />
                  <span className="text-slate-400">to</span>
                  <Input
                    type="number"
                    value={config.displayRange[1]}
                    onChange={(e) =>
                      setConfig({
                        ...config,
                        displayRange: [config.displayRange[0], parseInt(e.target.value) || 10],
                      })
                    }
                    className="w-20 bg-slate-900 border-slate-700 text-white"
                  />
                </div>
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
                  <Users className="w-16 h-16 mx-auto mb-4 opacity-50" />
                  <p>Free mode widget preview will appear here</p>
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
