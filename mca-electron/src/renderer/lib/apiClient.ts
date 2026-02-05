import { apiFetch, type ApiFetchOptions } from '@/lib/api'
import { useStore } from '@/stores/useStore'

export async function apiFetchAuth<T>(
  path: string,
  options: ApiFetchOptions = {}
): Promise<T> {
  const store = useStore()
  const headers = new Headers(options.headers ?? {})
  const token = store.user?.accessToken
  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  return apiFetch<T>(path, { ...options, headers })
}
