import { apiFetchAuth } from '@/lib/apiClient'
import type { ApiResponse, ApiPresetDTO } from '@/types'

export const listPresets = async () =>
  apiFetchAuth<ApiResponse<ApiPresetDTO[]>>('/presets')

export const getPreset = async (presetId: string) =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>(`/presets/${presetId}`)

export const getDefaultPreset = async () =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>('/presets/default')

export const createPreset = async (payload: {
  name: string
  mode?: 'sticker' | 'pk' | 'free'
  gameMode?: 'STICKER' | 'PK' | 'FREE'
  anchors?: { anchorId: string; exclusiveGifts?: string[]; displayOrder?: number }[]
  config?: unknown
  targetGifts?: unknown
}) => {
  const gameMode = payload.gameMode ?? payload.mode?.toUpperCase() as 'STICKER' | 'PK' | 'FREE'
  return apiFetchAuth<ApiResponse<ApiPresetDTO>>('/presets', {
    method: 'POST',
    json: {
      name: payload.name,
      gameMode,
      anchors: payload.anchors,
      gameConfig: payload.config,
      targetGifts: payload.targetGifts,
    },
  })
}

export const updatePreset = async (
  presetId: string,
  payload: {
    name?: string
    gameMode?: 'STICKER' | 'PK' | 'FREE'
    targetGifts?: unknown
    gameConfig?: unknown
    widgetSettings?: unknown
  }
) =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>(`/presets/${presetId}`, {
    method: 'PUT',
    json: payload,
  })

export const updateGameConfig = async (
  presetId: string,
  payload: {
    sticker?: unknown
    pk?: unknown
    free?: unknown
    targetGifts?: unknown[]
  }
) =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>(`/presets/${presetId}/game-config`, {
    method: 'PUT',
    json: payload,
  })

export const getPresetPreviewUrl = async (presetId: string) =>
  apiFetchAuth<ApiResponse<string>>(`/presets/${presetId}/preview-url`)

export const deletePreset = async (presetId: string) =>
  apiFetchAuth<ApiResponse<void>>(`/presets/${presetId}`, {
    method: 'DELETE',
  })

export const setDefaultPreset = async (presetId: string) =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>(`/presets/${presetId}/set-default`, {
    method: 'POST',
  })

export const attachAnchorToPreset = async (
  presetId: string,
  payload: { anchorId: string; exclusiveGifts?: string[]; displayOrder?: number }
) =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>(`/presets/${presetId}/anchors`, {
    method: 'POST',
    json: payload,
  })

export const detachAnchorFromPreset = async (presetId: string, anchorId: string) =>
  apiFetchAuth<ApiResponse<ApiPresetDTO>>(`/presets/${presetId}/anchors/${anchorId}`, {
    method: 'DELETE',
  })
