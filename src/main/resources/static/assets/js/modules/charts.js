export function renderTrendChart(canvasId, dates = [], counts = []) {
  if (typeof Chart === 'undefined') return null;
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;

  return new Chart(ctx, {
    type: 'line',
    data: {
      labels: dates,
      datasets: [{
        label: 'Grievances Filed',
        data: counts,
        borderColor: '#3b82f6',
        backgroundColor: 'rgba(59, 130, 246, 0.12)',
        borderWidth: 2.5,
        fill: true,
        tension: 0.35,
        pointBackgroundColor: '#3b82f6',
        pointRadius: 3
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: { color: 'rgba(150, 150, 150, 0.1)' }
        },
        x: {
          grid: { display: false }
        }
      }
    }
  });
}

export function renderCategoryChart(canvasId, labels = [], counts = []) {
  if (typeof Chart === 'undefined') return null;
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;

  const colors = [
    '#3b82f6', '#14b8a6', '#f59e0b', '#ec4899', '#8b5cf6',
    '#06b6d4', '#10b981', '#f43f5e', '#6366f1', '#64748b'
  ];

  return new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: counts,
        backgroundColor: colors.slice(0, labels.length),
        borderWidth: 2,
        borderColor: 'transparent'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          position: 'right',
          labels: { boxWidth: 12, font: { size: 11 } }
        }
      },
      cutout: '70%'
    }
  });
}

export function renderDepartmentChart(canvasId, departments = [], resolved = [], pending = []) {
  if (typeof Chart === 'undefined') return null;
  const ctx = document.getElementById(canvasId);
  if (!ctx) return null;

  return new Chart(ctx, {
    type: 'bar',
    data: {
      labels: departments,
      datasets: [
        {
          label: 'Resolved',
          data: resolved,
          backgroundColor: '#10b981',
          borderRadius: 6
        },
        {
          label: 'Pending',
          data: pending,
          backgroundColor: '#f59e0b',
          borderRadius: 6
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        x: { stacked: true, grid: { display: false } },
        y: { stacked: true, grid: { color: 'rgba(150, 150, 150, 0.1)' } }
      }
    }
  });
}
