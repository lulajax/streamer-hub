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

async function getTikTokConnection(uniqueId: string) {
  if (tiktokConnection && tiktokUniqueId === uniqueId) {
    return tiktokConnection
  }

  if (tiktokConnection) {
    try {
      tiktokConnection.disconnect()
    } catch (err) {
      console.warn('Failed to disconnect existing TikTok connection', err)
    }
    tiktokConnection = null
  }

  const { WebcastPushConnection } = await import('tiktok-live-connector')
  const connection = new WebcastPushConnection(uniqueId, {
    enableExtendedGiftInfo: true,
  })

  connection.on('gift', (data: any) => {
    sendTikTokEvent({
      type: 'gift',
      payload: {
        user: mapTikTokUser(data),
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
      },
    })
  })

  connection.on('chat', (data: any) => {
    sendTikTokEvent({
      type: 'chat',
      payload: {
        user: mapTikTokUser(data),
        comment: data.comment || '',
      },
    })
  })

  connection.on('member', (data: any) => {
    sendTikTokEvent({
      type: 'member',
      payload: {
        user: mapTikTokUser(data),
      },
    })
  })

  connection.on('like', (data: any) => {
    sendTikTokEvent({
      type: 'like',
      payload: {
        user: mapTikTokUser(data),
        likeCount: data.likeCount || 1,
        totalLikeCount: data.totalLikeCount || 0,
      },
    })
  })

  connection.on('connected', (state: any) => {
    sendTikTokEvent({ type: 'connected', payload: { roomId: state?.roomId } })
  })

  connection.on('disconnected', () => {
    sendTikTokEvent({ type: 'disconnected' })
  })

  connection.on('error', (err: Error) => {
    sendTikTokEvent({ type: 'error', payload: { message: err.message } })
  })

  tiktokConnection = connection
  tiktokUniqueId = uniqueId

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
  const connection = await getTikTokConnection(uniqueId)
  const result = await connection.fetchIsLive()
  return Boolean(result?.isLive)
})

ipcMain.handle('tiktok-live-get-room-info', async (_, uniqueId: string) => {
  const connection = await getTikTokConnection(uniqueId)
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
})

ipcMain.handle('tiktok-live-get-available-gifts', async (_, uniqueId: string) => {
  const connection = await getTikTokConnection(uniqueId)
  const gifts = await connection.fetchAvailableGifts()
  if (!Array.isArray(gifts)) return []
  return gifts.map((gift: any) => ({
    giftId: gift.giftId || '',
    giftName: gift.name || '',
    giftIcon: gift.image || '',
    diamondCost: gift.diamondCount || 0,
    quantity: 1,
    totalCost: gift.diamondCount || 0,
  }))
})

ipcMain.handle('tiktok-live-connect', async (_, uniqueId: string) => {
  const connection = await getTikTokConnection(uniqueId)
  await connection.connect()
  return { success: true }
})

ipcMain.handle('tiktok-live-disconnect', () => {
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
