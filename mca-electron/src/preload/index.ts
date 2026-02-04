import { ipcRenderer, contextBridge } from 'electron'

// API exposed to renderer process
const electronAPI = {
  // Window controls
  window: {
    minimize: () => ipcRenderer.invoke('window-minimize'),
    maximize: () => ipcRenderer.invoke('window-maximize'),
    close: () => ipcRenderer.invoke('window-close'),
  },

  // Monitor window
  monitor: {
    open: (roomId: string) => ipcRenderer.invoke('open-monitor', roomId),
    close: () => ipcRenderer.invoke('close-monitor'),
    onClosed: (callback: () => void) => {
      const handler = (_: Electron.IpcRendererEvent) => callback()
      ipcRenderer.on('monitor-closed', handler)
      return () => ipcRenderer.off('monitor-closed', handler)
    },
  },

  // Widget window
  widget: {
    open: (widgetUrl: string, title: string) => ipcRenderer.invoke('open-widget', widgetUrl, title),
  },

  // App info
  app: {
    getVersion: () => ipcRenderer.invoke('get-app-version'),
    getSystemInfo: () => ipcRenderer.invoke('get-system-info'),
    getDeviceInfo: () => ipcRenderer.invoke('get-device-info'),
    setDeviceName: (deviceName: string) => ipcRenderer.invoke('set-device-name', deviceName),
  },

  // Dialog
  dialog: {
    showSaveDialog: (options: Electron.SaveDialogOptions) => ipcRenderer.invoke('show-save-dialog', options),
    showOpenDialog: (options: Electron.OpenDialogOptions) => ipcRenderer.invoke('show-open-dialog', options),
  },

  // Gift data capture
  onGiftRequestCaptured: (callback: (url: string) => void) => {
    const handler = (_: Electron.IpcRendererEvent, url: string) => callback(url)
    ipcRenderer.on('gift-request-captured', handler)
    return () => ipcRenderer.off('gift-request-captured', handler)
  },

  // TikTok Live
  tiktokLive: {
    fetchIsLive: (uniqueId: string) => ipcRenderer.invoke('tiktok-live-fetch-is-live', uniqueId),
    getRoomInfo: (uniqueId: string) => ipcRenderer.invoke('tiktok-live-get-room-info', uniqueId),
    getAvailableGifts: (uniqueId: string) =>
      ipcRenderer.invoke('tiktok-live-get-available-gifts', uniqueId),
    connect: (uniqueId: string) => ipcRenderer.invoke('tiktok-live-connect', uniqueId),
    disconnect: () => ipcRenderer.invoke('tiktok-live-disconnect'),
    onEvent: (callback: (event: any) => void) => {
      const handler = (_: Electron.IpcRendererEvent, event: any) => callback(event)
      ipcRenderer.on('tiktok-live-event', handler)
      return () => ipcRenderer.off('tiktok-live-event', handler)
    },
  },
}

contextBridge.exposeInMainWorld('electronAPI', electronAPI)

export type ElectronAPI = typeof electronAPI
