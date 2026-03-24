import { render, screen, waitFor } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import App from '../App'

const storageKey = 'seguranca-auth'

function buildSession() {
  return {
    accessToken: 'token-antigo',
    tokenType: 'Bearer',
    expiresAt: '2026-03-23T18:00:00.000Z',
    username: 'admin',
    roles: ['ROLE_ADMIN'],
  }
}

describe('App', () => {
  it('mostra a tela de login com os rótulos principais em pt-BR', () => {
    // Essa regressao protege a porta de entrada do sistema, que nao pode quebrar silenciosamente.
    render(<App />)

    expect(screen.getByRole('heading', { name: 'Seguranca Comunitaria' })).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Usuario')).toBeInTheDocument()
    expect(screen.getByPlaceholderText('Senha')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('descarta sessao local invalida e volta para a tela de login', async () => {
    // Esse e o caso que ja quebrou na pratica: token antigo salvo no navegador e 401 na validacao.
    window.localStorage.setItem(storageKey, JSON.stringify(buildSession()))
    vi.mocked(fetch).mockResolvedValueOnce({
      ok: false,
      status: 401,
      json: async () => ({ message: 'nao autorizado' }),
    } as Response)

    render(<App />)

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Seguranca Comunitaria' })).toBeInTheDocument()
    })

    expect(screen.getByText('Sua sessao expirou ou as credenciais sao invalidas.')).toBeInTheDocument()
    expect(window.localStorage.getItem(storageKey)).toBeNull()
  })
})
