(() => {
  const LIFE_S = 15 * 60;
  const WEB = 'https://droid.withaster.dev/s';
  const MAX_ENCODED = 8 * 1024;
  const LOCAL = ['ollama', 'lmstudio'];
  const NEEDS_URL = ['ollama', 'lmstudio', 'custom'];
  const NO_KEY = ['kilo', 'ollama', 'lmstudio'];
  const PORTS = { ollama: '11434', lmstudio: '1234' };

  const ID = /^[a-z0-9_]{1,40}$/;
  const MODEL = /^[A-Za-z0-9._:/@+-]{1,200}$/;
  const TELEGRAM_TOKEN = /^[0-9]{5,15}:[A-Za-z0-9_-]{20,80}$/;
  const TOKENISH = /^[A-Za-z0-9_-]{4,256}$/;
  const USERS = /^[A-Za-z0-9_@+.,-]{1,512}$/;

  const $ = (id) => document.getElementById(id);
  const form = $('gen');
  const out = $('out');
  const error = $('error');
  const provider = $('provider');
  let timer = 0;

  const chat = () => form.querySelector('input[name="chat"]:checked').value;

  function sync() {
    const p = provider.value;
    const c = chat();
    const show = {
      kilo: p === 'kilo',
      local: LOCAL.includes(p),
      url: NEEDS_URL.includes(p),
      model: p !== '',
      key: p !== '' && p !== 'kilo',
      effort: p !== '',
      tg: c === 'tg',
      photon: c === 'photon',
      people: c === 'tg' || c === 'photon',
    };
    form.querySelectorAll('[data-show]').forEach((n) => { n.hidden = !show[n.dataset.show]; });
    $('model-opt').textContent = NEEDS_URL.includes(p) ? 'required' : 'optional';
    $('key-opt').textContent = NO_KEY.includes(p) ? 'only if the server wants one' : 'optional if the phone has it';
    $('url').placeholder = PORTS[p] ? `http://192.168.1.20:${PORTS[p]}/v1` : 'https://example.com/v1';
  }

  function privateHost(host) {
    const h = host.replace(/^\[|\]$/g, '').toLowerCase();
    if (h === 'localhost' || (h.endsWith('.local') && h.length > 6)) return true;
    const v4 = h.match(/^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/);
    if (v4) {
      const [a, b] = [Number(v4[1]), Number(v4[2])];
      return a === 10 || a === 127 || (a === 172 && b >= 16 && b <= 31) || (a === 192 && b === 168) || (a === 169 && b === 254) || (a === 100 && b >= 64 && b <= 127);
    }
    return h === '::1' || /^f[cd]/.test(h) || /^fe[89ab]/.test(h);
  }

  function checkUrl(text) {
    let u;
    try { u = new URL(text); } catch { return 'The server address is not a full address. Start it with http:// or https://.'; }
    if (u.username || u.password) return 'Leave the name and password out of the server address.';
    if (u.protocol === 'https:') return null;
    if (u.protocol === 'http:' && privateHost(u.hostname)) {
      if (['localhost', '127.0.0.1', '[::1]'].includes(u.hostname)) return 'On the phone, localhost is the phone. Use your computer\'s address on your Wi-Fi or Tailscale.';
      return null;
    }
    return 'Plain http only works to a private address. Use https for anything on the internet.';
  }

  const clean = (v) => v.trim();
  const users = (v) => v.split(',').map((s) => s.trim()).filter(Boolean).join(',');
  const hasControl = (v) => /[\u0000-\u001f\u007f-\u009f]/.test(v);

  function build() {
    const payload = { v: 1, exp: 0, n: '' };
    const p = provider.value;
    if (p) {
      if (!ID.test(p)) return 'Pick a provider from the list.';
      const brain = { id: p };
      const url = clean($('url').value);
      const model = clean($('model').value);
      const key = clean($('key').value);
      const effort = $('effort').value;
      if (NEEDS_URL.includes(p)) {
        if (!url) return 'Add the server address.';
        const bad = checkUrl(url);
        if (bad) return bad;
        if (!model) return 'Add the model name, as the server lists it.';
        brain.url = url;
      }
      if (model) {
        if (!MODEL.test(model)) return 'The model name has characters Droid will not read.';
        brain.m = model;
      }
      if (key && p !== 'kilo') {
        if (hasControl(key) || key.length > 4096) return 'The key has characters Droid will not write.';
        brain.k = key;
      }
      if (effort) brain.e = effort;
      payload.p = brain;
    }
    const c = chat();
    const people = users($('users').value);
    if ((c === 'tg' || c === 'photon') && people && !USERS.test(people)) return 'Allowed people can only be ids, phone numbers or handles, separated by commas.';
    if (c === 'app') payload.ch = { app: true };
    if (c === 'tg') {
      const t = clean($('tg-token').value);
      if (!TELEGRAM_TOKEN.test(t)) return 'That does not look like a bot token from @BotFather. It is a number, a colon, then a long code.';
      payload.ch = { tg: people ? { t, u: people } : { t } };
    }
    if (c === 'photon') {
      const k = clean($('ph-key').value);
      const pr = clean($('ph-project').value);
      if (!TOKENISH.test(k) || !TOKENISH.test(pr)) return 'Add both the Photon project key and the project id.';
      payload.ch = { photon: people ? { k, p: pr, u: people } : { k, p: pr } };
    }
    const mcp = $('mcp').value;
    if (mcp) payload.mcp = { on: mcp === 'on' };
    const relay = clean($('relay').value);
    if (relay) {
      let u;
      try { u = new URL(relay); } catch { return 'The relay address is not a full https address.'; }
      if (u.protocol !== 'https:') return 'The relay address must start with https://.';
      if (u.hostname === 'relay.withaster.dev') return 'Aster Relay is not open yet. Use the address of a relay you host yourself.';
      payload.relay = { url: relay };
    }
    if (!payload.p && !payload.ch && !payload.mcp && !payload.relay) return 'Pick at least one thing for the link to set up.';
    return payload;
  }

  function nonce() {
    return b64url(crypto.getRandomValues(new Uint8Array(12)));
  }

  function b64url(bytes) {
    let s = '';
    for (const b of bytes) s += String.fromCharCode(b);
    return btoa(s).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  }

  async function deflate(text) {
    const stream = new Blob([text]).stream().pipeThrough(new CompressionStream('deflate-raw'));
    return new Uint8Array(await new Response(stream).arrayBuffer());
  }

  function fail(message) {
    error.textContent = message;
    error.hidden = false;
    error.scrollIntoView({ block: 'center', behavior: 'smooth' });
  }

  function countdown(exp) {
    clearInterval(timer);
    const tick = () => {
      const left = exp - Math.floor(Date.now() / 1000);
      if (left <= 0) {
        clearInterval(timer);
        $('timer').textContent = 'This link has expired. Make a new one.';
        out.classList.add('expired');
        return;
      }
      const m = Math.floor(left / 60);
      const s = String(left % 60).padStart(2, '0');
      $('timer').textContent = `Works once, for the next ${m}:${s}.`;
    };
    tick();
    timer = setInterval(tick, 1000);
  }

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    error.hidden = true;
    if (typeof CompressionStream === 'undefined') { fail('This browser cannot make setup links. Update it, or use a recent Chrome, Firefox or Safari.'); return; }
    const payload = build();
    if (typeof payload === 'string') { fail(payload); return; }
    payload.exp = Math.floor(Date.now() / 1000) + LIFE_S;
    payload.n = nonce();
    const data = b64url(await deflate(JSON.stringify(payload)));
    if (data.length > MAX_ENCODED) { fail('This setup is too long for one link. Shorten the allowed people list.'); return; }
    const link = `${WEB}#1.${data}`;
    $('link').textContent = link;
    drawQR($('qr'), link, 'QR code of your setup link');
    out.classList.remove('expired');
    out.hidden = false;
    countdown(payload.exp);
    out.scrollIntoView({ block: 'start', behavior: 'smooth' });
  });

  $('copy').addEventListener('click', async () => {
    const btn = $('copy');
    try { await navigator.clipboard.writeText($('link').textContent); btn.textContent = 'Copied'; } catch {
      const range = document.createRange(); range.selectNodeContents($('link'));
      const sel = getSelection(); sel.removeAllRanges(); sel.addRange(range);
      btn.textContent = 'Selected, press Ctrl+C';
    }
    setTimeout(() => { btn.textContent = 'Copy link'; }, 1600);
  });

  $('again').addEventListener('click', () => {
    clearInterval(timer);
    out.hidden = true;
    $('qr').replaceChildren();
    $('link').textContent = '';
    form.scrollIntoView({ block: 'start', behavior: 'smooth' });
  });

  $('show').addEventListener('change', (e) => {
    form.querySelectorAll('#key, #tg-token, #ph-key').forEach((n) => { n.type = e.target.checked ? 'text' : 'password'; });
  });

  provider.addEventListener('change', sync);
  form.querySelectorAll('input[name="chat"]').forEach((r) => r.addEventListener('change', sync));
  sync();
})();
