// Gift types
export interface Gift {
  id: string
  name: string
  diamondCost: number
  iconUrl: string
  animationUrl?: string
}

export interface GiftRecord {
  id: string
  giftId: string
  giftName: string
  giftIcon: string
  userId: string
  userName: string
  userAvatar?: string
  anchorId: string
  anchorName: string
  quantity: number
  diamondCost: number
  totalCost: number
  timestamp: number
  bindType: 'auto' | 'manual' | 'single' | 'none'
}

export interface TargetGift {
  giftId?: string
  giftName?: string
  giftIcon?: string
  diamondCost?: number
  points?: number
  isTarget?: boolean
}

export interface TargetGiftsConfig {
  targetGifts: TargetGift[]
}

// Anchor types
export interface Anchor {
  id: string
  tiktokId?: string
  name: string
  avatar?: string
  exclusiveGifts: string[]
  totalScore: number
  isEliminated: boolean
  isActive: boolean
  order: number
}

export interface UserAnchor {
  id: string
  tiktokId?: string
  name: string
  avatarUrl?: string
  createdAt?: number
  updatedAt?: number
}

// User types
export interface User {
  id: string
  tiktokId: string
  name: string
  avatar?: string
  totalContribution: number
  boundAnchorId?: string
  bindType?: 'auto' | 'manual' | 'single'
}

// Room types
export interface Room {
  id: string
  tiktokId: string
  title?: string
  coverImage?: string
  isConnected: boolean
  connectedAt?: number
}

// Mode types
export type GameMode = 'sticker' | 'pk' | 'free'

export interface StickerModeConfig {
  type: 'normal' | 'scoring'
  countdownEnabled: boolean
  countdownDuration: number
  decayEnabled: boolean
  decayDuration: number
  autoFlipEnabled: boolean
  flipInterval: number
  maxPages: number
  gameplayGifts: GameplayGift[]
}

export interface GameplayGift {
  giftId: string
  effect: 'bounce' | 'shake' | 'glow'
}

export interface PKModeConfig {
  advantage: 'defender' | 'attacker' | 'draw'
  scoringMethod: 'individual' | 'split' | 'defender'
  countdownEnabled: boolean
  countdownDuration: number
  singleGiftBindEnabled: boolean
  freezeEffectEnabled: boolean
  freezeThresholds: {
    low: number
    medium: number
    high: number
  }
}

export interface FreeModeConfig {
  roundDuration: number
  targetScore: number
  showRoundNumber: boolean
  displayRange: [number, number]
}

// Preset types
export interface Preset {
  id: string
  name: string
  mode: GameMode
  anchors: Anchor[]
  config: StickerModeConfig | PKModeConfig | FreeModeConfig
  targetGifts?: TargetGiftsConfig
  widgetSettings: WidgetSettings
  widgetToken?: string
  createdAt: number
  updatedAt: number
  isDefault?: boolean
}

export interface WidgetSettings {
  showAnchorName: boolean
  showGiftCount: boolean
  showTotalScore: boolean
  showProgressBar: boolean
  showCountdown: boolean
  theme: 'default' | 'neon' | 'minimal'
  layout: 'horizontal' | 'vertical'
}

// Widget types
export interface WidgetLink {
  id: string
  type: 'main' | 'leaderboard' | 'avatar-frame' | 'freeze-effect'
  name: string
  url: string
  width: number
  height: number
}

// Session types
export interface Session {
  id: string
  roomId: string
  presetId: string
  mode: GameMode
  status: 'idle' | 'running' | 'paused' | 'ended'
  startedAt?: number
  endedAt?: number
  currentRound: number
  rounds: Round[]
  giftRecords: GiftRecord[]
}

export interface Round {
  id: string
  roundNumber: number
  anchors: RoundAnchor[]
  startTime: number
  endTime?: number
  winnerId?: string
}

export interface RoundAnchor {
  anchorId: string
  score: number
  rank: number
}

// Leaderboard types
export interface LeaderboardEntry {
  rank: number
  anchorId: string
  anchorName: string
  anchorAvatar?: string
  score: number
  giftCount: number
}

// Settings types
export interface AppSettings {
  language: 'zh' | 'en' | 'vi' | 'th'
  theme: 'light' | 'dark' | 'system'
  notifications: boolean
  autoSave: boolean
  saveInterval: number
}

// User DTO from backend
export interface UserDTO {
  id: string
  email: string
  nickname: string
  avatarUrl?: string
  isEmailVerified: boolean
  isActivated: boolean
  subscriptionType: 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE'
  subscriptionExpiresAt?: number
  activatedAt?: number
  activationCode?: string
  lastLoginAt?: number
  createdAt: number
  accessToken?: string
  refreshToken?: string
}

// Activation types
export interface ActivationInfo {
  isActivated: boolean
  deviceName?: string
  activatedAt?: number
  expiresAt?: number
  activationCode?: string
}

// API Response types
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
  message?: string
}

// API DTOs
export interface ApiAnchorDTO {
  id: string
  tiktokId?: string
  name: string
  avatarUrl?: string
  exclusiveGifts?: string[]
  totalScore?: number
  isEliminated?: boolean
  isActive?: boolean
  displayOrder?: number
}

export interface ApiUserAnchorDTO {
  id: string
  tiktokId?: string
  name: string
  avatarUrl?: string
  createdAt?: number
  updatedAt?: number
}

export interface ApiPresetDTO {
  id: string
  name: string
  deviceId: string
  gameMode: 'STICKER' | 'PK' | 'FREE'
  anchors?: ApiAnchorDTO[]
  targetGiftsJson?: string
  configJson?: string
  widgetSettingsJson?: string
  widgetToken?: string
  isDefault?: boolean
  createdAt?: number
  updatedAt?: number
}

// WebSocket types
export interface WebSocketMessage {
  type: 'gift' | 'like' | 'follow' | 'share' | 'comment' | 'join' | 'leave'
  data: unknown
  timestamp: number
}

// Report types
export interface ReportData {
  sessionId: string
  roomId: string
  startTime: number
  endTime: number
  totalGifts: number
  totalDiamonds: number
  anchorStats: AnchorStat[]
  userStats: UserStat[]
  giftStats: GiftStat[]
}

export interface AnchorStat {
  anchorId: string
  anchorName: string
  totalScore: number
  giftCount: number
  uniqueUsers: number
  roundsWon: number
}

export interface UserStat {
  userId: string
  userName: string
  totalSpent: number
  giftCount: number
  favoriteAnchor?: string
}

export interface GiftStat {
  giftId: string
  giftName: string
  totalCount: number
  totalValue: number
}
