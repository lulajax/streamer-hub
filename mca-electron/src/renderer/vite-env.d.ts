/// <reference types="vite/client" />

declare module '*.svg' {
  const content: React.FunctionComponent<React.SVGAttributes<SVGElement>>
  export default content
}

interface ImportMetaEnv {
  readonly VITE_APP_TITLE: string
  readonly VITE_API_URL: string
  readonly VITE_WS_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// Electron API types
interface DeviceInfo {
  deviceId: string
  deviceName: string
}

interface SystemInfo {
  platform: string
  arch: string
  version: string
  hostname: string
}

interface ElectronAPI {
  window: {
    minimize: () => Promise<void>
    maximize: () => Promise<void>
    close: () => Promise<void>
  }
  monitor: {
    open: (roomId: string) => Promise<{ success: boolean }>
    close: () => Promise<{ success: boolean }>
    onClosed: (callback: () => void) => () => void
  }
  widget: {
    open: (widgetUrl: string, title: string) => Promise<{ success: boolean; windowId?: number }>
  }
  app: {
    getVersion: () => Promise<string>
    getSystemInfo: () => Promise<SystemInfo>
    getDeviceInfo: () => Promise<DeviceInfo>
    setDeviceName: (deviceName: string) => Promise<{ success: boolean }>
  }
  dialog: {
    showSaveDialog: (options: import('electron').SaveDialogOptions) => Promise<import('electron').SaveDialogReturnValue>
    showOpenDialog: (options: import('electron').OpenDialogOptions) => Promise<import('electron').OpenDialogReturnValue>
  }
  onGiftRequestCaptured: (callback: (url: string) => void) => () => void
  tiktokLive: {
    fetchIsLive: (uniqueId: string) => Promise<boolean>
    getRoomInfo: (uniqueId: string) => Promise<{
      roomId: string
      title: string
      owner: {
        userId: string
        userName: string
        userNickname: string
        userAvatar: string
      }
      viewerCount: number
      likeCount: number
      isLive: boolean
    } | null>
    getAvailableGifts: (uniqueId: string) => Promise<Array<{
      giftId: string
      giftName: string
      giftIcon: string
      diamondCost: number
      quantity: number
      totalCost: number
    }>>
    connect: (uniqueId: string) => Promise<{ success: boolean }>
    disconnect: () => Promise<{ success: boolean }>
    onEvent: (callback: (event: any) => void) => () => void
  }
}

declare global {
  interface Window {
    electronAPI: ElectronAPI
  }
}
