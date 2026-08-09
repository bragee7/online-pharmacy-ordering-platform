import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api, getErrorMessage } from '../api'

function MedicineCard({ medicine }) {
  return (
    <div className="col">
      <div className="card h-100 shadow-sm border-0">
        <div className="card-body d-flex flex-column">
          <div className="d-flex justify-content-between align-items-start">
            <div>
              <span className="badge text-bg-success mb-2">
                {medicine.categoryName || 'Uncategorised'}
              </span>
            </div>
            {medicine.prescriptionRequired ? (
              <span className="badge text-bg-warning">Rx required</span>
            ) : (
              <span className="badge text-bg-info">OTC</span>
            )}
          </div>
          <h5 className="card-title mb-1">{medicine.name}</h5>
          <div className="text-muted small mb-2">
            {medicine.genericName && <div>{medicine.genericName}</div>}
            {medicine.manufacturer && <div>{medicine.manufacturer}</div>}
          </div>
          <p className="card-text text-muted small flex-grow-1">
            {medicine.description ? `${medicine.description.slice(0, 110)}…` : 'No description available.'}
          </p>
          <div className="d-flex justify-content-between align-items-center mt-2">
            <span className="fs-5 fw-bold text-success">${Number(medicine.price).toFixed(2)}</span>
            {medicine.inStock ? (
              <span className="badge text-bg-success">In stock · {medicine.availableQuantity}</span>
            ) : (
              <span className="badge text-bg-danger">Out of stock</span>
            )}
          </div>
          <Link to={`/medicines/${medicine.id}`} className="btn btn-outline-success w-100 mt-3">
            View details
          </Link>
        </div>
      </div>
    </div>
  )
}

export default function HomePage() {
  const [featured, setFeatured] = useState([])
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/medicines', { params: { size: 6, sort: 'name,asc' } })
      .then((res) => setFeatured(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)))
  }, [])

  return (
    <div>
      <section className="py-5 text-white" style={{ background: 'linear-gradient(135deg, #198754 0%, #0d6efd 100%)' }}>
        <div className="container py-4">
          <div className="row align-items-center">
            <div className="col-lg-7">
              <h1 className="display-4 fw-bold mb-3">Your health, delivered.</h1>
              <p className="lead mb-4">
                Browse genuine medicines, get your prescriptions filled and order online —
                delivered to your door.
              </p>
              <Link to="/medicines" className="btn btn-light btn-lg fw-semibold me-2">
                <i className="bi bi-search me-2" />Browse the catalogue
              </Link>
              <Link to="/register" className="btn btn-outline-light btn-lg">
                Create an account
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section className="container py-5">
        <div className="d-flex justify-content-between align-items-center mb-4">
          <h2 className="h3 mb-0">Popular medicines</h2>
          <Link to="/medicines" className="link-success fw-semibold">See all &rarr;</Link>
        </div>

        {error && <div className="alert alert-danger">{error}</div>}

        {!error && featured.length === 0 && (
          <div className="card shadow-sm border-0">
            <div className="card-body text-center py-5 text-muted">
              <i className="bi bi-box-seam display-5 d-block mb-3" />
              The catalogue is empty. Ask an admin to add medicines.
            </div>
          </div>
        )}

        <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 g-4">
          {featured.map((m) => <MedicineCard key={m.id} medicine={m} />)}
        </div>
      </section>
    </div>
  )
}