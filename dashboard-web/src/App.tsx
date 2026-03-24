import { useEffect, useRef, useState, useTransition } from 'react'
import type { FormEvent } from 'react'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import './styles.css'
import logoVmab from './assets/vmab-logo.png'
import type {
  Agent,
  AgentStatus,
  AppUser,
  AppUserRole,
  AuthSession,
  ClientPortal,
  DashboardSummary,
  Incident,
  IncidentPriority,
  IncidentStatus,
  IncidentType,
  Resident,
  ResidentStatus,
  Shift,
  ShiftAttendanceStatus,
  ShiftStatus,
  Vehicle,
  VehicleMaintenanceRecord,
  VehicleMaintenanceType,
  VehicleStatus,
} from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''
const STORAGE_KEY = 'seguranca-auth'

const agentStatusOptions: AgentStatus[] = ['ACTIVE', 'ON_DUTY', 'OFF_DUTY', 'BLOCKED']
const residentStatusOptions: ResidentStatus[] = ['ACTIVE', 'INACTIVE']
const vehicleStatusOptions: VehicleStatus[] = ['AVAILABLE', 'IN_OPERATION', 'MAINTENANCE', 'BLOCKED']
const vehicleMaintenanceTypeOptions: VehicleMaintenanceType[] = ['PREVENTIVE', 'CORRECTIVE', 'INSPECTION', 'DOCUMENTATION']
const shiftStatusOptions: ShiftStatus[] = ['PLANNED', 'ACTIVE', 'HANDOFF', 'CLOSED']
const shiftAttendanceOptions: ShiftAttendanceStatus[] = ['PENDING', 'ON_TIME', 'LATE', 'ABSENT', 'COVERED']
const incidentTypeOptions: IncidentType[] = ['PANIC', 'SUSPICIOUS_ACTIVITY', 'MEDICAL', 'ESCORT']
const incidentPriorityOptions: IncidentPriority[] = ['HIGH', 'MEDIUM', 'LOW']
const incidentStatusOptions: IncidentStatus[] = ['OPEN', 'DISPATCHED', 'ON_SITE', 'CLOSED']
const appUserRoleOptions: AppUserRole[] = ['ADMIN', 'SUPERVISOR', 'CLIENT', 'RONDA']

const initialCredentials = { username: 'admin', password: 'admin123' }
const initialSession: AuthSession | null = null
const initialUserForm = {
  username: '',
  password: '',
  role: 'SUPERVISOR' as AppUserRole,
  enabled: true,
}

const initialAgentForm = {
  fullName: '',
  badgeCode: '',
  cnhCategory: '',
  cnhExpiry: '',
  photoUrl: '',
  medicalExamExpiry: '',
  workExamsExpiry: '',
  documentNotes: '',
  status: 'ACTIVE' as AgentStatus,
}

const initialVehicleForm = {
  plate: '',
  model: '',
  currentKm: '',
  nextMaintenanceKm: '',
  ipvaExpiry: '',
  licensingExpiry: '',
  insuranceExpiry: '',
  lastMaintenanceAt: '',
  maintenanceNotes: '',
  status: 'AVAILABLE' as VehicleStatus,
}

const initialMaintenanceForm = {
  vehicleId: '',
  type: 'PREVENTIVE' as VehicleMaintenanceType,
  serviceDate: '',
  kmAtService: '',
  nextMaintenanceKm: '',
  costAmount: '',
  supplierName: '',
  description: '',
  resolved: false,
}

const initialResidentForm = {
  fullName: '',
  phoneNumber: '',
  address: '',
  referenceNote: '',
  status: 'ACTIVE' as ResidentStatus,
}

const initialShiftForm = {
  agentId: '',
  vehicleId: '',
  scheduledStartAt: '',
  scheduledEndAt: '',
  status: 'PLANNED' as ShiftStatus,
  attendanceStatus: 'PENDING' as ShiftAttendanceStatus,
  coverageForAgentId: '',
  attendanceNotes: '',
  endKm: '',
  handoffToAgentId: '',
  handoffNotes: '',
  fuelLevelPercent: '',
  tiresChecked: false,
  lightsChecked: false,
  documentsChecked: false,
  checklistNotes: '',
}

const initialIncidentForm = {
  type: 'PANIC' as IncidentType,
  priority: 'HIGH' as IncidentPriority,
  status: 'OPEN' as IncidentStatus,
  residentId: '',
  residentName: '',
  address: '',
  assignedAgentId: '',
  vehicleId: '',
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

function formatDateTimeLocal(value: string) {
  const date = new Date(value)
  const offset = date.getTimezoneOffset()
  const local = new Date(date.getTime() - offset * 60000)
  return local.toISOString().slice(0, 16)
}

function hasAnyRole(roles: string[], allowed: string[]) {
  return roles.some((role) => allowed.includes(role))
}

function translateAgentStatus(status: AgentStatus) {
  return {
    ACTIVE: 'Ativo',
    ON_DUTY: 'Em servico',
    OFF_DUTY: 'Fora de servico',
    BLOCKED: 'Bloqueado',
  }[status]
}

function translateResidentStatus(status: ResidentStatus) {
  return {
    ACTIVE: 'Ativo',
    INACTIVE: 'Inativo',
  }[status]
}

function translateVehicleStatus(status: VehicleStatus) {
  return {
    AVAILABLE: 'Disponivel',
    IN_OPERATION: 'Em operacao',
    MAINTENANCE: 'Em manutencao',
    BLOCKED: 'Bloqueada',
  }[status]
}

function translateShiftStatus(status: ShiftStatus) {
  return {
    PLANNED: 'Planejado',
    ACTIVE: 'Ativo',
    HANDOFF: 'Troca de turno',
    CLOSED: 'Encerrado',
  }[status]
}

function translateShiftAttendanceStatus(status: ShiftAttendanceStatus) {
  return {
    PENDING: 'Pendente',
    ON_TIME: 'No horario',
    LATE: 'Atrasado',
    ABSENT: 'Falta',
    COVERED: 'Cobertura',
  }[status]
}

function translateVehicleMaintenanceType(type: VehicleMaintenanceType) {
  return {
    PREVENTIVE: 'Preventiva',
    CORRECTIVE: 'Corretiva',
    INSPECTION: 'Inspecao',
    DOCUMENTATION: 'Documentacao',
  }[type]
}

function translateIncidentType(type: IncidentType) {
  return {
    PANIC: 'Panico',
    SUSPICIOUS_ACTIVITY: 'Atitude suspeita',
    MEDICAL: 'Emergencia medica',
    ESCORT: 'Escolta',
  }[type]
}

function translateIncidentPriority(priority: IncidentPriority) {
  return {
    HIGH: 'Alta',
    MEDIUM: 'Media',
    LOW: 'Baixa',
  }[priority]
}

function translateIncidentStatus(status: IncidentStatus) {
  return {
    OPEN: 'Aberta',
    DISPATCHED: 'Despachada',
    ON_SITE: 'No local',
    CLOSED: 'Encerrada',
  }[status]
}

function translateRole(role: AppUserRole | string) {
  return {
    ADMIN: 'Administrador',
    SUPERVISOR: 'Supervisor',
    CLIENT: 'Cliente',
    RONDA: 'Ronda',
    ROLE_ADMIN: 'Administrador',
    ROLE_SUPERVISOR: 'Supervisor',
    ROLE_CLIENT: 'Cliente',
    ROLE_RONDA: 'Ronda',
  }[role] ?? role
}

function translateGenericOperationalText(value: string) {
  const lookup: Record<string, string> = {
    OPEN: 'Aberta',
    DISPATCHED: 'Despachada',
    ON_SITE: 'No local',
    CLOSED: 'Encerrada',
    PLANNED: 'Planejado',
    ACTIVE: 'Ativo',
    HANDOFF: 'Troca de turno',
    AVAILABLE: 'Disponivel',
    IN_OPERATION: 'Em operacao',
    MAINTENANCE: 'Em manutencao',
    BLOCKED: 'Bloqueado',
  }

  return lookup[value] ?? value
}

function readStoredSession(): AuthSession | null {
  const saved = window.localStorage.getItem(STORAGE_KEY)
  if (!saved) return null

  try {
    const parsed = JSON.parse(saved) as Partial<AuthSession>
    if (typeof parsed.accessToken !== 'string' || typeof parsed.username !== 'string' || !Array.isArray(parsed.roles)) {
      window.localStorage.removeItem(STORAGE_KEY)
      return null
    }

    return {
      accessToken: parsed.accessToken,
      tokenType: typeof parsed.tokenType === 'string' ? parsed.tokenType : 'Bearer',
      expiresAt: typeof parsed.expiresAt === 'string' ? parsed.expiresAt : '',
      username: parsed.username,
      roles: parsed.roles,
    }
  } catch {
    window.localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

function buildGuardMarkerIcon(photoUrl: string | null | undefined, name: string) {
  // Gera um marcador HTML do Leaflet com foto real do vigilante ou fallback por iniciais.
  const initials = name
    .split(' ')
    .map((part) => part[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

  const visual = photoUrl
    ? `<img src="${photoUrl}" alt="${name}" class="guard-marker-image" />`
    : `<div class="guard-marker-fallback">${initials}</div>`

  return L.divIcon({
    className: 'guard-marker-shell',
    html: `
      <div class="guard-marker-pin">
        <div class="guard-marker-pulse"></div>
        <div class="guard-marker-frame">
          ${visual}
        </div>
      </div>
    `,
    iconSize: [72, 72],
    iconAnchor: [36, 36],
    popupAnchor: [0, -30],
  })
}

function PatrolLiveMap({ patrol }: { patrol: NonNullable<DashboardSummary['activePatrol']> }) {
  // Controla o mapa real da patrulha com OpenStreetMap, ultimo ponto e trilha historica.
  const mapContainerRef = useRef<HTMLDivElement | null>(null)
  const mapRef = useRef<L.Map | null>(null)
  const markerRef = useRef<L.Marker | null>(null)
  const accuracyCircleRef = useRef<L.Circle | null>(null)
  const routeLineRef = useRef<L.Polyline | null>(null)

  useEffect(() => {
    if (!mapContainerRef.current) return

    if (!mapRef.current) {
      mapRef.current = L.map(mapContainerRef.current, {
        zoomControl: false,
      }).setView([patrol.latitude, patrol.longitude], 16)

      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap contributors',
      }).addTo(mapRef.current)
    }

    const currentPosition = L.latLng(patrol.latitude, patrol.longitude)
    const trailPoints = patrol.telemetryTrail.length > 0
      ? patrol.telemetryTrail.map((point) => L.latLng(point.latitude, point.longitude))
      : [currentPosition]
    const markerIcon = buildGuardMarkerIcon(patrol.agentPhotoUrl, patrol.agentName)

    if (!routeLineRef.current) {
      routeLineRef.current = L.polyline(trailPoints, {
        color: '#d3a24a',
        weight: 5,
        opacity: 0.9,
      }).addTo(mapRef.current)
    } else {
      routeLineRef.current.setLatLngs(trailPoints)
    }

    if (trailPoints.length > 1) {
      mapRef.current.fitBounds(routeLineRef.current.getBounds(), {
        padding: [36, 36],
        maxZoom: 17,
      })
    } else {
      mapRef.current.setView(currentPosition, Math.max(mapRef.current.getZoom(), 16))
    }

    if (!markerRef.current) {
      markerRef.current = L.marker(currentPosition, { icon: markerIcon }).addTo(mapRef.current)
    } else {
      markerRef.current.setLatLng(currentPosition)
      markerRef.current.setIcon(markerIcon)
    }

    markerRef.current.bindPopup(
      `<strong>${patrol.agentName}</strong><br/>${patrol.vehiclePlate} • ${patrol.vehicleModel}<br/>${patrol.speedKmh.toFixed(0)} km/h • precisao ${patrol.accuracyMeters.toFixed(0)} m`,
    )

    if (!accuracyCircleRef.current) {
      accuracyCircleRef.current = L.circle(currentPosition, {
        radius: Math.max(patrol.accuracyMeters, 15),
        color: '#d3a24a',
        fillColor: '#d3a24a',
        fillOpacity: 0.12,
      }).addTo(mapRef.current)
    } else {
      accuracyCircleRef.current.setLatLng(currentPosition)
      accuracyCircleRef.current.setRadius(Math.max(patrol.accuracyMeters, 15))
    }
  }, [
    patrol.accuracyMeters,
    patrol.agentName,
    patrol.agentPhotoUrl,
    patrol.latitude,
    patrol.longitude,
    patrol.speedKmh,
    patrol.telemetryTrail,
    patrol.vehicleModel,
    patrol.vehiclePlate,
  ])

  useEffect(() => {
    return () => {
      mapRef.current?.remove()
      mapRef.current = null
      markerRef.current = null
      accuracyCircleRef.current = null
      routeLineRef.current = null
    }
  }, [])

  return <div className="live-map" ref={mapContainerRef} />
}

function App() {
  // Estado da sessao, dados operacionais e formularios de manutencao do painel.
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [portal, setPortal] = useState<ClientPortal | null>(null)
  const [users, setUsers] = useState<AppUser[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [session, setSession] = useState<AuthSession | null>(() => readStoredSession() ?? initialSession)
  const [credentials, setCredentials] = useState(() => {
    return initialCredentials
  })
  const [authenticated, setAuthenticated] = useState(() => Boolean(readStoredSession()))
  const [currentUsername, setCurrentUsername] = useState<string | null>(null)
  const [currentRoles, setCurrentRoles] = useState<string[]>([])
  const [agentForm, setAgentForm] = useState(initialAgentForm)
  const [userForm, setUserForm] = useState(initialUserForm)
  const [residentForm, setResidentForm] = useState(initialResidentForm)
  const [vehicleForm, setVehicleForm] = useState(initialVehicleForm)
  const [maintenanceForm, setMaintenanceForm] = useState(initialMaintenanceForm)
  const [shiftForm, setShiftForm] = useState(initialShiftForm)
  const [incidentForm, setIncidentForm] = useState(initialIncidentForm)
  const [editingAgentId, setEditingAgentId] = useState<number | null>(null)
  const [editingUserId, setEditingUserId] = useState<number | null>(null)
  const [editingResidentId, setEditingResidentId] = useState<number | null>(null)
  const [editingVehicleId, setEditingVehicleId] = useState<number | null>(null)
  const [editingShiftId, setEditingShiftId] = useState<number | null>(null)
  const [editingIncidentId, setEditingIncidentId] = useState<number | null>(null)
  const [isPending, startTransition] = useTransition()

  // Deriva os escopos reais do usuario para nao exibir acoes que o backend vai negar.
  const isClient = currentRoles.includes('ROLE_CLIENT')
  const canManageUsers = currentRoles.includes('ROLE_ADMIN')
  const canManageCatalog = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR'])
  const canUpdateOperations = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_RONDA'])
  const canCreateOperations = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR'])
  const canRegisterHandoff = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR'])

  const shiftSubmitDisabled = !canCreateOperations && editingShiftId === null
  const incidentSubmitDisabled = !canCreateOperations && editingIncidentId === null

  async function apiFetch(path: string, init?: RequestInit) {
    // Wrapper unico para chamadas autenticadas e expiracao de sessao.
    const response = await fetch(`${API_BASE_URL}${path}`, {
      ...init,
      headers: {
        ...(init?.headers ?? {}),
        ...(session ? { Authorization: `Bearer ${session.accessToken}` } : {}),
      },
    })

    if (response.status === 401) {
      window.localStorage.removeItem(STORAGE_KEY)
      setSession(null)
      setAuthenticated(false)
      setCurrentUsername(null)
      setCurrentRoles([])
      throw new Error('Sua sessao expirou ou as credenciais sao invalidas.')
    }

    return response
  }

  async function loadData() {
    // Carrega o perfil logado e escolhe entre dashboard administrativo ou portal do cliente.
    if (!authenticated) return

    setLoading(true)
    setError(null)

    try {
      const meResponse = await apiFetch('/api/auth/me')
      if (!meResponse.ok) throw new Error('Nao foi possivel validar a sessao.')

      const me = (await meResponse.json()) as { username: string; roles: string[] }
      setCurrentUsername(me.username)
      setCurrentRoles(me.roles)
      const adminCanManageUsers = me.roles.includes('ROLE_ADMIN')

      if (me.roles.includes('ROLE_CLIENT')) {
        const portalResponse = await apiFetch('/api/client/portal')
        if (!portalResponse.ok) throw new Error('Nao foi possivel carregar o portal do cliente.')
        setPortal((await portalResponse.json()) as ClientPortal)
        setSummary(null)
        setUsers([])
      } else {
        const [summaryResponse, usersResponse] = await Promise.all([
          apiFetch('/api/dashboard/summary'),
          adminCanManageUsers ? apiFetch('/api/users') : Promise.resolve(null),
        ])
        if (!summaryResponse.ok) throw new Error('Nao foi possivel carregar o painel operacional.')
        if (usersResponse && !usersResponse.ok) throw new Error('Nao foi possivel carregar a gestao de usuarios.')
        setSummary((await summaryResponse.json()) as DashboardSummary)
        setUsers(usersResponse ? ((await usersResponse.json()) as AppUser[]) : [])
        setPortal(null)
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao carregar a interface.')
      setSummary(null)
      setPortal(null)
      setUsers([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadData()
  }, [authenticated])

  function resetAgentForm() {
    setAgentForm(initialAgentForm)
    setEditingAgentId(null)
  }

  function resetUserForm() {
    setUserForm(initialUserForm)
    setEditingUserId(null)
  }

  function resetResidentForm() {
    setResidentForm(initialResidentForm)
    setEditingResidentId(null)
  }

  function resetVehicleForm() {
    setVehicleForm(initialVehicleForm)
    setEditingVehicleId(null)
  }

  function resetMaintenanceForm() {
    setMaintenanceForm(initialMaintenanceForm)
  }

  function resetShiftForm() {
    setShiftForm(initialShiftForm)
    setEditingShiftId(null)
  }

  function resetIncidentForm() {
    setIncidentForm(initialIncidentForm)
    setEditingIncidentId(null)
  }

  function startAgentEdit(agent: Agent) {
    setEditingAgentId(agent.id)
    setAgentForm({
      fullName: agent.fullName,
      badgeCode: agent.badgeCode,
      cnhCategory: agent.cnhCategory,
      cnhExpiry: agent.cnhExpiry,
      photoUrl: agent.photoUrl ?? '',
      medicalExamExpiry: agent.medicalExamExpiry ?? '',
      workExamsExpiry: agent.workExamsExpiry ?? '',
      documentNotes: agent.documentNotes ?? '',
      status: agent.status,
    })
  }

  function startUserEdit(user: AppUser) {
    setEditingUserId(user.id)
    setUserForm({
      username: user.username,
      password: '',
      role: user.role,
      enabled: user.enabled,
    })
  }

  function startVehicleEdit(vehicle: Vehicle) {
    setEditingVehicleId(vehicle.id)
    setVehicleForm({
      plate: vehicle.plate,
      model: vehicle.model,
      currentKm: String(vehicle.currentKm),
      nextMaintenanceKm: String(vehicle.nextMaintenanceKm),
      ipvaExpiry: vehicle.ipvaExpiry ?? '',
      licensingExpiry: vehicle.licensingExpiry ?? '',
      insuranceExpiry: vehicle.insuranceExpiry ?? '',
      lastMaintenanceAt: vehicle.lastMaintenanceAt ?? '',
      maintenanceNotes: vehicle.maintenanceNotes ?? '',
      status: vehicle.status,
    })
  }

  function startResidentEdit(resident: Resident) {
    setEditingResidentId(resident.id)
    setResidentForm({
      fullName: resident.fullName,
      phoneNumber: resident.phoneNumber,
      address: resident.address,
      referenceNote: resident.referenceNote ?? '',
      status: resident.status,
    })
  }

  function startShiftEdit(shift: Shift) {
    setEditingShiftId(shift.id)
    setShiftForm({
      agentId: String(shift.agentId),
      vehicleId: String(shift.vehicleId),
      scheduledStartAt: formatDateTimeLocal(shift.scheduledStartAt),
      scheduledEndAt: formatDateTimeLocal(shift.scheduledEndAt),
      status: shift.status,
      attendanceStatus: shift.attendanceStatus,
      coverageForAgentId: shift.coverageForAgentId != null ? String(shift.coverageForAgentId) : '',
      attendanceNotes: shift.attendanceNotes ?? '',
      endKm: shift.endKm != null ? String(shift.endKm) : '',
      handoffToAgentId: shift.handoffToAgentId != null ? String(shift.handoffToAgentId) : '',
      handoffNotes: shift.handoffNotes ?? '',
      fuelLevelPercent: shift.fuelLevelPercent != null ? String(shift.fuelLevelPercent) : '',
      tiresChecked: shift.tiresChecked,
      lightsChecked: shift.lightsChecked,
      documentsChecked: shift.documentsChecked,
      checklistNotes: shift.checklistNotes ?? '',
    })
  }

  function handleResidentSelection(residentId: string) {
    const resident = summary?.residents.find((item) => String(item.id) === residentId)
    setIncidentForm((current) => ({
      ...current,
      residentId,
      residentName: resident ? resident.fullName : current.residentName,
      address: resident ? resident.address : current.address,
    }))
  }

  function startIncidentEdit(incident: Incident) {
    const resident = summary?.residents.find((item) => item.fullName === incident.residentName && item.address === incident.address)
    const agent = summary?.agents.find((item) => item.fullName === incident.assignedAgentName)
    const vehicle = summary?.vehicles.find((item) => item.plate === incident.vehiclePlate)

    setEditingIncidentId(incident.id)
    setIncidentForm({
      type: incident.type,
      priority: incident.priority,
      status: incident.status,
      residentId: resident ? String(resident.id) : '',
      residentName: incident.residentName,
      address: incident.address,
      assignedAgentId: agent ? String(agent.id) : '',
      vehicleId: vehicle ? String(vehicle.id) : '',
    })
  }

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)

    try {
      const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      })

      if (!response.ok) {
        throw new Error('Login invalido. Verifique usuario e senha.')
      }

      const loginSession = (await response.json()) as AuthSession
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(loginSession))
      setSession(loginSession)
      setAuthenticated(true)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao autenticar.')
    }
  }

  function handleLogout() {
    window.localStorage.removeItem(STORAGE_KEY)
    setSession(null)
    setAuthenticated(false)
    setCurrentUsername(null)
    setCurrentRoles([])
    setSummary(null)
    setPortal(null)
    setUsers([])
    resetAgentForm()
    resetUserForm()
    resetResidentForm()
    resetVehicleForm()
    resetShiftForm()
    resetIncidentForm()
  }

  async function saveEntity(path: string, method: 'POST' | 'PUT', payload: unknown, failMessage: string, onSuccess: () => void) {
    // Padroniza criacao/edicao de cadastros e operacoes no painel.
    const response = await apiFetch(path, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })

    if (response.status === 403) {
      setError('Seu perfil nao tem permissao para esta acao.')
      return
    }

    if (!response.ok) {
      setError(failMessage)
      return
    }

    onSuccess()
    startTransition(() => {
      void loadData()
    })
  }

  async function handleDelete(path: string, message: string, afterDelete: () => void) {
    // Padroniza exclusoes com confirmacao e recarga do estado.
    if (!window.confirm(message)) return

    const response = await apiFetch(path, { method: 'DELETE' })

    if (response.status === 403) {
      setError('Seu perfil nao tem permissao para excluir este registro.')
      return
    }

    if (!response.ok) {
      setError('Nao foi possivel concluir a exclusao.')
      return
    }

    afterDelete()
    startTransition(() => {
      void loadData()
    })
  }

  async function handleAgentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const path = editingAgentId === null ? '/api/agents' : `/api/agents/${editingAgentId}`
    const method = editingAgentId === null ? 'POST' : 'PUT'
    const payload =
      editingAgentId === null
        ? {
            fullName: agentForm.fullName,
            badgeCode: agentForm.badgeCode,
            cnhCategory: agentForm.cnhCategory,
            cnhExpiry: agentForm.cnhExpiry,
            photoUrl: agentForm.photoUrl || null,
            medicalExamExpiry: agentForm.medicalExamExpiry || null,
            workExamsExpiry: agentForm.workExamsExpiry || null,
            documentNotes: agentForm.documentNotes || null,
          }
        : agentForm

    await saveEntity(path, method, payload, 'Nao foi possivel salvar o agente.', resetAgentForm)
  }

  async function handleUserSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const path = editingUserId === null ? '/api/users' : `/api/users/${editingUserId}`
    const method = editingUserId === null ? 'POST' : 'PUT'
    const payload = editingUserId === null
      ? userForm
      : {
          username: userForm.username,
          password: userForm.password || null,
          role: userForm.role,
          enabled: userForm.enabled,
        }

    await saveEntity(path, method, payload, 'Nao foi possivel salvar o usuario.', resetUserForm)
  }

  async function handleVehicleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const path = editingVehicleId === null ? '/api/vehicles' : `/api/vehicles/${editingVehicleId}`
    const method = editingVehicleId === null ? 'POST' : 'PUT'
    const basePayload = {
      plate: vehicleForm.plate,
      model: vehicleForm.model,
      currentKm: Number(vehicleForm.currentKm),
      nextMaintenanceKm: Number(vehicleForm.nextMaintenanceKm),
      ipvaExpiry: vehicleForm.ipvaExpiry || null,
      licensingExpiry: vehicleForm.licensingExpiry || null,
      insuranceExpiry: vehicleForm.insuranceExpiry || null,
      lastMaintenanceAt: vehicleForm.lastMaintenanceAt || null,
      maintenanceNotes: vehicleForm.maintenanceNotes || null,
    }
    const payload = editingVehicleId === null ? basePayload : { ...basePayload, status: vehicleForm.status }
    await saveEntity(path, method, payload, 'Nao foi possivel salvar a viatura.', resetVehicleForm)
  }

  async function handleMaintenanceSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    await saveEntity(
      `/api/vehicles/${Number(maintenanceForm.vehicleId)}/maintenance`,
      'POST',
      {
        type: maintenanceForm.type,
        serviceDate: maintenanceForm.serviceDate || null,
        kmAtService: maintenanceForm.kmAtService ? Number(maintenanceForm.kmAtService) : null,
        nextMaintenanceKm: maintenanceForm.nextMaintenanceKm ? Number(maintenanceForm.nextMaintenanceKm) : null,
        costAmount: maintenanceForm.costAmount ? Number(maintenanceForm.costAmount) : null,
        supplierName: maintenanceForm.supplierName || null,
        description: maintenanceForm.description,
        resolved: maintenanceForm.resolved,
      },
      'Nao foi possivel registrar a manutencao.',
      resetMaintenanceForm,
    )
  }

  async function handleResidentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const path = editingResidentId === null ? '/api/residents' : `/api/residents/${editingResidentId}`
    const method = editingResidentId === null ? 'POST' : 'PUT'
    const basePayload = {
      fullName: residentForm.fullName,
      phoneNumber: residentForm.phoneNumber,
      address: residentForm.address,
      referenceNote: residentForm.referenceNote || null,
    }
    const payload = editingResidentId === null ? basePayload : { ...basePayload, status: residentForm.status }
    await saveEntity(path, method, payload, 'Nao foi possivel salvar o morador.', resetResidentForm)
  }

  async function handleShiftSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const path = editingShiftId === null ? '/api/shifts' : `/api/shifts/${editingShiftId}`
    const method = editingShiftId === null ? 'POST' : 'PUT'
    const basePayload = {
      agentId: Number(shiftForm.agentId),
      vehicleId: Number(shiftForm.vehicleId),
      scheduledStartAt: new Date(shiftForm.scheduledStartAt).toISOString(),
      scheduledEndAt: new Date(shiftForm.scheduledEndAt).toISOString(),
      attendanceStatus: shiftForm.attendanceStatus,
      coverageForAgentId: shiftForm.coverageForAgentId ? Number(shiftForm.coverageForAgentId) : null,
      attendanceNotes: shiftForm.attendanceNotes || null,
      endKm: shiftForm.endKm ? Number(shiftForm.endKm) : null,
      fuelLevelPercent: shiftForm.fuelLevelPercent ? Number(shiftForm.fuelLevelPercent) : null,
      tiresChecked: shiftForm.tiresChecked,
      lightsChecked: shiftForm.lightsChecked,
      documentsChecked: shiftForm.documentsChecked,
      checklistNotes: shiftForm.checklistNotes || null,
    }
    const payload = editingShiftId === null ? basePayload : { ...basePayload, status: shiftForm.status }
    await saveEntity(path, method, payload, 'Nao foi possivel salvar o turno.', resetShiftForm)
  }

  async function handleShiftHandoff(shiftId: number) {
    // Registra no painel a troca formal de vigilante sem descartar o turno em andamento.
    if (!shiftForm.handoffToAgentId) {
      setError('Selecione o vigilante que assumira o turno.')
      return
    }

    const currentShift = summary?.shifts.find((shift) => shift.id === shiftId)
    if (!currentShift) {
      setError('Turno nao encontrado para registrar a passagem.')
      return
    }

    await saveEntity(
      `/api/shifts/${shiftId}/handoff`,
      'POST',
      {
        fromAgentId: currentShift.agentId,
        toAgentId: Number(shiftForm.handoffToAgentId),
        notes: shiftForm.handoffNotes || null,
      },
      'Nao foi possivel concluir a troca de turno.',
      resetShiftForm,
    )
  }

  async function handleIncidentSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const path = editingIncidentId === null ? '/api/incidents' : `/api/incidents/${editingIncidentId}`
    const method = editingIncidentId === null ? 'POST' : 'PUT'
    const basePayload = {
      type: incidentForm.type,
      priority: incidentForm.priority,
      residentId: incidentForm.residentId ? Number(incidentForm.residentId) : null,
      residentName: incidentForm.residentName,
      address: incidentForm.address,
      assignedAgentId: incidentForm.assignedAgentId ? Number(incidentForm.assignedAgentId) : null,
      vehicleId: incidentForm.vehicleId ? Number(incidentForm.vehicleId) : null,
    }
    const payload = editingIncidentId === null ? basePayload : { ...basePayload, status: incidentForm.status }
    await saveEntity(path, method, payload, 'Nao foi possivel salvar a ocorrencia.', resetIncidentForm)
  }

  if (!authenticated) {
    // Tela inicial de autenticacao para administracao, ronda e cliente.
    return (
      <main className="login-shell">
        <section className="login-card">
          <img alt="Logo VMAB" className="brand-logo login-logo" src={logoVmab} />
          <p className="eyebrow">Acesso ao Sistema</p>
          <h1>Seguranca Comunitaria</h1>
          <p className="hero-copy">Entre com um dos perfis do ambiente para testar o painel operacional, a visao da ronda ou o portal do cliente.</p>
          {error ? <div className="alert error">{error}</div> : null}
          <form className="login-form" onSubmit={handleLogin}>
            <input required placeholder="Usuario" value={credentials.username} onChange={(event) => setCredentials((current) => ({ ...current, username: event.target.value }))} />
            <input required type="password" placeholder="Senha" value={credentials.password} onChange={(event) => setCredentials((current) => ({ ...current, password: event.target.value }))} />
            <button type="submit">Entrar</button>
          </form>

          <div className="hint-grid">
            <article className="hint-card">
              <strong>Admin</strong>
              <small>admin / admin123</small>
            </article>
            <article className="hint-card">
              <strong>Supervisor</strong>
              <small>supervisor / super123</small>
            </article>
            <article className="hint-card">
              <strong>Cliente</strong>
              <small>cliente / cliente123</small>
            </article>
            <article className="hint-card">
              <strong>Ronda</strong>
              <small>ronda / ronda123</small>
            </article>
          </div>
        </section>
      </main>
    )
  }

  if (isClient && portal) {
    return (
      <div className="app-shell client-shell">
        <aside className="sidebar">
          <img alt="Logo VMAB" className="brand-logo sidebar-logo" src={logoVmab} />
          <p className="eyebrow">Portal do Cliente</p>
          <h1>Visao do Contrato</h1>
          <p className="sidebar-copy">Sessao autenticada como {currentUsername}. Esta area e somente leitura para acompanhamento da operacao.</p>
          <div className="sidebar-block">
            <span className="sidebar-label">Acesso contratado</span>
            <ul>
              <li>resumo do SLA operacional</li>
              <li>incidentes recentes</li>
              <li>visao consolidada da frota</li>
            </ul>
          </div>
        </aside>

        <main className="workspace">
          <header className="hero">
            <div>
              <p className="eyebrow">Portal</p>
              <h2>Resumo operacional do cliente</h2>
              <p className="hero-copy">Acompanhe a operacao em tempo real sem acessar cadastros internos da equipe ou parametros administrativos.</p>
            </div>
            <div className="hero-actions">
              <button className="refresh-button" onClick={() => void loadData()} type="button">Atualizar</button>
              <button className="secondary-button" onClick={handleLogout} type="button">Sair</button>
            </div>
          </header>

          {error ? <div className="alert error">{error}</div> : null}
          {loading ? <div className="alert">Carregando portal...</div> : null}

          <section className="stats-grid stats-grid-client">
            <article className="metric-card"><span>Turnos ativos</span><strong>{portal.activeShifts}</strong></article>
            <article className="metric-card"><span>Ocorrencias abertas</span><strong>{portal.openIncidents}</strong></article>
            <article className="metric-card"><span>Viaturas disponiveis</span><strong>{portal.availableVehicles}</strong></article>
            <article className="metric-card"><span>Alertas de manutencao</span><strong>{portal.maintenanceAlerts}</strong></article>
          </section>

          <section className="panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">Incidentes recentes</p>
                <h3>Ultimas ocorrencias</h3>
              </div>
            </div>
            {portal.recentIncidents.length === 0 ? (
              <p className="panel-note">Nenhuma ocorrencia recente para este cliente.</p>
            ) : (
              <div className="list">
                {portal.recentIncidents.map((incident) => (
                  <article className="list-row" key={incident.id}>
                    <div>
                      <strong>{translateIncidentType(incident.type)} | {incident.residentName}</strong>
                      <small>{incident.address} | {incident.assignedAgentName ?? 'Sem agente'} | {incident.vehiclePlate ?? 'Sem viatura'}</small>
                    </div>
                    <span className={`tag ${incident.priority.toLowerCase()}`}>{translateIncidentPriority(incident.priority)}</span>
                  </article>
                ))}
              </div>
            )}
          </section>
        </main>
      </div>
    )
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <img alt="Logo VMAB" className="brand-logo sidebar-logo" src={logoVmab} />
        <p className="eyebrow">Painel Operacional</p>
        <h1>Operacao Comunitaria</h1>
        <p className="sidebar-copy">Sessao autenticada como {currentUsername}. O conteudo abaixo respeita o perfil logado e as permissoes do backend.</p>

        <div className="sidebar-block">
          <span className="sidebar-label">Perfis ativos</span>
          <div className="role-list">
            {currentRoles.map((role) => (
              <span className="role-chip" key={role}>{translateRole(role)}</span>
            ))}
          </div>
        </div>

        <div className="sidebar-block">
          <span className="sidebar-label">Escopo liberado</span>
          <ul>
            <li>{canManageCatalog ? 'cadastros completos de equipe e frota' : 'cadastros apenas em leitura'}</li>
            <li>{canCreateOperations ? 'criacao e exclusao de turnos e ocorrencias' : 'atualizacao operacional apenas em itens existentes'}</li>
            <li>dashboard em tempo real via backend</li>
          </ul>
        </div>
      </aside>

      <main className="workspace">
        <header className="hero">
          <div>
            <p className="eyebrow">Painel</p>
            <h2>Core administrativo integrado</h2>
            <p className="hero-copy">Esta interface concentra cadastros, jornada, ocorrencias e a base operacional do produto em um unico fluxo.</p>
          </div>
          <div className="hero-actions">
            <button className="refresh-button" onClick={() => void loadData()} type="button">Atualizar</button>
            <button className="secondary-button" onClick={handleLogout} type="button">Sair</button>
          </div>
        </header>

        {error ? <div className="alert error">{error}</div> : null}
        {loading ? <div className="alert">Carregando painel...</div> : null}
        {isPending ? <div className="alert">Sincronizando alteracoes...</div> : null}

        {summary ? (
          <>
            {summary.activePatrol ? (
              // Bloco principal de acompanhamento da patrulha ativa em tempo real.
              <section className="panel patrol-panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Patrulha Ativa</p>
                    <h3>Rastreamento da viatura em tempo real</h3>
                  </div>
                </div>
                <div className="patrol-layout">
                  <div className="patrol-identity">
                    {summary.activePatrol.agentPhotoUrl ? (
                      <img alt={summary.activePatrol.agentName} className="agent-photo" src={summary.activePatrol.agentPhotoUrl} />
                    ) : (
                      <div className="agent-photo agent-photo-fallback">{summary.activePatrol.agentName.slice(0, 2).toUpperCase()}</div>
                    )}
                    <div>
                      <strong className="patrol-name">{summary.activePatrol.agentName}</strong>
                      <p className="patrol-meta">Vigilante em ronda • cracha {summary.activePatrol.agentBadgeCode}</p>
                      <p className="patrol-meta">Viatura {summary.activePatrol.vehiclePlate} • {summary.activePatrol.vehicleModel}</p>
                      <p className="patrol-meta">KM atual {summary.activePatrol.vehicleCurrentKm.toLocaleString('pt-BR')} • status {translateGenericOperationalText(summary.activePatrol.vehicleStatus)}</p>
                    </div>
                  </div>

                  <div className="patrol-map-card">
                    <div className="patrol-map-header">
                      <div>
                        <p className="eyebrow">Mapa Tatico</p>
                        <strong>Mapa real da viatura</strong>
                      </div>
                      <span className="patrol-map-status">GPS online</span>
                    </div>

                    <div className="live-map-shell">
                      <PatrolLiveMap patrol={summary.activePatrol} />

                      <div className="map-overlay overlay-top">
                        <strong>{summary.activePatrol.agentName}</strong>
                        <small>{summary.activePatrol.vehiclePlate} â€¢ {summary.activePatrol.vehicleModel}</small>
                      </div>

                      <div className="map-overlay overlay-bottom">
                        <strong>{summary.activePatrol.speedKmh.toFixed(0)} km/h</strong>
                        <small>Precisao {summary.activePatrol.accuracyMeters.toFixed(0)} m â€¢ turno {summary.activePatrol.shiftId}</small>
                      </div>
                    </div>
                  </div>

                    <div className="patrol-telemetry">
                      <div className="telemetry-card">
                        <span>Velocidade</span>
                        <strong>{summary.activePatrol.speedKmh.toFixed(0)} km/h</strong>
                        <small>Ultima atualizacao {formatDate(summary.activePatrol.updatedAt)}</small>
                      </div>
                      <div className="telemetry-card">
                        <span>KM percorridos no turno</span>
                        <strong>{summary.activePatrol.traveledKmInShift.toFixed(2)} km</strong>
                        <small>Turno {summary.activePatrol.shiftId} com trilha GPS consolidada</small>
                      </div>
                    </div>

                    <div className="route-card">
                      <div className="route-list">
                        {summary.activePatrol.routeStops.map((stop) => (
                          <article className="route-stop" key={`${stop.title}-${stop.detail}`}>
                          <strong>{stop.title}</strong>
                          <small>{stop.detail}</small>
                            <span>{translateGenericOperationalText(stop.status)}</span>
                        </article>
                      ))}
                    </div>
                  </div>
                </div>
              </section>
            ) : null}

            <section className="stats-grid">
              <article className="metric-card"><span>Moradores no cadastro</span><strong>{summary.totalResidents}</strong></article>
              <article className="metric-card"><span>Agentes no cadastro</span><strong>{summary.totalAgents}</strong></article>
              <article className="metric-card"><span>Agentes ativos</span><strong>{summary.activeAgents}</strong></article>
              <article className="metric-card"><span>Viaturas disponiveis</span><strong>{summary.availableVehicles}</strong></article>
              <article className="metric-card"><span>Turnos em operacao</span><strong>{summary.activeShifts}</strong></article>
              <article className="metric-card"><span>Turnos atrasados</span><strong>{summary.lateShifts}</strong></article>
              <article className="metric-card"><span>Faltas abertas</span><strong>{summary.absentShifts}</strong></article>
              <article className="metric-card"><span>Ocorrencias abertas</span><strong>{summary.openIncidents}</strong></article>
              <article className="metric-card"><span>Alertas de manutencao</span><strong>{summary.maintenanceAlerts}</strong></article>
            </section>

            <section className="content-grid">
              {/* Area transacional do painel com cadastros e operacao diaria. */}
              {canManageUsers ? (
                <section className="panel">
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">Acesso</p>
                      <h3>{editingUserId === null ? 'Usuarios e perfis' : 'Editar usuario'}</h3>
                    </div>
                  </div>
                  <p className="panel-note">Apenas administradores podem criar, editar, ativar ou remover acessos do sistema.</p>
                  <form className="form-grid" onSubmit={handleUserSubmit}>
                    <input required placeholder="Nome de usuario" value={userForm.username} onChange={(event) => setUserForm((current) => ({ ...current, username: event.target.value }))} />
                    <input required={editingUserId === null} type="password" placeholder={editingUserId === null ? 'Senha inicial' : 'Nova senha (opcional)'} value={userForm.password} onChange={(event) => setUserForm((current) => ({ ...current, password: event.target.value }))} />
                    <select value={userForm.role} onChange={(event) => setUserForm((current) => ({ ...current, role: event.target.value as AppUserRole }))}>
                      {appUserRoleOptions.map((role) => <option key={role} value={role}>{translateRole(role)}</option>)}
                    </select>
                    <label className="checkbox-field">
                      <input checked={userForm.enabled} type="checkbox" onChange={(event) => setUserForm((current) => ({ ...current, enabled: event.target.checked }))} />
                      <span>Usuario ativo</span>
                    </label>
                    <div className="button-row">
                      <button type="submit">{editingUserId === null ? 'Cadastrar usuario' : 'Salvar usuario'}</button>
                      {editingUserId !== null ? <button className="secondary-button" onClick={resetUserForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                  <div className="list">
                    {users.map((user) => (
                      <article className="list-row" key={user.id}>
                        <div>
                          <strong>{user.username}</strong>
                          <small>{translateRole(user.role)} | criado em {formatDate(user.createdAt)}</small>
                        </div>
                        <div className="row-actions">
                          <span className={`tag ${user.enabled ? 'active' : 'blocked'}`}>{user.enabled ? 'Ativo' : 'Bloqueado'}</span>
                          <button className="ghost-button" onClick={() => startUserEdit(user)} type="button">Editar</button>
                          {user.username !== 'admin' ? (
                            <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/users/${user.id}`, 'Deseja remover este usuario?', resetUserForm)} type="button">Excluir</button>
                          ) : null}
                        </div>
                      </article>
                    ))}
                  </div>
                </section>
              ) : null}

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Cadastro</p>
                    <h3>{editingAgentId === null ? 'Equipe de Ronda' : 'Editar agente'}</h3>
                  </div>
                </div>
                <p className="panel-note">{canManageCatalog ? 'Cadastro completo liberado para admin e supervisor.' : 'Seu perfil possui apenas leitura neste bloco.'}</p>
                {canManageCatalog ? (
                  <form className="form-grid" onSubmit={handleAgentSubmit}>
                    <input required placeholder="Nome completo" value={agentForm.fullName} onChange={(event) => setAgentForm((current) => ({ ...current, fullName: event.target.value }))} />
                    <input required placeholder="Codigo / cracha" value={agentForm.badgeCode} onChange={(event) => setAgentForm((current) => ({ ...current, badgeCode: event.target.value }))} />
                    <input required placeholder="Categoria CNH" value={agentForm.cnhCategory} onChange={(event) => setAgentForm((current) => ({ ...current, cnhCategory: event.target.value }))} />
                    <input required type="date" value={agentForm.cnhExpiry} onChange={(event) => setAgentForm((current) => ({ ...current, cnhExpiry: event.target.value }))} />
                    <input type="date" value={agentForm.medicalExamExpiry} onChange={(event) => setAgentForm((current) => ({ ...current, medicalExamExpiry: event.target.value }))} />
                    <input type="date" value={agentForm.workExamsExpiry} onChange={(event) => setAgentForm((current) => ({ ...current, workExamsExpiry: event.target.value }))} />
                    <input placeholder="URL da foto do vigilante" value={agentForm.photoUrl} onChange={(event) => setAgentForm((current) => ({ ...current, photoUrl: event.target.value }))} />
                    <input placeholder="Observacoes documentais" value={agentForm.documentNotes} onChange={(event) => setAgentForm((current) => ({ ...current, documentNotes: event.target.value }))} />
                    <select value={agentForm.status} onChange={(event) => setAgentForm((current) => ({ ...current, status: event.target.value as AgentStatus }))}>
                      {agentStatusOptions.map((status) => <option key={status} value={status}>{translateAgentStatus(status)}</option>)}
                    </select>
                    <div className="button-row">
                      <button type="submit">{editingAgentId === null ? 'Cadastrar agente' : 'Salvar agente'}</button>
                      {editingAgentId !== null ? <button className="secondary-button" onClick={resetAgentForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                ) : null}
                <div className="list">
                  {summary.agents.map((agent) => (
                    <article className="list-row" key={agent.id}>
                      <div>
                        <strong>{agent.fullName}</strong>
                        <small>Cracha {agent.badgeCode} | CNH {agent.cnhCategory} ate {agent.cnhExpiry} | exame medico {agent.medicalExamExpiry ?? 'nao informado'}</small>
                      </div>
                      <div className="row-actions">
                        <span className={`tag ${agent.status.toLowerCase()}`}>{translateAgentStatus(agent.status)}</span>
                        {canManageCatalog ? <button className="ghost-button" onClick={() => startAgentEdit(agent)} type="button">Editar</button> : null}
                        {canManageCatalog ? <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/agents/${agent.id}`, 'Deseja remover este agente?', resetAgentForm)} type="button">Excluir</button> : null}
                      </div>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Moradores</p>
                    <h3>{editingResidentId === null ? 'Cadastro de moradores' : 'Editar morador'}</h3>
                  </div>
                </div>
                <p className="panel-note">{canManageCatalog ? 'Cadastre moradores para agilizar o disparo de ocorrencias e o atendimento.' : 'Seu perfil possui apenas leitura neste bloco.'}</p>
                {canManageCatalog ? (
                  <form className="form-grid" onSubmit={handleResidentSubmit}>
                    <input required placeholder="Nome completo" value={residentForm.fullName} onChange={(event) => setResidentForm((current) => ({ ...current, fullName: event.target.value }))} />
                    <input required placeholder="Telefone" value={residentForm.phoneNumber} onChange={(event) => setResidentForm((current) => ({ ...current, phoneNumber: event.target.value }))} />
                    <input required placeholder="Endereco" value={residentForm.address} onChange={(event) => setResidentForm((current) => ({ ...current, address: event.target.value }))} />
                    <input placeholder="Observacao / referencia" value={residentForm.referenceNote} onChange={(event) => setResidentForm((current) => ({ ...current, referenceNote: event.target.value }))} />
                    <select value={residentForm.status} onChange={(event) => setResidentForm((current) => ({ ...current, status: event.target.value as ResidentStatus }))}>
                      {residentStatusOptions.map((status) => <option key={status} value={status}>{translateResidentStatus(status)}</option>)}
                    </select>
                    <div className="button-row">
                      <button type="submit">{editingResidentId === null ? 'Cadastrar morador' : 'Salvar morador'}</button>
                      {editingResidentId !== null ? <button className="secondary-button" onClick={resetResidentForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                ) : null}
                <div className="list">
                  {summary.residents.map((resident) => (
                    <article className="list-row" key={resident.id}>
                      <div>
                        <strong>{resident.fullName}</strong>
                        <small>{resident.phoneNumber} | {resident.address}</small>
                      </div>
                      <div className="row-actions">
                        <span className={`tag ${resident.status.toLowerCase()}`}>{translateResidentStatus(resident.status)}</span>
                        {canManageCatalog ? <button className="ghost-button" onClick={() => startResidentEdit(resident)} type="button">Editar</button> : null}
                        {canManageCatalog ? <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/residents/${resident.id}`, 'Deseja remover este morador?', resetResidentForm)} type="button">Excluir</button> : null}
                      </div>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Frota</p>
                    <h3>{editingVehicleId === null ? 'Viaturas ativas' : 'Editar viatura'}</h3>
                  </div>
                </div>
                <p className="panel-note">{canManageCatalog ? 'Cadastre, edite e acompanhe manutencao da frota.' : 'Seu perfil acompanha a frota em leitura.'}</p>
                {canManageCatalog ? (
                  <form className="form-grid" onSubmit={handleVehicleSubmit}>
                    <input required placeholder="Placa" value={vehicleForm.plate} onChange={(event) => setVehicleForm((current) => ({ ...current, plate: event.target.value }))} />
                    <input required placeholder="Modelo" value={vehicleForm.model} onChange={(event) => setVehicleForm((current) => ({ ...current, model: event.target.value }))} />
                    <input required min="0" type="number" placeholder="KM atual" value={vehicleForm.currentKm} onChange={(event) => setVehicleForm((current) => ({ ...current, currentKm: event.target.value }))} />
                    <input required min="1" type="number" placeholder="Proxima manutencao" value={vehicleForm.nextMaintenanceKm} onChange={(event) => setVehicleForm((current) => ({ ...current, nextMaintenanceKm: event.target.value }))} />
                    <input type="date" value={vehicleForm.lastMaintenanceAt} onChange={(event) => setVehicleForm((current) => ({ ...current, lastMaintenanceAt: event.target.value }))} />
                    <input type="date" value={vehicleForm.ipvaExpiry} onChange={(event) => setVehicleForm((current) => ({ ...current, ipvaExpiry: event.target.value }))} />
                    <input type="date" value={vehicleForm.licensingExpiry} onChange={(event) => setVehicleForm((current) => ({ ...current, licensingExpiry: event.target.value }))} />
                    <input type="date" value={vehicleForm.insuranceExpiry} onChange={(event) => setVehicleForm((current) => ({ ...current, insuranceExpiry: event.target.value }))} />
                    <input placeholder="Observacoes de manutencao / documentos" value={vehicleForm.maintenanceNotes} onChange={(event) => setVehicleForm((current) => ({ ...current, maintenanceNotes: event.target.value }))} />
                    <select value={vehicleForm.status} onChange={(event) => setVehicleForm((current) => ({ ...current, status: event.target.value as VehicleStatus }))}>
                      {vehicleStatusOptions.map((status) => <option key={status} value={status}>{translateVehicleStatus(status)}</option>)}
                    </select>
                    <div className="button-row">
                      <button type="submit">{editingVehicleId === null ? 'Cadastrar viatura' : 'Salvar viatura'}</button>
                      {editingVehicleId !== null ? <button className="secondary-button" onClick={resetVehicleForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                ) : null}
                {/* OS de manutencao para vincular custo, KM e liberacao da viatura ao cadastro da frota. */}
                {canManageCatalog ? (
                  <form className="form-grid maintenance-form" onSubmit={handleMaintenanceSubmit}>
                    <select required value={maintenanceForm.vehicleId} onChange={(event) => setMaintenanceForm((current) => ({ ...current, vehicleId: event.target.value }))}>
                      <option value="">Viatura da manutencao</option>
                      {summary.vehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.plate} - {vehicle.model}</option>)}
                    </select>
                    <select value={maintenanceForm.type} onChange={(event) => setMaintenanceForm((current) => ({ ...current, type: event.target.value as VehicleMaintenanceType }))}>
                      {vehicleMaintenanceTypeOptions.map((type) => <option key={type} value={type}>{translateVehicleMaintenanceType(type)}</option>)}
                    </select>
                    <input type="date" value={maintenanceForm.serviceDate} onChange={(event) => setMaintenanceForm((current) => ({ ...current, serviceDate: event.target.value }))} />
                    <input min="0" type="number" placeholder="KM da manutencao" value={maintenanceForm.kmAtService} onChange={(event) => setMaintenanceForm((current) => ({ ...current, kmAtService: event.target.value }))} />
                    <input min="0" type="number" placeholder="Proxima revisao (km)" value={maintenanceForm.nextMaintenanceKm} onChange={(event) => setMaintenanceForm((current) => ({ ...current, nextMaintenanceKm: event.target.value }))} />
                    <input min="0" step="0.01" type="number" placeholder="Custo (R$)" value={maintenanceForm.costAmount} onChange={(event) => setMaintenanceForm((current) => ({ ...current, costAmount: event.target.value }))} />
                    <input placeholder="Fornecedor / oficina" value={maintenanceForm.supplierName} onChange={(event) => setMaintenanceForm((current) => ({ ...current, supplierName: event.target.value }))} />
                    <input required placeholder="Descricao do servico" value={maintenanceForm.description} onChange={(event) => setMaintenanceForm((current) => ({ ...current, description: event.target.value }))} />
                    <label className="checkbox-field">
                      <input checked={maintenanceForm.resolved} type="checkbox" onChange={(event) => setMaintenanceForm((current) => ({ ...current, resolved: event.target.checked }))} />
                      <span>Servico concluido e viatura liberada</span>
                    </label>
                    <div className="button-row">
                      <button type="submit">Registrar manutencao</button>
                      <button className="secondary-button" onClick={resetMaintenanceForm} type="button">Limpar</button>
                    </div>
                  </form>
                ) : null}
                <div className="list">
                  {summary.vehicles.map((vehicle) => (
                    <article className="list-row" key={vehicle.id}>
                      <div>
                        <strong>{vehicle.model}</strong>
                        <small>{vehicle.plate} | {vehicle.currentKm.toLocaleString('pt-BR')} km | revisao em {vehicle.nextMaintenanceKm.toLocaleString('pt-BR')} km | IPVA {vehicle.ipvaExpiry ?? 'nao informado'}</small>
                      </div>
                      <div className="row-actions">
                        <span className={`tag ${vehicle.status.toLowerCase()}`}>{translateVehicleStatus(vehicle.status)}</span>
                        {canManageCatalog ? <button className="ghost-button" onClick={() => startVehicleEdit(vehicle)} type="button">Editar</button> : null}
                        {canManageCatalog ? <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/vehicles/${vehicle.id}`, 'Deseja remover esta viatura?', resetVehicleForm)} type="button">Excluir</button> : null}
                      </div>
                    </article>
                  ))}
                </div>
                {/* Historico resumido da manutencao para consulta rapida da equipe operacional. */}
                <div className="list maintenance-list">
                  {summary.maintenanceRecords.map((record: VehicleMaintenanceRecord) => (
                    <article className="list-row" key={record.id}>
                      <div>
                        <strong>{translateVehicleMaintenanceType(record.type)} | {record.vehiclePlate}</strong>
                        <small>{record.serviceDate ?? 'sem data'} | {record.kmAtService != null ? `${record.kmAtService.toLocaleString('pt-BR')} km` : 'km nao informado'} | {record.supplierName ?? 'fornecedor nao informado'} | {record.costAmount != null ? `R$ ${record.costAmount.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : 'sem custo informado'}</small>
                        <small>{record.description}</small>
                      </div>
                      <div className="row-actions">
                        <span className={`tag ${record.resolved ? 'active' : 'maintenance'}`}>{record.resolved ? 'Concluida' : 'Em aberto'}</span>
                      </div>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Turnos</p>
                    <h3>{editingShiftId === null ? 'Operacao de turno' : 'Editar turno'}</h3>
                  </div>
                </div>
                <p className="panel-note">
                  {canCreateOperations
                    ? 'Admin e supervisor podem criar, reatribuir e encerrar turnos.'
                    : 'A ronda pode atualizar turnos existentes. Para criar um novo, use admin ou supervisor.'}
                </p>
                {canUpdateOperations ? (
                  <form className="form-grid" onSubmit={handleShiftSubmit}>
                    <select required value={shiftForm.agentId} onChange={(event) => setShiftForm((current) => ({ ...current, agentId: event.target.value }))}>
                      <option value="">Selecione um agente</option>
                      {summary.agents.map((agent) => <option key={agent.id} value={agent.id}>{agent.fullName}</option>)}
                    </select>
                    <select required value={shiftForm.vehicleId} onChange={(event) => setShiftForm((current) => ({ ...current, vehicleId: event.target.value }))}>
                      <option value="">Selecione uma viatura</option>
                      {summary.vehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.plate} - {vehicle.model}</option>)}
                    </select>
                    <input required type="datetime-local" value={shiftForm.scheduledStartAt} onChange={(event) => setShiftForm((current) => ({ ...current, scheduledStartAt: event.target.value }))} />
                    <input required type="datetime-local" value={shiftForm.scheduledEndAt} onChange={(event) => setShiftForm((current) => ({ ...current, scheduledEndAt: event.target.value }))} />
                    <input min="0" type="number" placeholder="KM final ao encerrar" value={shiftForm.endKm} onChange={(event) => setShiftForm((current) => ({ ...current, endKm: event.target.value }))} />
                    <input min="0" max="100" type="number" placeholder="Combustivel (%)" value={shiftForm.fuelLevelPercent} onChange={(event) => setShiftForm((current) => ({ ...current, fuelLevelPercent: event.target.value }))} />
                    <select value={shiftForm.status} onChange={(event) => setShiftForm((current) => ({ ...current, status: event.target.value as ShiftStatus }))}>
                      {shiftStatusOptions.map((status) => <option key={status} value={status}>{translateShiftStatus(status)}</option>)}
                    </select>
                    <select value={shiftForm.attendanceStatus} onChange={(event) => setShiftForm((current) => ({ ...current, attendanceStatus: event.target.value as ShiftAttendanceStatus }))}>
                      {shiftAttendanceOptions.map((status) => <option key={status} value={status}>{translateShiftAttendanceStatus(status)}</option>)}
                    </select>
                    <select value={shiftForm.coverageForAgentId} onChange={(event) => setShiftForm((current) => ({ ...current, coverageForAgentId: event.target.value }))}>
                      <option value="">Vigilante coberto</option>
                      {summary.agents.filter((agent) => String(agent.id) !== shiftForm.agentId).map((agent) => <option key={agent.id} value={agent.id}>{agent.fullName}</option>)}
                    </select>
                    <select value={shiftForm.handoffToAgentId} onChange={(event) => setShiftForm((current) => ({ ...current, handoffToAgentId: event.target.value }))}>
                      <option value="">Vigilante que assume</option>
                      {summary.agents.filter((agent) => String(agent.id) !== shiftForm.agentId).map((agent) => <option key={agent.id} value={agent.id}>{agent.fullName}</option>)}
                    </select>
                    {/* Checklist minimo para fechar jornada e registrar a condicao da viatura no turno. */}
                    <input placeholder="Observacoes de escala / presenca" value={shiftForm.attendanceNotes} onChange={(event) => setShiftForm((current) => ({ ...current, attendanceNotes: event.target.value }))} />
                    <input placeholder="Observacoes da troca de turno" value={shiftForm.handoffNotes} onChange={(event) => setShiftForm((current) => ({ ...current, handoffNotes: event.target.value }))} />
                    <input placeholder="Observacoes do checklist" value={shiftForm.checklistNotes} onChange={(event) => setShiftForm((current) => ({ ...current, checklistNotes: event.target.value }))} />
                    <label className="checkbox-field">
                      <input checked={shiftForm.tiresChecked} type="checkbox" onChange={(event) => setShiftForm((current) => ({ ...current, tiresChecked: event.target.checked }))} />
                      <span>Pneus verificados</span>
                    </label>
                    <label className="checkbox-field">
                      <input checked={shiftForm.lightsChecked} type="checkbox" onChange={(event) => setShiftForm((current) => ({ ...current, lightsChecked: event.target.checked }))} />
                      <span>Luzes verificadas</span>
                    </label>
                    <label className="checkbox-field">
                      <input checked={shiftForm.documentsChecked} type="checkbox" onChange={(event) => setShiftForm((current) => ({ ...current, documentsChecked: event.target.checked }))} />
                      <span>Documentos da viatura conferidos</span>
                    </label>
                    <div className="button-row">
                      <button disabled={shiftSubmitDisabled} type="submit">{editingShiftId === null ? 'Cadastrar turno' : 'Salvar turno'}</button>
                      {editingShiftId !== null && canRegisterHandoff ? <button className="secondary-button" onClick={() => void handleShiftHandoff(editingShiftId)} type="button">Registrar troca</button> : null}
                      {editingShiftId !== null ? <button className="secondary-button" onClick={resetShiftForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                ) : null}
                <div className="list">
                  {summary.shifts.map((shift) => (
                    <article className="list-row" key={shift.id}>
                      <div>
                        <strong>{shift.agentName}</strong>
                        <small>{shift.vehiclePlate} | escala {formatDate(shift.scheduledStartAt)} ate {formatDate(shift.scheduledEndAt)} | presenca {translateShiftAttendanceStatus(shift.attendanceStatus)}{shift.lateMinutes != null ? ` (${shift.lateMinutes} min)` : ''} | cobertura {shift.coverageForAgentName ?? 'nao aplicada'} | checklist {shift.documentsChecked ? 'ok' : 'pendente'}</small>
                      </div>
                      <div className="row-actions">
                        <span className={`tag ${shift.status.toLowerCase()}`}>{translateShiftStatus(shift.status)}</span>
                        {canUpdateOperations ? <button className="ghost-button" onClick={() => startShiftEdit(shift)} type="button">Editar</button> : null}
                        {canCreateOperations ? <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/shifts/${shift.id}`, 'Deseja remover este turno?', resetShiftForm)} type="button">Excluir</button> : null}
                      </div>
                    </article>
                  ))}
                </div>
              </section>

              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Ocorrencias</p>
                    <h3>{editingIncidentId === null ? 'Central de chamados' : 'Editar ocorrencia'}</h3>
                  </div>
                </div>
                <p className="panel-note">
                  {canCreateOperations
                    ? 'Admin e supervisor podem registrar novas ocorrencias e encerrar o ciclo completo.'
                    : 'A ronda pode atualizar status de ocorrencias ja abertas.'}
                </p>
                {canUpdateOperations ? (
                  <form className="form-grid" onSubmit={handleIncidentSubmit}>
                    <select value={incidentForm.type} onChange={(event) => setIncidentForm((current) => ({ ...current, type: event.target.value as IncidentType }))}>
                      {incidentTypeOptions.map((type) => <option key={type} value={type}>{translateIncidentType(type)}</option>)}
                    </select>
                    <select value={incidentForm.priority} onChange={(event) => setIncidentForm((current) => ({ ...current, priority: event.target.value as IncidentPriority }))}>
                      {incidentPriorityOptions.map((priority) => <option key={priority} value={priority}>{translateIncidentPriority(priority)}</option>)}
                    </select>
                    <select value={incidentForm.residentId} onChange={(event) => handleResidentSelection(event.target.value)}>
                      <option value="">Morador avulso</option>
                      {summary.residents.map((resident) => <option key={resident.id} value={resident.id}>{resident.fullName}</option>)}
                    </select>
                    <input required placeholder="Morador" value={incidentForm.residentName} onChange={(event) => setIncidentForm((current) => ({ ...current, residentName: event.target.value }))} />
                    <input required placeholder="Endereco" value={incidentForm.address} onChange={(event) => setIncidentForm((current) => ({ ...current, address: event.target.value }))} />
                    <select value={incidentForm.assignedAgentId} onChange={(event) => setIncidentForm((current) => ({ ...current, assignedAgentId: event.target.value }))}>
                      <option value="">Sem agente</option>
                      {summary.agents.map((agent) => <option key={agent.id} value={agent.id}>{agent.fullName}</option>)}
                    </select>
                    <select value={incidentForm.vehicleId} onChange={(event) => setIncidentForm((current) => ({ ...current, vehicleId: event.target.value }))}>
                      <option value="">Sem viatura</option>
                      {summary.vehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.plate} - {vehicle.model}</option>)}
                    </select>
                    <select value={incidentForm.status} onChange={(event) => setIncidentForm((current) => ({ ...current, status: event.target.value as IncidentStatus }))}>
                      {incidentStatusOptions.map((status) => <option key={status} value={status}>{translateIncidentStatus(status)}</option>)}
                    </select>
                    <div className="button-row">
                      <button disabled={incidentSubmitDisabled} type="submit">{editingIncidentId === null ? 'Cadastrar ocorrencia' : 'Salvar ocorrencia'}</button>
                      {editingIncidentId !== null ? <button className="secondary-button" onClick={resetIncidentForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                ) : null}
                <div className="list">
                  {summary.incidents.map((incident) => (
                    <article className="list-row" key={incident.id}>
                      <div>
                        <strong>{translateIncidentType(incident.type)} | {incident.residentName}</strong>
                        <small>{incident.address} | {incident.assignedAgentName ?? 'Sem agente'} | {incident.vehiclePlate ?? 'Sem viatura'}</small>
                      </div>
                      <div className="row-actions">
                        <span className={`tag ${incident.priority.toLowerCase()}`}>{translateIncidentPriority(incident.priority)}</span>
                        {canUpdateOperations ? <button className="ghost-button" onClick={() => startIncidentEdit(incident)} type="button">Editar</button> : null}
                        {canCreateOperations ? <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/incidents/${incident.id}`, 'Deseja remover esta ocorrencia?', resetIncidentForm)} type="button">Excluir</button> : null}
                      </div>
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
