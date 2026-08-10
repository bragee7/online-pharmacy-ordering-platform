import { useCallback, useEffect, useState } from 'react'
import { api, getErrorMessage } from '../../api'

const emptyForm = { name: '', description: '' }

function CategoryForm({ initial, onSubmit, onCancel, busy }) {
  const [form, setForm] = useState(initial)
  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }))
  const setCheck = (e) => setForm((f) => ({ ...f, active: e.target.checked }))

  const handleSubmit = (e) => {
    e.preventDefault()
    onSubmit(form)
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="mb-3">
        <label className="form-label">Name *</label>
        <input className="form-control" required maxLength={100} value={form.name} onChange={set('name')} />
      </div>
      <div className="mb-3">
        <label className="form-label">Description</label>
        <textarea className="form-control" rows="3" maxLength={500} value={form.description || ''} onChange={set('description')} />
      </div>
      {initial.id !== undefined && (
        <div className="form-check mb-3">
          <input className="form-check-input" type="checkbox" id="catActive" checked={form.active !== false} onChange={setCheck} />
          <label className="form-check-label" htmlFor="catActive">Active (visible everywhere)</label>
        </div>
      )}
      <div className="d-flex justify-content-end gap-2">
        <button type="button" className="btn btn-outline-secondary" onClick={onCancel}>Cancel</button>
        <button type="submit" className="btn btn-success" disabled={busy}>
          {busy ? 'Saving…' : initial.id !== undefined ? 'Save changes' : 'Create category'}
        </button>
      </div>
    </form>
  )
}

export default function ManageCategoriesPage() {
  const [categories, setCategories] = useState([])
  const [editing, setEditing] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  const load = useCallback(() => {
    api.get('/categories/all')
      .then((res) => setCategories(res.data))
      .catch((err) => setError(getErrorMessage(err)))
  }, [])

  useEffect(load, [load])

  const openCreate = () => setEditing({ ...emptyForm })
  const openEdit = (c) => setEditing({ id: c.id, name: c.name, description: c.description || '', active: c.active })

  const handleSubmit = async (payload) => {
    setBusy(true)
    setError('')
    setNotice('')
    try {
      if (editing.id !== undefined) {
        await api.put(`/categories/${editing.id}`, payload)
        setNotice('Category updated.')
      } else {
        await api.post('/categories', payload)
        setNotice('Category created.')
      }
      setEditing(null)
      load()
    } catch (err) {
      setError(getErrorMessage(err))
    } finally {
      setBusy(false)
    }
  }

  const handleDelete = async (c) => {
    if (!window.confirm(`Deactivate category "${c.name}"?`)) return
    setBusy(true)
    setError('')
    setNotice('')
    try {
      await api.delete(`/categories/${c.id}`)
      setNotice('Category deactivated.')
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
        <h1 className="h3 mb-0">Manage categories</h1>
        <button className="btn btn-success" onClick={openCreate}>
          <i className="bi bi-plus-lg me-1" />New category
        </button>
      </div>

      {error && <div className="alert alert-danger py-2 small">{error}</div>}
      {notice && <div className="alert alert-success py-2 small">{notice}</div>}

      <div className="card shadow-sm border-0">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead className="table-light">
              <tr>
                <th style={{ width: '60px' }}>ID</th>
                <th>Name</th>
                <th>Description</th>
                <th className="text-center">Active</th>
                <th className="text-end" style={{ width: '140px' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.length === 0 && (
                <tr><td colSpan="5" className="text-center text-muted py-4">No categories yet.</td></tr>
              )}
              {categories.map((c) => (
                <tr key={c.id}>
                  <td>{c.id}</td>
                  <td className="fw-semibold">{c.name}</td>
                  <td className="text-muted small">{c.description || '—'}</td>
                  <td className="text-center">
                    {c.active ? <span className="badge text-bg-success">Yes</span> : <span className="badge text-bg-secondary">No</span>}
                  </td>
                  <td className="text-end">
                    <button className="btn btn-outline-success btn-sm me-1" onClick={() => openEdit(c)}>
                      <i className="bi bi-pencil" />
                    </button>
                    <button className="btn btn-outline-danger btn-sm" disabled={busy} onClick={() => handleDelete(c)}>
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
        <>
          <div className="modal show d-block" tabIndex="-1" role="dialog">
            <div className="modal-dialog modal-dialog-centered">
              <div className="modal-content">
                <div className="modal-header">
                  <h5 className="modal-title">{editing.id !== undefined ? 'Edit category' : 'New category'}</h5>
                  <button type="button" className="btn-close" onClick={() => setEditing(null)} />
                </div>
                <div className="modal-body">
                  <CategoryForm
                    initial={editing}
                    onSubmit={handleSubmit}
                    onCancel={() => setEditing(null)}
                    busy={busy}
                  />
                </div>
              </div>
            </div>
          </div>
          <div className="modal-backdrop show" />
        </>
      )}
    </div>
  )
}