# TODO VMAB

Tabela objetiva do que ainda falta para o sistema atingir um estado forte de producao. Esta lista deve ser atualizada a cada bloco entregue.

## Status

| ID | Bloco | Status | Prioridade | Observacao objetiva |
|---|---|---|---|---|
| 01 | Autenticacao com usuarios persistidos e JWT | Concluido | Alta | Entregue no backend, web e mobile. |
| 02 | Gestao real de usuarios e perfis no painel | Concluido | Alta | CRUD minimo entregue no backend e no painel, restrito ao perfil administrador, com vinculo opcional entre usuario operacional e vigilante. |
| 03 | Recuperacao de senha, bloqueio e ciclo de sessao | Concluido | Alta | Fluxo de reset, bloqueio por tentativas, logout, invalidacao por versao de token e integracao basica no painel web estao entregues. |
| 04 | App do morador | Concluido | Alta | O app oficial passou a ser o `ronda-mobile`, com selecao de perfil, sessao separada do morador, historico, bloqueio de alerta ativo, PIN dedicado, logout real e notificacao push Expo; `resident-mobile` ficou congelado como legado de referencia. |
| 05 | Fluxo real de alerta, panico, coacao e escolta | Concluido | Alta | O ciclo do alerta do morador foi fechado com bloqueio de segundo alerta ativo, PIN discreto para coacao, destino obrigatorio para escolta, regras de cancelamento critico, push Expo em `ACKNOWLEDGED`, `DISPATCHED`, `ON_SITE`, `RESOLVED` e `CANCELLED`, com texto neutro para fluxo silencioso. |
| 06 | Operacao de ronda com aceite, despacho e encerramento mais completos | Concluido | Alta | O app do colaborador agora opera a fila completa pelo celular, com despacho usando o proprio turno, chegada ao local, encerramento com observacao obrigatoria, validacao de vinculo do vigilante, bloqueio de viatura irregular e notificacao local de mudanca na fila. |
| 07 | Troca de turno com aceite duplo e trilha formal | Concluido | Alta | Pedido de troca via /handoff-request, com aceite e rejeicao amarrados ao usuario de ronda vinculado ao vigilante designado; status HANDOFF_PENDING visivel na lista de turnos com botoes coerentes com a permissao real. |
| 08 | Escala, falta, atraso e cobertura | Concluido | Alta | O backend agora bloqueia combinacoes incoerentes, o painel executa acoes formais de atraso, falta, normalizacao e cobertura, e a supervisao gera push operacional para ronda, supervisao e administracao. |
| 09 | Quilometragem percorrida, checklist e manutencao mais profundos | Concluido | Alta | Painel agora carrega /api/vehicles/report e exibe saude da frota, OS abertas por tipo, custo 30d e historico das ultimas OS com prioridade e status de ciclo de vida. |
| 10 | Relatorios executivos e prova de execucao para cliente | Concluido | Alta | O portal do cliente agora consome `/api/client/report`, mostra indicadores contratuais, prova de execucao com ocorrencias consolidadas e ordens de servico recentes, sem expor cadastros internos. |
| 11 | Exportacao e impressao de relatorios | Concluido | Media | O backend exporta CSV formal e o painel gera impressao dedicada do relatorio operacional do cliente; pipeline grafico de producao pode evoluir depois, mas o bloco funcional esta fechado. |
| 12 | Background tracking no mobile | Concluido | Alta | Rastreamento continuo e task de segundo plano estao implementados; a validacao completa depende de build nativa/dev client. |
| 13 | Operacao offline degradada com sincronizacao posterior | Concluido | Alta | Fila local de telemetria, reenvio posterior e sincronizacao ao voltar online estao implementados. |
| 14 | Antifraude de localizacao e contexto | Concluido | Alta | Sinais basicos de anomalia, salto geografico e GPS preso foram implementados no mobile. |
| 15 | Evidencias de ocorrencia com fotos e anexos | Concluido | Media | Backend com upload, download e exclusao controlada (soft delete com auditoria); mobile com captura por camera e galeria; painel exibe lista com download e data de expiracao. |
| 16 | Notificacoes em tempo real | Concluido | Media | O painel web exibe um centro de eventos em tempo real via SSE para operacao e cliente, o morador recebe push Expo e o colaborador registra dispositivo Expo para push operacional remoto nas mudancas da fila de ocorrencias. |
| 17 | Auditoria de acoes criticas | Concluido | Alta | Operacoes e auth registram eventos criticos em trilha de auditoria persistida. |
| 18 | Observabilidade, logs e monitoramento | Concluido | Alta | Logs estruturados, correlation id, actuator, readiness/liveness, metricas e healthcheck real em Docker foram validados tecnicamente. |
| 19 | LGPD e politicas de retencao | Em andamento | Alta | O backend e o painel agora cobrem pedido, andamento, notificacao registrada, rascunho formal de comunicacao, exportacao JSON, anonimização, revogacao de dispositivos e retencao tecnica; o que ainda falta nao e codigo trivial, e sim revisao juridica formal e integracao de notificacao real para fora da plataforma. |
| 20 | Deploy definitivo na VPS com dominio fixo | Pendente | Alta | Estrutura existe, mas ainda nao foi concluida na VPS real. |
| 21 | Testes automatizados de backend | Concluido | Media | A suite de auth e frota foi validada tecnicamente; o runner do Gradle foi estabilizado ao mover o build para um caminho ASCII fora do workspace com acento. |
| 22 | Testes automatizados de frontend | Concluido | Media | Vitest com regressao basica da tela de login e limpeza de sessao invalida foi implementado e validado com sucesso. |
| 23 | Refino visual final do painel web | Concluido | Media | O painel foi aliviado, ganhou hierarquia melhor no portal do cliente, cards executivos mais limpos e callouts de compliance sem perder a identidade VMAB. |
| 24 | Refino visual final do app da ronda | Concluido | Media | O app unico recebeu refinamento visual completo com hierarquia melhor entre morador e colaborador, cards mais leves, leitura melhor de status e linguagem visual mais tecnologica. |

## Sequencia recomendada

1. Gestao real de usuarios e perfis.
2. Turno, troca de responsabilidade e jornada.
3. Background tracking e offline.
4. App do morador.
5. Portal do cliente e relatorios executivos.
6. Antifraude, auditoria e observabilidade.
7. Deploy definitivo na VPS.

## Regra de manutencao desta tabela

- `Concluido`: bloco funcional validado tecnicamente.
- `Em andamento`: bloco parcialmente implementado, mas ainda incompleto para uso real.
- `Pendente`: ainda nao entregue.
