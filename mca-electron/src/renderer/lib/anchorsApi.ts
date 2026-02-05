import { apiFetchAuth } from '@/lib/apiClient'
import type { ApiResponse, ApiUserAnchorDTO } from '@/types'

export const listUserAnchors = async () =>
  apiFetchAuth<ApiResponse<ApiUserAnchorDTO[]>>('/anchors')

export const createUserAnchor = async (payload: {
  tiktokId?: string
  name: string
  avatarUrl?: string
}) =>
  apiFetchAuth<ApiResponse<ApiUserAnchorDTO>>('/anchors', {
    method: 'POST',
    json: payload,
  })

export const updateUserAnchor = async (
  anchorId: string,
  payload: { tiktokId?: string | null; name?: string; avatarUrl?: string | null }
) =>
  apiFetchAuth<ApiResponse<ApiUserAnchorDTO>>(`/anchors/${anchorId}`, {
    method: 'PUT',
    json: payload,
  })

export const deleteUserAnchor = async (anchorId: string) =>
  apiFetchAuth<ApiResponse<void>>(`/anchors/${anchorId}`, {
    method: 'DELETE',
  })
