export interface DeviceInfo {
  deviceId: string
  deviceName: string
}

const DEVICE_ID_KEY = 'mca-device-id'
const DEVICE_NAME_KEY = 'mca-device-name'

let cachedDeviceInfo: DeviceInfo | null = null

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

export const getDeviceInfo = (): DeviceInfo => {
  if (cachedDeviceInfo) {
    return cachedDeviceInfo
  }

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

  cachedDeviceInfo = { deviceId, deviceName }
  return cachedDeviceInfo
}
