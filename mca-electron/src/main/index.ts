import { app, BrowserWindow, ipcMain, shell, session, dialog } from 'electron'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import os from 'node:os'
import fs from 'node:fs'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

process.env.APP_ROOT = path.join(__dirname, '../..')

export const MAIN_DIST = path.join(process.env.APP_ROOT, 'dist/main')
export const RENDERER_DIST = path.join(process.env.APP_ROOT, 'dist/renderer')
export const VITE_DEV_SERVER_URL = process.env.VITE_DEV_SERVER_URL

process.env.VITE_PUBLIC = VITE_DEV_SERVER_URL
  ? path.join(process.env.APP_ROOT, 'public')
  : RENDERER_DIST

let mainWindow: BrowserWindow | null = null
let monitorWindow: BrowserWindow | null = null
let tiktokConnection: any | null = null
let tiktokUniqueId = ''
let tiktokIsConnected = false
let tiktokEventCount = 0

function logTikTok(message: string, data?: Record<string, unknown>) {
  if (data) {
    console.log(`[TikTokLive] ${message}`, data)
  } else {
    console.log(`[TikTokLive] ${message}`)
  }
}

// Device info storage
const DEVICE_INFO_FILE = path.join(app.getPath('userData'), 'device-info.json')

interface DeviceInfoData {
  deviceId: string
  deviceName: string
}

function readDeviceInfo(): DeviceInfoData | null {
  try {
    if (fs.existsSync(DEVICE_INFO_FILE)) {
      const data = fs.readFileSync(DEVICE_INFO_FILE, 'utf-8')
      return JSON.parse(data) as DeviceInfoData
    }
  } catch (err) {
    console.error('Failed to read device info:', err)
  }
  return null
}

function writeDeviceInfo(deviceInfo: DeviceInfoData): void {
  try {
    fs.writeFileSync(DEVICE_INFO_FILE, JSON.stringify(deviceInfo, null, 2), 'utf-8')
  } catch (err) {
    console.error('Failed to write device info:', err)
  }
}

function generateDeviceId(): string {
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
}

function generateDeviceName(): string {
  const platform = process.platform
  const hostname = os.hostname()
  return `MCA-${platform}-${hostname}`
}

function getOrCreateDeviceInfo(): DeviceInfoData {
  let deviceInfo = readDeviceInfo()
  if (!deviceInfo) {
    deviceInfo = {
      deviceId: generateDeviceId(),
      deviceName: generateDeviceName()
    }
    writeDeviceInfo(deviceInfo)
  }
  return deviceInfo
}

type TikTokLiveEvent =
  | { type: 'gift'; payload: any }
  | { type: 'chat'; payload: any }
  | { type: 'member'; payload: any }
  | { type: 'like'; payload: any }
  | { type: 'connected'; payload: { roomId?: string } }
  | { type: 'disconnected' }
  | { type: 'error'; payload: { message: string } }

function sendTikTokEvent(event: TikTokLiveEvent): void {
  mainWindow?.webContents.send('tiktok-live-event', event)
}

function disconnectTikTok(): void {
  if (tiktokConnection) {
    try {
      tiktokConnection.disconnect()
    } catch (err) {
      console.warn('Failed to disconnect TikTok connection', err)
    }
    tiktokConnection = null
    tiktokUniqueId = ''
    tiktokIsConnected = false
    tiktokEventCount = 0
    logTikTok('Disconnected')
  }
}

function mapTikTokUser(data: any) {
  return {
    userId: data.user?.userId || '',
    userName: data.user?.uniqueId || '',
    userNickname: data.user?.nickname || '',
    userAvatar: data.user?.avatarThumb || '',
  }
}

async function getRoomInfoFromConnection(connection: any) {
  if (!connection) return null
  if (typeof connection.getRoomInfo === 'function') {
    const info = await connection.getRoomInfo()
    logTikTok('Room info via getRoomInfo', { roomId: info?.roomId || '' })
    return info
  }
  if (typeof connection.fetchRoomInfo === 'function') {
    const info = await connection.fetchRoomInfo()
    logTikTok('Room info via fetchRoomInfo', { roomId: info?.roomId || '' })
    return info
  }
  return null
}

async function getAvailableGiftsFromConnection(connection: any) {
  if (!connection) return []
  if (typeof connection.getAvailableGifts === 'function') {
    const gifts = await connection.getAvailableGifts()
    logTikTok('Available gifts via getAvailableGifts', { count: Array.isArray(gifts) ? gifts.length : 0 })
    return gifts
  }
  if (typeof connection.fetchAvailableGifts === 'function') {
    const gifts = await connection.fetchAvailableGifts()
    logTikTok('Available gifts via fetchAvailableGifts', { count: Array.isArray(gifts) ? gifts.length : 0 })
    return gifts
  }
  return []
}

async function getTikTokConnection(uniqueId: string) {
  if (tiktokConnection && tiktokUniqueId === uniqueId) {
    logTikTok('Reuse connection', { uniqueId })
    return tiktokConnection
  }

  if (tiktokConnection) {
    try {
      tiktokConnection.disconnect()
    } catch (err) {
      console.warn('Failed to disconnect existing TikTok connection', err)
    }
    tiktokConnection = null
    tiktokIsConnected = false
    tiktokEventCount = 0
  }

  const tiktokModule: any = await import('tiktok-live-connector')
  const TikTokLiveConnection =
    tiktokModule.TikTokLiveConnection ?? tiktokModule.WebcastPushConnection ?? tiktokModule.default
  const WebcastEvent = tiktokModule.WebcastEvent ?? {
    GIFT: 'gift',
    CHAT: 'chat',
    MEMBER: 'member',
    LIKE: 'like',
  }
  const ControlEvent = tiktokModule.ControlEvent ?? {
    CONNECTED: 'connected',
    DISCONNECTED: 'disconnected',
    ERROR: 'error',
  }

  logTikTok('Create connection', { uniqueId })
  const connection = new TikTokLiveConnection(uniqueId, {
    enableExtendedGiftInfo: true,
    fetchRoomInfoOnConnect: true,
  })

  connection.on(WebcastEvent.GIFT, (data: any) => {
    const giftInfo = data.gift ?? data.giftInfo ?? {}
    const diamondCost = giftInfo.diamondCost ?? giftInfo.diamondCount ?? 0
    const quantity = data.repeatCount ?? data.quantity ?? 1
    tiktokEventCount += 1
    logTikTok('Gift event', {
      uniqueId,
      giftId: giftInfo.giftId || data.giftId || '',
      user: data.user?.uniqueId || '',
      totalCost: diamondCost * quantity,
      count: tiktokEventCount,
    })
    sendTikTokEvent({
      type: 'gift',
      payload: {
        user: mapTikTokUser(data),
        gift: {
          giftId: giftInfo.giftId || data.giftId || '',
          giftName: giftInfo.name || data.giftName || '',
          giftIcon: giftInfo.image || '',
          diamondCost,
          quantity,
          totalCost: diamondCost * quantity,
        },
        repeatCount: data.repeatCount || quantity,
        repeatEnd: data.repeatEnd ?? true,
      },
    })
  })

  connection.on(WebcastEvent.CHAT, (data: any) => {
    tiktokEventCount += 1
    logTikTok('Chat event', {
      uniqueId,
      user: data.user?.uniqueId || '',
      comment: data.comment ? String(data.comment).slice(0, 80) : '',
      count: tiktokEventCount,
    })
    sendTikTokEvent({
      type: 'chat',
      payload: {
        user: mapTikTokUser(data),
        comment: data.comment || '',
      },
    })
  })

  connection.on(WebcastEvent.MEMBER, (data: any) => {
    tiktokEventCount += 1
    logTikTok('Member event', {
      uniqueId,
      user: data.user?.uniqueId || '',
      count: tiktokEventCount,
    })
    sendTikTokEvent({
      type: 'member',
      payload: {
        user: mapTikTokUser(data),
      },
    })
  })

  connection.on(WebcastEvent.LIKE, (data: any) => {
    tiktokEventCount += 1
    logTikTok('Like event', {
      uniqueId,
      user: data.user?.uniqueId || '',
      likeCount: data.likeCount || 1,
      count: tiktokEventCount,
    })
    sendTikTokEvent({
      type: 'like',
      payload: {
        user: mapTikTokUser(data),
        likeCount: data.likeCount || 1,
        totalLikeCount: data.totalLikeCount || 0,
      },
    })
  })

  connection.on(ControlEvent.CONNECTED, (state: any) => {
    if (!tiktokIsConnected) {
      tiktokIsConnected = true
      logTikTok('Connected', { uniqueId, roomId: state?.roomId })
      sendTikTokEvent({ type: 'connected', payload: { roomId: state?.roomId } })
    }
  })

  connection.on(ControlEvent.DISCONNECTED, () => {
    tiktokIsConnected = false
    logTikTok('Disconnected event', { uniqueId })
    sendTikTokEvent({ type: 'disconnected' })
  })

  connection.on(ControlEvent.ERROR, (err: any) => {
    const message = err instanceof Error ? err.message : err?.message || 'TikTok Live error'
    logTikTok('Error event', { uniqueId, message })
    sendTikTokEvent({ type: 'error', payload: { message } })
  })

  tiktokConnection = connection
  tiktokUniqueId = uniqueId
  tiktokIsConnected = false

  return connection
}

function createMainWindow(): void {
  mainWindow = new BrowserWindow({
    title: 'MCA - Multi-Caster Assistant',
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    autoHideMenuBar: true,
    icon: path.join(process.env.VITE_PUBLIC, 'mca-icon.png'),
    webPreferences: {
      preload: path.join(MAIN_DIST, '../preload/index.js'),
      contextIsolation: true,
      nodeIntegration: false,
    },
    titleBarStyle: 'default',
    show: false,
  })

  if (VITE_DEV_SERVER_URL) {
    mainWindow.loadURL(VITE_DEV_SERVER_URL)
    mainWindow.webContents.openDevTools({ mode: 'detach' })
  } else {
    mainWindow.loadFile(path.join(RENDERER_DIST, 'index.html'))
  }

  mainWindow.once('ready-to-show', () => {
    mainWindow?.show()
  })

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    if (url.startsWith('https:')) shell.openExternal(url)
    return { action: 'deny' }
  })
}

function createMonitorWindow(roomUrl: string): void {
  if (monitorWindow && !monitorWindow.isDestroyed()) {
    monitorWindow.focus()
    return
  }

  monitorWindow = new BrowserWindow({
    title: 'MCA - Live Monitor',
    width: 400,
    height: 700,
    minWidth: 350,
    minHeight: 500,
    parent: mainWindow || undefined,
    icon: path.join(process.env.VITE_PUBLIC, 'mca-icon.png'),
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      webSecurity: false,
    },
    show: false,
  })

  monitorWindow.loadURL(roomUrl)

  monitorWindow.once('ready-to-show', () => {
    monitorWindow?.show()
  })

  monitorWindow.on('closed', () => {
    monitorWindow = null
    mainWindow?.webContents.send('monitor-closed')
  })

  // Intercept network requests to capture gift data
  monitorWindow.webContents.session.webRequest.onBeforeRequest(
    { urls: ['*://*.tiktok.com/*', '*://*.tiktokcdn.com/*'] },
    (details, callback) => {
      if (details.url.includes('gift') || details.url.includes('webcast')) {
        mainWindow?.webContents.send('gift-request-captured', details.url)
      }
      callback({})
    }
  )
}

function createWidgetWindow(widgetUrl: string, title: string): BrowserWindow {
  const widgetWindow = new BrowserWindow({
    title: `MCA Widget - ${title}`,
    width: 800,
    height: 600,
    minWidth: 400,
    minHeight: 300,
    frame: false,
    transparent: true,
    alwaysOnTop: true,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
    },
    show: false,
  })

  widgetWindow.loadURL(widgetUrl)

  widgetWindow.once('ready-to-show', () => {
    widgetWindow.show()
  })

  return widgetWindow
}

// IPC Handlers
ipcMain.handle('window-minimize', () => {
  mainWindow?.minimize()
})

ipcMain.handle('window-maximize', () => {
  if (mainWindow?.isMaximized()) {
    mainWindow.unmaximize()
  } else {
    mainWindow?.maximize()
  }
})

ipcMain.handle('window-close', () => {
  mainWindow?.close()
})

ipcMain.handle('open-monitor', (_, roomId: string) => {
  const roomUrl = `https://www.tiktok.com/@${roomId}/live`
  createMonitorWindow(roomUrl)
  return { success: true }
})

ipcMain.handle('close-monitor', () => {
  if (monitorWindow && !monitorWindow.isDestroyed()) {
    monitorWindow.close()
    monitorWindow = null
  }
  return { success: true }
})

ipcMain.handle('open-widget', (_, widgetUrl: string, title: string) => {
  const widgetWin = createWidgetWindow(widgetUrl, title)
  return { success: true, windowId: widgetWin.id }
})

ipcMain.handle('get-app-version', () => {
  return app.getVersion()
})

ipcMain.handle('get-system-info', () => {
  return {
    platform: process.platform,
    arch: process.arch,
    version: os.release(),
    hostname: os.hostname(),
  }
})

ipcMain.handle('get-device-info', () => {
  return getOrCreateDeviceInfo()
})

ipcMain.handle('set-device-name', (_, deviceName: string) => {
  const deviceInfo = getOrCreateDeviceInfo()
  deviceInfo.deviceName = deviceName
  writeDeviceInfo(deviceInfo)
  return { success: true }
})

ipcMain.handle('show-save-dialog', async (_, options) => {
  if (!mainWindow) return { canceled: true }
  const result = await dialog.showSaveDialog(mainWindow, options)
  return result
})

ipcMain.handle('show-open-dialog', async (_, options) => {
  if (!mainWindow) return { canceled: true }
  const result = await dialog.showOpenDialog(mainWindow, options)
  return result
})

ipcMain.handle('tiktok-live-fetch-is-live', async (_, uniqueId: string) => {
  try {
    logTikTok('fetchIsLive', { uniqueId })
    const connection = await getTikTokConnection(uniqueId)
    const info = await getRoomInfoFromConnection(connection)
    if (!info) return false
    if (typeof info.isLive === 'boolean') return info.isLive
    return true
  } catch {
    logTikTok('fetchIsLive failed', { uniqueId })
    return false
  }
})

ipcMain.handle('tiktok-live-get-room-info', async (_, uniqueId: string) => {
  logTikTok('getRoomInfo', { uniqueId })
  const connection = await getTikTokConnection(uniqueId)
  const info = await getRoomInfoFromConnection(connection)
  if (!info) return null
  const owner = info.owner || info.host || info.user || {}
  const viewerCount = info.viewerCount ?? info.stats?.viewerCount ?? 0
  const likeCount = info.likeCount ?? info.stats?.likeCount ?? 0
  const isLive = typeof info.isLive === 'boolean' ? info.isLive : true
  return {
    roomId: info.roomId || '',
    title: info.title || '',
    owner: {
      userId: owner.userId || '',
      userName: owner.uniqueId || '',
      userNickname: owner.nickname || '',
      userAvatar: owner.avatarThumb || owner.avatar || '',
    },
    viewerCount,
    likeCount,
    isLive,
  }
})

ipcMain.handle('tiktok-live-get-available-gifts', async (_, uniqueId: string) => {
  logTikTok('getAvailableGifts', { uniqueId })
  const connection = await getTikTokConnection(uniqueId)
  const gifts = await getAvailableGiftsFromConnection(connection)
  if (!Array.isArray(gifts)) return []
  return gifts.map((gift: any) => ({
    giftId: gift.giftId || gift.id || '',
    giftName: gift.name || gift.giftName || '',
    giftIcon: gift.image || gift.giftIcon || '',
    diamondCost: gift.diamondCost ?? gift.diamondCount ?? 0,
    quantity: 1,
    totalCost: gift.diamondCost ?? gift.diamondCount ?? 0,
  }))
})

ipcMain.handle('tiktok-live-connect', async (_, uniqueId: string) => {
  logTikTok('connect', { uniqueId })
  const connection = await getTikTokConnection(uniqueId)
  const state = await connection.connect()
  const roomId = state?.roomId || (await getRoomInfoFromConnection(connection))?.roomId
  if (roomId && !tiktokIsConnected) {
    tiktokIsConnected = true
    sendTikTokEvent({ type: 'connected', payload: { roomId } })
  }
  return { success: true }
})

ipcMain.handle('tiktok-live-disconnect', () => {
  logTikTok('disconnect')
  disconnectTikTok()
  return { success: true }
})

// App lifecycle
app.whenReady().then(() => {
  createMainWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createMainWindow()
    }
  })
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('before-quit', () => {
  disconnectTikTok()
  if (monitorWindow && !monitorWindow.isDestroyed()) {
    monitorWindow.close()
  }
})

// Security: Prevent new window creation
app.on('web-contents-created', (_, contents) => {
  contents.on('new-window', (event, navigationUrl) => {
    event.preventDefault()
    shell.openExternal(navigationUrl)
  })
})
