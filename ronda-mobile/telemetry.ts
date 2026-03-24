import AsyncStorage from '@react-native-async-storage/async-storage'
import * as Location from 'expo-location'
import * as TaskManager from 'expo-task-manager'

export const BACKGROUND_LOCATION_TASK_NAME = 'vmab-background-location'
export const API_URL_STORAGE_KEY = 'seguranca-api-url'
export const CREDENTIALS_STORAGE_KEY = 'seguranca-ronda-creds'
export const SESSION_STORAGE_KEY = 'seguranca-ronda-session'
export const ACTIVE_SHIFT_STORAGE_KEY = 'seguranca-ronda-active-shift'
export const TELEMETRY_QUEUE_STORAGE_KEY = 'seguranca-ronda-telemetry-queue'
export const LAST_TELEMETRY_SNAPSHOT_STORAGE_KEY = 'seguranca-ronda-last-telemetry-snapshot'

export type TelemetrySource = 'foreground' | 'background'

export type TelemetrySignalCode =
  | 'GPS_IMPRECISO'
  | 'VELOCIDADE_SUSPEITA'
  | 'SALTO_LOCALIZACAO'
  | 'GPS_TRAVADO'

export type TelemetrySignal = {
  code: TelemetrySignalCode
  label: string
  detail: string
}

export type TelemetrySample = {
  shiftId: number
  latitude: number
  longitude: number
  speedKmh: number
  accuracyMeters: number
  headingDegrees: number | null
  recordedAt: string
  source: TelemetrySource
}

export type TelemetryQueueItem = {
  id: string
  sample: TelemetrySample
  signals: TelemetrySignal[]
  reason: string
  createdAt: string
  attempts: number
  lastAttemptAt?: string | null
}

export type StoredTelemetrySnapshot = TelemetrySample

export type StoredSession = {
  accessToken: string
}

function nowIso() {
  return new Date().toISOString()
}

function safeParseJson<T>(value: string | null) {
  if (!value) return null

  try {
    return JSON.parse(value) as T
  } catch {
    return null
  }
}

export function normalizeApiBaseUrl(value: string) {
  return value.trim().replace(/\/+$/, '')
}

export function formatTelemetrySignalSummary(signals: TelemetrySignal[]) {
  if (!signals.length) {
    return 'Sem anomalias detectadas.'
  }

  return signals.map((signal) => signal.label).join(' | ')
}

export function createTelemetrySample(
  location: Location.LocationObject,
  shiftId: number,
  source: TelemetrySource,
): TelemetrySample {
  return {
    shiftId,
    latitude: location.coords.latitude,
    longitude: location.coords.longitude,
    speedKmh: Math.max(0, (location.coords.speed ?? 0) * 3.6),
    accuracyMeters: Math.max(0, location.coords.accuracy ?? 0),
    headingDegrees:
      location.coords.heading != null && location.coords.heading >= 0 ? location.coords.heading : null,
    recordedAt: new Date(location.timestamp).toISOString(),
    source,
  }
}

function haversineDistanceMeters(
  startLatitude: number,
  startLongitude: number,
  endLatitude: number,
  endLongitude: number,
) {
  const earthRadiusMeters = 6_371_000
  const deltaLatitude = ((endLatitude - startLatitude) * Math.PI) / 180
  const deltaLongitude = ((endLongitude - startLongitude) * Math.PI) / 180
  const startLatitudeRad = (startLatitude * Math.PI) / 180
  const endLatitudeRad = (endLatitude * Math.PI) / 180

  const a =
    Math.sin(deltaLatitude / 2) ** 2 +
    Math.cos(startLatitudeRad) * Math.cos(endLatitudeRad) * Math.sin(deltaLongitude / 2) ** 2

  return 2 * earthRadiusMeters * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
}

export function evaluateTelemetrySignals(
  sample: TelemetrySample,
  previousSnapshot: StoredTelemetrySnapshot | null,
) {
  const signals: TelemetrySignal[] = []

  // Sinal simples de qualidade de GPS: precisão muito ruim costuma exigir atenção operacional.
  if (sample.accuracyMeters >= 120) {
    signals.push({
      code: 'GPS_IMPRECISO',
      label: 'GPS com baixa precisão',
      detail: `A precisão estimada do sinal ficou em ${Math.round(sample.accuracyMeters)} m.`,
    })
  }

  // Sinal de velocidade fora do padrão operacional esperado para patrulha de viatura.
  if (sample.speedKmh >= 130) {
    signals.push({
      code: 'VELOCIDADE_SUSPEITA',
      label: 'Velocidade fora do padrão',
      detail: `O ponto atual registrou cerca de ${Math.round(sample.speedKmh)} km/h.`,
    })
  }

  if (previousSnapshot) {
    const currentTime = new Date(sample.recordedAt).getTime()
    const previousTime = new Date(previousSnapshot.recordedAt).getTime()
    const elapsedSeconds = Math.max(1, Math.round((currentTime - previousTime) / 1000))
    const distanceMeters = haversineDistanceMeters(
      previousSnapshot.latitude,
      previousSnapshot.longitude,
      sample.latitude,
      sample.longitude,
    )
    const impliedSpeedKmh = distanceMeters / ((currentTime - previousTime) / 3_600_000 || 1)

    // Salto geográfico grande demais em pouco tempo costuma indicar GPS estranho ou coordenada falsa.
    if (distanceMeters >= 500 && impliedSpeedKmh >= 160) {
      signals.push({
        code: 'SALTO_LOCALIZACAO',
        label: 'Salto anormal de localização',
        detail: `Houve deslocamento de ${Math.round(distanceMeters)} m em cerca de ${elapsedSeconds} s.`,
      })
    }

    // Coordenada presa por muito tempo enquanto o veículo aparenta estar em movimento é um sinal fraco de fraude.
    if (distanceMeters <= 8 && elapsedSeconds >= 45 && sample.speedKmh >= 10) {
      signals.push({
        code: 'GPS_TRAVADO',
        label: 'Possível GPS travado',
        detail: 'As coordenadas quase não mudaram apesar de a viatura indicar movimento.',
      })
    }
  }

  return signals
}

export async function loadStoredActiveShiftId() {
  const value = await AsyncStorage.getItem(ACTIVE_SHIFT_STORAGE_KEY)
  if (!value) return null

  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
}

export async function saveStoredActiveShiftId(shiftId: number | null) {
  if (!shiftId) {
    await AsyncStorage.removeItem(ACTIVE_SHIFT_STORAGE_KEY)
    return
  }

  await AsyncStorage.setItem(ACTIVE_SHIFT_STORAGE_KEY, String(shiftId))
}

export async function loadStoredSession() {
  return safeParseJson<StoredSession>(await AsyncStorage.getItem(SESSION_STORAGE_KEY))
}

export async function loadStoredTelemetrySnapshot() {
  return safeParseJson<StoredTelemetrySnapshot>(
    await AsyncStorage.getItem(LAST_TELEMETRY_SNAPSHOT_STORAGE_KEY),
  )
}

export async function saveStoredTelemetrySnapshot(snapshot: StoredTelemetrySnapshot | null) {
  if (!snapshot) {
    await AsyncStorage.removeItem(LAST_TELEMETRY_SNAPSHOT_STORAGE_KEY)
    return
  }

  await AsyncStorage.setItem(LAST_TELEMETRY_SNAPSHOT_STORAGE_KEY, JSON.stringify(snapshot))
}

export async function loadTelemetryQueue() {
  return safeParseJson<TelemetryQueueItem[]>(await AsyncStorage.getItem(TELEMETRY_QUEUE_STORAGE_KEY)) ?? []
}

export async function saveTelemetryQueue(queue: TelemetryQueueItem[]) {
  await AsyncStorage.setItem(TELEMETRY_QUEUE_STORAGE_KEY, JSON.stringify(queue))
}

export async function appendTelemetryQueueItem(
  sample: TelemetrySample,
  signals: TelemetrySignal[],
  reason: string,
) {
  const currentQueue = await loadTelemetryQueue()
  const nextItem: TelemetryQueueItem = {
    id: `${Date.now()}-${Math.random().toString(16).slice(2)}`,
    sample,
    signals,
    reason,
    createdAt: nowIso(),
    attempts: 0,
  }

  const nextQueue = [...currentQueue, nextItem]
  await saveTelemetryQueue(nextQueue)
  return nextQueue
}

export async function postTelemetrySample(
  baseUrl: string,
  accessToken: string,
  sample: TelemetrySample,
) {
  const response = await fetch(`${normalizeApiBaseUrl(baseUrl)}/api/shifts/${sample.shiftId}/telemetry`, {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      latitude: sample.latitude,
      longitude: sample.longitude,
      speedKmh: sample.speedKmh,
      accuracyMeters: sample.accuracyMeters,
      headingDegrees: sample.headingDegrees,
      recordedAt: sample.recordedAt,
    }),
  })

  if (!response.ok) {
    throw new Error(`Falha ao enviar telemetria (${response.status}).`)
  }
}

export async function flushTelemetryQueue(baseUrl: string, accessToken: string) {
  const queue = await loadTelemetryQueue()
  if (!queue.length) {
    return { sent: 0, remaining: 0 }
  }

  const remaining: TelemetryQueueItem[] = []
  let sent = 0

  // A fila e processada do ponto mais antigo para o mais novo para nao perder a ordem da patrulha.
  for (const item of queue) {
    try {
      await postTelemetrySample(baseUrl, accessToken, item.sample)
      sent += 1
      await saveStoredTelemetrySnapshot(item.sample)
    } catch {
      remaining.push({
        ...item,
        attempts: item.attempts + 1,
        lastAttemptAt: nowIso(),
      })
      break
    }
  }

  for (const item of queue.slice(remaining.length + sent)) {
    remaining.push(item)
  }

  await saveTelemetryQueue(remaining)

  return { sent, remaining: remaining.length }
}

export async function sendTelemetrySample(
  baseUrl: string,
  accessToken: string,
  sample: TelemetrySample,
) {
  await postTelemetrySample(baseUrl, accessToken, sample)
  await saveStoredTelemetrySnapshot(sample)
}

export async function ensureBackgroundTrackingForShift(shiftId: number) {
  // O background tracking só funciona com permissao em primeiro plano e em segundo plano.
  const foregroundPermission = await Location.requestForegroundPermissionsAsync()
  if (foregroundPermission.status !== 'granted') {
    return {
      started: false,
      message: 'Permissao de localizacao em primeiro plano negada.',
    }
  }

  const backgroundPermission = await Location.requestBackgroundPermissionsAsync()
  if (backgroundPermission.status !== 'granted') {
    return {
      started: false,
      message: 'Permissao de localizacao em segundo plano negada.',
    }
  }

  const alreadyStarted = await Location.hasStartedLocationUpdatesAsync(BACKGROUND_LOCATION_TASK_NAME)
  if (!alreadyStarted) {
    await Location.startLocationUpdatesAsync(BACKGROUND_LOCATION_TASK_NAME, {
      accuracy: Location.Accuracy.Balanced,
      timeInterval: 15_000,
      distanceInterval: 20,
      pausesUpdatesAutomatically: false,
      foregroundService: {
        notificationTitle: 'VMAB em ronda',
        notificationBody: 'Rastreamento em segundo plano ativo.',
        notificationColor: '#d3a24a',
      },
    })
  }

  await saveStoredActiveShiftId(shiftId)

  return {
    started: true,
    message: alreadyStarted
      ? 'Rastreamento em segundo plano já estava ativo.'
      : 'Rastreamento em segundo plano ativado.',
  }
}

export async function stopBackgroundTracking() {
  const alreadyStarted = await Location.hasStartedLocationUpdatesAsync(BACKGROUND_LOCATION_TASK_NAME)
  if (alreadyStarted) {
    await Location.stopLocationUpdatesAsync(BACKGROUND_LOCATION_TASK_NAME)
  }

  await saveStoredActiveShiftId(null)
}

if (!TaskManager.isTaskDefined(BACKGROUND_LOCATION_TASK_NAME)) {
  // A task roda fora da tela para manter a telemetria viva mesmo quando o app fica em segundo plano.
  TaskManager.defineTask(BACKGROUND_LOCATION_TASK_NAME, async ({ data, error }) => {
    if (error) {
      return
    }

    // O pacote nao expoe um tipo util para o payload do background nesta versao, entao usamos uma forma estrutural.
    const taskData = data as { locations?: Location.LocationObject[] } | undefined
    const location = taskData?.locations?.[0]
    if (!location) {
      return
    }

    const shiftId = await loadStoredActiveShiftId()
    if (!shiftId) {
      return
    }

    const payload = createTelemetrySample(location, shiftId, 'background')
    const previousSnapshot = await loadStoredTelemetrySnapshot()
    const signals = evaluateTelemetrySignals(payload, previousSnapshot)
    await saveStoredTelemetrySnapshot(payload)

    const baseUrl = normalizeApiBaseUrl((await AsyncStorage.getItem(API_URL_STORAGE_KEY)) ?? '')
    const session = await loadStoredSession()

    if (!baseUrl || !session?.accessToken) {
      await appendTelemetryQueueItem(payload, signals, 'Configuração ou sessão indisponível no background.')
      return
    }

    try {
      await postTelemetrySample(baseUrl, session.accessToken, payload)
    } catch (cause) {
      await appendTelemetryQueueItem(
        payload,
        signals,
        cause instanceof Error ? cause.message : 'Falha ao enviar telemetria em background.',
      )
    }
  })
}
