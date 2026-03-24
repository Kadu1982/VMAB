import { StatusBar } from 'expo-status-bar'
import AsyncStorage from '@react-native-async-storage/async-storage'
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
const API_URL_STORAGE_KEY = 'seguranca-api-url'
const CREDENTIALS_STORAGE_KEY = 'seguranca-ronda-creds'
const SESSION_STORAGE_KEY = 'seguranca-ronda-session'

type ShiftStatus = 'PLANNED' | 'ACTIVE' | 'HANDOFF' | 'CLOSED'
type IncidentPriority = 'HIGH' | 'MEDIUM' | 'LOW'
type IncidentStatus = 'OPEN' | 'DISPATCHED' | 'ON_SITE' | 'CLOSED'

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

function normalizeApiUrl(value: string) {
  return normalizeApiBaseUrl(value)
}

export default function App() {
  // Estado do app da ronda: sessao, telemetria, fila offline e resumo operacional.
  const [credentials, setCredentials] = useState(initialCredentials)
  const [session, setSession] = useState<AuthSession | null>(null)
  const [apiBaseUrl, setApiBaseUrl] = useState(DEFAULT_API_BASE_URL)
  const [authenticated, setAuthenticated] = useState(false)
  const [loading, setLoading] = useState(false)
  const [syncingGps, setSyncingGps] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [trackingStatus, setTrackingStatus] = useState('GPS inativo')
  const [offlineQueueCount, setOfflineQueueCount] = useState(0)
  const [telemetrySignals, setTelemetrySignals] = useState<TelemetrySignal[]>([])
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const foregroundSubscriptionRef = useRef<Location.LocationSubscription | null>(null)

  useEffect(() => {
    // Restaura URL da API, credenciais e sessao para nao exigir reconfiguracao a cada abertura.
    async function hydrateSession() {
      try {
        const [storedApiUrl, storedCredentials, storedSession] = await Promise.all([
          AsyncStorage.getItem(API_URL_STORAGE_KEY),
          AsyncStorage.getItem(CREDENTIALS_STORAGE_KEY),
          AsyncStorage.getItem(SESSION_STORAGE_KEY),
        ])

        if (storedApiUrl) {
          setApiBaseUrl(storedApiUrl)
        }

        if (storedCredentials) {
          setCredentials(JSON.parse(storedCredentials) as typeof initialCredentials)
        }

        if (storedSession) {
          const parsedSession = JSON.parse(storedSession) as AuthSession
          setSession(parsedSession)
          setAuthenticated(true)
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
    void AsyncStorage.setItem(CREDENTIALS_STORAGE_KEY, JSON.stringify(credentials))
  }, [credentials])

  useEffect(() => {
    if (session) {
      void AsyncStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session))
      return
    }

    void AsyncStorage.removeItem(SESSION_STORAGE_KEY)
  }, [session])

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
    // Quando o app volta ao primeiro plano, tenta drenar a fila offline e revalidar o resumo.
    const subscription = AppState.addEventListener('change', (state) => {
      if (state !== 'active' || !authenticated) return

      void refreshOfflineQueueCount()
      void flushOfflineQueue()
      void fetchSummary()
    })

    return () => {
      subscription.remove()
    }
  }, [authenticated, apiBaseUrl, session?.accessToken])

  if (!authenticated) {
    // Tela de acesso do app da ronda com configuracao da URL da API.
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="light" />
        <View style={styles.loginShell}>
          <Text style={styles.eyebrow}>Ronda Mobile</Text>
          <Text style={styles.title}>Painel da Equipe</Text>
          <Text style={styles.copy}>
            Entre com o perfil da ronda para acompanhar turnos, incidentes, frota e manter a telemetria ativa.
          </Text>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

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
              <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar'}</Text>
            </Pressable>
          </View>

          <View style={styles.infoBlock}>
            <Text style={styles.infoTitle}>Credenciais iniciais</Text>
            <Text style={styles.infoText}>usuario: ronda</Text>
            <Text style={styles.infoText}>senha: ronda123</Text>
            <Text style={styles.infoText}>API atual: {normalizeApiUrl(apiBaseUrl) || 'nao configurada'}</Text>
          </View>
        </View>
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
            <Text style={styles.copy}>A telemetria do celular alimenta a localizacao da viatura no dashboard web.</Text>
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
          </>
        ) : null}
      </ScrollView>
    </SafeAreaView>
  )
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#070707',
  },
  loginShell: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
    gap: 20,
  },
  scrollContent: {
    padding: 22,
    gap: 16,
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
  formCard: {
    padding: 20,
    borderRadius: 24,
    backgroundColor: '#101722',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.14)',
    gap: 12,
  },
  input: {
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.06)',
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 12,
    backgroundColor: 'rgba(255,255,255,0.035)',
    color: '#f7f4ec',
  },
  primaryButton: {
    borderRadius: 18,
    backgroundColor: '#e4bc74',
    paddingVertical: 14,
    alignItems: 'center',
  },
  primaryButtonText: {
    color: '#1d1407',
    fontWeight: '700',
  },
  secondaryButton: {
    borderRadius: 16,
    backgroundColor: 'rgba(255,255,255,0.03)',
    borderWidth: 1,
    borderColor: 'rgba(228,188,116,0.12)',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  secondaryButtonText: {
    color: '#f5f0e5',
    fontWeight: '700',
  },
  infoBlock: {
    padding: 16,
    borderRadius: 20,
    backgroundColor: '#0f151f',
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
    justifyContent: 'flex-end',
  },
  activePatrolCard: {
    gap: 14,
    padding: 20,
    borderRadius: 24,
    backgroundColor: '#0f151f',
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
    width: 88,
    height: 88,
    borderRadius: 24,
  },
  avatarFallback: {
    width: 88,
    height: 88,
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
    borderRadius: 22,
    padding: 18,
    backgroundColor: '#131b27',
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
    borderRadius: 24,
    padding: 18,
    backgroundColor: '#0f151f',
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
    borderRadius: 16,
    backgroundColor: '#131b27',
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.04)',
    padding: 14,
    gap: 4,
  },
  rowTitle: {
    fontWeight: '700',
    color: '#f7f4ec',
  },
  rowMeta: {
    color: '#9ba7b7',
  },
})
