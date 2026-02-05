import { useState, useEffect, useRef, useCallback } from 'react'
import { useRoomWebSocket, type WsStateMessage, type WsEventMessage } from './useRoomWebSocket'
import { useTikTokLive } from './useTikTokLive'
import { StickerDanceEngine, AttackDefenseEngine, FreeModeEngine, type GameState, type GameEvent } from '@/services/GameEngine'
import { useStore } from '@/stores/useStore'
import { useToast } from '@/hooks/use-toast'
import type { Anchor, GameMode } from '@/types'

/**
 * Game Manager Hook
 * 
 * Integrates:
 * - Game Engine (local gameplay logic)
 * - WebSocket Producer (state broadcasting)
 * - TikTok Live Connector (gift events)
 * 
 * Flow:
 * 1. User connects to TikTok live room
 * 2. Game Manager initializes game engine
 * 3. WebSocket connects as producer
 * 4. Gift events trigger game state updates
 * 5. State is broadcast to server → widgets
 */

export interface GameManagerState {
  isInitialized: boolean
  isRunning: boolean
  gameState: GameState | null
  roomId: string | null
  error: string | null
}

export function useGameManager() {
  const { toast } = useToast()
  const { anchors, currentSession, user } = useStore()
  const { status, connect, disconnect, switchRoom, sendState, sendEvent } = useRoomWebSocket()
  const tiktok = useTikTokLive()
  const [tiktokAnchorMap, setTiktokAnchorMap] = useState<Record<string, string>>({})
  
  const [managerState, setManagerState] = useState<GameManagerState>({
    isInitialized: false,
    isRunning: false,
    gameState: null,
    roomId: null,
    error: null,
  })
  
  const engineRef = useRef<StickerDanceEngine | AttackDefenseEngine | FreeModeEngine | null>(null)
  const gameLoopRef = useRef<NodeJS.Timeout | null>(null)
  const roomIdRef = useRef<string | null>(null)

  // Initialize game engine
  const initializeGame = useCallback((mode: GameMode, config: any) => {
    if (!anchors || anchors.length === 0) {
      setManagerState(prev => ({ ...prev, error: 'No anchors configured' }))
      return false
    }

    // Create appropriate engine based on mode
    switch (mode) {
      case 'sticker':
        engineRef.current = new StickerDanceEngine(anchors, config)
        break
      case 'pk':
        // For PK mode, need to split anchors into defenders and attackers
        const mid = Math.ceil(anchors.length / 2)
        const defenders = anchors.slice(0, mid)
        const attackers = anchors.slice(mid)
        engineRef.current = new AttackDefenseEngine(defenders, attackers, config)
        break
      case 'free':
        engineRef.current = new FreeModeEngine(anchors, config)
        break
      default:
        setManagerState(prev => ({ ...prev, error: 'Unknown game mode' }))
        return false
    }

    const initialState = engineRef.current.getState()
    
    setManagerState(prev => ({
      ...prev,
      isInitialized: true,
      gameState: initialState,
      error: null,
    }))

    return true
  }, [anchors])

  // Connect to WebSocket as producer
  const connectProducer = useCallback(async (roomId: string) => {
    if (!user?.accessToken) {
      setManagerState(prev => ({ ...prev, error: 'Not authenticated' }))
      return false
    }

    roomIdRef.current = roomId
    
    // Connect WebSocket
    connect(roomId, user.accessToken, 'producer')

    // Connect TikTok live events
    await tiktok.connect(roomId)
    
    setManagerState(prev => ({
      ...prev,
      roomId,
    }))

    return true
  }, [connect, tiktok, user])

  // Switch to a different room without disconnecting WebSocket
  const switchProducerRoom = useCallback(async (roomId: string) => {
    if (!user?.accessToken) {
      toast({
        title: 'Error',
        description: 'Not authenticated',
        variant: 'destructive',
      })
      return false
    }

    // Disconnect from current TikTok room
    await tiktok.disconnect()

    roomIdRef.current = roomId

    // Switch WebSocket room without disconnecting
    switchRoom(roomId, user.accessToken, 'producer')

    // Connect to new TikTok room
    await tiktok.connect(roomId)

    setManagerState(prev => ({
      ...prev,
      roomId,
    }))

    toast({
      title: 'Room Switched',
      description: `Switched to room: ${roomId}`,
    })

    return true
  }, [switchRoom, tiktok, user, toast])

  // Start game
  const startGame = useCallback(() => {
    if (!engineRef.current) {
      toast({
        title: 'Error',
        description: 'Game not initialized',
        variant: 'destructive',
      })
      return
    }

    const state = engineRef.current.start()
    
    setManagerState(prev => ({
      ...prev,
      isRunning: true,
      gameState: state,
    }))

    // Broadcast initial state
    broadcastState(state)

    // Start game loop for countdown/decay
    gameLoopRef.current = setInterval(() => {
      if (engineRef.current) {
        const result = engineRef.current.tickCountdown()
        setManagerState(prev => ({ ...prev, gameState: result.state }))
        broadcastState(result.state)
        
        if (result.event) {
          broadcastEvent(result.event)
        }
      }
    }, 1000)

    toast({
      title: 'Game Started',
      description: 'Gameplay is now live!',
    })
  }, [toast])

  // Pause game
  const pauseGame = useCallback(() => {
    if (!engineRef.current) return

    const state = engineRef.current.pause()
    
    setManagerState(prev => ({
      ...prev,
      isRunning: false,
      gameState: state,
    }))

    // Stop game loop
    if (gameLoopRef.current) {
      clearInterval(gameLoopRef.current)
      gameLoopRef.current = null
    }

    broadcastState(state)

    toast({
      title: 'Game Paused',
    })
  }, [])

  // Stop game
  const stopGame = useCallback(() => {
    // Stop game loop
    if (gameLoopRef.current) {
      clearInterval(gameLoopRef.current)
      gameLoopRef.current = null
    }

    // Disconnect WebSocket
    disconnect()
    tiktok.disconnect()

    setManagerState({
      isInitialized: false,
      isRunning: false,
      gameState: null,
      roomId: null,
      error: null,
    })

    engineRef.current = null
    roomIdRef.current = null

    toast({
      title: 'Game Stopped',
    })
  }, [disconnect, tiktok, toast])

  // Process gift from TikTok Live
  const processGift = useCallback((anchorId: string, giftValue: number, quantity: number = 1) => {
    if (!engineRef.current || !managerState.isRunning) return

    const result = engineRef.current.processGift(anchorId, giftValue, quantity)
    
    setManagerState(prev => ({
      ...prev,
      gameState: result.state,
    }))

    // Broadcast state and event
    broadcastState(result.state)
    broadcastEvent(result.event)
  }, [managerState.isRunning])

  const ensureTikTokAnchorMap = useCallback(() => {
    if (!anchors || anchors.length === 0) return
    setTiktokAnchorMap((prev) => {
      const next = { ...prev }
      anchors.forEach((anchor) => {
        if (anchor.tiktokId && !next[anchor.tiktokId]) {
          next[anchor.tiktokId] = anchor.id
        }
      })
      return next
    })
  }, [anchors])

  // Broadcast state to server
  const broadcastState = useCallback((state: GameState) => {
    if (!status.isJoined) return

    const wsState: WsStateMessage['payload'] = {
      mode: state.mode,
      round: state.round,
      score: state.score,
      anchors: state.anchors.map(a => ({
        id: a.id,
        name: a.name,
        avatar: a.avatar,
        score: a.score,
        rank: a.rank,
        isActive: a.isActive,
      })),
      combo: state.combo,
      progress: state.progress,
      countdown: state.countdown,
      isRunning: state.isRunning,
    }

    sendState(wsState)
  }, [status.isJoined, sendState])

  // Broadcast event to server
  const broadcastEvent = useCallback((event: GameEvent) => {
    if (!status.isJoined) return

    const wsEvent: WsEventMessage['payload'] = {
      eventType: event.eventType,
      anchorId: event.anchorId,
      giftId: event.giftId ? parseInt(event.giftId) : undefined,
      user: event.userName,
      effect: event.effect,
    }

    sendEvent(wsEvent)
  }, [status.isJoined, sendEvent])

  // Get widget URL for OBS
  const getWidgetUrl = useCallback(() => {
    if (!roomIdRef.current || !user?.accessToken) return null
    
    // In production, use a proper overlay token instead of access token
    return `http://localhost:3000/index.html?roomId=${roomIdRef.current}&token=${user.accessToken}`
  }, [user])

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (gameLoopRef.current) {
        clearInterval(gameLoopRef.current)
      }
      disconnect()
      tiktok.disconnect()
    }
  }, [disconnect, tiktok])

  useEffect(() => {
    ensureTikTokAnchorMap()
  }, [ensureTikTokAnchorMap])

  useEffect(() => {
    if (!managerState.isRunning) return

    const unsubscribe = tiktok.onGift((event) => {
      const anchorId = tiktokAnchorMap[event.user.userName]
      if (!anchorId) return

      processGift(anchorId, event.gift.diamondCost, event.gift.quantity)
    })

    return () => {
      if (unsubscribe) {
        unsubscribe()
      }
    }
  }, [managerState.isRunning, processGift, tiktok, tiktokAnchorMap])

  return {
    // State
    ...managerState,
    wsStatus: status,

    // Actions
    initializeGame,
    connectProducer,
    switchProducerRoom,
    startGame,
    pauseGame,
    stopGame,
    processGift,
    getWidgetUrl,
  }
}
