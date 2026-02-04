import { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Toaster } from '@/components/ui/toaster'
import { AuthLayout } from '@/components/AuthLayout'
import { MainLayout } from '@/components/MainLayout'
import { useToast } from '@/hooks/use-toast'
import { apiFetch } from '@/lib/api'
import { normalizeUserDates } from '@/lib/user'
import type { ApiResponse, UserDTO } from '@/types'
import './i18n'
import './styles/globals.css'

function App() {
  const { i18n } = useTranslation()
  const { settings, user, isAuthenticated, setUser, currentRoom } = useStore()
  const { toast } = useToast()

  useEffect(() => {
    // Set language from settings
    if (settings.language) {
      i18n.changeLanguage(settings.language)
    }
  }, [settings.language, i18n])

  useEffect(() => {
    const { pathname, search } = window.location
    if (pathname !== '/verify-email' && pathname !== '/verify-email/') return

    const params = new URLSearchParams(search)
    const token = params.get('token')
    if (!token) {
      toast({
        title: 'Error',
        description: 'Missing verification token',
        variant: 'destructive',
      })
      window.history.replaceState({}, '', '/')
      return
    }

    apiFetch<ApiResponse<UserDTO>>(`/auth/verify-email?token=${encodeURIComponent(token)}`)
      .then((data) => {
        if (data?.success) {
          setUser(normalizeUserDates(data.data))
          toast({
            title: 'Success',
            description: 'Email verified successfully!',
          })
        } else {
          toast({
            title: 'Error',
            description: data?.error || 'Email verification failed',
            variant: 'destructive',
          })
        }
      })
      .catch(() => {
        toast({
          title: 'Error',
          description: 'Network error. Please try again.',
          variant: 'destructive',
        })
      })
      .finally(() => {
        window.history.replaceState({}, '', '/')
      })
  }, [setUser, toast])

  // Show auth layout if not authenticated
  if (!isAuthenticated()) {
    return (
      <>
        <AuthLayout onAuthSuccess={() => {}} />
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
