(() => {
  const dateNode = document.getElementById('top-date');
  const timeNode = document.getElementById('top-time');
  const updateClock = () => {
    const now = new Date();
    if (dateNode) dateNode.textContent = new Intl.DateTimeFormat(undefined, { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' }).format(now);
    if (timeNode) timeNode.textContent = new Intl.DateTimeFormat(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' }).format(now);
  };
  updateClock();
  window.setInterval(updateClock, 1000);

  const search = document.getElementById('table-search');
  if (search) search.addEventListener('input', () => {
    const term = search.value.trim().toLocaleLowerCase();
    document.querySelectorAll('.data-row').forEach(row => {
      row.hidden = !row.textContent.toLocaleLowerCase().includes(term);
    });
  });

  const paymentType = document.getElementById('payment-type');
  const reference = document.getElementById('payment-reference');
  if (paymentType && reference) {
    const groups = Array.from(reference.querySelectorAll('optgroup'));
    const updatePaymentOptions = () => {
      const isReceipt = paymentType.value === 'RECEIPT';
      groups.forEach((group, index) => { group.hidden = index !== (isReceipt ? 0 : 1); });
    };
    paymentType.addEventListener('change', updatePaymentOptions);
    updatePaymentOptions();
  }
})();