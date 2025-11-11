async function loadRange(fromIso, toIso) {
    const url = `/api/dati?from=${encodeURIComponent(fromIso)}&to=${encodeURIComponent(toIso)}`;
    const res = await fetch(url);
    const data = await res.json();

    const amb = data.filter(d => d.temperatura != null || d.umiditaSuolo != null || d.pioggia != null);
    const prod = data.filter(d => d.resa != null);

    const ambLabels = amb.map(d => new Date(d.timestamp).toLocaleTimeString());
    chartTemperatura.data.labels = ambLabels;
    chartUmidita.data.labels = ambLabels;
    chartPioggia.data.labels = ambLabels;

    chartTemperatura.data.datasets[0].data = amb.map(d => d.temperatura);
    chartUmidita.data.datasets[0].data = amb.map(d => d.umiditaSuolo);
    chartPioggia.data.datasets[0].data = amb.map(d => d.pioggia);
    chartTemperatura.update(); chartUmidita.update(); chartPioggia.update();

    const prodLabels = prod.map(d => new Date(d.timestamp).toLocaleTimeString());
    chartResa.data.labels = prodLabels;
    chartResa.data.datasets[0].data = prod.map(d => d.resa);
    chartResa.update();
}

function setRangeMinutes(mins) {
    const to = new Date(); const from = new Date(to.getTime() - mins * 60000);
    loadRange(from.toISOString(), to.toISOString());
}
function setRangeToday() {
    const to = new Date(); const from = new Date(); from.setHours(0, 0, 0, 0);
    loadRange(from.toISOString(), to.toISOString());
}
function createLineChart(canvasId, labels, label, data) {
    const ctx = document.getElementById(canvasId).getContext('2d');
    return new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels.map(ts => new Date(ts).toLocaleTimeString()),
            datasets: [{ label: label, data: data, tension: .2 }]
        },
        options: {
            responsive: true, animation: false,
            scales: { x: { ticks: { maxRotation: 0, autoSkip: true } } }
        }
    });
}

const L = window.__INITIAL__ || { labelsAmb: [], tempList: [], umidList: [], pioggiaList: [], labelsProd: [], resaList: [] };
const chartTemperatura = createLineChart('chartTemperatura', L.labelsAmb, 'Temperatura (°C)', L.tempList);
const chartUmidita = createLineChart('chartUmidita', L.labelsAmb, 'Umidità suolo (%)', L.umidList);
const chartPioggia = createLineChart('chartPioggia', L.labelsAmb, 'Pioggia (mm)', L.pioggiaList);
const chartResa = createLineChart('chartResa', L.labelsProd, 'Resa', L.resaList);
const WIN = 20;
let winResa = (L.resaList || []).slice(-WIN);
let winAcqua = (L.acquaList || []).slice(-WIN);
let winCrescita = (L.crescitaList || []).slice(-WIN);

const sum = arr => arr.reduce((a, b) => a + (+b || 0), 0);
const computeWUE = () => { const sR = sum(winResa), sA = sum(winAcqua); return sA > 0 ? (sR / sA) : 0; };
function computeTrend() {
    const n = winCrescita.length; if (n < 6) return 0;
    const half = Math.floor(n / 2), avg1 = sum(winCrescita.slice(0, half)) / half, avg2 = sum(winCrescita.slice(half)) / (n - half);
    const d = avg2 - avg1; if (d > 0.02) return 1; if (d < -0.02) return -1; return 0;
}
function renderWue() { const el = document.getElementById('kpiWue'); if (el) el.textContent = computeWUE().toFixed(2); }
function renderTrend(t) {
    const el = document.getElementById('kpiTrend'); if (!el) return;
    el.textContent = t > 0 ? '↑' : (t < 0 ? '↓' : '→');
    el.classList.remove('up', 'down', 'flat'); el.classList.add(t > 0 ? 'up' : (t < 0 ? 'down' : 'flat'));
}
renderWue(); renderTrend(computeTrend());
let alertTimer = null; const TTL = 10000;
function checkAlerts(dto) {
    const S = window.__THRESHOLDS__ || {};
    const msgs = [];
    if (dto.temperatura != null && dto.temperatura >= S.S_CALDO) msgs.push(`Allerta caldo: temperatura ≥ ${S.S_CALDO}°C`);
    if (dto.umiditaSuolo != null && dto.umiditaSuolo <= S.S_SUOLO_SECCO) msgs.push(`Allerta suolo secco: umidità ≤ ${S.S_SUOLO_SECCO}%`);
    if (dto.pioggia != null && dto.pioggia >= S.S_PIOGGIA) msgs.push(`Evento meteo: pioggia intensa (≥${S.S_PIOGGIA} mm)`);
    const eff = (dto.acquaUtilizzata && dto.resa) ? (dto.resa / dto.acquaUtilizzata) : NaN;
    if (!isNaN(eff) && eff < S.S_EFF) msgs.push(`Efficienza idrica bassa (< ${S.S_EFF})`);
    const box = document.getElementById('alertBox'); if (!box) return;
    if (msgs.length) {
        box.innerHTML = msgs.map(m => '• ' + m).join('<br>'); box.style.display = 'block';
        if (alertTimer) clearTimeout(alertTimer); alertTimer = setTimeout(() => { box.style.display = 'none'; alertTimer = null; }, TTL);
    }
}

const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);
stompClient.debug = null;

stompClient.connect({}, function () {
    stompClient.subscribe('/topic/dati', function (message) {
        const dto = JSON.parse(message.body);
        const label = new Date(dto.timestamp).toLocaleTimeString();

        if (dto.temperatura != null) {
            chartTemperatura.data.labels.push(label);
            chartTemperatura.data.datasets[0].data.push(dto.temperatura);
            if (chartTemperatura.data.labels.length > 100) { chartTemperatura.data.labels.shift(); chartTemperatura.data.datasets[0].data.shift(); }
            chartTemperatura.update();
        }
        if (dto.umiditaSuolo != null) {
            chartUmidita.data.labels.push(label);
            chartUmidita.data.datasets[0].data.push(dto.umiditaSuolo);
            if (chartUmidita.data.labels.length > 100) { chartUmidita.data.labels.shift(); chartUmidita.data.datasets[0].data.shift(); }
            chartUmidita.update();
        }
        if (dto.pioggia != null) {
            chartPioggia.data.labels.push(label);
            chartPioggia.data.datasets[0].data.push(dto.pioggia);
            if (chartPioggia.data.labels.length > 100) { chartPioggia.data.labels.shift(); chartPioggia.data.datasets[0].data.shift(); }
            chartPioggia.update();
        }
        if (dto.resa != null) {
            chartResa.data.labels.push(label);
            chartResa.data.datasets[0].data.push(dto.resa);
            if (chartResa.data.labels.length > 100) { chartResa.data.labels.shift(); chartResa.data.datasets[0].data.shift(); }
            chartResa.update();
        }

        if (dto.resa != null && !isNaN(dto.resa)) { winResa.push(+dto.resa); if (winResa.length > WIN) winResa.shift(); }
        if (dto.acquaUtilizzata != null && !isNaN(dto.acquaUtilizzata)) { winAcqua.push(+dto.acquaUtilizzata); if (winAcqua.length > WIN) winAcqua.shift(); }
        if (dto.crescita != null && !isNaN(dto.crescita)) { winCrescita.push(+dto.crescita); if (winCrescita.length > WIN) winCrescita.shift(); }
        renderWue(); renderTrend(computeTrend());

        checkAlerts(dto);
    });
});

function toggleSoglie() {
    const f = document.getElementById('formSoglie'); const b = document.getElementById('btnToggleSoglie');
    if (f.style.display === 'none') {
        const S = window.__THRESHOLDS__; inCaldo.value = S.S_CALDO; inSuolo.value = S.S_SUOLO_SECCO; inPioggia.value = S.S_PIOGGIA; inEff.value = S.S_EFF;
        f.style.display = 'inline-block'; b.textContent = 'Chiudi';
    } else { f.style.display = 'none'; b.textContent = 'Modifica soglie'; }
}
async function salvaSoglie(ev) {
    ev.preventDefault();
    const body = {
        caldo: parseFloat(inCaldo.value),
        suoloSecco: parseFloat(inSuolo.value),
        pioggia: parseFloat(inPioggia.value),
        efficienza: parseFloat(inEff.value)
    };
    const res = await fetch('/api/soglie', { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    if (!res.ok) { alert('Errore aggiornamento soglie'); return false; }
    const js = await res.json();
    window.__THRESHOLDS__.S_CALDO = js.caldo;
    window.__THRESHOLDS__.S_SUOLO_SECCO = js.suoloSecco;
    window.__THRESHOLDS__.S_PIOGGIA = js.pioggia;
    window.__THRESHOLDS__.S_EFF = js.efficienza;
    sCaldo.textContent = js.caldo; sSuolo.textContent = js.suoloSecco; sPioggia.textContent = js.pioggia; sEff.textContent = js.efficienza;
    msgSoglie.style.display = 'block'; setTimeout(() => msgSoglie.style.display = 'none', 2000);
    return false;
}