/* auth.js — login.html + register.html */
function saveSession(d) { localStorage.setItem('medicare_token', d.token); if (d.user) localStorage.setItem('medicare_user', JSON.stringify(d.user)); }
document.addEventListener('DOMContentLoaded', () => {
  const lf = document.getElementById('login-form');
  if (lf) lf.onsubmit = async e => {
    e.preventDefault();
    try { const d = await MedAPI.login({ email: document.getElementById('email').value, password: document.getElementById('password').value });
      saveSession(d); toast('Welcome back!'); location.href = 'index.html';
    } catch (err) { toast(err.message); }
  };
  const rf = document.getElementById('register-form');
  if (rf) rf.onsubmit = async e => {
    e.preventDefault();
    try { const d = await MedAPI.register({ name: document.getElementById('name').value, email: document.getElementById('email').value, password: document.getElementById('password').value, phone: document.getElementById('phone').value, address: document.getElementById('address').value });
      saveSession(d); toast('Account created!'); location.href = 'index.html';
    } catch (err) { toast(err.message); }
  };
});
