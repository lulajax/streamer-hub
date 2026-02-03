import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar'
import { Gift, Users, Trophy, TrendingUp, Clock, Diamond, UserPlus, Minus, Plus } from 'lucide-react'
import type { GiftRecord, Anchor, LeaderboardEntry } from '@/types'

export function Dashboard() {
  const { t } = useTranslation()
  const { anchors, giftRecords, leaderboard, currentSession } = useStore()

  const totalGifts = giftRecords.length
  const totalDiamonds = giftRecords.reduce((sum, r) => sum + r.totalCost, 0)
  const uniqueUsers = new Set(giftRecords.map((r) => r.userId)).size

  const handleAddScore = (anchorId: string, amount: number) => {
    // Implement score adjustment
  }

  const handleEliminate = (anchorId: string) => {
    // Implement eliminate/revive
  }

  return (
    <div className="space-y-6">
      {/* Stats Overview */}
      <div className="grid grid-cols-4 gap-4">
        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Total Gifts</p>
                <p className="text-2xl font-bold text-white">{totalGifts}</p>
              </div>
              <div className="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
                <Gift className="w-5 h-5 text-blue-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Total Diamonds</p>
                <p className="text-2xl font-bold text-white">{totalDiamonds.toLocaleString()}</p>
              </div>
              <div className="w-10 h-10 bg-purple-500/20 rounded-lg flex items-center justify-center">
                <Diamond className="w-5 h-5 text-purple-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Unique Users</p>
                <p className="text-2xl font-bold text-white">{uniqueUsers}</p>
              </div>
              <div className="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center">
                <Users className="w-5 h-5 text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Active Anchors</p>
                <p className="text-2xl font-bold text-white">
                  {anchors.filter((a) => !a.isEliminated).length}/{anchors.length}
                </p>
              </div>
              <div className="w-10 h-10 bg-amber-500/20 rounded-lg flex items-center justify-center">
                <Trophy className="w-5 h-5 text-amber-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-2 gap-6">
        {/* Anchor Leaderboard */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white flex items-center gap-2">
              <Trophy className="w-5 h-5 text-amber-400" />
              Anchor Leaderboard
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-80">
              <div className="space-y-2">
                {anchors
                  .sort((a, b) => b.totalScore - a.totalScore)
                  .map((anchor, index) => (
                    <div
                      key={anchor.id}
                      className={`flex items-center gap-3 p-3 rounded-lg ${
                        anchor.isEliminated ? 'bg-slate-800/50 opacity-50' : 'bg-slate-800'
                      }`}
                    >
                      <div className="w-6 text-center">
                        {index === 0 && <span className="text-amber-400 font-bold">1</span>}
                        {index === 1 && <span className="text-slate-300 font-bold">2</span>}
                        {index === 2 && <span className="text-amber-600 font-bold">3</span>}
                        {index > 2 && <span className="text-slate-500">{index + 1}</span>}
                      </div>

                      <Avatar className="w-10 h-10">
                        <AvatarImage src={anchor.avatar} />
                        <AvatarFallback className="bg-gradient-to-br from-pink-500 to-rose-600 text-white">
                          {anchor.name[0]}
                        </AvatarFallback>
                      </Avatar>

                      <div className="flex-1">
                        <p className="text-white font-medium">{anchor.name}</p>
                        <p className="text-slate-400 text-sm">
                          {anchor.exclusiveGifts.length} exclusive gifts
                        </p>
                      </div>

                      <div className="text-right">
                        <p className="text-white font-bold">{anchor.totalScore.toLocaleString()}</p>
                        <p className="text-slate-400 text-xs">points</p>
                      </div>

                      <div className="flex items-center gap-1">
                        <Button
                          variant="ghost"
                          size="icon"
                          className="w-7 h-7 text-green-400 hover:text-green-300 hover:bg-green-500/20"
                          onClick={() => handleAddScore(anchor.id, 100)}
                        >
                          <Plus className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="w-7 h-7 text-red-400 hover:text-red-300 hover:bg-red-500/20"
                          onClick={() => handleAddScore(anchor.id, -100)}
                        >
                          <Minus className="w-4 h-4" />
                        </Button>
                      </div>

                      <Button
                        variant="ghost"
                        size="sm"
                        className={
                          anchor.isEliminated
                            ? 'text-green-400 hover:text-green-300'
                            : 'text-red-400 hover:text-red-300'
                        }
                        onClick={() => handleEliminate(anchor.id)}
                      >
                        {anchor.isEliminated ? 'Revive' : 'Eliminate'}
                      </Button>
                    </div>
                  ))}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>

        {/* Recent Gift Records */}
        <Card className="bg-slate-900 border-slate-800">
          <CardHeader>
            <CardTitle className="text-white flex items-center gap-2">
              <Gift className="w-5 h-5 text-pink-400" />
              Recent Gifts
            </CardTitle>
          </CardHeader>
          <CardContent>
            <ScrollArea className="h-80">
              <div className="space-y-2">
                {giftRecords.slice(0, 20).map((record) => (
                  <div
                    key={record.id}
                    className="flex items-center gap-3 p-2 rounded-lg bg-slate-800/50"
                  >
                    <Avatar className="w-8 h-8">
                      <AvatarImage src={record.userAvatar} />
                      <AvatarFallback className="bg-slate-700 text-slate-300 text-xs">
                        {record.userName[0]}
                      </AvatarFallback>
                    </Avatar>

                    <div className="flex-1 min-w-0">
                      <p className="text-white text-sm truncate">{record.userName}</p>
                      <p className="text-slate-400 text-xs">
                        to {record.anchorName}
                      </p>
                    </div>

                    <div className="flex items-center gap-2">
                      <img
                        src={record.giftIcon}
                        alt={record.giftName}
                        className="w-6 h-6"
                        onError={(e) => {
                          (e.target as HTMLImageElement).src = 'https://via.placeholder.com/24'
                        }}
                      />
                      <span className="text-white text-sm">x{record.quantity}</span>
                    </div>

                    <Badge
                      variant="secondary"
                      className={`text-xs ${
                        record.bindType === 'auto'
                          ? 'bg-slate-700 text-slate-300'
                          : record.bindType === 'manual'
                          ? 'bg-red-500/20 text-red-400'
                          : record.bindType === 'single'
                          ? 'bg-amber-500/20 text-amber-400'
                          : 'bg-slate-700 text-slate-500'
                      }`}
                    >
                      {record.totalCost}
                    </Badge>
                  </div>
                ))}

                {giftRecords.length === 0 && (
                  <div className="text-center py-8 text-slate-500">
                    <Gift className="w-12 h-12 mx-auto mb-3 opacity-50" />
                    <p>No gift records yet</p>
                    <p className="text-sm">Gifts will appear here when received</p>
                  </div>
                )}
              </div>
            </ScrollArea>
          </CardContent>
        </Card>
      </div>

      {/* Session Info */}
      {currentSession && (
        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className="flex items-center gap-2">
                  <Clock className="w-4 h-4 text-slate-400" />
                  <span className="text-slate-300">
                    Round {currentSession.currentRound} of {currentSession.rounds.length}
                  </span>
                </div>
                <Badge
                  variant={
                    currentSession.status === 'running'
                      ? 'default'
                      : currentSession.status === 'paused'
                      ? 'secondary'
                      : 'outline'
                  }
                >
                  {currentSession.status.toUpperCase()}
                </Badge>
              </div>
              <div className="text-slate-400 text-sm">
                Started: {currentSession.startedAt ? new Date(currentSession.startedAt).toLocaleString() : 'N/A'}
              </div>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
