const $ = (s) => document.querySelector(s);
const $$ = (s) => [...document.querySelectorAll(s)];
const screens = $$(".screen");
const navItems = $$(".nav-item");
const scenarioBtns = $$(".scenario-btn");
const routeLayer = $("#route-layer");
const markerLayer = $("#marker-layer");
const poiLayer = $("#poi-layer");
const ui = {
  screenTitle: $("#screen-title"),
  play: $("#play-pause"),
  speed: $("#speed-btn"),
  heroTitle: $("#hero-title"),
  heroCopy: $("#hero-copy"),
  metricEta: $("#metric-eta"),
  metricAvailability: $("#metric-availability"),
  metricResidents: $("#metric-residents"),
  residentHomeAddress: $("#resident-home-address"),
  residentHomePatrol: $("#resident-home-patrol"),
  residentHomeEta: $("#resident-home-eta"),
  residentHomePhase: $("#resident-home-phase"),
  residentHomeSummary: $("#resident-home-summary"),
  escortTitle: $("#escort-title"),
  escortBadge: $("#escort-badge"),
  escortSummary: $("#escort-summary"),
  escortEta: $("#escort-eta"),
  escortNote: $("#escort-note"),
  trackingTitle: $("#tracking-title"),
  trackingBadge: $("#tracking-badge"),
  trackingPatrol: $("#tracking-patrol-label"),
  trackingEta: $("#tracking-eta"),
  trackingLast: $("#tracking-last-update"),
  phaseTitle: $("#phase-title"),
  phaseCopy: $("#phase-copy"),
  telemetryPhase: $("#telemetry-phase"),
  telemetryStage: $("#telemetry-stage"),
  incidentList: $("#incident-list"),
  unitCards: $("#unit-cards"),
  residentCards: $("#resident-cards"),
  queueBadge: $("#queue-badge"),
  queueSummary: $("#queue-summary"),
  opsTitle: $("#ops-title"),
  opsBadge: $("#ops-badge"),
  opsDest: $("#ops-destination"),
  opsNote: $("#ops-destination-note"),
  tutorTitle: $("#tutor-title"),
  dashOccurrences: $("#dash-occurrences"),
  dashSla: $("#dash-sla"),
  dashEscort: $("#dash-escort"),
  dashCancel: $("#dash-cancel"),
  dashboardSummary: $("#dashboard-summary"),
  geofenceNote: $("#geofence-note"),
  geofenceSummary: $("#geofence-summary"),
  reportSummary: $("#report-summary"),
  historySummary: $("#history-summary"),
  historyNote: $("#history-note"),
  shiftBadge: $("#shift-badge"),
  shiftAvatar: $("#shift-avatar"),
  shiftAgentName: $("#shift-agent-name"),
  shiftAgentMeta: $("#shift-agent-meta"),
  shiftAgentSummary: $("#shift-agent-summary"),
  shiftClockSummary: $("#shift-clock-summary"),
  shiftVehicleName: $("#shift-vehicle-name"),
  shiftVehicleNote: $("#shift-vehicle-note"),
  shiftMaintenanceSummary: $("#shift-maintenance-summary"),
  teamActive: $("#team-active"),
  teamHandoffs: $("#team-handoffs"),
  fleetActive: $("#fleet-active"),
  fleetMaint: $("#fleet-maint"),
  teamAvatar: $("#team-avatar"),
  teamLeadName: $("#team-lead-name"),
  teamLeadMeta: $("#team-lead-meta"),
  teamSummary: $("#team-summary"),
  fleetSummary: $("#fleet-summary"),
  fleetStatusName: $("#fleet-status-name"),
  fleetStatusNote: $("#fleet-status-note"),
};

const pois = [
  { id: "base", type: "base", label: "Base Alpha", sub: "Centro operacional", p: [110, 100] },
  { id: "ana-home", type: "home", label: "Casa Ana", sub: "Rua das Acácias, 85", p: [565, 210] },
  { id: "bruno-home", type: "home", label: "Casa Bruno", sub: "Alameda Ipê, 210", p: [520, 360] },
  { id: "carla-home", type: "home", label: "Casa Carla", sub: "Rua das Flores, 72", p: [260, 420] },
];

const workforceBase = {
  shiftBadge: "Turno ativo",
  shiftAvatar: "CN",
  shiftAgentName: "Carlos Nunes",
  shiftAgentMeta: "Ronda noturna • CNH AB válida até 08/2027",
  shiftAgentSummary: [
    "Foto de identificação validada",
    "Ponto registrado às 18:00",
    "Substituição prevista às 22:00"
  ],
  shiftClockSummary: [
    "Check-in por operador e dispositivo",
    "Troca de funcionário com aceite duplo",
    "Histórico auditável de jornada"
  ],
  shiftVehicleName: "Renault Duster • ABC1D23",
  shiftVehicleNote: "Quilometragem inicial 48.215 km • atual 48.241 km",
  shiftMaintenanceSummary: [
    "Próxima revisão em 785 km",
    "Checklist diário sem pendências críticas",
    "Pneus e óleo dentro da janela operacional"
  ],
  teamStats: ["6", "2", "2", "1"],
  teamAvatar: "ML",
  teamLeadName: "Marina Luz",
  teamLeadMeta: "Foto cadastrada • CNH AB válida até 03/2028",
  teamSummary: [
    "Substitui Carlos Nunes às 22:00",
    "Treinamento tático e rota noturna em dia",
    "Assinatura digital de recebimento pendente"
  ],
  fleetSummary: [
    "Alpha-01 • Renault Duster • Placa ABC1D23",
    "Quilometragem atual 48.241 km",
    "Próxima manutenção preventiva em 785 km"
  ],
  fleetStatusName: "Checklist diário concluído",
  fleetStatusNote: "Sem bloqueio de uso, com alerta amarelo para revisão preventiva."
};

const scenarios = {
  alerta: {
    hero: ["Pânico em residência com viatura em deslocamento", "Ana está em casa. Alpha-01 sai da base e a Beta-02 mantém cobertura do bairro."],
    phase: ["Despacho da Alpha-01", "Viatura principal em movimento até a residência de Ana com rastreio simultâneo das equipes e moradores."],
    metrics: ["03 min", "1 livre", "3"],
    target: { resident: "Ana", address: "Rua das Acácias, 85", status: "Em deslocamento", label: "Alpha-01 em rota" },
    hist: {
      lines: ["Abertura: 22:14", "Chegada da ronda: 22:19", "SLA total: 5m18s", "Escalonamento PM: não necessário"],
      note: "Portão verificado, rua liberada e moradora protegida dentro da residência."
    },
    dash: ["18", "06m12s", "7", "4"],
    dashLines: ["Rua das Acácias concentra mais eventos nesta semana", "Pico operacional entre 19h e 22h", "Sem envio automático para o 190"],
    queueLines: ["Pânico recebe maior peso na fila", "Distância Alpha-01 -> residência", "Beta-02 preservando cobertura"],
    reportLines: ["PDF executivo mensal", "Mapa de calor por rua e horário", "Tempo médio por tipo de ocorrência"],
    geo: "Geofence com tolerância de GPS ativa e tratamento de exceções fora da área.",
    incidents: [
      ["critical", "Pânico - Rua das Acácias, 85", "Ana em casa • ETA 03 min"],
      ["medium", "Escolta - Alameda Ipê, 210", "Monitorada para o próximo despacho • ETA 06 min"],
      ["low", "Patrulha preventiva - Rua das Flores", "Beta-02 mantendo cobertura"]
    ],
    units: [
      { id: "alpha", label: "Alpha-01", type: "patrol", speed: .16, status: "Despachada", path: [[110,100],[120,120],[120,210],[260,210],[420,210],[565,210]] },
      { id: "beta", label: "Beta-02", type: "patrol", speed: .06, status: "Patrulha", loop: true, path: [[260,320],[420,320],[580,320],[580,420],[420,420],[260,420],[260,320]] }
    ],
    residents: [
      { id: "ana", label: "Ana", type: "risk", speed: 0, status: "Em casa", path: [[565,210],[565,210]] },
      { id: "bruno", label: "Bruno", type: "resident", speed: 0, status: "Em casa", path: [[520,360],[520,360]] },
      { id: "carla", label: "Carla", type: "resident", speed: 0, status: "Em casa", path: [[260,420],[260,420]] }
    ]
  },
  escolta: {
    hero: ["Escolta de retorno com interceptação antes da residência", "Bruno compartilha a rota temporariamente e a Alpha-01 faz a interceptação antes da chegada em casa."],
    phase: ["Interceptação para escolta", "O morador se aproxima do bairro enquanto a Alpha-01 ajusta a rota para encontrá-lo a um quarteirão do destino."],
    metrics: ["04 min", "1 em apoio", "3"],
    target: { resident: "Bruno", address: "Alameda Ipê, 210", status: "Escolta ativa", label: "Alpha-01 em interceptação" },
    hist: {
      lines: ["Abertura: 21:37", "Interceptação: 21:41", "Chegada ao destino: 21:44", "Fechamento seguro: portão concluído"],
      note: "Rota compartilhada somente durante a escolta, encerrada após a entrada do morador."
    },
    dash: ["16", "05m48s", "8", "3"],
    dashLines: ["Alameda Ipê com maior volume de escoltas noturnas", "Interceptação abaixo de 4 minutos", "Rota temporária e auditável"],
    queueLines: ["ETA do encontro com o morador", "Distância até a residência final", "Cobertura secundária mantida pela Beta-02"],
    reportLines: ["Escoltas por faixa horária", "Rotas de retorno mais usadas", "Tempo entre interceptação e encerramento"],
    geo: "Geofence ativa com entrada assistida no bairro e retenção curta da rota do morador.",
    incidents: [
      ["medium", "Escolta - Alameda Ipê, 210", "Bruno em aproximação • interceptação em 04 min"],
      ["low", "Patrulha - Rua das Acácias", "Beta-02 mantendo cobertura fixa"],
      ["low", "Moradora Ana em casa", "Sem eventos ativos"]
    ],
    units: [
      { id: "alpha", label: "Alpha-01", type: "patrol", speed: .18, status: "Escolta", path: [[120,210],[260,210],[340,280],[420,320],[470,345],[520,360]] },
      { id: "beta", label: "Beta-02", type: "patrol", speed: .05, status: "Cobertura", loop: true, path: [[260,420],[420,420],[580,420],[580,320],[420,320],[260,320],[260,420]] }
    ],
    residents: [
      { id: "ana", label: "Ana", type: "resident", speed: 0, status: "Em casa", path: [[565,210],[565,210]] },
      { id: "bruno", label: "Bruno", type: "resident", speed: .16, status: "Indo para casa", path: [[40,320],[120,320],[260,320],[340,320],[420,330],[470,345],[520,360]] },
      { id: "carla", label: "Carla", type: "resident", speed: 0, status: "Em casa", path: [[260,420],[260,420]] }
    ]
  },
  retorno: {
    hero: ["Morador indo para casa com apoio preventivo", "Carla cruza a geofence do bairro e a Alpha-01 acompanha o retorno até o fechamento do portão."],
    phase: ["Acompanhamento de retorno", "A moradora entra no perímetro, a Alpha-01 encurta o ETA e o painel mostra os dois deslocamentos até a chegada."],
    metrics: ["05 min", "2 em operação", "3"],
    target: { resident: "Carla", address: "Rua das Flores, 72", status: "Acompanhamento ativo", label: "Alpha-01 aproximando" },
    hist: {
      lines: ["Abertura: 20:52", "Entrada na geofence: 20:54", "Chegada da viatura: 20:57", "Portão fechado: 20:59"],
      note: "Telemetria do retorno preservada apenas para SLA e auditoria do atendimento."
    },
    dash: ["15", "05m31s", "9", "2"],
    dashLines: ["Rua das Flores com aumento de retornos monitorados", "Alpha-01 e Beta-02 operando sem conflito", "SLA abaixo da meta interna"],
    queueLines: ["Entrada do morador no perímetro protegido", "ETA da viatura até o ponto de apoio", "Disponibilidade simultânea de duas equipes"],
    reportLines: ["Acompanhamentos por entrada no bairro", "Tempo entre geofence e chegada da viatura", "Taxa de encerramento seguro"],
    geo: "Painel exibe cruzamento da geofence e mudança automática para acompanhamento monitorado.",
    incidents: [
      ["medium", "Retorno monitorado - Rua das Flores, 72", "Carla em deslocamento • ETA 05 min"],
      ["low", "Base operacional - Alpha-01", "Saída confirmada há 30s"],
      ["low", "Beta-02 - cobertura norte", "Patrulha no eixo principal"]
    ],
    units: [
      { id: "alpha", label: "Alpha-01", type: "patrol", speed: .14, status: "Acompanhamento", path: [[110,100],[120,210],[120,320],[180,380],[220,405],[260,420]] },
      { id: "beta", label: "Beta-02", type: "patrol", speed: .07, status: "Patrulha", loop: true, path: [[580,110],[580,210],[580,320],[420,320],[420,210],[580,210],[580,110]] }
    ],
    residents: [
      { id: "ana", label: "Ana", type: "resident", speed: 0, status: "Em casa", path: [[565,210],[565,210]] },
      { id: "bruno", label: "Bruno", type: "resident", speed: 0, status: "Em casa", path: [[520,360],[520,360]] },
      { id: "carla", label: "Carla", type: "resident", speed: .12, status: "Indo para casa", path: [[40,420],[120,420],[180,420],[220,420],[260,420]] }
    ]
  }
};

const state = { scenario: "alerta", playing: true, speed: 1, startedAt: performance.now(), lastNow: performance.now(), pauseAt: 0 };
const labels = {
  "morador-home": "Home do Morador",
  "morador-alerta": "Alerta de Pânico",
  "morador-escolta": "Solicitar Escolta",
  "morador-tracking": "Acompanhar Viatura",
  "morador-historico": "Ocorrência Resolvida",
  "ronda-fila": "Fila Operacional da Ronda",
  "ronda-ocorrencia": "Atendimento da Ronda",
  "ronda-turno": "Turno e Ponto da Ronda",
  "tutor-dashboard": "Dashboard do Tutor",
  "tutor-geofence": "Geofence e Cadastros",
  "tutor-equipe": "Equipe e Frota",
  "tutor-relatorios": "Relatórios Operacionais"
};

function setText(id, value) { ui[id].textContent = value; }
function setList(id, items) { ui[id].innerHTML = items.map((i) => `<li>${i}</li>`).join(""); }
function d(a, b) { return Math.hypot(b[0] - a[0], b[1] - a[1]); }
function svg(tag, attrs) { const el = document.createElementNS("http://www.w3.org/2000/svg", tag); Object.entries(attrs).forEach(([k, v]) => el.setAttribute(k, v)); return el; }

function pointOnPath(path, progress) {
  if (path.length < 2) return { point: path[0], segment: 0 };
  const segs = []; let total = 0;
  for (let i = 0; i < path.length - 1; i += 1) { const len = d(path[i], path[i + 1]); segs.push(len); total += len; }
  const target = progress * total; let acc = 0;
  for (let i = 0; i < segs.length; i += 1) {
    const next = acc + segs[i];
    if (target <= next || i === segs.length - 1) {
      const local = segs[i] ? (target - acc) / segs[i] : 0;
      return { point: [path[i][0] + (path[i + 1][0] - path[i][0]) * local, path[i][1] + (path[i + 1][1] - path[i][1]) * local], segment: i };
    }
    acc = next;
  }
  return { point: path[path.length - 1], segment: path.length - 2 };
}

function progressOf(entity, elapsed) {
  if (!entity.speed || entity.path.length < 2) return entity.loop ? 0 : 1;
  const value = elapsed * entity.speed;
  return entity.loop ? value % 1 : Math.min(value, 1);
}

function renderPOIs() {
  poiLayer.innerHTML = "";
  pois.forEach((poi) => {
    const [x, y] = poi.p;
    const size = poi.type === "base" ? 18 : 14;
    poiLayer.append(
      svg("rect", { x: x - size / 2, y: y - size / 2, width: size, height: size, rx: poi.type === "base" ? 5 : 3, class: `poi-shape ${poi.type}` }),
      Object.assign(svg("text", { x: x + 14, y: y - 4, class: "poi-label" }), { textContent: poi.label }),
      Object.assign(svg("text", { x: x + 14, y: y + 12, class: "poi-sub" }), { textContent: poi.sub }),
    );
  });
}

function renderRoutes(cfg) {
  routeLayer.innerHTML = "";
  [...cfg.units, ...cfg.residents].forEach((e) => {
    if (e.path.length < 2) return;
    routeLayer.append(svg("polyline", {
      points: e.path.map((p) => p.join(",")).join(" "),
      class: `route-line ${e.type === "patrol" ? "patrol" : "resident"}`
    }));
  });
}

function marker(entity, p) {
  const g = svg("g", { class: `marker ${entity.type}`, transform: `translate(${p[0]},${p[1]})` });
  if (entity.type === "risk") g.append(svg("circle", { cx: 0, cy: 0, r: 22, class: "pulse" }));
  g.append(svg("circle", { cx: 0, cy: 0, r: 14 }));
  const t = svg("text", { x: 0, y: 4 }); t.textContent = entity.label.slice(0, 2).toUpperCase(); g.append(t);
  return g;
}

function entityRow(entity, p) {
  const row = document.createElement("div");
  row.className = "entity-row";
  row.innerHTML = `<div><strong>${entity.label}</strong><small>${entity.status}</small></div><span>${Math.round(p[0])}, ${Math.round(p[1])}</span>`;
  return row;
}

function timelineState(p) {
  if (p < .12) return ["done", "", "", ""];
  if (p < .55) return ["done", "done", "current", ""];
  if (p < .9) return ["done", "done", "done", "current"];
  return ["done", "done", "done", "done"];
}

function applyTimeline(ids, classes) {
  ids.forEach((id, i) => {
    const el = $("#" + id);
    el.className = `timeline-item${classes[i] ? ` ${classes[i]}` : ""}`;
  });
}

function bindScenario(cfg, positions) {
  const target = cfg.target;
  const alpha = positions.alpha;
  const beta = positions.beta;
  const resident = positions[target.resident.toLowerCase()];
  const [eta, availability, residentsCount] = cfg.metrics;
  const workforce = workforceBase;
  const [teamActive, teamHandoffs, fleetActive, fleetMaint] = workforce.teamStats;

  setText("heroTitle", cfg.hero[0]); setText("heroCopy", cfg.hero[1]);
  setText("metricEta", eta); setText("metricAvailability", availability); setText("metricResidents", residentsCount);
  setText("residentHomeAddress", target.address); setText("residentHomePatrol", "Viatura Alpha-01"); setText("residentHomeEta", `A ${eta} do local`);
  setText("residentHomePhase", `${target.resident} • ${target.status}`);
  setList("residentHomeSummary", [`Morador principal: ${target.resident}`, `Alpha-01 em ${target.status.toLowerCase()}`, "Beta-02 preservando cobertura"]);

  setText("escortTitle", cfg.target.status === "Em deslocamento" ? "Solicitação manual" : "Escolta monitorada");
  setText("escortBadge", target.status); setText("escortEta", `Viatura Alpha-01 em ${eta}`); setText("escortNote", cfg.hero[1]);
  setList("escortSummary", [`Endereço destino: ${target.address}`, `Morador monitorado: ${target.resident}`, "Rota compartilhada: temporária e auditável"]);

  setText("trackingTitle", cfg.target.status === "Em deslocamento" ? "Acompanhar viatura" : "Acompanhar deslocamento");
  setText("trackingBadge", target.status); setText("trackingPatrol", target.label); setText("trackingEta", `ETA ${eta}`);
  setText("trackingLast", `Alpha-01 em (${Math.round(alpha[0])}, ${Math.round(alpha[1])}) • ${target.resident} em (${Math.round(resident[0])}, ${Math.round(resident[1])})`);

  setText("phaseTitle", cfg.phase[0]); setText("phaseCopy", cfg.phase[1]);
  setText("opsTitle", cfg.hero[0]); setText("opsBadge", target.status); setText("opsDest", target.address);
  setText("opsNote", `${target.resident} monitorado • Beta-02 em ${Math.round(beta[0])}, ${Math.round(beta[1])}`);

  setText("tutorTitle", cfg.phase[0]); setText("dashOccurrences", cfg.dash[0]); setText("dashSla", cfg.dash[1]); setText("dashEscort", cfg.dash[2]); setText("dashCancel", cfg.dash[3]);
  setList("dashboardSummary", cfg.dashLines); setText("geofenceNote", cfg.geo);
  setList("geofenceSummary", ["83 moradores ativos", "2 viaturas cadastradas com telemetria", "MFA obrigatório para perfis administrativos"]);
  setList("reportSummary", cfg.reportLines); setList("historySummary", cfg.hist.lines); setText("historyNote", cfg.hist.note); setList("queueSummary", cfg.queueLines);

  setList("geofenceSummary", ["83 moradores ativos", "2 viaturas cadastradas com telemetria", "Cadastros com foto, CNH e MFA administrativo"]);
  setText("shiftBadge", workforce.shiftBadge); setText("shiftAvatar", workforce.shiftAvatar); setText("shiftAgentName", workforce.shiftAgentName); setText("shiftAgentMeta", workforce.shiftAgentMeta);
  setList("shiftAgentSummary", workforce.shiftAgentSummary); setList("shiftClockSummary", workforce.shiftClockSummary); setText("shiftVehicleName", workforce.shiftVehicleName); setText("shiftVehicleNote", workforce.shiftVehicleNote);
  setList("shiftMaintenanceSummary", workforce.shiftMaintenanceSummary); setText("teamActive", teamActive); setText("teamHandoffs", teamHandoffs); setText("fleetActive", fleetActive); setText("fleetMaint", fleetMaint);
  setText("teamAvatar", workforce.teamAvatar); setText("teamLeadName", workforce.teamLeadName); setText("teamLeadMeta", workforce.teamLeadMeta); setList("teamSummary", workforce.teamSummary);
  setList("fleetSummary", workforce.fleetSummary); setText("fleetStatusName", workforce.fleetStatusName); setText("fleetStatusNote", workforce.fleetStatusNote);

  ui.incidentList.innerHTML = "";
  cfg.incidents.forEach(([severity, title, meta]) => {
    const el = document.createElement("article");
    el.className = `incident ${severity}`;
    el.innerHTML = `<strong>${title}</strong><span>${meta}</span>`;
    if (severity !== "low") el.addEventListener("click", () => activateScreen("ronda-ocorrencia"));
    ui.incidentList.append(el);
  });
  ui.queueBadge.textContent = cfg.incidents.some((i) => i[0] === "critical") ? "1 crítico" : "Fila assistida";
}

function renderEntities(cfg, elapsed) {
  markerLayer.innerHTML = "";
  ui.unitCards.innerHTML = "";
  ui.residentCards.innerHTML = "";
  const positions = {};
  [...cfg.units, ...cfg.residents].forEach((e) => {
    const p = pointOnPath(e.path, progressOf(e, elapsed)).point;
    positions[e.id] = p;
    markerLayer.append(marker(e, p));
    (e.type === "patrol" ? ui.unitCards : ui.residentCards).append(entityRow(e, p));
  });
  return positions;
}

function activateScreen(id) {
  screens.forEach((s) => s.classList.toggle("active", s.id === id));
  navItems.forEach((n) => n.classList.toggle("active", n.dataset.screen === id));
  ui.screenTitle.textContent = labels[id] || "Protótipo";
}

function setScenario(key) {
  state.scenario = key;
  state.startedAt = performance.now();
  state.lastNow = performance.now();
  scenarioBtns.forEach((b) => b.classList.toggle("active", b.dataset.scenario === key));
  renderRoutes(scenarios[key]);
  render();
}

function render() {
  const cfg = scenarios[state.scenario];
  const now = state.lastNow;
  const elapsed = (((state.playing ? now : state.pauseAt) - state.startedAt) / 1000) * state.speed;
  const positions = renderEntities(cfg, elapsed);
  bindScenario(cfg, positions);
  const p = progressOf(cfg.units[0], elapsed);
  const cls = timelineState(p);
  applyTimeline(["timeline-open", "timeline-accept", "timeline-route", "timeline-close"], cls);
  applyTimeline(["ops-step-1", "ops-step-2", "ops-step-3", "ops-step-4"], cls);
  const phases = [
    [.12, "Despacho inicial", "Chamada registrada e validação de geofence"],
    [.55, "Deslocamento ativo", "Viatura e morador monitorados em tempo real"],
    [.9, "Aproximação segura", "ETA crítico e fechamento do atendimento"],
    [2, "Encerramento", "Operação concluída com trilha auditável"]
  ];
  const phase = phases.find((f) => p < f[0]) || phases[phases.length - 1];
  setText("telemetryPhase", phase[1]); setText("telemetryStage", phase[2]);
}

function tick(now) {
  if (state.playing) state.lastNow = now;
  render();
  requestAnimationFrame(tick);
}

navItems.forEach((item) => item.addEventListener("click", () => activateScreen(item.dataset.screen)));
$$("[data-screen-target]").forEach((el) => el.addEventListener("click", () => activateScreen(el.dataset.screenTarget)));
scenarioBtns.forEach((btn) => btn.addEventListener("click", () => setScenario(btn.dataset.scenario)));

ui.play.addEventListener("click", () => {
  state.playing = !state.playing;
  if (state.playing) {
    state.startedAt += performance.now() - state.pauseAt;
    state.lastNow = performance.now();
    ui.play.textContent = "Pausar";
  } else {
    state.pauseAt = performance.now();
    ui.play.textContent = "Retomar";
  }
});

ui.speed.addEventListener("click", () => {
  const next = state.speed === 1 ? 1.75 : state.speed === 1.75 ? 2.5 : 1;
  const now = performance.now();
  const elapsed = ((now - state.startedAt) / 1000) * state.speed;
  state.speed = next;
  state.startedAt = now - (elapsed / state.speed) * 1000;
  ui.speed.textContent = `Velocidade ${state.speed}x`;
});

renderPOIs();
setScenario("alerta");
requestAnimationFrame(tick);
