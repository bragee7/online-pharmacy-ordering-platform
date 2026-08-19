/* prescriptions.html */
requireAuth();
async function loadRx() {
  const g = document.getElementById('rx-grid'); g.innerHTML = '<p>Loading…</p>';
  try { const r = await MedAPI.myPrescriptions(); const list = Array.isArray(r) ? r : (r.content || []);
    g.innerHTML = list.length ? list.map(p => '<div class="card"><h3>Rx #' + p.id + '</h3><span class="badge status-' + p.status + '">' + p.status + '</span>' +
      (p.fileUrl || p.filePath ? '<a href="' + (p.fileUrl || p.filePath) + '" target="_blank">View file</a>' : '') +
      (p.rejectionReason ? '<p class="muted">Reason: ' + p.rejectionReason + '</p>' : '') +
      (p.createdAt ? '<p class="muted">' + p.createdAt + '</p>' : '') + '</div>').join('') : '<p>No prescriptions yet.</p>';
  } catch (e) { g.innerHTML = '<p>' + e.message + '</p>'; }
}
document.addEventListener('DOMContentLoaded', () => {
  loadRx();
  document.getElementById('upload-form').onsubmit = async e => {
    e.preventDefault(); const f = document.getElementById('file').files[0]; if (!f) return;
    try { await MedAPI.uploadPrescription(f); toast('Uploaded'); e.target.reset(); loadRx(); }
    catch (err) { toast(err.message); }
  };
});
