import { useState, useEffect, useRef, useCallback } from 'react'
import { useToast } from '@/hooks/use-toast'

// WebSocket message types
export type WsRole = 'producer' | 'consumer'

export interface WsMessage {
  type: string
  roomId?: string
  seq?: number
  ts?: number
  payload?: any
  error?: string
}

export interface WsStateMessage extends WsMessage {
  type: 'state'
  payload: {
    mode: 'attack_defense' | 'sticker_dance' | 'free'
    round: number
    score: Record<string, number>
    combo?: number
    progress?: number
    anchors?: WsAnchor[]
    countdown?: number
    [key: string]: any
  }
}

export interface WsEventMessage extends WsMessage {
  type: 'event'
  payload: {
    eventType: string
    giftId?: number
    user?: string
    effect?: string
    anchorId?: string
    [key: string]: any
  }
}

export interface WsAnchor {
  id: string
  name: string
  avatar?: string
  score: number
  rank: number
  isActive: boolean
}

export interface WsConnectionStatus {
  isConnected: boolean
  isJoined: boolean
  role: WsRole | null
  roomId: string | null
  error: string | null
  lastSeq: number
}

// WebSocket configuration
const WS_URL = 'ws://localhost:8080/ws/room'
const RECONNECT_DELAYS = [1000, 2000, 4000, 8000, 16000] // Exponential backoff
const HEARTBEAT_INTERVAL = 30000 // 30 seconds

/**
 * Hook for Room WebSocket connection (Producer pattern)
 *
 * Usage:
 * const { connect, disconnect, switchRoom, sendState, sendEvent, status } = useRoomWebSocket()
 *
 * // Connect as producer
 * connect('room_123', 'jwt_token', 'producer')
 *
 * // Switch room without disconnecting
 * switchRoom('room_456', 'jwt_token', 'producer')
 *
 * // Send state
 * sendState({ mode: 'sticker_dance', round: 1, score: { a: 10, b: 5 } })
 *
 * // Send event
 * sendEvent({ eventType: 'GIFT_TRIGGER', giftId: 5655, user: 'tom' })
 */
export function useRoomWebSocket() {
  const { toast } = useToast()
  
  const [status, setStatus] = useState<WsConnectionStatus>({
    isConnected: false,
    isJoined: false,
    role: null,
    roomId: null,
    error: null,
    lastSeq: 0,
  })
  
  const wsRef = useRef<WebSocket | null>(null)
  const reconnectAttemptRef = useRef(0)
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null)
  const heartbeatIntervalRef = useRef<NodeJS.Timeout | null>(null)
  const pendingRoomIdRef = useRef<string | null>(null)
  const pendingTokenRef = useRef<string | null>(null)
  const pendingRoleRef = useRef<WsRole | null>(null)
  const seqRef = useRef(0)

  // Clear all timers
  const clearTimers = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current)
      reconnectTimeoutRef.current = null
    }
    if (heartbeatIntervalRef.current) {
      clearInterval(heartbeatIntervalRef.current)
      heartbeatIntervalRef.current = null
    }
  }, [])

  // Send message helper
  const sendMessage = useCallback((message: object) => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      wsRef.current.send(JSON.stringify(message))
      return true
    }
    return false
  }, [])

  // Start heartbeat
  const startHeartbeat = useCallback(() => {
    heartbeatIntervalRef.current = setInterval(() => {
      sendMessage({
        type: 'heartbeat',
        ts: Date.now(),
      })
    }, HEARTBEAT_INTERVAL)
  }, [sendMessage])

  // Handle WebSocket open
  const handleOpen = useCallback(() => {
    console.log('[WebSocket] Connected')
    
    setStatus(prev => ({
      ...prev,
      isConnected: true,
      error: null,
    }))
    
    reconnectAttemptRef.current = 0
    
    // Send join message
    if (pendingRoomIdRef.current && pendingTokenRef.current && pendingRoleRef.current) {
      sendMessage({
        type: 'join',
        role: pendingRoleRef.current,
        roomId: pendingRoomIdRef.current,
        token: pendingTokenRef.current,
      })
    }
    
    startHeartbeat()
  }, [sendMessage, startHeartbeat])

  // Handle WebSocket message
  const handleMessage = useCallback((event: MessageEvent) => {
    try {
      const message: WsMessage = JSON.parse(event.data)
      
      switch (message.type) {
        case 'join_success':
          console.log('[WebSocket] Join success:', message)
          setStatus(prev => ({
            ...prev,
            isJoined: true,
            roomId: message.roomId || pendingRoomIdRef.current,
          }))
          toast({
            title: 'Connected',
            description: `Connected to room: ${message.roomId}`,
          })
          break
          
        case 'error':
          console.error('[WebSocket] Server error:', message.error)
          setStatus(prev => ({
            ...prev,
            error: message.error || 'Unknown error',
          }))
          toast({
            title: 'Connection Error',
            description: message.error,
            variant: 'destructive',
          })
          break
          
        case 'heartbeat':
          // Heartbeat response, ignore
          break
          
        case 'waiting':
          console.log('[WebSocket] Waiting for producer')
          break
          
        default:
          console.log('[WebSocket] Received:', message)
      }
    } catch (err) {
      console.error('[WebSocket] Failed to parse message:', err)
    }
  }, [toast])

  // Handle WebSocket close
  const handleClose = useCallback((event: CloseEvent) => {
    console.log('[WebSocket] Disconnected:', event.code, event.reason)
    
    clearTimers()
    
    setStatus(prev => ({
      ...prev,
      isConnected: false,
      isJoined: false,
    }))
    
    // Attempt reconnection if not manually closed
    if (event.code !== 1000 && event.code !== 1001) {
      const delay = RECONNECT_DELAYS[Math.min(reconnectAttemptRef.current, RECONNECT_DELAYS.length - 1)]
      reconnectAttemptRef.current++
      
      console.log(`[WebSocket] Reconnecting in ${delay}ms (attempt ${reconnectAttemptRef.current})`)
      
      reconnectTimeoutRef.current = setTimeout(() => {
        if (pendingRoomIdRef.current && pendingTokenRef.current && pendingRoleRef.current) {
          connect(pendingRoomIdRef.current, pendingTokenRef.current, pendingRoleRef.current)
        }
      }, delay)
    }
  }, [clearTimers])

  // Handle WebSocket error
  const handleError = useCallback((error: Event) => {
    console.error('[WebSocket] Error:', error)
    setStatus(prev => ({
      ...prev,
      error: 'WebSocket connection error',
    }))
  }, [])

  // Connect to WebSocket
  const connect = useCallback((roomId: string, token: string, role: WsRole) => {
    // Store connection params for reconnection
    pendingRoomIdRef.current = roomId
    pendingTokenRef.current = token
    pendingRoleRef.current = role
    
    // Close existing connection
    if (wsRef.current) {
      wsRef.current.close(1000, 'Reconnecting')
    }
    
    clearTimers()
    
    setStatus(prev => ({
      ...prev,
      role,
      roomId,
      error: null,
    }))
    
    try {
      wsRef.current = new WebSocket(WS_URL)
      
      wsRef.current.onopen = handleOpen
      wsRef.current.onmessage = handleMessage
      wsRef.current.onclose = handleClose
      wsRef.current.onerror = handleError
      
    } catch (err) {
      console.error('[WebSocket] Failed to create connection:', err)
      setStatus(prev => ({
        ...prev,
        error: 'Failed to create WebSocket connection',
      }))
    }
  }, [handleOpen, handleMessage, handleClose, handleError, clearTimers])

  // Switch room without disconnecting WebSocket
  const switchRoom = useCallback((roomId: string, token: string, role: WsRole) => {
    console.log('[WebSocket] Switching room to:', roomId)

    // Store new connection params for reconnection
    pendingRoomIdRef.current = roomId
    pendingTokenRef.current = token
    pendingRoleRef.current = role
    seqRef.current = 0

    // Reset joined state
    setStatus(prev => ({
      ...prev,
      isJoined: false,
      roomId,
      role,
      error: null,
      lastSeq: 0,
    }))

    // If already connected, send join message directly
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      sendMessage({
        type: 'join',
        role,
        roomId,
        token,
      })
    } else {
      // If not connected, fall back to connect behavior
      connect(roomId, token, role)
    }
  }, [sendMessage, connect])

  // Disconnect from WebSocket
  const disconnect = useCallback(() => {
    console.log('[WebSocket] Disconnecting...')
    
    clearTimers()
    
    pendingRoomIdRef.current = null
    pendingTokenRef.current = null
    pendingRoleRef.current = null
    reconnectAttemptRef.current = 0
    seqRef.current = 0
    
    if (wsRef.current) {
      wsRef.current.close(1000, 'Manual disconnect')
      wsRef.current = null
    }
    
    setStatus({
      isConnected: false,
      isJoined: false,
      role: null,
      roomId: null,
      error: null,
      lastSeq: 0,
    })
  }, [clearTimers])

  // Send state message (producer only)
  const sendState = useCallback((payload: WsStateMessage['payload']) => {
    if (pendingRoleRef.current !== 'producer') {
      console.warn('[WebSocket] Only producer can send state')
      return false
    }
    
    seqRef.current++
    
    return sendMessage({
      type: 'state',
      roomId: pendingRoomIdRef.current,
      seq: seqRef.current,
      ts: Date.now(),
      payload,
    })
  }, [sendMessage])

  // Send event message (producer only)
  const sendEvent = useCallback((payload: WsEventMessage['payload']) => {
    if (pendingRoleRef.current !== 'producer') {
      console.warn('[WebSocket] Only producer can send event')
      return false
    }
    
    seqRef.current++
    
    return sendMessage({
      type: 'event',
      roomId: pendingRoomIdRef.current,
      seq: seqRef.current,
      ts: Date.now(),
      payload,
    })
  }, [sendMessage])

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      disconnect()
    }
  }, [disconnect])

  return {
    // State
    status,

    // Actions
    connect,
    disconnect,
    switchRoom,
    sendState,
    sendEvent,
  }
}
