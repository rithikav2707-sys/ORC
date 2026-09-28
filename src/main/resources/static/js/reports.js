(() => {
  const currency = amount => `₹${Number(amount || 0).toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
  const renderLines = (id, endpoint, groups) => {
    const host = document.getElementById(id);
    if (!host) return;
    fetch(endpoint).then(response => response.json()).then(values => {
      host.replaceChildren();
      groups.forEach(group => {
        if (group.heading) {
          const heading = document.createElement('div'); heading.className = 'report-line total'; heading.textContent = group.heading; host.append(heading);
        } else {
          const line = document.createElement('div'); line.className = `report-line${group.total ? ' total' : ''}${group.net ? ' net' : ''}`;
          const label = document.createElement('span'); label.textContent = group.label;
          const amount = document.createElement('strong'); amount.textContent = currency(values[group.key]);
          line.append(label, amount); host.append(line);
        }
      });
    }).catch(() => { host.textContent = 'Report data could not be loaded.'; });
  };
  renderLines('profit-loss-report', '/api/reports/profit-loss', [
    { heading: 'Income' }, { label: 'ORC Power Sales Revenue', key: 'orcPowerSalesRevenue' }, { label: 'Carbon Offset Credits', key: 'carbonOffsetCredits' }, { label: 'Total Income', key: 'totalIncome', total: true },
    { heading: 'Expenses' }, { label: 'Working Fluid Replenishment Costs', key: 'workingFluidCosts' }, { label: 'Turbine Servicing Expenses', key: 'turbineServicingExpenses' }, { label: 'Total Expenses', key: 'totalExpenses', total: true }, { label: 'Net Profit / (Loss)', key: 'netProfitLoss', net: true }
  ]);
  renderLines('balance-sheet-report', '/api/reports/balance-sheet', [
    { heading: 'Assets' }, { label: 'ORC Power Plant Equipment', key: 'plantEquipment' }, { label: 'Working Fluid Inventory', key: 'workingFluidInventory' }, { label: 'Cash / Bank', key: 'cashBank' }, { label: 'Customer Receivables', key: 'customerReceivables' }, { label: 'Total Assets', key: 'totalAssets', total: true },
    { heading: 'Liabilities' }, { label: 'Maintenance Vendor Payables', key: 'maintenancePayables' }, { label: 'Chemical Supplier Creditors', key: 'supplierCreditors' }, { label: 'Total Liabilities', key: 'totalLiabilities', total: true }
  ]);

  const form = document.getElementById('generation-filter');
  const body = document.getElementById('generation-rows');
  if (form && body) {
    const today = new Date().toISOString().slice(0, 10);
    form.elements.from.value = new Date(Date.now() - 7 * 86400000).toISOString().slice(0, 10);
    form.elements.to.value = today;
    const load = event => {
      if (event) event.preventDefault();
      const query = new URLSearchParams(new FormData(form));
      fetch(`/api/reports/generation?${query}`).then(response => response.json()).then(rows => {
        body.replaceChildren();
        rows.forEach(row => {
          const tr = document.createElement('tr');
          [new Date(row.timestamp).toLocaleString(), row.turbine.turbineName, `${row.powerOutputKw} kW`, `${row.energyGeneratedKwh} kWh`, `${row.energyExportedKwh} kWh`, `${row.thermalEfficiency}%`].forEach(value => {
            const td = document.createElement('td'); td.textContent = value; tr.append(td);
          });
          body.append(tr);
        });
        if (!rows.length) { const row = body.insertRow(); const cell = row.insertCell(); cell.colSpan = 6; cell.className = 'empty-row'; cell.textContent = 'No telemetry for the selected dates.'; }
      });
    };
    form.addEventListener('submit', load); load();
  }
})();