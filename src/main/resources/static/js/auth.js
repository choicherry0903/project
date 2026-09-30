const panels = document.querySelectorAll('.auth-panel');
const registerUsername = document.getElementById('register-username');
const usernameFeedback = document.getElementById('username-feedback');
let checkedUsername = null;

document.querySelectorAll('[data-open-panel]').forEach((button) => {
  button.addEventListener('click', () => {
    const panel = document.getElementById(`${button.dataset.openPanel}-panel`);
    const willOpen = panel.hidden;
    panels.forEach((item) => { item.hidden = true; });
    panel.hidden = !willOpen;
    if (willOpen) panel.querySelector('input').focus();
  });
});

registerUsername.addEventListener('input', () => {
  checkedUsername = null;
  usernameFeedback.textContent = '';
});

const checkButton = document.getElementById('check-username');
checkButton.addEventListener('click', async () => {
  if (!registerUsername.reportValidity()) return;
  const value = registerUsername.value.trim();
  try {
    const response = await fetch(`${checkButton.dataset.checkUrl}?username=${encodeURIComponent(value)}`);
    const result = await response.json();
    checkedUsername = response.ok && result.available ? value : null;
    usernameFeedback.textContent = result.message;
  } catch {
    checkedUsername = null;
    usernameFeedback.textContent = '중복 확인을 다시 시도해 주세요.';
  }
});

document.querySelectorAll('[data-auth-form]').forEach((form) => {
  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    if (form.dataset.authForm === 'register' && checkedUsername !== registerUsername.value.trim()) {
      usernameFeedback.textContent = '아이디 중복 확인을 해 주세요.';
      registerUsername.focus();
      return;
    }
    const submit = form.querySelector('[type="submit"]');
    submit.disabled = true;
    try {
      const response = await fetch(form.action, {
        method: 'POST',
        body: new URLSearchParams(new FormData(form)),
      });
      const result = await response.json();
      alert(result.message || '요청을 처리하지 못했습니다.');
      if (response.ok && form.dataset.authForm === 'register') {
        form.reset();
        checkedUsername = null;
        usernameFeedback.textContent = '';
        document.getElementById('register-panel').hidden = true;
        document.getElementById('username').focus();
      }
    } catch {
      alert('요청을 처리하지 못했습니다. 다시 시도해 주세요.');
    } finally {
      submit.disabled = false;
    }
  });
});
