import { NavLink, Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

const roleLabel = (role) => {
  switch (role) {
    case 'ADMIN': return 'Admin'
    case 'PHARMACIST': return 'Pharmacist'
    default: return 'Customer'
  }
}

export default function Navbar() {
  const { user, isAuthenticated, isAdmin, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const linkClass = ({ isActive }) =>
    `nav-link ${isActive ? 'active fw-semibold' : ''}`

  return (
    <nav className="navbar navbar-expand-lg navbar-dark bg-success shadow-sm">
      <div className="container">
        <Link to="/" className="navbar-brand d-flex align-items-center gap-2">
          <i className="bi bi-capsule" />
          <span>MediMart Pharmacy</span>
        </Link>

        <div className="d-flex align-items-center gap-2 order-lg-3 ms-auto ms-lg-0">
          {isAuthenticated && user ? (
            <div className="d-flex align-items-center gap-2">
              <span className="text-white small d-none d-sm-inline">
                <i className="bi bi-person-circle me-1" />
                {user.name}
                <span className="badge text-bg-light text-success ms-2">{roleLabel(user.role)}</span>
              </span>
              <button
                type="button"
                className="btn btn-light btn-sm fw-semibold"
                onClick={handleLogout}
                title="Log out"
              >
                <i className="bi bi-box-arrow-right me-1" />Log out
              </button>
            </div>
          ) : (
            <div className="d-flex align-items-center gap-2">
              <Link to="/login" className="btn btn-sm fw-semibold text-white">Log in</Link>
              <Link to="/register" className="btn btn-light btn-sm fw-semibold">Register</Link>
            </div>
          )}
        </div>

        <button className="navbar-toggler order-lg-4" type="button" data-bs-toggle="collapse"
          data-bs-target="#mainNav" aria-controls="mainNav" aria-expanded="false"
          aria-label="Toggle navigation">
          <span className="navbar-toggler-icon" />
        </button>

        <div className="collapse navbar-collapse order-lg-2" id="mainNav">
          <ul className="navbar-nav me-auto mb-2 mb-lg-0">
            <li className="nav-item">
              <NavLink to="/" end className={linkClass}><i className="bi bi-house me-1" />Home</NavLink>
            </li>
            <li className="nav-item">
              <NavLink to="/medicines" className={linkClass}><i className="bi bi-search me-1" />Catalogue</NavLink>
            </li>
            {isAuthenticated && (
              <li className="nav-item">
                <NavLink to="/dashboard" className={linkClass}><i className="bi bi-person-circle me-1" />Dashboard</NavLink>
              </li>
            )}
            {isAdmin && (
              <>
                <li className="nav-item">
                  <NavLink to="/admin/medicines" className={linkClass}>
                    <i className="bi bi-pencil-square me-1" />Manage Medicines
                  </NavLink>
                </li>
                <li className="nav-item">
                  <NavLink to="/admin/categories" className={linkClass}>
                    <i className="bi bi-tags me-1" />Categories
                  </NavLink>
                </li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  )
}