import { defineStore } from 'pinia'
import type {
  Room,
  Anchor,
  User,
  GiftRecord,
  Preset,
  Session,
  AppSettings,
  ActivationInfo,
  LeaderboardEntry,
  WidgetLink,
  UserDTO,
  UserAnchor,
} from '@/types'

interface AppState {
  user: UserDTO | null
  activation: ActivationInfo
  settings: AppSettings
  currentRoom: Room | null
  recentRooms: Room[]
  anchors: Anchor[]
  userAnchors: UserAnchor[]
  users: User[]
  giftRecords: GiftRecord[]
  presets: Preset[]
  presetSelections: {
    sticker: string | null
    pk: string | null
    free: string | null
  }
  currentPreset: Preset | null
  currentSession: Session | null
  leaderboard: LeaderboardEntry[]
  widgets: WidgetLink[]
  isMonitorOpen: boolean
  activeTab: string
  isLoading: boolean
  error: string | null
}

const defaultSettings: AppSettings = {
  language: 'zh',
  theme: 'system',
  notifications: true,
  autoSave: true,
  saveInterval: 30,
}

export const useStore = defineStore('app', {
  state: (): AppState => ({
    user: null,
    activation: { isActivated: true },
    settings: defaultSettings,
    currentRoom: null,
    recentRooms: [],
    anchors: [],
    userAnchors: [],
    users: [],
    giftRecords: [],
    presets: [],
    presetSelections: {
      sticker: null,
      pk: null,
      free: null,
    },
    currentPreset: null,
    currentSession: null,
    leaderboard: [],
    widgets: [],
    isMonitorOpen: false,
    activeTab: 'dashboard',
    isLoading: false,
    error: null,
  }),
  getters: {
    isAuthenticated: (state) => state.user != null && state.user.accessToken != null,
    hasPremium: (state) => state.user != null && state.user.isActivated === true,
  },
  actions: {
    setUser(user: UserDTO | null) {
      this.user = user
    },
    logout() {
      this.user = null
      this.currentRoom = null
      this.currentSession = null
      this.anchors = []
      this.userAnchors = []
      this.presets = []
      this.currentPreset = null
      this.presetSelections = {
        sticker: null,
        pk: null,
        free: null,
      }
      this.activeTab = 'dashboard'
    },
    setActivation(activation: ActivationInfo) {
      this.activation = activation
    },
    setSettings(settings: Partial<AppSettings>) {
      this.settings = { ...this.settings, ...settings }
    },
    setCurrentRoom(room: Room | null) {
      this.currentRoom = room
    },
    addRecentRoom(room: Room) {
      const filtered = this.recentRooms.filter((r) => r.id !== room.id)
      this.recentRooms = [room, ...filtered].slice(0, 3)
    },
    setAnchors(anchors: Anchor[]) {
      this.anchors = anchors
    },
    addAnchor(anchor: Anchor) {
      this.anchors = [...this.anchors, anchor]
    },
    updateAnchor(id: string, updates: Partial<Anchor>) {
      this.anchors = this.anchors.map((anchor) =>
        anchor.id === id ? { ...anchor, ...updates } : anchor
      )
    },
    removeAnchor(id: string) {
      this.anchors = this.anchors.filter((anchor) => anchor.id !== id)
    },
    setUsers(users: User[]) {
      this.users = users
    },
    setUserAnchors(anchors: UserAnchor[]) {
      this.userAnchors = anchors
    },
    addUserAnchor(anchor: UserAnchor) {
      this.userAnchors = [...this.userAnchors, anchor]
    },
    updateUserAnchor(id: string, updates: Partial<UserAnchor>) {
      this.userAnchors = this.userAnchors.map((anchor) =>
        anchor.id === id ? { ...anchor, ...updates } : anchor
      )
    },
    removeUserAnchor(id: string) {
      this.userAnchors = this.userAnchors.filter((anchor) => anchor.id !== id)
    },
    updateUser(id: string, updates: Partial<User>) {
      this.users = this.users.map((user) => (user.id === id ? { ...user, ...updates } : user))
    },
    setGiftRecords(records: GiftRecord[]) {
      this.giftRecords = records
    },
    addGiftRecord(record: GiftRecord) {
      this.giftRecords = [record, ...this.giftRecords]
    },
    updateGiftRecord(id: string, updates: Partial<GiftRecord>) {
      this.giftRecords = this.giftRecords.map((record) =>
        record.id === id ? { ...record, ...updates } : record
      )
    },
    setPresets(presets: Preset[]) {
      this.presets = presets
    },
    setPresetSelection(mode: 'sticker' | 'pk' | 'free', presetId: string | null) {
      this.presetSelections = {
        ...this.presetSelections,
        [mode]: presetId,
      }
    },
    selectPresetForMode(mode: 'sticker' | 'pk' | 'free', preset: Preset | null) {
      this.setPresetSelection(mode, preset?.id ?? null)
      this.setCurrentPreset(preset)
    },
    applyPresetSelection(mode: 'sticker' | 'pk' | 'free') {
      const presetId = this.presetSelections[mode]
      const preset =
        (presetId ? this.presets.find((item) => item.id === presetId) : null) ??
        this.presets.find((item) => item.mode === mode) ??
        null
      this.setCurrentPreset(preset)
      if (preset) {
        this.setPresetSelection(mode, preset.id)
      }
    },
    setCurrentPreset(preset: Preset | null) {
      this.currentPreset = preset
      this.anchors = preset?.anchors ?? []
    },
    addPreset(preset: Preset) {
      this.presets = [...this.presets, preset]
    },
    updatePreset(id: string, updates: Partial<Preset>) {
      this.presets = this.presets.map((preset) =>
        preset.id === id ? { ...preset, ...updates } : preset
      )
    },
    deletePreset(id: string) {
      this.presets = this.presets.filter((preset) => preset.id !== id)
    },
    setCurrentSession(session: Session | null) {
      this.currentSession = session
    },
    updateSession(updates: Partial<Session>) {
      if (this.currentSession) {
        this.currentSession = { ...this.currentSession, ...updates }
      }
    },
    setLeaderboard(entries: LeaderboardEntry[]) {
      this.leaderboard = entries
    },
    setWidgets(widgets: WidgetLink[]) {
      this.widgets = widgets
    },
    addWidget(widget: WidgetLink) {
      this.widgets = [...this.widgets, widget]
    },
    removeWidget(id: string) {
      this.widgets = this.widgets.filter((widget) => widget.id !== id)
    },
    setIsMonitorOpen(isOpen: boolean) {
      this.isMonitorOpen = isOpen
    },
    setActiveTab(tab: string) {
      this.activeTab = tab
    },
    setIsLoading(loading: boolean) {
      this.isLoading = loading
    },
    setError(error: string | null) {
      this.error = error
    },
  },
  persist: true,
})
