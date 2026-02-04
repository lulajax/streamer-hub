import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { ScrollArea } from '@/components/ui/scroll-area'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { useToast } from '@/hooks/use-toast'
import { Dashboard } from '@/pages/Dashboard'
import { StickerMode } from '@/pages/StickerMode'
import { PKMode } from '@/pages/PKMode'
import { FreeMode } from '@/pages/FreeMode'
import { Widgets } from '@/pages/Widgets'
import { Reports } from '@/pages/Reports'
import { Settings } from '@/pages/Settings'
import {
  LayoutDashboard,
  Sticker,
  Swords,
  Users,
  LayoutTemplate,
  BarChart3,
  Settings2,
  Radio,
  Power,
  Globe,
  LogOut,
  History,
  CheckCircle2,
  XCircle,
} from 'lucide-react'

export function MainLayout() {
  const { t, i18n } = useTranslation()
  const { toast } = useToast()
  const {
    currentRoom,
    setCurrentRoom,
    activation,
    user,
    logout,
    recentRooms,
    addRecentRoom,
  } = useStore()
  const subscriptionExpiresAt = user?.subscriptionExpiresAt
  const subscriptionType = user?.subscriptionType ?? 'FREE'
  const isSubscriptionActive =
    subscriptionType !== 'FREE' &&
    (subscriptionExpiresAt ? Date.now() < subscriptionExpiresAt : false)
  const [activeTab, setActiveTab] = useState('dashboard')
  const [isSwitchDialogOpen, setIsSwitchDialogOpen] = useState(false)
  const [roomInput, setRoomInput] = useState('')
  const [isSwitching, setIsSwitching] = useState(false)

  const extractRoomId = (input: string): string | null => {
    const urlMatch = input.match(/@([^/\s]+)/)
    if (urlMatch) {
      return urlMatch[1]
    }
    if (/^[a-zA-Z0-9_.]+$/.test(input)) {
      return input
    }
    return null
  }

  const handleSwitchRoom = async (input: string) => {
    if (!isSubscriptionActive) {
      toast({
        title: t('error'),
        description: 'Your subscription has expired. Please renew to continue.',
        variant: 'destructive',
      })
      return
    }

    const roomId = extractRoomId(input)
    if (!roomId) {
      toast({
        title: t('error'),
        description: 'Invalid TikTok room ID or URL',
        variant: 'destructive',
      })
      return
    }

    setIsSwitching(true)
    try {
      if (currentRoom?.isConnected) {
        await window.electronAPI.monitor.close()
      }

      const result = await window.electronAPI.monitor.open(roomId)
      if (result.success) {
        const room = {
          id: `room-${Date.now()}`,
          tiktokId: roomId,
          isConnected: true,
          connectedAt: Date.now(),
        }
        setCurrentRoom(room)
        addRecentRoom(room)
        setIsSwitchDialogOpen(false)
        setRoomInput('')

        toast({
          title: t('success'),
          description: `Connected to @${roomId}`,
        })
      }
    } catch {
      toast({
        title: t('error'),
        description: 'Failed to connect to room',
        variant: 'destructive',
      })
    } finally {
      setIsSwitching(false)
    }
  }

  const handleLogout = async () => {
    await window.electronAPI.monitor.close()
    setCurrentRoom(null)
    logout()
  }

  const navItems = [
    { id: 'dashboard', label: t('dashboard'), icon: LayoutDashboard },
    { id: 'sticker', label: t('stickerMode'), icon: Sticker },
    { id: 'pk', label: t('pkMode'), icon: Swords },
    { id: 'free', label: t('freeMode'), icon: Users },
    { id: 'widgets', label: t('widgets'), icon: LayoutTemplate },
    { id: 'reports', label: t('reports'), icon: BarChart3 },
    { id: 'settings', label: t('settings'), icon: Settings2 },
  ]

  return (
    <div className="h-screen flex flex-col bg-slate-950">
      {/* Header */}
      <header className="h-14 bg-slate-900 border-b border-slate-800 flex items-center justify-between px-4 flex-shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 bg-gradient-to-br from-blue-500 to-purple-600 rounded-lg flex items-center justify-center">
            <span className="text-white font-bold text-sm">M</span>
          </div>
          <span className="text-white font-semibold">MCA</span>
          <Badge variant="secondary" className="bg-slate-800 text-slate-300">
            v1.0.0
          </Badge>
        </div>

        <div className="flex items-center gap-4">
          {currentRoom?.isConnected && (
            <div className="flex items-center gap-2">
              <Radio className="w-4 h-4 text-green-400 animate-pulse" />
              <span className="text-slate-300 text-sm">@{currentRoom.tiktokId}</span>
            </div>
          )}

          <div className="flex items-center gap-2">
            <Globe className="w-4 h-4 text-slate-400" />
            <select
              value={i18n.language}
              onChange={(e) => i18n.changeLanguage(e.target.value)}
              className="bg-slate-800 text-slate-300 text-sm rounded px-2 py-1 border border-slate-700"
            >
              <option value="zh">中文</option>
              <option value="en">English</option>
              <option value="vi">Tiếng Việt</option>
              <option value="th">ไทย</option>
            </select>
          </div>

          <Button
            variant="ghost"
            size="sm"
            onClick={() => setIsSwitchDialogOpen(true)}
            className="text-slate-400 hover:text-red-400 hover:bg-red-500/10"
          >
            <Power className="w-4 h-4 mr-1" />
            {t('switchRoom')}
          </Button>

          <Button
            variant="ghost"
            size="sm"
            onClick={handleLogout}
            className="text-slate-400 hover:text-amber-400 hover:bg-amber-500/10"
          >
            <LogOut className="w-4 h-4 mr-1" />
            {t('logout') || 'Logout'}
          </Button>
        </div>
      </header>

      {/* Main Content */}
      <div className="flex-1 flex overflow-hidden">
        {/* Sidebar */}
        <aside className="w-56 bg-slate-900 border-r border-slate-800 flex-shrink-0">
          <ScrollArea className="h-full">
            <nav className="p-2 space-y-1">
              {navItems.map((item) => {
                const Icon = item.icon
                return (
                  <button
                    key={item.id}
                    onClick={() => setActiveTab(item.id)}
                    className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                      activeTab === item.id
                        ? 'bg-blue-500/20 text-blue-400'
                        : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                    }`}
                  >
                    <Icon className="w-4 h-4" />
                    {item.label}
                  </button>
                )
              })}
            </nav>

            <div className="p-4 mt-auto">
              <div className="bg-slate-800/50 rounded-lg p-3">
                <p className="text-slate-400 text-xs mb-1">{t('deviceName')}</p>
                <p className="text-slate-200 text-sm font-medium">{activation.deviceName}</p>
                <p className="text-slate-500 text-xs mt-2">
                  Subscription: {subscriptionType} · {isSubscriptionActive ? 'Active' : 'Inactive'}
                </p>
                <p className="text-slate-500 text-xs mt-2">
                  {t('validUntil')}: {activation.expiresAt ? new Date(activation.expiresAt).toLocaleDateString() : 'N/A'}
                </p>
              </div>
            </div>
          </ScrollArea>
        </aside>

        {/* Content Area */}
        <main className="flex-1 overflow-hidden bg-slate-950">
          <ScrollArea className="h-full">
            <div className="p-6">
              {activeTab === 'dashboard' && <Dashboard />}
              {activeTab === 'sticker' && <StickerMode />}
              {activeTab === 'pk' && <PKMode />}
              {activeTab === 'free' && <FreeMode />}
              {activeTab === 'widgets' && <Widgets />}
              {activeTab === 'reports' && <Reports />}
              {activeTab === 'settings' && <Settings />}
            </div>
          </ScrollArea>
        </main>
      </div>

      <Dialog open={isSwitchDialogOpen} onOpenChange={setIsSwitchDialogOpen}>
        <DialogContent className="bg-slate-900 border-slate-800 text-white">
          <DialogHeader>
            <DialogTitle className="text-white">{t('switchRoom')}</DialogTitle>
            <DialogDescription className="text-slate-400">
              Enter TikTok username or full live URL
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <Input
                placeholder="@username or https://www.tiktok.com/@username/live"
                value={roomInput}
                onChange={(e) => setRoomInput(e.target.value)}
                className="flex-1 bg-slate-950 border-slate-700 text-white"
                onKeyDown={(e) => e.key === 'Enter' && handleSwitchRoom(roomInput)}
              />
              <Button
                onClick={() => handleSwitchRoom(roomInput)}
                disabled={isSwitching || !roomInput}
                className="bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
              >
                {isSwitching ? t('loading') : t('connect')}
              </Button>
            </div>

            {recentRooms.length > 0 && (
              <div className="space-y-2">
                <div className="flex items-center gap-2 text-slate-400 text-sm">
                  <History className="w-4 h-4" />
                  {t('recentRooms')}
                </div>
                <div className="space-y-2">
                  {recentRooms.map((room) => (
                    <button
                      key={room.id}
                      className="w-full flex items-center justify-between p-3 bg-slate-950/60 rounded-lg hover:bg-slate-950 transition-colors"
                      onClick={() => handleSwitchRoom(room.tiktokId)}
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 h-8 bg-gradient-to-br from-pink-500 to-rose-600 rounded-full flex items-center justify-center">
                          <span className="text-white font-bold text-xs">
                            @{room.tiktokId[0].toUpperCase()}
                          </span>
                        </div>
                        <div className="text-left">
                          <p className="text-white text-sm font-medium">@{room.tiktokId}</p>
                          <p className="text-slate-400 text-xs">
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
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </DialogContent>
      </Dialog>
    </div>
  )
}
