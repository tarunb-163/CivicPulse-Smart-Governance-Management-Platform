export function initComplaintPickerMap(mapContainerId, initialLat = 14.4644, initialLng = 75.9218, onLocationSelected) {
  if (typeof L === 'undefined') {
    console.warn('Leaflet library not loaded');
    return null;
  }

  const map = L.map(mapContainerId).setView([initialLat, initialLng], 14);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors'
  }).addTo(map);

  const marker = L.marker([initialLat, initialLng], {
    draggable: true
  }).addTo(map);

  async function updateCoords(lat, lng) {
    let address = '';
    try {
      const res = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`, {
        headers: { 'User-Agent': 'CivicPulse-Gov/1.0' }
      });
      if (res.ok) {
        const data = await res.json();
        address = data.display_name || '';
      }
    } catch {
      // Fallback
    }

    if (onLocationSelected) {
      onLocationSelected({ lat, lng, address });
    }
  }

  marker.on('dragend', function (e) {
    const pos = marker.getLatLng();
    updateCoords(pos.lat, pos.lng);
  });

  map.on('click', function (e) {
    marker.setLatLng(e.latlng);
    updateCoords(e.latlng.lat, e.latlng.lng);
  });

  // Initial trigger
  updateCoords(initialLat, initialLng);

  // Return map handle so caller can invalidateSize when shown in stepper
  return {
    map,
    marker,
    setCoords: (lat, lng) => {
      marker.setLatLng([lat, lng]);
      map.setView([lat, lng], 15);
      updateCoords(lat, lng);
    }
  };
}

export function initStaticDisplayMap(mapContainerId, lat, lng, popupText = '') {
  if (typeof L === 'undefined') return null;
  const map = L.map(mapContainerId, {
    zoomControl: false,
    dragging: false,
    scrollWheelZoom: false
  }).setView([lat, lng], 15);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap'
  }).addTo(map);

  const m = L.marker([lat, lng]).addTo(map);
  if (popupText) {
    m.bindPopup(popupText).openPopup();
  }
  return map;
}

export function initHeatmap(mapContainerId, points = []) {
  if (typeof L === 'undefined') return null;
  const centerLat = points.length ? points[0].lat : 14.4644;
  const centerLng = points.length ? points[0].lng : 75.9218;

  const map = L.map(mapContainerId).setView([centerLat, centerLng], 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; OpenStreetMap'
  }).addTo(map);

  if (L.heatLayer && points.length) {
    const heatData = points.map(p => [p.lat, p.lng, p.intensity || 0.8]);
    L.heatLayer(heatData, {
      radius: 25,
      blur: 15,
      maxZoom: 17
    }).addTo(map);
  } else {
    // Fallback to circle markers
    points.forEach(p => {
      L.circleMarker([p.lat, p.lng], {
        radius: 8,
        fillColor: '#ef4444',
        color: '#dc2626',
        weight: 1,
        fillOpacity: 0.6
      }).addTo(map);
    });
  }

  return map;
}
