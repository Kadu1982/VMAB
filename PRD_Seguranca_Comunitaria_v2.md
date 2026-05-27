# PRD - Sistema de SeguranÃ§a ComunitÃ¡ria Colaborativa

## 1. Resumo Executivo
Plataforma digital de seguranÃ§a colaborativa para bairros e condomÃ­nios abertos, conectando moradores, equipe de ronda privada e a rede de VizinhanÃ§a SolidÃ¡ria da PolÃ­cia Militar do Estado de SÃ£o Paulo. O produto tem foco em prevenÃ§Ã£o, resposta rÃ¡pida, triagem humana responsÃ¡vel e padronizaÃ§Ã£o operacional, evitando acionamentos indevidos ao 190 e elevando a qualidade do atendimento local.

O sistema serÃ¡ composto por:
- aplicativo mobile multiplataforma para moradores e equipe de ronda;
- painel web administrativo para tutores/gestores;
- backend transacional com suporte a geolocalizaÃ§Ã£o, filas operacionais, eventos em tempo real e auditoria.

## 2. Problema
Hoje, bairros com vigilÃ¢ncia privada e iniciativas comunitÃ¡rias de seguranÃ§a enfrentam:
- demora no despacho da ronda por falta de contexto e localizaÃ§Ã£o precisa;
- alto volume de comunicaÃ§Ã£o informal em grupos de mensagem;
- dificuldade para priorizar ocorrÃªncias concorrentes;
- ausÃªncia de trilha operacional confiÃ¡vel para SLA e auditoria;
- risco de falsos positivos e perda de credibilidade junto Ã  seguranÃ§a pÃºblica.

## 3. Objetivos do Produto
### 3.1 Objetivos de NegÃ³cio
- reduzir o tempo mÃ©dio de resposta da ronda;
- organizar o fluxo operacional do bairro;
- reduzir falsos acionamentos;
- aumentar a percepÃ§Ã£o de seguranÃ§a dos moradores;
- gerar indicadores para melhoria contÃ­nua da operaÃ§Ã£o.

### 3.2 Objetivos do UsuÃ¡rio
- permitir que o morador solicite ajuda com poucos toques;
- mostrar transparÃªncia sobre o atendimento em andamento;
- permitir que a ronda atenda com contexto, localizaÃ§Ã£o e prioridade;
- dar ao tutor visibilidade da operaÃ§Ã£o, cadastro, cobertura e desempenho.

## 4. Perfis de UsuÃ¡rio
### 4.1 Morador
- aciona alertas e pedidos de escolta;
- acompanha o deslocamento da viatura;
- recebe atualizaÃ§Ãµes da ocorrÃªncia;
- visualiza histÃ³rico bÃ¡sico e comunicados.

### 4.2 Ronda Privada
- recebe chamados em tempo real com prioridade;
- navega atÃ© o local da ocorrÃªncia;
- atualiza status do atendimento;
- registra entrada e saÃ­da de turno via ponto eletrÃ´nico;
- realiza troca de funcionÃ¡rio com passagem formal de responsabilidade;
- vincula agente, viatura e quilometragem no inÃ­cio e no fim do turno;
- aciona escalonamento humano para a rede de VizinhanÃ§a SolidÃ¡ria quando necessÃ¡rio.

### 4.3 Tutor / Administrador
- gerencia moradores, viaturas e operadores;
- mantÃ©m cadastro de funcionÃ¡rios com foto, documentos e habilitaÃ§Ã£o;
- acompanha trocas de turno, ponto eletrÃ´nico e histÃ³rico de jornada;
- controla quilometragem da frota e agenda de manutenÃ§Ã£o;
- define perÃ­metro de atendimento;
- acompanha ocorrÃªncias em andamento;
- extrai relatÃ³rios operacionais e indicadores.

## 5. Proposta de Valor
- resposta local mais rÃ¡pida do que canais informais;
- operaÃ§Ã£o padronizada e auditÃ¡vel;
- menor ruÃ­do operacional;
- uso responsÃ¡vel de recursos pÃºblicos, sem integraÃ§Ã£o automÃ¡tica ao 190;
- inteligÃªncia territorial com dados de atendimento e mapa de calor.

## 6. Escopo do MVP
O MVP serÃ¡ intencionalmente enxuto para validar operaÃ§Ã£o real antes de adicionar automaÃ§Ãµes mais sofisticadas.

### 6.1 Funcionalidades no MVP
#### Morador
- autenticaÃ§Ã£o segura;
- botÃ£o de alerta categorizado: pÃ¢nico, atitude suspeita e emergÃªncia mÃ©dica;
- contagem regressiva de 5 segundos para cancelamento;
- PIN de coaÃ§Ã£o;
- visualizaÃ§Ã£o da viatura em deslocamento;
- solicitaÃ§Ã£o de escolta;
- recebimento de atualizaÃ§Ãµes da ocorrÃªncia;
- consulta do status de atendimento.

#### Ronda
- recebimento de nova ocorrÃªncia com prioridade e alerta sonoro;
- visualizaÃ§Ã£o da fila operacional;
- mapa da ocorrÃªncia com rota e ETA;
- atualizaÃ§Ã£o de status: recebida, em deslocamento, em atendimento e encerrada;
- ponto eletrÃ´nico com check-in, check-out e confirmaÃ§Ã£o de troca de turno;
- identificaÃ§Ã£o do agente em serviÃ§o com foto e habilitaÃ§Ã£o validada;
- vinculaÃ§Ã£o obrigatÃ³ria da viatura ao turno com registro de quilometragem inicial e final;
- checklist rÃ¡pido de disponibilidade e manutenÃ§Ã£o da viatura;
- botÃ£o de escalonamento humano com mensagem estruturada para VizinhanÃ§a SolidÃ¡ria.

#### Tutor
- cadastro e gestÃ£o de moradores;
- cadastro de operadores e viaturas;
- cadastro completo de funcionÃ¡rios da ronda com foto, habilitaÃ§Ã£o, validade e status;
- gestÃ£o de escala, troca de funcionÃ¡rio e trilha de responsabilidade por turno;
- cadastro de veÃ­culos com placa, modelo, quilometragem atual e prÃ³xima manutenÃ§Ã£o;
- definiÃ§Ã£o de geofence do bairro;
- monitoramento de ocorrÃªncias ativas;
- relatÃ³rios bÃ¡sicos de SLA, volume e tempo mÃ©dio de resposta.

### 6.2 Funcionalidades PÃ³s-MVP
- motor avanÃ§ado de priorizaÃ§Ã£o com mÃºltiplos critÃ©rios;
- escolta preditiva automÃ¡tica a 1 km;
- interceptaÃ§Ã£o assistida com heurÃ­stica de aproximaÃ§Ã£o;
- mapa de calor avanÃ§ado por rua, horÃ¡rio e categoria;
- geraÃ§Ã£o automatizada de PDF mensal com grÃ¡ficos executivos;
- mÃºltiplas viaturas com alocaÃ§Ã£o inteligente;
- regras preditivas de gargalo operacional.

## 7. Funcionalidades Detalhadas
### 7.1 Alertas
- o morador escolhe o tipo de ocorrÃªncia;
- o app envia localizaÃ§Ã£o e contexto mÃ­nimo necessÃ¡rio;
- o sistema registra horÃ¡rio de abertura e origem;
- a ocorrÃªncia entra em fila operacional com prioridade inicial por categoria e proximidade.

### 7.2 PrevenÃ§Ã£o de Falsos Positivos
- countdown de 5 segundos antes do envio final;
- botÃ£o de cancelamento visÃ­vel durante o countdown;
- PIN de coaÃ§Ã£o que aparenta cancelamento normal, mas gera alerta silencioso de alta prioridade;
- proteÃ§Ã£o contra toques acidentais e duplicidade de abertura.

### 7.3 Escolta
- solicitaÃ§Ã£o manual de escolta no MVP;
- compartilhamento temporÃ¡rio da localizaÃ§Ã£o do morador apenas durante a operaÃ§Ã£o;
- encerramento automÃ¡tico do rastreamento ao fim da escolta;
- orientaÃ§Ã£o de espera segura quando houver fila ou indisponibilidade.

### 7.4 Monitoramento em Tempo Real
- acompanhamento da viatura pelo morador durante a ocorrÃªncia ativa;
- atualizaÃ§Ã£o de posiÃ§Ã£o em tempo real via WebSocket;
- fallback de atualizaÃ§Ã£o periÃ³dica quando houver instabilidade de conexÃ£o.

### 7.5 Geofence
- delimitaÃ§Ã£o do perÃ­metro operacional pelo tutor;
- validaÃ§Ã£o do local do morador no acionamento;
- em caso de baixa confianÃ§a de GPS, o sistema solicita confirmaÃ§Ã£o adicional;
- em vez de bloquear cegamente o uso fora da Ã¡rea, o sistema registra exceÃ§Ã£o controlada para anÃ¡lise operacional.

### 7.6 Registro Operacional
- captura de localizaÃ§Ã£o do morador e da ronda nos marcos principais do atendimento;
- cÃ¡lculo de SLA por etapa: abertura, aceite, chegada e encerramento;
- trilha auditÃ¡vel de mudanÃ§a de status, operador responsÃ¡vel e observaÃ§Ãµes;
- registro do agente que iniciou e do agente que encerrou o turno ou a ocorrÃªncia;
- vÃ­nculo entre ocorrÃªncia, viatura utilizada e quilometragem operacional.

### 7.7 Escalonamento Humano
- nÃ£o haverÃ¡ integraÃ§Ã£o sistÃªmica com o 190;
- o produto gera mensagem estruturada com contexto da ocorrÃªncia, endereÃ§o e link de mapa;
- o envio Ã© feito por operador humano para o canal definido pela associaÃ§Ã£o ou rede local.

### 7.8 GestÃ£o de Equipe e Frota
- cada funcionÃ¡rio da ronda terÃ¡ cadastro com foto, documento funcional, habilitaÃ§Ã£o e validade;
- o inÃ­cio do turno exigirÃ¡ registro de ponto eletrÃ´nico, vÃ­nculo com a viatura e quilometragem inicial;
- a troca de funcionÃ¡rio exigirÃ¡ confirmaÃ§Ã£o do agente de saÃ­da e do agente de entrada, preservando a responsabilidade operacional;
- a finalizaÃ§Ã£o do turno registrarÃ¡ quilometragem final, observaÃ§Ãµes do veÃ­culo e eventual alerta de manutenÃ§Ã£o;
- o tutor visualizarÃ¡ histÃ³rico de jornadas, uso por veÃ­culo, odÃ´metro acumulado e manutenÃ§Ã£o preventiva pendente.

## 8. Regras de NegÃ³cio CrÃ­ticas
- nenhuma ocorrÃªncia serÃ¡ encaminhada automaticamente ao COPOM;
- a triagem e o escalonamento serÃ£o sempre humanos;
- o PIN de coaÃ§Ã£o nÃ£o deve alterar a aparÃªncia do fluxo para o usuÃ¡rio coagido;
- a localizaÃ§Ã£o da escolta deve ser temporÃ¡ria e limitada Ã  operaÃ§Ã£o ativa;
- a ronda visualizarÃ¡ apenas os dados estritamente necessÃ¡rios ao atendimento;
- toda ocorrÃªncia deve ter trilha de auditoria;
- toda troca de turno deve registrar responsÃ¡vel anterior, novo responsÃ¡vel, horÃ¡rio e viatura associada;
- a quilometragem da viatura deve ser obrigatÃ³ria na abertura e no encerramento do turno;
- chamadas duplicadas da mesma origem em curto intervalo devem ser consolidadas ou sinalizadas;
- a geofence deve considerar imprecisÃ£o natural de GPS e nÃ£o pode gerar bloqueio cego em casos ambÃ­guos.

## 9. Requisitos NÃ£o Funcionais
### 9.1 Performance
- tempo de abertura de ocorrÃªncia inferior a 3 segundos em rede estÃ¡vel;
- atualizaÃ§Ã£o de status quase em tempo real;
- dashboards administrativos com resposta aceitÃ¡vel para operaÃ§Ã£o diÃ¡ria.

### 9.2 Disponibilidade
- tolerÃ¢ncia a falhas de conexÃ£o mobile;
- reconexÃ£o automÃ¡tica de canal em tempo real;
- fallback operacional para cenÃ¡rios degradados.

### 9.3 Escalabilidade
- arquitetura apta a operar inicialmente em um bairro e escalar para mÃºltiplos bairros ou bases;
- separaÃ§Ã£o clara entre dados transacionais, tempo real e analÃ­ticos.

### 9.4 Observabilidade
- logs estruturados;
- mÃ©tricas de API, fila e conexÃ£o em tempo real;
- alertas internos para indisponibilidade ou atrasos crÃ­ticos.

## 10. Arquitetura TÃ©cnica Recomendada
### 10.1 Aplicativo Mobile
**Tecnologia recomendada: React Native**

Justificativa:
- melhor aderÃªncia para geolocalizaÃ§Ã£o em background;
- melhor ecossistema para push, mapas e tempo real;
- melhor adequaÃ§Ã£o para fluxos crÃ­ticos de mobilidade e atendimento operacional;
- mais apropriado do que Vue.js puro para este contexto mobile nativo.

### 10.2 Painel Administrativo
**Tecnologia sugerida: React ou Vue para Web**

O painel administrativo pode ser web, separado do app mobile, sem necessidade de stack idÃªntica ao aplicativo.

### 10.3 Backend
**Tecnologia recomendada: Java + Spring Boot**

Justificativa:
- robustez para regras operacionais;
- boa estrutura para seguranÃ§a, autenticaÃ§Ã£o, auditoria e integraÃ§Ãµes;
- facilidade para modelar processos de atendimento, filas e relatÃ³rios.

### 10.4 Banco de Dados
**Tecnologia recomendada: PostgreSQL + PostGIS**

Justificativa:
- suporte maduro a consultas espaciais;
- cÃ¡lculo de distÃ¢ncia, Ã¡rea, geofence e mapas;
- base sÃ³lida para relatÃ³rios e mÃ©tricas operacionais.

### 10.5 Componentes Complementares
- `Redis` para cache, presenÃ§a, rate limiting e estados efÃªmeros;
- `WebSockets` para rastreamento e atualizaÃ§Ã£o operacional;
- `FCM/APNs` para notificaÃ§Ãµes push;
- `RabbitMQ` como opÃ§Ã£o de fila assÃ­ncrona caso o volume operacional cresÃ§a.

## 11. SeguranÃ§a e Privacidade
SeguranÃ§a nÃ£o Ã© requisito secundÃ¡rio neste produto. Ela faz parte do desenho central.

### 11.1 Controles ObrigatÃ³rios
- criptografia em trÃ¢nsito com TLS;
- criptografia em repouso para dados sensÃ­veis;
- autenticaÃ§Ã£o com expiraÃ§Ã£o e renovaÃ§Ã£o controlada de sessÃ£o;
- MFA obrigatÃ³rio para tutor e perfis administrativos;
- controle de acesso por papel e escopo;
- trilha de auditoria para aÃ§Ãµes crÃ­ticas;
- revogaÃ§Ã£o remota de sessÃ£o e dispositivo;
- rate limiting e proteÃ§Ã£o contra abuso de API.

### 11.2 ProteÃ§Ã£o de Dados SensÃ­veis
- coleta mÃ­nima necessÃ¡ria;
- retenÃ§Ã£o limitada de histÃ³rico de localizaÃ§Ã£o;
- segregaÃ§Ã£o entre dados operacionais ativos e histÃ³ricos analÃ­ticos;
- mascaramento de dados para perfis sem necessidade total de visualizaÃ§Ã£o;
- exclusÃ£o ou anonimizaÃ§Ã£o conforme polÃ­tica de retenÃ§Ã£o.

### 11.3 SeguranÃ§a Mobile
- proteÃ§Ã£o contra sessÃ£o indevida;
- validaÃ§Ã£o de integridade do app quando viÃ¡vel;
- detecÃ§Ã£o de localizaÃ§Ã£o simulada ou anÃ´mala;
- bloqueio de capturas sensÃ­veis em telas crÃ­ticas, se aplicÃ¡vel;
- proteÃ§Ã£o de credenciais em armazenamento seguro do dispositivo.

### 11.4 LGPD e GovernanÃ§a
- base legal e consentimento adequados;
- polÃ­tica de uso e retenÃ§Ã£o transparente;
- registro de operaÃ§Ãµes sobre dados pessoais;
- acesso administrativo restrito e auditÃ¡vel;
- processo para solicitaÃ§Ã£o de exclusÃ£o, revisÃ£o ou exportaÃ§Ã£o quando aplicÃ¡vel.

## 12. Fluxos Principais
### 12.1 Fluxo de Alerta de PÃ¢nico
1. Morador seleciona `PÃ¢nico`.
2. Sistema inicia countdown de 5 segundos.
3. UsuÃ¡rio cancela, confirma ou informa PIN de coaÃ§Ã£o.
4. OcorrÃªncia Ã© criada com localizaÃ§Ã£o e prioridade.
5. Ronda recebe alerta e aceita atendimento.
6. Morador acompanha deslocamento da viatura.
7. Ronda encerra ocorrÃªncia com registro operacional.

### 12.2 Fluxo de Atitude Suspeita
1. Morador aciona alerta silencioso.
2. Sistema registra localizaÃ§Ã£o e contexto.
3. Ronda recebe ocorrÃªncia com prioridade compatÃ­vel.
4. Tutor pode acompanhar volume e recorrÃªncia por Ã¡rea.

### 12.3 Fluxo de Escolta
1. Morador solicita escolta.
2. Sistema avalia disponibilidade operacional.
3. Ronda recebe pedido com ETA.
4. Durante a escolta, localizaÃ§Ã£o do morador Ã© compartilhada temporariamente.
5. OperaÃ§Ã£o Ã© encerrada no fechamento seguro do destino.

## 13. Modelo de Dados de Alto NÃ­vel
### 13.1 Entidades Principais
- usuÃ¡rio;
- perfil de acesso;
- morador;
- operador de ronda;
- viatura;
- ocorrÃªncia;
- evento de ocorrÃªncia;
- geofence;
- dispositivo;
- comunicado;
- relatÃ³rio operacional.

### 13.2 Campos CrÃ­ticos de OcorrÃªncia
- identificador;
- tipo;
- prioridade;
- status;
- coordenada de origem;
- timestamps por etapa;
- operador responsÃ¡vel;
- viatura atribuÃ­da;
- observaÃ§Ãµes;
- indicador de escalonamento;
- trilha de auditoria.

## 14. MÃ©tricas de Sucesso
- tempo mÃ©dio atÃ© aceite;
- tempo mÃ©dio atÃ© chegada;
- percentual de ocorrÃªncias atendidas dentro do SLA;
- percentual de cancelamentos durante countdown;
- taxa de falsos positivos;
- volume por tipo de ocorrÃªncia;
- incidÃªncia por Ã¡rea e horÃ¡rio;
- tempo mÃ©dio de escolta;
- satisfaÃ§Ã£o percebida dos moradores.

## 15. Riscos e MitigaÃ§Ãµes
### 15.1 GPS Impreciso
MitigaÃ§Ã£o:
- margem operacional na geofence;
- confirmaÃ§Ã£o adicional;
- registro de baixa confianÃ§a.

### 15.2 Queda de Conectividade
MitigaÃ§Ã£o:
- reconexÃ£o automÃ¡tica;
- fallback periÃ³dico;
- sinalizaÃ§Ã£o visual de Ãºltima atualizaÃ§Ã£o.

### 15.3 Abuso ou Uso Malicioso
MitigaÃ§Ã£o:
- auditoria;
- histÃ³rico de acionamentos;
- regras antifraude;
- suspensÃ£o controlada médiante anÃ¡lise administrativa.

### 15.4 ExposiÃ§Ã£o Indevida de LocalizaÃ§Ã£o
MitigaÃ§Ã£o:
- minimizaÃ§Ã£o de dados;
- visibilidade contextual por perfil;
- retenÃ§Ã£o curta para localizaÃ§Ã£o granular.

## 16. CritÃ©rios de Aceite do MVP
- morador consegue abrir alerta em menos de 3 passos;
- ronda recebe nova ocorrÃªncia em tempo quase real;
- mapa da ocorrÃªncia exibe posiÃ§Ã£o e ETA da viatura;
- tutor consegue cadastrar moradores e geofence;
- sistema registra timestamps e GPS dos eventos principais;
- painel exibe pelo menos SLA, volume e tempo mÃ©dio de resposta;
- trilha de auditoria estÃ¡ disponÃ­vel para aÃ§Ãµes crÃ­ticas;
- PIN de coaÃ§Ã£o dispara fluxo silencioso corretamente.

## 17. Roadmap Sugerido
### Fase 1 - MVP Operacional
- alertas;
- acompanhamento da viatura;
- solicitaÃ§Ã£o manual de escolta;
- painel de tutoria;
- relatÃ³rios bÃ¡sicos;
- seguranÃ§a, auditoria e LGPD essenciais.

### Fase 2 - InteligÃªncia Operacional
- motor de priorizaÃ§Ã£o mais sofisticado;
- automaÃ§Ã£o parcial da escolta preditiva;
- relatÃ³rios executivos;
- indicadores por rua e horÃ¡rio;
- regras de gargalo operacional.

### Fase 3 - Escala e GovernanÃ§a
- mÃºltiplas bases e bairros;
- gestÃ£o avanÃ§ada de operadores;
- benchmarking entre regiÃµes;
- mÃ³dulos complementares de prevenÃ§Ã£o e comunicaÃ§Ã£o.

