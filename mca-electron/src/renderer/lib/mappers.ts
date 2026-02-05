import type {
  Anchor,
  ApiAnchorDTO,
  ApiUserAnchorDTO,
  ApiPresetDTO,
  FreeModeConfig,
  GameMode,
  PKModeConfig,
  Preset,
  StickerModeConfig,
  TargetGiftsConfig,
  UserAnchor,
  WidgetSettings,
} from '@/types'

const defaultStickerConfig: StickerModeConfig = {
  type: 'normal',
  countdownEnabled: false,
  countdownDuration: 60,
  decayEnabled: false,
  decayDuration: 30,
  autoFlipEnabled: false,
  flipInterval: 10,
  maxPages: 2,
  gameplayGifts: [],
}

const defaultPKConfig: PKModeConfig = {
  advantage: 'defender',
  scoringMethod: 'individual',
  countdownEnabled: false,
  countdownDuration: 60,
  singleGiftBindEnabled: false,
  freezeEffectEnabled: false,
  freezeThresholds: { low: 10, medium: 30, high: 60 },
}

const defaultFreeConfig: FreeModeConfig = {
  roundDuration: 60,
  targetScore: 1000,
  showRoundNumber: true,
  displayRange: [1, 5],
}

const defaultWidgetSettings: WidgetSettings = {
  showAnchorName: true,
  showGiftCount: true,
  showTotalScore: true,
  showProgressBar: true,
  showCountdown: true,
  theme: 'default',
  layout: 'horizontal',
}

const defaultTargetGifts: TargetGiftsConfig = {
  targetGifts: [],
  scoringRules: {
    mode: 'POINTS',
    multiplier: 1,
  },
}

const safeJsonParse = <T>(value?: string, fallback?: T): T | undefined => {
  if (!value) return fallback
  try {
    return JSON.parse(value) as T
  } catch {
    return fallback
  }
}

const tryJsonParse = (value?: string): unknown => {
  if (!value) return undefined
  try {
    return JSON.parse(value)
  } catch {
    return undefined
  }
}

const mapGameMode = (mode?: string): GameMode => {
  switch (mode) {
    case 'PK':
      return 'pk'
    case 'FREE':
      return 'free'
    case 'STICKER':
    default:
      return 'sticker'
  }
}

const resolveConfig = (mode: GameMode, configJson?: string) => {
  const parsed = tryJsonParse(configJson)
  if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
    const record = parsed as Record<string, unknown>
    const nested =
      mode === 'pk'
        ? record.pk
        : mode === 'free'
          ? record.free
          : record.sticker ?? record.stickerMode
    if (nested && typeof nested === 'object') {
      return nested as PKModeConfig | FreeModeConfig | StickerModeConfig
    }
    if (mode === 'pk' && 'advantage' in record) {
      return record as PKModeConfig
    }
    if (mode === 'free' && 'roundDuration' in record) {
      return record as FreeModeConfig
    }
    if (mode === 'sticker' && 'type' in record) {
      return record as StickerModeConfig
    }
  }
  switch (mode) {
    case 'pk':
      return safeJsonParse<PKModeConfig>(configJson, defaultPKConfig) ?? defaultPKConfig
    case 'free':
      return safeJsonParse<FreeModeConfig>(configJson, defaultFreeConfig) ?? defaultFreeConfig
    case 'sticker':
    default:
      return safeJsonParse<StickerModeConfig>(configJson, defaultStickerConfig) ?? defaultStickerConfig
  }
}

export const mapApiAnchor = (dto: ApiAnchorDTO): Anchor => ({
  id: dto.id,
  tiktokId: dto.tiktokId ?? undefined,
  name: dto.name,
  avatar: dto.avatarUrl ?? undefined,
  exclusiveGifts: dto.exclusiveGifts ?? [],
  totalScore: dto.totalScore ?? 0,
  isEliminated: dto.isEliminated ?? false,
  isActive: dto.isActive ?? true,
  order: dto.displayOrder ?? 0,
})

export const mapApiUserAnchor = (dto: ApiUserAnchorDTO): UserAnchor => ({
  id: dto.id,
  tiktokId: dto.tiktokId ?? undefined,
  name: dto.name,
  avatarUrl: dto.avatarUrl ?? undefined,
  createdAt: dto.createdAt,
  updatedAt: dto.updatedAt,
})

export const mapApiPreset = (dto: ApiPresetDTO): Preset => {
  const mode = mapGameMode(dto.gameMode)
  return {
    id: dto.id,
    name: dto.name,
    mode,
    anchors: (dto.anchors ?? []).map(mapApiAnchor),
    config: resolveConfig(mode, dto.configJson),
    targetGifts:
      safeJsonParse<TargetGiftsConfig>(dto.targetGiftsJson, defaultTargetGifts) ??
      defaultTargetGifts,
    widgetSettings:
      safeJsonParse<WidgetSettings>(dto.widgetSettingsJson, defaultWidgetSettings) ??
      defaultWidgetSettings,
    widgetToken: dto.widgetToken,
    createdAt: dto.createdAt ?? Date.now(),
    updatedAt: dto.updatedAt ?? Date.now(),
    isDefault: dto.isDefault ?? false,
  }
}
