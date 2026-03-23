# PRD v3 - Plataforma de Segurança Comunitária e Operação de Ronda

## 1. Visão Executiva
Plataforma digital para operação de segurança comunitária, desenhada para bairros, loteamentos, condomínios horizontais, associações de moradores e empresas de segurança privada que executam ronda motorizada ou apoio local.

O produto conecta:
- moradores;
- equipe de ronda;
- supervisores e gestores;
- contratantes da operação;
- frota operacional.

O objetivo não é apenas registrar alertas, mas operar a segurança local de ponta a ponta, com:
- despacho rápido;
- acompanhamento em tempo real;
- gestão de equipe;
- controle de turno e responsabilidade;
- gestão de viaturas e manutenção;
- relatórios de SLA e prova de execução;
- trilha auditável para operação, gestão e cliente final.

## 2. Tese de Produto
O sistema deve ser um investimento claro para a empresa de segurança, não apenas um aplicativo operacional.

Para isso, ele precisa entregar 4 blocos de valor:
- melhorar a resposta operacional;
- reduzir passivo trabalhista e falhas de processo;
- aumentar a capacidade de provar serviço ao cliente contratante;
- criar base para expansão comercial e retenção de contratos.

## 3. Problema
Hoje a operação de segurança local normalmente sofre com:
- alertas recebidos por canais informais;
- falta de contexto e localização precisa no despacho;
- baixa padronização entre agentes e turnos;
- troca de funcionário sem trilha clara de responsabilidade;
- pouco controle da quilometragem, checklist e manutenção das viaturas;
- ausência de portal ou relatório claro para demonstrar valor ao cliente;
- risco de fraude operacional, marcação indevida de presença e perda de evidência;
- dificuldade para medir SLA, produtividade, cobertura e custo da operação;
- risco jurídico por tratamento inadequado de dados pessoais e uso incompleto de ponto eletrônico.

## 4. Perfis de Usuário
### 4.1 Morador
- aciona alerta ou solicitação de escolta;
- acompanha atendimento em tempo real;
- recebe atualizações de status;
- acessa histórico básico e comunicados.

### 4.2 Agente de Ronda
- registra início e fim de turno;
- recebe ocorrências priorizadas;
- navega até a ocorrência;
- atualiza atendimento;
- registra troca de responsabilidade;
- executa checklist de viatura e registra quilometragem;
- comprova presença em postos e rotas quando aplicável.

### 4.3 Supervisor / Operador Líder
- acompanha fila operacional;
- redistribui atendimento;
- aprova ajustes de escala e ocorrências críticas;
- acompanha jornada, atrasos, faltas e trocas;
- monitora desvios operacionais.

### 4.4 Tutor / Gestor da Operação
- gerencia moradores, equipes, viaturas e geofences;
- acompanha SLAs, volume e produtividade;
- controla manutenção, escala e cobertura;
- audita eventos, acessos e decisões operacionais.

### 4.5 Cliente Contratante
- acompanha indicadores da operação do seu posto, bairro ou condomínio;
- consulta relatórios executivos e operacionais;
- valida cumprimento contratual e qualidade do serviço.

## 5. Segmentos e ICP
### 5.1 ICP Primário
- empresas de segurança que atendem condomínios e bairros;
- associações de moradores com operação terceirizada de ronda;
- loteamentos e condomínios horizontais com patrulhamento dedicado.

### 5.2 ICP Secundário
- administradoras condominiais;
- integradores de segurança;
- operações multi-base com necessidade de portal para cliente.

## 6. Proposta de Valor
### 6.1 Para a Empresa de Segurança
- centraliza operação, equipe e frota;
- reduz falhas de comunicação;
- cria prova auditável da execução do serviço;
- melhora gestão de jornada, troca de turno e responsabilidade;
- reduz risco de indisponibilidade da frota;
- gera material comercial e retenção contratual.

### 6.2 Para o Cliente Contratante
- transparência sobre a operação;
- indicadores objetivos de SLA e cobertura;
- menor dependência de WhatsApp e ligações;
- evidência de cumprimento do contrato.

### 6.3 Para o Morador
- acionamento simples;
- resposta rápida;
- visibilidade do atendimento;
- maior sensação de segurança.

## 7. Objetivos do Produto
### 7.1 Objetivos de Negócio
- reduzir tempo médio de resposta;
- reduzir falsos positivos;
- elevar taxa de atendimento dentro do SLA;
- aumentar retenção dos contratos da empresa de segurança;
- reduzir falhas de jornada, troca de turno e uso de viatura;
- gerar insumos para upsell, renovação e prova comercial.

### 7.2 Objetivos Operacionais
- padronizar abertura, despacho, atendimento e encerramento;
- garantir rastreabilidade por agente, turno e viatura;
- suportar operação com conectividade instável;
- tornar supervisão e auditoria rotinas nativas da plataforma.

## 8. Princípios de Produto
- nenhuma ocorrência será encaminhada automaticamente ao 190;
- escalonamento para autoridades será sempre humano;
- o app deve funcionar com o mínimo de fricção possível no momento crítico;
- o sistema deve registrar responsabilidade operacional em cada transição;
- dados pessoais devem ser coletados pelo mínimo necessário;
- operação offline degradada é obrigatória;
- prova de execução para cliente e auditoria interna é requisito de produto, não adição opcional.

## 9. Escopo Funcional Consolidado
### 9.1 Módulo Morador
- autenticação segura;
- perfis por unidade ou endereço;
- botão de alerta categorizado: pânico, atitude suspeita, emergência médica;
- countdown de 5 segundos para cancelamento;
- PIN de coação;
- solicitação de escolta;
- acompanhamento da viatura em tempo real;
- recebimento de atualização da ocorrência;
- histórico básico;
- comunicados e avisos da operação.

### 9.2 Módulo Operação de Ronda
- fila operacional em tempo real;
- priorização inicial por categoria, distância e disponibilidade;
- aceite de ocorrência;
- atualização de status: recebida, em deslocamento, no local, em atendimento, encerrada;
- mapa com rota e ETA;
- envio de observações, fotos e evidências quando permitido;
- geração de mensagem estruturada para escalonamento humano;
- fechamento com resumo operacional.

### 9.3 Módulo Jornada, Escala e Responsabilidade
- check-in e check-out de turno;
- escala por posto, base, bairro e agente;
- registro de atraso, falta e cobertura;
- troca de turno com aceite duplo;
- histórico de responsabilidade por ocorrência;
- supervisor podendo aprovar exceções;
- cálculo de horas previstas e efetivamente trabalhadas;
- exportação para folha ou integração com parceiro.

### 9.4 Módulo Ponto Eletrônico
- registro de jornada com trilha por dispositivo, horário e localização;
- política de marcação online e offline com sincronização posterior;
- evidência do contexto da marcação;
- relatório de inconsistências;
- possibilidade de assinatura digital do agente quando exigido.

Decisão obrigatória:
- se o produto for vendido como ponto oficial, o módulo deve ser implementado com aderência ao modelo regulatório aplicável, incluindo estratégia de REP-P;
- alternativamente, o sistema poderá operar como controle operacional de jornada e integrar com um fornecedor formal de ponto.

### 9.5 Módulo Cadastro de Equipe
- cadastro com foto;
- documento funcional;
- CNH e categoria;
- validade de documentos;
- treinamentos obrigatórios;
- status do agente: ativo, bloqueado, férias, afastado, desligado;
- vínculo com base, posto, turno e perfil de acesso.

### 9.6 Módulo Frota e Viaturas
- cadastro de viaturas com placa, modelo, tipo e status;
- vínculo da viatura ao turno;
- quilometragem inicial e final por turno;
- odômetro acumulado;
- checklist diário;
- combustível, observações e avarias;
- manutenção preventiva e corretiva;
- bloqueio de uso em caso de pendência crítica.

### 9.7 Módulo Patrulha, Postos e Prova de Execução
- roteiros e rondas por pontos de controle;
- registro de passagem por QR Code, NFC ou geofencing;
- comprovação de visita ao posto;
- alerta de posto não realizado;
- desvio de rota e parada prolongada;
- consolidação por turno e contrato.

### 9.8 Módulo Supervisor e Centro Operacional
- mapa consolidado com viaturas e ocorrências;
- redistribuição manual de chamados;
- alerta de agente offline, app inativo ou bateria crítica;
- alerta de GPS inconsistente;
- visão de cobertura por área;
- painel de incidentes, escalas e viaturas.

### 9.9 Módulo Tutor / Gestão
- cadastro de moradores, operadores, supervisores e viaturas;
- configuração de geofence;
- configuração de regras de operação e SLA;
- gestão de múltiplas bases e clientes;
- relatórios operacionais;
- auditoria e trilha de alterações.

### 9.10 Módulo Portal do Cliente
- portal web somente leitura para contratante;
- visão por condomínio, bairro, posto ou contrato;
- indicadores do período;
- histórico de ocorrências;
- comprovação de rondas e cobertura;
- exportação de relatórios;
- branding da empresa de segurança ou white-label quando contratado.

### 9.11 Módulo Relatórios e ROI
- SLA por tipo de ocorrência;
- tempo de aceite, deslocamento e encerramento;
- escoltas realizadas;
- taxa de cancelamento;
- produtividade por agente e turno;
- cobertura de ronda por posto;
- quilometragem por viatura;
- custo operacional por viatura e por contrato;
- indisponibilidade por manutenção;
- relatórios executivos para renovação contratual.

## 10. Escopo do MVP Definitivo
O MVP definitivo deve ser vendável para operação piloto real.

### 10.1 Obrigatório no MVP
- app do morador com alertas, PIN de coação e escolta manual;
- app da ronda com fila, mapa, ETA, aceite e encerramento;
- dashboard de gestão com ocorrências ativas e histórico;
- cadastro completo de equipe com foto e CNH;
- vínculo de turno, agente e viatura;
- quilometragem inicial e final por turno;
- checklist básico da viatura;
- troca de turno com aceite duplo;
- escala básica por agente e turno;
- portal do cliente com leitura de indicadores e histórico;
- relatórios básicos de SLA, jornada e uso de viatura;
- operação offline degradada com sincronização posterior;
- alertas de fraude operacional mínima: GPS inconsistente, marcação fora de contexto e ausência de sincronização por tempo excessivo;
- controle de acesso, auditoria e retenção mínima LGPD.

### 10.2 Pós-MVP
- heurística avançada de priorização;
- escolta preditiva;
- manutenção com custo financeiro detalhado;
- roteirização inteligente;
- OCR de documentos;
- analytics avançado por rua, horário e perfil;
- integração com folha, ERP e sistemas de terceiros;
- white-label multiempresa;
- benchmarking entre contratos e regiões.

## 11. Fluxos Críticos
### 11.1 Alerta de Pânico
1. Morador seleciona pânico.
2. Countdown de 5 segundos inicia.
3. Usuário cancela, confirma ou informa PIN de coação.
4. Ocorrência é criada com localização e contexto mínimo.
5. Centro operacional e ronda recebem prioridade.
6. Ronda aceita e se desloca.
7. Morador acompanha o atendimento.
8. Supervisor pode escalar humanamente se necessário.
9. Ocorrência é encerrada com trilha completa.

### 11.2 Solicitação de Escolta
1. Morador solicita escolta.
2. Sistema calcula disponibilidade e ETA.
3. Solicitação é atribuída a uma viatura.
4. Localização do morador é compartilhada apenas durante a operação.
5. Encerramento ocorre no fechamento seguro do trajeto.

### 11.3 Início de Turno
1. Agente realiza check-in.
2. Supervisor valida ou o sistema aplica regra automática.
3. Agente seleciona ou recebe a viatura.
4. Quilometragem inicial e checklist são registrados.
5. Turno passa a constar como ativo no centro operacional.

### 11.4 Troca de Funcionário
1. Agente de saída inicia transferência.
2. Agente de entrada autentica recebimento.
3. Sistema registra horário, responsabilidade, viatura e contexto.
4. Ocorrências em aberto permanecem associadas à transição registrada.

### 11.5 Fechamento de Turno
1. Agente encerra turno.
2. Sistema exige km final, checklist final e observações.
3. Pendências de manutenção geram alerta.
4. Jornada e uso da viatura são consolidados para relatório.

### 11.6 Prova de Execução para Cliente
1. Sistema consolida rondas, ocorrências, jornada e uso da frota.
2. Portal do cliente exibe resumo do período.
3. Gestor exporta relatório para renovação ou reunião operacional.

## 12. Regras de Negócio Críticas
- nenhuma ocorrência será enviada automaticamente à autoridade pública;
- toda ocorrência deve ter responsável atual rastreável;
- toda troca de turno deve registrar agente de saída, agente de entrada, horário e viatura;
- nenhuma viatura pode iniciar turno sem km inicial;
- turno não pode ser encerrado sem km final ou justificativa formal;
- viatura com pendência crítica de manutenção deve ser bloqueada;
- localização de escolta é temporária e limitada à operação;
- documentos sensíveis devem ter acesso restrito por perfil;
- inconsistências de GPS, horário ou dispositivo devem ser sinalizadas;
- dados offline sincronizados devem manter ordem temporal e trilha de origem;
- chamadas duplicadas em janela curta devem ser consolidadas ou sinalizadas;
- o PIN de coação não deve alterar a aparência do fluxo para o morador coagido.

## 13. Requisitos de Segurança, Privacidade e Compliance
### 13.1 Segurança
- TLS em trânsito;
- criptografia em repouso para dados sensíveis;
- RBAC por perfil e escopo;
- MFA obrigatório para perfis administrativos;
- revogação remota de sessão e dispositivo;
- auditoria de ações críticas;
- rate limiting e proteção contra abuso.

### 13.2 LGPD
- base legal definida por tipo de dado e processo;
- política de retenção por categoria de dado;
- minimização de coleta;
- direito de revisão, exportação e exclusão quando aplicável;
- segregação entre dados operacionais ativos e históricos analíticos;
- plano de resposta a incidente;
- registro de operações sobre dados pessoais.

### 13.3 Jornada e Ponto
- definição explícita de posicionamento do produto: controle operacional ou ponto oficial;
- se ponto oficial, aderência ao modelo regulatório aplicável e documentação técnica correspondente;
- se integração com parceiro, exportação confiável e auditável dos eventos.

## 14. Requisitos de Confiabilidade Operacional
- funcionamento com internet instável;
- fila local no dispositivo para eventos offline;
- reenvio automático com idempotência;
- indicador de última sincronização;
- detecção de app parado, sem permissão de localização ou bateria crítica;
- fallback visual e operacional quando mapa em tempo real falhar.

## 15. Requisitos Antifraude
- detecção de localização simulada ou anômala;
- alerta para marcação de turno fora do contexto esperado;
- inconsistência entre rota, posto e presença;
- trilha de dispositivo por marcação;
- restrição a uso em dispositivos comprometidos quando viável;
- logs de alteração de documentos, jornadas e quilometragem.

## 16. Requisitos Não Funcionais
### 16.1 Performance
- abertura de ocorrência em até 3 segundos em rede estável;
- atualização de status em tempo quase real;
- carregamento de dashboards em tempo aceitável para uso diário.

### 16.2 Disponibilidade
- suporte a reconexão automática;
- tolerância a falhas parciais;
- observabilidade de fila, WebSocket e sincronização.

### 16.3 Escalabilidade
- suportar início com um bairro ou base;
- expandir para múltiplos contratos, bases e clientes;
- isolamento lógico por empresa e contrato.

### 16.4 Observabilidade
- logs estruturados;
- métricas por serviço;
- alertas internos de atraso, indisponibilidade e falha de sincronização.

## 17. Modelo de Dados de Alto Nível
### 17.1 Entidades Principais
- usuário;
- perfil de acesso;
- morador;
- agente;
- supervisor;
- cliente contratante;
- contrato;
- posto;
- escala;
- jornada;
- viatura;
- checklist de viatura;
- manutenção;
- ocorrência;
- evento de ocorrência;
- geofence;
- dispositivo;
- ronda planejada;
- passagem por posto;
- relatório.

### 17.2 Campos Críticos
#### Ocorrência
- tipo;
- prioridade;
- status;
- localização;
- timestamps por etapa;
- agente responsável;
- viatura;
- histórico de transferência;
- escalonamento;
- evidências.

#### Jornada
- agente;
- turno planejado;
- check-in;
- check-out;
- dispositivo;
- localização da marcação;
- supervisor responsável;
- inconsistências.

#### Viatura
- placa;
- modelo;
- status;
- km atual;
- km por turno;
- próxima manutenção;
- bloqueios ativos.

## 18. Indicadores de Sucesso
### 18.1 Operação
- tempo médio até aceite;
- tempo médio até chegada;
- percentual de ocorrências dentro do SLA;
- taxa de cancelamento durante countdown;
- tempo médio de escolta;
- percentual de turnos iniciados corretamente;
- taxa de troca de turno com registro completo;
- percentual de checklists concluídos.

### 18.2 Equipe e Frota
- horas planejadas vs realizadas;
- faltas e atrasos;
- km por viatura;
- indisponibilidade por manutenção;
- custo por km e por contrato;
- alertas de fraude ou inconsistência por período.

### 18.3 Comercial
- renovação de contrato;
- churn por cliente;
- uso do portal do cliente;
- relatórios exportados por período;
- upsell de módulos adicionais.

## 19. Critérios de Aceite do MVP
- morador consegue abrir alerta crítico em até 3 passos;
- ronda recebe nova ocorrência em tempo quase real;
- centro operacional exibe responsáveis, viaturas e ocorrências ativas;
- turno só inicia com agente e viatura vinculados;
- troca de turno gera trilha auditável;
- km inicial e final são registrados por turno;
- portal do cliente exibe indicadores básicos do contrato;
- sistema opera com perda temporária de conexão e sincroniza depois;
- inconsistências mínimas de GPS ou contexto são sinalizadas;
- relatórios de SLA, jornada e viatura são exportáveis;
- controles de acesso e auditoria estão ativos.

## 20. Riscos e Mitigações
### 20.1 Baixa Adoção Operacional
Mitigação:
- UX enxuta;
- treinamento objetivo;
- rotinas obrigatórias com poucos passos.

### 20.2 Complexidade Regulatória do Ponto
Mitigação:
- decidir cedo entre módulo oficial e integração com parceiro;
- não prometer conformidade trabalhista sem solução aderente.

### 20.3 Fraude Operacional
Mitigação:
- antifraude por contexto, dispositivo e trilha de rota;
- auditoria ativa por supervisor.

### 20.4 Conectividade Instável
Mitigação:
- offline first para eventos críticos;
- sincronização assíncrona com idempotência.

### 20.5 Risco de Exposição de Dados
Mitigação:
- minimização;
- segregação por perfil;
- retenção curta quando aplicável;
- plano de resposta a incidente.

## 21. Arquitetura Recomendada
### 21.1 Mobile
- React Native para moradores e ronda.

### 21.2 Web
- React para gestão, supervisor e portal do cliente.

### 21.3 Backend
- Java + Spring Boot para regras operacionais, segurança e auditoria.

### 21.4 Dados
- PostgreSQL + PostGIS para dados transacionais e geoespaciais;
- Redis para presença, cache e estados efêmeros;
- WebSockets para operação em tempo real;
- mensageria assíncrona para eventos e integrações.

## 22. Roadmap de Entrega
### Fase 1 - Core Operacional Vendável
- morador;
- ronda;
- gestão;
- equipe;
- frota;
- portal do cliente;
- relatórios básicos;
- auditoria, offline e antifraude mínima.

### Fase 2 - Eficiência e Escala
- supervisor avançado;
- escala mais robusta;
- pontos de controle e ronda por posto;
- manutenção financeira;
- integrações.

### Fase 3 - Inteligência e Expansão Comercial
- priorização avançada;
- analytics de ROI;
- white-label multiempresa;
- benchmarking contratual;
- módulos comerciais e preditivos.

## 23. Decisões Imediatas Antes do Desenvolvimento Definitivo
- definir se o módulo de ponto será oficial ou integrado;
- definir se o MVP terá QR/NFC para prova de ronda já na primeira versão;
- definir o escopo inicial do portal do cliente;
- definir a granularidade de custo de frota no MVP;
- definir política de retenção de localização e documentos;
- definir ICP inicial: associação/condomínio ou empresa de segurança multi-contrato.
