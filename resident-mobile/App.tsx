import AsyncStorage from '@react-native-async-storage/async-storage'
import { StatusBar } from 'expo-status-bar'
import * as Location from 'expo-location'
import { useEffect, useState } from 'react'
import {
  ActivityIndicator,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
} from 'react-native'

const DEFAULT_API_BASE_URL = process.env.EXPO_PUBLIC_API_BASE_URL ?? ''
const API_URL_STORAGE_KEY = 'vmab-resident-api-url'
const SESSION_STORAGE_KEY = 'vmab-resident-session'
const CREDENTIALS_STORAGE_KEY = 'vmab-resident-credentials'

type ResidentAlertType = 'PANICO' | 'COACAO' | 'ESCOLTA' | 'SUSPEITA' | 'MEDICA'
type ResidentAlertStatus = 'OPEN' | 'ACKNOWLEDGED' | 'DISPATCHED' | 'ON_SITE' | 'RESOLVED' | 'CANCELLED'

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

type LoginForm = {
  residentId: string
  phoneNumber: string
}

type AlertDraft = {
  notes: string
}

const initialLoginForm: LoginForm = {
  residentId: '1',
  phoneNumber: '(11) 99888-1122',
}

const initialAlertDraft: AlertDraft = {
  notes: '',
}

function normalizeApiBaseUrl(value: string) {
  return value.trim().replace(/\/+$/, '')
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}

function translateAlertType(type: ResidentAlertType) {
  return {
    PANICO: 'Pânico',
    COACAO: 'Coação',
    ESCOLTA: 'Escolta',
    SUSPEITA: 'Suspeita',
    MEDICA: 'Médica',
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

export default function App() {
  // Estado do app do morador: sessao, abertura de alerta e historico de atendimento.
  const [apiBaseUrl, setApiBaseUrl] = useState(DEFAULT_API_BASE_URL)
  const [loginForm, setLoginForm] = useState(initialLoginForm)
  const [alertDraft, setAlertDraft] = useState(initialAlertDraft)
  const [session, setSession] = useState<ResidentSession | null>(null)
  const [profile, setProfile] = useState<ResidentProfile | null>(null)
  const [alerts, setAlerts] = useState<ResidentAlert[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)
  const [sendingAlert, setSendingAlert] = useState<ResidentAlertType | null>(null)

  useEffect(() => {
    // Restaura configuracao local para o usuario nao precisar digitar tudo a cada abertura.
    async function restoreSession() {
      try {
        const [storedApiUrl, storedLogin, storedSession] = await Promise.all([
          AsyncStorage.getItem(API_URL_STORAGE_KEY),
          AsyncStorage.getItem(CREDENTIALS_STORAGE_KEY),
          AsyncStorage.getItem(SESSION_STORAGE_KEY),
        ])

        if (storedApiUrl) {
          setApiBaseUrl(storedApiUrl)
        }

        if (storedLogin) {
          setLoginForm(JSON.parse(storedLogin) as LoginForm)
        }

        if (storedSession) {
          const parsedSession = JSON.parse(storedSession) as ResidentSession
          setSession(parsedSession)
        }
      } catch {
        // Falha silenciosa de restauração nao bloqueia o app.
      }
    }

    void restoreSession()
  }, [])

  useEffect(() => {
    void AsyncStorage.setItem(API_URL_STORAGE_KEY, apiBaseUrl)
  }, [apiBaseUrl])

  useEffect(() => {
    void AsyncStorage.setItem(CREDENTIALS_STORAGE_KEY, JSON.stringify(loginForm))
  }, [loginForm])

  useEffect(() => {
    if (session) {
      void AsyncStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(session))
      return
    }

    void AsyncStorage.removeItem(SESSION_STORAGE_KEY)
  }, [session])

  async function apiFetch(path: string, init?: RequestInit, accessToken?: string) {
    const baseUrl = normalizeApiBaseUrl(apiBaseUrl)
    if (!baseUrl) {
      throw new Error('Informe a URL da API antes de continuar.')
    }

    const headers = new Headers(init?.headers)
    headers.set('Content-Type', 'application/json')
    const token = accessToken ?? session?.accessToken
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }

    return fetch(`${baseUrl}${path}`, {
      ...init,
      headers,
    })
  }

  async function refreshResidentData(nextSession = session) {
    // Busca a ficha do morador e o historico de alertas para manter a tela atualizada.
    if (!nextSession?.accessToken) {
      return
    }

    setLoading(true)
    setError(null)

    try {
      const profileResponse = await apiFetch('/api/resident-app/me', undefined, nextSession?.accessToken)
      if (!profileResponse.ok) {
        throw new Error('Nao foi possivel validar sua sessao.')
      }
      setProfile((await profileResponse.json()) as ResidentProfile)

      const alertsResponse = await apiFetch('/api/resident-app/alerts', undefined, nextSession?.accessToken)
      if (!alertsResponse.ok) {
        throw new Error('Nao foi possivel carregar seus alertas.')
      }
      setAlerts((await alertsResponse.json()) as ResidentAlert[])
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao atualizar o app.')
      setSession(null)
      setProfile(null)
      setAlerts([])
    } finally {
      setLoading(false)
    }
  }

  async function handleLogin() {
    // Autentica o morador com o cadastro e o telefone que ja existem no backend.
    setLoading(true)
    setError(null)

    try {
      const response = await apiFetch('/api/resident-app/session', {
        method: 'POST',
        body: JSON.stringify({
          residentId: Number(loginForm.residentId),
          phoneNumber: loginForm.phoneNumber,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel entrar. Verifique o ID e o telefone do morador.')
      }

      const nextSession = (await response.json()) as ResidentSession
      setSession(nextSession)
      await refreshResidentData(nextSession)
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao autenticar.')
      setSession(null)
      setProfile(null)
      setAlerts([])
    } finally {
      setLoading(false)
    }
  }

  async function submitAlert(type: ResidentAlertType) {
    // Abre o alerta com opcional de localizacao para ajudar a central a localizar a ocorrencia.
    if (!session?.accessToken) {
      setError('Sua sessao expirou. Entre novamente.')
      return
    }

    setSendingAlert(type)
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

      const response = await apiFetch('/api/resident-app/alerts', {
        method: 'POST',
        body: JSON.stringify({
          type,
          notes: alertDraft.notes,
          latitude,
          longitude,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel abrir o alerta.')
      }

      setAlertDraft(initialAlertDraft)
      await refreshResidentData()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao abrir o alerta.')
    } finally {
      setSendingAlert(null)
    }
  }

  async function cancelAlert(alertId: number) {
    // Cancela o proprio alerta enquanto ele ainda nao entrou em atendimento avancado.
    setError(null)

    try {
      const response = await apiFetch(`/api/resident-app/alerts/${alertId}/cancel`, {
        method: 'POST',
        body: JSON.stringify({
          cancellationReason: 'Cancelado pelo morador no app',
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

  useEffect(() => {
    // Mantem o painel do morador sincronizado sem depender de acao manual o tempo todo.
    if (!session?.accessToken) {
      return
    }

    void refreshResidentData(session)
    const intervalId = setInterval(() => {
      void refreshResidentData(session)
    }, 20000)

    return () => {
      clearInterval(intervalId)
    }
  }, [session?.accessToken])

  if (!session) {
    // Tela de acesso enxuta para o morador iniciar o uso do app.
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="light" />
        <View style={styles.shell}>
          <View style={styles.heroGlow} />
          <Text style={styles.eyebrow}>VMAB Morador</Text>
          <Text style={styles.title}>Alerta rápido. Controle real.</Text>
          <Text style={styles.copy}>
            Entre com o ID do morador e o telefone cadastrado para abrir alertas, acompanhar status e falar com a central.
          </Text>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <View style={styles.card}>
            <TextInput
              placeholder="URL da API"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={apiBaseUrl}
              onChangeText={setApiBaseUrl}
            />
            <TextInput
              keyboardType="numeric"
              placeholder="ID do morador"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={loginForm.residentId}
              onChangeText={(value) => setLoginForm((current) => ({ ...current, residentId: value }))}
            />
            <TextInput
              placeholder="Telefone cadastrado"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={loginForm.phoneNumber}
              onChangeText={(value) => setLoginForm((current) => ({ ...current, phoneNumber: value }))}
            />
            <Pressable style={styles.primaryButton} onPress={() => void handleLogin()}>
              <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar'}</Text>
            </Pressable>
          </View>

          <View style={styles.hintCard}>
            <Text style={styles.hintTitle}>Exemplo de teste</Text>
            <Text style={styles.hintText}>ID: 1</Text>
            <Text style={styles.hintText}>Telefone: (11) 99888-1122</Text>
          </View>
        </View>
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="light" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <View>
            <Text style={styles.eyebrow}>Atendimento do morador</Text>
            <Text style={styles.title}>{profile?.fullName ?? session.fullName}</Text>
            <Text style={styles.copy}>{profile?.address ?? session.address}</Text>
          </View>
          <Pressable
            style={styles.secondaryButton}
            onPress={() => {
              setSession(null)
              setProfile(null)
              setAlerts([])
              setError(null)
            }}
          >
            <Text style={styles.secondaryButtonText}>Sair</Text>
          </Pressable>
        </View>

        {error ? <Text style={styles.errorText}>{error}</Text> : null}
        {loading ? <ActivityIndicator size="large" color="#d8b468" /> : null}

        <View style={styles.profileCard}>
          <Text style={styles.sectionTitle}>Sessao ativa</Text>
          <Text style={styles.meta}>Telefone: {profile?.phoneNumber ?? session.phoneNumber}</Text>
          <Text style={styles.meta}>Validade: {profile?.sessionExpiresAt ? formatDate(profile.sessionExpiresAt) : formatDate(session.expiresAt)}</Text>
          {profile?.referenceNote ? <Text style={styles.meta}>Referencia: {profile.referenceNote}</Text> : null}
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Abrir alerta</Text>
          <Text style={styles.meta}>A localizacao e opcional, mas ajuda a central a encurtar o atendimento.</Text>
          <TextInput
            multiline
            placeholder="Observacao do alerta"
            placeholderTextColor="#8c8e92"
            style={[styles.input, styles.textArea]}
            value={alertDraft.notes}
            onChangeText={(value) => setAlertDraft({ notes: value })}
          />
          <View style={styles.alertGrid}>
            {(['PANICO', 'COACAO', 'ESCOLTA', 'SUSPEITA', 'MEDICA'] as ResidentAlertType[]).map((type) => (
              <Pressable key={type} style={styles.alertButton} onPress={() => void submitAlert(type)}>
                <Text style={styles.alertButtonLabel}>{sendingAlert === type ? 'Enviando...' : translateAlertType(type)}</Text>
              </Pressable>
            ))}
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Alertas recentes</Text>
          {alerts.length === 0 ? <Text style={styles.meta}>Nenhum alerta registrado ainda.</Text> : null}
          {alerts.map((alert) => (
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
                <Pressable style={styles.secondaryButtonSmall} onPress={() => void cancelAlert(alert.id)}>
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

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#090b0f',
  },
  shell: {
    flex: 1,
    padding: 24,
    justifyContent: 'center',
    gap: 18,
  },
  scrollContent: {
    padding: 20,
    gap: 16,
  },
  heroGlow: {
    position: 'absolute',
    top: -40,
    right: -50,
    width: 180,
    height: 180,
    borderRadius: 999,
    backgroundColor: 'rgba(216, 180, 104, 0.14)',
  },
  eyebrow: {
    fontSize: 11,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: '#d8b468',
    marginBottom: 6,
  },
  title: {
    fontSize: 30,
    fontWeight: '800',
    color: '#f7f5ef',
  },
  copy: {
    fontSize: 15,
    lineHeight: 22,
    color: '#b7bec7',
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
  input: {
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
    borderRadius: 16,
    paddingHorizontal: 14,
    paddingVertical: 12,
    backgroundColor: 'rgba(255,255,255,0.03)',
    color: '#f7f5ef',
  },
  textArea: {
    minHeight: 88,
    textAlignVertical: 'top',
  },
  primaryButton: {
    borderRadius: 18,
    backgroundColor: '#d8b468',
    paddingVertical: 14,
    alignItems: 'center',
  },
  primaryButtonText: {
    color: '#221605',
    fontWeight: '800',
  },
  secondaryButton: {
    borderRadius: 16,
    backgroundColor: 'rgba(255,255,255,0.03)',
    borderWidth: 1,
    borderColor: 'rgba(216,180,104,0.14)',
    paddingHorizontal: 14,
    paddingVertical: 12,
  },
  secondaryButtonSmall: {
    alignSelf: 'flex-start',
    borderRadius: 14,
    backgroundColor: 'rgba(255,255,255,0.03)',
    borderWidth: 1,
    borderColor: 'rgba(216,180,104,0.14)',
    paddingHorizontal: 12,
    paddingVertical: 10,
  },
  secondaryButtonText: {
    color: '#f5f1e8',
    fontWeight: '700',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#f7f5ef',
  },
  meta: {
    color: '#9aa3ad',
  },
  body: {
    color: '#dbe1e8',
    lineHeight: 20,
  },
  errorText: {
    color: '#ffb7ab',
    fontWeight: '700',
  },
  hintCard: {
    padding: 16,
    borderRadius: 18,
    backgroundColor: '#10141b',
    borderWidth: 1,
    borderColor: 'rgba(216,180,104,0.12)',
  },
  hintTitle: {
    color: '#f7f5ef',
    fontWeight: '800',
    marginBottom: 6,
  },
  hintText: {
    color: '#9aa3ad',
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
  alertButtonLabel: {
    color: '#f7f5ef',
    fontWeight: '800',
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
})
