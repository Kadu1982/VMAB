# TODO VMAB

Tabela objetiva do que ainda falta para o sistema atingir um estado forte de producao. Esta lista deve ser atualizada a cada bloco entregue.

## Status

| ID | Bloco | Status | Prioridade | Observacao objetiva |
|---|---|---|---|---|
| 01 | Autenticacao com usuarios persistidos e JWT | Concluido | Alta | Entregue no backend, web e mobile. |
| 02 | Gestao real de usuarios e perfis no painel | Concluido | Alta | CRUD minimo entregue no backend e no painel, restrito ao perfil administrador, com vinculo opcional entre usuario operacional e vigilante. |
| 03 | Recuperacao de senha, bloqueio e ciclo de sessao | Concluido | Alta | Fluxo de reset, bloqueio por tentativas, logout, invalidacao por versao de token e integracao basica no painel web estao entregues. |
| 04 | App do morador | Em andamento | Alta | O `resident-mobile` agora usa PIN dedicado, restaura sessao/configuracao local, faz contagem regressiva antes do disparo e exige destino/PIN nos fluxos sensiveis; ainda faltam push, hardening final e validacao de uso real. |
| 05 | Fluxo real de alerta, panico, coacao e escolta | Em andamento | Alta | O backend agora diferencia alerta silencioso de coacao, exige PIN de coacao, exige destino na escolta e bloqueia transicoes/cancelamentos incoerentes; ainda faltam push, antifraude mais forte e regras operacionais finais. |
| 06 | Operacao de ronda com aceite, despacho e encerramento mais completos | Em andamento | Alta | Despacho, chegada ao local, encerramento e trilha de auditoria ja existem no backend e no painel; ainda faltam mobile, notificacao e refinamento de regras operacionais. |
| 07 | Troca de turno com aceite duplo e trilha formal | Concluido | Alta | Pedido de troca via /handoff-request, com aceite e rejeicao amarrados ao usuario de ronda vinculado ao vigilante designado; status HANDOFF_PENDING visivel na lista de turnos com botoes coerentes com a permissao real. |
| 08 | Escala, falta, atraso e cobertura | Em andamento | Alta | O painel e o backend ja suportam marcacao formal de atraso, falta, normalizacao e cobertura; ainda faltam notificacoes e regras mais fortes de supervisao. |
| 09 | Quilometragem percorrida, checklist e manutencao mais profundos | Concluido | Alta | Painel agora carrega /api/vehicles/report e exibe saude da frota, OS abertas por tipo, custo 30d e historico das ultimas OS com prioridade e status de ciclo de vida. |
| 10 | Relatorios executivos e prova de execucao para cliente | Em andamento | Alta | Portal do cliente ganhou leitura executiva, saude do contrato e visao consolidada, mas ainda falta backend dedicado para relatorios formais. |
| 11 | Exportacao e impressao de relatorios | Em andamento | Media | Exportacao local em Markdown e CSV ja existe no painel; ainda falta exportacao com impressao formal e pipeline de producao. |
| 12 | Background tracking no mobile | Concluido | Alta | Rastreamento continuo e task de segundo plano estao implementados; a validacao completa depende de build nativa/dev client. |
| 13 | Operacao offline degradada com sincronizacao posterior | Concluido | Alta | Fila local de telemetria, reenvio posterior e sincronizacao ao voltar online estao implementados. |
| 14 | Antifraude de localizacao e contexto | Concluido | Alta | Sinais basicos de anomalia, salto geografico e GPS preso foram implementados no mobile. |
| 15 | Evidencias de ocorrencia com fotos e anexos | Concluido | Media | Backend com upload, download e exclusao controlada (soft delete com auditoria); mobile com captura por camera e galeria; painel exibe lista com download e data de expiracao. |
| 16 | Notificacoes em tempo real | Em andamento | Media | O painel web agora consome SSE de forma real, sem depender so do polling; ainda faltam push mobile e notificacao formal para morador e cliente. |
| 17 | Auditoria de acoes criticas | Concluido | Alta | Operacoes e auth registram eventos criticos em trilha de auditoria persistida. |
| 18 | Observabilidade, logs e monitoramento | Concluido | Alta | Logs estruturados, correlation id, actuator, readiness/liveness, metricas e healthcheck real em Docker foram validados tecnicamente. |
| 19 | LGPD e politicas de retencao | Em andamento | Alta | O backend agora registra notificacao do titular, gera exportacao JSON, marca exportacao/exclusao aplicada e anonimiza morador, usuario e agente; ainda falta revisao juridica formal e integracao de notificacao real ao titular. |
| 20 | Deploy definitivo na VPS com dominio fixo | Pendente | Alta | Estrutura existe, mas ainda nao foi concluida na VPS real. |
| 21 | Testes automatizados de backend | Concluido | Media | A suite de auth e frota foi validada tecnicamente; o runner do Gradle foi estabilizado ao mover o build para um caminho ASCII fora do workspace com acento. |
| 22 | Testes automatizados de frontend | Concluido | Media | Vitest com regressao basica da tela de login e limpeza de sessao invalida foi implementado e validado com sucesso. |
| 23 | Refino visual final do painel web | Em andamento | Media | Painel ficou mais limpo com relatorios executivos e exportacao, mas ainda pode ganhar ajustes finos de hierarquia e espacamento. |
| 24 | Refino visual final do app da ronda | Em andamento | Media | Ja melhorou, mas ainda pode ficar mais leve e mais objetivo. |

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
