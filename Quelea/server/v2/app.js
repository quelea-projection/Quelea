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
    'app.title': 'Quelea Remote',
    'menu.open': 'Schedule',
    'menu.back': 'Back',
    'drawer.schedule': 'Schedule',
    'page.add': 'Add songs and bible passages',
    'page.notices': 'Notices',
    'page.media': 'Images and videos',
    'page.themes': 'Themes',
    'media.images': 'Images',
    'media.videos': 'Videos',
    'media.uploadImages': 'Upload images from this device',
    'media.uploadVideos': 'Upload videos from this device',
    'media.empty': 'Nothing here yet. Files in Quelea\'s library show up here.',
    'media.add': 'Add',
    'media.added': 'Added to the schedule',
    'media.uploading': 'Uploading %s…',
    'media.uploaded': 'Uploaded %s',
    'media.uploadFailed': 'Could not upload %s',
    'app.remote': 'Remote',
    'live.prev': 'Previous section',
    'live.next': 'Next section',
    'live.prevItem': 'Previous item',
    'live.nextItem': 'Next item',
    'live.logo': 'Logo',
    'live.black': 'Black screen',
    'live.clear': 'Clear text',
    'live.nothing': 'Nothing is live.',
    'live.slide': 'Slide',
    'media.play': '▶ Play', 'media.pause': '❚❚ Pause', 'media.stop': '■ Stop', 'media.loop': 'Loop',
    'media.position': 'Position',
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
    'song.new': 'New song',
    'song.title': 'Title',
    'song.author': 'Author',
    'song.lyrics': 'Verse 1\nFirst line\nSecond line\n\nChorus\n…',
    'song.help': 'Leave a blank line between sections. A first line such as “Verse 1”, “Chorus” or “Bridge” names the section.',
    'song.save': 'Save',
    'song.saveAdd': 'Save and add',
    'song.saved': 'Song saved',
    'notice.title': 'Notices',
    'notice.text': 'Text that scrolls across the screen',
    'notice.once': 'Once', 'notice.twice': 'Twice', 'notice.three': '3 times', 'notice.five': '5 times',
    'notice.always': 'Until removed',
    'notice.color': 'Text colour',
    'notice.show': 'Show',
    'notice.shown': 'Notice shown',
    'notice.remove': 'Remove',
    'notice.left': '%s more times',
    'notice.leftOne': 'Once more',
    'notice.untilRemoved': 'Until removed',
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
    'app.title': 'Quelea Fjärrstyrning',
    'menu.open': 'Gudstjänstordning',
    'menu.back': 'Tillbaka',
    'drawer.schedule': 'Gudstjänstordning',
    'page.add': 'Lägg till sånger och bibeltext',
    'page.notices': 'Meddelanden',
    'page.media': 'Bilder och videor',
    'page.themes': 'Teman',
    'media.images': 'Bilder',
    'media.videos': 'Videor',
    'media.uploadImages': 'Ladda upp bilder från enheten',
    'media.uploadVideos': 'Ladda upp videor från enheten',
    'media.empty': 'Här finns inget än. Filer i Queleas bibliotek visas här.',
    'media.add': 'Lägg till',
    'media.added': 'Tillagd i schemat',
    'media.uploading': 'Laddar upp %s…',
    'media.uploaded': '%s är uppladdad',
    'media.uploadFailed': 'Kunde inte ladda upp %s',
    'app.remote': 'Fjärrstyrning',
    'live.prev': 'Föregående del',
    'live.next': 'Nästa del',
    'live.prevItem': 'Föregående post',
    'live.nextItem': 'Nästa post',
    'live.logo': 'Logga',
    'live.black': 'Svart skärm',
    'live.clear': 'Blank skärm',
    'live.nothing': 'Inget är live.',
    'live.slide': 'Bild',
    'media.play': '▶ Spela', 'media.pause': '❚❚ Pausa', 'media.stop': '■ Stopp', 'media.loop': 'Upprepa',
    'media.position': 'Position',
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
    'song.new': 'Ny sång',
    'song.title': 'Titel',
    'song.author': 'Författare',
    'song.lyrics': 'Verse 1\nFörsta raden\nAndra raden\n\nChorus\n…',
    'song.help': 'Lämna en tom rad mellan delarna. En första rad som ”Verse 1”, ”Chorus” eller ”Bridge” blir delens namn (Quelea känner igen de engelska namnen).',
    'song.save': 'Spara',
    'song.saveAdd': 'Spara och lägg till',
    'song.saved': 'Sången är sparad',
    'notice.title': 'Meddelanden',
    'notice.text': 'Text som rullar över skärmen',
    'notice.once': 'En gång', 'notice.twice': 'Två gånger', 'notice.three': '3 gånger', 'notice.five': '5 gånger',
    'notice.always': 'Tills det tas bort',
    'notice.color': 'Textfärg',
    'notice.show': 'Visa',
    'notice.shown': 'Meddelandet visas',
    'notice.remove': 'Ta bort',
    'notice.left': '%s gånger till',
    'notice.leftOne': '1 gång till',
    'notice.untilRemoved': 'Tills det tas bort',
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
  document.querySelectorAll('[data-t-title]').forEach(node => { node.title = t(node.dataset.tTitle); });
  // Icon-only buttons: the label is for screen readers and the tooltip.
  document.querySelectorAll('[data-t-label]').forEach(node => {
    node.title = t(node.dataset.tLabel);
    node.setAttribute('aria-label', t(node.dataset.tLabel));
  });
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

// ---- Live tab ----------------------------------------------------------

function renderLive() {
  $('live-title').textContent = live && live.title ? live.title : '';
  $('live-heading').hidden = !(live && live.title);
  ['logo', 'black', 'clear'].forEach(name => {
    $('toggle-' + name).setAttribute('aria-pressed', live && live[name] ? 'true' : 'false');
  });
  const nothing = !live || !live.itemId;
  $('nothing-live').hidden = !nothing;
  ['prev', 'next'].forEach(id => { $(id).disabled = nothing; });
  renderMedia();
}

// The live video, audio file or timer. Positions are not sent as events,
// so the state is fetched every second while something plays.
let mediaPoll = null;
let seeking = false;

function renderMedia() {
  const media = live && live.media;
  $('media-tools').hidden = !media;
  clearInterval(mediaPoll);
  mediaPoll = null;
  if (!media) return;
  $('media-play').textContent = t(media.playing ? 'media.pause' : 'media.play');
  const timer = media.kind === 'timer';
  $('media-loop').hidden = timer;
  $('media-position').hidden = timer;
  $('media-loop').setAttribute('aria-pressed', media.loop ? 'true' : 'false');
  if (!seeking && typeof media.position === 'number') $('media-position').value = media.position;
  if (media.playing && !timer) {
    mediaPoll = setInterval(() => {
      if (document.hidden || $('tab-live').hidden) return;
      api.get('/state').then(applyState).catch(() => { /* the WebSocket reports connection problems */ });
    }, 1000);
  }
}

function renderSections() {
  const list = $('sections');
  list.replaceChildren();
  if (!sections || !sections.itemId) return;
  sections.sections.forEach((section, index) => {
    const hasText = section.lines.length > 0;
    const item = el('li', {
      class: (hasText ? '' : 'slide ') + (index === sections.sectionIndex ? 'current' : ''),
      onclick: () => run(() => api.post('/live/section', { index }).then(applyState))
    },
    section.title ? el('span', { class: 'stitle ' + sectionKind(section.title) }, section.title) : null,
    hasText ? section.lines.join('\n') : t('live.slide') + ' ' + (index + 1));
    list.append(item);
  });
  scrollToCurrent();
}

// Section headings take Quelea's colours: verses, choruses and the rest.
function sectionKind(title) {
  const lower = title.toLowerCase();
  if (lower.startsWith('verse')) return 'verse';
  if (lower.startsWith('chorus') || lower.startsWith('pre-chorus') || lower.startsWith('pre chorus')) return 'chorus';
  return 'other';
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
  $('media-play').onclick = () => run(() => api.post(live.media.playing ? '/live/media/pause' : '/live/media/play')
    .then(applyState));
  $('media-stop').onclick = () => run(() => api.post('/live/media/stop').then(applyState));
  $('media-loop').onclick = () => run(() => api.put('/live/media', { loop: !live.media.loop }).then(applyState));
  const position = $('media-position');
  position.oninput = () => { seeking = true; };
  position.onchange = () => run(() => api.put('/live/media', { position: Number(position.value) })
    .then(applyState)).finally(() => { seeking = false; });
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
    list.append(el('li', { class: item.live ? 'live' : item.preview ? 'preview' : '' },
      el('span', { class: 'live-dot', 'aria-hidden': 'true' }),
      el('span', { class: 'item-title', onclick: () => { closeDrawer(); run(() => api.post('/schedule/items/' + item.id + '/live').then(applyState)); } },
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

// ---- Notices (in the add tab) ------------------------------------------

let notices = [];

function renderNotices() {
  const list = $('notices');
  list.replaceChildren();
  notices.forEach(notice => list.append(el('li', {},
    el('span', {}, notice.text,
      el('small', {}, notice.timesLeft == null ? t('notice.untilRemoved')
        : notice.timesLeft === 1 ? t('notice.leftOne') : t('notice.left', notice.timesLeft))),
    el('button', { onclick: () => removeNotice(notice) }, t('notice.remove')))));
}

async function loadNotices() {
  const result = await run(() => api.get('/notices'));
  if (!result) return;
  notices = result.notices;
  renderNotices();
  const saved = $('notice-saved');
  saved.replaceChildren();
  result.saved.forEach(notice => saved.append(el('button', {
    type: 'button',
    onclick: () => {
      $('notice-text').value = notice.text;
      $('notice-times').value = [1, 2, 3, 5, 0].includes(notice.times) ? String(notice.times) : '1';
      $('notice-color').value = notice.color;
      $('notice-text').focus();
    }
  }, notice.text)));
}

async function removeNotice(notice) {
  const result = await run(() => api.del('/notices/' + encodeURIComponent(notice.id)));
  if (result) {
    notices = result.notices;
    renderNotices();
  }
}

function setupNotices() {
  $('notice-form').onsubmit = async event => {
    event.preventDefault();
    const text = $('notice-text').value.trim();
    if (!text) return;
    const result = await run(() => api.post('/notices',
      { text, times: Number($('notice-times').value), color: $('notice-color').value }));
    if (result) {
      notices = result.notices;
      renderNotices();
      $('notice-text').value = '';
      toast(t('notice.shown'));
    }
  };
}

// ---- New song (in the add tab) -----------------------------------------

function setupNewSong() {
  $('new-song-form').onsubmit = async event => {
    event.preventDefault();
    const andAdd = event.submitter && event.submitter.id === 'new-song-add';
    const song = await run(() => api.post('/songs', {
      title: $('new-song-title').value,
      author: $('new-song-author').value,
      lyrics: $('new-song-lyrics').value
    }));
    if (!song) return;
    $('new-song-form').reset();
    $('new-song').open = false;
    if (andAdd) await addToSchedule({ songId: song.id });
    else toast(t('song.saved'));
  };
}

async function loadTranslations() {
  const translations = await run(() => api.get('/bible/translations'));
  const select = $('bible-translation');
  select.replaceChildren();
  (translations || []).forEach(translation => select.append(
    el('option', { value: translation.name, selected: translation.isDefault }, translation.name)));
}

// ---- Images and videos (Quelea's library) --------------------------------

let mediaKind = 'images';
const mediaThumbs = [];

async function loadMedia() {
  const kind = mediaKind;
  const files = await run(() => api.get('/library/' + kind));
  if (kind !== mediaKind) return;
  mediaThumbs.splice(0).forEach(url => URL.revokeObjectURL(url));
  const list = $('media-list');
  list.replaceChildren();
  $('media-empty').hidden = !files || files.length > 0;
  (files || []).forEach(file => {
    const image = el('img', { alt: '' });
    const field = kind === 'images' ? 'image' : 'video';
    list.append(el('li', { class: kind === 'videos' ? 'video' : '' },
      image,
      el('span', {}, file.name),
      el('button', { onclick: () => addToSchedule({ [field]: file.name }) }, t('media.add'))));
    // Thumbnails need the token, so they are fetched rather than linked.
    api.blob('/library/' + kind + '/thumb.png?name=' + encodeURIComponent(file.name))
      .then(blob => { const url = URL.createObjectURL(blob); mediaThumbs.push(url); image.src = url; })
      .catch(() => { /* no thumbnail (yet) */ });
  });
}

function setMediaKind(kind) {
  mediaKind = kind;
  $('media-images').setAttribute('aria-selected', String(kind === 'images'));
  $('media-videos').setAttribute('aria-selected', String(kind === 'videos'));
  $('upload-input').accept = kind === 'images' ? 'image/*' : 'video/*';
  $('upload-label').textContent = t(kind === 'images' ? 'media.uploadImages' : 'media.uploadVideos');
  loadMedia();
}

/** Upload one file with progress: fetch cannot report upload progress. */
function uploadFile(kind, file, onProgress) {
  return new Promise((resolve, reject) => {
    const xhr = new XMLHttpRequest();
    xhr.open('PUT', API + '/library/' + kind + '?name=' + encodeURIComponent(file.name));
    if (token) xhr.setRequestHeader('Authorization', 'Bearer ' + token);
    xhr.setRequestHeader('Content-Type', file.type || 'application/octet-stream');
    xhr.upload.onprogress = event => { if (event.lengthComputable) onProgress(event.loaded / event.total); };
    xhr.onload = () => {
      let body = null;
      try { body = JSON.parse(xhr.responseText); } catch (e) { /* not JSON */ }
      if (xhr.status === 200) resolve(body);
      else reject(new Error(body && body.error ? body.error.message : 'HTTP ' + xhr.status));
    };
    xhr.onerror = () => reject(new Error(t('error.network')));
    xhr.send(file);
  });
}

function setupMedia() {
  $('media-images').onclick = () => setMediaKind('images');
  $('media-videos').onclick = () => setMediaKind('videos');
  $('upload-input').onchange = async () => {
    const files = Array.from($('upload-input').files);
    $('upload-input').value = '';
    const kind = mediaKind;
    const bar = $('upload-progress');
    for (const file of files) {
      bar.hidden = false;
      bar.querySelector('span').textContent = t('media.uploading', file.name);
      bar.querySelector('div').style.width = '0%';
      try {
        const saved = await uploadFile(kind, file, fraction => {
          bar.querySelector('div').style.width = Math.round(fraction * 100) + '%';
        });
        toast(t('media.uploaded', saved ? saved.name : file.name));
      } catch (e) {
        toast(t('media.uploadFailed', file.name) + ': ' + e.message);
      }
    }
    bar.hidden = true;
    if (kind === mediaKind) loadMedia();
  };
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

// ---- Pages and the drawer ----------------------------------------------
// Like the mobile app: the live view, the schedule in a drawer, and pages
// for adding, notices, images and videos, and themes with a back button.

const TAB_LOADERS = { add: loadTranslations, notices: loadNotices, media: loadMedia, themes: loadThemes };
const loadedTabs = new Set();
let currentPage = 'live';

function showTab(name) {
  currentPage = name;
  document.querySelectorAll('.tab').forEach(tab => { tab.hidden = tab.id !== 'tab-' + name; });
  const isLive = name === 'live';
  $('menu-button').hidden = !isLive;
  $('back-button').hidden = isLive;
  $('page-title').textContent = isLive ? t('app.title') : t('page.' + name);
  if (TAB_LOADERS[name] && !loadedTabs.has(name)) {
    loadedTabs.add(name);
    TAB_LOADERS[name]();
  }
  window.scrollTo(0, 0);
  if (isLive) scrollToCurrent();
}

function openDrawer() {
  $('drawer').classList.add('open');
  $('drawer').setAttribute('aria-hidden', 'false');
  $('scrim').hidden = false;
}

function closeDrawer() {
  $('drawer').classList.remove('open');
  $('drawer').setAttribute('aria-hidden', 'true');
  $('scrim').hidden = true;
}

function setupPages() {
  $('menu-button').onclick = openDrawer;
  $('scrim').onclick = closeDrawer;
  $('back-button').onclick = () => showTab('live');
  document.querySelectorAll('[data-page]').forEach(button => {
    button.onclick = () => {
      closeDrawer();
      showTab(button.dataset.page);
      if (button.dataset.open) $(button.dataset.open).open = true;
    };
  });
  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') { closeDrawer(); if (currentPage !== 'live') showTab('live'); }
  });
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
    loadSchedule();
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
        // live.media follows for an item that plays.
        live.media = null;
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
    case 'live.media':
      if (live && live.itemId === data.itemId) {
        live.media = data.media;
        renderLive();
        // Events carry no position, and a stopped or finished video has
        // jumped back to the start: read it.
        if (data.media && data.media.kind !== 'timer') api.get('/state').then(applyState).catch(() => {});
      }
      break;
    case 'live.screen':
      if (live) Object.assign(live, data);
      renderLive();
      break;
    case 'notices.changed':
      notices = data.notices || [];
      renderNotices();
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
  closeDrawer();
  ['menu-button', 'back-button', 'search-button', 'notices-button'].forEach(id => { $(id).hidden = true; });
  $('page-title').textContent = 'Quelea';
  $('pair-view').hidden = false;
  $('pair-error').textContent = message || '';
  $('live-title').textContent = '';
  setConnection('offline');
}

function showApp() {
  $('pair-view').hidden = true;
  $('app-view').hidden = false;
  $('search-button').hidden = false;
  $('notices-button').hidden = false;
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
    closeDrawer();
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
  setupNotices();
  setupNewSong();
  setupMedia();
  setupPages();
  // Reconnect at once when a phone wakes up or the tab comes back.
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden && token && !socket) connect();
  });
  if (token) showApp(); else showPairing();
});
