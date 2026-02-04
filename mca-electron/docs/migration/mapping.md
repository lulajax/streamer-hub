# Migration Mapping

## Pages (routes)
- Dashboard -> src/renderer/pages/Dashboard.vue
- Sticker Mode -> src/renderer/pages/StickerMode.vue
- PK Mode -> src/renderer/pages/PKMode.vue
- Free Mode -> src/renderer/pages/FreeMode.vue
- Widgets -> src/renderer/pages/Widgets.vue
- Reports -> src/renderer/pages/Reports.vue
- Settings -> src/renderer/pages/Settings.vue

## Base UI Components
- Avatar -> src/renderer/components/ui/Avatar.vue
- Badge -> src/renderer/components/ui/Badge.vue
- Button -> src/renderer/components/ui/Button.vue + src/renderer/components/ui/button.ts
- Card -> src/renderer/components/ui/Card.vue + CardHeader/CardContent/CardTitle/CardDescription/CardFooter
- Dialog -> src/renderer/components/ui/Dialog.vue + DialogContent/DialogHeader/DialogFooter/DialogTitle/DialogDescription
- Input -> src/renderer/components/ui/Input.vue
- ScrollArea -> src/renderer/components/ui/ScrollArea.vue
- Select -> src/renderer/components/ui/Select*.vue
- Slider -> src/renderer/components/ui/Slider.vue
- Switch -> src/renderer/components/ui/Switch.vue
- Table -> src/renderer/components/ui/Table*.vue
- Tabs -> src/renderer/components/ui/Tabs*.vue
- Toast -> src/renderer/components/ui/Toast*.vue + Toaster

## Store Mapping (React -> Pinia)
- Auth + user: useStore.user -> Pinia store.user (UserDTO)
- Activation: useStore.activation -> Pinia store.activation (ActivationInfo)
- Settings: useStore.settings -> Pinia store.settings (AppSettings)
- Rooms: useStore.currentRoom/recentRooms -> Pinia store.currentRoom/recentRooms
- Anchors + leaderboard: useStore.anchors/leaderboard -> Pinia store.anchors/leaderboard
- Presets + sessions: useStore.presets/currentPreset/currentSession -> Pinia store.presets/currentPreset/currentSession
- Widgets: useStore.widgets -> Pinia store.widgets
