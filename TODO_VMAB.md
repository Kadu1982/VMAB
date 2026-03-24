# TODO VMAB

Tabela objetiva do que ainda falta para o sistema atingir um estado forte de producao. Esta lista deve ser atualizada a cada bloco entregue.

## Status

| ID | Bloco | Status | Prioridade | Observacao objetiva |
|---|---|---|---|---|
| 01 | Autenticacao com usuarios persistidos e JWT | Concluido | Alta | Entregue no backend, web e mobile. |
| 02 | Gestao real de usuarios e perfis no painel | Concluido | Alta | CRUD minimo entregue no backend e no painel, restrito ao perfil administrador. |
| 03 | Recuperacao de senha, bloqueio e ciclo de sessao | Concluido | Alta | Fluxo de reset, bloqueio por tentativas, logout, invalidacao por versao de token e integracao basica no painel web estao entregues. |
| 04 | App do morador | Em andamento | Alta | App inicial criado em `resident-mobile` com login, abertura de alertas e historico; ainda falta endurecer seguranca, push e UX final. |
| 05 | Fluxo real de alerta, panico, coacao e escolta | Em andamento | Alta | Backend, app do morador e painel operacional ja conseguem abrir e operar alertas; ainda faltam notificacoes em tempo real, seguranca forte e regras avancadas. |
| 06 | Operacao de ronda com aceite, despacho e encerramento mais completos | Em andamento | Alta | Despacho, chegada ao local, encerramento e trilha de auditoria ja existem no backend e no painel; ainda faltam mobile, notificacao e refinamento de regras operacionais. |
| 07 | Troca de turno com aceite duplo e trilha formal | Em andamento | Alta | Pedido de troca, aceite e recusa ja existem com trilha temporal e responsavel; ainda falta endurecer o vinculo entre usuario logado e agente operacional. |
| 08 | Escala, falta, atraso e cobertura | Em andamento | Alta | O painel e o backend ja suportam marcacao formal de atraso, falta, normalizacao e cobertura; ainda faltam notificacoes e regras mais fortes de supervisao. |
| 09 | Quilometragem percorrida, checklist e manutencao mais profundos | Em andamento | Alta | Ja existe KM por turno, checklist basico, bloqueio de alocacao para viatura irregular, OS formal de manutencao, status claros e relatorio operacional da frota no backend; ainda falta integracao visual final e refinamentos de fluxo no painel. |
| 10 | Relatorios executivos e prova de execucao para cliente | Em andamento | Alta | Portal do cliente ganhou leitura executiva, saude do contrato e visao consolidada, mas ainda falta backend dedicado para relatorios formais. |
| 11 | Exportacao e impressao de relatorios | Em andamento | Media | Exportacao local em Markdown e CSV ja existe no painel; ainda falta exportacao com impressao formal e pipeline de producao. |
| 12 | Background tracking no mobile | Concluido | Alta | Rastreamento continuo e task de segundo plano estao implementados; a validacao completa depende de build nativa/dev client. |
| 13 | Operacao offline degradada com sincronizacao posterior | Concluido | Alta | Fila local de telemetria, reenvio posterior e sincronizacao ao voltar online estao implementados. |
| 14 | Antifraude de localizacao e contexto | Concluido | Alta | Sinais basicos de anomalia, salto geografico e GPS preso foram implementados no mobile. |
| 15 | Evidencias de ocorrencia com fotos e anexos | Em andamento | Media | Backend e painel ja suportam upload, listagem e download de evidencias por ocorrencia; ainda faltam captura mobile, exclusao controlada e politicas de retencao. |
| 16 | Notificacoes em tempo real | Em andamento | Media | Painel agora recebe eventos por SSE e reage sem depender so de polling; ainda faltam push mobile e canal formal para cliente/morador. |
| 17 | Auditoria de acoes criticas | Concluido | Alta | Operacoes e auth registram eventos criticos em trilha de auditoria persistida. |
| 18 | Observabilidade, logs e monitoramento | Concluido | Alta | Logs estruturados, correlation id, actuator, readiness/liveness, metricas e healthcheck real em Docker foram validados tecnicamente. |
| 19 | LGPD e politicas de retencao | Em andamento | Alta | Limpeza tecnica configuravel, exportacao/exclusao solicitavel e trilha auditavel ja existem; ainda falta revisao juridica e integracao de interface. |
| 20 | Deploy definitivo na VPS com dominio fixo | Pendente | Alta | Estrutura existe, mas ainda nao foi concluida na VPS real. |
| 21 | Testes automatizados de backend | Em andamento | Media | A suite de auth e frota foi escrita, mas o runner do Gradle ainda apresenta instabilidade de classpath neste ambiente Windows e nao pode ser tratado como totalmente confiavel. |
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
