import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { useStore } from '@/stores/useStore'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { useToast } from '@/hooks/use-toast'
import { Mail, Lock, User, Eye, EyeOff, Sparkles, Loader2 } from 'lucide-react'

interface AuthLayoutProps {
  onAuthSuccess: () => void
}

export function AuthLayout({ onAuthSuccess }: AuthLayoutProps) {
  const { t } = useTranslation()
  const { toast } = useToast()
  const { setUser } = useStore()
  
  const [activeTab, setActiveTab] = useState('login')
  const [isLoading, setIsLoading] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  
  // Login form
  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  
  // Register form
  const [registerEmail, setRegisterEmail] = useState('')
  const [registerPassword, setRegisterPassword] = useState('')
  const [registerConfirmPassword, setRegisterConfirmPassword] = useState('')
  const [registerNickname, setRegisterNickname] = useState('')
  
  // Activation form
  const [activationCode, setActivationCode] = useState('')
  const [showActivation, setShowActivation] = useState(false)

  const handleLogin = async () => {
    if (!loginEmail || !loginPassword) {
      toast({
        title: 'Error',
        description: 'Please enter email and password',
        variant: 'destructive',
      })
      return
    }
    
    setIsLoading(true)
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: loginEmail,
          password: loginPassword,
        }),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setUser(data.data)
        
        // Check if user needs activation
        if (!data.data.isActivated) {
          setShowActivation(true)
          setIsLoading(false)
          return
        }
        
        toast({
          title: 'Success',
          description: 'Login successful!',
        })
        onAuthSuccess()
      } else {
        toast({
          title: 'Error',
          description: data.error || 'Login failed',
          variant: 'destructive',
        })
      }
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Network error. Please try again.',
        variant: 'destructive',
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleRegister = async () => {
    if (!registerEmail || !registerPassword || !registerConfirmPassword) {
      toast({
        title: 'Error',
        description: 'Please fill in all required fields',
        variant: 'destructive',
      })
      return
    }
    
    if (registerPassword !== registerConfirmPassword) {
      toast({
        title: 'Error',
        description: 'Passwords do not match',
        variant: 'destructive',
      })
      return
    }
    
    if (registerPassword.length < 8) {
      toast({
        title: 'Error',
        description: 'Password must be at least 8 characters',
        variant: 'destructive',
      })
      return
    }
    
    setIsLoading(true)
    
    try {
      const response = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email: registerEmail,
          password: registerPassword,
          confirmPassword: registerConfirmPassword,
          nickname: registerNickname || registerEmail.split('@')[0],
        }),
      })
      
      const data = await response.json()
      
      if (data.success) {
        toast({
          title: 'Success',
          description: 'Registration successful! Please check your email to verify your account.',
        })
        setActiveTab('login')
        setLoginEmail(registerEmail)
      } else {
        toast({
          title: 'Error',
          description: data.error || 'Registration failed',
          variant: 'destructive',
        })
      }
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Network error. Please try again.',
        variant: 'destructive',
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleActivate = async () => {
    if (!activationCode) {
      toast({
        title: 'Error',
        description: 'Please enter activation code',
        variant: 'destructive',
      })
      return
    }
    
    setIsLoading(true)
    
    try {
      const user = useStore.getState().user
      const response = await fetch('http://localhost:8080/api/auth/activate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${user?.accessToken}`,
        },
        body: JSON.stringify({
          activationCode: activationCode.toUpperCase(),
        }),
      })
      
      const data = await response.json()
      
      if (data.success) {
        setUser(data.data)
        toast({
          title: 'Success',
          description: 'Premium activated successfully!',
        })
        setShowActivation(false)
        onAuthSuccess()
      } else {
        toast({
          title: 'Error',
          description: data.error || 'Activation failed',
          variant: 'destructive',
        })
      }
    } catch (error) {
      toast({
        title: 'Error',
        description: 'Network error. Please try again.',
        variant: 'destructive',
      })
    } finally {
      setIsLoading(false)
    }
  }

  // Activation Code Input View
  if (showActivation) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
        <Card className="w-full max-w-md bg-slate-800/50 border-slate-700 backdrop-blur">
          <CardHeader className="text-center">
            <div className="w-16 h-16 bg-gradient-to-br from-amber-500 to-orange-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-amber-500/25">
              <Sparkles className="w-8 h-8 text-white" />
            </div>
            <CardTitle className="text-white text-2xl">Activate Premium</CardTitle>
            <CardDescription className="text-slate-400">
              Enter your activation code to unlock premium features
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div>
              <label className="text-sm font-medium text-slate-300 mb-2 block">
                Activation Code
              </label>
              <Input
                placeholder="XXX-XXXXXXXXXXXX"
                value={activationCode}
                onChange={(e) => setActivationCode(e.target.value.toUpperCase())}
                className="bg-slate-900 border-slate-600 text-white text-center tracking-widest font-mono text-lg"
              />
              <p className="text-slate-500 text-xs mt-2">
                Format: BAS-XXXXXXXXXXXX, PRO-XXXXXXXXXXXX, or ENT-XXXXXXXXXXXX
              </p>
            </div>
            
            <Button
              onClick={handleActivate}
              disabled={isLoading}
              className="w-full bg-gradient-to-r from-amber-500 to-orange-600 hover:from-amber-600 hover:to-orange-700"
            >
              {isLoading ? (
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
              ) : (
                <Sparkles className="w-4 h-4 mr-2" />
              )}
              Activate
            </Button>
            
            <Button
              variant="ghost"
              onClick={() => setShowActivation(false)}
              className="w-full text-slate-400"
            >
              Skip for now
            </Button>
          </CardContent>
        </Card>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="w-20 h-20 bg-gradient-to-br from-blue-500 to-purple-600 rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg shadow-blue-500/25">
            <Sparkles className="w-10 h-10 text-white" />
          </div>
          <h1 className="text-3xl font-bold text-white mb-2">MCA</h1>
          <p className="text-slate-400">Multi-Caster Assistant for TikTok Live</p>
        </div>

        <Card className="bg-slate-800/50 border-slate-700 backdrop-blur">
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="grid w-full grid-cols-2 bg-slate-900">
              <TabsTrigger value="login">Login</TabsTrigger>
              <TabsTrigger value="register">Register</TabsTrigger>
            </TabsList>

            <TabsContent value="login" className="mt-4">
              <CardContent className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-slate-300 mb-2 block">
                    Email
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                    <Input
                      type="email"
                      placeholder="your@email.com"
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      className="pl-10 bg-slate-900 border-slate-600 text-white"
                      onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
                    />
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-slate-300 mb-2 block">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                    <Input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="Enter your password"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      className="pl-10 pr-10 bg-slate-900 border-slate-600 text-white"
                      onKeyDown={(e) => e.key === 'Enter' && handleLogin()}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <Button
                  onClick={handleLogin}
                  disabled={isLoading}
                  className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
                >
                  {isLoading ? (
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  ) : null}
                  Login
                </Button>

                <div className="text-center">
                  <button
                    onClick={() => toast({ title: 'Coming soon', description: 'Password reset feature will be available soon' })}
                    className="text-sm text-blue-400 hover:text-blue-300"
                  >
                    Forgot password?
                  </button>
                </div>
              </CardContent>
            </TabsContent>

            <TabsContent value="register" className="mt-4">
              <CardContent className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-slate-300 mb-2 block">
                    Email
                  </label>
                  <div className="relative">
                    <Mail className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                    <Input
                      type="email"
                      placeholder="your@email.com"
                      value={registerEmail}
                      onChange={(e) => setRegisterEmail(e.target.value)}
                      className="pl-10 bg-slate-900 border-slate-600 text-white"
                    />
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-slate-300 mb-2 block">
                    Nickname (optional)
                  </label>
                  <div className="relative">
                    <User className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                    <Input
                      type="text"
                      placeholder="Your display name"
                      value={registerNickname}
                      onChange={(e) => setRegisterNickname(e.target.value)}
                      className="pl-10 bg-slate-900 border-slate-600 text-white"
                    />
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-slate-300 mb-2 block">
                    Password
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                    <Input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="At least 8 characters"
                      value={registerPassword}
                      onChange={(e) => setRegisterPassword(e.target.value)}
                      className="pl-10 pr-10 bg-slate-900 border-slate-600 text-white"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                    >
                      {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                <div>
                  <label className="text-sm font-medium text-slate-300 mb-2 block">
                    Confirm Password
                  </label>
                  <div className="relative">
                    <Lock className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-slate-500" />
                    <Input
                      type={showPassword ? 'text' : 'password'}
                      placeholder="Confirm your password"
                      value={registerConfirmPassword}
                      onChange={(e) => setRegisterConfirmPassword(e.target.value)}
                      className="pl-10 bg-slate-900 border-slate-600 text-white"
                      onKeyDown={(e) => e.key === 'Enter' && handleRegister()}
                    />
                  </div>
                </div>

                <Button
                  onClick={handleRegister}
                  disabled={isLoading}
                  className="w-full bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700"
                >
                  {isLoading ? (
                    <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  ) : null}
                  Register
                </Button>
              </CardContent>
            </TabsContent>
          </Tabs>
        </Card>

        <div className="text-center mt-6 text-slate-500 text-sm">
          <p>By using MCA, you agree to our Terms of Service and Privacy Policy</p>
        </div>
      </div>
    </div>
  )
}
