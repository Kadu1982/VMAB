import { StatusBar } from 'expo-status-bar'
import AsyncStorage from '@react-native-async-storage/async-storage'
import Constants from 'expo-constants'
import * as ImagePicker from 'expo-image-picker'
import * as Location from 'expo-location'
import * as Notifications from 'expo-notifications'
import { useEffect, useRef, useState } from 'react'
import {
  ActivityIndicator,
  AppState,
  Image,
  Platform,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'
import { WebView } from 'react-native-webview'
import {
  appendTelemetryQueueItem,
  createTelemetrySample,
  ensureBackgroundTrackingForShift,
  evaluateTelemetrySignals,
  flushTelemetryQueue,
  formatTelemetrySignalSummary,
  loadStoredTelemetrySnapshot,
  loadTelemetryQueue,
  normalizeApiBaseUrl,
  saveStoredActiveShiftId,
  saveStoredTelemetrySnapshot,
  sendTelemetrySample,
  stopBackgroundTracking,
  type TelemetrySignal,
  type TelemetrySource,
} from './telemetry'

const DEFAULT_API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? ''
const API_URL_STORAGE_KEY = 'vmab-mobile-api-url'
const MODE_STORAGE_KEY = 'vmab-mobile-mode'
const COLLABORATOR_CREDENTIALS_STORAGE_KEY = 'vmab-mobile-collaborator-creds'
const COLLABORATOR_SESSION_STORAGE_KEY = 'vmab-mobile-collaborator-session'
const COLLABORATOR_PUSH_TOKEN_STORAGE_KEY = 'vmab-mobile-collaborator-push-token'
const RESIDENT_CREDENTIALS_STORAGE_KEY = 'vmab-mobile-resident-creds'
const RESIDENT_SESSION_STORAGE_KEY = 'vmab-mobile-resident-session'
const RESIDENT_PUSH_TOKEN_STORAGE_KEY = 'vmab-mobile-resident-push-token'

Notifications.setNotificationHandler({
  handleNotification: async () => ({
    shouldShowBanner: true,
    shouldShowList: true,
    shouldPlaySound: true,
    shouldSetBadge: false,
  }),
})

type AppMode = 'COLLABORATOR' | 'RESIDENT'

type ShiftStatus = 'PLANNED' | 'ACTIVE' | 'HANDOFF' | 'CLOSED'
type IncidentPriority = 'HIGH' | 'MEDIUM' | 'LOW'
type IncidentStatus = 'OPEN' | 'DISPATCHED' | 'ON_SITE' | 'CLOSED'
type ResidentAlertType = 'PANICO' | 'COACAO' | 'ESCOLTA' | 'SUSPEITA' | 'MEDICA'
type ResidentAlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'DISPATCHED' | 'ON_SITE' | 'RESOLVED' | 'CANCELLED'

type ActivePatrol = {
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
  routeStops: {
    title: string
    detail: string
    status: string
  }[]
}

type AuthSession = {
  accessToken: string
  tokenType: string
  expiresAt: string
  username: string
  roles: string[]
}

type ResidentSession = {
  tokenType: 'Bearer'
  accessToken: string
  expiresAt: string
  residentId: number
  fullName: string
  phoneNumber: string
  address: string
  referenceNote?: string | null
}

type ResidentProfile = {
  residentId: number
  fullName: string
  phoneNumber: string
  address: string
  referenceNote?: string | null
  sessionExpiresAt: string
}

type ResidentAlert = {
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
  assignedAgentName?: string | null
  vehiclePlate?: string | null
  acknowledgmentNotes?: string | null
  dispatchNotes?: string | null
  arrivalNotes?: string | null
  resolutionNotes?: string | null
  cancellationReason?: string | null
}

type ResidentLoginForm = {
  residentId: string
  accessPin: string
}

type EvidenceAsset = {
  uri: string
  fileName: string
  mimeType: string
}

type DashboardSummary = {
  activeShifts: number
  openIncidents: number
  availableVehicles: number
  maintenanceAlerts: number
  activePatrol?: ActivePatrol | null
  shifts: {
    id: number
    agentName: string
    vehiclePlate: string
    status: ShiftStatus
    startedAt: string
    scheduledEndAt: string
  }[]
  incidents: {
    id: number
    type: string
    residentName: string
    address: string
    priority: IncidentPriority
    status: IncidentStatus
    assignedAgentName?: string | null
    vehiclePlate?: string | null
  }[]
  vehicles: {
    id: number
    plate: string
    model: string
    status: string
  }[]
}

type OperationalIncident = {
  id: number
  type: string
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

const initialCredentials = {
  username: 'ronda',
  password: 'ronda123',
}

const initialResidentCredentials: ResidentLoginForm = {
  residentId: '1',
  accessPin: '1122',
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

function formatCoordinate(value: number) {
  return value.toFixed(5)
}

function translateShiftStatus(status: ShiftStatus) {
  return {
    PLANNED: 'Planejado',
    ACTIVE: 'Ativo',
    HANDOFF: 'Troca de turno',
    CLOSED: 'Encerrado',
  }[status]
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

function translateIncidentType(type: string) {
  return {
    PANIC: 'Panico',
    SUSPICIOUS_ACTIVITY: 'Atitude suspeita',
    MEDICAL: 'Emergencia medica',
    ESCORT: 'Escolta',
  }[type] ?? type
}

function translateGenericOperationalText(value: string) {
  return {
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
  }[value] ?? value
}

function translateAlertType(type: ResidentAlertType) {
  return {
    PANICO: 'Panico',
    COACAO: 'Coacao',
    ESCOLTA: 'Escolta',
    SUSPEITA: 'Suspeita',
    MEDICA: 'Medica',
  }[type]
}

function translateAlertStatus(status: ResidentAlertStatus) {
  return {
    OPEN: 'Aberto',
    ACKNOWLEDGED: 'Recebido',
    DISPATCHED: 'Despachado',
    ON_SITE: 'No local',
    RESOLVED: 'Resolvido',
    CANCELLED: 'Cancelado',
  }[status]
}

function isActiveAlert(status: ResidentAlertStatus) {
  return status === 'OPEN' || status === 'ACKNOWLEDGED' || status === 'DISPATCHED' || status === 'ON_SITE'
}

function getAlertOperationalMessage(alert: ResidentAlert) {
  if (alert.status === 'OPEN') {
    return alert.silent
      ? 'Sinal silencioso registrado. Mantenha a rotina normal enquanto a central avalia o atendimento.'
      : 'Alerta registrado. A central ainda vai confirmar o recebimento.'
  }

  if (alert.status === 'ACKNOWLEDGED') {
    return 'A central recebeu seu alerta e esta preparando o atendimento.'
  }

  if (alert.status === 'DISPATCHED') {
    return `${alert.assignedAgentName ?? 'Equipe'} em deslocamento${alert.vehiclePlate ? ` com viatura ${alert.vehiclePlate}` : ''}.`
  }

  if (alert.status === 'ON_SITE') {
    return 'A equipe ja esta no local.'
  }

  if (alert.status === 'RESOLVED') {
    return 'Atendimento encerrado pela operacao.'
  }

  return 'Alerta cancelado.'
}

function normalizeApiUrl(value: string) {
  return normalizeApiBaseUrl(value)
}

function buildPatrolMapHtml(activePatrol: ActivePatrol) {
  // Desenha um mapa leve em WebView para manter a posicao da ronda visivel ao morador.
  return `
    <!DOCTYPE html>
    <html lang="pt-BR">
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
        <link
          rel="stylesheet"
          href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
          integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
          crossorigin=""
        />
        <style>
          html, body, #map {
            margin: 0;
            padding: 0;
            width: 100%;
            height: 100%;
            background: #0b1118;
          }
          .leaflet-container {
            background: #0b1118;
          }
        </style>
      </head>
      <body>
        <div id="map"></div>
        <script
          src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
          integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
          crossorigin=""
        ></script>
        <script>
          const latitude = ${activePatrol.latitude};
          const longitude = ${activePatrol.longitude};
          const map = L.map('map', { zoomControl: false }).setView([latitude, longitude], 16);
          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap',
          }).addTo(map);
          const marker = L.marker([latitude, longitude]).addTo(map);
          marker.bindPopup('${activePatrol.agentName.replace(/'/g, "\\'")}').openPopup();
          L.circle([latitude, longitude], {
            radius: ${Math.max(activePatrol.accuracyMeters, 15)},
            color: '#d8b468',
            fillColor: '#d8b468',
            fillOpacity: 0.16,
            weight: 1,
          }).addTo(map);
        </script>
      </body>
    </html>
  `
}

async function registerExpoPushToken() {
  // Solicita permissao e tenta obter o token Expo apenas em aparelho fisico.
  if (Platform.OS === 'web' || !Constants.isDevice) {
    return null
  }

  if (Platform.OS === 'android') {
    await Notifications.setNotificationChannelAsync('vmab-alertas', {
      name: 'VMAB Alertas',
      importance: Notifications.AndroidImportance.MAX,
      vibrationPattern: [0, 250, 250, 250],
      lightColor: '#d8b468',
    })
  }

  const permission = await Notifications.requestPermissionsAsync()
  if (permission.status !== 'granted') {
    return null
  }

  const projectId =
    Constants.easConfig?.projectId ??
    Constants.expoConfig?.extra?.eas?.projectId ??
    undefined

  const tokenResponse = await Notifications.getExpoPushTokenAsync(projectId ? { projectId } : undefined)
  return tokenResponse.data
}

function translateResidentActionLabel(type: ResidentAlertType) {
  return {
    PANICO: 'Panico',
    COACAO: 'Ajuda discreta',
    ESCOLTA: 'Escolta',
    SUSPEITA: 'Suspeita',
    MEDICA: 'Medica',
  }[type]
}

function getResidentActionHint(type: ResidentAlertType) {
  return {
    PANICO: 'Use quando precisar de atendimento urgente imediato.',
    COACAO: 'Aciona a central de forma silenciosa e sem expor o motivo no push.',
    ESCOLTA: 'Solicita acompanhamento ate um destino informado.',
    SUSPEITA: 'Use para movimento ou pessoa suspeita.',
    MEDICA: 'Use para emergencia medica.',
  }[type]
}

function getCollaboratorIncidentNextAction(incident: OperationalIncident, activeAgentId: number | null) {
  if (incident.status === 'OPEN') {
    return 'dispatch'
  }

  if (incident.assignedAgentId !== activeAgentId) {
    return null
  }

  if (incident.status === 'DISPATCHED') {
    return 'onsite'
  }

  if (incident.status === 'ON_SITE') {
    return 'close'
  }

  return null
}

export default function App() {
  // Estado do app da ronda: sessao, telemetria, fila offline e resumo operacional.
  const [mode, setMode] = useState<AppMode>('COLLABORATOR')
  const [credentials, setCredentials] = useState(initialCredentials)
  const [session, setSession] = useState<AuthSession | null>(null)
  const [residentCredentials, setResidentCredentials] = useState(initialResidentCredentials)
  const [residentSession, setResidentSession] = useState<ResidentSession | null>(null)
  const [residentProfile, setResidentProfile] = useState<ResidentProfile | null>(null)
  const [residentAlerts, setResidentAlerts] = useState<ResidentAlert[]>([])
  const [residentPatrol, setResidentPatrol] = useState<ActivePatrol | null>(null)
  const [residentSendingAlert, setResidentSendingAlert] = useState<ResidentAlertType | null>(null)
  const [residentCountdownType, setResidentCountdownType] = useState<ResidentAlertType | null>(null)
  const [residentCountdownSeconds, setResidentCountdownSeconds] = useState(0)
  const [apiBaseUrl, setApiBaseUrl] = useState(DEFAULT_API_BASE_URL)
  const [authenticated, setAuthenticated] = useState(false)
  const [loading, setLoading] = useState(false)
  const [syncingGps, setSyncingGps] = useState(false)
  const [uploadingEvidence, setUploadingEvidence] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [trackingStatus, setTrackingStatus] = useState('GPS inativo')
  const [offlineQueueCount, setOfflineQueueCount] = useState(0)
  const [telemetrySignals, setTelemetrySignals] = useState<TelemetrySignal[]>([])
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [incidentQueue, setIncidentQueue] = useState<OperationalIncident[]>([])
  const [incidentActionNotes, setIncidentActionNotes] = useState<Record<number, string>>({})
  const [incidentActionLoadingId, setIncidentActionLoadingId] = useState<number | null>(null)
  const [incidentActionLoadingType, setIncidentActionLoadingType] = useState<'dispatch' | 'onsite' | 'close' | null>(null)
  const [evidenceIncidentId, setEvidenceIncidentId] = useState('')
  const [evidenceNotes, setEvidenceNotes] = useState('')
  const [evidenceAsset, setEvidenceAsset] = useState<EvidenceAsset | null>(null)
  const foregroundSubscriptionRef = useRef<Location.LocationSubscription | null>(null)
  const residentNotificationListenerRef = useRef<Notifications.EventSubscription | null>(null)
  const residentNotificationResponseRef = useRef<Notifications.EventSubscription | null>(null)
  const collaboratorIncidentSnapshotRef = useRef<string>('')
  const activeResidentAlert = residentAlerts.find((alert) => isActiveAlert(alert.status)) ?? null
  const activeCollaboratorAgentId = summary?.activePatrol?.agentId ?? null

  useEffect(() => {
    // Restaura URL da API, credenciais e sessao para nao exigir reconfiguracao a cada abertura.
    async function hydrateSession() {
      try {
        const [
          storedApiUrl,
          storedMode,
          storedCredentials,
          storedSession,
          storedResidentCredentials,
          storedResidentSession,
        ] = await Promise.all([
          AsyncStorage.getItem(API_URL_STORAGE_KEY),
          AsyncStorage.getItem(MODE_STORAGE_KEY),
          AsyncStorage.getItem(COLLABORATOR_CREDENTIALS_STORAGE_KEY),
          AsyncStorage.getItem(COLLABORATOR_SESSION_STORAGE_KEY),
          AsyncStorage.getItem(RESIDENT_CREDENTIALS_STORAGE_KEY),
          AsyncStorage.getItem(RESIDENT_SESSION_STORAGE_KEY),
        ])

        if (storedApiUrl) {
          setApiBaseUrl(storedApiUrl)
        }

        if (storedMode === 'COLLABORATOR' || storedMode === 'RESIDENT') {
          setMode(storedMode)
        }

        if (storedCredentials) {
          setCredentials(JSON.parse(storedCredentials) as typeof initialCredentials)
        }

        if (storedSession) {
          const parsedSession = JSON.parse(storedSession) as AuthSession
          setSession(parsedSession)
          setAuthenticated(true)
        }

        if (storedResidentCredentials) {
          setResidentCredentials(JSON.parse(storedResidentCredentials) as ResidentLoginForm)
        }

        if (storedResidentSession) {
          setResidentSession(JSON.parse(storedResidentSession) as ResidentSession)
        }

        const queue = await loadTelemetryQueue()
        setOfflineQueueCount(queue.length)
      } catch {
        // fallback silencioso
      }
    }

    void hydrateSession()
  }, [])

  useEffect(() => {
    void AsyncStorage.setItem(API_URL_STORAGE_KEY, apiBaseUrl)
  }, [apiBaseUrl])

  useEffect(() => {
    void AsyncStorage.setItem(MODE_STORAGE_KEY, mode)
  }, [mode])

  useEffect(() => {
    void AsyncStorage.setItem(COLLABORATOR_CREDENTIALS_STORAGE_KEY, JSON.stringify(credentials))
  }, [credentials])

  useEffect(() => {
    if (session) {
      void AsyncStorage.setItem(COLLABORATOR_SESSION_STORAGE_KEY, JSON.stringify(session))
      return
    }

    void AsyncStorage.removeItem(COLLABORATOR_SESSION_STORAGE_KEY)
  }, [session])

  useEffect(() => {
    void AsyncStorage.setItem(RESIDENT_CREDENTIALS_STORAGE_KEY, JSON.stringify(residentCredentials))
  }, [residentCredentials])

  useEffect(() => {
    if (residentSession) {
      void AsyncStorage.setItem(RESIDENT_SESSION_STORAGE_KEY, JSON.stringify(residentSession))
      return
    }

    void AsyncStorage.removeItem(RESIDENT_SESSION_STORAGE_KEY)
  }, [residentSession])

  useEffect(() => {
    // Quando um push chega com o app aberto, o perfil ativo recarrega seus dados reais.
    residentNotificationListenerRef.current?.remove()
    residentNotificationResponseRef.current?.remove()
    residentNotificationListenerRef.current = Notifications.addNotificationReceivedListener(() => {
      if (residentSession?.accessToken) {
        void refreshResidentData()
      } else if (session?.accessToken) {
        void fetchSummary()
      }
    })
    residentNotificationResponseRef.current = Notifications.addNotificationResponseReceivedListener(() => {
      if (residentSession?.accessToken) {
        void refreshResidentData()
      } else if (session?.accessToken) {
        void fetchSummary()
      }
    })

    return () => {
      residentNotificationListenerRef.current?.remove()
      residentNotificationListenerRef.current = null
      residentNotificationResponseRef.current?.remove()
      residentNotificationResponseRef.current = null
    }
  }, [residentSession?.accessToken, session?.accessToken])

  async function refreshOfflineQueueCount() {
    const queue = await loadTelemetryQueue()
    setOfflineQueueCount(queue.length)
  }

  async function flushOfflineQueue() {
    const baseUrl = normalizeApiUrl(apiBaseUrl)
    if (!baseUrl || !session?.accessToken) {
      return
    }

    const result = await flushTelemetryQueue(baseUrl, session.accessToken)
    setOfflineQueueCount(result.remaining)

    if (result.sent > 0) {
      setTrackingStatus(`Fila offline sincronizada: ${result.sent} ponto(s) enviados.`)
    }
  }

  async function pickEvidenceFromLibrary() {
    // Permite anexar foto da ocorrencia direto do celular da ronda.
    const permission = await ImagePicker.requestMediaLibraryPermissionsAsync()
    if (!permission.granted) {
      setError('Permita acesso a biblioteca para anexar evidencias.')
      return
    }

    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ['images'],
      quality: 0.7,
    })

    if (result.canceled || !result.assets.length) {
      return
    }

    const asset = result.assets[0]
    setEvidenceAsset({
      uri: asset.uri,
      fileName: asset.fileName ?? `evidencia-${Date.now()}.jpg`,
      mimeType: asset.mimeType ?? 'image/jpeg',
    })
  }

  async function takeEvidencePhoto() {
    // Captura foto em campo para vincular prova visual a uma ocorrencia.
    const permission = await ImagePicker.requestCameraPermissionsAsync()
    if (!permission.granted) {
      setError('Permita acesso a camera para fotografar a evidencia.')
      return
    }

    const result = await ImagePicker.launchCameraAsync({
      mediaTypes: ['images'],
      quality: 0.7,
    })

    if (result.canceled || !result.assets.length) {
      return
    }

    const asset = result.assets[0]
    setEvidenceAsset({
      uri: asset.uri,
      fileName: asset.fileName ?? `evidencia-${Date.now()}.jpg`,
      mimeType: asset.mimeType ?? 'image/jpeg',
    })
  }

  async function uploadEvidence() {
    // Envia a foto para a ocorrencia selecionada sem depender do painel web.
    const baseUrl = normalizeApiUrl(apiBaseUrl)
    if (!baseUrl || !session?.accessToken) {
      setError('Sua sessao nao existe mais. Entre novamente.')
      return
    }

    if (!evidenceIncidentId || !evidenceAsset) {
      setError('Selecione a ocorrencia e a foto da evidencia.')
      return
    }

    setUploadingEvidence(true)
    setError(null)

    try {
      const formData = new FormData()
      formData.append('notes', evidenceNotes)
      formData.append('file', {
        uri: evidenceAsset.uri,
        name: evidenceAsset.fileName,
        type: evidenceAsset.mimeType,
      } as never)

      const response = await fetch(`${baseUrl}/api/incidents/${evidenceIncidentId}/evidence`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${session.accessToken}`,
        },
        body: formData,
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel enviar a evidencia da ocorrencia.')
      }

      setEvidenceAsset(null)
      setEvidenceNotes('')
      setTrackingStatus('Evidencia enviada com sucesso para a ocorrencia selecionada.')
      await fetchSummary()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao enviar a evidencia.')
    } finally {
      setUploadingEvidence(false)
    }
  }

  async function notifyCollaboratorIncidentUpdate(title: string, body: string) {
    // Usa notificacao local para destacar mudancas operacionais ao colaborador enquanto o app estiver ativo.
    if (Platform.OS === 'web') {
      return
    }

    await Notifications.scheduleNotificationAsync({
      content: {
        title,
        body,
        sound: true,
      },
      trigger: null,
    })
  }

  async function fetchIncidentQueue(activeSession = session, activeAgentId = summary?.activePatrol?.agentId ?? null) {
    // Carrega a fila completa de ocorrencias para o colaborador operar despacho, chegada e encerramento pelo celular.
    const baseUrl = normalizeApiUrl(apiBaseUrl)
    if (!baseUrl || !activeSession?.accessToken) {
      return
    }

    const response = await fetch(`${baseUrl}/api/incidents`, {
      headers: {
        Authorization: `Bearer ${activeSession.accessToken}`,
      },
    })

    if (!response.ok) {
      throw new Error('Nao foi possivel carregar a fila operacional de ocorrencias.')
    }

    const nextIncidents = (await response.json()) as OperationalIncident[]
    const normalizedSnapshot = JSON.stringify(
      nextIncidents.map((incident) => ({
        id: incident.id,
        status: incident.status,
        assignedAgentId: incident.assignedAgentId ?? null,
      })),
    )

    if (collaboratorIncidentSnapshotRef.current) {
      const previousSnapshot = new Map<number, { status: IncidentStatus; assignedAgentId: number | null }>(
        JSON.parse(collaboratorIncidentSnapshotRef.current).map(
          (incident: { id: number; status: IncidentStatus; assignedAgentId: number | null }) => [incident.id, incident],
        ),
      )

      const changedIncident = nextIncidents.find((incident) => {
        const previous = previousSnapshot.get(incident.id)
        if (!previous) {
          return true
        }

        return previous.status !== incident.status || previous.assignedAgentId !== (incident.assignedAgentId ?? null)
      })

      if (changedIncident) {
        const isAssignedToCurrentRonda = activeAgentId != null && changedIncident.assignedAgentId === activeAgentId
        const title = isAssignedToCurrentRonda ? 'Ocorrencia da sua ronda atualizada' : 'Fila operacional atualizada'
        const body = `${translateIncidentType(changedIncident.type)} - ${translateIncidentStatus(changedIncident.status)} - ${changedIncident.address}`
        await notifyCollaboratorIncidentUpdate(title, body)
      }
    }

    collaboratorIncidentSnapshotRef.current = normalizedSnapshot
    setIncidentQueue(nextIncidents)
  }

  async function handleCollaboratorIncidentAction(
    incident: OperationalIncident,
    action: 'dispatch' | 'onsite' | 'close',
  ) {
    // Executa o fluxo ponta a ponta da ronda no celular sem exigir que ela volte para o painel web.
    const baseUrl = normalizeApiUrl(apiBaseUrl)
    if (!baseUrl || !session?.accessToken) {
      setError('Sua sessao nao existe mais. Entre novamente.')
      return
    }

    const actionNote = incidentActionNotes[incident.id]?.trim() ?? ''
    if (action === 'close' && !actionNote) {
      setError('Informe a observacao final para encerrar a ocorrencia.')
      return
    }

    const endpoint = action === 'dispatch'
      ? `/api/incidents/${incident.id}/dispatch/me`
      : action === 'onsite'
        ? `/api/incidents/${incident.id}/onsite/me`
        : `/api/incidents/${incident.id}/close/me`
    const payload =
      action === 'dispatch'
        ? { dispatchNotes: actionNote || null }
        : action === 'onsite'
          ? { arrivalNotes: actionNote || null }
          : { closureNotes: actionNote }

    setIncidentActionLoadingId(incident.id)
    setIncidentActionLoadingType(action)
    setError(null)

    try {
      const response = await fetch(`${baseUrl}${endpoint}`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${session.accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      })

      if (!response.ok) {
        const responseText = await response.text()
        throw new Error(responseText || 'Nao foi possivel atualizar a ocorrencia no mobile.')
      }

      setIncidentActionNotes((current) => ({ ...current, [incident.id]: '' }))
      setTrackingStatus(
        action === 'dispatch'
          ? 'Ocorrencia assumida e despachada pela ronda.'
          : action === 'onsite'
            ? 'Chegada ao local registrada.'
            : 'Ocorrencia encerrada pelo celular da viatura.',
      )
      await fetchSummary()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao atualizar a ocorrencia.')
    } finally {
      setIncidentActionLoadingId(null)
      setIncidentActionLoadingType(null)
    }
  }


  async function fetchSummary(activeSession = session) {
    // Carrega o resumo que alimenta a tela da ronda usando a sessao autenticada.
    setLoading(true)
    setError(null)

    try {
      const baseUrl = normalizeApiUrl(apiBaseUrl)
      if (!baseUrl) {
        throw new Error('Informe a URL da API antes de entrar.')
      }

      if (!activeSession?.accessToken) {
        throw new Error('Sua sessao nao existe mais. Entre novamente.')
      }

      const response = await fetch(`${baseUrl}/api/dashboard/summary`, {
        headers: {
          Authorization: `Bearer ${activeSession.accessToken}`,
        },
      })

      if (response.status === 401 || response.status === 403) {
        throw new Error('Sua sessao nao existe mais. Entre novamente.')
      }

      if (!response.ok) {
        throw new Error('Nao foi possivel carregar a operacao da ronda.')
      }

      const nextSummary = (await response.json()) as DashboardSummary
      setSummary(nextSummary)
      setAuthenticated(true)
      await fetchIncidentQueue(activeSession, nextSummary.activePatrol?.agentId ?? null)
      await saveStoredActiveShiftId(nextSummary.activePatrol?.shiftId ?? null)
      await refreshOfflineQueueCount()
      return true
    } catch (cause) {
      const message = cause instanceof Error ? cause.message : 'Falha inesperada no mobile.'

      if (message.includes('sessao nao existe mais')) {
        setAuthenticated(false)
        setSession(null)
        setSummary(null)
        setIncidentQueue([])
        await saveStoredActiveShiftId(null)
        await stopBackgroundTracking()
      }

      setError(message)
      return false
    } finally {
      setLoading(false)
    }
  }

  async function handleLogin() {
    // Autentica o operador da ronda e guarda o token localmente para reuso.
    setLoading(true)
    setError(null)

    try {
      const baseUrl = normalizeApiUrl(apiBaseUrl)
      if (!baseUrl) {
        throw new Error('Informe a URL da API antes de entrar.')
      }

      const response = await fetch(`${baseUrl}/api/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(credentials),
      })

      if (!response.ok) {
        throw new Error('Login invalido. Verifique usuario e senha.')
      }

      const nextSession = (await response.json()) as AuthSession
      setSession(nextSession)
      setAuthenticated(true)
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
      setResidentPatrol(null)
      setIncidentQueue([])
      await registerCollaboratorPushToken(nextSession.accessToken)
      await fetchSummary(nextSession)
      await flushOfflineQueue()
    } catch (cause) {
      setAuthenticated(false)
      setSession(null)
      setSummary(null)
      setResidentPatrol(null)
      setError(cause instanceof Error ? cause.message : 'Falha inesperada no mobile.')
    } finally {
      setLoading(false)
    }
  }

  async function handleLogout() {
    // Encerra a sessao tanto no backend quanto no estado local do app.
    try {
      const baseUrl = normalizeApiUrl(apiBaseUrl)
      if (baseUrl && session?.accessToken) {
        const storedPushToken = await AsyncStorage.getItem(COLLABORATOR_PUSH_TOKEN_STORAGE_KEY)
        if (storedPushToken) {
          await fetch(`${baseUrl}/api/auth/push-device/revoke`, {
            method: 'POST',
            headers: {
              Authorization: `Bearer ${session.accessToken}`,
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              expoPushToken: storedPushToken,
            }),
          })
        }

        await fetch(`${baseUrl}/api/auth/logout`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${session.accessToken}`,
          },
        })
      }
    } catch {
      // O logout local ainda precisa acontecer mesmo se o backend estiver fora.
    } finally {
      await AsyncStorage.removeItem(COLLABORATOR_PUSH_TOKEN_STORAGE_KEY)
      await stopBackgroundTracking()
      foregroundSubscriptionRef.current?.remove()
      foregroundSubscriptionRef.current = null
      setSession(null)
      setAuthenticated(false)
      setSummary(null)
      setIncidentQueue([])
      setIncidentActionNotes({})
      collaboratorIncidentSnapshotRef.current = ''
      setTelemetrySignals([])
      setOfflineQueueCount(0)
      setTrackingStatus('GPS inativo')
      setError(null)
    }
  }

  async function residentApiFetch(path: string, init?: RequestInit, accessToken?: string) {
    const baseUrl = normalizeApiUrl(apiBaseUrl)
    if (!baseUrl) {
      throw new Error('Informe a URL da API antes de continuar.')
    }

    const headers = new Headers(init?.headers)
    headers.set('Content-Type', 'application/json')
    const token = accessToken ?? residentSession?.accessToken
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }

    return fetch(`${baseUrl}${path}`, {
      ...init,
      headers,
    })
  }

  async function refreshResidentData(nextSession = residentSession) {
    // Carrega ficha, alertas e a patrulha ativa visivel para o morador acompanhar a ronda em tempo real.
    if (!nextSession?.accessToken) {
      return
    }

    setLoading(true)
    setError(null)

    try {
      const [profileResponse, alertsResponse, patrolResponse] = await Promise.all([
        residentApiFetch('/api/resident-app/me', undefined, nextSession.accessToken),
        residentApiFetch('/api/resident-app/alerts', undefined, nextSession.accessToken),
        residentApiFetch('/api/resident-app/patrol', undefined, nextSession.accessToken),
      ])

      if (!profileResponse.ok) {
        throw new Error('Nao foi possivel validar sua sessao de morador.')
      }
      if (!alertsResponse.ok) {
        throw new Error('Nao foi possivel carregar seus alertas.')
      }
      if (!patrolResponse.ok) {
        throw new Error('Nao foi possivel carregar a patrulha visivel.')
      }

      setResidentProfile((await profileResponse.json()) as ResidentProfile)
      setResidentAlerts((await alertsResponse.json()) as ResidentAlert[])
      const patrolPayload = (await patrolResponse.text()).trim()
      setResidentPatrol(patrolPayload ? (JSON.parse(patrolPayload) as ActivePatrol) : null)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao atualizar o app do morador.')
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
      setResidentPatrol(null)
    } finally {
      setLoading(false)
    }
  }

  async function registerResidentPushToken(accessToken: string) {
    // Registra o token Expo do aparelho atual para que o morador receba atualizacoes mesmo com o app fechado.
    try {
      const expoPushToken = await registerExpoPushToken()
      if (!expoPushToken) {
        return
      }

      const storedPushToken = await AsyncStorage.getItem(RESIDENT_PUSH_TOKEN_STORAGE_KEY)
      if (storedPushToken === expoPushToken) {
        return
      }

      const response = await residentApiFetch(
        '/api/resident-app/push-device',
        {
          method: 'POST',
          body: JSON.stringify({
            expoPushToken,
            deviceLabel: `${Platform.OS}-${Constants.deviceName ?? 'dispositivo'}`,
          }),
        },
        accessToken,
      )

      if (!response.ok) {
        throw new Error('Nao foi possivel registrar o dispositivo para notificacoes.')
      }

      await AsyncStorage.setItem(RESIDENT_PUSH_TOKEN_STORAGE_KEY, expoPushToken)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao ativar notificacoes do morador.')
    }
  }

  async function registerCollaboratorPushToken(accessToken: string) {
    // Registra o aparelho operacional para push remoto quando a fila da ronda mudar fora do app.
    try {
      const expoPushToken = await registerExpoPushToken()
      if (!expoPushToken) {
        return
      }

      const storedPushToken = await AsyncStorage.getItem(COLLABORATOR_PUSH_TOKEN_STORAGE_KEY)
      if (storedPushToken === expoPushToken) {
        return
      }

      const baseUrl = normalizeApiUrl(apiBaseUrl)
      if (!baseUrl) {
        return
      }

      const response = await fetch(`${baseUrl}/api/auth/push-device`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          expoPushToken,
          deviceLabel: `${Platform.OS}-${Constants.deviceName ?? 'dispositivo-operacional'}`,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel registrar o dispositivo operacional para notificacoes.')
      }

      await AsyncStorage.setItem(COLLABORATOR_PUSH_TOKEN_STORAGE_KEY, expoPushToken)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha ao ativar notificacoes operacionais.')
    }
  }

  async function handleResidentLogin() {
    // Autentica o morador com PIN dedicado e abre a tela de alerta.
    setLoading(true)
    setError(null)

    try {
      const response = await residentApiFetch('/api/resident-app/session', {
        method: 'POST',
        body: JSON.stringify({
          residentId: Number(residentCredentials.residentId),
          accessPin: residentCredentials.accessPin,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel entrar. Verifique o ID e o PIN do morador.')
      }

      const nextSession = (await response.json()) as ResidentSession
      setResidentSession(nextSession)
      setMode('RESIDENT')
      setSession(null)
      setAuthenticated(false)
      setSummary(null)
      setResidentPatrol(null)
      setIncidentQueue([])
      setIncidentActionNotes({})
      collaboratorIncidentSnapshotRef.current = ''
      setTelemetrySignals([])
      setOfflineQueueCount(0)
      setTrackingStatus('GPS inativo')
      await registerResidentPushToken(nextSession.accessToken)
      await refreshResidentData(nextSession)
    } catch (cause) {
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
      setResidentPatrol(null)
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao autenticar morador.')
    } finally {
      setLoading(false)
    }
  }

  async function handleResidentLogout() {
    // Revoga a sessao do morador no backend antes de limpar o estado local.
    try {
      if (residentSession?.accessToken) {
        const storedPushToken = await AsyncStorage.getItem(RESIDENT_PUSH_TOKEN_STORAGE_KEY)
        if (storedPushToken) {
          await residentApiFetch(
            '/api/resident-app/push-device/revoke',
            {
              method: 'POST',
              body: JSON.stringify({
                expoPushToken: storedPushToken,
              }),
            },
            residentSession.accessToken,
          )
        }

        await residentApiFetch(
          '/api/resident-app/session/logout',
          {
            method: 'POST',
          },
          residentSession.accessToken,
        )
      }
    } catch {
      // Se a sessao ja expirou, o logout local continua acontecendo.
    } finally {
      await AsyncStorage.removeItem(RESIDENT_PUSH_TOKEN_STORAGE_KEY)
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
      setResidentPatrol(null)
      setResidentCountdownType(null)
      setResidentCountdownSeconds(0)
      setError(null)
    }
  }

  async function submitResidentAlert(type: ResidentAlertType) {
    // Abre o alerta do morador com localizacao opcional e sem misturar com o fluxo do colaborador.
    if (!residentSession?.accessToken) {
      setError('Sua sessao de morador expirou. Entre novamente.')
      return
    }

    setResidentSendingAlert(type)
    setError(null)

    try {
      let latitude: number | null = null
      let longitude: number | null = null

      const permission = await Location.requestForegroundPermissionsAsync()
      if (permission.status !== 'granted') {
        setError('Permissao de localizacao obrigatoria para enviar alerta.')
        setResidentCountdownType(null)
        setResidentCountdownSeconds(0)
        return
      }

      const currentPosition = await Location.getCurrentPositionAsync({
        accuracy: Location.Accuracy.Balanced,
      })
      latitude = currentPosition.coords.latitude
      longitude = currentPosition.coords.longitude

      const response = await residentApiFetch('/api/resident-app/alerts', {
        method: 'POST',
        body: JSON.stringify({
          type,
          latitude,
          longitude,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel abrir o alerta.')
      }

      setResidentCountdownType(null)
      setResidentCountdownSeconds(0)
      await refreshResidentData()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao abrir o alerta.')
    } finally {
      setResidentSendingAlert(null)
    }
  }

  function beginResidentAlertCountdown(type: ResidentAlertType) {
    // Aplica uma pequena janela de cancelamento para evitar disparos acidentais.
    if (residentCountdownType === type) {
      setResidentCountdownType(null)
      setResidentCountdownSeconds(0)
      return
    }

    if (activeResidentAlert) {
      return
    }

    setResidentCountdownType(type)
    setResidentCountdownSeconds(5)
  }

  async function cancelResidentAlert(alertId: number) {
    // Cancela o proprio alerta enquanto ainda esta em estado recuperavel.
    setError(null)

    try {
      const response = await residentApiFetch(`/api/resident-app/alerts/${alertId}/cancel`, {
        method: 'POST',
        body: JSON.stringify({
          cancellationReason: 'Cancelado pelo morador no app unificado',
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel cancelar o alerta.')
      }

      await refreshResidentData()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao cancelar o alerta.')
    }
  }

  async function processTelemetryLocation(
    shiftId: number,
    location: Location.LocationObject,
    source: TelemetrySource,
  ) {
    // Converte cada leitura de GPS em telemetria operacional, sinaliza anomalias e tenta enviar ao backend.
    setSyncingGps(true)

    try {
      const sample = createTelemetrySample(location, shiftId, source)
      const previousSnapshot = await loadStoredTelemetrySnapshot()
      const signals = evaluateTelemetrySignals(sample, previousSnapshot)

      setTelemetrySignals(signals)
      await saveStoredTelemetrySnapshot(sample)

      const baseUrl = normalizeApiUrl(apiBaseUrl)
      if (!baseUrl || !session?.accessToken) {
        await appendTelemetryQueueItem(sample, signals, 'Sessao ou URL da API indisponivel.')
        await refreshOfflineQueueCount()
        setTrackingStatus(`Telemetria guardada em fila offline (${source}).`)
        return
      }

      try {
        await sendTelemetrySample(baseUrl, session.accessToken, sample)
        setSummary((current) => {
          if (!current?.activePatrol) return current

          return {
            ...current,
            activePatrol: {
              ...current.activePatrol,
              latitude: sample.latitude,
              longitude: sample.longitude,
              speedKmh: sample.speedKmh,
              accuracyMeters: sample.accuracyMeters,
              updatedAt: sample.recordedAt,
            },
          }
        })

        setTrackingStatus(
          source === 'background'
            ? `GPS em segundo plano sincronizado em ${formatDate(sample.recordedAt)}`
            : `GPS sincronizado em ${formatDate(sample.recordedAt)}`,
        )
      } catch (cause) {
        await appendTelemetryQueueItem(
          sample,
          signals,
          cause instanceof Error ? cause.message : 'Falha ao enviar telemetria.',
        )
        setTrackingStatus(`Fila offline atualizada (${source}).`)
      }

      await refreshOfflineQueueCount()
    } finally {
      setSyncingGps(false)
    }
  }

  useEffect(() => {
    // Mantem o resumo do app atualizado enquanto a sessao da ronda estiver aberta.
    if (!authenticated) return

    void fetchSummary()

    const intervalId = setInterval(() => {
      void fetchSummary()
      void flushOfflineQueue()
    }, 15_000)

    return () => {
      clearInterval(intervalId)
    }
  }, [authenticated, apiBaseUrl, session?.accessToken])

  useEffect(() => {
    // Tenta iniciar rastreamento continuo quando existir um turno ativo e uma sessao valida.
    if (!authenticated || !summary?.activePatrol?.shiftId) {
      foregroundSubscriptionRef.current?.remove()
      foregroundSubscriptionRef.current = null
      void stopBackgroundTracking()
      return
    }

    const activeShiftId = summary.activePatrol.shiftId
    let cancelled = false

    async function startTracking() {
      const permission = await Location.requestForegroundPermissionsAsync()

      if (cancelled) return

      if (permission.status !== 'granted') {
        setTrackingStatus('Permissao de localizacao negada.')
        return
      }

      foregroundSubscriptionRef.current?.remove()
      foregroundSubscriptionRef.current = await Location.watchPositionAsync(
        {
          accuracy: Location.Accuracy.Balanced,
          timeInterval: 10_000,
          distanceInterval: 15,
        },
        (location) => {
          void processTelemetryLocation(activeShiftId, location, 'foreground')
        },
      )

      const backgroundResult = await ensureBackgroundTrackingForShift(activeShiftId)
      if (!cancelled) {
        setTrackingStatus(backgroundResult.message)
      }
    }

    void startTracking()

    return () => {
      cancelled = true
      foregroundSubscriptionRef.current?.remove()
      foregroundSubscriptionRef.current = null
    }
  }, [authenticated, summary?.activePatrol?.shiftId, apiBaseUrl, session?.accessToken])

  useEffect(() => {
    if (!residentCountdownType || residentCountdownSeconds <= 0) {
      return
    }

    const timeoutId = setTimeout(() => {
      setResidentCountdownSeconds((current) => current - 1)
    }, 1000)

    return () => {
      clearTimeout(timeoutId)
    }
  }, [residentCountdownType, residentCountdownSeconds])

  useEffect(() => {
    if (!residentCountdownType || residentCountdownSeconds !== 0) {
      return
    }

    void submitResidentAlert(residentCountdownType)
  }, [residentCountdownType, residentCountdownSeconds])

  useEffect(() => {
    // Mantem o morador sincronizado com o status do atendimento.
    if (!residentSession?.accessToken) return

    void refreshResidentData(residentSession)
    void registerResidentPushToken(residentSession.accessToken)

    const intervalId = setInterval(() => {
      void refreshResidentData(residentSession)
    }, activeResidentAlert ? 8_000 : 20_000)

    return () => {
      clearInterval(intervalId)
    }
  }, [activeResidentAlert, residentSession?.accessToken])

  useEffect(() => {
    // Quando o app volta ao primeiro plano, tenta drenar a fila offline e revalidar o resumo.
    const subscription = AppState.addEventListener('change', (state) => {
      if (state !== 'active') return

      if (authenticated) {
        void refreshOfflineQueueCount()
        void flushOfflineQueue()
        void fetchSummary()
      }

      if (residentSession?.accessToken) {
        void refreshResidentData()
      }
    })

    return () => {
      subscription.remove()
    }
  }, [authenticated, residentSession?.accessToken, apiBaseUrl, session?.accessToken])

  if (!authenticated && !residentSession) {
    // Tela de acesso unificada com selecao de perfil antes de entrar.
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="dark" />
        <View style={styles.loginShell}>
          {/* Camada visual de entrada: deixa o app com leitura mais premium sem alterar o fluxo de autenticacao. */}
          <View style={styles.heroCard}>
            <View style={styles.heroBadge}>
              <Text style={styles.heroBadgeText}>VMAB Mobile</Text>
            </View>
            <Text style={styles.title}>Um app. Dois perfis.</Text>
            <Text style={styles.copy}>Escolha se o acesso sera de morador ou colaborador e entre com as credenciais desse perfil.</Text>
            <View style={styles.heroDivider} />
            <Text style={styles.heroSupportingText}>Interface unica para emergencia, patrulha, evidencia e resposta operacional.</Text>
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <View style={styles.modeSwitcher}>
            <Pressable style={[styles.modeButton, mode === 'COLLABORATOR' ? styles.modeButtonActive : null]} onPress={() => setMode('COLLABORATOR')}>
              <Text style={[styles.modeButtonText, mode === 'COLLABORATOR' ? styles.modeButtonTextActive : null]}>Colaborador</Text>
            </Pressable>
            <Pressable style={[styles.modeButton, mode === 'RESIDENT' ? styles.modeButtonActive : null]} onPress={() => setMode('RESIDENT')}>
              <Text style={[styles.modeButtonText, mode === 'RESIDENT' ? styles.modeButtonTextActive : null]}>Morador</Text>
            </Pressable>
          </View>

          <View style={styles.formCard}>
            <View style={styles.sectionHeaderRow}>
              <Text style={styles.sectionTitle}>Acesso seguro</Text>
              <View style={styles.inlinePill}>
                <Text style={styles.inlinePillText}>{mode === 'COLLABORATOR' ? 'Colaborador' : 'Morador'}</Text>
              </View>
            </View>
            <TextInput
              autoCapitalize="none"
              autoCorrect={false}
              placeholder="URL da API"
              placeholderTextColor="#857759"
              style={styles.input}
              value={apiBaseUrl}
              onChangeText={setApiBaseUrl}
            />

            {mode === 'COLLABORATOR' ? (
              <>
                <TextInput
                  autoCapitalize="none"
                  placeholder="Usuario"
                  placeholderTextColor="#857759"
                  style={styles.input}
                  value={credentials.username}
                  onChangeText={(value) => setCredentials((current) => ({ ...current, username: value }))}
                />
                <TextInput
                  secureTextEntry
                  placeholder="Senha"
                  placeholderTextColor="#857759"
                  style={styles.input}
                  value={credentials.password}
                  onChangeText={(value) => setCredentials((current) => ({ ...current, password: value }))}
                />
                <Pressable style={styles.primaryButton} onPress={() => void handleLogin()}>
                  <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar como colaborador'}</Text>
                </Pressable>
              </>
            ) : (
              <>
                <TextInput
                  keyboardType="numeric"
                  inputMode="numeric"
                  placeholder="ID do morador"
                  placeholderTextColor="#857759"
                  style={styles.input}
                  value={residentCredentials.residentId}
                  onChangeText={(value) => setResidentCredentials((current) => ({ ...current, residentId: value.replace(/\D/g, '') }))}
                />
                <TextInput
                  keyboardType="numeric"
                  secureTextEntry
                  inputMode="numeric"
                  placeholder="PIN de acesso"
                  placeholderTextColor="#857759"
                  style={styles.input}
                  value={residentCredentials.accessPin}
                  onChangeText={(value) => setResidentCredentials((current) => ({ ...current, accessPin: value.replace(/\D/g, '').slice(0, 6) }))}
                />
                <Pressable style={styles.primaryButton} onPress={() => void handleResidentLogin()}>
                  <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar como morador'}</Text>
                </Pressable>
              </>
            )}
          </View>

          {/* Painel curto de apoio para acelerar o primeiro acesso em homologacao. */}
          <View style={styles.infoBlock}>
            <Text style={styles.infoTitle}>Credenciais iniciais</Text>
            {mode === 'COLLABORATOR' ? (
              <>
                <Text style={styles.infoText}>usuario: ronda</Text>
                <Text style={styles.infoText}>senha: ronda123</Text>
              </>
            ) : (
              <>
                <Text style={styles.infoText}>ID: 1</Text>
                <Text style={styles.infoText}>PIN padrao local: 1122</Text>
              </>
            )}
            <Text style={styles.infoText}>API atual: {normalizeApiUrl(apiBaseUrl) || 'nao configurada'}</Text>
          </View>
        </View>
      </SafeAreaView>
    )
  }

  if (residentSession) {
    // Tela do morador: alerta, cancelamento, contagem regressiva e historico.
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="dark" />
        <ScrollView contentContainerStyle={styles.scrollContent}>
          {/* Cabecalho principal do morador com linguagem mais leve e orientada ao estado do atendimento. */}
          <View style={styles.heroCard}>
            <View style={styles.headerRow}>
              <View style={styles.headerCopyBlock}>
                <Text style={styles.eyebrow}>Atendimento do morador</Text>
                <Text style={styles.title}>{residentProfile?.fullName ?? residentSession.fullName}</Text>
                <Text style={styles.copy}>{residentProfile?.address ?? residentSession.address}</Text>
              </View>
              <Pressable style={styles.secondaryButton} onPress={() => void handleResidentLogout()}>
                <Text style={styles.secondaryButtonText}>Sair</Text>
              </Pressable>
            </View>
            <View style={styles.highlightStrip}>
              <View style={styles.highlightItem}>
                <Text style={styles.highlightLabel}>Telefone</Text>
                <Text style={styles.highlightValue}>{residentProfile?.phoneNumber ?? residentSession.phoneNumber}</Text>
              </View>
              <View style={styles.highlightItem}>
                <Text style={styles.highlightLabel}>Sessao ate</Text>
                <Text style={styles.highlightValue}>
                  {residentProfile?.sessionExpiresAt ? formatDate(residentProfile.sessionExpiresAt) : formatDate(residentSession.expiresAt)}
                </Text>
              </View>
            </View>
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}
          {loading ? <ActivityIndicator size="large" color="#d8b468" /> : null}

          <View style={styles.profileCard}>
            <Text style={styles.sectionTitle}>Sessao ativa</Text>
            <Text style={styles.meta}>Telefone: {residentProfile?.phoneNumber ?? residentSession.phoneNumber}</Text>
            <Text style={styles.meta}>Validade: {residentProfile?.sessionExpiresAt ? formatDate(residentProfile.sessionExpiresAt) : formatDate(residentSession.expiresAt)}</Text>
            {residentProfile?.referenceNote ? <Text style={styles.meta}>Referencia: {residentProfile.referenceNote}</Text> : null}
          </View>

          <View style={styles.card}>
            <View style={styles.sectionHeaderRow}>
              <Text style={styles.sectionTitle}>Patrulha visivel</Text>
              <View style={styles.inlinePill}>
                <Text style={styles.inlinePillText}>Mapa em tempo real</Text>
              </View>
            </View>
            <Text style={styles.meta}>A localizacao atual da ronda fica exposta para dar ciencia do patrulhamento e evidenciar possivel atraso no atendimento.</Text>
            {residentPatrol ? (
              <>
                <View style={styles.mapCard}>
                  <WebView
                    source={{ html: buildPatrolMapHtml(residentPatrol) }}
                    originWhitelist={['*']}
                    style={styles.mapFrame}
                    javaScriptEnabled
                    domStorageEnabled
                    scrollEnabled={false}
                  />
                </View>
                <Text style={styles.meta}>
                  Ultima atualizacao: {formatDate(residentPatrol.updatedAt)} • {formatCoordinate(residentPatrol.latitude)},{' '}
                  {formatCoordinate(residentPatrol.longitude)}
                </Text>

                <View style={styles.metricsGrid}>
                  <View style={styles.metricCard}>
                    <Text style={styles.metricLabel}>Vigilante</Text>
                    <Text style={styles.metricValueSmall}>{residentPatrol.agentName}</Text>
                    <Text style={styles.meta}>Cracha {residentPatrol.agentBadgeCode}</Text>
                  </View>
                  <View style={styles.metricCard}>
                    <Text style={styles.metricLabel}>Viatura</Text>
                    <Text style={styles.metricValueSmall}>{residentPatrol.vehiclePlate}</Text>
                    <Text style={styles.meta}>{residentPatrol.vehicleModel}</Text>
                  </View>
                  <View style={styles.metricCard}>
                    <Text style={styles.metricLabel}>Velocidade atual</Text>
                    <Text style={styles.metricValueSmall}>{residentPatrol.speedKmh.toFixed(0)} km/h</Text>
                    <Text style={styles.meta}>Precisao {residentPatrol.accuracyMeters.toFixed(0)} m</Text>
                  </View>
                </View>
              </>
            ) : (
              <Text style={styles.meta}>Nenhuma patrulha ativa com GPS disponivel neste momento.</Text>
            )}
          </View>

          {activeResidentAlert ? (
            <View style={styles.activeAlertCard}>
              <View style={styles.sectionHeaderRow}>
                <Text style={styles.sectionTitle}>Atendimento em andamento</Text>
                <View style={styles.alertPulsePill}>
                  <Text style={styles.alertPulsePillText}>Ao vivo</Text>
                </View>
              </View>
              <Text style={styles.activeAlertTitle}>{activeResidentAlert.silent ? 'Solicitacao silenciosa' : translateResidentActionLabel(activeResidentAlert.type)}</Text>
              <Text style={styles.body}>{getAlertOperationalMessage(activeResidentAlert)}</Text>
              {activeResidentAlert.escortDestination ? <Text style={styles.meta}>Destino da escolta: {activeResidentAlert.escortDestination}</Text> : null}
              <Text style={styles.meta}>Ultima atualizacao: {formatDate(activeResidentAlert.updatedAt)}</Text>
              <Pressable style={styles.secondaryButtonSmall} onPress={() => void cancelResidentAlert(activeResidentAlert.id)}>
                <Text style={styles.secondaryButtonText}>Cancelar alerta atual</Text>
              </Pressable>
            </View>
          ) : null}

          <View style={styles.card}>
            <View style={styles.sectionHeaderRow}>
              <Text style={styles.sectionTitle}>Abrir alerta</Text>
              <View style={styles.inlinePill}>
                <Text style={styles.inlinePillText}>Acao imediata</Text>
              </View>
            </View>
            <Text style={styles.meta}>
              {activeResidentAlert
                ? 'Ja existe um alerta em atendimento. Aguarde a central concluir ou cancele o alerta atual.'
                : 'A localizacao e obrigatoria para abrir um alerta.'}
            </Text>
            {residentCountdownType ? (
              <Text style={styles.meta}>
                Alerta {translateResidentActionLabel(residentCountdownType)} sera enviado em {residentCountdownSeconds}s. Toque no mesmo botao para cancelar.
              </Text>
            ) : null}
            <View style={styles.alertGrid}>
              {(['PANICO', 'COACAO', 'ESCOLTA', 'SUSPEITA', 'MEDICA'] as ResidentAlertType[]).map((type) => (
                <Pressable
                  key={type}
                  disabled={Boolean(activeResidentAlert) && residentCountdownType !== type}
                  style={[styles.alertButton, Boolean(activeResidentAlert) && residentCountdownType !== type ? styles.alertButtonDisabled : null]}
                  onPress={() => beginResidentAlertCountdown(type)}
                >
                  <Text style={styles.alertButtonLabel}>
                    {residentSendingAlert === type
                      ? 'Enviando...'
                      : residentCountdownType === type
                        ? `Cancelar ${translateResidentActionLabel(type)}`
                        : translateResidentActionLabel(type)}
                  </Text>
                  <Text style={styles.alertButtonHint}>{getResidentActionHint(type)}</Text>
                </Pressable>
              ))}
            </View>
          </View>

          <View style={styles.card}>
            <View style={styles.sectionHeaderRow}>
              <Text style={styles.sectionTitle}>Alertas recentes</Text>
              <View style={styles.inlinePillMuted}>
                <Text style={styles.inlinePillMutedText}>{residentAlerts.length} registro(s)</Text>
              </View>
            </View>
            {residentAlerts.length === 0 ? <Text style={styles.meta}>Nenhum alerta registrado ainda.</Text> : null}
            {residentAlerts.map((alert) => (
              <View key={alert.id} style={styles.alertItem}>
                <View style={styles.alertHeader}>
                  <Text style={styles.alertTitle}>{alert.silent ? 'Solicitacao silenciosa' : translateResidentActionLabel(alert.type)}</Text>
                  <Text style={styles.statusPill}>{translateAlertStatus(alert.status)}</Text>
                </View>
                <Text style={styles.meta}>{formatDate(alert.openedAt)}</Text>
                {alert.notes ? <Text style={styles.body}>{alert.notes}</Text> : null}
                <Text style={styles.meta}>
                  {alert.assignedAgentName ? `${alert.assignedAgentName} • ` : ''}
                  {alert.vehiclePlate ?? 'Sem viatura'}
                </Text>
                {isActiveAlert(alert.status) ? (
                  <Pressable style={styles.secondaryButtonSmall} onPress={() => void cancelResidentAlert(alert.id)}>
                    <Text style={styles.secondaryButtonText}>Cancelar</Text>
                  </Pressable>
                ) : null}
              </View>
            ))}
          </View>
        </ScrollView>
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        {/* Hero da operacao: concentra contexto do turno e acoes frequentes sem poluir a tela. */}
        <View style={styles.heroCard}>
          <View style={styles.headerRow}>
            <View style={styles.headerCopyBlock}>
              <Text style={styles.eyebrow}>Operacao em campo</Text>
              <Text style={styles.title}>Ronda em tempo real</Text>
              <Text style={styles.copy}>A tela prioriza patrulha, fila offline, ocorrencias e evidencia operacional.</Text>
            </View>
            <View style={styles.actionRow}>
              <Pressable style={styles.secondaryButton} onPress={() => void fetchSummary()}>
                <Text style={styles.secondaryButtonText}>{loading ? 'Atualizando...' : 'Atualizar'}</Text>
              </Pressable>
              <Pressable style={styles.secondaryButton} onPress={() => void flushOfflineQueue()}>
                <Text style={styles.secondaryButtonText}>Sincronizar fila</Text>
              </Pressable>
              <Pressable style={styles.secondaryButton} onPress={() => void handleLogout()}>
                <Text style={styles.secondaryButtonText}>Sair</Text>
              </Pressable>
            </View>
          </View>
          <View style={styles.highlightStrip}>
            <View style={styles.highlightItem}>
              <Text style={styles.highlightLabel}>Status GPS</Text>
              <Text style={styles.highlightValue}>{syncingGps ? 'Sincronizando' : 'Ativo'}</Text>
            </View>
            <View style={styles.highlightItem}>
              <Text style={styles.highlightLabel}>Fila local</Text>
              <Text style={styles.highlightValue}>{offlineQueueCount} pendencia(s)</Text>
            </View>
          </View>
        </View>

        {loading ? <ActivityIndicator size="large" color="#d3a24a" /> : null}
        {error ? <Text style={styles.errorText}>{error}</Text> : null}

        {summary?.activePatrol ? (
          // Visao principal da patrulha em curso para o vigilante em campo.
          <View style={styles.activePatrolCard}>
            <View style={styles.profileRow}>
              {summary.activePatrol.agentPhotoUrl ? (
                <Image source={{ uri: summary.activePatrol.agentPhotoUrl }} style={styles.avatar} />
              ) : (
                <View style={styles.avatarFallback}>
                  <Text style={styles.avatarFallbackText}>{summary.activePatrol.agentName.slice(0, 2).toUpperCase()}</Text>
                </View>
              )}
              <View style={styles.profileContent}>
                <Text style={styles.sectionTitle}>{summary.activePatrol.agentName}</Text>
                <Text style={styles.rowMeta}>Vigilante em ronda â€¢ cracha {summary.activePatrol.agentBadgeCode}</Text>
                <Text style={styles.rowMeta}>
                  {summary.activePatrol.vehiclePlate} â€¢ {summary.activePatrol.vehicleModel}
                </Text>
                <Text style={styles.rowMeta}>
                  KM {summary.activePatrol.vehicleCurrentKm.toLocaleString('pt-BR')} â€¢{' '}
                  {translateGenericOperationalText(summary.activePatrol.vehicleStatus)}
                </Text>
              </View>
            </View>

            <View style={styles.metricsGrid}>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>KM percorridos no turno</Text>
                <Text style={styles.metricValue}>{summary.activePatrol.traveledKmInShift.toFixed(1)} km</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Fila offline</Text>
                <Text style={styles.metricValue}>{offlineQueueCount}</Text>
              </View>
            </View>

            <View style={styles.infoBlock}>
              <Text style={styles.infoTitle}>Status da telemetria</Text>
              <Text style={styles.infoText}>{trackingStatus}</Text>
              <Text style={styles.infoText}>
                {syncingGps ? 'Sincronizando GPS...' : `Ultima telemetria no backend: ${formatDate(summary.activePatrol.updatedAt)}`}
              </Text>
              <Text style={styles.infoText}>
                {telemetrySignals.length ? formatTelemetrySignalSummary(telemetrySignals) : 'Sem anomalias detectadas.'}
              </Text>
            </View>

            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>Sinais de antifraude</Text>
              {telemetrySignals.length ? (
                telemetrySignals.map((signal) => (
                  <View style={styles.rowCard} key={signal.code}>
                    <Text style={styles.rowTitle}>{signal.label}</Text>
                    <Text style={styles.rowMeta}>{signal.detail}</Text>
                  </View>
                ))
              ) : (
                <Text style={styles.rowMeta}>Nenhuma anomalia forte detectada nas ultimas leituras.</Text>
              )}
            </View>

            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>Rota da viatura</Text>
              {summary.activePatrol.routeStops.map((stop) => (
                <View style={styles.rowCard} key={`${stop.title}-${stop.detail}`}>
                  <Text style={styles.rowTitle}>{stop.title}</Text>
                  <Text style={styles.rowMeta}>{stop.detail}</Text>
                  <Text style={styles.rowMeta}>{translateGenericOperationalText(stop.status)}</Text>
                </View>
              ))}
            </View>
          </View>
        ) : (
          <View style={styles.sectionCard}>
            <Text style={styles.sectionTitle}>Patrulha ativa</Text>
            <Text style={styles.rowMeta}>Nenhum turno ativo disponivel para iniciar rastreamento.</Text>
          </View>
        )}

        {summary ? (
          <>
            <View style={styles.metricsGrid}>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Ocorrencias abertas</Text>
                <Text style={styles.metricValue}>{summary.openIncidents}</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Alertas manutencao</Text>
                <Text style={styles.metricValue}>{summary.maintenanceAlerts}</Text>
              </View>
            </View>

            <View style={styles.sectionCard}>
              <View style={styles.sectionHeaderRow}>
                <Text style={styles.sectionTitle}>Fila operacional de ocorrencias</Text>
                <View style={styles.inlinePillMuted}>
                  <Text style={styles.inlinePillMutedText}>{incidentQueue.length} item(ns)</Text>
                </View>
              </View>
              {incidentQueue.length === 0 ? <Text style={styles.rowMeta}>Nenhuma ocorrencia carregada no momento.</Text> : null}
              {incidentQueue.map((incident) => {
                const nextAction = getCollaboratorIncidentNextAction(incident, activeCollaboratorAgentId)
                const actionLoading = incidentActionLoadingId === incident.id
                const actionLabel =
                  nextAction === 'dispatch'
                    ? 'Assumir e despachar'
                    : nextAction === 'onsite'
                      ? 'Cheguei ao local'
                      : nextAction === 'close'
                        ? 'Encerrar atendimento'
                        : null
                const actionPlaceholder =
                  nextAction === 'dispatch'
                    ? 'Observacao do despacho'
                    : nextAction === 'onsite'
                      ? 'Observacao da chegada'
                      : nextAction === 'close'
                        ? 'Observacao final obrigatoria'
                        : null

                return (
                  <View style={styles.rowCard} key={incident.id}>
                    <View style={styles.sectionHeaderRow}>
                      <Text style={styles.rowTitle}>
                        {translateIncidentType(incident.type)} | {incident.residentName}
                      </Text>
                      <View style={styles.priorityBadge}>
                        <Text style={styles.priorityBadgeText}>{translateIncidentPriority(incident.priority)}</Text>
                      </View>
                    </View>
                    <Text style={styles.rowMeta}>{incident.address}</Text>
                    <Text style={styles.rowMeta}>
                      {translateIncidentStatus(incident.status)} | {incident.vehiclePlate ?? 'Sem viatura'}
                    </Text>
                    <Text style={styles.rowMeta}>Aberta em {formatDate(incident.openedAt)}</Text>
                    {incident.assignedAgentName ? <Text style={styles.rowMeta}>Ronda responsavel: {incident.assignedAgentName}</Text> : null}
                    {incident.dispatchNotes ? <Text style={styles.rowMeta}>Despacho: {incident.dispatchNotes}</Text> : null}
                    {incident.arrivalNotes ? <Text style={styles.rowMeta}>Chegada: {incident.arrivalNotes}</Text> : null}
                    {incident.closureNotes ? <Text style={styles.rowMeta}>Fechamento: {incident.closureNotes}</Text> : null}
                    {nextAction && actionPlaceholder ? (
                      <>
                        <TextInput
                          placeholder={actionPlaceholder}
                          placeholderTextColor="#7f8ca1"
                          style={styles.input}
                          value={incidentActionNotes[incident.id] ?? ''}
                          onChangeText={(value) => setIncidentActionNotes((current) => ({ ...current, [incident.id]: value }))}
                        />
                        <Pressable
                          style={styles.primaryButton}
                          onPress={() => void handleCollaboratorIncidentAction(incident, nextAction)}
                        >
                          <Text style={styles.primaryButtonText}>
                            {actionLoading ? 'Atualizando...' : actionLabel}
                          </Text>
                        </Pressable>
                      </>
                    ) : null}
                  </View>
                )
              })}
            </View>

            <View style={styles.sectionCard}>
              <View style={styles.sectionHeaderRow}>
                <Text style={styles.sectionTitle}>Evidencia rapida</Text>
                <View style={styles.inlinePill}>
                  <Text style={styles.inlinePillText}>Camera da viatura</Text>
                </View>
              </View>
              <Text style={styles.rowMeta}>Selecione a ocorrencia e envie a foto direto do celular da viatura.</Text>
              <View style={styles.formStack}>
                <TextInput
                  keyboardType="numeric"
                  inputMode="numeric"
                  placeholder="ID da ocorrencia"
                  placeholderTextColor="#7f8ca1"
                  style={styles.input}
                  value={evidenceIncidentId}
                  onChangeText={(value) => setEvidenceIncidentId(value.replace(/\D/g, ''))}
                />
                <TextInput
                  placeholder="Observacao da evidencia"
                  placeholderTextColor="#7f8ca1"
                  style={styles.input}
                  value={evidenceNotes}
                  onChangeText={setEvidenceNotes}
                />
                <View style={styles.actionRow}>
                  <Pressable style={styles.secondaryButton} onPress={() => void takeEvidencePhoto()}>
                    <Text style={styles.secondaryButtonText}>Fotografar</Text>
                  </Pressable>
                  <Pressable style={styles.secondaryButton} onPress={() => void pickEvidenceFromLibrary()}>
                    <Text style={styles.secondaryButtonText}>Escolher imagem</Text>
                  </Pressable>
                </View>
                <Pressable style={styles.primaryButton} onPress={() => void uploadEvidence()}>
                  <Text style={styles.primaryButtonText}>{uploadingEvidence ? 'Enviando...' : 'Enviar evidencia'}</Text>
                </Pressable>
                <Text style={styles.rowMeta}>{evidenceAsset ? `Arquivo pronto: ${evidenceAsset.fileName}` : 'Nenhuma imagem selecionada.'}</Text>
                {evidenceAsset ? (
                  <View style={styles.evidencePreviewCard}>
                    <Image source={{ uri: evidenceAsset.uri }} style={styles.evidencePreview} />
                  </View>
                ) : null}
              </View>
            </View>
          </>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#eef2f7',
  },
  loginShell: {
    flex: 1,
    padding: 22,
    justifyContent: 'center',
    gap: 18,
  },
  scrollContent: {
    padding: 18,
    gap: 16,
    backgroundColor: '#eef2f7',
  },
  eyebrow: {
    fontSize: 11,
    letterSpacing: 2.6,
    textTransform: 'uppercase',
    color: '#a97422',
    marginBottom: 6,
    fontWeight: '700',
  },
  title: {
    fontSize: 31,
    fontWeight: '800',
    color: '#15202b',
  },
  copy: {
    fontSize: 15,
    lineHeight: 22,
    color: '#556477',
  },
  body: {
    color: '#2c3a48',
    lineHeight: 21,
  },
  meta: {
    color: '#6d7a89',
    lineHeight: 20,
  },
  modeSwitcher: {
    flexDirection: 'row',
    gap: 12,
  },
  modeButton: {
    flex: 1,
    borderRadius: 18,
    paddingVertical: 14,
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
  },
  modeButtonActive: {
    backgroundColor: 'rgba(77,125,142,0.08)',
    borderColor: 'rgba(77,125,142,0.28)',
    shadowColor: '#4d7d8e',
    shadowOpacity: 0.18,
    shadowRadius: 16,
    shadowOffset: { width: 0, height: 10 },
    elevation: 2,
  },
  modeButtonText: {
    color: '#1f2d3a',
    fontWeight: '700',
  },
  modeButtonTextActive: {
    color: '#4d7d8e',
  },
  formCard: {
    padding: 18,
    borderRadius: 24,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 13,
  },
  card: {
    padding: 18,
    borderRadius: 24,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 12,
  },
  profileCard: {
    padding: 18,
    borderRadius: 24,
    backgroundColor: '#f7fafc',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 6,
  },
  activeAlertCard: {
    padding: 18,
    borderRadius: 24,
    backgroundColor: 'rgba(200,85,68,0.08)',
    borderWidth: 1,
    borderColor: 'rgba(200,85,68,0.18)',
    gap: 10,
  },
  activeAlertTitle: {
    color: '#7f2a1f',
    fontWeight: '800',
    fontSize: 18,
  },
  input: {
    minHeight: 54,
    borderWidth: 1.5,
    borderColor: 'rgba(200,154,69,0.45)',
    borderRadius: 18,
    paddingHorizontal: 16,
    paddingVertical: 14,
    backgroundColor: '#ffffff',
    color: '#15202b',
    shadowColor: '#15202b',
    shadowOpacity: 0.05,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 4 },
    elevation: 2,
  },
  textArea: {
    minHeight: 88,
    textAlignVertical: 'top',
  },
  primaryButton: {
    borderRadius: 18,
    backgroundColor: '#c89a45',
    paddingVertical: 14,
    alignItems: 'center',
    shadowColor: '#c89a45',
    shadowOpacity: 0.26,
    shadowRadius: 14,
    shadowOffset: { width: 0, height: 8 },
    elevation: 3,
  },
  primaryButtonText: {
    color: '#1a1308',
    fontWeight: '800',
  },
  secondaryButton: {
    borderRadius: 16,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  secondaryButtonSmall: {
    alignSelf: 'flex-start',
    borderRadius: 16,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  secondaryButtonText: {
    color: '#1f2d3a',
    fontWeight: '700',
  },
  infoBlock: {
    padding: 14,
    borderRadius: 20,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 5,
  },
  infoTitle: {
    fontWeight: '700',
    color: '#15202b',
    marginBottom: 8,
  },
  infoText: {
    color: '#6d7a89',
  },
  errorText: {
    color: '#c85544',
    fontWeight: '600',
    backgroundColor: 'rgba(200,85,68,0.08)',
    borderWidth: 1,
    borderColor: 'rgba(200,85,68,0.18)',
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderRadius: 16,
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 16,
    flexWrap: 'wrap',
  },
  actionRow: {
    flexDirection: 'row',
    gap: 10,
    flexWrap: 'wrap',
    justifyContent: 'flex-start',
  },
  activePatrolCard: {
    gap: 12,
    padding: 18,
    borderRadius: 24,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
  },
  profileRow: {
    flexDirection: 'row',
    gap: 14,
    alignItems: 'center',
  },
  profileContent: {
    flex: 1,
    gap: 4,
  },
  avatar: {
    width: 76,
    height: 76,
    borderRadius: 24,
    borderWidth: 1,
    borderColor: 'rgba(242,201,125,0.25)',
  },
  avatarFallback: {
    width: 76,
    height: 76,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#e4bc74',
  },
  avatarFallbackText: {
    fontSize: 26,
    fontWeight: '700',
    color: '#1d1407',
  },
  metricsGrid: {
    gap: 12,
  },
  metricCard: {
    borderRadius: 20,
    padding: 15,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
  },
  metricLabel: {
    fontSize: 13,
    color: '#6d7a89',
    textTransform: 'uppercase',
    letterSpacing: 0.4,
  },
  metricValue: {
    marginTop: 6,
    fontSize: 30,
    fontWeight: '800',
    color: '#15202b',
  },
  metricValueSmall: {
    marginTop: 6,
    fontSize: 18,
    fontWeight: '800',
    color: '#15202b',
  },
  sectionCard: {
    borderRadius: 22,
    padding: 16,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#15202b',
    marginBottom: 4,
  },
  rowCard: {
    borderRadius: 18,
    backgroundColor: '#f7fafc',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.08)',
    padding: 14,
    gap: 5,
  },
  alertItem: {
    padding: 15,
    borderRadius: 20,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 7,
  },
  alertHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    alignItems: 'center',
  },
  alertTitle: {
    color: '#15202b',
    fontWeight: '800',
    flex: 1,
  },
  statusPill: {
    color: '#a97422',
    fontWeight: '800',
  },
  alertGrid: {
    gap: 10,
  },
  alertButton: {
    borderRadius: 18,
    paddingVertical: 15,
    alignItems: 'center',
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
  },
  alertButtonDisabled: {
    opacity: 0.45,
  },
  alertButtonLabel: {
    color: '#15202b',
    fontWeight: '800',
    marginBottom: 4,
  },
  alertButtonHint: {
    color: '#6d7a89',
    fontSize: 12,
    lineHeight: 17,
    paddingHorizontal: 12,
    textAlign: 'center',
  },
  formStack: {
    gap: 10,
  },
  evidencePreviewCard: {
    marginTop: 2,
    borderRadius: 20,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    backgroundColor: '#ffffff',
  },
  evidencePreview: {
    width: '100%',
    height: 188,
  },
  rowTitle: {
    fontWeight: '800',
    color: '#15202b',
  },
  rowMeta: {
    color: '#6d7a89',
    lineHeight: 19,
  },
  mapCard: {
    marginTop: 4,
    borderRadius: 22,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    backgroundColor: '#f7fafc',
  },
  mapFrame: {
    width: '100%',
    height: 260,
    backgroundColor: '#f7fafc',
  },
  heroCard: {
    borderRadius: 28,
    padding: 20,
    gap: 14,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    shadowColor: '#16202b',
    shadowOpacity: 0.12,
    shadowRadius: 18,
    shadowOffset: { width: 0, height: 10 },
    elevation: 3,
  },
  heroBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: 'rgba(200,154,69,0.12)',
    borderWidth: 1,
    borderColor: 'rgba(200,154,69,0.22)',
  },
  heroBadgeText: {
    color: '#a97422',
    fontWeight: '800',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
    fontSize: 11,
  },
  heroDivider: {
    height: 1,
    backgroundColor: 'rgba(42,57,75,0.12)',
  },
  heroSupportingText: {
    color: '#6d7a89',
    lineHeight: 20,
  },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    gap: 10,
    flexWrap: 'wrap',
  },
  inlinePill: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 999,
    backgroundColor: 'rgba(200,154,69,0.12)',
    borderWidth: 1,
    borderColor: 'rgba(200,154,69,0.22)',
  },
  inlinePillText: {
    color: '#a97422',
    fontSize: 11,
    fontWeight: '800',
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  inlinePillMuted: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 999,
    backgroundColor: 'rgba(77,125,142,0.08)',
    borderWidth: 1,
    borderColor: 'rgba(77,125,142,0.16)',
  },
  inlinePillMutedText: {
    color: '#4d7d8e',
    fontSize: 11,
    fontWeight: '800',
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  alertPulsePill: {
    paddingHorizontal: 12,
    paddingVertical: 7,
    borderRadius: 999,
    backgroundColor: 'rgba(200,85,68,0.08)',
    borderWidth: 1,
    borderColor: 'rgba(200,85,68,0.18)',
  },
  alertPulsePillText: {
    color: '#c85544',
    fontSize: 11,
    fontWeight: '800',
    textTransform: 'uppercase',
    letterSpacing: 0.6,
  },
  priorityBadge: {
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
    backgroundColor: 'rgba(200,154,69,0.1)',
    borderWidth: 1,
    borderColor: 'rgba(200,154,69,0.2)',
  },
  priorityBadgeText: {
    color: '#a97422',
    fontSize: 11,
    fontWeight: '800',
    textTransform: 'uppercase',
    letterSpacing: 0.5,
  },
  highlightStrip: {
    flexDirection: 'row',
    gap: 10,
    flexWrap: 'wrap',
  },
  highlightItem: {
    flexGrow: 1,
    minWidth: 130,
    borderRadius: 18,
    paddingHorizontal: 14,
    paddingVertical: 12,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
  },
  highlightLabel: {
    color: '#6d7a89',
    fontSize: 12,
    textTransform: 'uppercase',
    letterSpacing: 0.5,
    marginBottom: 6,
  },
  highlightValue: {
    color: '#15202b',
    fontWeight: '800',
  },
  headerCopyBlock: {
    flex: 1,
    minWidth: 220,
  },
})
