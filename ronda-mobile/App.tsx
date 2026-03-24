import { StatusBar } from 'expo-status-bar'
import AsyncStorage from '@react-native-async-storage/async-storage'
import * as ImagePicker from 'expo-image-picker'
import * as Location from 'expo-location'
import { useEffect, useRef, useState } from 'react'
import {
  ActivityIndicator,
  AppState,
  Image,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'
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
const RESIDENT_CREDENTIALS_STORAGE_KEY = 'vmab-mobile-resident-creds'
const RESIDENT_SESSION_STORAGE_KEY = 'vmab-mobile-resident-session'

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

type ResidentAlertDraft = {
  notes: string
  escortDestination: string
  coercionPin: string
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

const initialCredentials = {
  username: 'ronda',
  password: 'ronda123',
}

const initialResidentCredentials: ResidentLoginForm = {
  residentId: '1',
  accessPin: '1122',
}

const initialResidentAlertDraft: ResidentAlertDraft = {
  notes: '',
  escortDestination: '',
  coercionPin: '',
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

export default function App() {
  // Estado do app da ronda: sessao, telemetria, fila offline e resumo operacional.
  const [mode, setMode] = useState<AppMode>('COLLABORATOR')
  const [credentials, setCredentials] = useState(initialCredentials)
  const [session, setSession] = useState<AuthSession | null>(null)
  const [residentCredentials, setResidentCredentials] = useState(initialResidentCredentials)
  const [residentSession, setResidentSession] = useState<ResidentSession | null>(null)
  const [residentProfile, setResidentProfile] = useState<ResidentProfile | null>(null)
  const [residentAlerts, setResidentAlerts] = useState<ResidentAlert[]>([])
  const [residentAlertDraft, setResidentAlertDraft] = useState(initialResidentAlertDraft)
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
  const [evidenceIncidentId, setEvidenceIncidentId] = useState('')
  const [evidenceNotes, setEvidenceNotes] = useState('')
  const [evidenceAsset, setEvidenceAsset] = useState<EvidenceAsset | null>(null)
  const foregroundSubscriptionRef = useRef<Location.LocationSubscription | null>(null)
  const activeResidentAlert = residentAlerts.find((alert) => isActiveAlert(alert.status)) ?? null

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
      await saveStoredActiveShiftId(nextSummary.activePatrol?.shiftId ?? null)
      await refreshOfflineQueueCount()
      return true
    } catch (cause) {
      const message = cause instanceof Error ? cause.message : 'Falha inesperada no mobile.'

      if (message.includes('sessao nao existe mais')) {
        setAuthenticated(false)
        setSession(null)
        setSummary(null)
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
      await fetchSummary(nextSession)
      await flushOfflineQueue()
    } catch (cause) {
      setAuthenticated(false)
      setSession(null)
      setSummary(null)
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
      await stopBackgroundTracking()
      foregroundSubscriptionRef.current?.remove()
      foregroundSubscriptionRef.current = null
      setSession(null)
      setAuthenticated(false)
      setSummary(null)
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
    // Carrega ficha e alertas do morador autenticado.
    if (!nextSession?.accessToken) {
      return
    }

    setLoading(true)
    setError(null)

    try {
      const profileResponse = await residentApiFetch('/api/resident-app/me', undefined, nextSession.accessToken)
      if (!profileResponse.ok) {
        throw new Error('Nao foi possivel validar sua sessao de morador.')
      }
      setResidentProfile((await profileResponse.json()) as ResidentProfile)

      const alertsResponse = await residentApiFetch('/api/resident-app/alerts', undefined, nextSession.accessToken)
      if (!alertsResponse.ok) {
        throw new Error('Nao foi possivel carregar seus alertas.')
      }
      setResidentAlerts((await alertsResponse.json()) as ResidentAlert[])
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao atualizar o app do morador.')
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
    } finally {
      setLoading(false)
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
      setTelemetrySignals([])
      setOfflineQueueCount(0)
      setTrackingStatus('GPS inativo')
      await refreshResidentData(nextSession)
    } catch (cause) {
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao autenticar morador.')
    } finally {
      setLoading(false)
    }
  }

  async function handleResidentLogout() {
    // Revoga a sessao do morador no backend antes de limpar o estado local.
    try {
      if (residentSession?.accessToken) {
        await residentApiFetch('/api/resident-app/session/logout', {
          method: 'POST',
        })
      }
    } catch {
      // Se a sessao ja expirou, o logout local continua acontecendo.
    } finally {
      setResidentSession(null)
      setResidentProfile(null)
      setResidentAlerts([])
      setResidentAlertDraft(initialResidentAlertDraft)
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
      if (permission.status === 'granted') {
        const currentPosition = await Location.getCurrentPositionAsync({
          accuracy: Location.Accuracy.Balanced,
        })
        latitude = currentPosition.coords.latitude
        longitude = currentPosition.coords.longitude
      }

      const response = await residentApiFetch('/api/resident-app/alerts', {
        method: 'POST',
        body: JSON.stringify({
          type,
          notes: residentAlertDraft.notes,
          escortDestination: type === 'ESCOLTA' ? residentAlertDraft.escortDestination : null,
          coercionPin: type === 'COACAO' ? residentAlertDraft.coercionPin : null,
          latitude,
          longitude,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel abrir o alerta.')
      }

      setResidentAlertDraft(initialResidentAlertDraft)
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
        <StatusBar style="light" />
        <View style={styles.loginShell}>
          <Text style={styles.eyebrow}>VMAB Mobile</Text>
          <Text style={styles.title}>Um app. Dois perfis.</Text>
          <Text style={styles.copy}>Escolha se o acesso sera de morador ou colaborador e entre com as credenciais desse perfil.</Text>

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
                  placeholder="ID do morador"
                  placeholderTextColor="#857759"
                  style={styles.input}
                  value={residentCredentials.residentId}
                  onChangeText={(value) => setResidentCredentials((current) => ({ ...current, residentId: value }))}
                />
                <TextInput
                  keyboardType="numeric"
                  placeholder="PIN de acesso"
                  placeholderTextColor="#857759"
                  style={styles.input}
                  value={residentCredentials.accessPin}
                  onChangeText={(value) => setResidentCredentials((current) => ({ ...current, accessPin: value }))}
                />
                <Pressable style={styles.primaryButton} onPress={() => void handleResidentLogin()}>
                  <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar como morador'}</Text>
                </Pressable>
              </>
            )}
          </View>

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
        <StatusBar style="light" />
        <ScrollView contentContainerStyle={styles.scrollContent}>
          <View style={styles.headerRow}>
            <View>
              <Text style={styles.eyebrow}>Atendimento do morador</Text>
              <Text style={styles.title}>{residentProfile?.fullName ?? residentSession.fullName}</Text>
              <Text style={styles.copy}>{residentProfile?.address ?? residentSession.address}</Text>
            </View>
            <Pressable style={styles.secondaryButton} onPress={() => void handleResidentLogout()}>
              <Text style={styles.secondaryButtonText}>Sair</Text>
            </Pressable>
          </View>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}
          {loading ? <ActivityIndicator size="large" color="#d8b468" /> : null}

          <View style={styles.profileCard}>
            <Text style={styles.sectionTitle}>Sessao ativa</Text>
            <Text style={styles.meta}>Telefone: {residentProfile?.phoneNumber ?? residentSession.phoneNumber}</Text>
            <Text style={styles.meta}>Validade: {residentProfile?.sessionExpiresAt ? formatDate(residentProfile.sessionExpiresAt) : formatDate(residentSession.expiresAt)}</Text>
            {residentProfile?.referenceNote ? <Text style={styles.meta}>Referencia: {residentProfile.referenceNote}</Text> : null}
          </View>

          {activeResidentAlert ? (
            <View style={styles.activeAlertCard}>
              <Text style={styles.sectionTitle}>Atendimento em andamento</Text>
              <Text style={styles.activeAlertTitle}>{activeResidentAlert.silent ? 'Emergencia silenciosa' : translateAlertType(activeResidentAlert.type)}</Text>
              <Text style={styles.body}>{getAlertOperationalMessage(activeResidentAlert)}</Text>
              {activeResidentAlert.escortDestination ? <Text style={styles.meta}>Destino da escolta: {activeResidentAlert.escortDestination}</Text> : null}
              <Text style={styles.meta}>Ultima atualizacao: {formatDate(activeResidentAlert.updatedAt)}</Text>
              <Pressable style={styles.secondaryButtonSmall} onPress={() => void cancelResidentAlert(activeResidentAlert.id)}>
                <Text style={styles.secondaryButtonText}>Cancelar alerta atual</Text>
              </Pressable>
            </View>
          ) : null}

          <View style={styles.card}>
            <Text style={styles.sectionTitle}>Abrir alerta</Text>
            <Text style={styles.meta}>
              {activeResidentAlert
                ? 'Ja existe um alerta em atendimento. Aguarde a central concluir ou cancele o alerta atual.'
                : 'A localizacao e opcional, mas ajuda a central a encurtar o atendimento.'}
            </Text>
            <TextInput
              multiline
              placeholder="Observacao do alerta"
              placeholderTextColor="#8c8e92"
              style={[styles.input, styles.textArea]}
              value={residentAlertDraft.notes}
              onChangeText={(value) => setResidentAlertDraft((current) => ({ ...current, notes: value }))}
            />
            <TextInput
              placeholder="Destino da escolta"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={residentAlertDraft.escortDestination}
              onChangeText={(value) => setResidentAlertDraft((current) => ({ ...current, escortDestination: value }))}
            />
            <TextInput
              keyboardType="numeric"
              placeholder="PIN de coacao"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={residentAlertDraft.coercionPin}
              onChangeText={(value) => setResidentAlertDraft((current) => ({ ...current, coercionPin: value }))}
            />
            {residentCountdownType ? <Text style={styles.meta}>Alerta {translateAlertType(residentCountdownType)} sera enviado em {residentCountdownSeconds}s. Toque no mesmo botao para cancelar.</Text> : null}
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
                        ? `Cancelar ${translateAlertType(type)}`
                        : translateAlertType(type)}
                  </Text>
                </Pressable>
              ))}
            </View>
          </View>

          <View style={styles.card}>
            <Text style={styles.sectionTitle}>Alertas recentes</Text>
            {residentAlerts.length === 0 ? <Text style={styles.meta}>Nenhum alerta registrado ainda.</Text> : null}
            {residentAlerts.map((alert) => (
              <View key={alert.id} style={styles.alertItem}>
                <View style={styles.alertHeader}>
                  <Text style={styles.alertTitle}>{translateAlertType(alert.type)}</Text>
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
      <StatusBar style="light" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.headerRow}>
          <View>
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
                <Text style={styles.metricLabel}>Velocidade</Text>
                <Text style={styles.metricValue}>{summary.activePatrol.speedKmh.toFixed(0)} km/h</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>KM percorridos no turno</Text>
                <Text style={styles.metricValue}>{summary.activePatrol.traveledKmInShift.toFixed(1)} km</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Precisao</Text>
                <Text style={styles.metricValue}>{summary.activePatrol.accuracyMeters.toFixed(0)} m</Text>
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
                <Text style={styles.metricLabel}>Turnos ativos</Text>
                <Text style={styles.metricValue}>{summary.activeShifts}</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Ocorrencias abertas</Text>
                <Text style={styles.metricValue}>{summary.openIncidents}</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Viaturas disponiveis</Text>
                <Text style={styles.metricValue}>{summary.availableVehicles}</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Alertas manutencao</Text>
                <Text style={styles.metricValue}>{summary.maintenanceAlerts}</Text>
              </View>
            </View>

            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>Ocorrencias</Text>
              {summary.incidents.map((incident) => (
                <View style={styles.rowCard} key={incident.id}>
                  <Text style={styles.rowTitle}>
                    {translateIncidentType(incident.type)} | {incident.residentName}
                  </Text>
                  <Text style={styles.rowMeta}>{incident.address}</Text>
                  <Text style={styles.rowMeta}>
                    {translateIncidentPriority(incident.priority)} | {translateIncidentStatus(incident.status)} |{' '}
                    {incident.vehiclePlate ?? 'Sem viatura'}
                  </Text>
                </View>
              ))}
            </View>

            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>Evidencia rapida</Text>
              <Text style={styles.rowMeta}>Selecione a ocorrencia e envie a foto direto do celular da viatura.</Text>
              <View style={styles.formStack}>
                <TextInput
                  placeholder="ID da ocorrencia"
                  placeholderTextColor="#7f8ca1"
                  style={styles.input}
                  value={evidenceIncidentId}
                  onChangeText={setEvidenceIncidentId}
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
    backgroundColor: '#07101a',
  },
  loginShell: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
    gap: 20,
  },
  scrollContent: {
    padding: 18,
    gap: 14,
  },
  eyebrow: {
    fontSize: 11,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: '#e4bc74',
    marginBottom: 6,
  },
  title: {
    fontSize: 30,
    fontWeight: '700',
    color: '#f7f4ec',
  },
  copy: {
    fontSize: 15,
    lineHeight: 22,
    color: '#b9c2cf',
  },
  body: {
    color: '#dbe1e8',
    lineHeight: 20,
  },
  meta: {
    color: '#9ba7b7',
  },
  modeSwitcher: {
    flexDirection: 'row',
    gap: 10,
  },
  modeButton: {
    flex: 1,
    borderRadius: 16,
    paddingVertical: 12,
    alignItems: 'center',
    backgroundColor: 'rgba(255,255,255,0.03)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.12)',
  },
  modeButtonActive: {
    backgroundColor: 'rgba(225,183,103,0.18)',
    borderColor: 'rgba(225,183,103,0.35)',
  },
  modeButtonText: {
    color: '#f5f0e5',
    fontWeight: '700',
  },
  modeButtonTextActive: {
    color: '#f9e6bf',
  },
  formCard: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: 'rgba(15,24,35,0.96)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.12)',
    gap: 12,
  },
  card: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: '#11151c',
    borderWidth: 1,
    borderColor: 'rgba(216,180,104,0.12)',
    gap: 10,
  },
  profileCard: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: '#10141b',
    borderWidth: 1,
    borderColor: 'rgba(216,180,104,0.12)',
    gap: 4,
  },
  activeAlertCard: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: '#161014',
    borderWidth: 1,
    borderColor: 'rgba(216,104,104,0.24)',
    gap: 8,
  },
  activeAlertTitle: {
    color: '#ffd6d0',
    fontWeight: '800',
    fontSize: 18,
  },
  input: {
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    borderRadius: 14,
    paddingHorizontal: 14,
    paddingVertical: 12,
    backgroundColor: 'rgba(255,255,255,0.03)',
    color: '#f7f4ec',
  },
  textArea: {
    minHeight: 88,
    textAlignVertical: 'top',
  },
  primaryButton: {
    borderRadius: 16,
    backgroundColor: '#e1b767',
    paddingVertical: 13,
    alignItems: 'center',
  },
  primaryButtonText: {
    color: '#1d1407',
    fontWeight: '700',
  },
  secondaryButton: {
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,0.025)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.12)',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  secondaryButtonSmall: {
    alignSelf: 'flex-start',
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,0.025)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.12)',
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  secondaryButtonText: {
    color: '#f5f0e5',
    fontWeight: '700',
  },
  infoBlock: {
    padding: 14,
    borderRadius: 18,
    backgroundColor: 'rgba(15,24,35,0.94)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.1)',
    gap: 4,
  },
  infoTitle: {
    fontWeight: '700',
    color: '#f7f4ec',
    marginBottom: 8,
  },
  infoText: {
    color: '#9ba7b7',
  },
  errorText: {
    color: '#ffb8aa',
    fontWeight: '600',
  },
  headerRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 16,
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
    borderRadius: 22,
    backgroundColor: 'rgba(15,24,35,0.96)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.14)',
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
    borderRadius: 20,
  },
  avatarFallback: {
    width: 76,
    height: 76,
    borderRadius: 20,
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
    borderRadius: 18,
    padding: 14,
    backgroundColor: '#111a25',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.04)',
  },
  metricLabel: {
    fontSize: 13,
    color: '#90a0b4',
  },
  metricValue: {
    marginTop: 6,
    fontSize: 28,
    fontWeight: '700',
    color: '#f7f4ec',
  },
  sectionCard: {
    borderRadius: 20,
    padding: 16,
    backgroundColor: 'rgba(15,24,35,0.94)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.1)',
    gap: 10,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#f7f4ec',
    marginBottom: 4,
  },
  rowCard: {
    borderRadius: 14,
    backgroundColor: '#111a25',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.04)',
    padding: 12,
    gap: 4,
  },
  alertItem: {
    padding: 14,
    borderRadius: 18,
    backgroundColor: '#171c25',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.05)',
    gap: 6,
  },
  alertHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    alignItems: 'center',
  },
  alertTitle: {
    color: '#f7f5ef',
    fontWeight: '800',
  },
  statusPill: {
    color: '#d8b468',
    fontWeight: '800',
  },
  alertGrid: {
    gap: 10,
  },
  alertButton: {
    borderRadius: 16,
    paddingVertical: 14,
    alignItems: 'center',
    backgroundColor: '#171c25',
    borderWidth: 1,
    borderColor: 'rgba(216,180,104,0.18)',
  },
  alertButtonDisabled: {
    opacity: 0.45,
  },
  alertButtonLabel: {
    color: '#f7f5ef',
    fontWeight: '800',
  },
  formStack: {
    gap: 10,
  },
  evidencePreviewCard: {
    marginTop: 2,
    borderRadius: 18,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: 'rgba(125,184,199,0.18)',
    backgroundColor: 'rgba(8,16,26,0.9)',
  },
  evidencePreview: {
    width: '100%',
    height: 188,
  },
  rowTitle: {
    fontWeight: '700',
    color: '#f7f4ec',
  },
  rowMeta: {
    color: '#9ba7b7',
  },
})
