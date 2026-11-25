const reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;
const wait = (ms) => new Promise((r) => setTimeout(r, reduce ? 0 : ms));
const el = (tag, cls, html) => { const n = document.createElement(tag); if (cls) n.className = cls; if (html != null) n.innerHTML = html; return n; };
const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;');
const CHECK = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12.5l4.5 4.5L19 7.5"/></svg>';

/* iPhones on iOS 26, one conversation, each in its chat app's own interface */
const I = {
  chev: '<svg width="12" height="20" viewBox="0 0 12 20" aria-hidden="true"><path d="M10 2 2.5 10 10 18" fill="none" stroke="currentColor" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  chevSm: '<svg width="7" height="12" viewBox="0 0 7 12" aria-hidden="true"><path d="M1.5 1.5 5.5 6l-4 4.5" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  video: '<svg width="27" height="19" viewBox="0 0 28 20" aria-hidden="true"><rect x="1.2" y="2.2" width="17.6" height="15.6" rx="4.5" fill="none" stroke="currentColor" stroke-width="2.1"/><path d="M20 8.3 26.5 4.2v11.6L20 11.7z" fill="none" stroke="currentColor" stroke-width="2.1" stroke-linejoin="round"/></svg>',
  call: '<svg width="22" height="22" viewBox="0 0 24 24" aria-hidden="true"><path d="M6.6 2.8 9 2.3l2 4.6-2.3 1.6a11.5 11.5 0 0 0 6.8 6.8l1.6-2.3 4.6 2-.5 2.4a2.4 2.4 0 0 1-2.4 1.9A17.4 17.4 0 0 1 2.7 5.2a2.4 2.4 0 0 1 1.9-2.4z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/></svg>',
  plus: '<svg width="20" height="20" viewBox="0 0 20 20" aria-hidden="true"><path d="M10 2.5v15M2.5 10h15" stroke="currentColor" stroke-width="2.1" stroke-linecap="round"/></svg>',
  plusBig: '<svg width="25" height="25" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 3v18M3 12h18" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>',
  mic: '<svg width="15" height="21" viewBox="0 0 16 22" aria-hidden="true"><rect x="4" y="1.2" width="8" height="12.6" rx="4" fill="currentColor"/><path d="M1.2 10a6.8 6.8 0 0 0 13.6 0M8 17v3.8" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
  camera: '<svg width="27" height="23" viewBox="0 0 26 22" aria-hidden="true"><path d="M2.5 6.5h4.2l2-3h8.6l2 3h4.2v12.5H2.5z" fill="none" stroke="currentColor" stroke-width="2" stroke-linejoin="round"/><circle cx="13" cy="12.4" r="4" fill="none" stroke="currentColor" stroke-width="2"/></svg>',
  sticker: '<svg width="22" height="22" viewBox="0 0 22 22" aria-hidden="true"><path d="M20 11.5V7a5 5 0 0 0-5-5H7a5 5 0 0 0-5 5v8a5 5 0 0 0 5 5h4.5z" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linejoin="round"/><path d="M11.5 20v-3.5a5 5 0 0 1 5-5H20" fill="none" stroke="currentColor" stroke-width="1.9"/></svg>',
  clip: '<svg width="22" height="22" viewBox="0 0 24 24" aria-hidden="true"><path d="m20.5 11.4-8.6 8.6a5.8 5.8 0 0 1-8.2-8.2l8.6-8.6a3.9 3.9 0 0 1 5.5 5.5l-8.6 8.6a1.9 1.9 0 0 1-2.7-2.7l7.9-7.9" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
  menu: '<svg width="18" height="14" viewBox="0 0 18 14" aria-hidden="true"><path d="M1.5 1.5h15M1.5 7h15M1.5 12.5h15" stroke="#fff" stroke-width="2.2" stroke-linecap="round"/></svg>',
  emoji: '<svg width="23" height="23" viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="9.5" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="8.8" cy="10" r="1.2" fill="currentColor"/><circle cx="15.2" cy="10" r="1.2" fill="currentColor"/><path d="M8 14.2a4.6 4.6 0 0 0 8 0" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>',
  plane: '<svg width="17" height="17" viewBox="0 0 24 24" aria-hidden="true"><path d="M21.5 2.6 2.8 10.1c-.9.4-.9 1.6 0 1.9l4.7 1.6 1.8 5.6c.3.9 1.4 1.1 2 .4l2.6-2.7 4.8 3.5c.7.5 1.7.1 1.9-.8l3.1-15.6c.2-1-.8-1.8-1.7-1.4zM9.5 14.2l8.6-7.4-6.8 8.7-.4 3.1z" fill="currentColor"/></svg>',
  wave: '<svg width="22" height="18" viewBox="0 0 22 18" aria-hidden="true"><path d="M2 7v4M6 4v10M10 1.5v15M14 5v8M18 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>',
  ticks: '<svg viewBox="0 0 17 11" aria-hidden="true"><path d="M1 5.8 4 8.8 10.4 1.6M6.4 8.2l.6.6 6.4-7.2" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  up: '<svg width="17" height="17" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 19V5M5 12l7-7 7 7" fill="none" stroke="currentColor" stroke-width="2.8" stroke-linecap="round" stroke-linejoin="round"/></svg>',
  check: '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12.5l4.5 4.5L19 7.5"/></svg>',
};
const STATUS_IOS = `<div class="i-status"><span>12:52</span><span class="r"><svg width="19" height="12" viewBox="0 0 19 12" aria-hidden="true"><rect x="0" y="8" width="3.2" height="4" rx="1" fill="currentColor"/><rect x="5" y="5.5" width="3.2" height="6.5" rx="1" fill="currentColor"/><rect x="10" y="3" width="3.2" height="9" rx="1" fill="currentColor"/><rect x="15" y="0" width="3.2" height="12" rx="1" fill="currentColor"/></svg><svg width="17" height="12" viewBox="0 0 17 12" aria-hidden="true"><path d="M8.5 2.4c2.4 0 4.6.9 6.3 2.5l1.2-1.3A10.8 10.8 0 0 0 8.5.6 10.8 10.8 0 0 0 1 3.6l1.2 1.3a9 9 0 0 1 6.3-2.5zm0 3.6c1.4 0 2.7.5 3.7 1.4l1.2-1.3a7.2 7.2 0 0 0-9.8 0l1.2 1.3c1-.9 2.3-1.4 3.7-1.4zm0 3.5L6.4 7.8a3 3 0 0 1 4.2 0z" fill="currentColor"/></svg><span class="i-batt">85</span></span></div>`;
const AVATAR = 'mascot/avatar.png';
const CHAT = [
  { me: 'Order my usual from KFC', t: '12:52 PM' },
  { them: 'On it. Opening Uber Eats.', t: '12:52 PM' },
  { them: '12pc Chicken Bucket for $21.99, down from $34.99. Should I place the order?', t: '12:52 PM', ask: true },
  { me: 'Yes', t: '12:53 PM', reply: true },
  { them: 'Placed. It arrives between 1:10 and 1:25 pm. I will check on the driver every 8 minutes.', t: '12:53 PM' },
];
const REPLY_YES = (s) => s.replace('Should I place the order?', 'Reply yes to place the order.');
function grouped(lines) {
  return lines.map((l, i) => {
    const who = l.me ? 'me' : 'them';
    const side = (x) => x && (x.me ? 'me' : 'them');
    return { ...l, who, text: l.me || l.them, first: side(lines[i - 1]) !== who, last: side(lines[i + 1]) !== who };
  });
}
function buildIOS(ui, app) {
  let chrome = '', body = '';
  if (app === 'imessage') {
    const g = grouped(CHAT);
    chrome = `<div class="i-fade"></div><span class="i-cap g im-back">${I.chev}<span class="im-badge">4</span></span><img class="i-avatar im-avatar" src="${AVATAR}" alt=""><span class="i-cap g im-name">Droid ${I.chevSm}</span><span class="i-circ g im-video">${I.video}</span>
      <span class="i-circ g im-plus">${I.plus}</span><span class="i-cap g im-field"><span>iMessage</span>${I.wave}</span>`;
    body = `<div class="stamp"><b>Today</b> 12:52 PM</div>` + g.map((m) => `<div class="imb ${m.who}${m.last ? ' last' : ''}${m.first && m.who === 'me' && m !== g[0] ? ' mt' : ''}">${esc(REPLY_YES(m.text))}</div>${m.reply ? '<div class="im-delivered">Delivered</div>' : ''}`).join('');
  } else if (app === 'whatsapp') {
    const g = grouped(CHAT);
    chrome = `<div class="wa-bar"></div><span class="i-circ wa-back">${I.chev}</span><img class="i-avatar wa-avatar" src="${AVATAR}" alt=""><span class="wa-who"><strong>Droid</strong><small>online</small></span><span class="i-cap wa-calls">${I.video}${I.call}</span>
      <div class="wa-compose"></div><span class="wa-plus">${I.plusBig}</span><span class="wa-field">${I.sticker}</span><span class="wa-cam">${I.camera}</span><span class="wa-mic">${I.mic}</span>`;
    body = `<div class="wa-day">Today</div>` + g.map((m) => `<div class="wab ${m.who}${m.last ? ' last' : ''}${m.first ? ' mt' : ''}">${esc(REPLY_YES(m.text))}<span class="tm">${m.t}${m.who === 'me' ? I.ticks : ''}</span></div>`).join('');
  } else if (app === 'telegram') {
    const g = grouped(CHAT.filter((m) => !m.reply));
    chrome = `<div class="i-fade"></div><span class="i-circ g tg-back">${I.chev}</span><span class="i-cap g tg-name"><strong>Droid</strong><small>bot</small></span><img class="i-avatar tg-avatar" src="${AVATAR}" alt="">
      <span class="i-circ tg-menu">${I.menu}</span><span class="i-circ g tg-clip">${I.clip}</span><span class="i-cap g tg-field"><span>Message</span><span class="e">${I.emoji}</span><span class="tg-send">${I.plane}</span></span>`;
    body = `<div class="tg-day">Today</div>` + g.map((m) => `<div class="tgb ${m.who}${m.last || m.ask ? ' last' : ''}${m.first ? ' mt' : ''}">${esc(m.text)}<span class="tm">${m.t.replace(' PM', '')}${m.who === 'me' ? I.ticks : ''}</span></div>${m.ask ? '<div class="tg-keys"><span>✓ Place order</span><span>Not now</span></div>' : ''}`).join('');
  }
  ui.innerHTML = `<div class="i-scroll">${body}</div>` + chrome + STATUS_IOS + '<span class="i-home"></span>';
}
document.querySelectorAll('.fan4 .ios-ui').forEach((ui) => buildIOS(ui, ui.dataset.app));

/* the hero: a Pixel running Droid's own chat, one task played live */
const STATUS_ANDROID = `<div class="a-status"><span>12:52</span><span class="r"><svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true"><path d="M12 20.5 1.2 9.2a15.6 15.6 0 0 1 21.6 0z" fill="currentColor"/></svg><svg width="15" height="15" viewBox="0 0 24 24" aria-hidden="true"><path d="M22 22H2L22 2z" fill="currentColor"/></svg><span class="a-batt"><i></i></span></span></div>`;
const STOP = '<svg width="12" height="12" viewBox="0 0 12 12" aria-hidden="true"><rect x="1" y="1" width="10" height="10" rx="2.2" fill="currentColor"/></svg>';
function buildDroid(ui) {
  ui.innerHTML = `<div class="i-scroll"></div>
    <div class="a-bar"><img class="i-avatar" src="${AVATAR}" alt=""><span class="a-who"><strong>Droid</strong><small>Running on this phone</small></span><span class="a-stop">${STOP}Stop</span></div>
    <div class="a-compose"><span class="a-field"><span>Message Droid</span></span><span class="d-send">${I.up}</span></div>` + STATUS_ANDROID;
}

const LIVE = {
  food: [
    { me: 'Order my usual from KFC' },
    { chip: 'Opening Uber Eats' },
    { them: '12pc Chicken Bucket for $21.99, down from $34.99. Place the order?', ask: 'Place order' },
    { chip: 'Order placed' },
    { them: 'It arrives between 1:10 and 1:25 pm. I will check on the driver every 8 minutes.', done: true },
  ],
  text: [
    { me: 'Tell Sam I am running 10 minutes late' },
    { chip: 'Opening WhatsApp' },
    { them: 'Send "Running 10 minutes late, sorry!" to Sam on WhatsApp?', ask: 'Send' },
    { chip: 'Sent to Sam' },
    { them: 'Done. I will tell you when Sam replies.', done: true },
  ],
  music: [
    { me: 'Play my focus playlist, not too loud' },
    { chip: 'Opening Spotify' },
    { them: 'Focus is playing at 40% volume. Tell me when to stop.', done: true },
  ],
  remember: [
    { me: 'I like a quiet phone after 10 pm' },
    { them: 'Noted. After 10 pm I will keep things quiet.' },
    { me: 'Make that a routine' },
    { chip: 'Saved skill /quiet-mode' },
    { them: 'It turns on Do Not Disturb and lowers the ring volume at 10 pm.', done: true },
  ],
};
const ORDER = ['food', 'text', 'music', 'remember'];
const heroPhone = document.querySelector('.hero-phone');
const heroUI = heroPhone.querySelector('.a-ui');
const perch = document.querySelector('.mascot-perch');
const perchImg = perch.querySelector('img');
const perchSrc = perch.querySelector('source');
const railFill = document.querySelector('.channel.is-on .rail-fill');
const taskButtons = [...document.querySelectorAll('.task')];
let task = 'food';
let run = 0;
let autoplay = !reduce;

function setRail(progress) {
  railFill.style.transition = progress > 0 ? 'transform 500ms cubic-bezier(0.23, 1, 0.32, 1)' : 'none';
  railFill.style.transform = `scaleY(${progress})`;
}

/* the conversation pushes up like the real thing: new rows grow in, the rest glide */
function mount() {
  buildDroid(heroUI);
  const scroll = heroUI.querySelector('.i-scroll');
  scroll.innerHTML = '<div class="i-track"></div>';
  return scroll.firstChild;
}
function push(track, node) {
  const before = track.offsetHeight;
  track.append(node);
  const delta = track.offsetHeight - before;
  if (reduce || !delta) return node;
  track.style.transition = 'none';
  track.style.transform = `translateY(${delta}px)`;
  track.offsetHeight;
  track.style.transition = 'transform 420ms cubic-bezier(0.23, 1, 0.32, 1)';
  track.style.transform = 'translateY(0)';
  return node;
}
function line(l, live) {
  const pop = live ? ' pop' : '';
  if (l.chip) return el('div', `d-chip${pop}`, `${I.check}${esc(l.chip)}`);
  if (l.me) return el('div', `ddb me${pop}`, esc(l.me));
  const n = el('div', `ddb them${pop}`, esc(l.them));
  if (l.ask) n.append(el('div', 'd-acts', `<span class="yes">${I.check.replace('<svg', '<svg width="14" height="14"')}${esc(l.ask)}</span><span class="no">Not now</span>`));
  return n;
}
async function typeInto(text, id) {
  const f = heroUI.querySelector('.a-field span');
  const placeholder = f.textContent;
  f.textContent = '';
  const t = el('span', 'typed'); f.append(t);
  for (const ch of text) {
    if (id !== run) return false;
    t.textContent += ch;
    await wait(34 + Math.random() * 46);
  }
  await wait(260);
  const sendBtn = heroUI.querySelector('.d-send');
  sendBtn.classList.add('press');
  await wait(130);
  sendBtn.classList.remove('press');
  f.textContent = placeholder;
  return id === run;
}
function cheer() {
  perchImg.src = 'mascot/bunny-happy.png';
  perchSrc.srcset = 'mascot/bunny-happy.webp';
  perch.classList.remove('hop'); perch.offsetWidth; perch.classList.add('hop');
  setTimeout(() => { perchImg.src = 'mascot/bunny-hello.png'; perchSrc.srcset = 'mascot/bunny-hello.webp'; }, 1800);
}

async function play() {
  const id = ++run;
  const track = mount();
  const lines = LIVE[task];
  setRail(0);
  await wait(500);
  for (let i = 0; i < lines.length; i++) {
    if (id !== run) return;
    const l = lines[i];
    if (l.me) {
      if (!(await typeInto(l.me, id))) return;
      push(track, line(l, true));
    } else if (l.chip) {
      const ask = track.querySelector('.d-acts .yes');
      if (ask) {
        ask.classList.add('press');
        await wait(260);
        if (id !== run) return;
        ask.closest('.d-acts').classList.add('gone');
        await wait(200);
        if (id !== run) return;
      }
      push(track, line(l, true));
    } else {
      const dots = push(track, el('div', 'ddb them typing pop', '<i></i><i></i><i></i>'));
      await wait(l.ask ? 1100 : 900);
      if (id !== run) return;
      dots.remove();
      push(track, line(l, true));
      if (l.done) cheer();
    }
    setRail((i + 1) / lines.length);
    await wait(l.me ? 520 : l.ask ? 1500 : 900);
  }
  if (!autoplay) return;
  await wait(3000);
  if (id !== run || !autoplay) return;
  task = ORDER[(ORDER.indexOf(task) + 1) % ORDER.length];
  taskButtons.forEach((t) => t.setAttribute('aria-pressed', String(t.dataset.task === task)));
  switchTask();
}

/* switching tasks: the screen dissolves down, the next settles in from above, the phone turns a touch */
let nudge = 0;
async function switchTask() {
  run++;
  if (reduce) { showStatic(); return; }
  heroPhone.classList.add('leaving');
  nudge = 5;
  await wait(220);
  heroPhone.classList.remove('leaving');
  heroPhone.classList.add('entering');
  play();
  requestAnimationFrame(() => requestAnimationFrame(() => heroPhone.classList.remove('entering')));
}
function showStatic() {
  const track = mount();
  LIVE[task].forEach((l) => track.append(line(l, false)));
  setRail(1);
}
taskButtons.forEach((b) => b.addEventListener('click', () => {
  autoplay = false;
  task = b.dataset.task;
  taskButtons.forEach((t) => t.setAttribute('aria-pressed', String(t === b)));
  switchTask();
}));

/* entrance: copy rises, the phone swings in and lands, Tap pops up and waves */
function enter() {
  document.querySelectorAll('.hero .pre').forEach((n) => n.classList.remove('pre'));
  setTimeout(() => { perch.classList.add('wave'); }, 1500);
  setTimeout(startTilt, 1250);
}
requestAnimationFrame(() => requestAnimationFrame(enter));
if (reduce) showStatic();
else setTimeout(play, 900);

/* the phone follows the pointer through a spring, and the glare slides across the glass */
function startTilt() {
  if (reduce || !matchMedia('(hover: hover) and (pointer: fine)').matches) return;
  const hero = document.querySelector('.hero');
  let tx = -14, ty = 6, x = -14, y = 6, vx = 0, vy = 0, raf = 0;
  heroPhone.style.transition = 'opacity 500ms ease-out';
  const step = () => {
    const ax = (tx + nudge - x) * 0.06 - vx * 0.28;
    const ay = (ty - y) * 0.06 - vy * 0.28;
    vx += ax; vy += ay; x += vx; y += vy;
    nudge *= 0.9;
    heroPhone.style.setProperty('--ry', `${x.toFixed(2)}deg`);
    heroPhone.style.setProperty('--rx', `${y.toFixed(2)}deg`);
    heroPhone.style.setProperty('--gx', `${(50 + (x + 14) * 5).toFixed(1)}%`);
    raf = requestAnimationFrame(step);
  };
  hero.addEventListener('pointermove', (e) => {
    const r = hero.getBoundingClientRect();
    tx = -14 + ((e.clientX - r.left) / r.width - 0.5) * 16;
    ty = 6 - ((e.clientY - r.top) / r.height - 0.5) * 9;
  });
  hero.addEventListener('pointerleave', () => { tx = -14; ty = 6; });
  document.addEventListener('visibilitychange', () => { if (document.hidden) { cancelAnimationFrame(raf); raf = 0; } else if (!raf) raf = requestAnimationFrame(step); });
  raf = requestAnimationFrame(step);
}

document.querySelectorAll('.lock').forEach((lock) => {
  if (reduce) return;
  new IntersectionObserver(([e], o) => { if (e.isIntersecting) { o.disconnect(); lock.classList.add('arrive'); } }, { threshold: 0.6 }).observe(lock);
});

document.querySelectorAll('.marquee-track').forEach((t) => {
  [...t.children].forEach((c) => { const d = c.cloneNode(true); d.setAttribute('aria-hidden', 'true'); t.append(d); });
});

document.querySelectorAll('.qa').forEach((d) => {
  d.querySelector('summary').addEventListener('click', (e) => {
    e.preventDefault();
    if (d.classList.contains('is-open')) {
      d.classList.remove('is-open');
      setTimeout(() => { if (!d.classList.contains('is-open')) d.open = false; }, reduce ? 0 : 280);
    } else {
      d.open = true;
      requestAnimationFrame(() => d.classList.add('is-open'));
    }
  });
});

document.querySelectorAll('.cmd-copy').forEach((btn) => {
  const code = btn.previousElementSibling;
  const label = btn.getAttribute('aria-label');
  btn.addEventListener('click', async () => {
    let copied = true;
    try { await navigator.clipboard.writeText(code.textContent); } catch {
      const range = document.createRange(); range.selectNodeContents(code);
      const sel = getSelection(); sel.removeAllRanges(); sel.addRange(range);
      try { copied = document.execCommand('copy'); } catch { copied = false; }
    }
    if (!copied) { btn.setAttribute('aria-label', 'Command selected, press Ctrl+C to copy'); return; }
    btn.classList.add('done');
    btn.setAttribute('aria-label', 'Copied');
    clearTimeout(btn._t);
    btn._t = setTimeout(() => { btn.classList.remove('done'); btn.setAttribute('aria-label', label); }, 1600);
  });
});
