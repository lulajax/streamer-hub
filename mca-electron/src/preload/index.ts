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
      ipcRenderer.on('monitor-closed', callback)
      return () => ipcRenderer.off('monitor-closed', callback)
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
  },

  // Dialog
  dialog: {
    showSaveDialog: (options: Electron.SaveDialogOptions) => ipcRenderer.invoke('show-save-dialog', options),
    showOpenDialog: (options: Electron.OpenDialogOptions) => ipcRenderer.invoke('show-open-dialog', options),
  },

  // Gift data capture
  onGiftRequestCaptured: (callback: (url: string) => void) => {
    ipcRenderer.on('gift-request-captured', (_, url) => callback(url))
    return () => ipcRenderer.off('gift-request-captured', callback)
  },
}

contextBridge.exposeInMainWorld('electronAPI', electronAPI)

export type ElectronAPI = typeof electronAPI
