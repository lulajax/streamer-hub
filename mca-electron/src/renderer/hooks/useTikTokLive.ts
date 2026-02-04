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

interface TikTokLiveBridge {
  fetchIsLive: (uniqueId: string) => Promise<boolean>
  getRoomInfo: (uniqueId: string) => Promise<TikTokRoomInfo | null>
  getAvailableGifts: (uniqueId: string) => Promise<TikTokGift[]>
  connect: (uniqueId: string) => Promise<{ success: boolean }>
  disconnect: () => Promise<{ success: boolean }>
  onEvent: (callback: (event: { type: string; payload?: any }) => void) => () => void
}

declare global {
  interface Window {
    electronAPI?: {
      tiktokLive?: TikTokLiveBridge
    }
  }
}

// TikTok Live Connector Hook
export function useTikTokLive() {
  const { toast } = useToast()
  
  const [isConnected, setIsConnected] = useState(false)
  const [isConnecting, setIsConnecting] = useState(false)
  const [roomInfo, setRoomInfo] = useState<TikTokRoomInfo | null>(null)
  const [availableGifts, setAvailableGifts] = useState<TikTokGift[]>([])
  const [error, setError] = useState<string | null>(null)
  
  const uniqueIdRef = useRef<string>('')
  const giftCallbacksRef = useRef<Array<(event: TikTokGiftEvent) => void>>([])
  const chatCallbacksRef = useRef<Array<(event: TikTokChatEvent) => void>>([])
  const memberCallbacksRef = useRef<Array<(event: TikTokMemberEvent) => void>>([])
  const likeCallbacksRef = useRef<Array<(event: TikTokLikeEvent) => void>>([])

  const getBridge = useCallback(() => window.electronAPI?.tiktokLive, [])

  // Check if user is live
  const checkIsLive = useCallback(async (uniqueId: string): Promise<boolean> => {
    try {
      const bridge = getBridge()
      if (!bridge) throw new Error('TikTok Live bridge is not available')
      return await bridge.fetchIsLive(uniqueId)
    } catch (err) {
      console.error('Failed to check if user is live:', err)
      return false
    }
  }, [getBridge])

  // Get room info
  const getRoomInfo = useCallback(async (uniqueId: string): Promise<TikTokRoomInfo | null> => {
    try {
      const bridge = getBridge()
      if (!bridge) throw new Error('TikTok Live bridge is not available')
      return await bridge.getRoomInfo(uniqueId)
    } catch (err) {
      console.error('Failed to get room info:', err)
      return null
    }
  }, [getBridge])

  // Get available gifts
  const getAvailableGifts = useCallback(async (uniqueId: string): Promise<TikTokGift[]> => {
    try {
      const bridge = getBridge()
      if (!bridge) throw new Error('TikTok Live bridge is not available')
      return await bridge.getAvailableGifts(uniqueId)
    } catch (err) {
      console.error('Failed to get available gifts:', err)
      return []
    }
  }, [getBridge])

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

      const bridge = getBridge()
      if (!bridge) {
        throw new Error('TikTok Live bridge is not available')
      }
      await bridge.connect(uniqueId)

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
  }, [checkIsLive, getRoomInfo, getAvailableGifts, getBridge, isConnecting, isConnected, toast])

  // Disconnect from TikTok Live
  const disconnect = useCallback(() => {
    const bridge = getBridge()
    if (bridge) {
      bridge.disconnect().catch((err) => {
        console.warn('Failed to disconnect TikTok connection', err)
      })
    }

    uniqueIdRef.current = ''
    setIsConnected(false)
    setRoomInfo(null)
    setAvailableGifts([])
    setError(null)
  }, [getBridge])

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
    const bridge = getBridge()
    const unsubscribe = bridge?.onEvent((event) => {
      switch (event.type) {
        case 'gift':
          giftCallbacksRef.current.forEach((callback) => callback(event.payload))
          break
        case 'chat':
          chatCallbacksRef.current.forEach((callback) => callback(event.payload))
          break
        case 'member':
          memberCallbacksRef.current.forEach((callback) => callback(event.payload))
          break
        case 'like':
          likeCallbacksRef.current.forEach((callback) => callback(event.payload))
          break
        case 'connected':
          setIsConnected(true)
          break
        case 'disconnected':
          setIsConnected(false)
          break
        case 'error':
          setError(event.payload?.message || 'TikTok Live error')
          break
        default:
          break
      }
    })

    return () => {
      unsubscribe?.()
      disconnect()
    }
  }, [disconnect, getBridge])

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
