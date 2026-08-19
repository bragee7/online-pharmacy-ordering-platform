/* MediCare API client — same-origin, JWT in localStorage */
const API_BASE = '';
const TOKEN_KEY = 'medicare_token', USER_KEY = 'medicare_user';
const getToken = () => localStorage.getItem(TOKEN_KEY);
const setToken = t => t ? localStorage.setItem(TOKEN_KEY, t) : localStorage.removeItem(TOKEN_KEY);
const getUser = () => { try { return JSON.parse(localStorage.getItem(USER_KEY)); } catch { return null; } };
const setUser = u => u ? localStorage.setItem(USER_KEY, JSON.stringify(u)) : localStorage.removeItem(USER_KEY);
const clearAuth = () => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(USER_KEY); };
function authHeaders() { const t = getToken(); return t ? { 'Authorization': 'Bearer ' + t } : {}; }
async function request(method, path, body) {
  const opts = { method, headers: { ...authHeaders() } };
  if (body !== undefined) { opts.headers['Content-Type'] = 'application/json'; opts.body = JSON.stringify(body); }
  const res = await fetch(API_BASE + path, opts);
  if (res.status === 401 && !location.pathname.endsWith('login.html')) { clearAuth(); location.href = 'login.html'; throw new Error('Session expired, please login'); }
  const text = await res.text();
  let data = null; try { data = text ? JSON.parse(text) : null; } catch { data = text; }
  if (!res.ok) throw new Error((data && (data.message || data.error)) || ('HTTP ' + res.status));
  return data;
}
const unwrap = d => (d && d.success !== undefined) ? d.data : d;
const MedAPI = {
  register: b => request('POST', '/api/auth/register', b).then(unwrap),
  login: b => request('POST', '/api/auth/login', b).then(unwrap),
  me: () => request('GET', '/api/auth/me').then(unwrap),
  getMedicines: (p = {}) => { const q = new URLSearchParams(); ['keyword','categoryId','rx','page','size','sort'].forEach(k => { if (p[k] !== undefined && p[k] !== '' && p[k] !== null) q.set(k, p[k]); }); return request('GET', '/api/medicines' + (q.toString() ? '?' + q : '')); },
  getMedicine: id => request('GET', '/api/medicines/' + id).then(unwrap),
  getCategories: () => request('GET', '/api/categories').then(unwrap),
  getCart: () => request('GET', '/api/cart').then(unwrap),
  addToCart: (medicineId, quantity = 1) => request('POST', '/api/cart/items', { medicineId, quantity }).then(unwrap),
  updateCartItem: (itemId, quantity) => request('PUT', '/api/cart/items/' + itemId, { quantity }).then(unwrap),
  removeCartItem: itemId => request('DELETE', '/api/cart/items/' + itemId),
  checkout: b => request('POST', '/api/orders/checkout', b).then(unwrap),
  myOrders: () => request('GET', '/api/orders/my').then(unwrap),
  getOrderByNumber: n => request('GET', '/api/orders/number/' + encodeURIComponent(n)).then(unwrap),
  cancelOrder: id => request('POST', '/api/orders/' + id + '/cancel').then(unwrap),
  uploadPrescription: async file => { const fd = new FormData(); fd.append('file', file); const res = await fetch(API_BASE + '/api/prescriptions/upload', { method: 'POST', headers: { ...authHeaders() }, body: fd }); if (res.status === 401) { clearAuth(); location.href = 'login.html'; throw new Error('Login required'); } const j = await res.json().catch(() => null); if (!res.ok) throw new Error((j && j.message) || 'Upload failed'); return unwrap(j); },
  myPrescriptions: () => request('GET', '/api/prescriptions/my').then(unwrap),
  adminDashboard: () => request('GET', '/api/admin/dashboard').then(unwrap),
  lowStock: () => request('GET', '/api/inventory/low-stock').then(unwrap),
  pendingPrescriptions: (status = 'PENDING') => request('GET', '/api/prescriptions?status=' + status).then(unwrap),
  reviewPrescription: (id, approve, rejectionReason) => request('PUT', '/api/prescriptions/' + id + '/review', { approve, rejectionReason }).then(unwrap),
  updateOrderStatus: (id, status) => request('PATCH', '/api/orders/' + id + '/status', { status }).then(unwrap),
  updateDelivery: (orderId, status) => request('PUT', '/api/deliveries/order/' + orderId, { status }).then(unwrap),
  health: () => fetch('/health').then(r => r.text()),
};
window.MedAPI = MedAPI;
