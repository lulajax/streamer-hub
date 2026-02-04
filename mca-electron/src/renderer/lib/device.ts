export interface DeviceInfo {
  deviceId: string
  deviceName: string
}

// Legacy localStorage keys for backward compatibility
const DEVICE_ID_KEY = 'mca-device-id'
const DEVICE_NAME_KEY = 'mca-device-name'

let cachedDeviceInfo: DeviceInfo | null = null
let electronAPIAvailable: boolean | null = null

const getLocalStorage = () => {
  try {
    return window.localStorage
  } catch {
    return null
  }
}

const readValue = (key: string) => {
  const storage = getLocalStorage()
  return storage?.getItem(key) ?? null
}

const writeValue = (key: string, value: string) => {
  const storage = getLocalStorage()
  if (!storage) return
  storage.setItem(key, value)
}

const isElectronAPIAvailable = (): boolean => {
  if (electronAPIAvailable !== null) {
    return electronAPIAvailable
  }
  electronAPIAvailable = typeof window !== 'undefined' && 'electronAPI' in window
  return electronAPIAvailable
}

const generateDeviceId = () => {
  if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
    return crypto.randomUUID()
  }
  return `${Date.now().toString(36)}-${Math.random().toString(36).slice(2, 10)}`
}

const generateDeviceName = () => {
  const platform =
    typeof navigator !== 'undefined' && navigator.platform
      ? navigator.platform
      : 'Unknown'
  return `MCA-${platform}`
}

// Fallback device info using localStorage
const getFallbackDeviceInfo = (): DeviceInfo => {
  let deviceId = readValue(DEVICE_ID_KEY)
  if (!deviceId) {
    deviceId = generateDeviceId()
    writeValue(DEVICE_ID_KEY, deviceId)
  }

  let deviceName = readValue(DEVICE_NAME_KEY)
  if (!deviceName) {
    deviceName = generateDeviceName()
    writeValue(DEVICE_NAME_KEY, deviceName)
  }

  return { deviceId, deviceName }
}

// Get device info from Electron main process
const getElectronDeviceInfo = async (): Promise<DeviceInfo> => {
  try {
    const api = (window as unknown as { electronAPI: { app: { getDeviceInfo: () => Promise<DeviceInfo> } } }).electronAPI
    return await api.app.getDeviceInfo()
  } catch (err) {
    console.error('Failed to get device info from Electron:', err)
    throw err
  }
}

// Main function to get device info
export const getDeviceInfo = async (): Promise<DeviceInfo> => {
  if (cachedDeviceInfo) {
    return cachedDeviceInfo
  }

  // Try to use Electron API first
  if (isElectronAPIAvailable()) {
    try {
      const deviceInfo = await getElectronDeviceInfo()
      cachedDeviceInfo = deviceInfo
      return deviceInfo
    } catch {
      // Fall back to localStorage if Electron API fails
    }
  }

  // Fallback to localStorage
  const fallbackInfo = getFallbackDeviceInfo()
  cachedDeviceInfo = fallbackInfo
  return fallbackInfo
}

// Synchronous version for backward compatibility (may return cached or fallback)
export const getDeviceInfoSync = (): DeviceInfo => {
  if (cachedDeviceInfo) {
    return cachedDeviceInfo
  }

  // Return fallback immediately for sync usage
  const fallbackInfo = getFallbackDeviceInfo()
  cachedDeviceInfo = fallbackInfo
  return fallbackInfo
}

// Update device name
export const updateDeviceName = async (deviceName: string): Promise<void> => {
  if (isElectronAPIAvailable()) {
    try {
      const api = (window as unknown as { electronAPI: { app: { setDeviceName: (name: string) => Promise<{ success: boolean }> } } }).electronAPI
      await api.app.setDeviceName(deviceName)
    } catch (err) {
      console.error('Failed to update device name via Electron:', err)
    }
  }

  // Always update localStorage as backup
  writeValue(DEVICE_NAME_KEY, deviceName)

  // Update cache
  if (cachedDeviceInfo) {
    cachedDeviceInfo.deviceName = deviceName
  } else {
    cachedDeviceInfo = { deviceId: readValue(DEVICE_ID_KEY) || generateDeviceId(), deviceName }
  }
}

// Initialize device info on module load (for sync compatibility)
getDeviceInfo().catch(() => {})
