export interface TikTokLiveConnectResult {
  success: boolean
  error?: string
}

export async function connectTikTokLiveForRoom(uniqueId: string): Promise<TikTokLiveConnectResult> {
  const bridge = window.electronAPI?.tiktokLive
  if (!bridge) {
    return { success: false, error: 'TikTok Live bridge is not available' }
  }

  try {
    console.info('[TikTokLive] connect requested', { uniqueId })
    // Always reset the connection so we connect to the latest room.
    try {
      await bridge.disconnect()
      console.info('[TikTokLive] disconnected existing connection')
    } catch {
      // Ignore disconnect errors to keep connect flow resilient.
    }

    let isLive: boolean | null = null
    try {
      isLive = await bridge.fetchIsLive(uniqueId)
      console.info('[TikTokLive] fetchIsLive result', { uniqueId, isLive })
    } catch (err) {
      console.warn('[TikTokLive] fetchIsLive failed, will attempt connect anyway', err)
    }

    await bridge.connect(uniqueId)
    console.info('[TikTokLive] connect success', { uniqueId })
    return { success: true }
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Failed to connect to TikTok Live'
    console.error('[TikTokLive] connect failed', { uniqueId, message })
    if (message.toLowerCase().includes('not currently live')) {
      return { success: false, error: 'User is not currently live' }
    }
    return { success: false, error: message }
  }
}

export async function disconnectTikTokLive(): Promise<void> {
  const bridge = window.electronAPI?.tiktokLive
  if (!bridge) return
  try {
    await bridge.disconnect()
    console.info('[TikTokLive] disconnected')
  } catch {
    // Ignore disconnect errors.
  }
}
