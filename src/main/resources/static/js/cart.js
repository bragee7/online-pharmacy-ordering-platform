/* cart.html */
requireAuth();
async function loadCart() {
  const tb = document.getElementById('cart-body');
  try {
    const cart = await MedAPI.getCart();
    const items = cart.items || cart.cartItems || [];
    if (!items.length) { tb.innerHTML = '<tr><td colspan="5">Cart is empty. <a href="medicines.html">Shop now</a></td></tr>'; }
    else tb.innerHTML = items.map(it => {
      const m = it.medicine || {}; const id = it.id || it.itemId;
      const price = it.price ?? m.price ?? 0, qty = it.quantity ?? 1;
      return '<tr><td>' + (m.name || it.medicineName || 'Item') + '</td><td>' + formatCurrency(price) + '</td>' +
        '<td><span class="qty"><button onclick="chQty(' + id + ',' + (qty - 1) + ')">−</button>' + qty + '<button onclick="chQty(' + id + ',' + (qty + 1) + ')">+</button></span></td>' +
        '<td>' + formatCurrency(price * qty) + '</td><td><button class="btn small danger" onclick="rmItem(' + id + ')">Remove</button></td></tr>';
    }).join('');
    document.getElementById('cart-total').textContent = formatCurrency(cart.totalAmount ?? cart.total ?? items.reduce((s, it) => s + ((it.price ?? it.medicine?.price ?? 0) * (it.quantity ?? 1)), 0));
  } catch (e) { tb.innerHTML = '<tr><td colspan="5">' + e.message + '</td></tr>'; }
}
async function chQty(id, q) { try { if (q <= 0) await MedAPI.removeCartItem(id); else await MedAPI.updateCartItem(id, q); loadCart(); } catch (e) { toast(e.message); } }
async function rmItem(id) { try { await MedAPI.removeCartItem(id); toast('Removed'); loadCart(); } catch (e) { toast(e.message); } }
document.addEventListener('DOMContentLoaded', () => {
  loadCart();
  document.getElementById('checkout-form').onsubmit = async e => {
    e.preventDefault();
    try { const o = await MedAPI.checkout({ shippingAddress: document.getElementById('address').value, paymentMethod: document.getElementById('pay').value });
      toast('Order placed: ' + (o.orderNumber || o.number || o.id)); loadCart(); setTimeout(() => location.href = 'orders.html', 800);
    } catch (err) { toast(err.message); }
  };
});
window.chQty = chQty; window.rmItem = rmItem;
