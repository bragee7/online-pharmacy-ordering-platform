import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { api, getErrorMessage } from '../api'

const PAGE_SIZE = 12

function Pagination({ page, totalPages, onPage }) {
  if (totalPages <= 1) return null
  const pages = []
  for (let i = 0; i < totalPages; i++) pages.push(i)
  return (
    <nav aria-label="Catalogue pagination">
      <ul className="pagination justify-content-center">
        <li className={`page-item ${page === 0 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page - 1)}>Previous</button>
        </li>
        {pages.map((p) => (
          <li key={p} className={`page-item ${p === page ? 'active' : ''}`}>
            <button className="page-link" onClick={() => onPage(p)}>{p + 1}</button>
          </li>
        ))}
        <li className={`page-item ${page === totalPages - 1 ? 'disabled' : ''}`}>
          <button className="page-link" onClick={() => onPage(page + 1)}>Next</button>
        </li>
      </ul>
    </nav>
  )
}

export default function CataloguePage() {
  const [searchParams, setSearchParams] = useSearchParams()

  const [q, setQ] = useState(searchParams.get('q') || '')
  const [categoryId, setCategoryId] = useState(searchParams.get('categoryId') || '')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [prescription, setPrescription] = useState(searchParams.get('prescriptionRequired') === 'true')
  const [inStock, setInStock] = useState(searchParams.get('inStock') === 'true')
  const [page, setPage] = useState(0)

  const [categories, setCategories] = useState([])
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0, number: 0 })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/categories')
      .then((res) => setCategories(res.data))
      .catch(() => {})
  }, [])

  useEffect(() => {
    const params = {
      size: PAGE_SIZE,
      page,
      sort: 'name,asc',
    }
    if (q.trim()) params.q = q.trim()
    if (categoryId) params.categoryId = categoryId
    if (minPrice !== '') params.minPrice = minPrice
    if (maxPrice !== '') params.maxPrice = maxPrice
    if (prescription) params.prescriptionRequired = true
    if (inStock) params.inStock = true

    setSearchParams(Object.entries(params).filter(([, v]) => v !== undefined).map(([k, v]) => [k, String(v)]),
      { replace: true })

    setLoading(true)
    setError('')
    api.get('/medicines', { params })
      .then((res) => setData(res.data))
      .catch((err) => setError(getErrorMessage(err)))
      .finally(() => setLoading(false))
  }, [q, categoryId, minPrice, maxPrice, prescription, inStock, page, setSearchParams])

  const applySearch = () => {
    setPage(0)
    setSearchParams({})
    // force refetch through state change below
  }

  return (
    <div className="container py-4">
      <h1 className="h3 mb-4">Medicine catalogue</h1>
      <p className="text-muted small mb-4">
        {loading ? 'Searching…' : `${data.totalElements} medicine${data.totalElements === 1 ? '' : 's'} found`}
      </p>

      <div className="card shadow-sm border-0 mb-4">
        <div className="card-body">
          <div className="row g-2">
            <div className="col-md-4">
              <input
                className="form-control"
                placeholder="Search name, brand, generic…"
                value={q}
                onChange={(e) => setQ(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && applySearch()}
              />
            </div>
            <div className="col-md-3">
              <select className="form-select" value={categoryId} onChange={(e) => setCategoryId(e.target.value)}>
                <option value="">All categories</option>
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>{c.name}</option>
                ))}
              </select>
            </div>
            <div className="col-md-2">
              <input className="form-control" type="number" min="0" step="0.01" placeholder="Min price"
                value={minPrice} onChange={(e) => setMinPrice(e.target.value)} />
            </div>
            <div className="col-md-3">
              <input className="form-control" type="number" min="0" step="0.01" placeholder="Max price"
                value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} />
            </div>
            <div className="col-md-3">
              <div className="form-check form-switch mt-2 ms-1">
                <input className="form-check-input" type="checkbox" id="rxFilter"
                  checked={prescription} onChange={(e) => setPrescription(e.target.checked)} />
                <label className="form-check-label small" htmlFor="rxFilter">Prescription only</label>
              </div>
              <div className="form-check form-switch mt-2 ms-1">
                <input className="form-check-input" type="checkbox" id="stockFilter"
                  checked={inStock} onChange={(e) => setInStock(e.target.checked)} />
                <label className="form-check-label small" htmlFor="stockFilter">In stock</label>
              </div>
            </div>
            <div className="col-md-2 d-flex align-items-center">
              <button className="btn btn-success w-100" onClick={applySearch}>
                <i className="bi bi-search me-1" />Search
              </button>
            </div>
          </div>
        </div>
      </div>

      {error && <div className="alert alert-danger">{error}</div>}

      {!error && data.content.length === 0 && !loading && (
        <div className="card shadow-sm border-0">
          <div className="card-body text-center py-5 text-muted">
            <i className="bi bi-search display-5 d-block mb-3" />
            No medicines match your filters. Try widening the search.
          </div>
        </div>
      )}

      <div className="row row-cols-1 row-cols-sm-2 row-cols-md-3 row-cols-lg-4 g-4">
        {data.content.map((m) => (
          <div className="col" key={m.id}>
            <div className="card h-100 shadow-sm border-0">
              <div className="card-body d-flex flex-column">
                <div className="badge text-bg-success align-self-start mb-2">{m.categoryName || 'Uncategorised'}</div>
                <h6 className="card-title mb-1">{m.name}</h6>
                <div className="text-muted small mb-2">{m.genericName}</div>
                <div className="mt-auto d-flex justify-content-between align-items-center">
                  <span className="fw-bold text-success">${Number(m.price).toFixed(2)}</span>
                  {m.prescriptionRequired
                    ? <span className="badge text-bg-warning">Rx</span>
                    : <span className="badge text-bg-info">OTC</span>}
                </div>
                <div className="mt-2">
                  {m.inStock
                    ? <span className="badge text-bg-success">In stock</span>
                    : <span className="badge text-bg-danger">Out of stock</span>}
                </div>
                <Link className="btn btn-outline-success btn-sm mt-2 w-100" to={`/medicines/${m.id}`}>Details</Link>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="mt-4">
        <Pagination page={data.number} totalPages={data.totalPages} onPage={setPage} />
      </div>
    </div>
  )
}