import '@testing-library/jest-dom/vitest'
import { afterEach, beforeAll, vi } from 'vitest'
import { cleanup } from '@testing-library/react'

class MockEventSource {
  onopen: ((event: Event) => void) | null = null
  onmessage: ((event: MessageEvent) => void) | null = null
  onerror: ((event: Event) => void) | null = null
  readonly url: string

  constructor(url: string) {
    this.url = url
  }

  addEventListener() {
    // O componente so precisa que o contrato exista durante os testes.
  }

  removeEventListener() {
    // A implementacao real nao e exercitada no ambiente de teste.
  }

  close() {
    // Fecha o stream simulado sem efeitos colaterais.
  }
}

beforeAll(() => {
  // Garante que o frontend de teste tenha o mesmo contrato minimo do navegador real.
  vi.stubGlobal('EventSource', MockEventSource)
  vi.stubGlobal('fetch', vi.fn())
})

afterEach(() => {
  cleanup()
  window.localStorage.clear()
  vi.clearAllMocks()
})
