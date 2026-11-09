/*
 * Quelea Remote v2: the web remote served by Quelea on the v2 port.
 * Plain JavaScript, no build step. Talks only to /api/v2 on the same server.
 * Text from Quelea (titles, lyrics) is always set with textContent, never as HTML.
 */
'use strict';

const API = '/api/v2';
const TOKEN_KEY = 'quelea.v2.token';

// ---- Texts -------------------------------------------------------------

const TEXTS = {
  en: {
    'pair.title': 'Pair this browser',
    'pair.help': 'In Quelea, open Settings → Server and choose “Pair new device”. Enter the code shown there, or the remote control password.',
    'pair.code': 'Pairing code',
    'pair.or': 'or',
    'pair.password': 'Remote control password',
    'pair.name': 'Name of this device',
    'pair.button': 'Pair',
    'pair.defaultName': 'Web browser',
    'pair.needSecret': 'Enter the code or the password.',
    'live.prev': '◀ Previous',
    'live.next': 'Next ▶',
    'live.prevItem': '⏮ Previous item',
    'live.nextItem': 'Next item ⏭',
    'live.logo': 'Logo',
    'live.black': 'Black',
    'live.clear': 'Clear',
    'live.nothing': 'Nothing is live.',
    'live.slide': 'Slide',
    'live.transposeDown': '♭ −1',
    'live.transposeUp': '♯ +1',
    'live.chords': 'Chords',
    'live.transposed': 'Transposed for this service only',
    'schedule.empty': 'The schedule is empty.',
    'schedule.up': 'Move up',
    'schedule.down': 'Move down',
    'schedule.remove': 'Remove',
    'schedule.removeConfirm': 'Remove “%s” from the schedule?',
    'add.songs': 'Songs',
    'add.songSearch': 'Title, author or lyrics',
    'add.search': 'Search',
    'add.noSongs': 'No songs found.',
    'add.bible': 'Bible',
    'add.bibleRef': 'For example John 3:16-18',
    'add.show': 'Show',
    'add.add': 'Add to schedule',
    'add.added': 'Added to the schedule',
    'tab.live': 'Live',
    'tab.schedule': 'Schedule',
    'tab.add': 'Add',
    'tab.themes': 'Themes',
    'more.unpair': 'Unpair this browser',
    'more.unpairConfirm': 'Unpair this browser? You will need a new code to use it again.',
    'theme.set': 'Theme changed',
    'conn.online': 'Connected',
    'conn.connecting': 'Connecting…',
    'conn.offline': 'Not connected',
    'error.changed': 'Someone else changed the schedule first. Showing the latest version.',
    'error.unavailable': 'Quelea is not responding.',
    'error.network': 'Could not reach Quelea.',
    'error.revoked': 'This browser was unpaired in Quelea.',
    'type.song': 'Song', 'type.bible': 'Bible', 'type.presentation': 'Presentation', 'type.pdf': 'PDF',
    'type.images': 'Images', 'type.image': 'Image', 'type.video': 'Video', 'type.audio': 'Audio',
    'type.timer': 'Timer', 'type.web': 'Web page', 'type.other': 'Other'
  },
  sv: {
    'pair.title': 'Para den här webbläsaren',
    'pair.help': 'Öppna Inställningar → Server i Quelea och välj ”Para ny enhet”. Skriv in koden som visas där, eller fjärrstyrningens lösenord.',
    'pair.code': 'Parningskod',
    'pair.or': 'eller',
    'pair.password': 'Fjärrstyrningens lösenord',
    'pair.name': 'Namn på den här enheten',
    'pair.button': 'Para',
    'pair.defaultName': 'Webbläsare',
    'pair.needSecret': 'Skriv in koden eller lösenordet.',
    'live.prev': '◀ Föregående',
    'live.next': 'Nästa ▶',
    'live.prevItem': '⏮ Föregående post',
    'live.nextItem': 'Nästa post ⏭',
    'live.logo': 'Logo',
    'live.black': 'Svart',
    'live.clear': 'Rensa',
    'live.nothing': 'Inget är live.',
    'live.slide': 'Bild',
    'live.transposeDown': '♭ −1',
    'live.transposeUp': '♯ +1',
    'live.chords': 'Ackord',
    'live.transposed': 'Transponerad bara för den här gången',
    'schedule.empty': 'Schemat är tomt.',
    'schedule.up': 'Flytta upp',
    'schedule.down': 'Flytta ned',
    'schedule.remove': 'Ta bort',
    'schedule.removeConfirm': 'Ta bort ”%s” ur schemat?',
    'add.songs': 'Sånger',
    'add.songSearch': 'Titel, författare eller text',
    'add.search': 'Sök',
    'add.noSongs': 'Inga sånger hittades.',
    'add.bible': 'Bibel',
    'add.bibleRef': 'Till exempel Joh 3:16-18',
    'add.show': 'Visa',
    'add.add': 'Lägg till i schemat',
    'add.added': 'Tillagd i schemat',
    'tab.live': 'Live',
    'tab.schedule': 'Schema',
    'tab.add': 'Lägg till',
    'tab.themes': 'Teman',
    'more.unpair': 'Avpara den här webbläsaren',
    'more.unpairConfirm': 'Avpara den här webbläsaren? Du behöver en ny kod för att använda den igen.',
    'theme.set': 'Temat är bytt',
    'conn.online': 'Ansluten',
    'conn.connecting': 'Ansluter…',
    'conn.offline': 'Inte ansluten',
    'error.changed': 'Någon annan hann ändra schemat först. Visar den senaste versionen.',
    'error.unavailable': 'Quelea svarar inte.',
    'error.network': 'Kunde inte nå Quelea.',
    'error.revoked': 'Den här webbläsaren avparades i Quelea.',
    'type.song': 'Sång', 'type.bible': 'Bibel', 'type.presentation': 'Presentation', 'type.pdf': 'PDF',
    'type.images': 'Bilder', 'type.image': 'Bild', 'type.video': 'Video', 'type.audio': 'Ljud',
    'type.timer': 'Timer', 'type.web': 'Webbsida', 'type.other': 'Övrigt'
  }
};

const LANG = (navigator.language || 'en').toLowerCase().startsWith('sv') ? 'sv' : 'en';

function t(key, arg) {
  const text = TEXTS[LANG][key] || TEXTS.en[key] || key;
  return arg === undefined ? text : text.replace('%s', arg);
}

function applyTexts() {
  document.documentElement.lang = LANG;
  document.querySelectorAll('[data-t]').forEach(node => { node.textContent = t(node.dataset.t); });
  document.querySelectorAll('[data-t-placeholder]').forEach(node => { node.placeholder = t(node.dataset.tPlaceholder); });
}

// ---- Small helpers -----------------------------------------------------

const $ = id => document.getElementById(id);

/** Create an element; strings become text nodes, so nothing is parsed as HTML. */
function el(tag, props, ...children) {
  const node = document.createElement(tag);
  Object.entries(props || {}).forEach(([key, value]) => {
    if (key === 'class') node.className = value;
    else if (key.startsWith('on')) node.addEventListener(key.slice(2), value);
    else if (value !== undefined && value !== null && value !== false) node.setAttribute(key, value === true ? '' : value);
  });
  children.flat().forEach(child => {
    if (child !== null && child !== undefined) node.append(child instanceof Node ? child : String(child));
  });
  return node;
}

let toastTimer;
function toast(message) {
  const box = $('toast');
  box.textContent = message;
  box.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { box.hidden = true; }, 3000);
}

// ---- API ---------------------------------------------------------------

class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

let token = null;
try { token = localStorage.getItem(TOKEN_KEY); } catch (e) { /* storage blocked */ }

function saveToken(value) {
  token = value;
  try {
    if (value) localStorage.setItem(TOKEN_KEY, value);
    else localStorage.removeItem(TOKEN_KEY);
  } catch (e) { /* storage blocked: the token lives until the page is closed */ }
}

async function request(method, path, body, raw) {
  const headers = {};
  if (token) headers.Authorization = 'Bearer ' + token;
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  let response;
  try {
    response = await fetch(API + path, { method, headers, body: body === undefined ? undefined : JSON.stringify(body) });
  } catch (e) {
    throw new ApiError(0, 'network', t('error.network'));
  }
  if (response.status === 401 && token && !path.startsWith('/pair')) {
    // The token is no longer valid: revoked in Quelea.
    signedOut(t('error.revoked'));
    throw new ApiError(401, 'unauthorized', t('error.revoked'));
  }
  if (!response.ok) {
    let code = 'error';
    let message = response.statusText;
    try {
      const error = (await response.json()).error;
      code = error.code;
      message = error.message;
    } catch (e) { /* not JSON */ }
    throw new ApiError(response.status, code, message);
  }
  return raw ? response.blob() : response.json();
}

const api = {
  get: path => request('GET', path),
  post: (path, body) => request('POST', path, body === undefined ? {} : body),
  put: (path, body) => request('PUT', path, body),
  del: path => request('DELETE', path),
  blob: path => request('GET', path, undefined, true)
};

/** Run an action from a button and report what went wrong. */
async function run(action) {
  try {
    return await action();
  } catch (e) {
    if (e.code === 'conflict') {
      toast(t('error.changed'));
      loadSchedule();
    } else if (e.code === 'unavailable') {
      toast(t('error.unavailable'));
    } else if (e.status !== 401) {
      toast(e.message || t('error.network'));
    }
    return null;
  }
}

// ---- State -------------------------------------------------------------

let live = null;          // the latest LiveState
let sections = null;      // the latest LiveSections
let schedule = null;      // the latest ScheduleSnapshot
let socket = null;
let lastSeq = 0;
let retryDelay = 500;
let pingTimer = null;
let pongTimer = null;
let reconnectTimer = null;
let showChords = false;
try { showChords = localStorage.getItem('quelea.v2.chords') === '1'; } catch (e) { /* storage blocked */ }

// ---- Live tab ----------------------------------------------------------

function renderLive() {
  $('live-title').textContent = live && live.title ? live.title : '';
  ['logo', 'black', 'clear'].forEach(name => {
    $('toggle-' + name).setAttribute('aria-pressed', live && live[name] ? 'true' : 'false');
  });
  const nothing = !live || !live.itemId;
  $('nothing-live').hidden = !nothing;
  $('song-tools').hidden = !live || live.itemType !== 'song';
  $('toggle-chords').setAttribute('aria-pressed', showChords ? 'true' : 'false');
  ['prev', 'next'].forEach(id => { $(id).disabled = nothing; });
}

function renderSections() {
  const list = $('sections');
  list.replaceChildren();
  if (!sections || !sections.itemId) return;
  sections.sections.forEach((section, index) => {
    const hasText = section.lines.length > 0;
    const chords = showChords && section.chordLines;
    const item = el('li', {
      class: (hasText ? '' : 'slide ') + (chords ? 'chords ' : '') + (index === sections.sectionIndex ? 'current' : ''),
      onclick: () => run(() => api.post('/live/section', { index }).then(applyState))
    },
    section.title ? el('span', { class: 'stitle' }, section.title) : null,
    hasText ? (chords ? section.chordLines : section.lines).join('\n') : t('live.slide') + ' ' + (index + 1));
    list.append(item);
  });
  scrollToCurrent();
}

function markSection(index) {
  if (sections) sections.sectionIndex = index;
  Array.from($('sections').children).forEach((item, i) => item.classList.toggle('current', i === index));
  scrollToCurrent();
}

function scrollToCurrent() {
  const current = $('sections').querySelector('.current');
  if (current && !$('tab-live').hidden) current.scrollIntoView({ block: 'nearest', behavior: 'smooth' });
}

async function loadSections() {
  sections = await run(() => api.get('/live/sections'));
  renderSections();
}

function applyState(state) {
  if (!state) return;
  const itemChanged = !live || live.itemId !== state.itemId;
  live = state;
  renderLive();
  if (itemChanged) loadSections();
  else markSection(state.sectionIndex);
}

function setupLiveTab() {
  $('next').onclick = () => run(() => api.post('/live/next').then(applyState));
  $('prev').onclick = () => run(() => api.post('/live/prev').then(applyState));
  $('next-item').onclick = () => run(() => api.post('/live/item/next').then(applyState));
  $('prev-item').onclick = () => run(() => api.post('/live/item/prev').then(applyState));
  ['logo', 'black', 'clear'].forEach(name => {
    $('toggle-' + name).onclick = () => run(() => api.put('/live/screen', { [name]: !(live && live[name]) }).then(applyState));
  });
  // The transposed song replaces the live item; live.item then reloads the sections.
  const transpose = semitones => run(() => api.post('/schedule/items/' + live.itemId + '/transpose', { semitones })
    .then(() => toast(t('live.transposed'))));
  $('transpose-down').onclick = () => transpose(-1);
  $('transpose-up').onclick = () => transpose(1);
  $('toggle-chords').onclick = () => {
    showChords = !showChords;
    try { localStorage.setItem('quelea.v2.chords', showChords ? '1' : '0'); } catch (e) { /* storage blocked */ }
    renderLive();
    renderSections();
  };
}

// ---- Schedule tab ------------------------------------------------------

async function loadSchedule() {
  const loaded = await run(() => api.get('/schedule'));
  if (loaded) {
    schedule = loaded;
    renderSchedule();
  }
}

function renderSchedule() {
  const list = $('schedule');
  list.replaceChildren();
  const items = schedule ? schedule.items : [];
  $('schedule-empty').hidden = items.length > 0;
  items.forEach((item, index) => {
    const change = (method, path, body) => run(() =>
      request(method, path + '?ifRevision=' + schedule.revision, body).then(result => {
        schedule = result.schedule;
        renderSchedule();
      }));
    list.append(el('li', { class: item.live ? 'live' : '' },
      el('span', { class: 'item-title', onclick: () => run(() => api.post('/schedule/items/' + item.id + '/live').then(applyState)) },
        item.title, el('span', { class: 'item-type' }, t('type.' + item.type))),
      el('button', { title: t('schedule.up'), 'aria-label': t('schedule.up'), disabled: index === 0,
        onclick: () => change('POST', '/schedule/items/' + item.id + '/move', { to: index - 1 }) }, '↑'),
      el('button', { title: t('schedule.down'), 'aria-label': t('schedule.down'), disabled: index === items.length - 1,
        onclick: () => change('POST', '/schedule/items/' + item.id + '/move', { to: index + 1 }) }, '↓'),
      el('button', { title: t('schedule.remove'), 'aria-label': t('schedule.remove'),
        onclick: () => { if (confirm(t('schedule.removeConfirm', item.title))) change('DELETE', '/schedule/items/' + item.id); } }, '✕')));
  });
}

// ---- Add tab -----------------------------------------------------------

let biblePassage = null;

async function addToSchedule(body) {
  const result = await run(() => api.post('/schedule/items', body));
  if (result) {
    schedule = result.schedule;
    renderSchedule();
    toast(t('add.added'));
  }
}

function setupAddTab() {
  $('song-search').onsubmit = async event => {
    event.preventDefault();
    const query = $('song-query').value.trim();
    if (!query) return;
    const songs = await run(() => api.get('/songs?limit=30&q=' + encodeURIComponent(query)));
    const list = $('song-results');
    list.replaceChildren();
    if (!songs) return;
    if (songs.length === 0) list.append(el('li', {}, el('span', { class: 'muted' }, t('add.noSongs'))));
    songs.forEach(song => list.append(el('li', {},
      el('span', {}, song.title, song.author ? el('small', {}, song.author) : null),
      el('button', { onclick: () => addToSchedule({ songId: song.id }) }, t('add.add')))));
  };

  $('bible-form').onsubmit = async event => {
    event.preventDefault();
    const translation = $('bible-translation').value;
    const reference = $('bible-ref').value.trim();
    if (!reference) return;
    biblePassage = await run(() => api.get('/bible/passage?translation=' + encodeURIComponent(translation)
      + '&ref=' + encodeURIComponent(reference)));
    const preview = $('bible-preview');
    preview.replaceChildren();
    $('bible-add').hidden = !biblePassage;
    if (!biblePassage) return;
    preview.append(el('strong', {}, biblePassage.location), '\n',
      biblePassage.verses.map(verse => verse.verse + ' ' + verse.text).join('\n'));
  };

  $('bible-add').onclick = event => {
    event.preventDefault();
    if (biblePassage) addToSchedule({ bible: { translation: biblePassage.translation, reference: biblePassage.location } });
  };
}

async function loadTranslations() {
  const translations = await run(() => api.get('/bible/translations'));
  const select = $('bible-translation');
  select.replaceChildren();
  (translations || []).forEach(translation => select.append(
    el('option', { value: translation.name, selected: translation.isDefault }, translation.name)));
}

// ---- Themes tab --------------------------------------------------------

const thumbUrls = [];

async function loadThemes() {
  const themes = await run(() => api.get('/themes'));
  thumbUrls.splice(0).forEach(url => URL.revokeObjectURL(url));
  const list = $('themes');
  list.replaceChildren();
  (themes || []).forEach(theme => {
    const image = el('img', { alt: '' });
    list.append(el('li', { onclick: () => run(() => api.put('/live/theme', { name: theme.name })
      .then(state => { applyState(state); toast(t('theme.set')); })) }, image, el('span', {}, theme.name)));
    // Images need the token, so they are fetched rather than linked.
    api.blob('/themes/thumb.png?name=' + encodeURIComponent(theme.name))
      .then(blob => { const url = URL.createObjectURL(blob); thumbUrls.push(url); image.src = url; })
      .catch(() => { /* no thumbnail */ });
  });
}

// ---- Tabs --------------------------------------------------------------

const TAB_LOADERS = { schedule: loadSchedule, add: loadTranslations, themes: loadThemes };
const loadedTabs = new Set();

function showTab(name) {
  document.querySelectorAll('.tab').forEach(tab => { tab.hidden = tab.id !== 'tab-' + name; });
  document.querySelectorAll('#tabs button').forEach(button => button.classList.toggle('active', button.dataset.tab === name));
  if (TAB_LOADERS[name] && !loadedTabs.has(name)) {
    loadedTabs.add(name);
    TAB_LOADERS[name]();
  }
  if (name === 'live') scrollToCurrent();
}

// ---- WebSocket ---------------------------------------------------------

function setConnection(state) {
  const dot = $('conn');
  dot.dataset.state = state;
  dot.title = t('conn.' + state);
}

function connect() {
  clearTimeout(reconnectTimer);
  if (!token) return;
  setConnection('connecting');
  const url = (location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + API + '/ws';
  const ws = new WebSocket(url);
  socket = ws;
  ws.onopen = () => {
    ws.send(JSON.stringify({ type: 'auth', token }));
    // Browsers cannot send WebSocket pings, so the protocol has its own.
    pingTimer = setInterval(() => {
      ws.send(JSON.stringify({ type: 'ping' }));
      pongTimer = setTimeout(() => ws.close(), 10000);
    }, 20000);
  };
  ws.onmessage = event => handleMessage(JSON.parse(event.data));
  ws.onclose = event => {
    clearInterval(pingTimer);
    clearTimeout(pongTimer);
    if (socket !== ws) return;
    socket = null;
    setConnection('offline');
    if (event.code === 4401) {
      signedOut(t('error.revoked'));
      return;
    }
    reconnectTimer = setTimeout(connect, retryDelay);
    retryDelay = Math.min(retryDelay * 2, 8000);
  };
}

function handleMessage(message) {
  if (message.type === 'pong') {
    clearTimeout(pongTimer);
    return;
  }
  if (message.type === 'device.revoked') {
    signedOut(t('error.revoked'));
    return;
  }
  if (message.type === 'snapshot') {
    retryDelay = 500;
    setConnection('online');
    lastSeq = message.seq;
    live = null;
    applyState(message.data);
    loadedTabs.forEach(name => TAB_LOADERS[name]());
    return;
  }
  if (typeof message.seq === 'number') {
    if (message.seq <= lastSeq) return;
    lastSeq = message.seq;
  }
  const data = message.data || {};
  switch (message.type) {
    case 'live.item':
      if (live) {
        live.itemId = data.itemId;
        live.title = data.title;
        live.itemType = data.itemType;
      }
      renderLive();
      loadSections();
      if (schedule) loadSchedule();
      break;
    case 'live.section':
      if (live) live.sectionIndex = data.index;
      if (sections && sections.itemId === data.itemId) markSection(data.index);
      else loadSections();
      break;
    case 'live.screen':
      if (live) Object.assign(live, data);
      renderLive();
      break;
    case 'schedule.changed':
      if (!schedule || schedule.revision !== data.revision) loadSchedule();
      break;
    default:
      // Unknown events from newer Quelea versions are ignored.
      break;
  }
}

// ---- Pairing -----------------------------------------------------------

function showPairing(message) {
  $('app-view').hidden = true;
  $('tabs').hidden = true;
  $('pair-view').hidden = false;
  $('pair-error').textContent = message || '';
  $('live-title').textContent = '';
  setConnection('offline');
}

function showApp() {
  $('pair-view').hidden = true;
  $('app-view').hidden = false;
  $('tabs').hidden = false;
  showTab('live');
  connect();
}

function signedOut(message) {
  saveToken(null);
  const ws = socket;
  socket = null;
  if (ws) ws.close();
  live = sections = schedule = null;
  loadedTabs.clear();
  showPairing(message);
}

function setupPairing() {
  $('pair-name').value = t('pair.defaultName');
  $('pair-form').onsubmit = async event => {
    event.preventDefault();
    const code = $('pair-code').value.trim();
    const password = $('pair-password').value;
    if (!code && !password) {
      $('pair-error').textContent = t('pair.needSecret');
      return;
    }
    const body = { name: $('pair-name').value.trim() || t('pair.defaultName'), platform: 'web' };
    if (code) body.code = code; else body.password = password;
    try {
      const paired = await request('POST', '/pair', body);
      saveToken(paired.token);
      $('pair-code').value = '';
      $('pair-password').value = '';
      showApp();
    } catch (e) {
      $('pair-error').textContent = e.message;
    }
  };
  $('unpair').onclick = async () => {
    if (!confirm(t('more.unpairConfirm'))) return;
    await run(() => api.del('/devices/me'));
    signedOut('');
  };
}

// ---- Start -------------------------------------------------------------

document.addEventListener('DOMContentLoaded', () => {
  applyTexts();
  setupPairing();
  setupLiveTab();
  setupAddTab();
  document.querySelectorAll('#tabs button').forEach(button => { button.onclick = () => showTab(button.dataset.tab); });
  // Reconnect at once when a phone wakes up or the tab comes back.
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden && token && !socket) connect();
  });
  if (token) showApp(); else showPairing();
});
