/* medicines.html: search / filter / sort / pagination */
let page = 0; const size = 12;
async function loadCats() {
  try { const c = await MedAPI.getCategories(); const list = c.content || c || [];
    document.getElementById('category').innerHTML = '<option value="">All Categories</option>' + list.map(x => '<option value="' + x.id + '">' + x.name + '</option>').join('');
  } catch {}
}
async function load() {
  const g = document.getElementById('grid'); g.innerHTML = '<p>Loading…</p>';
  const params = { keyword: document.getElementById('q').value, categoryId: document.getElementById('category').value, page, size, sort: document.getElementById('sort').value };
  if (document.getElementById('rx').checked) params.rx = true;
  try {
    const p = await MedAPI.getMedicines(params);
    const list = p.content || [];
    g.innerHTML = list.length ? list.map(medCard).join('') : '<p>No results.</p>';
    document.getElementById('page-info').textContent = 'Page ' + ((p.number || 0) + 1) + ' / ' + (p.totalPages || 1) + ' (' + (p.totalElements || list.length) + ' items)';
  } catch (e) { g.innerHTML = '<p>' + e.message + '</p>'; }
}
document.addEventListener('DOMContentLoaded', () => {
  loadCats().then(load);
  document.getElementById('search-btn').onclick = () => { page = 0; load(); };
  document.getElementById('q').onkeydown = e => { if (e.key === 'Enter') { page = 0; load(); } };
  document.getElementById('prev').onclick = () => { if (page > 0) { page--; load(); } };
  document.getElementById('next').onclick = () => { page++; load(); };
});
