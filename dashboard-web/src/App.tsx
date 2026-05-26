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
  AuthenticatedUser,
  AuthSession,
  ClientOperationalReport,
  ClientPortal,
  DashboardSummary,
  HrAttendanceType,
  HrEmployeeCategory,
  HrEmployeeStatus,
  FleetOperationalReport,
  Incident,
  IncidentEvidence,
  IncidentPriority,
  IncidentStatus,
  IncidentType,
  OperationsStreamEvent,
  PrivacyRequest,
  PrivacyRequestStatus,
  PrivacyRequestType,
  PrivacyRetentionStatus,
  PrivacySubjectType,
  Resident,
  ResidentStatus,
  AuditActionType,
  AuditRecord,
  AuditReportCategory,
  AuditReportResponse,
  Shift,
  ShiftAttendanceStatus,
  ShiftStatus,
  Vehicle,
  VehicleMaintenanceRecord,
  VehicleMaintenancePriority,
  VehicleMaintenanceStatus,
  VehicleMaintenanceType,
  VehicleStatus,
} from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''
const STORAGE_KEY = 'seguranca-auth'

const agentStatusOptions: AgentStatus[] = ['ACTIVE', 'ON_DUTY', 'OFF_DUTY', 'BLOCKED']
const residentStatusOptions: ResidentStatus[] = ['ACTIVE', 'INACTIVE']
const vehicleStatusOptions: VehicleStatus[] = ['AVAILABLE', 'IN_OPERATION', 'MAINTENANCE', 'BLOCKED']
const vehicleMaintenanceTypeOptions: VehicleMaintenanceType[] = ['PREVENTIVE', 'CORRECTIVE', 'INSPECTION', 'DOCUMENTATION']
const shiftStatusOptions: ShiftStatus[] = ['PLANNED', 'ACTIVE', 'HANDOFF_PENDING', 'HANDOFF', 'CLOSED']
const privacyRequestTypeOptions: PrivacyRequestType[] = ['EXPORT', 'DELETE']
const privacySubjectTypeOptions: PrivacySubjectType[] = ['RESIDENT', 'APP_USER', 'AGENT']
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
  linkedAgentId: '',
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
  accessPin: '',
  coercionPin: '',
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
  dispatchNotes: '',
  arrivalNotes: '',
  closureNotes: '',
}

const initialPrivacyForm = {
  requestType: 'EXPORT' as PrivacyRequestType,
  subjectType: 'RESIDENT' as PrivacySubjectType,
  subjectId: '',
  notes: '',
  status: 'IN_PROGRESS' as PrivacyRequestStatus,
  notifySubject: false,
  notificationChannel: '',
  notificationNotes: '',
}

const dashboardSectionLinks = [
  { id: 'sec-tempo-real', label: 'Tempo real', description: 'Eventos recentes e trilha operacional' },
  { id: 'sec-patrulha', label: 'Patrulha', description: 'Mapa, rota e telemetria da viatura' },
  { id: 'sec-indicadores', label: 'Indicadores', description: 'Resumo geral da operação' },
  { id: 'sec-rh', label: 'RH', description: 'Cadastros, acessos, turnos e ponto' },
  { id: 'sec-frota', label: 'Frota', description: 'Saude e manutencao das viaturas' },
  { id: 'sec-auditoria', label: 'Auditoria', description: 'Acoes criticas e rastreabilidade' },
  { id: 'sec-evidencias', label: 'Evidencias', description: 'Arquivos operacionais e LGPD' },
] as const

type DashboardSectionId = (typeof dashboardSectionLinks)[number]['id'] | 'sec-acesso' | 'sec-cadastro' | 'sec-operacao'

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value))
}

function formatOptionalDate(value?: string | null) {
  return value ? new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' }).format(new Date(value)) : 'Nao informado'
}

function formatCurrency(value?: number | null) {
  if (value == null) {
    return 'Sem custo'
  }

  return value.toLocaleString('pt-BR', {
    style: 'currency',
    currency: 'BRL',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })
}

function formatDateTimeLocal(value: string) {
  const date = new Date(value)
  const offset = date.getTimezoneOffset()
  const local = new Date(date.getTime() - offset * 60000)
  return local.toISOString().slice(0, 16)
}

function normalizeUppercaseInput(value: string) {
  return value.toUpperCase()
}

function normalizeDigitsInput(value: string, maxLength?: number) {
  const digitsOnly = value.replace(/\D/g, '')
  return typeof maxLength === 'number' ? digitsOnly.slice(0, maxLength) : digitsOnly
}

function formatBrazilPhoneInput(value: string) {
  const digits = value.replace(/\D/g, '').slice(0, 11)

  if (digits.length === 0) {
    return ''
  }

  if (digits.length <= 2) {
    return `(${digits}`
  }

  if (digits.length <= 6) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2)}`
  }

  if (digits.length <= 10) {
    return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`
  }

  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`
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
  return ({
    PLANNED: 'Planejado',
    ACTIVE: 'Ativo',
    HANDOFF_PENDING: 'Troca pendente',
    HANDOFF: 'Troca de turno',
    CLOSED: 'Encerrado',
  } as Record<string, string>)[status] ?? status
}

function translatePrivacyRequestType(type: PrivacyRequestType) {
  return { EXPORT: 'Exportacao', DELETE: 'Exclusao' }[type]
}

function translatePrivacySubjectType(type: PrivacySubjectType) {
  return { RESIDENT: 'Morador', APP_USER: 'Usuario', AGENT: 'Agente' }[type]
}

function translatePrivacyRequestStatus(status: PrivacyRequestStatus) {
  return { OPEN: 'Aberto', IN_PROGRESS: 'Em andamento', COMPLETED: 'Concluido', REJECTED: 'Rejeitado' }[status]
}

function translateVehicleMaintenancePriority(priority: VehicleMaintenancePriority) {
  return { LOW: 'Baixa', MEDIUM: 'Media', HIGH: 'Alta', CRITICAL: 'Critica' }[priority]
}

function translateVehicleMaintenanceStatus(status: VehicleMaintenanceStatus) {
  return { OPEN: 'Aberta', IN_PROGRESS: 'Em andamento', WAITING_PARTS: 'Aguardando pecas', COMPLETED: 'Concluida', CANCELLED: 'Cancelada' }[status]
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

function translateHrEmployeeCategory(category: HrEmployeeCategory) {
  return {
    VIGILANTE: 'Vigilante',
    SUPERVISOR: 'Supervisor',
    ADMINISTRATIVO: 'Administrativo',
    OPERACIONAL: 'Operacional',
    OUTRO: 'Outro',
  }[category]
}

function translateHrEmployeeStatus(status: HrEmployeeStatus) {
  return {
    ACTIVE: 'Ativo',
    BLOCKED: 'Bloqueado',
    VACATION: 'Ferias',
    LEAVE: 'Afastado',
    TERMINATED: 'Desligado',
  }[status]
}

function translateHrAttendanceType(type: HrAttendanceType) {
  return {
    CHECK_IN: 'Entrada',
    CHECK_OUT: 'Saida',
  }[type]
}

function hrStatusTagClass(status: HrEmployeeStatus) {
  if (status === 'ACTIVE') return 'active'
  if (status === 'BLOCKED' || status === 'TERMINATED') return 'blocked'
  return 'maintenance'
}

function translateAuditActionType(actionType: AuditActionType) {
  return {
    CREATE: 'Criacao',
    UPDATE: 'Atualizacao',
    DELETE: 'Exclusao',
    HANDOFF: 'Troca',
    MAINTENANCE: 'Manutencao',
    TELEMETRY: 'Telemetria',
    INCIDENT_WORKFLOW: 'Fluxo da ocorrencia',
    AUTH: 'Autenticacao',
    RESIDENT_ALERT: 'Alerta do morador',
  }[actionType]
}

function auditCategoryForRecord(record: AuditRecord) {
  const entityName = record.entityName.toLowerCase()

  if (record.actionType === 'AUTH' || record.actionType === 'RESIDENT_ALERT') {
    return 'SEGURANCA' as const
  }

  if (record.actionType === 'MAINTENANCE' || entityName.includes('vehiclemaintenance') || entityName.includes('vehicle')) {
    return 'FROTA' as const
  }

  if (record.actionType === 'HANDOFF' || record.actionType === 'INCIDENT_WORKFLOW' || entityName.includes('incident') || entityName.includes('residentalert')) {
    return 'OPERACIONAL' as const
  }

  if (entityName.includes('hr') || entityName.includes('agent')) {
    return 'RH' as const
  }

  return 'GESTAO' as const
}

function translateAuditCategory(category: AuditReportCategory) {
  return {
    ALL: 'Todas',
    GESTAO: 'Gestao',
    OPERACIONAL: 'Operacional',
    FROTA: 'Frota',
    RH: 'RH',
    SEGURANCA: 'Seguranca',
  }[category]
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

function translateRealtimeEventType(type: string) {
  return {
    CONNECTED: 'Canal conectado',
    CREATE: 'Criacao',
    UPDATE: 'Atualizacao',
    DELETE: 'Exclusao',
    HANDOFF: 'Troca de turno',
    MAINTENANCE: 'Manutencao',
    TELEMETRY: 'Telemetria',
    INCIDENT_WORKFLOW: 'Workflow de ocorrencia',
    AUTH: 'Autenticacao',
    RESIDENT_ALERT: 'Alerta do morador',
  }[type] ?? type
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
  const [clientReport, setClientReport] = useState<ClientOperationalReport | null>(null)
  const [users, setUsers] = useState<AppUser[]>([])
  const [lastRefreshAt, setLastRefreshAt] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [authMessage, setAuthMessage] = useState<string | null>(null)
  const [session, setSession] = useState<AuthSession | null>(() => readStoredSession() ?? initialSession)
  const [credentials, setCredentials] = useState(() => {
    return initialCredentials
  })
  const [authenticated, setAuthenticated] = useState(() => Boolean(readStoredSession()))
  const [currentUsername, setCurrentUsername] = useState<string | null>(null)
  const [currentRoles, setCurrentRoles] = useState<string[]>([])
  const [currentLinkedAgentId, setCurrentLinkedAgentId] = useState<number | null>(null)
  const [agentForm, setAgentForm] = useState(initialAgentForm)
  const [userForm, setUserForm] = useState(initialUserForm)
  const [residentForm, setResidentForm] = useState(initialResidentForm)
  const [vehicleForm, setVehicleForm] = useState(initialVehicleForm)
  const [maintenanceForm, setMaintenanceForm] = useState(initialMaintenanceForm)
  const [shiftForm, setShiftForm] = useState(initialShiftForm)
  const [incidentForm, setIncidentForm] = useState(initialIncidentForm)
  const [passwordResetForm, setPasswordResetForm] = useState({ username: '', resetCode: '', newPassword: '' })
  const [editingAgentId, setEditingAgentId] = useState<number | null>(null)
  const [editingUserId, setEditingUserId] = useState<number | null>(null)
  const [editingResidentId, setEditingResidentId] = useState<number | null>(null)
  const [editingVehicleId, setEditingVehicleId] = useState<number | null>(null)
  const [editingShiftId, setEditingShiftId] = useState<number | null>(null)
  const [editingIncidentId, setEditingIncidentId] = useState<number | null>(null)
  const [fleetReport, setFleetReport] = useState<FleetOperationalReport | null>(null)
  const [incidentEvidences, setIncidentEvidences] = useState<IncidentEvidence[]>([])
  const [privacyRequests, setPrivacyRequests] = useState<PrivacyRequest[]>([])
  const [retentionStatus, setRetentionStatus] = useState<PrivacyRetentionStatus | null>(null)
  const [privacyForm, setPrivacyForm] = useState(initialPrivacyForm)
  const [editingPrivacyRequestId, setEditingPrivacyRequestId] = useState<number | null>(null)
  const [realtimeEvents, setRealtimeEvents] = useState<OperationsStreamEvent[]>([])
  const [activeDashboardSection, setActiveDashboardSection] = useState<DashboardSectionId>('sec-tempo-real')
  const [auditReport, setAuditReport] = useState<AuditReportResponse | null>(null)
  const [auditReportError, setAuditReportError] = useState<string | null>(null)
  const [auditReportLoading, setAuditReportLoading] = useState(false)
  const [auditFilter, setAuditFilter] = useState<AuditReportCategory>('ALL')
  const [auditPeriodDays, setAuditPeriodDays] = useState(30)
  const [auditIncludeAuth, setAuditIncludeAuth] = useState(false)
  const [isPending, startTransition] = useTransition()

  // Deriva os escopos reais do usuario para nao exibir acoes que o backend vai negar.
  const isClient = currentRoles.includes('ROLE_CLIENT')
  const canManageUsers = currentRoles.includes('ROLE_ADMIN')
  const canManagePrivacy = currentRoles.includes('ROLE_ADMIN')
  const canManageCatalog = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR'])
  const canUpdateOperations = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR', 'ROLE_RONDA'])
  const canCreateOperations = hasAnyRole(currentRoles, ['ROLE_ADMIN', 'ROLE_SUPERVISOR'])
  const auditRecords = auditReport?.records ?? []
  const auditCategoryCounts = {
    GESTAO: auditReport?.recordsByCategory?.GESTAO ?? 0,
    OPERACIONAL: auditReport?.recordsByCategory?.OPERACIONAL ?? 0,
    FROTA: auditReport?.recordsByCategory?.FROTA ?? 0,
    RH: auditReport?.recordsByCategory?.RH ?? 0,
    SEGURANCA: auditReport?.recordsByCategory?.SEGURANCA ?? 0,
  }
  const auditTopActors = Object.entries(auditReport?.recordsByActor ?? {}).slice(0, 4)
  const auditTopActions = Object.entries(auditReport?.recordsByActionType ?? {}).slice(0, 4)

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
      setCurrentLinkedAgentId(null)
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

      const me = (await meResponse.json()) as AuthenticatedUser
      setCurrentUsername(me.username)
      setCurrentRoles(me.roles)
      setCurrentLinkedAgentId(me.linkedAgentId ?? null)
      const adminCanManageUsers = me.roles.includes('ROLE_ADMIN')

      if (me.roles.includes('ROLE_CLIENT')) {
        const [portalResponse, reportResponse] = await Promise.all([
          apiFetch('/api/client/portal'),
          apiFetch('/api/client/report'),
        ])
        if (!portalResponse.ok) throw new Error('Nao foi possivel carregar o portal do cliente.')
        if (!reportResponse.ok) throw new Error('Nao foi possivel carregar o relatorio do cliente.')
        setPortal((await portalResponse.json()) as ClientPortal)
        setClientReport((await reportResponse.json()) as ClientOperationalReport)
        setSummary(null)
        setUsers([])
        setLastRefreshAt(new Date().toISOString())
      } else {
        const [summaryResponse, usersResponse, fleetReportResponse, evidenceResponse, privacyRequestsResponse, retentionResponse] = await Promise.all([
          apiFetch('/api/dashboard/summary'),
          adminCanManageUsers ? apiFetch('/api/users') : Promise.resolve(null),
          apiFetch('/api/vehicles/report'),
          apiFetch('/api/incidents/evidence'),
          me.roles.includes('ROLE_ADMIN') ? apiFetch('/api/privacy/requests') : Promise.resolve(null),
          me.roles.includes('ROLE_ADMIN') ? apiFetch('/api/privacy/status') : Promise.resolve(null),
        ])
        if (!summaryResponse.ok) throw new Error('Nao foi possivel carregar o painel operacional.')
        if (usersResponse && !usersResponse.ok) throw new Error('Nao foi possivel carregar a gestao de usuarios.')
        setSummary((await summaryResponse.json()) as DashboardSummary)
        setUsers(usersResponse ? ((await usersResponse.json()) as AppUser[]) : [])
        if (fleetReportResponse.ok) setFleetReport((await fleetReportResponse.json()) as FleetOperationalReport)
        if (evidenceResponse.ok) setIncidentEvidences((await evidenceResponse.json()) as IncidentEvidence[])
        if (privacyRequestsResponse?.ok) setPrivacyRequests((await privacyRequestsResponse.json()) as PrivacyRequest[])
        if (retentionResponse?.ok) setRetentionStatus((await retentionResponse.json()) as PrivacyRetentionStatus)
        setPortal(null)
        setClientReport(null)
        setLastRefreshAt(new Date().toISOString())
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao carregar a interface.')
      setSummary(null)
      setPortal(null)
      setClientReport(null)
      setUsers([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadData()
  }, [authenticated])

  useEffect(() => {
    if (!authenticated || activeDashboardSection !== 'sec-auditoria') {
      return
    }

    let cancelled = false

    async function loadAuditReport() {
      setAuditReportLoading(true)
      setAuditReportError(null)
      setAuditReport(null)

      try {
        const response = await apiFetch(`/api/audit/report?days=${auditPeriodDays}&category=${auditFilter}&includeAuth=${auditIncludeAuth}`)
        if (!response.ok) {
          throw new Error('Nao foi possivel carregar o relatorio de auditoria.')
        }

        const report = (await response.json()) as AuditReportResponse
        if (!cancelled) {
          setAuditReport(report)
        }
      } catch (cause) {
        if (cancelled) {
          return
        }

        setAuditReportError(cause instanceof Error ? cause.message : 'Falha inesperada ao carregar o relatorio de auditoria.')
      } finally {
        if (!cancelled) {
          setAuditReportLoading(false)
        }
      }
    }

    void loadAuditReport()

    const intervalId = window.setInterval(() => {
      void loadAuditReport()
    }, 30000)

    return () => {
      cancelled = true
      window.clearInterval(intervalId)
    }
  }, [authenticated, activeDashboardSection, auditFilter, auditPeriodDays, auditIncludeAuth, session])

  useEffect(() => {
    // Mantem o painel fresco em ambiente operacional sem exigir clique manual o tempo inteiro.
    if (!authenticated) {
      return
    }

    const intervalId = window.setInterval(() => {
      void loadData()
    }, 30000)

    return () => window.clearInterval(intervalId)
  }, [authenticated, session])

  useEffect(() => {
    // Usa SSE quando disponivel para reduzir atraso operacional no painel sem depender so de polling.
    if (!authenticated || !session?.accessToken || typeof window === 'undefined' || typeof EventSource === 'undefined') {
      return
    }

    const eventSource = new EventSource(`${API_BASE_URL}/api/events/stream?token=${encodeURIComponent(session.accessToken)}`)
    const handleRealtimeRefresh = (event: MessageEvent<string>) => {
      try {
        const parsedEvent = JSON.parse(event.data) as OperationsStreamEvent
        setRealtimeEvents((current) => [parsedEvent, ...current].slice(0, 6))
      } catch {
        // Se o payload vier malformado, o reload do painel ainda preserva a consistencia do dado.
      }

      startTransition(() => {
        void loadData()
      })
    }

    eventSource.addEventListener('operations', handleRealtimeRefresh)
    eventSource.onerror = () => {
      // O polling de fallback continua ativo mesmo se o canal realtime cair.
    }

    return () => {
      eventSource.removeEventListener('operations', handleRealtimeRefresh)
      eventSource.close()
    }
  }, [authenticated, session?.accessToken])

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
      linkedAgentId: user.linkedAgentId != null ? String(user.linkedAgentId) : '',
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
      accessPin: '',
      coercionPin: '',
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
      dispatchNotes: incident.dispatchNotes ?? '',
      arrivalNotes: incident.arrivalNotes ?? '',
      closureNotes: incident.closureNotes ?? '',
    })
  }

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setAuthMessage(null)

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
      setRealtimeEvents([])
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao autenticar.')
    }
  }

  function handleLogout() {
    void (async () => {
      if (session) {
        await apiFetch('/api/auth/logout', { method: 'POST' })
      }

      window.localStorage.removeItem(STORAGE_KEY)
      setSession(null)
      setAuthenticated(false)
      setRealtimeEvents([])
      setCurrentUsername(null)
      setCurrentRoles([])
      setCurrentLinkedAgentId(null)
      setSummary(null)
      setPortal(null)
      setUsers([])
      setAuthMessage('Sessao encerrada e tokens antigos invalidados.')
      resetAgentForm()
      resetUserForm()
      resetResidentForm()
      resetVehicleForm()
      resetMaintenanceForm()
      resetShiftForm()
      resetIncidentForm()
    })().catch(() => {
      window.localStorage.removeItem(STORAGE_KEY)
      setSession(null)
      setAuthenticated(false)
      setRealtimeEvents([])
      setCurrentUsername(null)
      setCurrentRoles([])
      setCurrentLinkedAgentId(null)
      setSummary(null)
      setPortal(null)
      setUsers([])
    })
  }

  async function handlePasswordResetRequest(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setAuthMessage(null)

    const response = await fetch(`${API_BASE_URL}/api/auth/password-reset/request`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: passwordResetForm.username }),
    })

    if (!response.ok) {
      setError('Nao foi possivel solicitar o reset de senha.')
      return
    }

    const payload = (await response.json()) as { username: string; resetCode: string; expiresAt: string }
    setPasswordResetForm((current) => ({ ...current, resetCode: payload.resetCode }))
    setAuthMessage(`Codigo de reset gerado para ${payload.username}: ${payload.resetCode}`)
  }

  async function handlePasswordResetConfirm(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError(null)
    setAuthMessage(null)

    const response = await fetch(`${API_BASE_URL}/api/auth/password-reset/confirm`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: passwordResetForm.username,
        resetCode: passwordResetForm.resetCode,
        newPassword: passwordResetForm.newPassword,
      }),
    })

    if (!response.ok) {
      setError('Nao foi possivel concluir o reset de senha.')
      return
    }

    setAuthMessage('Senha redefinida com sucesso. Todas as sessoes anteriores foram invalidadas.')
    setPasswordResetForm({ username: '', resetCode: '', newPassword: '' })
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
      ? {
          ...userForm,
          linkedAgentId: userForm.linkedAgentId ? Number(userForm.linkedAgentId) : null,
        }
      : {
          username: userForm.username,
          password: userForm.password || null,
          role: userForm.role,
          enabled: userForm.enabled,
          linkedAgentId: userForm.linkedAgentId ? Number(userForm.linkedAgentId) : null,
        }

    await saveEntity(path, method, payload, 'Nao foi possivel salvar o usuario.', resetUserForm)
  }

  function canRequestHandoffForShiftAgent(agentId: number) {
    // A ronda so pode pedir troca do proprio turno; admin e supervisor continuam podendo abrir a solicitacao.
    return canCreateOperations || (currentRoles.includes('ROLE_RONDA') && currentLinkedAgentId === agentId)
  }

  function canRespondHandoffForShift(shift: Shift) {
    // O aceite duplo agora depende do vigilante autenticado estar vinculado ao turno pendente.
    return currentRoles.includes('ROLE_RONDA') && currentLinkedAgentId != null && currentLinkedAgentId === shift.handoffToAgentId
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
      accessPin: residentForm.accessPin || null,
      coercionPin: residentForm.coercionPin || null,
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
    // Solicita troca formal de turno sem transferir responsabilidade ate o aceite do receptor.
    if (!shiftForm.handoffToAgentId) {
      setError('Selecione o vigilante que assumira o turno.')
      return
    }

    await saveEntity(
      `/api/shifts/${shiftId}/handoff-request`,
      'POST',
      {
        toAgentId: Number(shiftForm.handoffToAgentId),
        notes: shiftForm.handoffNotes || null,
      },
      'Nao foi possivel solicitar a troca de turno.',
      resetShiftForm,
    )
  }

  async function handleShiftHandoffAccept(shiftId: number) {
    // Supervisor confirma a passagem; so entao o agente receptor assume a responsabilidade.
    await saveEntity(
      `/api/shifts/${shiftId}/handoff-accept`,
      'POST',
      {},
      'Nao foi possivel aceitar a troca de turno.',
      resetShiftForm,
    )
  }

  async function handleShiftHandoffReject(shiftId: number, reason: string) {
    // Supervisor rejeita e o turno retorna ao agente original sem interrupcao da operacao.
    await saveEntity(
      `/api/shifts/${shiftId}/handoff-reject`,
      'POST',
      { rejectionReason: reason || 'Sem motivo informado.' },
      'Nao foi possivel rejeitar a troca de turno.',
      resetShiftForm,
    )
  }

  async function handleShiftSupervision(
    shiftId: number,
    action: 'MARK_ON_TIME' | 'MARK_LATE' | 'MARK_ABSENT' | 'APPLY_COVERAGE' | 'CLEAR_COVERAGE',
  ) {
    // Explicita no painel as decisoes formais de supervisao para atraso, falta e cobertura.
    let payload: {
      action: 'MARK_ON_TIME' | 'MARK_LATE' | 'MARK_ABSENT' | 'APPLY_COVERAGE' | 'CLEAR_COVERAGE'
      replacementAgentId?: number | null
      lateMinutes?: number | null
      notes?: string | null
    } = {
      action,
      replacementAgentId: null,
      lateMinutes: null,
      notes: null,
    }

    if (action === 'MARK_LATE') {
      const lateMinutesValue = window.prompt('Informe os minutos de atraso deste turno:', '15')
      if (!lateMinutesValue) {
        return
      }

      payload = {
        action,
        lateMinutes: Number(lateMinutesValue),
        notes: `Atraso formalizado em painel: ${lateMinutesValue} minutos.`,
      }
    }

    if (action === 'MARK_ABSENT') {
      payload = {
        action,
        notes: 'Falta registrada pela supervisao no painel operacional.',
      }
    }

    if (action === 'MARK_ON_TIME') {
      payload = {
        action,
        notes: 'Presenca normalizada pela supervisao no painel operacional.',
      }
    }

    if (action === 'APPLY_COVERAGE') {
      const replacementAgentId = window.prompt('Informe o ID do vigilante que fara a cobertura:', shiftForm.coverageForAgentId || '')
      if (!replacementAgentId) {
        return
      }

      payload = {
        action,
        replacementAgentId: Number(replacementAgentId),
        notes: `Cobertura aplicada via painel para o agente ${replacementAgentId}.`,
      }
    }

    if (action === 'CLEAR_COVERAGE') {
      payload = {
        action,
        notes: 'Cobertura removida pela supervisao no painel operacional.',
      }
    }

    await saveEntity(
      `/api/shifts/${shiftId}/supervision`,
      'POST',
      payload,
      'Nao foi possivel registrar a supervisao do turno.',
      resetShiftForm,
    )
  }

  function resetPrivacyForm() {
    setPrivacyForm(initialPrivacyForm)
    setEditingPrivacyRequestId(null)
  }

  function startPrivacyRequestEdit(request: PrivacyRequest) {
    // Reabre o pedido no formulario para o administrador registrar andamento, notificacao e conclusao.
    setEditingPrivacyRequestId(request.id)
    setPrivacyForm({
      requestType: request.requestType,
      subjectType: request.subjectType,
      subjectId: String(request.subjectId),
      notes: request.notes ?? '',
      status: request.status === 'OPEN' ? 'IN_PROGRESS' : request.status,
      notifySubject: Boolean(request.subjectNotifiedAt),
      notificationChannel: request.subjectNotificationChannel ?? '',
      notificationNotes: request.subjectNotificationNotes ?? '',
    })
  }

  async function handlePrivacyRequestSubmit(event: FormEvent<HTMLFormElement>) {
    // Registra pedido LGPD (exportacao ou exclusao) sem misturar com a operacao normal.
    event.preventDefault()
    if (!privacyForm.subjectId) {
      setError('Informe o ID do titular dos dados.')
      return
    }

    const path = editingPrivacyRequestId === null ? '/api/privacy/requests' : `/api/privacy/requests/${editingPrivacyRequestId}`
    const method = editingPrivacyRequestId === null ? 'POST' : 'PUT'
    const payload = editingPrivacyRequestId === null
      ? {
          requestType: privacyForm.requestType,
          subjectType: privacyForm.subjectType,
          subjectId: Number(privacyForm.subjectId),
          notes: privacyForm.notes || null,
        }
      : {
          status: privacyForm.status,
          notes: privacyForm.notes || null,
          notifySubject: privacyForm.notifySubject,
          notificationChannel: privacyForm.notifySubject ? privacyForm.notificationChannel || null : null,
          notificationNotes: privacyForm.notifySubject ? privacyForm.notificationNotes || null : null,
        }
    await saveEntity(path, method, payload, 'Nao foi possivel registrar o pedido LGPD.', resetPrivacyForm)
  }

  async function handlePrivacyRequestComplete(requestId: number) {
    await saveEntity(
      `/api/privacy/requests/${requestId}`,
      'PUT',
      {
        status: 'COMPLETED' as PrivacyRequestStatus,
        notifySubject: privacyForm.notifySubject,
        notificationChannel: privacyForm.notifySubject ? privacyForm.notificationChannel || null : null,
        notificationNotes: privacyForm.notifySubject ? privacyForm.notificationNotes || null : null,
      },
      'Nao foi possivel concluir o pedido LGPD.',
      resetPrivacyForm,
    )
  }

  async function handlePrivacyExportDownload(subjectType: PrivacySubjectType, subjectId: number) {
    // Gera o pacote de exportacao do titular sem depender de processo manual fora do painel.
    try {
      const response = await apiFetch(`/api/privacy/exports/${subjectType}/${subjectId}/download`)
      if (!response.ok) {
        throw new Error('Nao foi possivel gerar o arquivo de exportacao.')
      }

      const blob = await response.blob()
      const downloadUrl = window.URL.createObjectURL(blob)
      const anchor = window.document.createElement('a')
      anchor.href = downloadUrl
      anchor.download = `vmab-lgpd-${subjectType.toLowerCase()}-${subjectId}.json`
      anchor.click()
      window.URL.revokeObjectURL(downloadUrl)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao gerar a exportacao LGPD.')
    }
  }

  async function handlePrivacyNotificationDraftDownload(requestId: number) {
    // Baixa um rascunho formal da comunicacao ao titular para registro e envio externo controlado.
    try {
      const response = await apiFetch(`/api/privacy/requests/${requestId}/notification-draft`)
      if (!response.ok) {
        throw new Error('Nao foi possivel gerar o rascunho de notificacao do titular.')
      }

      const blob = await response.blob()
      const downloadUrl = window.URL.createObjectURL(blob)
      const anchor = window.document.createElement('a')
      anchor.href = downloadUrl
      anchor.download = `vmab-lgpd-notificacao-${requestId}.txt`
      anchor.click()
      window.URL.revokeObjectURL(downloadUrl)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao gerar o rascunho de notificacao.')
    }
  }

  async function handleClientReportCsvExport() {
    try {
      const response = await apiFetch('/api/client/report/export.csv')
      if (!response.ok) {
        throw new Error('Nao foi possivel exportar o relatorio CSV do cliente.')
      }

      const blob = await response.blob()
      const downloadUrl = window.URL.createObjectURL(blob)
      const anchor = window.document.createElement('a')
      anchor.href = downloadUrl
      anchor.download = 'vmab-relatorio-cliente.csv'
      anchor.click()
      window.URL.revokeObjectURL(downloadUrl)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao exportar o relatorio do cliente.')
    }
  }

  function handleClientReportPrint() {
    if (!clientReport) {
      setError('O relatorio do cliente ainda nao foi carregado.')
      return
    }

    const printWindow = window.open('', '_blank', 'width=1080,height=820')
    if (!printWindow) {
      setError('Nao foi possivel abrir a janela de impressao.')
      return
    }

    const html = `
      <html lang="pt-BR">
        <head>
          <title>Relatorio operacional VMAB</title>
          <style>
            body { font-family: Arial, sans-serif; margin: 32px; color: #111827; }
            h1, h2 { margin-bottom: 8px; }
            .meta { color: #4b5563; margin-bottom: 24px; }
            .grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 12px; margin-bottom: 24px; }
            .card { border: 1px solid #d1d5db; border-radius: 12px; padding: 16px; }
            .card strong { display: block; font-size: 22px; margin-top: 8px; }
            table { width: 100%; border-collapse: collapse; margin-top: 12px; }
            th, td { border: 1px solid #d1d5db; padding: 8px; text-align: left; font-size: 12px; }
            th { background: #f3f4f6; }
          </style>
        </head>
        <body>
          <h1>Relatorio operacional VMAB</h1>
          <p class="meta">Gerado em ${formatDate(clientReport.generatedAt)}</p>
          <div class="grid">
            <div class="card"><span>Turnos ativos</span><strong>${clientReport.activeShifts}</strong></div>
            <div class="card"><span>Ocorrencias abertas</span><strong>${clientReport.openIncidents}</strong></div>
            <div class="card"><span>Viaturas disponiveis</span><strong>${clientReport.availableVehicles}</strong></div>
            <div class="card"><span>Alertas de manutencao</span><strong>${clientReport.maintenanceAlerts}</strong></div>
            <div class="card"><span>Tempo medio de despacho</span><strong>${clientReport.averageDispatchMinutes.toFixed(1)} min</strong></div>
            <div class="card"><span>Tempo medio de resolucao</span><strong>${clientReport.averageResolutionMinutes.toFixed(1)} min</strong></div>
          </div>
          <h2>Ocorrencias</h2>
          <table>
            <thead>
              <tr><th>ID</th><th>Tipo</th><th>Status</th><th>Morador</th><th>Endereco</th><th>Agente</th><th>Viatura</th></tr>
            </thead>
            <tbody>
              ${clientReport.incidents.map((incident) => `
                <tr>
                  <td>${incident.id}</td>
                  <td>${translateIncidentType(incident.type)}</td>
                  <td>${translateIncidentStatus(incident.status)}</td>
                  <td>${incident.residentName}</td>
                  <td>${incident.address}</td>
                  <td>${incident.assignedAgentName ?? 'Sem agente'}</td>
                  <td>${incident.vehiclePlate ?? 'Sem viatura'}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
          <h2>Ordens de servico da frota</h2>
          <table>
            <thead>
              <tr><th>Codigo</th><th>Viatura</th><th>Tipo</th><th>Prioridade</th><th>Status</th><th>Custo</th></tr>
            </thead>
            <tbody>
              ${clientReport.maintenanceOrders.map((order) => `
                <tr>
                  <td>${order.workOrderCode}</td>
                  <td>${order.vehiclePlate}</td>
                  <td>${translateVehicleMaintenanceType(order.type)}</td>
                  <td>${translateVehicleMaintenancePriority(order.priority)}</td>
                  <td>${translateVehicleMaintenanceStatus(order.status)}</td>
                  <td>${formatCurrency(order.costAmount)}</td>
                </tr>
              `).join('')}
            </tbody>
          </table>
        </body>
      </html>
    `

    printWindow.document.open()
    printWindow.document.write(html)
    printWindow.document.close()
    printWindow.focus()
    printWindow.print()
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

  async function handleIncidentDispatch(incidentId: number) {
    if (!incidentForm.assignedAgentId || !incidentForm.vehicleId) {
      setError('Selecione agente e viatura para despachar a ocorrencia.')
      return
    }

    await saveEntity(
      `/api/incidents/${incidentId}/dispatch`,
      'POST',
      {
        assignedAgentId: Number(incidentForm.assignedAgentId),
        vehicleId: Number(incidentForm.vehicleId),
        dispatchNotes: incidentForm.dispatchNotes || null,
      },
      'Nao foi possivel despachar a ocorrencia.',
      resetIncidentForm,
    )
  }

  async function handleIncidentOnSite(incidentId: number) {
    await saveEntity(
      `/api/incidents/${incidentId}/onsite`,
      'POST',
      {
        arrivalNotes: incidentForm.arrivalNotes || null,
      },
      'Nao foi possivel registrar chegada no local.',
      resetIncidentForm,
    )
  }

  async function handleIncidentClose(incidentId: number) {
    await saveEntity(
      `/api/incidents/${incidentId}/close`,
      'POST',
      {
        closureNotes: incidentForm.closureNotes || null,
      },
      'Nao foi possivel encerrar a ocorrencia.',
      resetIncidentForm,
    )
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
          {authMessage ? <div className="alert success">{authMessage}</div> : null}
          <form className="login-form" onSubmit={handleLogin}>
            <input required placeholder="Usuario" value={credentials.username} onChange={(event) => setCredentials((current) => ({ ...current, username: event.target.value }))} />
            <input required type="password" placeholder="Senha" value={credentials.password} onChange={(event) => setCredentials((current) => ({ ...current, password: event.target.value }))} />
            <button type="submit">Entrar</button>
          </form>
          <form className="login-form" onSubmit={handlePasswordResetRequest}>
            <strong>Solicitar reset de senha</strong>
            <input required placeholder="Usuario para reset" value={passwordResetForm.username} onChange={(event) => setPasswordResetForm((current) => ({ ...current, username: event.target.value }))} />
            <button className="secondary-button" type="submit">Gerar codigo</button>
          </form>
          <form className="login-form" onSubmit={handlePasswordResetConfirm}>
            <strong>Confirmar reset</strong>
            <input
              required
              inputMode="numeric"
              maxLength={6}
              placeholder="Codigo de reset"
              value={passwordResetForm.resetCode}
              onChange={(event) => setPasswordResetForm((current) => ({ ...current, resetCode: normalizeDigitsInput(event.target.value, 6) }))}
            />
            <input required minLength={8} type="password" placeholder="Nova senha" value={passwordResetForm.newPassword} onChange={(event) => setPasswordResetForm((current) => ({ ...current, newPassword: event.target.value }))} />
            <button className="secondary-button" type="submit">Redefinir senha</button>
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
              <button className="secondary-button" onClick={() => void handleClientReportCsvExport()} type="button">Exportar CSV</button>
              <button className="secondary-button" onClick={handleClientReportPrint} type="button">Imprimir relatorio</button>
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

          {clientReport ? (
            <section className="panel report-panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Relatorio formal</p>
                  <h3>Indicadores contratuais</h3>
                </div>
              </div>
              <p className="panel-note">Este recorte consolida os indicadores que o cliente normalmente usaria para acompanhar SLA, disponibilidade da frota e prova operacional do contrato.</p>
              <div className="subpanel-grid">
                <article className="telemetry-card">
                  <span>Tempo medio de despacho</span>
                  <strong>{clientReport.averageDispatchMinutes.toFixed(1)} min</strong>
                  <small>Gerado em {formatDate(clientReport.generatedAt)}</small>
                </article>
                <article className="telemetry-card">
                  <span>Tempo medio de resolucao</span>
                  <strong>{clientReport.averageResolutionMinutes.toFixed(1)} min</strong>
                  <small>Baseado em ocorrencias encerradas</small>
                </article>
                <article className="telemetry-card">
                  <span>Turnos atrasados</span>
                  <strong>{clientReport.lateShifts}</strong>
                  <small>Escala com atraso registrado</small>
                </article>
                <article className="telemetry-card">
                  <span>Faltas abertas</span>
                  <strong>{clientReport.absentShifts}</strong>
                  <small>Turnos marcados como falta</small>
                </article>
              </div>
              <div className="report-summary-grid">
                <article className="report-insight-card">
                  <span>Ordens abertas</span>
                  <strong>{clientReport.openMaintenanceOrders}</strong>
                  <small>Ordens de servico ainda sem encerramento formal.</small>
                </article>
                <article className="report-insight-card">
                  <span>Ordens criticas</span>
                  <strong>{clientReport.criticalMaintenanceOrders}</strong>
                  <small>Viaturas com risco alto de indisponibilidade.</small>
                </article>
                <article className="report-insight-card">
                  <span>Prova operacional</span>
                  <strong>{clientReport.incidents.length}</strong>
                  <small>Ocorrencias recentes consideradas no relatorio atual.</small>
                </article>
              </div>
            </section>
          ) : null}

          <section className="panel">
            <div className="panel-header">
              <div>
                <p className="eyebrow">Tempo real</p>
                <h3>Atualizacoes recebidas</h3>
              </div>
            </div>
            {realtimeEvents.length === 0 ? (
              <p className="panel-note">Nenhum evento recebido nesta sessao ainda.</p>
            ) : (
              <div className="list">
                {realtimeEvents.map((event, index) => (
                  <article className="list-row" key={`${event.occurredAt}-${event.entityName}-${index}`}>
                    <div>
                      <strong>{translateRealtimeEventType(event.type)} | {event.entityName}</strong>
                      <small>{event.description}</small>
                    </div>
                    <span className="tag">{formatDate(event.occurredAt)}</span>
                  </article>
                ))}
              </div>
            )}
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

          {clientReport ? (
            <section className="panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Prova de execucao</p>
                  <h3>Ocorrencias consolidadas</h3>
                </div>
              </div>
              {clientReport.incidents.length === 0 ? (
                <p className="panel-note">Nao houve ocorrencias recentes incluidas no recorte formal do cliente.</p>
              ) : (
                <div className="list">
                  {clientReport.incidents.map((incident) => (
                    <article className="list-row" key={`client-report-incident-${incident.id}`}>
                      <div>
                        <strong>#{incident.id} | {translateIncidentType(incident.type)} | {incident.residentName}</strong>
                        <small>
                          {translateIncidentStatus(incident.status)} | {incident.address} | agente {incident.assignedAgentName ?? 'nao vinculado'} | viatura {incident.vehiclePlate ?? 'nao vinculada'}
                        </small>
                        <small>
                          Aberta {formatDate(incident.openedAt)}
                          {incident.dispatchedAt ? ` • despacho ${formatDate(incident.dispatchedAt)}` : ''}
                          {incident.closedAt ? ` • encerrada ${formatDate(incident.closedAt)}` : ''}
                        </small>
                      </div>
                      <span className={`tag ${incident.priority.toLowerCase()}`}>{translateIncidentPriority(incident.priority)}</span>
                    </article>
                  ))}
                </div>
              )}
            </section>
          ) : null}

          {clientReport ? (
            <section className="panel">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Frota</p>
                  <h3>Ordens de servico recentes</h3>
                </div>
              </div>
              {clientReport.maintenanceOrders.length === 0 ? (
                <p className="panel-note">Nenhuma ordem de servico recente registrada.</p>
              ) : (
                <div className="list">
                  {clientReport.maintenanceOrders.map((order) => (
                    <article className="list-row" key={order.id}>
                      <div>
                        <strong>{order.workOrderCode} | {order.vehiclePlate}</strong>
                        <small>
                          {translateVehicleMaintenanceType(order.type)} | {translateVehicleMaintenancePriority(order.priority)} | {translateVehicleMaintenanceStatus(order.status)} | {order.description}
                        </small>
                      </div>
                      <span className={`tag ${order.priority.toLowerCase()}`}>{formatCurrency(order.costAmount)}</span>
                    </article>
                  ))}
                </div>
              )}
            </section>
          ) : null}
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

        <div className="sidebar-block">
          <span className="sidebar-label">Acoes</span>
          <div className="sidebar-action-list">
            <button className="sidebar-action-button" onClick={() => void loadData()} type="button">Atualizar</button>
            <button className="sidebar-action-button sidebar-action-button-primary" onClick={handleLogout} type="button">Sair</button>
          </div>
          {lastRefreshAt ? <small className="sidebar-refresh-note">Ultimo sync {formatDate(lastRefreshAt)}</small> : null}
        </div>

        <div className="sidebar-block">
          <span className="sidebar-label">Seções</span>
          <div className="sidebar-section-list">
            {dashboardSectionLinks.map((section) => (
              <button
                aria-pressed={activeDashboardSection === section.id}
                className={`sidebar-section-button${activeDashboardSection === section.id ? ' active' : ''}`}
                key={section.id}
                onClick={() => setActiveDashboardSection(section.id)}
                type="button"
              >
                <strong>{section.label}</strong>
                <small>{section.description}</small>
              </button>
            ))}
          </div>
        </div>
      </aside>

      <main className="workspace">
        <header className="hero">
          <div>
            <p className="eyebrow">Painel</p>
            <h2>Core administrativo integrado</h2>
            <p className="hero-copy">Esta interface concentra cadastros, jornada, ocorrencias e a base operacional do produto em um unico fluxo.</p>
            {lastRefreshAt ? <small className="hero-refresh">Atualizacao automatica ativa • ultimo sync {formatDate(lastRefreshAt)}</small> : null}
          </div>
          <div className="hero-actions">
            <button className="refresh-button" onClick={() => void loadData()} type="button">Atualizar</button>
            <button className="secondary-button" onClick={handleLogout} type="button">Sair</button>
          </div>
        </header>

        {error ? <div className="alert error">{error}</div> : null}
        {loading ? <div className="alert">Carregando painel...</div> : null}
        {isPending ? <div className="alert">Sincronizando alteracoes...</div> : null}
        {realtimeEvents[0] ? <div className="alert success">Tempo real ativo: {realtimeEvents[0].description}</div> : null}

        {summary ? (
          <div className="dashboard-layout">
            <div className="dashboard-content dashboard-content-centered">
            {activeDashboardSection === 'sec-tempo-real' ? (
            <section className="panel dashboard-section-shell" id="sec-tempo-real">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Tempo real</p>
                  <h3>Ultimos eventos recebidos</h3>
                </div>
              </div>
              {realtimeEvents.length === 0 ? (
                <p className="panel-note">Nenhum evento operacional recebido nesta sessao ainda.</p>
              ) : (
                <div className="list">
                  {realtimeEvents.map((event, index) => (
                    <article className="list-row" key={`${event.occurredAt}-${event.entityName}-${index}`}>
                      <div>
                        <strong>{translateRealtimeEventType(event.type)} | {event.entityName}</strong>
                        <small>{event.description}</small>
                      </div>
                      <span className="tag">{formatDate(event.occurredAt)}</span>
                    </article>
                  ))}
                </div>
              )}
            </section>
            ) : null}

            {activeDashboardSection === 'sec-patrulha' && summary.activePatrol ? (
              // Bloco principal de acompanhamento da patrulha ativa em tempo real.
              <section className="panel patrol-panel dashboard-section-shell" id="sec-patrulha">
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

            {activeDashboardSection === 'sec-indicadores' ? (
            <section className="stats-grid dashboard-section-shell" id="sec-indicadores">
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
            ) : null}

            {activeDashboardSection === 'sec-rh' ? (
            <section className="panel dashboard-section-shell" id="sec-rh">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">RH</p>
                  <h3>Cadastro mestre e ponto operacional</h3>
                </div>
              </div>
              <p className="panel-note">O RH centraliza vigilantes e demais funcionarios, com cadastro, acessos, turnos, ponto operacional e alertas de validade.</p>
              <div className="dashboard-hub-grid">
                <button className="hub-card" onClick={() => setActiveDashboardSection('sec-cadastro')} type="button">
                  <strong>Funcionarios / Vigilantes</strong>
                  <small>Cadastro mestre, documentos e vinculos</small>
                </button>
                <button className="hub-card" onClick={() => setActiveDashboardSection('sec-acesso')} type="button">
                  <strong>Acessos</strong>
                  <small>Usuarios, perfis e permissões</small>
                </button>
                <button className="hub-card" onClick={() => setActiveDashboardSection('sec-operacao')} type="button">
                  <strong>Turnos e ponto</strong>
                  <small>Jornada, cobertura e ocorrências</small>
                </button>
                <button className="hub-card" onClick={() => setActiveDashboardSection('sec-rh')} type="button">
                  <strong>Alertas</strong>
                  <small>CNH, exames e treinamentos</small>
                </button>
              </div>
              <div className="subpanel-grid">
                <article className="telemetry-card">
                  <span>Total de funcionarios</span>
                  <strong>{summary.hr.totalEmployees}</strong>
                  <small>{summary.hr.activeEmployees} ativos | {summary.hr.blockedEmployees} bloqueados/desligados</small>
                </article>
                <article className="telemetry-card">
                  <span>Ponto ativo agora</span>
                  <strong>{summary.hr.checkedInNowEmployees}</strong>
                  <small>{summary.hr.expiringSoonAlerts} alertas de renovacao nas proximas 4 semanas</small>
                </article>
                <article className="telemetry-card">
                  <span>CNH / exames</span>
                  <strong>{summary.hr.alerts.filter((alert) => alert.alertType === 'CNH').length}</strong>
                  <small>{summary.hr.alerts.filter((alert) => alert.alertType === 'EXAME_MEDICO').length} exames | {summary.hr.alerts.filter((alert) => alert.alertType === 'TREINAMENTO').length} treinamentos</small>
                </article>
                <article className="telemetry-card">
                  <span>Registros recentes</span>
                  <strong>{summary.hr.attendance.length}</strong>
                  <small>Ultimas entradas e saidas consolidadas no proprio backend</small>
                </article>
              </div>

              <div className="subpanel-grid hr-summary-grid" style={{ marginTop: 16 }}>
                <div>
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">Funcionarios</p>
                      <h3>Quadro atual</h3>
                    </div>
                  </div>
                  <div className="list">
                    {summary.hr.employees.slice(0, 8).map((employee) => (
                      <article className="list-row" key={employee.id}>
                        <div>
                          <strong>{employee.fullName}</strong>
                          <small>{employee.employeeCode} | {translateHrEmployeeCategory(employee.category)} | CNH {employee.cnhCategory ?? 'nao informada'} {employee.cnhExpiry ? `ate ${formatOptionalDate(employee.cnhExpiry)}` : ''}</small>
                          <small>{employee.linkedAgentName ? `Vinculado ao vigilante ${employee.linkedAgentName}` : 'Sem vinculo operacional'}</small>
                        </div>
                        <div className="row-actions">
                          <span className={`tag ${hrStatusTagClass(employee.status)}`}>{translateHrEmployeeStatus(employee.status)}</span>
                          <span className={`tag ${employee.pointEnabled ? 'active' : 'blocked'}`}>{employee.pointEnabled ? 'Ponto liberado' : 'Ponto bloqueado'}</span>
                        </div>
                      </article>
                    ))}
                  </div>
                </div>

                <div>
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">Alertas</p>
                      <h3>Renovacoes e pendencias</h3>
                    </div>
                  </div>
                  <div className="list">
                    {summary.hr.alerts.length === 0 ? (
                      <article className="list-row">
                        <div>
                          <strong>Sem alertas no momento</strong>
                          <small>Nenhuma CNH, exame ou treinamento entrou na janela de renovacao.</small>
                        </div>
                      </article>
                    ) : (
                      summary.hr.alerts.slice(0, 8).map((alert) => (
                        <article className="list-row" key={`${alert.employeeId}-${alert.alertType}`}>
                          <div>
                            <strong>{alert.employeeName}</strong>
                            <small>{alert.alertType} | vence em {alert.daysRemaining} dia(s)</small>
                            <small>{alert.message}</small>
                          </div>
                          <span className={`tag ${alert.daysRemaining < 0 ? 'blocked' : 'maintenance'}`}>{formatOptionalDate(alert.dueDate)}</span>
                        </article>
                      ))
                    )}
                  </div>
                </div>

                <div>
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">Ponto</p>
                      <h3>Registros recentes</h3>
                    </div>
                  </div>
                  <div className="list">
                    {summary.hr.attendance.length === 0 ? (
                      <article className="list-row">
                        <div>
                          <strong>Sem registros de ponto</strong>
                          <small>O modulo de ponto operacional ainda nao recebeu eventos nesta sessao.</small>
                        </div>
                      </article>
                    ) : (
                      summary.hr.attendance.slice(0, 8).map((attendance) => (
                        <article className="list-row" key={attendance.id}>
                          <div>
                            <strong>{attendance.employeeName ?? `Funcionario #${attendance.employeeId}`}</strong>
                            <small>{translateHrAttendanceType(attendance.eventType)} | {attendance.deviceLabel ?? 'dispositivo nao informado'} | {formatDate(attendance.occurredAt)}</small>
                            <small>{attendance.anomalyFlag ? `Anomalia: ${attendance.anomalyReason ?? 'sem detalhamento'}` : 'Sem anomalia registrada'}</small>
                          </div>
                          <span className={`tag ${attendance.anomalyFlag ? 'blocked' : 'active'}`}>{attendance.anomalyFlag ? 'Revisar' : 'Normal'}</span>
                        </article>
                      ))
                    )}
                  </div>
                </div>
              </div>
            </section>
            ) : null}

            {activeDashboardSection === 'sec-frota' && fleetReport ? (
              <section className="panel dashboard-section-shell" id="sec-frota">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Frota</p>
                    <h3>Saude operacional da frota</h3>
                  </div>
                </div>
                <p className="panel-note">Snapshot calculado pelo backend a partir das viaturas, checklists e ordens de servico em aberto.</p>
                <div className="subpanel-grid">
                  <article className="telemetry-card">
                    <span>Operacionais</span>
                    <strong>{fleetReport.operationalVehicles}/{fleetReport.totalVehicles}</strong>
                    <small>{fleetReport.availableVehicles} disponíveis • {fleetReport.inOperationVehicles} em rota</small>
                  </article>
                  <article className="telemetry-card">
                    <span>Manutencao / Bloqueadas</span>
                    <strong>{fleetReport.maintenanceVehicles + fleetReport.blockedVehicles}</strong>
                    <small>{fleetReport.maintenanceDueSoonVehicles} proximas • {fleetReport.maintenanceOverdueVehicles} vencidas</small>
                  </article>
                  <article className="telemetry-card">
                    <span>OS abertas</span>
                    <strong>{fleetReport.maintenanceOrdersOpen}</strong>
                    <small>Preventiva {fleetReport.preventiveOrdersOpen} • Corretiva {fleetReport.correctiveOrdersOpen} • Inspecao {fleetReport.inspectionOrdersOpen}</small>
                  </article>
                  <article className="telemetry-card">
                    <span>Custo 30 dias</span>
                    <strong>R$ {fleetReport.totalMaintenanceCostLast30Days.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</strong>
                    <small>Total acumulado R$ {fleetReport.totalMaintenanceCostAllTime.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</small>
                  </article>
                </div>
                {fleetReport.latestOrders.length > 0 ? (
                  <>
                    <h4 style={{ margin: '16px 0 8px' }}>Ultimas ordens de servico</h4>
                    <div className="list">
                      {fleetReport.latestOrders.slice(0, 5).map((order) => (
                        <article className="list-row" key={order.id}>
                          <div>
                            <strong>{order.workOrderCode} | {order.vehiclePlate} - {order.vehicleModel}</strong>
                            <small>{translateVehicleMaintenanceType(order.type)} • {translateVehicleMaintenancePriority(order.priority)} • {translateVehicleMaintenanceStatus(order.status)} • {order.description}</small>
                            <small>{order.lifecycleLabel}{order.daysUntilDue != null ? ` • vence em ${order.daysUntilDue}d` : ''}{order.blockingVehicle ? ' • BLOQUEIA VIATURA' : ''}</small>
                          </div>
                          <span className={`tag ${order.priority.toLowerCase()}`}>{translateVehicleMaintenancePriority(order.priority)}</span>
                        </article>
                      ))}
                    </div>
                  </>
                ) : null}
              </section>
            ) : null}

            {activeDashboardSection === 'sec-auditoria' ? (
            <section className="panel dashboard-section-shell" id="sec-auditoria">
              <div className="panel-header">
                <div>
                  <p className="eyebrow">Auditoria</p>
                  <h3>Relatorio de eventos criticos</h3>
                </div>
              </div>
              <p className="panel-note">Relatorio operacional por periodo. Autenticacoes ficam ocultas por padrao e podem ser incluídas quando necessario.</p>
              <div className="audit-report-controls">
                <div className="audit-period-row" role="tablist" aria-label="Periodo do relatorio">
                  {[7, 30, 90].map((days) => {
                    const isActive = auditPeriodDays === days

                    return (
                      <button
                        key={days}
                        aria-pressed={isActive}
                        className={`audit-period-chip ${isActive ? 'active' : ''}`}
                        onClick={() => setAuditPeriodDays(days)}
                        type="button"
                      >
                        Ultimos {days} dias
                      </button>
                    )
                  })}
                </div>
                <label className="checkbox-field audit-auth-toggle">
                  <input checked={auditIncludeAuth} onChange={(event) => setAuditIncludeAuth(event.target.checked)} type="checkbox" />
                  <span>Incluir autenticacoes</span>
                </label>
              </div>
              <div className="audit-filter-row" role="tablist" aria-label="Filtros de auditoria">
                {(['ALL', 'GESTAO', 'OPERACIONAL', 'FROTA', 'RH', 'SEGURANCA'] as AuditReportCategory[]).map((category) => {
                  const isActive = auditFilter === category
                  const categoryCount = category === 'ALL'
                    ? (auditReport?.totalRecords ?? 0)
                    : auditCategoryCounts[category as Exclude<AuditReportCategory, 'ALL'>]

                  return (
                    <button
                      key={category}
                      aria-pressed={isActive}
                      className={`audit-filter-chip ${isActive ? 'active' : ''}`}
                      onClick={() => setAuditFilter(category)}
                      type="button"
                    >
                      <span>{translateAuditCategory(category)}</span>
                      <strong>{categoryCount}</strong>
                    </button>
                  )
                })}
              </div>
              {auditReportLoading ? <p className="panel-note">Carregando relatorio de auditoria.</p> : null}
              {!auditReportLoading && auditReportError ? <p className="panel-note audit-error">{auditReportError}</p> : null}
              {!auditReportLoading && !auditReportError && auditReport ? (
                <>
                  <div className="subpanel-grid audit-report-grid">
                    <article className="telemetry-card">
                      <span>Periodo</span>
                      <strong>{auditReport.days} dias</strong>
                      <small>Gerado em {formatDate(auditReport.generatedAt)}</small>
                    </article>
                    <article className="telemetry-card">
                      <span>Registros do periodo</span>
                      <strong>{auditReport.totalRecords}</strong>
                      <small>{auditReport.includeAuth ? 'Autenticacoes incluídas' : 'Autenticacoes ocultas'}</small>
                    </article>
                    <article className="telemetry-card">
                      <span>Registros visiveis</span>
                      <strong>{auditReport.visibleRecords}</strong>
                      <small>Filtro atual: {translateAuditCategory(auditReport.category)}</small>
                    </article>
                    <article className="telemetry-card">
                      <span>Seguranca</span>
                      <strong>{auditCategoryCounts.SEGURANCA}</strong>
                      <small>Alertas e autenticacoes sob demanda</small>
                    </article>
                  </div>
                  <div className="subpanel-grid audit-breakdown-grid">
                    <article className="mini-panel audit-breakdown-panel">
                      <span>Principais acoes</span>
                      <div className="audit-breakdown-list">
                        {auditTopActions.length > 0 ? auditTopActions.map(([actionType, total]) => (
                          <div key={actionType} className="audit-breakdown-row">
                            <strong>{translateAuditActionType(actionType as AuditActionType)}</strong>
                            <span>{total}</span>
                          </div>
                        )) : <small>Nenhuma acao consolidada no periodo.</small>}
                      </div>
                    </article>
                    <article className="mini-panel audit-breakdown-panel">
                      <span>Principais responsaveis</span>
                      <div className="audit-breakdown-list">
                        {auditTopActors.length > 0 ? auditTopActors.map(([actor, total]) => (
                          <div key={actor} className="audit-breakdown-row">
                            <strong>{actor}</strong>
                            <span>{total}</span>
                          </div>
                        )) : <small>Nenhum responsavel consolidado no periodo.</small>}
                      </div>
                    </article>
                  </div>
                  <div className="list">
                    {auditRecords.length === 0 ? (
                      <p className="panel-note">Nenhum evento encontrado para o filtro selecionado.</p>
                    ) : null}
                    {auditRecords.map((record) => (
                      <article className="list-row" key={record.id}>
                        <div>
                          <strong>{translateAuditActionType(record.actionType)} | {record.entityName}{record.entityId != null ? ` #${record.entityId}` : ''}</strong>
                          <small>{record.description}</small>
                          <small>{translateAuditCategory(auditCategoryForRecord(record))}</small>
                        </div>
                        <div className="row-actions">
                          <span className="tag active">{record.actorUsername}</span>
                          <small>{formatDate(record.occurredAt)}</small>
                        </div>
                      </article>
                    ))}
                  </div>
                </>
              ) : !auditReportLoading && !auditReportError ? (
                <p className="panel-note">Carregando relatorio de auditoria.</p>
              ) : null}
            </section>
            ) : null}

            {activeDashboardSection === 'sec-acesso' || activeDashboardSection === 'sec-cadastro' || activeDashboardSection === 'sec-operacao' || activeDashboardSection === 'sec-evidencias' ? (
            <section className="content-grid dashboard-section-shell" id="sec-gestao">
              {/* Area transacional do painel com cadastros e operacao diaria. */}
              {activeDashboardSection === 'sec-acesso' && canManageUsers ? (
                <section className="panel">
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">Acesso</p>
                      <h3>{editingUserId === null ? 'Usuarios e perfis' : 'Editar usuario'}</h3>
                    </div>
                  </div>
                  <p className="panel-note">Apenas administradores podem criar, editar, ativar ou remover acessos do sistema.</p>
                  <div className="button-row">
                    <button className="secondary-button" onClick={() => setActiveDashboardSection('sec-rh')} type="button">Voltar ao RH</button>
                  </div>
                  <form className="form-grid" onSubmit={handleUserSubmit}>
                    <input required placeholder="Nome de usuario" value={userForm.username} onChange={(event) => setUserForm((current) => ({ ...current, username: event.target.value }))} />
                    <input required={editingUserId === null} type="password" placeholder={editingUserId === null ? 'Senha inicial' : 'Nova senha (opcional)'} value={userForm.password} onChange={(event) => setUserForm((current) => ({ ...current, password: event.target.value }))} />
                    <select value={userForm.role} onChange={(event) => setUserForm((current) => ({ ...current, role: event.target.value as AppUserRole }))}>
                      {appUserRoleOptions.map((role) => <option key={role} value={role}>{translateRole(role)}</option>)}
                    </select>
                    <select value={userForm.linkedAgentId} onChange={(event) => setUserForm((current) => ({ ...current, linkedAgentId: event.target.value }))}>
                      <option value="">Sem vinculo com vigilante</option>
                      {(summary?.agents ?? []).map((agent) => <option key={agent.id} value={agent.id}>{agent.fullName}</option>)}
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
                          <small>{translateRole(user.role)} | criado em {formatDate(user.createdAt)}{user.linkedAgentName ? ` | vinculado a ${user.linkedAgentName}` : ''}</small>
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

              {activeDashboardSection === 'sec-cadastro' ? (
              <section className="panel">
                <div className="panel-header">
                  <div>
                    <p className="eyebrow">Cadastro</p>
                    <h3>{editingAgentId === null ? 'Equipe de Ronda' : 'Editar agente'}</h3>
                  </div>
                </div>
                <p className="panel-note">{canManageCatalog ? 'Cadastro completo liberado para admin e supervisor.' : 'Seu perfil possui apenas leitura neste bloco.'}</p>
                <div className="button-row">
                  <button className="secondary-button" onClick={() => setActiveDashboardSection('sec-rh')} type="button">Voltar ao RH</button>
                </div>
                {canManageCatalog ? (
                  <form className="form-grid" onSubmit={handleAgentSubmit}>
                    <input required placeholder="Nome completo" value={agentForm.fullName} onChange={(event) => setAgentForm((current) => ({ ...current, fullName: event.target.value }))} />
                    <input required placeholder="Codigo / cracha" value={agentForm.badgeCode} onChange={(event) => setAgentForm((current) => ({ ...current, badgeCode: normalizeUppercaseInput(event.target.value) }))} />
                    <input required placeholder="Categoria CNH" value={agentForm.cnhCategory} onChange={(event) => setAgentForm((current) => ({ ...current, cnhCategory: normalizeUppercaseInput(event.target.value) }))} />
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
              ) : null}

              {activeDashboardSection === 'sec-cadastro' ? (
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
                    <input required inputMode="tel" placeholder="Telefone" value={residentForm.phoneNumber} onChange={(event) => setResidentForm((current) => ({ ...current, phoneNumber: formatBrazilPhoneInput(event.target.value) }))} />
                    <input required placeholder="Endereco" value={residentForm.address} onChange={(event) => setResidentForm((current) => ({ ...current, address: event.target.value }))} />
                    <input placeholder="Observacao / referencia" value={residentForm.referenceNote} onChange={(event) => setResidentForm((current) => ({ ...current, referenceNote: event.target.value }))} />
                    <input placeholder="PIN de acesso (4 a 6 digitos)" inputMode="numeric" maxLength={6} value={residentForm.accessPin} onChange={(event) => setResidentForm((current) => ({ ...current, accessPin: normalizeDigitsInput(event.target.value, 6) }))} />
                    <input placeholder="PIN de coacao (4 a 6 digitos)" inputMode="numeric" maxLength={6} value={residentForm.coercionPin} onChange={(event) => setResidentForm((current) => ({ ...current, coercionPin: normalizeDigitsInput(event.target.value, 6) }))} />
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
                          <small>{resident.phoneNumber} | {resident.address} | acesso {resident.accessPinConfigured ? 'configurado' : 'pendente'} | coacao {resident.coercionPinConfigured ? 'configurado' : 'pendente'}</small>
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
              ) : null}

              {activeDashboardSection === 'sec-cadastro' ? (
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
                    <input required placeholder="Placa" value={vehicleForm.plate} onChange={(event) => setVehicleForm((current) => ({ ...current, plate: normalizeUppercaseInput(event.target.value) }))} />
                    <input required placeholder="Modelo" value={vehicleForm.model} onChange={(event) => setVehicleForm((current) => ({ ...current, model: event.target.value }))} />
                    <input required min="0" inputMode="numeric" type="number" placeholder="KM atual" value={vehicleForm.currentKm} onChange={(event) => setVehicleForm((current) => ({ ...current, currentKm: event.target.value }))} />
                    <input required min="1" inputMode="numeric" type="number" placeholder="Proxima manutencao" value={vehicleForm.nextMaintenanceKm} onChange={(event) => setVehicleForm((current) => ({ ...current, nextMaintenanceKm: event.target.value }))} />
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
                    <input min="0" inputMode="numeric" type="number" placeholder="KM da manutencao" value={maintenanceForm.kmAtService} onChange={(event) => setMaintenanceForm((current) => ({ ...current, kmAtService: event.target.value }))} />
                    <input min="0" inputMode="numeric" type="number" placeholder="Proxima revisao (km)" value={maintenanceForm.nextMaintenanceKm} onChange={(event) => setMaintenanceForm((current) => ({ ...current, nextMaintenanceKm: event.target.value }))} />
                    <input min="0" step="0.01" inputMode="decimal" type="number" placeholder="Custo (R$)" value={maintenanceForm.costAmount} onChange={(event) => setMaintenanceForm((current) => ({ ...current, costAmount: event.target.value }))} />
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
              ) : null}

              {activeDashboardSection === 'sec-operacao' ? (
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
                    <input min="0" inputMode="numeric" type="number" placeholder="KM final ao encerrar" value={shiftForm.endKm} onChange={(event) => setShiftForm((current) => ({ ...current, endKm: event.target.value }))} />
                    <input min="0" max="100" inputMode="numeric" type="number" placeholder="Combustivel (%)" value={shiftForm.fuelLevelPercent} onChange={(event) => setShiftForm((current) => ({ ...current, fuelLevelPercent: event.target.value }))} />
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
                      {editingShiftId !== null && canRequestHandoffForShiftAgent(Number(shiftForm.agentId || 0)) ? <button className="secondary-button" onClick={() => void handleShiftHandoff(editingShiftId)} type="button">Registrar troca</button> : null}
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
                        <span className={`tag ${shift.status.toLowerCase().replace('_', '-')}`}>{translateShiftStatus(shift.status)}</span>
                        {shift.status === 'HANDOFF_PENDING' && canRespondHandoffForShift(shift) ? (
                          <>
                            <button className="ghost-button" onClick={() => void handleShiftHandoffAccept(shift.id)} type="button">Aceitar troca</button>
                            <button className="ghost-button danger-button" onClick={() => void handleShiftHandoffReject(shift.id, 'Rejeitado pelo vigilante designado.')} type="button">Rejeitar</button>
                          </>
                        ) : null}
                        {canCreateOperations ? (
                          <>
                            {(shift.status === 'PLANNED' || shift.status === 'ACTIVE' || shift.status === 'HANDOFF') ? (
                              <button className="ghost-button" onClick={() => void handleShiftSupervision(shift.id, 'MARK_ON_TIME')} type="button">Normalizar</button>
                            ) : null}
                            {shift.status !== 'CLOSED' ? (
                              <button className="ghost-button" onClick={() => void handleShiftSupervision(shift.id, 'MARK_LATE')} type="button">Registrar atraso</button>
                            ) : null}
                            {shift.status !== 'CLOSED' ? (
                              <button className="ghost-button danger-button" onClick={() => void handleShiftSupervision(shift.id, 'MARK_ABSENT')} type="button">Registrar falta</button>
                            ) : null}
                            {(shift.attendanceStatus === 'ABSENT' || shift.attendanceStatus === 'LATE') ? (
                              <button className="ghost-button" onClick={() => void handleShiftSupervision(shift.id, 'APPLY_COVERAGE')} type="button">Aplicar cobertura</button>
                            ) : null}
                            {shift.attendanceStatus === 'COVERED' ? (
                              <button className="ghost-button" onClick={() => void handleShiftSupervision(shift.id, 'CLEAR_COVERAGE')} type="button">Remover cobertura</button>
                            ) : null}
                          </>
                        ) : null}
                        {canUpdateOperations ? <button className="ghost-button" onClick={() => startShiftEdit(shift)} type="button">Editar</button> : null}
                        {canCreateOperations ? <button className="ghost-button danger-button" onClick={() => void handleDelete(`/api/shifts/${shift.id}`, 'Deseja remover este turno?', resetShiftForm)} type="button">Excluir</button> : null}
                      </div>
                    </article>
                  ))}
                </div>
              </section>
              ) : null}

              {activeDashboardSection === 'sec-operacao' ? (
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
                <div className="button-row">
                  <button className="secondary-button" onClick={() => setActiveDashboardSection('sec-rh')} type="button">Voltar ao RH</button>
                </div>
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
                    <input placeholder="Observacoes do despacho" value={incidentForm.dispatchNotes} onChange={(event) => setIncidentForm((current) => ({ ...current, dispatchNotes: event.target.value }))} />
                    <input placeholder="Observacoes da chegada" value={incidentForm.arrivalNotes} onChange={(event) => setIncidentForm((current) => ({ ...current, arrivalNotes: event.target.value }))} />
                    <input placeholder="Observacoes do encerramento" value={incidentForm.closureNotes} onChange={(event) => setIncidentForm((current) => ({ ...current, closureNotes: event.target.value }))} />
                    <div className="button-row">
                      <button disabled={incidentSubmitDisabled} type="submit">{editingIncidentId === null ? 'Cadastrar ocorrencia' : 'Salvar ocorrencia'}</button>
                      {editingIncidentId !== null && canUpdateOperations ? <button className="secondary-button" onClick={() => void handleIncidentDispatch(editingIncidentId)} type="button">Despachar</button> : null}
                      {editingIncidentId !== null && canUpdateOperations ? <button className="secondary-button" onClick={() => void handleIncidentOnSite(editingIncidentId)} type="button">Chegada no local</button> : null}
                      {editingIncidentId !== null && canUpdateOperations ? <button className="secondary-button" onClick={() => void handleIncidentClose(editingIncidentId)} type="button">Encerrar</button> : null}
                      {editingIncidentId !== null ? <button className="secondary-button" onClick={resetIncidentForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                ) : null}
                <div className="list">
                  {summary.incidents.map((incident) => (
                    <article className="list-row" key={incident.id}>
                      <div>
                        <strong>{translateIncidentType(incident.type)} | {incident.residentName}</strong>
                        <small>{incident.address} | {incident.assignedAgentName ?? 'Sem agente'} | {incident.vehiclePlate ?? 'Sem viatura'} | despacho {incident.dispatchedAt ? formatDate(incident.dispatchedAt) : 'pendente'} | chegada {incident.onSiteAt ? formatDate(incident.onSiteAt) : 'pendente'} | encerramento {incident.closedAt ? formatDate(incident.closedAt) : 'pendente'}</small>
                        <small>{incident.dispatchNotes ?? incident.arrivalNotes ?? incident.closureNotes ?? 'Sem observacoes operacionais'}</small>
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
              ) : null}

              {activeDashboardSection === 'sec-evidencias' && incidentEvidences.length > 0 ? (
                <section className="panel">
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">Evidencias</p>
                      <h3>Anexos de ocorrencias</h3>
                    </div>
                  </div>
                  <p className="panel-note">Arquivos enviados pela ronda mobile para comprovar o atendimento. Cada evidencia expira conforme a politica de retencao configurada.</p>
                  <div className="list">
                    {incidentEvidences.map((evidence) => (
                      <article className="list-row" key={evidence.id}>
                        <div>
                          <strong>{evidence.originalFilename} | Ocorrencia #{evidence.incidentId} — {evidence.incidentResidentName}</strong>
                          <small>{(evidence.fileSizeBytes / 1024).toFixed(1)} KB • enviado por {evidence.uploadedBy} em {formatDate(evidence.uploadedAt)} • expira {formatDate(evidence.retentionExpiresAt)}</small>
                          {evidence.notes ? <small>{evidence.notes}</small> : null}
                        </div>
                        <div className="row-actions">
                          <a className="ghost-button" href={`${API_BASE_URL}${evidence.downloadPath}`} rel="noreferrer" target="_blank">Download</a>
                        </div>
                      </article>
                    ))}
                  </div>
                </section>
              ) : null}

              {activeDashboardSection === 'sec-evidencias' && canManagePrivacy ? (
                <section className="panel">
                  <div className="panel-header">
                    <div>
                      <p className="eyebrow">LGPD</p>
                      <h3>Pedidos de privacidade e retencao de dados</h3>
                    </div>
                  </div>
                  <p className="panel-note">Consolida pedidos LGPD de exportacao e exclusao sem misturar com a operacao normal da plataforma. Apenas administradores podem gerenciar esses fluxos.</p>
                  <div className="compliance-callout">
                    <strong>Limite objetivo</strong>
                    <span>O sistema ja cobre abertura, exportacao, anonimização e registro de notificacao. O que nao existe aqui e revisao juridica externa. Marcar isso como concluido sem essa etapa seria mentira.</span>
                  </div>
                  {retentionStatus ? (
                    <div className="subpanel-grid" style={{ marginBottom: 16 }}>
                      <article className="telemetry-card">
                        <span>Limpeza automatica</span>
                        <strong>{retentionStatus.enabled ? 'Ativa' : 'Inativa'}</strong>
                        <small>Cron: {retentionStatus.cleanupCron}</small>
                      </article>
                      <article className="telemetry-card">
                        <span>Pedidos abertos</span>
                        <strong>{retentionStatus.openRequests}</strong>
                        <small>Em andamento: {retentionStatus.inProgressRequests} • Concluidos: {retentionStatus.completedRequests}</small>
                      </article>
                      <article className="telemetry-card">
                        <span>Retencao de evidencias</span>
                        <strong>{retentionStatus.incidentEvidenceRetentionDays}d</strong>
                        <small>Sessoes moradores: {retentionStatus.residentSessionRetentionDays}d</small>
                      </article>
                      {retentionStatus.lastCleanupAt ? (
                        <article className="telemetry-card">
                          <span>Ultima limpeza</span>
                          <strong>{formatDate(retentionStatus.lastCleanupAt)}</strong>
                          <small>{retentionStatus.lastCleanupDescription ?? ''}</small>
                        </article>
                      ) : null}
                    </div>
                  ) : null}
                  <form className="form-grid" onSubmit={handlePrivacyRequestSubmit}>
                    <select disabled={editingPrivacyRequestId !== null} value={privacyForm.requestType} onChange={(event) => setPrivacyForm((current) => ({ ...current, requestType: event.target.value as PrivacyRequestType }))}>
                      {privacyRequestTypeOptions.map((type) => <option key={type} value={type}>{translatePrivacyRequestType(type)}</option>)}
                    </select>
                    <select disabled={editingPrivacyRequestId !== null} value={privacyForm.subjectType} onChange={(event) => setPrivacyForm((current) => ({ ...current, subjectType: event.target.value as PrivacySubjectType }))}>
                      {privacySubjectTypeOptions.map((type) => <option key={type} value={type}>{translatePrivacySubjectType(type)}</option>)}
                    </select>
                    <input disabled={editingPrivacyRequestId !== null} required placeholder="ID do titular" inputMode="numeric" type="number" value={privacyForm.subjectId} onChange={(event) => setPrivacyForm((current) => ({ ...current, subjectId: normalizeDigitsInput(event.target.value) }))} />
                    <input placeholder="Observacoes (opcional)" value={privacyForm.notes} onChange={(event) => setPrivacyForm((current) => ({ ...current, notes: event.target.value }))} />
                    {editingPrivacyRequestId !== null ? (
                      <>
                        <select value={privacyForm.status} onChange={(event) => setPrivacyForm((current) => ({ ...current, status: event.target.value as PrivacyRequestStatus }))}>
                          {(['IN_PROGRESS', 'COMPLETED', 'REJECTED'] as PrivacyRequestStatus[]).map((status) => (
                            <option key={status} value={status}>{translatePrivacyRequestStatus(status)}</option>
                          ))}
                        </select>
                        <input placeholder="Canal de notificacao do titular" value={privacyForm.notificationChannel} onChange={(event) => setPrivacyForm((current) => ({ ...current, notificationChannel: event.target.value }))} />
                        <input placeholder="Observacao da notificacao" value={privacyForm.notificationNotes} onChange={(event) => setPrivacyForm((current) => ({ ...current, notificationNotes: event.target.value }))} />
                        <label className="checkbox-field">
                          <input checked={privacyForm.notifySubject} onChange={(event) => setPrivacyForm((current) => ({ ...current, notifySubject: event.target.checked }))} type="checkbox" />
                          <span>Registrar notificacao formal ao titular neste passo</span>
                        </label>
                      </>
                    ) : null}
                    <div className="button-row">
                      <button type="submit">{editingPrivacyRequestId === null ? 'Abrir pedido LGPD' : 'Atualizar fluxo LGPD'}</button>
                      {editingPrivacyRequestId !== null ? <button className="secondary-button" onClick={resetPrivacyForm} type="button">Cancelar</button> : null}
                    </div>
                  </form>
                  {privacyRequests.length > 0 ? (
                    <div className="list" style={{ marginTop: 16 }}>
                      {privacyRequests.map((request) => (
                        <article className="list-row" key={request.id}>
                          <div>
                            <strong>{translatePrivacyRequestType(request.requestType)} | {translatePrivacySubjectType(request.subjectType)} #{request.subjectId} — {request.subjectLabel}</strong>
                            <small>Solicitado por {request.requestedBy} em {formatDate(request.requestedAt)}{request.handledAt ? ` • atendido por ${request.handledBy ?? '?'} em ${formatDate(request.handledAt)}` : ''}</small>
                            {request.notes ? <small>{request.notes}</small> : null}
                            {request.subjectNotifiedAt ? <small>Titular notificado em {formatDate(request.subjectNotifiedAt)} via {request.subjectNotificationChannel ?? 'canal nao informado'}.</small> : null}
                            {request.subjectNotificationNotes ? <small>{request.subjectNotificationNotes}</small> : null}
                            {request.exportGeneratedAt ? <small>Exportacao gerada em {formatDate(request.exportGeneratedAt)}.</small> : null}
                            {request.deletionAppliedAt ? <small>Anonimizacao aplicada em {formatDate(request.deletionAppliedAt)}.</small> : null}
                          </div>
                          <div className="row-actions">
                            <span className={`tag ${request.status.toLowerCase().replace('_', '-')}`}>{translatePrivacyRequestStatus(request.status)}</span>
                            {request.requestType === 'EXPORT' ? (
                              <button className="ghost-button" onClick={() => void handlePrivacyExportDownload(request.subjectType, request.subjectId)} type="button">Baixar JSON</button>
                            ) : null}
                            <button className="ghost-button" onClick={() => void handlePrivacyNotificationDraftDownload(request.id)} type="button">Baixar notificacao</button>
                            {request.status === 'OPEN' || request.status === 'IN_PROGRESS' ? (
                              <>
                                <button className="ghost-button" onClick={() => startPrivacyRequestEdit(request)} type="button">Editar fluxo</button>
                                <button className="ghost-button" onClick={() => void handlePrivacyRequestComplete(request.id)} type="button">Concluir</button>
                              </>
                            ) : null}
                          </div>
                        </article>
                      ))}
                    </div>
                  ) : <p className="panel-note">Nenhum pedido LGPD registrado.</p>}
                </section>
              ) : null}
            </section>
            ) : null}
            </div>
          </div>
        ) : null}
      </main>
    </div>
  )
}

export default App
