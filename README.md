# VMAB

Plataforma operacional de segurança comunitaria para empresas de segurança, condomínios, loteamentos, bairros monitorados e associacoes de moradores.

O projeto foi desenhado para resolver um problema real: operacoes de ronda e atendimento local normalmente funcionam com excesso de improviso, pouca rastreabilidade, baixa prova de execucao e dependencia de canais informais. O VMAB organiza essa operação de ponta a ponta com backoffice web, app mobile unificado, telemetria GPS, controle de equipe, frota e ocorrências.

## O que o VMAB entrega

O sistema conecta os principais atores da operação:
- morador;
- equipe de ronda;
- supervisor;
- gestor da operação;
- cliente contratante;
- viaturas e frota.

Na pratica, isso significa:
- acionamento mais rapido e estruturado;
- despacho operacional com contexto;
- rastreamento da viatura em tempo real;
- registro auditavel de quem estava em ronda, com qual viatura e em qual turno;
- controle de quilometragem, uso da frota e manutenção;
- indicadores para supervisao e prova de servico para o cliente.

## Por que isso e valioso para quem contrata

O VMAB não e apenas um app. Ele foi pensado para ser um investimento operacional e comercial para a empresa de segurança.

### Beneficios diretos
- reduz tempo de resposta e melhora o controle da operação;
- reduz dependencia de WhatsApp, telefone e registros dispersos;
- aumenta a capacidade de provar servico executado para o cliente;
- melhora a gestao de equipe, troca de turno e responsabilidade;
- cria visibilidade sobre uso de viaturas, quilometragem e manutenção;
- gera historico operacional para auditoria, renovacao contratual e crescimento comercial.

### Beneficios estrategicos
- fortalece a percepcao de profissionalismo da empresa contratada;
- ajuda a reter contratos com relatorios, trilha de execucao e SLA;
- cria base para operação multi-base e multi-cliente;
- reduz risco operacional causado por falhas de processo;
- prepara o negocio para escalar com padronizacao.

## Modulos da plataforma

### 1. Dashboard web operacional
Painel para administracao, supervisao e acompanhamento da operação.

Responsabilidades:
- login por perfil;
- visao geral de agentes, moradores, viaturas, turnos e ocorrências;
- acompanhamento da patrulha ativa;
- visualizacao da localizacao da viatura em mapa real;
- leitura da trilha percorrida por GPS;
- gestao operacional por perfis `admin`, `supervisor` e `cliente`.

### 2. App mobile unificado
Aplicativo oficial usado pelo vigilante em servico e pelo morador.

Responsabilidades:
- selecao de perfil no login (`Morador` ou `Colaborador`);
- login operacional e login do morador com PIN dedicado;
- configuracao persistente da URL da API;
- envio de localizacao GPS em tempo real no perfil operacional;
- associacao da telemetria ao turno em execucao;
- despacho, chegada e encerramento de ocorrências pelo celular da ronda;
- abertura, acompanhamento e cancelamento de alertas do morador;
- notificacoes push Expo do atendimento do morador;
- notificacoes push Expo operacionais para o colaborador em mudancas relevantes da fila de ocorrências;
- base para operação de atendimento, deslocamento e patrulha.

### 3. Backend operacional
API central da plataforma.

Responsabilidades:
- autenticacao e controle de acesso;
- CRUD de agentes, moradores, viaturas, turnos e ocorrências;
- persistencia com PostgreSQL;
- historico de telemetria por turno;
- resumo operacional do dashboard;
- portal do cliente em modo leitura.

### 4. Infraestrutura Docker
Estrutura pronta para ambiente local e producao.

Responsabilidades:
- stack de desenvolvimento com banco, backend e frontend;
- stack de producao com proxy reverso e HTTPS;
- preparo para deploy em VPS com dominio real.

## Funcionalidades entregues na base atual

- autenticacao basica com perfis `admin`, `supervisor`, `cliente` e `ronda`;
- cadastro e gestao de agentes;
- cadastro e gestao de moradores;
- cadastro e gestao de viaturas;
- cadastro e gestao de turnos;
- cadastro e gestao de ocorrências;
- dashboard com indicadores operacionais;
- patrulha ativa com mapa real via OpenStreetMap e Leaflet;
- exibicao de foto do vigilante, dados da viatura e telemetria atual;
- historico da trilha percorrida por GPS;
- app mobile unico com selecao de perfil entre morador e colaborador;
- captura de localizacao no perfil operacional;
- persistencia de URL da API, sessoes e credenciais por perfil no app mobile;
- fila operacional de ocorrências com despacho, chegada e encerramento no perfil colaborador;
- fluxo do morador com PIN dedicado, alerta ativo unico e push Expo;
- centro de eventos em tempo real no painel web para operação e cliente;
- portal do cliente com leitura da operação;
- banco PostgreSQL com Flyway;
- stack Docker para ambiente local;
- stack de producao preparada para VPS com HTTPS.

## Estrutura do repositorio

- [backend](C:\Users\G15\Documents\Segurança\backend): API Spring Boot com segurança, regras operacionais e persistencia
- [dashboard-web](C:\Users\G15\Documents\Segurança\dashboard-web): painel React/Vite para operação e supervisao
- [ronda-mobile](C:\Users\G15\Documents\Segurança\ronda-mobile): app Expo/React Native oficial com perfis de morador e colaborador
- [resident-mobile](C:\Users\G15\Documents\Segurança\resident-mobile): app legado congelado, mantido apenas como referencia tecnica do fluxo antigo do morador
- [mvp-estatico](C:\Users\G15\Documents\Segurança\mvp-estatico): prototipo visual de referencia
- [compose.yml](C:\Users\G15\Documents\Segurança\compose.yml): stack local
- [compose.prod.yml](C:\Users\G15\Documents\Segurança\compose.prod.yml): stack de producao
- [DEPLOY_VPS.md](C:\Users\G15\Documents\Segurança\DEPLOY_VPS.md): instrucoes de deploy em VPS
- [PRD_Segurança_Comunitaria_v3.md](C:\Users\G15\Documents\Segurança\PRD_Segurança_Comunitaria_v3.md): escopo consolidado do produto

## Credenciais de teste

- `admin / admin123`
- `supervisor / super123`
- `cliente / cliente123`
- `ronda / ronda123`

Essas credenciais servem apenas para ambiente de desenvolvimento e demonstracao. Em producao, isso precisa ser substituido por autenticacao real com usuários persistidos, politicas de senha, revogacao e tokenizacao adequada.

## Como rodar localmente com Docker

Na raiz do projeto:

```powershell
docker compose up --build
```

Servicos publicados:
- painel web: `http://localhost:3000`
- backend: `http://localhost:8091`
- postgres: `localhost:5432`

## Como rodar por modulo

### 1. Subir apenas o banco

```powershell
docker compose up -d postgres
```

### 2. Backend

```powershell
cd backend
.\gradlew.bat bootRun
```

### 3. Dashboard web

```powershell
cd dashboard-web
npm install
npm run dev
```

Se precisar apontar para outro backend:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8091"
```

### 4. App mobile unificado

```powershell
cd ronda-mobile
npm install
npm run start
```

Se o app for testado em celular fisico:

```powershell
$env:EXPO_PUBLIC_API_BASE_URL="http://IP_DA_SUA_MAQUINA:8091"
npm run start
```

Observacoes objetivas:
- `8082` ou outra porta do Expo não e a porta da API;
- a API da aplicacao roda em `8091`;
- se o celular não alcancar o backend na rede local, use uma URL HTTPS publica ou tunel;
- o perfil de colaborador depende de permissao de localizacao para enviar telemetria;
- o perfil de morador usa PIN dedicado e registra push Expo para receber atualizacoes do atendimento.

## Deploy em VPS com dominio e HTTPS

O projeto ja esta preparado para deploy com dominio real.

Arquivos principais:
- [compose.prod.yml](C:\Users\G15\Documents\Segurança\compose.prod.yml)
- [.env.production.example](C:\Users\G15\Documents\Segurança\.env.production.example)
- [Caddyfile](C:\Users\G15\Documents\Segurança\infra\caddy\Caddyfile)

Observacao para esta VPS:
- o VMAB não deve subir um Caddy proprio se `saude_nginx` ja estiver usando `80/443`;
- os servicos `backend` e `dashboard-web` devem entrar na rede compartilhada `saude_saude_network`;
- os aliases esperados sao `vmab-backend` e `vmab-dashboard`.
- o banco do VMAB deve ser acessado por `vmab-postgres`, não por `postgres`, para não colidir com a outra stack.

Fluxo de producao esperado:
- `painel.axiumsistemas.com` para o frontend;
- `api.axiumsistemas.com` para o backend.

Passos:
1. apontar os registros DNS para o IP da VPS;
2. copiar `.env.production.example` para `.env.production`;
3. ajustar dominio, email e credenciais;
4. subir a stack de producao:

```powershell
docker compose --env-file .env.production -f compose.prod.yml up -d --build
```

Com isso:
- o painel passa a responder em `https://painel.axiumsistemas.com`
- a API passa a responder em `https://api.axiumsistemas.com`
- o app mobile pode usar uma URL fixa e estavel

## Usabilidade e eficiencia operacional

O valor do VMAB esta em transformar uma operação informal em uma operação controlada.

### Ganhos de usabilidade
- menos friccao para a equipe em campo;
- painel centralizado para decisao rapida;
- leitura clara do status da operação;
- visualizacao direta de quem esta em ronda, em qual viatura e em qual posicao;
- organizacao melhor da informação para supervisores e gestores.

### Ganhos de eficiencia
- menos perda de contexto entre abertura e atendimento;
- mais capacidade de supervisao em tempo real;
- mais controle sobre frota e quilometragem;
- mais consistencia em turnos, ocorrências e execucao de patrulha;
- mais rastreabilidade para analisar falhas e corrigir processo.

## Estado atual do produto

O projeto ja e uma base executavel real e demonstravel, mas ainda não esta totalmente finalizado para operação comercial plena.

O que ja existe:
- base full stack funcional;
- mapa real com telemetria GPS;
- backend persistente;
- dashboard operacional;
- app mobile unico funcional para morador e colaborador;
- preparo para deploy em VPS.

O que ainda falta para fechamento forte de producao:
- autenticacao real com usuários no banco e tokens;
- background tracking robusto no mobile do colaborador;
- auditoria e evidências mais profundas;
- antifraude operacional mais forte;
- relatorios executivos mais completos;
- observabilidade, logs e monitoramento;
- endurecimento de segurança e políticas de dados.
## Validacoes feitas

- `npm run build` em [dashboard-web](C:\Users\G15\Documents\Segurança\dashboard-web)
- `.\gradlew.bat compileJava bootJar -x test` em [backend](C:\Users\G15\Documents\Segurança\backend)
- `npx tsc --noEmit` em [ronda-mobile](C:\Users\G15\Documents\Segurança\ronda-mobile)
- `docker compose config` nas stacks locais e de producao

## Observabilidade e monitoramento

O backend agora expõe sinais úteis para operação e produção:
- `GET /actuator/health` para verificação simples de disponibilidade;
- `GET /actuator/health/readiness` e `GET /actuator/health/liveness` para probes de container;
- `GET /actuator/info` para metadados básicos da aplicação;
- `GET /actuator/metrics` e `GET /actuator/prometheus` para integração com monitoramento externo;
- `GET /actuator/loggers` para ajuste administrativo de níveis de log.

Os logs do backend carregam:
- `X-Correlation-Id` na resposta;
- método, rota, usuário autenticado, IP de origem, status e duração no contexto de log;
- persistência em arquivo no container em `/app/logs/vmab.log`, além da saída no console.

## Proximos passos recomendados

1. publicar o projeto na VPS com dominio fixo e HTTPS real;
2. substituir credenciais fixas por autenticacao real;
3. finalizar o fluxo operacional da ronda com mais automacao de turno;
4. ampliar relatorios e portal do cliente;
5. fechar requisitos de segurança e operação em background;
6. preparar o deploy final na VPS com dominio fixo e HTTPS.

## Licenciamento e uso

Este repositorio representa a base do produto VMAB. O valor dele não esta apenas no software, mas na capacidade de estruturar uma operação de segurança local com mais controle, prova, padronizacao e escalabilidade.

