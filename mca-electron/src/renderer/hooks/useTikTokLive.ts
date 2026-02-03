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
  
  const connectionRef = useRef<any | null>(null)
  const uniqueIdRef = useRef<string>('')
  const giftCallbacksRef = useRef<Array<(event: TikTokGiftEvent) => void>>([])
  const chatCallbacksRef = useRef<Array<(event: TikTokChatEvent) => void>>([])
  const memberCallbacksRef = useRef<Array<(event: TikTokMemberEvent) => void>>([])
  const likeCallbacksRef = useRef<Array<(event: TikTokLikeEvent) => void>>([])

  const getConnection = useCallback(async (uniqueId: string) => {
    if (connectionRef.current && uniqueIdRef.current === uniqueId) {
      return connectionRef.current
    }

    if (connectionRef.current) {
      try {
        connectionRef.current.disconnect()
      } catch (err) {
        console.warn('Failed to disconnect existing TikTok connection', err)
      }
      connectionRef.current = null
    }

    const { WebcastPushConnection } = await import('tiktok-live-connector')
    const connection = new WebcastPushConnection(uniqueId, {
      enableExtendedGiftInfo: true,
    })

    connection.on('gift', (data: any) => {
      const event = {
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
        repeatEnd: data.repeatEnd ?? true,
      }
      giftCallbacksRef.current.forEach((callback) => callback(event))
    })

    connection.on('chat', (data: any) => {
      const event = {
        user: {
          userId: data.user?.userId || '',
          userName: data.user?.uniqueId || '',
          userNickname: data.user?.nickname || '',
          userAvatar: data.user?.avatarThumb || '',
        },
        comment: data.comment || '',
      }
      chatCallbacksRef.current.forEach((callback) => callback(event))
    })

    connection.on('member', (data: any) => {
      const event = {
        user: {
          userId: data.user?.userId || '',
          userName: data.user?.uniqueId || '',
          userNickname: data.user?.nickname || '',
          userAvatar: data.user?.avatarThumb || '',
        },
      }
      memberCallbacksRef.current.forEach((callback) => callback(event))
    })

    connection.on('like', (data: any) => {
      const event = {
        user: {
          userId: data.user?.userId || '',
          userName: data.user?.uniqueId || '',
          userNickname: data.user?.nickname || '',
          userAvatar: data.user?.avatarThumb || '',
        },
        likeCount: data.likeCount || 1,
        totalLikeCount: data.totalLikeCount || 0,
      }
      likeCallbacksRef.current.forEach((callback) => callback(event))
    })

    connectionRef.current = connection
    uniqueIdRef.current = uniqueId
    return connection
  }, [])

  // Check if user is live
  const checkIsLive = useCallback(async (uniqueId: string): Promise<boolean> => {
    try {
      const connection = await getConnection(uniqueId)
      const result = await connection.fetchIsLive()
      return Boolean(result?.isLive)
    } catch (err) {
      console.error('Failed to check if user is live:', err)
      return false
    }
  }, [getConnection])

  // Get room info
  const getRoomInfo = useCallback(async (uniqueId: string): Promise<TikTokRoomInfo | null> => {
    try {
      const connection = await getConnection(uniqueId)
      const info = await connection.fetchRoomInfo()
      if (!info) return null
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
    } catch (err) {
      console.error('Failed to get room info:', err)
      return null
    }
  }, [getConnection])

  // Get available gifts
  const getAvailableGifts = useCallback(async (uniqueId: string): Promise<TikTokGift[]> => {
    try {
      const connection = await getConnection(uniqueId)
      const gifts = await connection.fetchAvailableGifts()
      if (Array.isArray(gifts)) {
        return gifts.map((gift: any) => ({
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
  }, [getConnection])

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

      const gifts = await getAvailableGifts(uniqueId)
      setAvailableGifts(gifts)

      const connection = await getConnection(uniqueId)
      await connection.connect()

      console.log('Connected to TikTok Live')
      setIsConnected(true)
      setIsConnecting(false)
      toast({
        title: 'Connected',
        description: `Connected to @${uniqueId}'s live stream`,
      })

    } catch (err) {
      console.error('Failed to connect:', err)
      setError('Failed to connect to TikTok Live')
      setIsConnecting(false)
    }
  }, [checkIsLive, getRoomInfo, getAvailableGifts, getConnection, isConnecting, isConnected, toast])

  // Disconnect from TikTok Live
  const disconnect = useCallback(() => {
    if (connectionRef.current) {
      try {
        connectionRef.current.disconnect()
      } catch (err) {
        console.warn('Failed to disconnect TikTok connection', err)
      }
      connectionRef.current = null
    }

    uniqueIdRef.current = ''
    setIsConnected(false)
    setRoomInfo(null)
    setAvailableGifts([])
    setError(null)
  }, [])

  // Subscribe to events
  const onGift = useCallback((callback: (event: TikTokGiftEvent) => void) => {
    giftCallbacksRef.current.push(callback)
    return () => {
      giftCallbacksRef.current = giftCallbacksRef.current.filter((cb) => cb !== callback)
    }
  }, [])

  const onChat = useCallback((callback: (event: TikTokChatEvent) => void) => {
    chatCallbacksRef.current.push(callback)
    return () => {
      chatCallbacksRef.current = chatCallbacksRef.current.filter((cb) => cb !== callback)
    }
  }, [])

  const onMember = useCallback((callback: (event: TikTokMemberEvent) => void) => {
    memberCallbacksRef.current.push(callback)
    return () => {
      memberCallbacksRef.current = memberCallbacksRef.current.filter((cb) => cb !== callback)
    }
  }, [])

  const onLike = useCallback((callback: (event: TikTokLikeEvent) => void) => {
    likeCallbacksRef.current.push(callback)
    return () => {
      likeCallbacksRef.current = likeCallbacksRef.current.filter((cb) => cb !== callback)
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
