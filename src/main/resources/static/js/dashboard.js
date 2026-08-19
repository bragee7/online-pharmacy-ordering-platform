/* dashboard.html — role-gated single page */
requireAuth();
const role = (() => { try { return JSON.parse(localStorage.getItem('medicare_user'))?.role || 'CUSTOMER'; } catch { return 'CUSTOMER'; } })();
document.addEventListener('DOMContentLoaded', async () => {
  document.getElementById('dash-role').textContent = 'Logged in as ' + role;
  document.getElementById('dash-title').textContent = role + ' Dashboard';
  if (role === 'ADMIN') { document.getElementById('admin-view').style.display = ''; loadAdmin(); }
  else if (role === 'PHARMACIST') { document.getElementById('pharma-view').style.display = ''; loadPharma(); }
  else { document.getElementById('customer-view').style.display = ''; loadCustomer(); }
});
async function loadAdmin() {
  try {
    const d = await MedAPI.adminDashboard();
    const s = d.stats || {};
    document.getElementById('stats').innerHTML = Object.entries(s).map(([k, v]) => '<div class="stat"><b>' + v + '</b>' + k + '</div>').join('') || '<p>No stats</p>';
    document.getElementById('admin-orders').innerHTML = (d.recentOrders || []).map(o => '<tr><td>' + (o.orderNumber || o.id) + '</td><td><span class="badge status-' + o.status + '">' + o.status + '</span></td><td>' + formatCurrency(o.totalAmount || o.total) + '</td><td><button class="btn small secondary" onclick="setStatus(' + o.id + ')">Advance</button></td></tr>').join('');
    document.getElementById('admin-low').innerHTML = (d.lowStock || []).map(m => '<div class="card"><h3>' + m.name + '</h3><span class="badge stock-low">Stock: ' + (m.stockQuantity ?? m.stock) + '</span></div>').join('') || '<p>Stock OK</p>';
    document.getElementById('admin-audits').innerHTML = (d.recentAudits || []).map(a => '<tr><td>' + (a.action || a.event) + '</td><td>' + (a.username || a.user || '') + '</td><td>' + (a.createdAt || a.timestamp || '') + '</td></tr>').join('');
  } catch (e) { toast(e.message); try { const l = await MedAPI.lowStock(); document.getElementById('admin-low').innerHTML = (Array.isArray(l) ? l : []).map(m => '<div class="card"><h3>' + m.name + '</h3></div>').join(''); } catch {} }
}
async function loadPharma() {
  try { const rx = await MedAPI.pendingPrescriptions('PENDING'); const list = Array.isArray(rx) ? rx : (rx.content || []);
    document.getElementById('pharma-rx').innerHTML = list.length ? list.map(p => '<div class="card"><h3>Rx #' + p.id + '</h3><span class="badge status-' + p.status + '">' + p.status + '</span><div><button class="btn small" onclick="reviewRx(' + p.id + ',true)">Approve</button> <button class="btn small danger" onclick="reviewRx(' + p.id + ',false)">Reject</button></div></div>').join('') : '<p>No pending prescriptions.</p>';
  } catch (e) { toast(e.message); }
  try { const l = await MedAPI.lowStock(); const list = Array.isArray(l) ? l : (l.content || []);
    document.getElementById('pharma-low').innerHTML = list.length ? list.map(m => '<div class="card"><h3>' + m.name + '</h3><span class="badge stock-low">Stock: ' + (m.stockQuantity ?? m.stock) + '</span></div>').join('') : '<p>Stock OK</p>';
  } catch (e) { document.getElementById('pharma-low').innerHTML = '<p>' + e.message + '</p>'; }
  try { const o = await MedAPI.myOrders(); const list = Array.isArray(o) ? o : (o.content || []);
    document.getElementById('pharma-orders').innerHTML = list.map(x => '<tr><td>' + (x.orderNumber || x.id) + '</td><td><span class="badge status-' + x.status + '">' + x.status + '</span></td><td><button class="btn small secondary" onclick="setStatus(' + x.id + ')">Advance</button></td></tr>').join('');
  } catch {}
}
async function loadCustomer() {
  try { const o = await MedAPI.myOrders(); const list = Array.isArray(o) ? o : (o.content || []);
    const total = list.reduce((s, x) => s + (x.totalAmount || x.total || 0), 0);
    document.getElementById('cust-stats').innerHTML = '<div class="stat"><b>' + list.length + '</b>Orders</div><div class="stat"><b>' + formatCurrency(total) + '</b>Total spent</div>';
    document.getElementById('cust-orders').innerHTML = list.slice(0, 6).map(x => '<div class="card"><h3>' + (x.orderNumber || x.id) + '</h3><span class="badge status-' + x.status + '">' + x.status + '</span><p class="muted">' + formatCurrency(x.totalAmount || x.total) + '</p></div>').join('') || '<p>No orders.</p>';
  } catch (e) { toast(e.message); }
}
const FLOW = ['PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED'];
async function setStatus(id) {
  const s = prompt('New status (CONFIRMED/SHIPPED/DELIVERED/CANCELLED):', 'CONFIRMED'); if (!s) return;
  try { await MedAPI.updateOrderStatus(id, s.toUpperCase()); toast('Order updated'); location.reload(); }
  catch (e) { try { await MedAPI.updateDelivery(id, s.toUpperCase()); toast('Delivery updated'); location.reload(); } catch (e2) { toast(e.message); } }
}
async function reviewRx(id, ok) {
  const reason = ok ? null : (prompt('Rejection reason:') || 'Not valid');
  try { await MedAPI.reviewPrescription(id, ok, reason); toast(ok ? 'Approved' : 'Rejected'); location.reload(); } catch (e) { toast(e.message); }
}
window.setStatus = setStatus; window.reviewRx = reviewRx;
