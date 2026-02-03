import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type {
  Room,
  Anchor,
  User,
  GiftRecord,
  Preset,
  Session,
  AppSettings,
  ActivationInfo,
  GameMode,
  StickerModeConfig,
  PKModeConfig,
  FreeModeConfig,
  LeaderboardEntry,
  WidgetLink,
  UserDTO,
} from '@/types'

interface AppState {
  // User Auth
  user: UserDTO | null
  setUser: (user: UserDTO | null) => void
  logout: () => void
  isAuthenticated: () => boolean
  hasPremium: () => boolean

  // Activation
  activation: ActivationInfo
  setActivation: (activation: ActivationInfo) => void

  // Settings
  settings: AppSettings
  setSettings: (settings: Partial<AppSettings>) => void

  // Room
  currentRoom: Room | null
  recentRooms: Room[]
  setCurrentRoom: (room: Room | null) => void
  addRecentRoom: (room: Room) => void

  // Anchors
  anchors: Anchor[]
  setAnchors: (anchors: Anchor[]) => void
  addAnchor: (anchor: Anchor) => void
  updateAnchor: (id: string, updates: Partial<Anchor>) => void
  removeAnchor: (id: string) => void

  // Users
  users: User[]
  setUsers: (users: User[]) => void
  updateUser: (id: string, updates: Partial<User>) => void

  // Gift Records
  giftRecords: GiftRecord[]
  setGiftRecords: (records: GiftRecord[]) => void
  addGiftRecord: (record: GiftRecord) => void
  updateGiftRecord: (id: string, updates: Partial<GiftRecord>) => void

  // Presets
  presets: Preset[]
  currentPreset: Preset | null
  setPresets: (presets: Preset[]) => void
  setCurrentPreset: (preset: Preset | null) => void
  addPreset: (preset: Preset) => void
  updatePreset: (id: string, updates: Partial<Preset>) => void
  deletePreset: (id: string) => void

  // Session
  currentSession: Session | null
  setCurrentSession: (session: Session | null) => void
  updateSession: (updates: Partial<Session>) => void

  // Leaderboard
  leaderboard: LeaderboardEntry[]
  setLeaderboard: (entries: LeaderboardEntry[]) => void

  // Widgets
  widgets: WidgetLink[]
  setWidgets: (widgets: WidgetLink[]) => void
  addWidget: (widget: WidgetLink) => void
  removeWidget: (id: string) => void

  // UI State
  isMonitorOpen: boolean
  setIsMonitorOpen: (isOpen: boolean) => void
  activeTab: string
  setActiveTab: (tab: string) => void
  isLoading: boolean
  setIsLoading: (loading: boolean) => void
  error: string | null
  setError: (error: string | null) => void
}

const defaultSettings: AppSettings = {
  language: 'zh',
  theme: 'system',
  notifications: true,
  autoSave: true,
  saveInterval: 30,
}

export const useStore = create<AppState>()(
  persist(
    (set, get) => ({
      // User Auth
      user: null,
      setUser: (user) => set({ user }),
      logout: () => set({ user: null, currentRoom: null, currentSession: null }),
      isAuthenticated: () => {
        const user = get().user
        return user != null && user.accessToken != null
      },
      hasPremium: () => {
        const user = get().user
        return user != null && user.isActivated === true
      },

      // Activation
      activation: { isActivated: false },
      setActivation: (activation) => set({ activation }),

      // Settings
      settings: defaultSettings,
      setSettings: (newSettings) =>
        set((state) => ({
          settings: { ...state.settings, ...newSettings },
        })),

      // Room
      currentRoom: null,
      recentRooms: [],
      setCurrentRoom: (room) => set({ currentRoom: room }),
      addRecentRoom: (room) =>
        set((state) => {
          const filtered = state.recentRooms.filter((r) => r.id !== room.id)
          return {
            recentRooms: [room, ...filtered].slice(0, 3),
          }
        }),

      // Anchors
      anchors: [],
      setAnchors: (anchors) => set({ anchors }),
      addAnchor: (anchor) =>
        set((state) => ({ anchors: [...state.anchors, anchor] })),
      updateAnchor: (id, updates) =>
        set((state) => ({
          anchors: state.anchors.map((a) =>
            a.id === id ? { ...a, ...updates } : a
          ),
        })),
      removeAnchor: (id) =>
        set((state) => ({
          anchors: state.anchors.filter((a) => a.id !== id),
        })),

      // Users
      users: [],
      setUsers: (users) => set({ users }),
      updateUser: (id, updates) =>
        set((state) => ({
          users: state.users.map((u) =>
            u.id === id ? { ...u, ...updates } : u
          ),
        })),

      // Gift Records
      giftRecords: [],
      setGiftRecords: (records) => set({ giftRecords: records }),
      addGiftRecord: (record) =>
        set((state) => ({
          giftRecords: [record, ...state.giftRecords],
        })),
      updateGiftRecord: (id, updates) =>
        set((state) => ({
          giftRecords: state.giftRecords.map((r) =>
            r.id === id ? { ...r, ...updates } : r
          ),
        })),

      // Presets
      presets: [],
      currentPreset: null,
      setPresets: (presets) => set({ presets }),
      setCurrentPreset: (preset) => set({ currentPreset: preset }),
      addPreset: (preset) =>
        set((state) => ({ presets: [...state.presets, preset] })),
      updatePreset: (id, updates) =>
        set((state) => ({
          presets: state.presets.map((p) =>
            p.id === id ? { ...p, ...updates } : p
          ),
        })),
      deletePreset: (id) =>
        set((state) => ({
          presets: state.presets.filter((p) => p.id !== id),
        })),

      // Session
      currentSession: null,
      setCurrentSession: (session) => set({ currentSession: session }),
      updateSession: (updates) =>
        set((state) => ({
          currentSession: state.currentSession
            ? { ...state.currentSession, ...updates }
            : null,
        })),

      // Leaderboard
      leaderboard: [],
      setLeaderboard: (entries) => set({ leaderboard: entries }),

      // Widgets
      widgets: [],
      setWidgets: (widgets) => set({ widgets }),
      addWidget: (widget) =>
        set((state) => ({ widgets: [...state.widgets, widget] })),
      removeWidget: (id) =>
        set((state) => ({
          widgets: state.widgets.filter((w) => w.id !== id),
        })),

      // UI State
      isMonitorOpen: false,
      setIsMonitorOpen: (isOpen) => set({ isMonitorOpen: isOpen }),
      activeTab: 'dashboard',
      setActiveTab: (tab) => set({ activeTab: tab }),
      isLoading: false,
      setIsLoading: (loading) => set({ isLoading: loading }),
      error: null,
      setError: (error) => set({ error }),
    }),
    {
      name: 'mca-storage',
      partialize: (state) => ({
        user: state.user,
        activation: state.activation,
        settings: state.settings,
        recentRooms: state.recentRooms,
        presets: state.presets,
        anchors: state.anchors,
      }),
    }
  )
)
