import { useCallback, useEffect, useState } from 'react'
import { api, getErrorMessage } from '../../api'

const emptyForm = {
  name: '', genericName: '', brandName: '', description: '',
  categoryId: '', manufacturer: '', price: '', prescriptionRequired: false,
  dosageInformation: '', expiryDate: '',
}

function MedicineForm({ initial, categories, onSubmit, onCancel, busy }) {
  const [form, setForm] = useState(initial)
  const set = (field) => (e) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    setForm((f) => ({ ...f, [field]: value }))
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    onSubmit({ ...form, price: Number(form.price) || 0, categoryId: Number(form.categoryId) || null })
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="row g-3">
        <div className="col-md-8">
          <label className="form-label">Name *</label>
          <input className="form-control" required maxLength={150} value={form.name} onChange={set('name')} />
        </div>
        <div className="col-md-4">
          <label className="form-label">Category *</label>
          <select className="form-select" required value={form.categoryId} onChange={set('categoryId')}>
            <option value="">Select…</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </div>
        <div className="col-md-6">
          <label className="form-label">Generic name</label>
          <input className="form-control" maxLength={150} value={form.genericName} onChange={set('genericName')} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Brand name</label>
          <input className="form-control" maxLength={150} value={form.brandName} onChange={set('brandName')} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Manufacturer</label>
          <input className="form-control" maxLength={150} value={form.manufacturer} onChange={set('manufacturer')} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Price (USD) *</label>
          <input className="form-control" type="number" min="0.01" step="0.01" required value={form.price} onChange={set('price')} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Dosage information</label>
          <input className="form-control" maxLength={255} value={form.dosageInformation} onChange={set('dosageInformation')} />
        </div>
        <div className="col-md-6">
          <label className="form-label">Expiry date</label>
          <input className="form-control" type="date" value={form.expiryDate} onChange={set('expiryDate')} />
        </div>
        <div className="col-12">
          <label className="form-label">Description</label>
          <textarea className="form-control" rows="3" maxLength={2000} value={form.description} onChange={set('description')} />
        </div>
        <div className="col-12 d-flex gap-4">
          <div className="form-check">
            <input className="form-check-input" type="checkbox" id="rx" checked={form.prescriptionRequired} onChange={set('prescriptionRequired')} />
            <label className="form-check-label" htmlFor="rx">Prescription required</label>
          </div>
          {initial.id !== undefined && (
            <div className="form-check">
              <input className="form-check-input" type="checkbox" id="active" checked={form.active !== false} onChange={set('active')} />
              <label className="form-check-label" htmlFor="active">Active (visible in catalogue)</label>
            </div>
          )}
        </div>
      </div>
      <div className="d-flex justify-content-end gap-2 mt-4">
        <button type="button" className="btn btn-outline-secondary" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn btn-success" disabled={busy}>
          {busy ? 'Saving…' : initial.id !== undefined ? 'Save changes' : 'Create medicine'}
        </button>
      </div>
    </form>
  )
}

export default function ManageMedicinesPage() {
  const [medicines, setMedicines] = useState([])
  const [categories, setCategories] = useState([])
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const load = useCallback(() => {
    api.get('/medicines', { params: { size: 100, page: 0 } })
      .then((res) => setMedicines(res.data.content || []))
      .catch((err) => setError(getErrorMessage(err)))
  }, [])

  useEffect(() => {
    load()
    api.get('/categories/all')
      .then((res) => setCategories(res.data))
      .catch(() => api.get('/categories').then((res) => setCategories(res.data)).catch(() => {}))
  }, [load])

  const openCreate = () => setEditing({ ...emptyForm })
  const openEdit = (m) => setEditing({
    id: m.id,
    name: m.name,
    genericName: m.genericName || '',
    brandName: m.brandName || '',
    description: m.description || '',
    categoryId: m.categoryId || '',
    manufacturer: m.manufacturer || '',
    price: m.price,
    prescriptionRequired: m.prescriptionRequired,
    dosageInformation: m.dosageInformation || '',
    expiryDate: m.expiryDate || '',
    active: m.active,
  })

  const handleSubmit = async (payload) => {
    setBusy(true)
    setError('')
    setNotice('')
    try {
      if (editing.id !== undefined) {
        await api.put(`/medicines/${editing.id}`, payload)
        setNotice('Medicine updated.')
      } else {
        await api.post('/medicines', payload)
        setNotice('Medicine created.')
      }
      setEditing(null)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const handleDelete = async (m) => {
    if (!window.confirm(`Deactivate "${m.name}"? It is removed from the catalogue but kept for history.`)) return
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await api.delete(`/medicines/${m.id}`)
      setNotice('Medicine deactivated.')
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="container py-4">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h3 mb-0">Manage medicines</h1>
        <button className="btn btn-success" onClick={openCreate}>
          <i className="bi bi-plus-lg me-1" />New medicine
        </button>
      </div>

      {error && <div className="alert alert-danger py-2 small">{error}</div>}
      {notice && <div className="alert alert-success py-2 small">{notice}</div>}

      <div className="card shadow-sm border-0">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th>Name</th>
                <th>Category</th>
                <th className="text-end">Price</th>
                <th className="text-center">Rx</th>
                <th className="text-center">Stock</th>
                <th className="text-center">Active</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {medicines.length === 0 && (
                <tr><td colSpan="7" className="text-center text-muted py-4">No medicines yet.</td></tr>
              )}
              {medicines.map((m) => (
                <tr key={m.id}>
                  <td className="fw-semibold">{m.name}</td>
                  <td>{m.categoryName || '—'}</td>
                  <td className="text-end">${Number(m.price).toFixed(2)}</td>
                  <td className="text-center">{m.prescriptionRequired ? <span className="badge text-bg-warning">Rx</span> : <span className="badge text-bg-info">OTC</span>}</td>
                  <td className="text-center">
                    {m.inStock ? <span className="badge text-bg-success">{m.availableQuantity}</span> : <span className="badge text-bg-danger">0</span>}
                  </td>
                  <td className="text-center">
                    {m.active ? <span className="badge text-bg-success">Yes</span> : <span className="badge text-bg-secondary">No</span>}
                  </td>
                  <td className="text-end">
                    <button className="btn btn-outline-success btn-sm me-1" onClick={() => openEdit(m)}>
                      <i className="bi bi-pencil" />
                    </button>
                    <button className="btn btn-outline-danger btn-sm" disabled={busy} onClick={() => handleDelete(m)}>
                      <i className="bi bi-trash" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {editing && (
        <div className="modal show d-block" tabIndex="-1" role="dialog">
          <div className="modal-dialog modal-lg modal-dialog-centered">
            <div className="modal-content">
              <div className="modal-header">
                <h5 className="modal-title">{editing.id !== undefined ? 'Edit medicine' : 'New medicine'}</h5>
                <button type="button" className="btn-close" onClick={() => setEditing(null)} />
              </div>
              <div className="modal-body">
                <MedicineForm
                  initial={editing}
                  categories={categories}
                  onSubmit={handleSubmit}
                  onCancel={() => setEditing(null)}
                  busy={busy}
                />
              </div>
            </div>
          </div>
        </div>
      )}
      {editing && <div className="modal-backdrop show" />}
    </div>
  )
}