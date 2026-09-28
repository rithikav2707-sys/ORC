(() => {
  const powerCanvas = document.getElementById('power-chart');
  const monthlyCanvas = document.getElementById('monthly-chart');
  if (!powerCanvas || !monthlyCanvas || !window.Chart) return;
  const number = value => Number(value || 0);
  const chartColors = { teal: '#168478', pale: 'rgba(22,132,120,.12)', orange: '#e58b4e' };

  fetch('/api/dashboard/generation')
    .then(response => { if (!response.ok) throw new Error('Could not load generation data'); return response.json(); })
    .then(data => {
      new Chart(powerCanvas, {
        type: 'line',
        data: { labels: data.hourLabels, datasets: [{ data: data.powerOutput.map(number), borderColor: chartColors.teal, backgroundColor: chartColors.pale, fill: true, tension: .36, borderWidth: 2, pointRadius: 0, pointHoverRadius: 4 }] },
        options: { maintainAspectRatio: false, responsive: true, plugins: { legend: { display: false }, tooltip: { displayColors: false, callbacks: { label: context => `${context.parsed.y.toFixed(1)} kW` } } }, scales: { x: { grid: { display: false }, ticks: { color: '#829194', maxTicksLimit: 8, maxRotation: 0, font: { size: 9 } }, border: { display: false } }, y: { grid: { color: '#edf1ef' }, ticks: { color: '#829194', font: { size: 9 } }, border: { display: false }, suggestedMin: 200 } } }
      });
      new Chart(monthlyCanvas, {
        type: 'bar',
        data: { labels: data.monthLabels, datasets: [{ data: data.monthlyEnergy.map(number), backgroundColor: data.monthLabels.map((_, index) => index === data.monthLabels.length - 1 ? chartColors.teal : '#d8e9e4'), borderRadius: 4, maxBarThickness: 34 }] },
        options: { maintainAspectRatio: false, responsive: true, plugins: { legend: { display: false }, tooltip: { displayColors: false, callbacks: { label: context => `${context.parsed.y.toLocaleString()} kWh` } } }, scales: { x: { grid: { display: false }, ticks: { color: '#829194', font: { size: 9 } }, border: { display: false } }, y: { grid: { color: '#edf1ef' }, ticks: { color: '#829194', font: { size: 9 } }, border: { display: false }, beginAtZero: true } } }
      });
    })
    .catch(() => {
      [powerCanvas, monthlyCanvas].forEach(canvas => {
        const context = canvas.getContext('2d');
        context.fillStyle = '#83918f'; context.font = '12px sans-serif'; context.fillText('Generation data is not available.', 12, 24);
      });
    });
})();