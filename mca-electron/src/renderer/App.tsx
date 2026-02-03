import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Toaster } from '@/components/ui/toaster'
import { AuthLayout } from '@/components/AuthLayout'
import { RoomConnection } from '@/components/RoomConnection'
import { MainLayout } from '@/components/MainLayout'
import './i18n'
import './styles/globals.css'

function App() {
  const { i18n } = useTranslation()
  const { settings, user, isAuthenticated } = useStore()

  useEffect(() => {
    // Set language from settings
    if (settings.language) {
      i18n.changeLanguage(settings.language)
    }
  }, [settings.language, i18n])

  // Show auth layout if not authenticated
  if (!isAuthenticated()) {
    return (
      <>
        <AuthLayout onAuthSuccess={() => {}} />
        <Toaster />
      </>
    )
  }

  // Show room connection if no room connected
  const { currentRoom } = useStore()
  if (!currentRoom?.isConnected) {
    return (
      <>
        <RoomConnection />
        <Toaster />
      </>
    )
  }

  return (
    <>
      <MainLayout />
      <Toaster />
    </>
  )
}

export default App
