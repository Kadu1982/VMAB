export type AgentStatus = 'ACTIVE' | 'ON_DUTY' | 'OFF_DUTY' | 'BLOCKED'
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
  totalAgents: number
  activeAgents: number
  availableVehicles: number
  activeShifts: number
  openIncidents: number
  maintenanceAlerts: number
  agents: Agent[]
  vehicles: Vehicle[]
  shifts: Shift[]
  incidents: Incident[]
}
