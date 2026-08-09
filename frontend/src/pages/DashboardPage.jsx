import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const roleLabel = (role) => {
  switch (role) {
    case 'ADMIN': return 'Admin'
    case 'PHARMACIST': return 'Pharmacist'
    default: return 'Customer'
  }
}

export default function DashboardPage() {
  const { user, isAdmin, isPharmacist } = useAuth()

  const links = []
  if (isAdmin) {
    links.push(
      { to: '/admin/medicines', icon: 'bi-pencil-square', title: 'Manage medicines', desc: 'Add, edit and deactivate medicines.' },
      { to: '/admin/categories', icon: 'bi-tags', title: 'Manage categories', desc: 'Organise medicines into categories.' },
    )
  }
  if (isPharmacist) {
    links.push(
      { to: '/medicines', icon: 'bi-search', title: 'Browse catalogue', desc: 'Consult medicines, stock and prices.' },
    )
  }

  return (
    <div className="container py-4">
      <h1 className="h3 mb-4">Dashboard</h1>

      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body p-4">
          <div className="d-flex align-items-center gap-3">
            <div className="rounded-circle bg-success text-white d-flex align-items-center justify-content-center"
              style={{ width: '64px', height: '64px', fontSize: '1.5rem', fontWeight: 600 }}>
              {user.name.charAt(0).toUpperCase()}
            </div>
            <div>
              <h2 className="h5 mb-0">{user.name}</h2>
              <div className="text-muted small">{user.email}{user.phone ? ` · ${user.phone}` : ''}</div>
              <span className="badge text-bg-success mt-1">{roleLabel(user.role)}</span>
            </div>
          </div>
        </div>
      </div>

      {links.length > 0 && (
        <div className="row g-3">
          {links.map((l) => (
            <div className="col-md-6" key={l.to}>
              <Link to={l.to} className="card text-decoration-none shadow-sm border-0 h-100">
                <div className="card-body d-flex gap-3">
                  <i className={`bi ${l.icon} fs-3 text-success`} />
                  <div>
                    <h3 className="h6 mb-1">{l.title}</h3>
                    <p className="text-muted small mb-0">{l.desc}</p>
                  </div>
                </div>
              </Link>
            </div>
          ))}
        </div>
      )}

      <div className="mt-4">
        <Link to="/medicines" className="btn btn-outline-success">
          <i className="bi bi-capsule me-2" />Browse the catalogue
        </Link>
      </div>
    </div>
  )
}