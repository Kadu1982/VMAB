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

type LoginForm = {
  residentId: string
  accessPin: string
}

type AlertDraft = {
  notes: string
  escortDestination: string
  coercionPin: string
}

const initialLoginForm: LoginForm = {
  residentId: '1',
  accessPin: '1122',
}

const initialAlertDraft: AlertDraft = {
  notes: '',
  escortDestination: '',
  coercionPin: '',
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
  const [countdownAlertType, setCountdownAlertType] = useState<ResidentAlertType | null>(null)
  const [countdownSeconds, setCountdownSeconds] = useState(0)
  const activeAlert = alerts.find((alert) => isActiveAlert(alert.status)) ?? null

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
    // Autentica o morador com PIN dedicado para nao tratar telefone como segredo.
    setLoading(true)
    setError(null)

    try {
      const response = await apiFetch('/api/resident-app/session', {
        method: 'POST',
        body: JSON.stringify({
          residentId: Number(loginForm.residentId),
          accessPin: loginForm.accessPin,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel entrar. Verifique o ID e o PIN do morador.')
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
          escortDestination: type === 'ESCOLTA' ? alertDraft.escortDestination : null,
          coercionPin: type === 'COACAO' ? alertDraft.coercionPin : null,
          latitude,
          longitude,
        }),
      })

      if (!response.ok) {
        throw new Error('Nao foi possivel abrir o alerta.')
      }

      setAlertDraft(initialAlertDraft)
      setCountdownAlertType(null)
      setCountdownSeconds(0)
      await refreshResidentData()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Falha inesperada ao abrir o alerta.')
    } finally {
      setSendingAlert(null)
    }
  }

  function beginAlertCountdown(type: ResidentAlertType) {
    // O disparo passa por janela curta de cancelamento para reduzir acionamento acidental.
    if (countdownAlertType === type) {
      setCountdownAlertType(null)
      setCountdownSeconds(0)
      return
    }

    setCountdownAlertType(type)
    setCountdownSeconds(5)
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

  async function handleLogout() {
    // Encerra a sessao no backend antes de limpar o estado local do aparelho.
    try {
      if (session?.accessToken) {
        await apiFetch('/api/resident-app/session/logout', {
          method: 'POST',
        })
      }
    } catch {
      // Mesmo que a sessao ja esteja expirada, o app precisa permitir a saida local.
    } finally {
      setSession(null)
      setProfile(null)
      setAlerts([])
      setError(null)
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
    }, activeAlert ? 8000 : 20000)

    return () => {
      clearInterval(intervalId)
    }
  }, [activeAlert, session?.accessToken])

  useEffect(() => {
    if (!countdownAlertType || countdownSeconds <= 0) {
      return
    }

    const timeoutId = setTimeout(() => {
      setCountdownSeconds((current) => current - 1)
    }, 1000)

    return () => {
      clearTimeout(timeoutId)
    }
  }, [countdownAlertType, countdownSeconds])

  useEffect(() => {
    if (!countdownAlertType || countdownSeconds !== 0) {
      return
    }

    void submitAlert(countdownAlertType)
  }, [countdownAlertType, countdownSeconds])

  if (!session) {
    // Tela de acesso enxuta para o morador iniciar o uso do app.
    return (
      <SafeAreaView style={styles.safeArea}>
        <StatusBar style="dark" />
        <View style={styles.shell}>
          <View style={styles.heroGlow} />
          <Text style={styles.eyebrow}>VMAB Morador</Text>
          <Text style={styles.title}>Alerta rápido. Controle real.</Text>
          <Text style={styles.copy}>
            Entre com o ID do morador e o PIN de acesso para abrir alertas, acompanhar status e falar com a central.
          </Text>

          {error ? <Text style={styles.errorText}>{error}</Text> : null}

          <View style={styles.card}>
            <TextInput
              autoCapitalize="none"
              placeholder="URL da API"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={apiBaseUrl}
              onChangeText={setApiBaseUrl}
            />
            <TextInput
              keyboardType="numeric"
              inputMode="numeric"
              placeholder="ID do morador"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={loginForm.residentId}
              onChangeText={(value) => setLoginForm((current) => ({ ...current, residentId: value.replace(/\D/g, '') }))}
            />
            <TextInput
              keyboardType="numeric"
              secureTextEntry
              inputMode="numeric"
              placeholder="PIN de acesso"
              placeholderTextColor="#8c8e92"
              style={styles.input}
              value={loginForm.accessPin}
              onChangeText={(value) => setLoginForm((current) => ({ ...current, accessPin: value.replace(/\D/g, '').slice(0, 6) }))}
            />
            <Pressable style={styles.primaryButton} onPress={() => void handleLogin()}>
              <Text style={styles.primaryButtonText}>{loading ? 'Entrando...' : 'Entrar'}</Text>
            </Pressable>
          </View>

          <View style={styles.hintCard}>
            <Text style={styles.hintTitle}>Exemplo de teste</Text>
            <Text style={styles.hintText}>ID: 1</Text>
            <Text style={styles.hintText}>PIN padrao local: 1122</Text>
          </View>
        </View>
      </SafeAreaView>
    )
  }

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar style="dark" />
      <ScrollView contentContainerStyle={styles.scrollContent}>
        <View style={styles.header}>
          <View>
            <Text style={styles.eyebrow}>Atendimento do morador</Text>
            <Text style={styles.title}>{profile?.fullName ?? session.fullName}</Text>
            <Text style={styles.copy}>{profile?.address ?? session.address}</Text>
          </View>
          <Pressable
            style={styles.secondaryButton}
            onPress={() => void handleLogout()}
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

        {activeAlert ? (
          <View style={styles.activeAlertCard}>
            <Text style={styles.sectionTitle}>Atendimento em andamento</Text>
            <Text style={styles.activeAlertTitle}>{activeAlert.silent ? 'Emergencia silenciosa' : translateAlertType(activeAlert.type)}</Text>
            <Text style={styles.body}>{getAlertOperationalMessage(activeAlert)}</Text>
            {activeAlert.escortDestination ? <Text style={styles.meta}>Destino da escolta: {activeAlert.escortDestination}</Text> : null}
            <Text style={styles.meta}>Ultima atualizacao: {formatDate(activeAlert.updatedAt)}</Text>
            {isActiveAlert(activeAlert.status) ? (
              <Pressable style={styles.secondaryButtonSmall} onPress={() => void cancelAlert(activeAlert.id)}>
                <Text style={styles.secondaryButtonText}>Cancelar alerta atual</Text>
              </Pressable>
            ) : null}
          </View>
        ) : null}

        <View style={styles.card}>
          <Text style={styles.sectionTitle}>Abrir alerta</Text>
          <Text style={styles.meta}>
            {activeAlert
              ? 'Ja existe um alerta em atendimento. Aguarde a central concluir ou cancele o alerta atual.'
              : 'A localizacao e opcional, mas ajuda a central a encurtar o atendimento.'}
          </Text>
          <TextInput
            multiline
            placeholder="Observacao do alerta"
            placeholderTextColor="#8c8e92"
            style={[styles.input, styles.textArea]}
            value={alertDraft.notes}
            onChangeText={(value) => setAlertDraft((current) => ({ ...current, notes: value }))}
          />
          <TextInput
            placeholder="Destino da escolta"
            placeholderTextColor="#8c8e92"
            style={styles.input}
            value={alertDraft.escortDestination}
            onChangeText={(value) => setAlertDraft((current) => ({ ...current, escortDestination: value }))}
          />
          <TextInput
            keyboardType="numeric"
            secureTextEntry
            inputMode="numeric"
            placeholder="PIN de coacao"
            placeholderTextColor="#8c8e92"
            style={styles.input}
            value={alertDraft.coercionPin}
            onChangeText={(value) => setAlertDraft((current) => ({ ...current, coercionPin: value.replace(/\D/g, '').slice(0, 6) }))}
          />
          {countdownAlertType ? <Text style={styles.meta}>Alerta {translateAlertType(countdownAlertType)} sera enviado em {countdownSeconds}s. Toque no mesmo botao para cancelar.</Text> : null}
          <View style={styles.alertGrid}>
            {(['PANICO', 'COACAO', 'ESCOLTA', 'SUSPEITA', 'MEDICA'] as ResidentAlertType[]).map((type) => (
              <Pressable key={type} disabled={Boolean(activeAlert) && countdownAlertType !== type} style={[styles.alertButton, Boolean(activeAlert) && countdownAlertType !== type ? styles.alertButtonDisabled : null]} onPress={() => beginAlertCountdown(type)}>
                <Text style={styles.alertButtonLabel}>
                  {sendingAlert === type
                    ? 'Enviando...'
                    : countdownAlertType === type
                      ? `Cancelar ${translateAlertType(type)}`
                      : translateAlertType(type)}
                </Text>
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
    backgroundColor: '#eef2f7',
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
    backgroundColor: 'rgba(200, 154, 69, 0.12)',
  },
  eyebrow: {
    fontSize: 11,
    letterSpacing: 2,
    textTransform: 'uppercase',
    color: '#a97422',
    marginBottom: 6,
  },
  title: {
    fontSize: 30,
    fontWeight: '800',
    color: '#15202b',
  },
  copy: {
    fontSize: 15,
    lineHeight: 22,
    color: '#556477',
  },
  card: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 10,
  },
  profileCard: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: '#f7fafc',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
    gap: 4,
  },
  activeAlertCard: {
    padding: 18,
    borderRadius: 22,
    backgroundColor: 'rgba(200,85,68,0.08)',
    borderWidth: 1,
    borderColor: 'rgba(200,85,68,0.18)',
    gap: 8,
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
  },
  primaryButtonText: {
    color: '#1f1508',
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
    borderRadius: 14,
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
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    gap: 12,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '800',
    color: '#15202b',
  },
  meta: {
    color: '#6d7a89',
  },
  body: {
    color: '#2c3a48',
    lineHeight: 20,
  },
  errorText: {
    color: '#c85544',
    fontWeight: '700',
  },
  hintCard: {
    padding: 16,
    borderRadius: 18,
    backgroundColor: '#ffffff',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.1)',
  },
  hintTitle: {
    color: '#15202b',
    fontWeight: '800',
    marginBottom: 6,
  },
  hintText: {
    color: '#6d7a89',
  },
  alertGrid: {
    gap: 10,
  },
  alertButton: {
    borderRadius: 16,
    paddingVertical: 14,
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
  },
  alertItem: {
    padding: 14,
    borderRadius: 18,
    backgroundColor: '#f7fafc',
    borderWidth: 1,
    borderColor: 'rgba(42,57,75,0.08)',
    gap: 6,
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
  },
  statusPill: {
    color: '#a97422',
    fontWeight: '800',
  },
})
