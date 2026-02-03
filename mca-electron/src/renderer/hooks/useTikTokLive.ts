import { useState, useEffect, useRef, useCallback } from 'react'
import { useToast } from '@/hooks/use-toast'

// Types for TikTok Live events
export interface TikTokGift {
  giftId: string
  giftName: string
  giftIcon: string
  diamondCost: number
  quantity: number
  totalCost: number
}

export interface TikTokUser {
  userId: string
  userName: string
  userNickname: string
  userAvatar: string
}

export interface TikTokGiftEvent {
  user: TikTokUser
  gift: TikTokGift
  repeatCount: number
  repeatEnd: boolean
}

export interface TikTokChatEvent {
  user: TikTokUser
  comment: string
}

export interface TikTokMemberEvent {
  user: TikTokUser
}

export interface TikTokLikeEvent {
  user: TikTokUser
  likeCount: number
  totalLikeCount: number
}

export interface TikTokRoomInfo {
  roomId: string
  title: string
  owner: TikTokUser
  viewerCount: number
  likeCount: number
  isLive: boolean
}

// TikTok Live Connector Hook
export function useTikTokLive() {
  const { toast } = useToast()
  
  const [isConnected, setIsConnected] = useState(false)
  const [isConnecting, setIsConnecting] = useState(false)
  const [roomInfo, setRoomInfo] = useState<TikTokRoomInfo | null>(null)
  const [availableGifts, setAvailableGifts] = useState<TikTokGift[]>([])
  const [error, setError] = useState<string | null>(null)
  
  const wsRef = useRef<WebSocket | null>(null)
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null)
  const uniqueIdRef = useRef<string>('')

  // Check if user is live
  const checkIsLive = useCallback(async (uniqueId: string): Promise<boolean> => {
    try {
      const response = await fetch(`http://localhost:8080/api/tiktok/is-live/${uniqueId}`)
      const data = await response.json()
      return data.success && data.data.isLive
    } catch (err) {
      console.error('Failed to check if user is live:', err)
      return false
    }
  }, [])

  // Get room info
  const getRoomInfo = useCallback(async (uniqueId: string): Promise<TikTokRoomInfo | null> => {
    try {
      const response = await fetch(`http://localhost:8080/api/tiktok/room-info/${uniqueId}`)
      const data = await response.json()
      
      if (data.success) {
        const info = data.data
        return {
          roomId: info.roomId || '',
          title: info.title || '',
          owner: {
            userId: info.owner?.userId || '',
            userName: info.owner?.uniqueId || '',
            userNickname: info.owner?.nickname || '',
            userAvatar: info.owner?.avatarThumb || '',
          },
          viewerCount: info.viewerCount || 0,
          likeCount: info.likeCount || 0,
          isLive: true,
        }
      }
      return null
    } catch (err) {
      console.error('Failed to get room info:', err)
      return null
    }
  }, [])

  // Get available gifts
  const getAvailableGifts = useCallback(async (roomId: string): Promise<TikTokGift[]> => {
    try {
      const response = await fetch(`http://localhost:8080/api/tiktok/gifts/${roomId}`)
      const data = await response.json()
      
      if (data.success && Array.isArray(data.data)) {
        return data.data.map((gift: any) => ({
          giftId: gift.giftId || '',
          giftName: gift.name || '',
          giftIcon: gift.image || '',
          diamondCost: gift.diamondCount || 0,
          quantity: 1,
          totalCost: gift.diamondCount || 0,
        }))
      }
      return []
    } catch (err) {
      console.error('Failed to get available gifts:', err)
      return []
    }
  }, [])

  // Connect to TikTok Live
  const connect = useCallback(async (uniqueId: string) => {
    if (isConnecting || isConnected) {
      return
    }

    setIsConnecting(true)
    setError(null)
    uniqueIdRef.current = uniqueId

    try {
      // First, check if user is live
      const isLive = await checkIsLive(uniqueId)
      if (!isLive) {
        setError('User is not currently live')
        setIsConnecting(false)
        return
      }

      // Get room info
      const info = await getRoomInfo(uniqueId)
      if (!info) {
        setError('Failed to get room information')
        setIsConnecting(false)
        return
      }
      setRoomInfo(info)

      // Get available gifts
      const gifts = await getAvailableGifts(info.roomId)
      setAvailableGifts(gifts)

      // Get WebSocket URL
      const wsResponse = await fetch(
        `http://localhost:8080/api/tiktok/websocket-url?uniqueId=${uniqueId}&roomId=${info.roomId}`
      )
      const wsData = await wsResponse.json()

      if (!wsData.success || !wsData.data.websocketUrl) {
        setError('Failed to get WebSocket connection URL')
        setIsConnecting(false)
        return
      }

      // Connect to WebSocket
      const wsUrl = wsData.data.websocketUrl
      wsRef.current = new WebSocket(wsUrl)

      wsRef.current.onopen = () => {
        console.log('Connected to TikTok Live')
        setIsConnected(true)
        setIsConnecting(false)
        toast({
          title: 'Connected',
          description: `Connected to @${uniqueId}'s live stream`,
        })
      }

      wsRef.current.onclose = () => {
        console.log('Disconnected from TikTok Live')
        setIsConnected(false)
        
        // Auto reconnect after 5 seconds
        if (uniqueIdRef.current) {
          reconnectTimeoutRef.current = setTimeout(() => {
            connect(uniqueIdRef.current)
          }, 5000)
        }
      }

      wsRef.current.onerror = (error) => {
        console.error('WebSocket error:', error)
        setError('Connection error')
        setIsConnecting(false)
      }

    } catch (err) {
      console.error('Failed to connect:', err)
      setError('Failed to connect to TikTok Live')
      setIsConnecting(false)
    }
  }, [checkIsLive, getRoomInfo, getAvailableGifts, isConnecting, isConnected, toast])

  // Disconnect from TikTok Live
  const disconnect = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
      reconnectTimeoutRef.current = null
    }

    if (wsRef.current) {
      wsRef.current.close()
      wsRef.current = null
    }

    uniqueIdRef.current = ''
    setIsConnected(false)
    setRoomInfo(null)
    setAvailableGifts([])
    setError(null)
  }, [])

  // Subscribe to events
  const onGift = useCallback((callback: (event: TikTokGiftEvent) => void) => {
    if (!wsRef.current) return

    wsRef.current.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        
        switch (data.event) {
          case 'gift':
            callback({
              user: {
                userId: data.user?.userId || '',
                userName: data.user?.uniqueId || '',
                userNickname: data.user?.nickname || '',
                userAvatar: data.user?.avatarThumb || '',
              },
              gift: {
                giftId: data.gift?.giftId || '',
                giftName: data.gift?.name || '',
                giftIcon: data.gift?.image || '',
                diamondCost: data.gift?.diamondCount || 0,
                quantity: data.repeatCount || 1,
                totalCost: (data.gift?.diamondCount || 0) * (data.repeatCount || 1),
              },
              repeatCount: data.repeatCount || 1,
              repeatEnd: data.repeatEnd || true,
            })
            break
        }
      } catch (err) {
        console.error('Failed to parse WebSocket message:', err)
      }
    }
  }, [])

  const onChat = useCallback((callback: (event: TikTokChatEvent) => void) => {
    if (!wsRef.current) return

    const originalOnMessage = wsRef.current.onmessage
    wsRef.current.onmessage = (event) => {
      originalOnMessage?.(event)
      
      try {
        const data = JSON.parse(event.data)
        
        if (data.event === 'chat') {
          callback({
            user: {
              userId: data.user?.userId || '',
              userName: data.user?.uniqueId || '',
              userNickname: data.user?.nickname || '',
              userAvatar: data.user?.avatarThumb || '',
            },
            comment: data.comment || '',
          })
        }
      } catch (err) {
        console.error('Failed to parse chat message:', err)
      }
    }
  }, [])

  const onMember = useCallback((callback: (event: TikTokMemberEvent) => void) => {
    if (!wsRef.current) return

    const originalOnMessage = wsRef.current.onmessage
    wsRef.current.onmessage = (event) => {
      originalOnMessage?.(event)
      
      try {
        const data = JSON.parse(event.data)
        
        if (data.event === 'member') {
          callback({
            user: {
              userId: data.user?.userId || '',
              userName: data.user?.uniqueId || '',
              userNickname: data.user?.nickname || '',
              userAvatar: data.user?.avatarThumb || '',
            },
          })
        }
      } catch (err) {
        console.error('Failed to parse member message:', err)
      }
    }
  }, [])

  const onLike = useCallback((callback: (event: TikTokLikeEvent) => void) => {
    if (!wsRef.current) return

    const originalOnMessage = wsRef.current.onmessage
    wsRef.current.onmessage = (event) => {
      originalOnMessage?.(event)
      
      try {
        const data = JSON.parse(event.data)
        
        if (data.event === 'like') {
          callback({
            user: {
              userId: data.user?.userId || '',
              userName: data.user?.uniqueId || '',
              userNickname: data.user?.nickname || '',
              userAvatar: data.user?.avatarThumb || '',
            },
            likeCount: data.likeCount || 1,
            totalLikeCount: data.totalLikeCount || 0,
          })
        }
      } catch (err) {
        console.error('Failed to parse like message:', err)
      }
    }
  }, [])

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      disconnect()
    }
  }, [disconnect])

  return {
    isConnected,
    isConnecting,
    roomInfo,
    availableGifts,
    error,
    connect,
    disconnect,
    onGift,
    onChat,
    onMember,
    onLike,
  }
}
