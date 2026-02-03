import type { UserDTO } from '@/types'

const parseDateToMs = (value?: string | number | null) => {
  if (value === null || value === undefined) return undefined
  if (typeof value === 'number') return value
  if (typeof value !== 'string') return undefined
  const normalized = value.includes('T') ? value : value.replace(' ', 'T')
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? undefined : date.getTime()
}

export const normalizeUserDates = (user?: UserDTO | null): UserDTO | null => {
  if (!user) return user ?? null
  return {
    ...user,
    activatedAt: parseDateToMs(user.activatedAt),
    subscriptionExpiresAt: parseDateToMs(user.subscriptionExpiresAt),
    lastLoginAt: parseDateToMs(user.lastLoginAt),
    createdAt: parseDateToMs(user.createdAt),
  }
}
