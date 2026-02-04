# Migration Mapping

## Pages (routes)
- Dashboard -> src/renderer/pages/Dashboard.vue (React: src/renderer/pages/Dashboard.tsx)
- Sticker Mode -> src/renderer/pages/StickerMode.vue (React: src/renderer/pages/StickerMode.tsx)
- PK Mode -> src/renderer/pages/PKMode.vue (React: src/renderer/pages/PKMode.tsx)
- Free Mode -> src/renderer/pages/FreeMode.vue (React: src/renderer/pages/FreeMode.tsx)
- Widgets -> src/renderer/pages/Widgets.vue (React: src/renderer/pages/Widgets.tsx)
- Reports -> src/renderer/pages/Reports.vue (React: src/renderer/pages/Reports.tsx)
- Settings -> src/renderer/pages/Settings.vue (React: src/renderer/pages/Settings.tsx)

## Base UI Components
- Avatar -> src/renderer/components/ui/Avatar.vue (React: src/renderer/components/ui/avatar.tsx)
- Badge -> src/renderer/components/ui/Badge.vue (React: src/renderer/components/ui/badge.tsx)
- Button -> src/renderer/components/ui/Button.vue + src/renderer/components/ui/button.ts (React: src/renderer/components/ui/button.tsx)
- Card -> src/renderer/components/ui/Card.vue + CardHeader/CardContent/CardTitle/CardDescription/CardFooter (React: src/renderer/components/ui/card.tsx)
- Dialog -> src/renderer/components/ui/Dialog.vue + DialogContent/DialogHeader/DialogFooter/DialogTitle/DialogDescription (React: src/renderer/components/ui/dialog.tsx)
- Input -> src/renderer/components/ui/Input.vue (React: src/renderer/components/ui/input.tsx)
- ScrollArea -> src/renderer/components/ui/ScrollArea.vue (React: src/renderer/components/ui/scroll-area.tsx)
- Select -> src/renderer/components/ui/Select*.vue (React: src/renderer/components/ui/select.tsx)
- Slider -> src/renderer/components/ui/Slider.vue (React: src/renderer/components/ui/slider.tsx)
- Switch -> src/renderer/components/ui/Switch.vue (React: src/renderer/components/ui/switch.tsx)
- Table -> src/renderer/components/ui/Table*.vue (React: src/renderer/components/ui/table.tsx)
- Tabs -> src/renderer/components/ui/Tabs*.vue (React: src/renderer/components/ui/tabs.tsx)
- Toast -> src/renderer/components/ui/Toast*.vue + Toaster (React: src/renderer/components/ui/toast.tsx + toaster.tsx)

## Store Mapping (React -> Pinia)
- Auth + user: useStore.user -> Pinia store.user (UserDTO)
- Activation: useStore.activation -> Pinia store.activation (ActivationInfo)
- Settings: useStore.settings -> Pinia store.settings (AppSettings)
- Rooms: useStore.currentRoom/recentRooms -> Pinia store.currentRoom/recentRooms
- Anchors + leaderboard: useStore.anchors/leaderboard -> Pinia store.anchors/leaderboard
- Presets + sessions: useStore.presets/currentPreset/currentSession -> Pinia store.presets/currentPreset/currentSession
- Widgets: useStore.widgets -> Pinia store.widgets
