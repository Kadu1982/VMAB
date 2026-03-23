# Plataforma de Seguranca Comunitaria

Base executavel do produto de seguranca comunitaria com backend Spring Boot, painel web React, app mobile da ronda e infraestrutura Docker para desenvolvimento e deploy.

## Modulos atuais
- `backend`: API REST com Spring Boot, Spring Security, JPA, Flyway e PostgreSQL.
- `dashboard-web`: painel web para operacao administrativa e acompanhamento.
- `ronda-mobile`: app Expo para a equipe de ronda.
- `mvp-estatico`: prototipo visual preservado como referencia.

## Funcionalidades entregues nesta base
- autenticacao basica com perfis `admin`, `supervisor`, `cliente` e `ronda`;
- dashboard operacional com resumo de agentes, viaturas, turnos e ocorrencias;
- CRUD de agentes e viaturas para perfis administrativos;
- CRUD de turnos e ocorrencias com atualizacao operacional;
- portal do cliente em modo somente leitura;
- persistencia com PostgreSQL via Docker;
- seed inicial para ambiente de teste.

## Credenciais de teste
- `admin / admin123`
- `supervisor / super123`
- `cliente / cliente123`
- `ronda / ronda123`

## Como subir tudo com Docker
Na raiz do projeto:

```powershell
docker compose up --build
```

Servicos publicados:
- painel web: `http://localhost:3000`
- backend: `http://localhost:8091`
- postgres: `localhost:5432`

## Como rodar sem Docker
### Banco
```powershell
docker compose up -d postgres
```

### Backend
```powershell
cd backend
.\gradlew.bat bootRun
```

### Painel web
```powershell
cd dashboard-web
npm install
npm run dev
```

Se precisar apontar para outro backend:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

### App mobile da ronda
```powershell
cd ronda-mobile
npm install
npm run start
```

Se o app estiver rodando em celular fisico, use o IP local da sua maquina em vez de `localhost`:

```powershell
$env:EXPO_PUBLIC_API_BASE_URL="http://192.168.0.10:8091"
npm run start
```

Observacao:
- em emulador Android, `localhost` deve ser trocado pelo IP da maquina hospedeira.
- em celular fisico, o telefone e a maquina do backend precisam estar na mesma rede.
- a telemetria GPS da viatura e enviada para `POST /api/shifts/{shiftId}/telemetry`.
- o dashboard web passa a ler a ultima posicao real pelo `summary.activePatrol`.
- o rastreamento atual e em foreground; background tracking pode entrar na proxima fase.

## Como subir em VPS com dominio e HTTPS real
Arquivos preparados:
- [compose.prod.yml](C:\Users\G15\Documents\Segurança\compose.prod.yml)
- [.env.production.example](C:\Users\G15\Documents\Segurança\.env.production.example)
- [Caddyfile](C:\Users\G15\Documents\Segurança\infra\caddy\Caddyfile)

Passos:
1. Aponte dois DNS para a VPS:
   - `painel.axiumsistemas.com`
   - `api.axiumsistemas.com`
2. Copie `.env.production.example` para `.env.production` e ajuste dominios, email e senha do Postgres.
3. Na VPS, suba com:

```powershell
docker compose --env-file .env.production -f compose.prod.yml up -d --build
```

Resultado esperado:
- painel web em `https://painel.axiumsistemas.com`
- backend em `https://api.axiumsistemas.com`

No `ronda-mobile`, use a URL fixa da API:

```text
https://api.axiumsistemas.com
```

Isso elimina a dependencia de `loca.lt` e de IP local para o app da ronda.

## Validacoes feitas
- `npm run build` em `dashboard-web`
- `.\gradlew.bat compileJava bootJar -x test` em `backend`
- `npx tsc --noEmit` em `ronda-mobile`

## Proximos blocos naturais
- autenticar usuarios reais via banco e tokens;
- portal do cliente com filtros e relatorios;
- app do morador;
- anexos, evidencias e auditoria detalhada;
- antifraude, offline e observabilidade.
