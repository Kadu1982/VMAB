import { StatusBar } from 'expo-status-bar'
import AsyncStorage from '@react-native-async-storage/async-storage'
import * as Location from 'expo-location'
import { useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Image,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'

const DEFAULT_API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? ''
const API_URL_STORAGE_KEY = 'seguranca-api-url'
const CREDENTIALS_STORAGE_KEY = 'seguranca-ronda-creds'

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
  progressPercent: number
  updatedAt: string
  routeStops: {
    title: string
    detail: string
    status: string
  }[]
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

function encodeBase64(value: string) {
  const alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'
  let output = ''
  let index = 0

  while (index < value.length) {
    const a = value.charCodeAt(index++)
    const b = index < value.length ? value.charCodeAt(index++) : Number.NaN
    const c = index < value.length ? value.charCodeAt(index++) : Number.NaN
    const chunk = (a << 16) | ((Number.isNaN(b) ? 0 : b) << 8) | (Number.isNaN(c) ? 0 : c)

    output += alphabet[(chunk >> 18) & 63]
    output += alphabet[(chunk >> 12) & 63]
    output += Number.isNaN(b) ? '=' : alphabet[(chunk >> 6) & 63]
    output += Number.isNaN(c) ? '=' : alphabet[chunk & 63]
  }

  return output
}

function authHeader(username: string, password: string) {
  const encoded = typeof globalThis.btoa === 'function' ? globalThis.btoa(`${username}:${password}`) : encodeBase64(`${username}:${password}`)
  return `Basic ${encoded}`
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

function normalizeApiBaseUrl(value: string) {
  return value.trim().replace(/\/+$/, '')
}

export default function App() {
  // Estado local do app da ronda: sessao, URL da API, telemetria e resumo operacional.
  const [credentials, setCredentials] = useState(initialCredentials)
  const [apiBaseUrl, setApiBaseUrl] = useState(DEFAULT_API_BASE_URL)
  const [authenticated, setAuthenticated] = useState(false)
  const [loading, setLoading] = useState(false)
  const [syncingGps, setSyncingGps] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [trackingStatus, setTrackingStatus] = useState('GPS inativo')
  const [summary, setSummary] = useState<DashboardSummary | null>(null)

  useEffect(() => {
    // Restaura URL da API e credenciais para evitar reconfiguracao a cada abertura do app.
    async function hydrateSession() {
      try {
        const [storedApiUrl, storedCredentials] = await Promise.all([
          AsyncStorage.getItem(API_URL_STORAGE_KEY),
          AsyncStorage.getItem(CREDENTIALS_STORAGE_KEY),
        ])

        if (storedApiUrl) {
          setApiBaseUrl(storedApiUrl)
        }

        if (storedCredentials) {
          setCredentials(JSON.parse(storedCredentials) as typeof initialCredentials)
        }
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

  async function fetchSummary() {
    // Faz login operacional basico e carrega o resumo que alimenta a tela da ronda.
    setLoading(true)
    setError(null)

    try {
      const baseUrl = normalizeApiBaseUrl(apiBaseUrl)
      if (!baseUrl) {
        throw new Error('Informe a URL da API antes de entrar.')
      }

      const response = await fetch(`${baseUrl}/api/dashboard/summary`, {
        headers: {
          Authorization: authHeader(credentials.username, credentials.password),
        },
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel carregar a operacao da ronda.')
      }

      setSummary((await response.json()) as DashboardSummary)
      setAuthenticated(true)
    } catch (cause) {
      setAuthenticated(false)
      setSummary(null)
      setError(cause instanceof Error ? cause.message : 'Falha inesperada no mobile.')
    } finally {
      setLoading(false)
    }
  }

  async function sendTelemetry(shiftId: number, location: Location.LocationObject) {
    // Envia um ponto de GPS da viatura para o backend e atualiza a visao local imediatamente.
    setSyncingGps(true)

    try {
      const baseUrl = normalizeApiBaseUrl(apiBaseUrl)
      const response = await fetch(`${baseUrl}/api/shifts/${shiftId}/telemetry`, {
        method: 'POST',
        headers: {
          Authorization: authHeader(credentials.username, credentials.password),
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          latitude: location.coords.latitude,
          longitude: location.coords.longitude,
          speedKmh: Math.max(0, (location.coords.speed ?? 0) * 3.6),
          accuracyMeters: Math.max(0, location.coords.accuracy ?? 0),
          headingDegrees: location.coords.heading != null && location.coords.heading >= 0 ? location.coords.heading : null,
          recordedAt: new Date(location.timestamp).toISOString(),
        }),
      })

      if (!response.ok) {
        throw new Error('Falha ao enviar telemetria.')
      }

      setTrackingStatus(`GPS ativo • ultimo envio ${formatDate(new Date(location.timestamp).toISOString())}`)
      setSummary((current) => {
        if (!current?.activePatrol) return current

        return {
          ...current,
          activePatrol: {
            ...current.activePatrol,
            latitude: location.coords.latitude,
            longitude: location.coords.longitude,
            speedKmh: Math.max(0, (location.coords.speed ?? 0) * 3.6),
            accuracyMeters: Math.max(0, location.coords.accuracy ?? 0),
            updatedAt: new Date(location.timestamp).toISOString(),
            progressPercent: 100,
          },
        }
      })
    } catch (cause) {
      setTrackingStatus(cause instanceof Error ? cause.message : 'Erro ao sincronizar o GPS')
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
    }, 15000)

    return () => {
      clearInterval(intervalId)
    }
  }, [authenticated, credentials.username, credentials.password, apiBaseUrl])

  useEffect(() => {
    // Inicia o rastreamento em foreground assim que existir um turno ativo autenticado.
    if (!authenticated || !summary?.activePatrol?.shiftId) return

    const activeShiftId = summary.activePatrol.shiftId
    let cancelled = false
    let subscription: Location.LocationSubscription | null = null

    async function startTracking() {
      const permission = await Location.requestForegroundPermissionsAsync()

      if (cancelled) return

      if (permission.status !== 'granted') {
        setTrackingStatus('Permissao de localizacao negada')
        return
      }

      setTrackingStatus('Aguardando sinal GPS...')

      subscription = await Location.watchPositionAsync(
        {
          accuracy: Location.Accuracy.Balanced,
          timeInterval: 10000,
          distanceInterval: 15,
        },
        (location) => {
          void sendTelemetry(activeShiftId, location)
        },
      )
    }

    void startTracking()

    return () => {
      cancelled = true
      subscription?.remove()
    }
  }, [authenticated, summary?.activePatrol?.shiftId])

  if (!authenticated) {
    // Tela de acesso do app da ronda com configuracao da URL da API.
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="light" />
        <View style={styles.loginShell}>
          <Text style={styles.eyebrow}>Ronda Mobile</Text>
          <Text style={styles.title}>Painel da Equipe</Text>
          <Text style={styles.copy}>
            Entre com o perfil da ronda para acompanhar turnos, incidentes, frota e iniciar o rastreamento GPS da viatura.
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
            <Pressable style={styles.primaryButton} onPress={() => void fetchSummary()}>
              <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar'}</Text>
            </Pressable>
          </View>

          <View style={styles.infoBlock}>
            <Text style={styles.infoTitle}>Credenciais iniciais</Text>
            <Text style={styles.infoText}>usuario: ronda</Text>
            <Text style={styles.infoText}>senha: ronda123</Text>
            <Text style={styles.infoText}>API atual: {normalizeApiBaseUrl(apiBaseUrl) || 'nao configurada'}</Text>
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
            <Pressable
              style={styles.secondaryButton}
              onPress={() => {
                setAuthenticated(false)
                setSummary(null)
                setTrackingStatus('GPS inativo')
                setError(null)
              }}
            >
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
                <Text style={styles.rowMeta}>Vigilante em ronda • cracha {summary.activePatrol.agentBadgeCode}</Text>
                <Text style={styles.rowMeta}>{summary.activePatrol.vehiclePlate} • {summary.activePatrol.vehicleModel}</Text>
                <Text style={styles.rowMeta}>KM {summary.activePatrol.vehicleCurrentKm.toLocaleString('pt-BR')} • {summary.activePatrol.vehicleStatus}</Text>
              </View>
            </View>

            <View style={styles.metricsGrid}>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Latitude</Text>
                <Text style={styles.metricValue}>{formatCoordinate(summary.activePatrol.latitude)}</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Longitude</Text>
                <Text style={styles.metricValue}>{formatCoordinate(summary.activePatrol.longitude)}</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Velocidade</Text>
                <Text style={styles.metricValue}>{summary.activePatrol.speedKmh.toFixed(0)} km/h</Text>
              </View>
              <View style={styles.metricCard}>
                <Text style={styles.metricLabel}>Precisao</Text>
                <Text style={styles.metricValue}>{summary.activePatrol.accuracyMeters.toFixed(0)} m</Text>
              </View>
            </View>

            <View style={styles.infoBlock}>
              <Text style={styles.infoTitle}>Status do rastreamento</Text>
              <Text style={styles.infoText}>{trackingStatus}</Text>
              <Text style={styles.infoText}>{syncingGps ? 'Sincronizando GPS...' : `Ultima telemetria no backend: ${formatDate(summary.activePatrol.updatedAt)}`}</Text>
            </View>

            <View style={styles.sectionCard}>
              <Text style={styles.sectionTitle}>Rota da viatura</Text>
              {summary.activePatrol.routeStops.map((stop) => (
                <View style={styles.rowCard} key={`${stop.title}-${stop.detail}`}>
                  <Text style={styles.rowTitle}>{stop.title}</Text>
                  <Text style={styles.rowMeta}>{stop.detail}</Text>
                  <Text style={styles.rowMeta}>{stop.status}</Text>
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
                  <Text style={styles.rowTitle}>{incident.type} | {incident.residentName}</Text>
                  <Text style={styles.rowMeta}>{incident.address}</Text>
                  <Text style={styles.rowMeta}>{incident.priority} | {incident.status} | {incident.vehiclePlate ?? 'Sem viatura'}</Text>
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
