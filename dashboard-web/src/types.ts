export type AgentStatus = 'ACTIVE' | 'ON_DUTY' | 'OFF_DUTY' | 'BLOCKED'
// Tipos compartilhados pelo frontend para alinhar o contrato com a API.
export type ResidentStatus = 'ACTIVE' | 'INACTIVE'
export type VehicleStatus = 'AVAILABLE' | 'IN_OPERATION' | 'MAINTENANCE' | 'BLOCKED'
export type ShiftStatus = 'PLANNED' | 'ACTIVE' | 'HANDOFF' | 'CLOSED'
export type IncidentStatus = 'OPEN' | 'DISPATCHED' | 'ON_SITE' | 'CLOSED'
export type IncidentType = 'PANIC' | 'SUSPICIOUS_ACTIVITY' | 'MEDICAL' | 'ESCORT'
export type IncidentPriority = 'HIGH' | 'MEDIUM' | 'LOW'

export interface Agent {
  id: number
  fullName: string
  badgeCode: string
  cnhCategory: string
  cnhExpiry: string
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
  status: VehicleStatus
}

export interface Shift {
  id: number
  agentId: number
  agentName: string
  vehicleId: number
  vehiclePlate: string
  status: ShiftStatus
  startedAt: string
  scheduledEndAt: string
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
  assignedAgentName?: string | null
  vehiclePlate?: string | null
}

export interface DashboardSummary {
  totalResidents: number
  totalAgents: number
  activeAgents: number
  availableVehicles: number
  activeShifts: number
  openIncidents: number
  maintenanceAlerts: number
  activePatrol?: ActivePatrol | null
  residents: Resident[]
  agents: Agent[]
  vehicles: Vehicle[]
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
