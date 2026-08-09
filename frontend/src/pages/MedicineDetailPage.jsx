import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api, getErrorMessage } from '../api'

export default function MedicineDetailPage() {
  const { id } = useParams()
  const [medicine, setMedicine] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    api.get(`/medicines/${id}`)
      .then((res) => setMedicine(res.data))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) {
    return (
      <div className="container py-5 text-center text-muted">
        <div className="spinner-border text-success" role="status" />
        <p className="mt-3">Loading…</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="container py-5">
        <div className="alert alert-danger">{error}</div>
        <Link to="/medicines" className="btn btn-outline-success">&larr; Back to catalogue</Link>
      </div>
    )
  }

  const rows = [
    ['Category', medicine.categoryName || 'Uncategorised'],
    ['Generic name', medicine.genericName || '—'],
    ['Brand name', medicine.brandName || '—'],
    ['Manufacturer', medicine.manufacturer || '—'],
    ['Dosage', medicine.dosageInformation || '—'],
    ['Expiry date', medicine.expiryDate || '—'],
    ['Type', medicine.prescriptionRequired ? 'Prescription required (Rx)' : 'Over the counter'] ,
  ]

  return (
    <div className="container py-4">
      <Link to="/medicines" className="link-success fw-semibold mb-3 d-inline-block">&larr; Back to catalogue</Link>
      <div className="card shadow-sm border-0">
        <div className="card-body p-4 p-md-5">
          <div className="d-flex flex-wrap justify-content-between align-items-start gap-2 mb-3">
            <div>
              <span className="badge text-bg-success mb-2">{medicine.categoryName || 'Uncategorised'}</span>
              <h1 className="h2 mb-0">{medicine.name}</h1>
              {medicine.genericName && <p className="text-muted mb-0">{medicine.genericName}</p>}
            </div>
            <div className="text-end">
              <div className="display-6 fw-bold text-success">${Number(medicine.price).toFixed(2)}</div>
              {medicine.inStock ? (
                <span className="badge text-bg-success">In stock · {medicine.availableQuantity} available</span>
              ) : (
                <span className="badge text-bg-danger">Out of stock</span>
              )}
            </div>
          </div>

          <hr />

          {medicine.description && <p className="lead">{medicine.description}</p>}

          <div className="table-responsive mt-3">
            <table className="table table-sm">
              <tbody>
                {rows.map(([k, v]) => (
                  <tr key={k}>
                    <th className="text-muted" style={{ width: '180px' }}>{k}</th>
                    <td>{v}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  )
}