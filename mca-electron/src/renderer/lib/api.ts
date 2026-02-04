import { getDeviceInfo } from '@/lib/device'
import type { DeviceInfo } from '@/lib/device'

export interface ApiFetchOptions extends RequestInit {
  baseUrl?: string
  json?: unknown
}

const DEFAULT_API_BASE_URL =
  import.meta.env.VITE_API_URL || 'http://localhost:8080/api'

const isAbsoluteUrl = (url: string) => /^https?:\/\//i.test(url)

const joinUrl = (baseUrl: string, path: string) => {
  const base = baseUrl.replace(/\/+$/, '')
  const suffix = path.replace(/^\/+/, '')
  return `${base}/${suffix}`
}

const withDeviceInfo = async (json: unknown): Promise<unknown> => {
  if (!json || typeof json !== 'object' || Array.isArray(json)) {
    return json
  }

  const deviceInfo = await getDeviceInfo()
  const record = json as Record<string, unknown>
  const resolveValue = (value: unknown, fallback: string) =>
    typeof value === 'string' && value.trim().length > 0 ? value : fallback

  return {
    ...record,
    deviceId: resolveValue(record.deviceId, deviceInfo.deviceId),
    deviceName: resolveValue(record.deviceName, deviceInfo.deviceName),
  }
}

export async function apiFetch<T>(
  path: string,
  options: ApiFetchOptions = {}
): Promise<T> {
  const { baseUrl = DEFAULT_API_BASE_URL, headers, json, ...init } = options
  const url = isAbsoluteUrl(path) ? path : joinUrl(baseUrl, path)

  const deviceInfo = await getDeviceInfo()
  const mergedHeaders = new Headers(headers ?? {})
  if (!mergedHeaders.has('X-Device-Id')) {
    mergedHeaders.set('X-Device-Id', deviceInfo.deviceId)
  }
  if (!mergedHeaders.has('X-Device-Name')) {
    mergedHeaders.set('X-Device-Name', deviceInfo.deviceName)
  }

  let body = init.body
  if (json !== undefined) {
    if (!mergedHeaders.has('Content-Type')) {
      mergedHeaders.set('Content-Type', 'application/json')
    }
    body = JSON.stringify(await withDeviceInfo(json))
  }

  const response = await fetch(url, { ...init, headers: mergedHeaders, body })
  const text = await response.text()

  if (!text) {
    return undefined as T
  }

  try {
    return JSON.parse(text) as T
  } catch {
    return text as T
  }
}
