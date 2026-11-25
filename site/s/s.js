(() => {
  const hasLink = /^#1\.[A-Za-z0-9_-]+$/.test(location.hash);
  const android = /Android/i.test(navigator.userAgent);
  const when = !hasLink ? 'none' : android ? 'android' : 'desktop';
  for (const el of document.querySelectorAll('[data-when]')) {
    const key = el.dataset.when;
    el.hidden = key === 'link' ? !hasLink : key !== when;
  }
  if (when === 'desktop') drawQR(document.getElementById('qr'), `https://droid.withaster.dev/s${location.hash}`, 'QR code of this setup link');
  if (when === 'android') document.getElementById('open').href = `asterdroid://setup${location.hash}`;
})();
