# PRD - Sistema de Segurança Comunitária Colaborativa

## 1. Resumo Executivo
Plataforma digital de segurança colaborativa para bairros e condomínios abertos, conectando moradores, equipe de ronda privada e a rede de Vizinhança Solidária da Polícia Militar do Estado de São Paulo. O produto tem foco em prevenção, resposta rápida, triagem humana responsável e padronização operacional, evitando acionamentos indevidos ao 190 e elevando a qualidade do atendimento local.

O sistema será composto por:
- aplicativo mobile multiplataforma para moradores e equipe de ronda;
- painel web administrativo para tutores/gestores;
- backend transacional com suporte a geolocalização, filas operacionais, eventos em tempo real e auditoria.

## 2. Problema
Hoje, bairros com vigilância privada e iniciativas comunitárias de segurança enfrentam:
- demora no despacho da ronda por falta de contexto e localização precisa;
- alto volume de comunicação informal em grupos de mensagem;
- dificuldade para priorizar ocorrências concorrentes;
- ausência de trilha operacional confiável para SLA e auditoria;
- risco de falsos positivos e perda de credibilidade junto à segurança pública.

## 3. Objetivos do Produto
### 3.1 Objetivos de Negócio
- reduzir o tempo médio de resposta da ronda;
- organizar o fluxo operacional do bairro;
- reduzir falsos acionamentos;
- aumentar a percepção de segurança dos moradores;
- gerar indicadores para melhoria contínua da operação.

### 3.2 Objetivos do Usuário
- permitir que o morador solicite ajuda com poucos toques;
- mostrar transparência sobre o atendimento em andamento;
- permitir que a ronda atenda com contexto, localização e prioridade;
- dar ao tutor visibilidade da operação, cadastro, cobertura e desempenho.

## 4. Perfis de Usuário
### 4.1 Morador
- aciona alertas e pedidos de escolta;
- acompanha o deslocamento da viatura;
- recebe atualizações da ocorrência;
- visualiza histórico básico e comunicados.

### 4.2 Ronda Privada
- recebe chamados em tempo real com prioridade;
- navega até o local da ocorrência;
- atualiza status do atendimento;
- registra entrada e saída de turno via ponto eletrônico;
- realiza troca de funcionário com passagem formal de responsabilidade;
- vincula agente, viatura e quilometragem no início e no fim do turno;
- aciona escalonamento humano para a rede de Vizinhança Solidária quando necessário.

### 4.3 Tutor / Administrador
- gerencia moradores, viaturas e operadores;
- mantém cadastro de funcionários com foto, documentos e habilitação;
- acompanha trocas de turno, ponto eletrônico e histórico de jornada;
- controla quilometragem da frota e agenda de manutenção;
- define perímetro de atendimento;
- acompanha ocorrências em andamento;
- extrai relatórios operacionais e indicadores.

## 5. Proposta de Valor
- resposta local mais rápida do que canais informais;
- operação padronizada e auditável;
- menor ruído operacional;
- uso responsável de recursos públicos, sem integração automática ao 190;
- inteligência territorial com dados de atendimento e mapa de calor.

## 6. Escopo do MVP
O MVP será intencionalmente enxuto para validar operação real antes de adicionar automações mais sofisticadas.

### 6.1 Funcionalidades no MVP
#### Morador
- autenticação segura;
- botão de alerta categorizado: pânico, atitude suspeita e emergência médica;
- contagem regressiva de 5 segundos para cancelamento;
- PIN de coação;
- visualização da viatura em deslocamento;
- solicitação de escolta;
- recebimento de atualizações da ocorrência;
- consulta do status de atendimento.

#### Ronda
- recebimento de nova ocorrência com prioridade e alerta sonoro;
- visualização da fila operacional;
- mapa da ocorrência com rota e ETA;
- atualização de status: recebida, em deslocamento, em atendimento e encerrada;
- ponto eletrônico com check-in, check-out e confirmação de troca de turno;
- identificação do agente em serviço com foto e habilitação validada;
- vinculação obrigatória da viatura ao turno com registro de quilometragem inicial e final;
- checklist rápido de disponibilidade e manutenção da viatura;
- botão de escalonamento humano com mensagem estruturada para Vizinhança Solidária.

#### Tutor
- cadastro e gestão de moradores;
- cadastro de operadores e viaturas;
- cadastro completo de funcionários da ronda com foto, habilitação, validade e status;
- gestão de escala, troca de funcionário e trilha de responsabilidade por turno;
- cadastro de veículos com placa, modelo, quilometragem atual e próxima manutenção;
- definição de geofence do bairro;
- monitoramento de ocorrências ativas;
- relatórios básicos de SLA, volume e tempo médio de resposta.

### 6.2 Funcionalidades Pós-MVP
- motor avançado de priorização com múltiplos critérios;
- escolta preditiva automática a 1 km;
- interceptação assistida com heurística de aproximação;
- mapa de calor avançado por rua, horário e categoria;
- geração automatizada de PDF mensal com gráficos executivos;
- múltiplas viaturas com alocação inteligente;
- regras preditivas de gargalo operacional.

## 7. Funcionalidades Detalhadas
### 7.1 Alertas
- o morador escolhe o tipo de ocorrência;
- o app envia localização e contexto mínimo necessário;
- o sistema registra horário de abertura e origem;
- a ocorrência entra em fila operacional com prioridade inicial por categoria e proximidade.

### 7.2 Prevenção de Falsos Positivos
- countdown de 5 segundos antes do envio final;
- botão de cancelamento visível durante o countdown;
- PIN de coação que aparenta cancelamento normal, mas gera alerta silencioso de alta prioridade;
- proteção contra toques acidentais e duplicidade de abertura.

### 7.3 Escolta
- solicitação manual de escolta no MVP;
- compartilhamento temporário da localização do morador apenas durante a operação;
- encerramento automático do rastreamento ao fim da escolta;
- orientação de espera segura quando houver fila ou indisponibilidade.

### 7.4 Monitoramento em Tempo Real
- acompanhamento da viatura pelo morador durante a ocorrência ativa;
- atualização de posição em tempo real via WebSocket;
- fallback de atualização periódica quando houver instabilidade de conexão.

### 7.5 Geofence
- delimitação do perímetro operacional pelo tutor;
- validação do local do morador no acionamento;
- em caso de baixa confiança de GPS, o sistema solicita confirmação adicional;
- em vez de bloquear cegamente o uso fora da área, o sistema registra exceção controlada para análise operacional.

### 7.6 Registro Operacional
- captura de localização do morador e da ronda nos marcos principais do atendimento;
- cálculo de SLA por etapa: abertura, aceite, chegada e encerramento;
- trilha auditável de mudança de status, operador responsável e observações;
- registro do agente que iniciou e do agente que encerrou o turno ou a ocorrência;
- vínculo entre ocorrência, viatura utilizada e quilometragem operacional.

### 7.7 Escalonamento Humano
- não haverá integração sistêmica com o 190;
- o produto gera mensagem estruturada com contexto da ocorrência, endereço e link de mapa;
- o envio é feito por operador humano para o canal definido pela associação ou rede local.

### 7.8 Gestão de Equipe e Frota
- cada funcionário da ronda terá cadastro com foto, documento funcional, habilitação e validade;
- o início do turno exigirá registro de ponto eletrônico, vínculo com a viatura e quilometragem inicial;
- a troca de funcionário exigirá confirmação do agente de saída e do agente de entrada, preservando a responsabilidade operacional;
- a finalização do turno registrará quilometragem final, observações do veículo e eventual alerta de manutenção;
- o tutor visualizará histórico de jornadas, uso por veículo, odômetro acumulado e manutenção preventiva pendente.

## 8. Regras de Negócio Críticas
- nenhuma ocorrência será encaminhada automaticamente ao COPOM;
- a triagem e o escalonamento serão sempre humanos;
- o PIN de coação não deve alterar a aparência do fluxo para o usuário coagido;
- a localização da escolta deve ser temporária e limitada à operação ativa;
- a ronda visualizará apenas os dados estritamente necessários ao atendimento;
- toda ocorrência deve ter trilha de auditoria;
- toda troca de turno deve registrar responsável anterior, novo responsável, horário e viatura associada;
- a quilometragem da viatura deve ser obrigatória na abertura e no encerramento do turno;
- chamadas duplicadas da mesma origem em curto intervalo devem ser consolidadas ou sinalizadas;
- a geofence deve considerar imprecisão natural de GPS e não pode gerar bloqueio cego em casos ambíguos.

## 9. Requisitos Não Funcionais
### 9.1 Performance
- tempo de abertura de ocorrência inferior a 3 segundos em rede estável;
- atualização de status quase em tempo real;
- dashboards administrativos com resposta aceitável para operação diária.

### 9.2 Disponibilidade
- tolerância a falhas de conexão mobile;
- reconexão automática de canal em tempo real;
- fallback operacional para cenários degradados.

### 9.3 Escalabilidade
- arquitetura apta a operar inicialmente em um bairro e escalar para múltiplos bairros ou bases;
- separação clara entre dados transacionais, tempo real e analíticos.

### 9.4 Observabilidade
- logs estruturados;
- métricas de API, fila e conexão em tempo real;
- alertas internos para indisponibilidade ou atrasos críticos.

## 10. Arquitetura Técnica Recomendada
### 10.1 Aplicativo Mobile
**Tecnologia recomendada: React Native**

Justificativa:
- melhor aderência para geolocalização em background;
- melhor ecossistema para push, mapas e tempo real;
- melhor adequação para fluxos críticos de mobilidade e atendimento operacional;
- mais apropriado do que Vue.js puro para este contexto mobile nativo.

### 10.2 Painel Administrativo
**Tecnologia sugerida: React ou Vue para Web**

O painel administrativo pode ser web, separado do app mobile, sem necessidade de stack idêntica ao aplicativo.

### 10.3 Backend
**Tecnologia recomendada: Java + Spring Boot**

Justificativa:
- robustez para regras operacionais;
- boa estrutura para segurança, autenticação, auditoria e integrações;
- facilidade para modelar processos de atendimento, filas e relatórios.

### 10.4 Banco de Dados
**Tecnologia recomendada: PostgreSQL + PostGIS**

Justificativa:
- suporte maduro a consultas espaciais;
- cálculo de distância, área, geofence e mapas;
- base sólida para relatórios e métricas operacionais.

### 10.5 Componentes Complementares
- `Redis` para cache, presença, rate limiting e estados efêmeros;
- `WebSockets` para rastreamento e atualização operacional;
- `FCM/APNs` para notificações push;
- `RabbitMQ` como opção de fila assíncrona caso o volume operacional cresça.

## 11. Segurança e Privacidade
Segurança não é requisito secundário neste produto. Ela faz parte do desenho central.

### 11.1 Controles Obrigatórios
- criptografia em trânsito com TLS;
- criptografia em repouso para dados sensíveis;
- autenticação com expiração e renovação controlada de sessão;
- MFA obrigatório para tutor e perfis administrativos;
- controle de acesso por papel e escopo;
- trilha de auditoria para ações críticas;
- revogação remota de sessão e dispositivo;
- rate limiting e proteção contra abuso de API.

### 11.2 Proteção de Dados Sensíveis
- coleta mínima necessária;
- retenção limitada de histórico de localização;
- segregação entre dados operacionais ativos e históricos analíticos;
- mascaramento de dados para perfis sem necessidade total de visualização;
- exclusão ou anonimização conforme política de retenção.

### 11.3 Segurança Mobile
- proteção contra sessão indevida;
- validação de integridade do app quando viável;
- detecção de localização simulada ou anômala;
- bloqueio de capturas sensíveis em telas críticas, se aplicável;
- proteção de credenciais em armazenamento seguro do dispositivo.

### 11.4 LGPD e Governança
- base legal e consentimento adequados;
- política de uso e retenção transparente;
- registro de operações sobre dados pessoais;
- acesso administrativo restrito e auditável;
- processo para solicitação de exclusão, revisão ou exportação quando aplicável.

## 12. Fluxos Principais
### 12.1 Fluxo de Alerta de Pânico
1. Morador seleciona `Pânico`.
2. Sistema inicia countdown de 5 segundos.
3. Usuário cancela, confirma ou informa PIN de coação.
4. Ocorrência é criada com localização e prioridade.
5. Ronda recebe alerta e aceita atendimento.
6. Morador acompanha deslocamento da viatura.
7. Ronda encerra ocorrência com registro operacional.

### 12.2 Fluxo de Atitude Suspeita
1. Morador aciona alerta silencioso.
2. Sistema registra localização e contexto.
3. Ronda recebe ocorrência com prioridade compatível.
4. Tutor pode acompanhar volume e recorrência por área.

### 12.3 Fluxo de Escolta
1. Morador solicita escolta.
2. Sistema avalia disponibilidade operacional.
3. Ronda recebe pedido com ETA.
4. Durante a escolta, localização do morador é compartilhada temporariamente.
5. Operação é encerrada no fechamento seguro do destino.

## 13. Modelo de Dados de Alto Nível
### 13.1 Entidades Principais
- usuário;
- perfil de acesso;
- morador;
- operador de ronda;
- viatura;
- ocorrência;
- evento de ocorrência;
- geofence;
- dispositivo;
- comunicado;
- relatório operacional.

### 13.2 Campos Críticos de Ocorrência
- identificador;
- tipo;
- prioridade;
- status;
- coordenada de origem;
- timestamps por etapa;
- operador responsável;
- viatura atribuída;
- observações;
- indicador de escalonamento;
- trilha de auditoria.

## 14. Métricas de Sucesso
- tempo médio até aceite;
- tempo médio até chegada;
- percentual de ocorrências atendidas dentro do SLA;
- percentual de cancelamentos durante countdown;
- taxa de falsos positivos;
- volume por tipo de ocorrência;
- incidência por área e horário;
- tempo médio de escolta;
- satisfação percebida dos moradores.

## 15. Riscos e Mitigações
### 15.1 GPS Impreciso
Mitigação:
- margem operacional na geofence;
- confirmação adicional;
- registro de baixa confiança.

### 15.2 Queda de Conectividade
Mitigação:
- reconexão automática;
- fallback periódico;
- sinalização visual de última atualização.

### 15.3 Abuso ou Uso Malicioso
Mitigação:
- auditoria;
- histórico de acionamentos;
- regras antifraude;
- suspensão controlada mediante análise administrativa.

### 15.4 Exposição Indevida de Localização
Mitigação:
- minimização de dados;
- visibilidade contextual por perfil;
- retenção curta para localização granular.

## 16. Critérios de Aceite do MVP
- morador consegue abrir alerta em menos de 3 passos;
- ronda recebe nova ocorrência em tempo quase real;
- mapa da ocorrência exibe posição e ETA da viatura;
- tutor consegue cadastrar moradores e geofence;
- sistema registra timestamps e GPS dos eventos principais;
- painel exibe pelo menos SLA, volume e tempo médio de resposta;
- trilha de auditoria está disponível para ações críticas;
- PIN de coação dispara fluxo silencioso corretamente.

## 17. Roadmap Sugerido
### Fase 1 - MVP Operacional
- alertas;
- acompanhamento da viatura;
- solicitação manual de escolta;
- painel de tutoria;
- relatórios básicos;
- segurança, auditoria e LGPD essenciais.

### Fase 2 - Inteligência Operacional
- motor de priorização mais sofisticado;
- automação parcial da escolta preditiva;
- relatórios executivos;
- indicadores por rua e horário;
- regras de gargalo operacional.

### Fase 3 - Escala e Governança
- múltiplas bases e bairros;
- gestão avançada de operadores;
- benchmarking entre regiões;
- módulos complementares de prevenção e comunicação.
