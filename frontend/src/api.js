import axios from 'axios'

export const TOKEN_KEY = 'opharma_token'
export const USER_KEY = 'opharma_user'

export const api = axios.create({ baseURL: '/api' })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export function getErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error.response && error.response.data
  if (data) {
    if (data.fieldErrors) {
      const details = Object.entries(data.fieldErrors)
        .map(([field, msg]) => `${field}: ${msg}`)
        .join(', ')
      return data.message ? `${data.message} (${details})` : details
    }
    if (data.message) return data.message
  }
  if (error.code === 'ERR_NETWORK') {
    return 'Cannot reach the backend. Is it running on port 8081?'
  }
  return fallback
}

export function readAuth() {
  try {
    const user = JSON.parse(localStorage.getItem(USER_KEY))
    return { user, token: localStorage.getItem(TOKEN_KEY) }
  } catch {
    return { user: null, token: null }
  }
}

export function persistAuth(token, user) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
  if (user) localStorage.setItem(USER_KEY, JSON.stringify(user))
  else localStorage.removeItem(USER_KEY)
}