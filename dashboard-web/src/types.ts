export type AgentStatus = 'ACTIVE' | 'ON_DUTY' | 'OFF_DUTY' | 'BLOCKED'
// Tipos compartilhados pelo frontend para alinhar o contrato com a API.
export type ResidentStatus = 'ACTIVE' | 'INACTIVE'
export type VehicleStatus = 'AVAILABLE' | 'IN_OPERATION' | 'MAINTENANCE' | 'BLOCKED'
export type ShiftStatus = 'PLANNED' | 'ACTIVE' | 'HANDOFF_PENDING' | 'HANDOFF' | 'CLOSED'
export type ShiftAttendanceStatus = 'PENDING' | 'ON_TIME' | 'LATE' | 'ABSENT' | 'COVERED'
export type HrEmployeeCategory = 'VIGILANTE' | 'SUPERVISOR' | 'ADMINISTRATIVO' | 'OPERACIONAL' | 'OUTRO'
export type HrEmployeeStatus = 'ACTIVE' | 'BLOCKED' | 'VACATION' | 'LEAVE' | 'TERMINATED'
export type HrAttendanceType = 'CHECK_IN' | 'CHECK_OUT'
export type VehicleMaintenanceType = 'PREVENTIVE' | 'CORRECTIVE' | 'INSPECTION' | 'DOCUMENTATION'
export type VehicleMaintenancePriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
export type VehicleMaintenanceStatus = 'OPEN' | 'IN_PROGRESS' | 'WAITING_PARTS' | 'COMPLETED' | 'CANCELLED'
export type VehicleMaintenanceLifecycleStatus = 'OPEN' | 'BLOCKING' | 'DOCUMENTATION' | 'RESOLVED' | 'CANCELLED'
export type AuditActionType = 'CREATE' | 'UPDATE' | 'DELETE' | 'HANDOFF' | 'MAINTENANCE' | 'TELEMETRY' | 'INCIDENT_WORKFLOW' | 'AUTH' | 'RESIDENT_ALERT'
export type AuditReportCategory = 'ALL' | 'GESTAO' | 'OPERACIONAL' | 'FROTA' | 'RH' | 'SEGURANCA'
export type IncidentStatus = 'OPEN' | 'DISPATCHED' | 'ON_SITE' | 'CLOSED'
export type IncidentType = 'PANIC' | 'SUSPICIOUS_ACTIVITY' | 'MEDICAL' | 'ESCORT'
export type IncidentPriority = 'HIGH' | 'MEDIUM' | 'LOW'
export type AppUserRole = 'ADMIN' | 'SUPERVISOR' | 'CLIENT' | 'RONDA'
export type ResidentAlertType = 'PANIC' | 'COERCION' | 'ESCORT' | 'SUSPICIOUS_ACTIVITY' | 'MEDICAL'
export type ResidentAlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'DISPATCHED' | 'ON_SITE' | 'RESOLVED' | 'CANCELLED'
export type PrivacyRequestType = 'EXPORT' | 'DELETE'
export type PrivacySubjectType = 'RESIDENT' | 'APP_USER' | 'AGENT'
export type PrivacyRequestStatus = 'OPEN' | 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED'

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
  cpf?: string | null
  photoUrl?: string | null
  businessUnitId?: number | null
  referenceNote?: string | null
  status: ResidentStatus
  accessPinConfigured?: boolean
  coercionPinConfigured?: boolean
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
  maintenanceCode: string
  type: VehicleMaintenanceType
  priority: VehicleMaintenancePriority
  status: VehicleMaintenanceStatus
  openedAt: string
  completedAt?: string | null
  serviceDate?: string | null
  dueDate?: string | null
  kmAtService?: number | null
  nextMaintenanceKm?: number | null
  costAmount?: number | null
  supplierName?: string | null
  resolutionNotes?: string | null
  description: string
  resolved: boolean
}

export interface VehicleMaintenanceOrder {
  id: number
  workOrderCode: string
  vehicleId: number
  vehiclePlate: string
  vehicleModel: string
  type: VehicleMaintenanceType
  priority: VehicleMaintenancePriority
  status: VehicleMaintenanceStatus
  lifecycleStatus: VehicleMaintenanceLifecycleStatus
  lifecycleLabel: string
  openedAt: string
  completedAt?: string | null
  serviceDate?: string | null
  dueDate?: string | null
  kmAtService?: number | null
  nextMaintenanceKm?: number | null
  kmRemainingAtService?: number | null
  daysUntilDue?: number | null
  costAmount?: number | null
  supplierName?: string | null
  resolutionNotes?: string | null
  description: string
  resolved: boolean
  blockingVehicle: boolean
  ageDays: number
}

export interface FleetOperationalReport {
  totalVehicles: number
  operationalVehicles: number
  availableVehicles: number
  inOperationVehicles: number
  maintenanceVehicles: number
  blockedVehicles: number
  maintenanceDueSoonVehicles: number
  maintenanceOverdueVehicles: number
  documentAlertVehicles: number
  maintenanceOrdersOpen: number
  maintenanceOrdersResolved: number
  preventiveOrdersOpen: number
  correctiveOrdersOpen: number
  inspectionOrdersOpen: number
  documentationOrdersOpen: number
  totalMaintenanceCostLast30Days: number
  totalMaintenanceCostAllTime: number
  latestOrders: VehicleMaintenanceOrder[]
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

export interface AuditReportResponse {
  generatedAt: string
  from: string
  to: string
  days: number
  category: AuditReportCategory
  includeAuth: boolean
  totalRecords: number
  visibleRecords: number
  recordsByCategory: Record<string, number>
  recordsByActionType: Record<string, number>
  recordsByActor: Record<string, number>
  records: AuditRecord[]
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
  handoffRequestedAt?: string | null
  handoffRequestedBy?: string | null
  handoffRejectedAt?: string | null
  handoffRejectedBy?: string | null
  handoffRejectionReason?: string | null
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

export interface HrEmployee {
  id: number
  employeeCode: string
  fullName: string
  category: HrEmployeeCategory
  status: HrEmployeeStatus
  documentNumber?: string | null
  address?: string | null
  phoneNumber?: string | null
  email?: string | null
  photoUrl?: string | null
  cnhCategory?: string | null
  cnhExpiry?: string | null
  medicalExamExpiry?: string | null
  trainingExpiry?: string | null
  trainingNotes?: string | null
  documentNotes?: string | null
  hireDate?: string | null
  terminationDate?: string | null
  linkedAgentId?: number | null
  linkedAgentName?: string | null
  linkedAppUserId?: number | null
  linkedAppUserUsername?: string | null
  pointEnabled: boolean
  createdAt: string
  updatedAt: string
  lastCheckInAt?: string | null
  lastCheckOutAt?: string | null
  lastCheckInDevice?: string | null
  lastCheckOutDevice?: string | null
  lastCheckInLatitude?: number | null
  lastCheckInLongitude?: number | null
  lastCheckOutLatitude?: number | null
  lastCheckOutLongitude?: number | null
  lastPointNotes?: string | null
  cnhRenewalDue: boolean
  medicalExamRenewalDue: boolean
  trainingRenewalDue: boolean
}

export type BusinessUnitType = 'CONDOMINIUM' | 'NEIGHBORHOOD' | 'COMPANY' | 'OTHER'
export type FamilyRelationshipType = 'SPOUSE' | 'PARTNER' | 'SON' | 'DAUGHTER' | 'PARENT' | 'OTHER'
export type PersonDocumentOwnerType = 'RESIDENT' | 'EMPLOYEE' | 'RESIDENT_DEPENDENT' | 'EMPLOYEE_DEPENDENT'
export type PersonDocumentType = 'BACKGROUND_CHECK' | 'IDENTIFICATION' | 'PROOF_OF_ADDRESS' | 'MEDICAL_EXAM' | 'OTHER'

export interface BusinessUnit {
  id: number
  name: string
  type: BusinessUnitType
  cnpj?: string | null
  active: boolean
  notes?: string | null
}

export interface BusinessSector {
  id: number
  businessUnitId: number
  name: string
  code?: string | null
  active: boolean
  notes?: string | null
}

export interface ResidentDependent {
  id: number
  residentId: number
  fullName: string
  cpf?: string | null
  phoneNumber?: string | null
  relationship: FamilyRelationshipType
  accessEnabled: boolean
  appEnabled: boolean
  notes?: string | null
}

export interface ResidentVehicle {
  id: number
  residentId: number
  plate: string
  model?: string | null
  color?: string | null
  active: boolean
  notes?: string | null
}

export interface HrEmployeeAssignment {
  id: number
  employeeId: number
  businessUnitId: number
  businessSectorId?: number | null
  roleTitle?: string | null
  startDate?: string | null
  endDate?: string | null
  active: boolean
  notes?: string | null
}

export interface HrEmployeeDependent {
  id: number
  employeeId: number
  fullName: string
  cpf?: string | null
  phoneNumber?: string | null
  relationship: FamilyRelationshipType
  notes?: string | null
}

export interface PersonDocument {
  id: number
  ownerType: PersonDocumentOwnerType
  ownerId: number
  documentType: PersonDocumentType
  originalFilename: string
  storedFilename: string
  contentType?: string | null
  fileSizeBytes: number
  notes?: string | null
  uploadedBy: string
  uploadedAt: string
}

export interface HrEmployeeAlert {
  employeeId: number
  employeeName: string
  alertType: string
  dueDate?: string | null
  daysRemaining: number
  message: string
}

export interface HrAttendance {
  id: number
  employeeId: number
  employeeName?: string | null
  eventType: HrAttendanceType
  occurredAt: string
  deviceLabel?: string | null
  latitude?: number | null
  longitude?: number | null
  note?: string | null
  anomalyFlag: boolean
  anomalyReason?: string | null
}

export interface HrSummary {
  totalEmployees: number
  activeEmployees: number
  blockedEmployees: number
  checkedInNowEmployees: number
  expiringSoonAlerts: number
  employees: HrEmployee[]
  alerts: HrEmployeeAlert[]
  attendance: HrAttendance[]
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
  silent: boolean
  escortDestination?: string | null
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

export interface IncidentEvidence {
  id: number
  incidentId: number
  incidentResidentName: string
  originalFilename: string
  contentType: string
  fileSizeBytes: number
  notes?: string | null
  uploadedBy: string
  uploadedAt: string
  retentionExpiresAt: string
  downloadPath: string
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
  openMaintenanceOrders: number
  criticalMaintenanceOrders: number
  activePatrol?: ActivePatrol | null
  hr: HrSummary
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
  openMaintenanceOrders: number
  recentIncidents: Incident[]
}

export interface ClientOperationalReport {
  generatedAt: string
  activeShifts: number
  openIncidents: number
  availableVehicles: number
  maintenanceAlerts: number
  openMaintenanceOrders: number
  criticalMaintenanceOrders: number
  lateShifts: number
  absentShifts: number
  averageDispatchMinutes: number
  averageResolutionMinutes: number
  incidents: Incident[]
  maintenanceOrders: VehicleMaintenanceOrder[]
}

export interface AuthSession {
  accessToken: string
  tokenType: string
  expiresAt: string
  username: string
  roles: string[]
}

export interface AuthenticatedUser {
  username: string
  roles: string[]
  linkedAgentId?: number | null
  linkedAgentName?: string | null
}

export interface AppUser {
  id: number
  username: string
  role: AppUserRole
  enabled: boolean
  createdAt: string
  linkedAgentId?: number | null
  linkedAgentName?: string | null
}

export interface PrivacyRequest {
  id: number
  requestType: PrivacyRequestType
  subjectType: PrivacySubjectType
  subjectId: number
  subjectLabel: string
  status: PrivacyRequestStatus
  requestedBy: string
  requestedAt: string
  handledBy?: string | null
  handledAt?: string | null
  notes?: string | null
  subjectNotifiedAt?: string | null
  subjectNotificationChannel?: string | null
  subjectNotificationNotes?: string | null
  exportGeneratedAt?: string | null
  deletionAppliedAt?: string | null
}

export interface PrivacyRetentionStatus {
  enabled: boolean
  cleanupCron: string
  passwordResetTokenRetentionHours: number
  residentSessionRetentionDays: number
  incidentEvidenceRetentionDays: number
  vehicleMaintenanceRetentionDays: number
  hrAttendanceRetentionDays: number
  removeOrphanEvidenceFiles: boolean
  openRequests: number
  inProgressRequests: number
  completedRequests: number
  lastCleanupAt?: string | null
  lastCleanupDescription?: string | null
}

export interface OperationsStreamEvent {
  type: string
  entityName: string
  entityId?: number | null
  description: string
  occurredAt: string
}
