import { app, BrowserWindow, ipcMain, shell, session, dialog } from 'electron'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import os from 'node:os'

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

function createMainWindow(): void {
  mainWindow = new BrowserWindow({
    title: 'MCA - Multi-Caster Assistant',
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
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
    mainWindow.webContents.openDevTools()
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
