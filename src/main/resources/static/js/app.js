/* Shared UI: navbar auth state, toast, currency, featured */
function toast(msg) { const el = document.getElementById('toast'); if (!el) return alert(msg); el.textContent = msg; el.style.display = 'block'; clearTimeout(el._t); el._t = setTimeout(() => el.style.display = 'none', 2600); }
function formatCurrency(n) { return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(n || 0); }
function currentUser() { try { return JSON.parse(localStorage.getItem('medicare_user')); } catch { return null; } }
function logout() { localStorage.removeItem('medicare_token'); localStorage.removeItem('medicare_user'); location.href = 'index.html'; }
function requireAuth() { if (!localStorage.getItem('medicare_token')) location.href = 'login.html'; }
function userRole() { const u = currentUser(); return (u && (u.role || u.userType)) || 'CUSTOMER'; }
function renderNav() {
  const token = localStorage.getItem('medicare_token'), user = currentUser();
  document.querySelectorAll('[data-guest]').forEach(e => e.style.display = token ? 'none' : '');
  document.querySelectorAll('[data-auth]').forEach(e => e.style.display = token ? '' : 'none');
  const nu = document.getElementById('nav-user'); if (nu && user) nu.textContent = '👤 ' + (user.name || user.email) + ' (' + userRole() + ')';
  const lo = document.getElementById('logout-link'); if (lo) lo.onclick = e => { e.preventDefault(); logout(); };
}
function medCard(m) {
  const rx = m.requiresPrescription || m.rx;
  return '<div class="card">' + (m.imageUrl ? '<img src="' + m.imageUrl + '" alt="">' : '') +
    '<h3>' + (m.name || '') + '</h3><p class="muted">' + (m.manufacturer || m.brand || '') + ' · ' + (m.strength || '') + '</p>' +
    '<span class="badge ' + (rx ? 'rx' : 'otc') + '">' + (rx ? 'Rx Required' : 'OTC') + '</span>' +
    '<span class="price">' + formatCurrency(m.price) + '</span>' +
    '<button class="btn small" onclick="addToCart(' + m.id + ')">Add to Cart</button></div>';
}
async function addToCart(id) {
  if (!localStorage.getItem('medicare_token')) { location.href = 'login.html'; return; }
  try { await MedAPI.addToCart(id, 1); toast('Added to cart'); } catch (e) { toast(e.message); }
}
document.addEventListener('DOMContentLoaded', () => {
  renderNav();
  const feat = document.getElementById('featured-grid');
  if (feat && window.MedAPI) MedAPI.getMedicines({ page: 0, size: 8 }).then(p => {
    const list = p.content || p.data?.content || p.data || [];
    feat.innerHTML = list.length ? list.map(medCard).join('') : '<p>No medicines found.</p>';
  }).catch(() => feat.innerHTML = '<p class="muted">Could not load medicines. Is backend running?</p>');
});
window.toast = toast; window.logout = logout; window.requireAuth = requireAuth;
window.formatCurrency = formatCurrency; window.addToCart = addToCart;
