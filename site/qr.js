function drawQR(host, text, label) {
  const qr = qrcode(0, text.length > 300 ? 'L' : 'M');
  qr.addData(text);
  qr.make();
  const n = qr.getModuleCount();
  const q = 4;
  let d = '';
  for (let r = 0; r < n; r++) for (let c = 0; c < n; c++) if (qr.isDark(r, c)) d += `M${c + q} ${r + q}h1v1h-1z`;
  const size = n + q * 2;
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  svg.setAttribute('viewBox', `0 0 ${size} ${size}`);
  svg.setAttribute('shape-rendering', 'crispEdges');
  svg.setAttribute('role', 'img');
  svg.setAttribute('aria-label', label || 'QR code');
  svg.innerHTML = `<rect width="${size}" height="${size}" fill="#fff"/><path d="${d}" fill="#151a26"/>`;
  host.replaceChildren(svg);
}

const onComputer = !/Android|iPhone|iPad|iPod/i.test(navigator.userAgent) && matchMedia('(hover: hover) and (pointer: fine)').matches;
if (onComputer) {
  document.querySelectorAll('.get-qr').forEach((box) => {
    const host = box.querySelector('[data-qr]');
    drawQR(host, host.dataset.qr, host.dataset.qrLabel);
    box.hidden = false;
  });
}
