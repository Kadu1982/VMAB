import { useEffect, useState, useTransition } from 'react'
import type { FormEvent } from 'react'
import './styles.css'
import type { DashboardSummary } from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

const initialAgentForm = {
  fullName: '',
  badgeCode: '',
  cnhCategory: '',
  cnhExpiry: '',
}

const initialVehicleForm = {
  plate: '',
  model: '',
  currentKm: '',
  nextMaintenanceKm: '',
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

function App() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [agentForm, setAgentForm] = useState(initialAgentForm)
  const [vehicleForm, setVehicleForm] = useState(initialVehicleForm)
  const [isPending, startTransition] = useTransition()

  async function loadSummary() {
    setLoading(true)
    setError(null)

    try {
      const response = await fetch(`${API_BASE_URL}/api/dashboard/summary`)
      if (!response.ok) throw new Error('Nao foi possivel carregar o painel.')
      const data = (await response.json()) as DashboardSummary
      setSummary(data)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao carregar o painel.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadSummary()
  }, [])

  async function handleAgentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const response = await fetch(`${API_BASE_URL}/api/agents`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(agentForm),
    })

    if (!response.ok) {
      setError('Nao foi possivel cadastrar o agente.')
      return
    }

    setAgentForm(initialAgentForm)
    startTransition(() => {
      void loadSummary()
    })
  }

  async function handleVehicleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const response = await fetch(`${API_BASE_URL}/api/vehicles`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...vehicleForm,
        currentKm: Number(vehicleForm.currentKm),
        nextMaintenanceKm: Number(vehicleForm.nextMaintenanceKm),
      }),
    })

    if (!response.ok) {
      setError('Nao foi possivel cadastrar a viatura.')
      return
    }

    setVehicleForm(initialVehicleForm)
    startTransition(() => {
      void loadSummary()
    })
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <p className="eyebrow">Plataforma de Seguranca</p>
        <h1>Operacao Comunitaria</h1>
        <p className="sidebar-copy">
          Base inicial para gestao de agentes, viaturas, turnos e ocorrencias.
        </p>

        <div className="sidebar-block">
          <span className="sidebar-label">O que ja existe</span>
          <ul>
            <li>PRD v3 consolidado</li>
            <li>Protótipo visual navegável</li>
            <li>API inicial do dominio operacional</li>
            <li>Painel web conectado ao backend</li>
          </ul>
        </div>

        <div className="sidebar-block">
          <span className="sidebar-label">O que ainda falta</span>
          <ul>
            <li>persistencia em banco</li>
            <li>autenticacao e perfis</li>
            <li>portal do cliente</li>
            <li>mobile do morador e da ronda</li>
          </ul>
        </div>
      </aside>

      <main className="workspace">
        <header className="hero">
          <div>
            <p className="eyebrow">Estado Atual</p>
            <h2>Primeiro corte executavel do produto</h2>
            <p className="hero-copy">
              Este painel ja organiza o core administrativo que vai sustentar os modulos de jornada,
              frota, ocorrencias e supervisao.
            </p>
          </div>
          <button className="refresh-button" onClick={() => void loadSummary()} type="button">
            Atualizar dados
          </button>
        </header>

        {error ? <div className="alert error">{error}</div> : null}
        {loading ? <div className="alert">Carregando painel...</div> : null}
        {isPending ? <div className="alert">Sincronizando cadastros...</div> : null}

        {summary ? (
          <>
            <section className="stats-grid">
              <article className="metric-card">
                <span>Agentes no cadastro</span>
                <strong>{summary.totalAgents}</strong>
              </article>
              <article className="metric-card">
                <span>Agentes ativos</span>
                <strong>{summary.activeAgents}</strong>
              </article>
              <article className="metric-card">
                <span>Viaturas disponiveis</span>
                <strong>{summary.availableVehicles}</strong>
              </article>
              <article className="metric-card">
                <span>Turnos em operacao</span>
                <strong>{summary.activeShifts}</strong>
              </article>
              <article className="metric-card">
                <span>Ocorrencias abertas</span>
                <strong>{summary.openIncidents}</strong>
              </article>
              <article className="metric-card">
                <span>Alertas de manutencao</span>
                <strong>{summary.maintenanceAlerts}</strong>
              </article>
            </section>

            <section className="content-grid">
              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Cadastro</p>
                    <h3>Agentes</h3>
                  </div>
                </div>

                <form className="form-grid" onSubmit={handleAgentSubmit}>
                  <input
                    required
                    placeholder="Nome completo"
                    value={agentForm.fullName}
                    onChange={(event) => setAgentForm((current) => ({ ...current, fullName: event.target.value }))}
                  />
                  <input
                    required
                    placeholder="Codigo / cracha"
                    value={agentForm.badgeCode}
                    onChange={(event) => setAgentForm((current) => ({ ...current, badgeCode: event.target.value }))}
                  />
                  <input
                    required
                    placeholder="Categoria CNH"
                    value={agentForm.cnhCategory}
                    onChange={(event) => setAgentForm((current) => ({ ...current, cnhCategory: event.target.value }))}
                  />
                  <input
                    required
                    type="date"
                    value={agentForm.cnhExpiry}
                    onChange={(event) => setAgentForm((current) => ({ ...current, cnhExpiry: event.target.value }))}
                  />
                  <button type="submit">Cadastrar agente</button>
                </form>

                <div className="list">
                  {summary.agents.map((agent) => (
                    <article className="list-row" key={agent.id}>
                      <div>
                        <strong>{agent.fullName}</strong>
                        <small>
                          Cracha {agent.badgeCode} • CNH {agent.cnhCategory} ate {agent.cnhExpiry}
                        </small>
                      </div>
                      <span className={`tag ${agent.status.toLowerCase()}`}>{agent.status}</span>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Frota</p>
                    <h3>Viaturas</h3>
                  </div>
                </div>

                <form className="form-grid" onSubmit={handleVehicleSubmit}>
                  <input
                    required
                    placeholder="Placa"
                    value={vehicleForm.plate}
                    onChange={(event) => setVehicleForm((current) => ({ ...current, plate: event.target.value }))}
                  />
                  <input
                    required
                    placeholder="Modelo"
                    value={vehicleForm.model}
                    onChange={(event) => setVehicleForm((current) => ({ ...current, model: event.target.value }))}
                  />
                  <input
                    required
                    min="0"
                    type="number"
                    placeholder="KM atual"
                    value={vehicleForm.currentKm}
                    onChange={(event) => setVehicleForm((current) => ({ ...current, currentKm: event.target.value }))}
                  />
                  <input
                    required
                    min="1"
                    type="number"
                    placeholder="Proxima manutencao"
                    value={vehicleForm.nextMaintenanceKm}
                    onChange={(event) => setVehicleForm((current) => ({ ...current, nextMaintenanceKm: event.target.value }))}
                  />
                  <button type="submit">Cadastrar viatura</button>
                </form>

                <div className="list">
                  {summary.vehicles.map((vehicle) => (
                    <article className="list-row" key={vehicle.id}>
                      <div>
                        <strong>{vehicle.model}</strong>
                        <small>
                          {vehicle.plate} • {vehicle.currentKm.toLocaleString('pt-BR')} km • revisao em{' '}
                          {vehicle.nextMaintenanceKm.toLocaleString('pt-BR')} km
                        </small>
                      </div>
                      <span className={`tag ${vehicle.status.toLowerCase()}`}>{vehicle.status}</span>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Turnos</p>
                    <h3>Jornada e responsabilidade</h3>
                  </div>
                </div>

                <div className="list">
                  {summary.shifts.map((shift) => (
                    <article className="list-row" key={shift.id}>
                      <div>
                        <strong>{shift.agentName}</strong>
                        <small>
                          {shift.vehiclePlate} • inicio {formatDate(shift.startedAt)} • fim previsto{' '}
                          {formatDate(shift.scheduledEndAt)}
                        </small>
                      </div>
                      <span className={`tag ${shift.status.toLowerCase()}`}>{shift.status}</span>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Ocorrencias</p>
                    <h3>Fila operacional</h3>
                  </div>
                </div>

                <div className="list">
                  {summary.incidents.map((incident) => (
                    <article className="list-row" key={incident.id}>
                      <div>
                        <strong>
                          {incident.type} • {incident.residentName}
                        </strong>
                        <small>
                          {incident.address} • {incident.assignedAgentName ?? 'Sem agente'} •{' '}
                          {incident.vehiclePlate ?? 'Sem viatura'}
                        </small>
                      </div>
                      <span className={`tag ${incident.priority.toLowerCase()}`}>{incident.priority}</span>
                    </article>
                  ))}
                </div>
              </section>
            </section>
          </>
        ) : null}
      </main>
    </div>
  )
}

export default App
