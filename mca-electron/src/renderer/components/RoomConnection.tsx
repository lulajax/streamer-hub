import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { useToast } from '@/hooks/use-toast'
import { Radio, History, Link2, ExternalLink, CheckCircle2, XCircle, Sparkles } from 'lucide-react'
import type { Room } from '@/types'

export function RoomConnection() {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { recentRooms, setCurrentRoom, addRecentRoom, activation, user } = useStore()
  const subscriptionExpiresAt = user?.subscriptionExpiresAt
  const subscriptionType = user?.subscriptionType ?? 'FREE'
  const isSubscriptionActive =
    subscriptionType !== 'FREE' &&
    (subscriptionExpiresAt ? Date.now() < subscriptionExpiresAt : false)
  const [roomInput, setRoomInput] = useState('')
  const [isConnecting, setIsConnecting] = useState(false)

  const extractRoomId = (input: string): string | null => {
    // Extract from URL like https://www.tiktok.com/@username/live
    const urlMatch = input.match(/@([^/\s]+)/)
    if (urlMatch) {
      return urlMatch[1]
    }
    // Direct ID input
    if (/^[a-zA-Z0-9_.]+$/.test(input)) {
      return input
    }
    return null
  }

  const handleConnect = async () => {
    if (!activation?.isActivated) {
      toast({
        title: t('error'),
        description: 'Please activate your subscription before connecting a room',
        variant: 'destructive',
      })
      return
    }

    if (!isSubscriptionActive) {
      toast({
        title: t('error'),
        description: 'Your subscription has expired. Please renew to continue.',
        variant: 'destructive',
      })
      return
    }

    const roomId = extractRoomId(roomInput)
    if (!roomId) {
      toast({
        title: t('error'),
        description: 'Invalid TikTok room ID or URL',
        variant: 'destructive',
      })
      return
    }

    setIsConnecting(true)

    try {
      // Open monitor window via Electron API
      const result = await window.electronAPI.monitor.open(roomId)

      if (result.success) {
        const room: Room = {
          id: `room-${Date.now()}`,
          tiktokId: roomId,
          isConnected: true,
          connectedAt: Date.now(),
        }

        setCurrentRoom(room)
        addRecentRoom(room)

        toast({
          title: t('success'),
          description: `Connected to @${roomId}`,
        })
      }
    } catch (error) {
      toast({
        title: t('error'),
        description: 'Failed to connect to room',
        variant: 'destructive',
      })
    } finally {
      setIsConnecting(false)
    }
  }

  const handleRecentConnect = async (room: Room) => {
    setRoomInput(room.tiktokId)
    await handleConnect()
  }

  const handleDisconnect = async () => {
    await window.electronAPI.monitor.close()
    setCurrentRoom(null)
    toast({
      title: t('info'),
      description: 'Disconnected from room',
    })
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-3 mb-4">
            <div className="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center shadow-lg shadow-blue-500/25">
              <Radio className="w-8 h-8 text-white" />
            </div>
          </div>
          <h1 className="text-3xl font-bold text-white mb-2">{t('connectRoom')}</h1>
          <p className="text-slate-400">Enter TikTok live room ID to start</p>
        </div>

        <Card className="bg-slate-800/50 border-slate-700 backdrop-blur mb-6">
          <CardHeader>
            <CardTitle className="text-white flex items-center gap-2">
              <Link2 className="w-5 h-5 text-blue-400" />
              {t('roomId')}
            </CardTitle>
            <CardDescription className="text-slate-400">
              Enter TikTok username or full live URL
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="flex gap-3">
              <Input
                placeholder="@username or https://www.tiktok.com/@username/live"
                value={roomInput}
                onChange={(e) => setRoomInput(e.target.value)}
                className="flex-1 bg-slate-900 border-slate-600 text-white"
                onKeyDown={(e) => e.key === 'Enter' && handleConnect()}
              />
              <Button
                onClick={handleConnect}
                disabled={isConnecting || !roomInput}
                className="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              >
                {isConnecting ? t('loading') : t('connect')}
              </Button>
            </div>

            <div className="flex items-center gap-2 text-sm text-slate-400">
              <Sparkles className="w-4 h-4" />
              <span>Examples: @username, username, or full URL</span>
            </div>
          </CardContent>
        </Card>

        {recentRooms.length > 0 && (
          <Card className="bg-slate-800/50 border-slate-700 backdrop-blur">
            <CardHeader>
              <CardTitle className="text-white flex items-center gap-2 text-lg">
                <History className="w-5 h-5 text-purple-400" />
                {t('recentRooms')}
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {recentRooms.map((room) => (
                  <div
                    key={room.id}
                    className="flex items-center justify-between p-3 bg-slate-900/50 rounded-lg hover:bg-slate-900 transition-colors cursor-pointer"
                    onClick={() => handleRecentConnect(room)}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 bg-gradient-to-br from-pink-500 to-rose-600 rounded-full flex items-center justify-center">
                        <span className="text-white font-bold text-sm">
                          @{room.tiktokId[0].toUpperCase()}
                        </span>
                      </div>
                      <div>
                        <p className="text-white font-medium">@{room.tiktokId}</p>
                        <p className="text-slate-400 text-sm">
                          {room.connectedAt
                            ? new Date(room.connectedAt).toLocaleDateString()
                            : 'Unknown date'}
                        </p>
                      </div>
                    </div>
                    <Badge
                      variant={room.isConnected ? 'default' : 'secondary'}
                      className={room.isConnected ? 'bg-green-500' : ''}
                    >
                      {room.isConnected ? (
                        <>
                          <CheckCircle2 className="w-3 h-3 mr-1" />
                          {t('connected')}
                        </>
                      ) : (
                        <>
                          <XCircle className="w-3 h-3 mr-1" />
                          {t('disconnected')}
                        </>
                      )}
                    </Badge>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        )}

        <div className="mt-6 p-4 bg-amber-500/10 border border-amber-500/30 rounded-lg">
          <div className="flex items-start gap-3">
            <ExternalLink className="w-5 h-5 text-amber-400 flex-shrink-0 mt-0.5" />
            <div>
              <p className="text-amber-200 font-medium mb-1">Monitor Window Notice</p>
              <p className="text-amber-200/70 text-sm">
                A monitor window will open to capture live data. Please do not close or interact with it.
                If login is required, use a separate TikTok account (not your streaming account).
              </p>
            </div>
          </div>
        </div>

        <div className="text-center mt-6 text-slate-500 text-sm space-y-1">
          <p>
            Subscription: {subscriptionType} · {isSubscriptionActive ? 'Active' : 'Inactive'}
          </p>
          <p>Device: {activation.deviceName}</p>
          <p className="mt-1">
            Valid until: {activation.expiresAt ? new Date(activation.expiresAt).toLocaleDateString() : 'N/A'}
          </p>
        </div>
      </div>
    </div>
  )
}

// Extend Window interface for Electron API
declare global {
  interface Window {
    electronAPI: {
      monitor: {
        open: (roomId: string) => Promise<{ success: boolean }>
        close: () => Promise<{ success: boolean }>
      }
    }
  }
}
