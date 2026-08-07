import { Navigate, Route, Routes, useLocation } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import HomePage from './pages/HomePage.jsx'
import CataloguePage from './pages/CataloguePage.jsx'
import MedicineDetailPage from './pages/MedicineDetailPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import DashboardPage from './pages/DashboardPage.jsx'
import ManageMedicinesPage from './pages/admin/ManageMedicinesPage.jsx'
import ManageCategoriesPage from './pages/admin/ManageCategoriesPage.jsx'
import { useAuth } from './auth/AuthContext.jsx'

function RequireAuth({ admin, children }) {
  const { isAuthenticated, isAdmin } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  if (admin && !isAdmin) {
    return <Navigate to="/dashboard" replace />
  }
  return children
}

function Shell({ children }) {
  return (
    <>
      <Navbar />
      <main>{children}</main>
      <footer className="border-top mt-5 py-4 text-center text-muted small bg-white">
        &copy; {new Date().getFullYear()} MediMart Pharmacy — online pharmacy ordering platform
      </footer>
    </>
  )
}

export default function App() {
  return (
    <Shell>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/medicines" element={<CataloguePage />} />
        <Route path="/medicines/:id" element={<MedicineDetailPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route path="/dashboard" element={
          <RequireAuth><DashboardPage /></RequireAuth>
        } />

        <Route path="/admin/medicines" element={
          <RequireAuth admin><ManageMedicinesPage /></RequireAuth>
        } />
        <Route path="/admin/categories" element={
          <RequireAuth admin><ManageCategoriesPage /></RequireAuth>
        } />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Shell>
  )
}