document.querySelectorAll('.sidebar nav a').forEach(link => {
  const path = new URL(link.href).pathname;
  if (location.pathname === path || location.pathname.startsWith(path + '/')) {
    link.classList.add('active'); link.setAttribute('aria-current', 'page');
  }
});
document.querySelectorAll('form[data-dirty-check]').forEach(form => {
  let dirty = false;
  let submitting = false;
  form.addEventListener('input', () => { dirty = true; });
  window.addEventListener('beforeunload', event => {
    if (dirty && !submitting) { event.preventDefault(); event.returnValue = ''; }
  });
  form.addEventListener('submit', event => {
    if (submitting) { event.preventDefault(); return; }
    if (!form.checkValidity()) return;
    submitting = true;
    form.querySelectorAll('button[type="submit"]').forEach(button => { button.disabled = true; button.textContent = '저장 중…'; });
  });
  window.addEventListener('pageshow', () => {
    if (submitting) location.reload();
  });
});
document.querySelector('[role="alert"]')?.focus();
