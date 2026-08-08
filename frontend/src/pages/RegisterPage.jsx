import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  if (isAuthenticated) navigate('/dashboard', { replace: true })

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    const { user, error: err } = await register(form)
    setSubmitting(false)
    if (err) {
      setError(err)
      return
    }
    navigate('/dashboard', { replace: true })
  }

  return (
    <div className="container py-5" style={{ maxWidth: '480px' }}>
      <div className="card shadow-sm border-0">
        <div className="card-body p-4">
          <h1 className="h3 mb-1">Create your account</h1>
          <p className="text-muted mb-4">Register as a customer and start ordering</p>

          {error && <div className="alert alert-danger py-2 small">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label" htmlFor="name">Full name</label>
              <input id="name" name="name" className="form-control" required autoFocus
                value={form.name} onChange={handleChange} placeholder="Jane Doe" />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="email">Email</label>
              <input id="email" name="email" type="email" className="form-control" required
                value={form.email} onChange={handleChange} placeholder="you@example.com" />
            </div>
            <div className="mb-3">
              <label className="form-label" htmlFor="password">Password</label>
              <input id="password" name="password" type="password" className="form-control" required
                minLength={8} value={form.password} onChange={handleChange} placeholder="Min. 8 characters with a letter and digit" />
              <small className="text-muted">Must be 8+ characters and contain a letter and a digit.</small>
            </div>
            <div className="mb-4">
              <label className="form-label" htmlFor="phone">Phone (optional)</label>
              <input id="phone" name="phone" className="form-control"
                value={form.phone} onChange={handleChange} placeholder="+977 98 0000 0000" />
            </div>
            <button className="btn btn-success w-100" disabled={submitting}>
              {submitting ? 'Creating account…' : 'Create account'}
            </button>
          </form>

          <div className="mt-4 pt-3 border-top text-center">
            <span className="text-muted small">Already registered?</span>{' '}
            <Link to="/login" className="link-success small fw-semibold">Log in</Link>
          </div>
        </div>
      </div>
    </div>
  )
}