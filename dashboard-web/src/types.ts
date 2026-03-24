export type AgentStatus = 'ACTIVE' | 'ON_DUTY' | 'OFF_DUTY' | 'BLOCKED'
// Tipos compartilhados pelo frontend para alinhar o contrato com a API.
export type ResidentStatus = 'ACTIVE' | 'INACTIVE'
export type VehicleStatus = 'AVAILABLE' | 'IN_OPERATION' | 'MAINTENANCE' | 'BLOCKED'
export type ShiftStatus = 'PLANNED' | 'ACTIVE' | 'HANDOFF' | 'CLOSED'
export type ShiftAttendanceStatus = 'PENDING' | 'ON_TIME' | 'LATE' | 'ABSENT' | 'COVERED'
export type VehicleMaintenanceType = 'PREVENTIVE' | 'CORRECTIVE' | 'INSPECTION' | 'DOCUMENTATION'
export type AuditActionType = 'CREATE' | 'UPDATE' | 'DELETE' | 'HANDOFF' | 'MAINTENANCE' | 'TELEMETRY' | 'INCIDENT_WORKFLOW' | 'AUTH'
export type IncidentStatus = 'OPEN' | 'DISPATCHED' | 'ON_SITE' | 'CLOSED'
export type IncidentType = 'PANIC' | 'SUSPICIOUS_ACTIVITY' | 'MEDICAL' | 'ESCORT'
export type IncidentPriority = 'HIGH' | 'MEDIUM' | 'LOW'
export type AppUserRole = 'ADMIN' | 'SUPERVISOR' | 'CLIENT' | 'RONDA'
export type ResidentAlertType = 'PANIC' | 'COERCION' | 'ESCORT' | 'SUSPICIOUS_ACTIVITY' | 'MEDICAL'
export type ResidentAlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'DISPATCHED' | 'ON_SITE' | 'RESOLVED' | 'CANCELLED'

export interface Agent {
  id: number
  fullName: string
  badgeCode: string
  cnhCategory: string
  cnhExpiry: string
  medicalExamExpiry?: string | null
  workExamsExpiry?: string | null
  documentNotes?: string | null
  status: AgentStatus
  photoUrl?: string | null
}

export interface Resident {
  id: number
  fullName: string
  phoneNumber: string
  address: string
  referenceNote?: string | null
  status: ResidentStatus
}

export interface Vehicle {
  id: number
  plate: string
  model: string
  currentKm: number
  nextMaintenanceKm: number
  ipvaExpiry?: string | null
  licensingExpiry?: string | null
  insuranceExpiry?: string | null
  lastMaintenanceAt?: string | null
  maintenanceNotes?: string | null
  status: VehicleStatus
}

export interface VehicleMaintenanceRecord {
  id: number
  vehicleId: number
  vehiclePlate: string
  type: VehicleMaintenanceType
  openedAt: string
  completedAt?: string | null
  serviceDate?: string | null
  kmAtService?: number | null
  nextMaintenanceKm?: number | null
  costAmount?: number | null
  supplierName?: string | null
  description: string
  resolved: boolean
}

export interface AuditRecord {
  id: number
  actionType: AuditActionType
  entityName: string
  entityId?: number | null
  actorUsername: string
  occurredAt: string
  description: string
}

export interface Shift {
  id: number
  agentId: number
  agentName: string
  vehicleId: number
  vehiclePlate: string
  status: ShiftStatus
  startedAt?: string | null
  scheduledStartAt: string
  scheduledEndAt: string
  checkInAt?: string | null
  checkOutAt?: string | null
  startKm?: number | null
  endKm?: number | null
  handoffFromAgentId?: number | null
  handoffFromAgentName?: string | null
  handoffToAgentId?: number | null
  handoffToAgentName?: string | null
  handoffAcceptedAt?: string | null
  handoffNotes?: string | null
  fuelLevelPercent?: number | null
  tiresChecked: boolean
  lightsChecked: boolean
  documentsChecked: boolean
  checklistNotes?: string | null
  attendanceStatus: ShiftAttendanceStatus
  lateMinutes?: number | null
  coverageForAgentId?: number | null
  coverageForAgentName?: string | null
  attendanceNotes?: string | null
}

export interface PatrolRouteStop {
  title: string
  detail: string
  status: string
}

export interface TelemetryTrailPoint {
  latitude: number
  longitude: number
  speedKmh: number
  accuracyMeters: number
  recordedAt: string
}

export interface ActivePatrol {
  shiftId: number
  agentId: number
  agentName: string
  agentBadgeCode: string
  agentPhotoUrl?: string | null
  vehiclePlate: string
  vehicleModel: string
  vehicleCurrentKm: number
  vehicleStatus: string
  latitude: number
  longitude: number
  speedKmh: number
  accuracyMeters: number
  traveledKmInShift: number
  progressPercent: number
  updatedAt: string
  routeStops: PatrolRouteStop[]
  telemetryTrail: TelemetryTrailPoint[]
}

export interface Incident {
  id: number
  type: IncidentType
  priority: IncidentPriority
  status: IncidentStatus
  residentName: string
  address: string
  openedAt: string
  assignedAgentId?: number | null
  assignedAgentName?: string | null
  vehicleId?: number | null
  vehiclePlate?: string | null
  dispatchedAt?: string | null
  onSiteAt?: string | null
  closedAt?: string | null
  dispatchNotes?: string | null
  arrivalNotes?: string | null
  closureNotes?: string | null
}

export interface ResidentAlert {
  id: number
  residentId: number
  residentName: string
  residentPhoneNumber: string
  residentAddress: string
  type: ResidentAlertType
  status: ResidentAlertStatus
  latitude?: number | null
  longitude?: number | null
  notes?: string | null
  openedAt: string
  updatedAt: string
  acknowledgedAt?: string | null
  dispatchedAt?: string | null
  onSiteAt?: string | null
  resolvedAt?: string | null
  cancelledAt?: string | null
  assignedAgentId?: number | null
  assignedAgentName?: string | null
  vehicleId?: number | null
  vehiclePlate?: string | null
  acknowledgmentNotes?: string | null
  dispatchNotes?: string | null
  arrivalNotes?: string | null
  resolutionNotes?: string | null
  cancellationReason?: string | null
}

export interface DashboardSummary {
  totalResidents: number
  totalAgents: number
  activeAgents: number
  availableVehicles: number
  activeShifts: number
  lateShifts: number
  absentShifts: number
  openIncidents: number
  maintenanceAlerts: number
  activePatrol?: ActivePatrol | null
  auditRecords: AuditRecord[]
  residents: Resident[]
  agents: Agent[]
  vehicles: Vehicle[]
  maintenanceRecords: VehicleMaintenanceRecord[]
  shifts: Shift[]
  incidents: Incident[]
}

export interface ClientPortal {
  activeShifts: number
  openIncidents: number
  availableVehicles: number
  maintenanceAlerts: number
  recentIncidents: Incident[]
}

export interface AuthSession {
  accessToken: string
  tokenType: string
  expiresAt: string
  username: string
  roles: string[]
}

export interface AppUser {
  id: number
  username: string
  role: AppUserRole
  enabled: boolean
  createdAt: string
}
