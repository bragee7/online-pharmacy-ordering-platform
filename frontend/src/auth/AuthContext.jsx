import { createContext, useCallback, useContext, useMemo, useState } from 'react'
import { api, getErrorMessage, persistAuth, readAuth } from '../api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [{ token, user }, setAuth] = useState(readAuth)

  const setSession = useCallback((t, u) => {
    persistAuth(t, u)
    setAuth({ token: t, user: u })
  }, [])

  const login = useCallback(async (email, password) => {
    try {
      const res = await api.post('/auth/login', { email, password })
      setSession(res.data.token, res.data.user)
      return { user: res.data.user, error: null }
    } catch (err) {
      return { user: null, error: getErrorMessage(err, 'Login failed.') }
    }
  }, [setSession])

  const register = useCallback(async (payload) => {
    try {
      const res = await api.post('/auth/register', payload)
      setSession(res.data.token, res.data.user)
      return { user: res.data.user, error: null }
    } catch (err) {
      return { user: null, error: getErrorMessage(err, 'Registration failed.') }
    }
  }, [setSession])

  const logout = useCallback(() => setSession(null, null), [setSession])

  const value = useMemo(() => ({
    user,
    token,
    isAuthenticated: Boolean(token && user),
    isAdmin: Boolean(user && user.role === 'ADMIN'),
    isPharmacist: Boolean(user && user.role === 'PHARMACIST'),
    login,
    register,
    logout,
  }), [token, user, login, register, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>')
  return ctx
}