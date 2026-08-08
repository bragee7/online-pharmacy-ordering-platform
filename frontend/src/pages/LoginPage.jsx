import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const from = location.state?.from?.pathname || '/dashboard'

  if (isAuthenticated) navigate('/dashboard', { replace: true })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    const { user, error: err } = await login(email, password)
    setSubmitting(false)
    if (err) {
      setError(err)
      return
    }
    navigate(from, { replace: true })
  }

  return (
    <div className="container py-5" style={{ maxWidth: '480px' }}>
      <div className="card shadow-sm border-0">
        <div className="card-body p-4">
          <h1 className="h3 mb-1">Welcome back</h1>
          <p className="text-muted mb-4">Log in to your pharmacy account</p>

          {error && <div className="alert alert-danger py-2 small">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label" htmlFor="email">Email</label>
              <input id="email" type="email" className="form-control" required autoFocus
                value={email} onChange={(e) => setEmail(e.target.value)} placeholder="you@example.com" />
            </div>
            <div className="mb-4">
              <label className="form-label" htmlFor="password">Password</label>
              <input id="password" type="password" className="form-control" required
                value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" />
            </div>
            <button className="btn btn-success w-100" disabled={submitting}>
              {submitting ? 'Logging in…' : 'Log in'}
            </button>
          </form>

          <div className="mt-4 pt-3 border-top text-center">
            <span className="text-muted small">No account yet?</span>{' '}
            <Link to="/register" className="link-success small fw-semibold">Register</Link>
          </div>

          <div className="alert alert-light border mt-4 mb-0 small">
            <strong>Demo accounts</strong>
            <div className="mt-1">
              admin@example.com / Admin@123 (Admin)<br />
              pharmacist@example.com / Pharmacist@123 (Pharmacist)<br />
              customer@example.com / Customer@123 (Customer)
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}