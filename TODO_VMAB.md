# TODO VMAB

Tabela objetiva do que ainda falta para o sistema atingir um estado forte de producao. Esta lista deve ser atualizada a cada bloco entregue.

## Status

| ID | Bloco | Status | Prioridade | Observacao objetiva |
|---|---|---|---|---|
| 01 | Autenticacao com usuarios persistidos e JWT | Concluido | Alta | Entregue no backend, web e mobile. |
| 02 | Gestao real de usuarios e perfis no painel | Concluido | Alta | CRUD minimo entregue no backend e no painel, restrito ao perfil administrador. |
| 03 | Recuperacao de senha, bloqueio e ciclo de sessao | Concluido | Alta | Fluxo de reset, bloqueio por tentativas, logout, invalidacao por versao de token e integracao basica no painel web estao entregues. |
| 04 | App do morador | Em andamento | Alta | App inicial criado em `resident-mobile` com login, abertura de alertas e historico; ainda falta endurecer seguranca, push e UX final. |
| 05 | Fluxo real de alerta, panico, coacao e escolta | Em andamento | Alta | Backend agora tem tipos, sessao, criacao, cancelamento e workflow de status; ainda falta integrar isso ao painel e fechar notificacoes e regras avancadas. |
| 06 | Operacao de ronda com aceite, despacho e encerramento mais completos | Em andamento | Alta | Despacho, chegada ao local, encerramento e trilha de auditoria ja existem no backend e no painel; ainda faltam mobile, notificacao e refinamento de regras operacionais. |
| 07 | Troca de turno com aceite duplo e trilha formal | Em andamento | Alta | Ja existe registro de passagem de responsabilidade, horario e observacao, mas ainda falta aceite duplo separado por perfil e historico mais claro. |
| 08 | Escala, falta, atraso e cobertura | Em andamento | Alta | Turno agora guarda inicio/fim previsto, status de presenca, atraso em minutos e agente coberto, mas ainda faltam workflows dedicados de supervisao e notificacao. |
| 09 | Quilometragem percorrida, checklist e manutencao mais profundos | Em andamento | Alta | Ja existe KM por turno, checklist basico, bloqueio de alocacao para viatura irregular e historico de manutencao, mas ainda falta ciclo completo de OS e relatorios operacionais da frota. |
| 10 | Relatorios executivos e prova de execucao para cliente | Em andamento | Alta | Portal do cliente ganhou leitura executiva, saude do contrato e visao consolidada, mas ainda falta backend dedicado para relatórios formais. |
| 11 | Exportacao e impressao de relatorios | Em andamento | Media | Exportacao local em Markdown e CSV ja existe no painel; ainda falta exportacao com impressao formal e pipeline de producao. |
| 12 | Background tracking no mobile | Concluido | Alta | Rastreamento continuo e task de segundo plano estao implementados; a validacao completa depende de build nativa/dev client. |
| 13 | Operacao offline degradada com sincronizacao posterior | Concluido | Alta | Fila local de telemetria, reenvio posterior e sincronizacao ao voltar online estao implementados. |
| 14 | Antifraude de localizacao e contexto | Concluido | Alta | Sinais basicos de anomalia, salto geografico e GPS preso foram implementados no mobile. |
| 15 | Evidencias de ocorrencia com fotos e anexos | Pendente | Media | Falta comprovar atendimento com mais robustez. |
| 16 | Notificacoes em tempo real | Em andamento | Media | Painel agora tem polling automatico, mas ainda nao existe push real por WebSocket, evento ou mobile. |
| 17 | Auditoria de acoes criticas | Concluido | Alta | Operacoes e auth registram eventos criticos em trilha de auditoria persistida. |
| 18 | Observabilidade, logs e monitoramento | Pendente | Alta | Falta endurecimento de producao. |
| 19 | LGPD e politicas de retencao | Pendente | Alta | Nao esta fechado para uso real com dados sensiveis. |
| 20 | Deploy definitivo na VPS com dominio fixo | Pendente | Alta | Estrutura existe, mas ainda nao foi concluida na VPS real. |
| 21 | Testes automatizados de backend | Pendente | Media | Existe teste unitario inicial de JWT, mas a suite ainda nao esta confiavel nem abrangente. |
| 22 | Testes automatizados de frontend | Pendente | Media | Falta regressao basica da interface. |
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
