/* orders.html */
requireAuth();
function orderCard(o) {
  return '<div class="card"><h3>' + (o.orderNumber || o.number || ('#' + o.id)) + '</h3>' +
    '<span class="badge status-' + o.status + '">' + o.status + '</span>' +
    '<p class="muted">Total: <b>' + formatCurrency(o.totalAmount || o.total) + '</b></p>' +
    (o.paymentStatus ? '<p class="muted">Payment: ' + o.paymentStatus + '</p>' : '') +
    (o.trackingNumber ? '<p class="muted">Tracking: <b>' + o.trackingNumber + '</b></p>' : '') +
    (o.shippingAddress ? '<p class="muted">' + o.shippingAddress + '</p>' : '') +
    ((o.status === 'PLACED' || o.status === 'PENDING' || o.status === 'CONFIRMED') ? '<button class="btn small danger" onclick="cancelOrd(' + o.id + ')">Cancel</button>' : '') + '</div>';
}
async function loadOrders() {
  const g = document.getElementById('orders-grid'); g.innerHTML = '<p>Loading…</p>';
  try { const r = await MedAPI.myOrders(); const list = Array.isArray(r) ? r : (r.content || r.orders || []);
    g.innerHTML = list.length ? list.map(orderCard).join('') : '<p>No orders yet.</p>';
  } catch (e) { g.innerHTML = '<p>' + e.message + '</p>'; }
}
async function cancelOrd(id) { if (!confirm('Cancel this order?')) return; try { await MedAPI.cancelOrder(id); toast('Order cancelled'); loadOrders(); } catch (e) { toast(e.message); } }
document.addEventListener('DOMContentLoaded', () => {
  loadOrders();
  document.getElementById('lookup-btn').onclick = async () => {
    const n = document.getElementById('lookup').value.trim(); if (!n) return;
    try { const o = await MedAPI.getOrderByNumber(n); document.getElementById('lookup-result').innerHTML = orderCard(o); }
    catch (e) { toast(e.message); }
  };
});
window.cancelOrd = cancelOrd;
