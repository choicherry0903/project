(() => {
  const form = document.querySelector('#estimate-form');
  if (!form) return;
  const lines = document.querySelector('#estimate-lines');
  const field = (row, name) => row.querySelector(`[name$=".${name}"], [data-field="${name}"]`);
  const money = value => `${value.toLocaleString('ko-KR')}원`;
  function indexRows() {
    [...lines.children].forEach((row, index) => row.querySelectorAll('input, select').forEach(input => {
      const name = input.dataset.field || input.name.split('.').pop();
      input.name = `items[${index}].${name}`;
      input.id = `line-${index}-${name}`;
    }));
  }
  function scaled(value, places) {
    if (!/^\d+(\.\d+)?$/.test(value)) throw new Error('숫자 입력을 확인해 주세요.');
    const [whole, decimal = ''] = value.split('.');
    if (decimal.length > places) throw new Error('수량은 소수점 2자리, 세율은 소수점 4자리 이내로 입력해 주세요.');
    return BigInt(whole) * (10n ** BigInt(places)) + BigInt(decimal.padEnd(places, '0') || '0');
  }
  function calculate() {
    const error = document.querySelector('#amount-error');
    const displayed = document.querySelector('#displayedTotal');
    displayed.value = ''; error.hidden = true;
    try {
      let subtotal = 0n, incomplete = 0;
      [...lines.children].forEach(row => {
        const quantity = field(row, 'quantity').value, price = field(row, 'unitPrice').value;
        if (!field(row, 'name').value.trim() || !field(row, 'unit').value.trim() || quantity === '' || price === '') incomplete++;
        if (quantity === '' || price === '') { row.querySelector('output').textContent = '—'; return; }
        const q = scaled(quantity, 2), p = scaled(price, 0);
        if (q <= 0n || q > 99999999n || p > 999999999n) throw new Error('수량 또는 단가 범위를 확인해 주세요.');
        const amount = (q * p + 50n) / 100n;
        row.querySelector('output').textContent = money(amount); subtotal += amount;
      });
      const discount = scaled(document.querySelector('#discount').value, 0);
      if (discount > subtotal) throw new Error('할인은 항목 합계 이내여야 합니다.');
      const supply = subtotal - discount;
      const rate = scaled(document.querySelector('#taxRate').value, 4);
      const tax = (supply * rate + 5000n) / 10000n;
      const total = supply + tax;
      if (total > 999999999n || subtotal > 999999999n) throw new Error('견적 금액 상한(999,999,999원)을 초과했습니다.');
      for (const [name, value] of Object.entries({subtotal, supply, tax, total})) document.querySelector(`#${name}`).textContent = money(value);
      displayed.value = total.toString();
      document.querySelector('#draft-hint').textContent = incomplete ? `미완성 항목 ${incomplete}개 · 입력된 수량·단가만 금액에 반영됩니다. 초안 저장은 가능합니다.` : '입력한 항목을 초안으로 저장합니다.';
    } catch (e) {
      error.textContent = e.message; error.hidden = false;
      ['subtotal', 'supply', 'tax', 'total'].forEach(id => document.querySelector(`#${id}`).textContent = '—');
    }
  }
  function add(values = {}) {
    if (lines.children.length >= 100) { alert('견적 항목은 최대 100개입니다.'); return; }
    const row = document.querySelector('#line-template').content.firstElementChild.cloneNode(true);
    for (const [name, value] of Object.entries(values)) field(row, name).value = value;
    lines.append(row); indexRows(); calculate();
    form.dispatchEvent(new Event('input', {bubbles: true})); field(row, 'name').focus();
  }
  document.querySelector('#add-line').addEventListener('click', () => add());
  document.querySelector('#add-material').addEventListener('click', () => {
    const option = document.querySelector('#material-choice').selectedOptions[0];
    if (!option.value) return;
    add({category: 'MATERIAL', name: option.dataset.name, unit: option.dataset.unit, unitPrice: option.dataset.price, quantity: '1'});
  });
  lines.addEventListener('click', event => {
    const button = event.target.closest('.remove-line');
    if (!button) return;
    button.closest('tr').remove(); indexRows(); calculate(); form.dispatchEvent(new Event('input', {bubbles: true}));
  });
  form.addEventListener('input', calculate); form.addEventListener('change', calculate);
  form.addEventListener('submit', calculate, true);
  indexRows(); calculate();
})();
