import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useToast } from '@/hooks/use-toast'
import {
  BarChart3,
  Download,
  Calendar,
  Trash2,
  FileSpreadsheet,
  Users,
  Gift,
  Diamond,
  TrendingUp,
  Filter,
} from 'lucide-react'
import type { ReportData } from '@/types'

// Mock session data
const mockSessions: ReportData[] = [
  {
    sessionId: 'session-1',
    roomId: 'room-1',
    startTime: Date.now() - 86400000 * 2,
    endTime: Date.now() - 86400000 * 2 + 3600000,
    totalGifts: 1250,
    totalDiamonds: 45600,
    anchorStats: [
      { anchorId: 'a1', anchorName: 'Alice', totalScore: 15000, giftCount: 450, uniqueUsers: 89, roundsWon: 3 },
      { anchorId: 'a2', anchorName: 'Bob', totalScore: 12000, giftCount: 380, uniqueUsers: 76, roundsWon: 2 },
      { anchorId: 'a3', anchorName: 'Carol', totalScore: 9800, giftCount: 320, uniqueUsers: 65, roundsWon: 1 },
    ],
    userStats: [
      { userId: 'u1', userName: 'Fan1', totalSpent: 5000, giftCount: 50, favoriteAnchor: 'Alice' },
      { userId: 'u2', userName: 'Fan2', totalSpent: 3200, giftCount: 35, favoriteAnchor: 'Bob' },
    ],
    giftStats: [
      { giftId: 'g1', giftName: 'Rose', totalCount: 500, totalValue: 5000 },
      { giftId: 'g2', giftName: 'TikTok', totalCount: 200, totalValue: 20000 },
    ],
  },
  {
    sessionId: 'session-2',
    roomId: 'room-1',
    startTime: Date.now() - 86400000,
    endTime: Date.now() - 86400000 + 7200000,
    totalGifts: 2100,
    totalDiamonds: 78900,
    anchorStats: [
      { anchorId: 'a1', anchorName: 'Alice', totalScore: 25000, giftCount: 700, uniqueUsers: 120, roundsWon: 5 },
      { anchorId: 'a2', anchorName: 'Bob', totalScore: 18000, giftCount: 550, uniqueUsers: 95, roundsWon: 3 },
    ],
    userStats: [
      { userId: 'u3', userName: 'Fan3', totalSpent: 8000, giftCount: 80, favoriteAnchor: 'Alice' },
    ],
    giftStats: [
      { giftId: 'g1', giftName: 'Rose', totalCount: 800, totalValue: 8000 },
      { giftId: 'g3', giftName: 'Crown', totalCount: 50, totalValue: 25000 },
    ],
  },
]

export function Reports() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const [sessions, setSessions] = useState<ReportData[]>(mockSessions)
  const [selectedSession, setSelectedSession] = useState<ReportData | null>(null)
  const [dateFilter, setDateFilter] = useState('')

  const handleExportExcel = async (session: ReportData) => {
    // In production, this would generate and download an Excel file
    toast({
      title: 'Exporting...',
      description: 'Excel report is being generated',
    })

    // Simulate export delay
    setTimeout(() => {
      toast({
        title: 'Export Complete',
        description: `Session report_${session.sessionId}.xlsx downloaded`,
      })
    }, 1500)
  }

  const handleDeleteSession = (sessionId: string) => {
    setSessions(sessions.filter((s) => s.sessionId !== sessionId))
    if (selectedSession?.sessionId === sessionId) {
      setSelectedSession(null)
    }
    toast({
      title: 'Session Deleted',
      description: 'Session data has been removed',
    })
  }

  const totalStats = sessions.reduce(
    (acc, session) => ({
      totalGifts: acc.totalGifts + session.totalGifts,
      totalDiamonds: acc.totalDiamonds + session.totalDiamonds,
      totalSessions: acc.totalSessions + 1,
    }),
    { totalGifts: 0, totalDiamonds: 0, totalSessions: 0 }
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white flex items-center gap-2">
            <BarChart3 className="w-6 h-6 text-amber-400" />
            {t('reports')}
          </h1>
          <p className="text-slate-400">View and export session data</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Calendar className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
            <Input
              type="date"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
              className="pl-10 bg-slate-800 border-slate-700 text-white w-40"
            />
          </div>
          <Button variant="outline" className="border-slate-700 text-slate-300">
            <Filter className="w-4 h-4 mr-2" />
            Filter
          </Button>
        </div>
      </div>

      {/* Stats Overview */}
      <div className="grid grid-cols-4 gap-4">
        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Total Sessions</p>
                <p className="text-2xl font-bold text-white">{totalStats.totalSessions}</p>
              </div>
              <div className="w-10 h-10 bg-blue-500/20 rounded-lg flex items-center justify-center">
                <BarChart3 className="w-5 h-5 text-blue-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Total Gifts</p>
                <p className="text-2xl font-bold text-white">{totalStats.totalGifts.toLocaleString()}</p>
              </div>
              <div className="w-10 h-10 bg-pink-500/20 rounded-lg flex items-center justify-center">
                <Gift className="w-5 h-5 text-pink-400" />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-slate-900 border-slate-800">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-slate-400 text-sm">Total Diamonds</p>
                <p className="text-2xl font-bold text-white">
                  {totalStats.totalDiamonds.toLocaleString()}
                </p>
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
                <p className="text-slate-400 text-sm">Avg Diamonds/Session</p>
                <p className="text-2xl font-bold text-white">
                  {Math.round(totalStats.totalDiamonds / (totalStats.totalSessions || 1)).toLocaleString()}
                </p>
              </div>
              <div className="w-10 h-10 bg-green-500/20 rounded-lg flex items-center justify-center">
                <TrendingUp className="w-5 h-5 text-green-400" />
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Tabs defaultValue="sessions" className="w-full">
        <TabsList className="bg-slate-900 border border-slate-800">
          <TabsTrigger value="sessions">Sessions</TabsTrigger>
          <TabsTrigger value="details">Session Details</TabsTrigger>
        </TabsList>

        <TabsContent value="sessions" className="space-y-4">
          <Card className="bg-slate-900 border-slate-800">
            <CardHeader>
              <CardTitle className="text-white">Session History</CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow className="border-slate-800">
                    <TableHead className="text-slate-400">Date</TableHead>
                    <TableHead className="text-slate-400">Duration</TableHead>
                    <TableHead className="text-slate-400">Gifts</TableHead>
                    <TableHead className="text-slate-400">Diamonds</TableHead>
                    <TableHead className="text-slate-400">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sessions.map((session) => {
                    const duration = session.endTime
                      ? Math.round((session.endTime - session.startTime) / 60000)
                      : 0

                    return (
                      <TableRow key={session.sessionId} className="border-slate-800">
                        <TableCell className="text-white">
                          {new Date(session.startTime).toLocaleDateString()}
                        </TableCell>
                        <TableCell className="text-slate-300">{duration} min</TableCell>
                        <TableCell className="text-slate-300">
                          {session.totalGifts.toLocaleString()}
                        </TableCell>
                        <TableCell className="text-purple-400 font-medium">
                          {session.totalDiamonds.toLocaleString()}
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center gap-2">
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => setSelectedSession(session)}
                              className="text-blue-400 hover:text-blue-300"
                            >
                              View
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleExportExcel(session)}
                              className="text-green-400 hover:text-green-300"
                            >
                              <FileSpreadsheet className="w-4 h-4 mr-1" />
                              Export
                            </Button>
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleDeleteSession(session.sessionId)}
                              className="text-red-400 hover:text-red-300"
                            >
                              <Trash2 className="w-4 h-4" />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    )
                  })}

                  {sessions.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={5} className="text-center py-8 text-slate-500">
                        <BarChart3 className="w-12 h-12 mx-auto mb-3 opacity-50" />
                        <p>No session data available</p>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="details" className="space-y-4">
          {selectedSession ? (
            <>
              {/* Anchor Stats */}
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white flex items-center gap-2">
                    <Users className="w-5 h-5 text-blue-400" />
                    Anchor Statistics
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <Table>
                    <TableHeader>
                      <TableRow className="border-slate-800">
                        <TableHead className="text-slate-400">Anchor</TableHead>
                        <TableHead className="text-slate-400">Total Score</TableHead>
                        <TableHead className="text-slate-400">Gift Count</TableHead>
                        <TableHead className="text-slate-400">Unique Users</TableHead>
                        <TableHead className="text-slate-400">Rounds Won</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {selectedSession.anchorStats.map((stat) => (
                        <TableRow key={stat.anchorId} className="border-slate-800">
                          <TableCell className="text-white font-medium">{stat.anchorName}</TableCell>
                          <TableCell className="text-purple-400">{stat.totalScore.toLocaleString()}</TableCell>
                          <TableCell className="text-slate-300">{stat.giftCount}</TableCell>
                          <TableCell className="text-slate-300">{stat.uniqueUsers}</TableCell>
                          <TableCell className="text-amber-400">{stat.roundsWon}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>

              {/* Gift Stats */}
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white flex items-center gap-2">
                    <Gift className="w-5 h-5 text-pink-400" />
                    Gift Statistics
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <Table>
                    <TableHeader>
                      <TableRow className="border-slate-800">
                        <TableHead className="text-slate-400">Gift</TableHead>
                        <TableHead className="text-slate-400">Total Count</TableHead>
                        <TableHead className="text-slate-400">Total Value</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {selectedSession.giftStats.map((stat) => (
                        <TableRow key={stat.giftId} className="border-slate-800">
                          <TableCell className="text-white font-medium">{stat.giftName}</TableCell>
                          <TableCell className="text-slate-300">{stat.totalCount}</TableCell>
                          <TableCell className="text-purple-400">{stat.totalValue.toLocaleString()}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>

              {/* Top Users */}
              <Card className="bg-slate-900 border-slate-800">
                <CardHeader>
                  <CardTitle className="text-white flex items-center gap-2">
                    <Diamond className="w-5 h-5 text-amber-400" />
                    Top Contributors
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <Table>
                    <TableHeader>
                      <TableRow className="border-slate-800">
                        <TableHead className="text-slate-400">User</TableHead>
                        <TableHead className="text-slate-400">Total Spent</TableHead>
                        <TableHead className="text-slate-400">Gift Count</TableHead>
                        <TableHead className="text-slate-400">Favorite Anchor</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {selectedSession.userStats.map((stat) => (
                        <TableRow key={stat.userId} className="border-slate-800">
                          <TableCell className="text-white font-medium">{stat.userName}</TableCell>
                          <TableCell className="text-purple-400">{stat.totalSpent.toLocaleString()}</TableCell>
                          <TableCell className="text-slate-300">{stat.giftCount}</TableCell>
                          <TableCell className="text-blue-400">{stat.favoriteAnchor}</TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </CardContent>
              </Card>
            </>
          ) : (
            <Card className="bg-slate-900 border-slate-800">
              <CardContent className="p-8 text-center">
                <BarChart3 className="w-16 h-16 mx-auto mb-4 text-slate-600" />
                <p className="text-slate-400">Select a session from the Sessions tab to view details</p>
              </CardContent>
            </Card>
          )}
        </TabsContent>
      </Tabs>
    </div>
  )
}
