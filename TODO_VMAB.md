# TODO VMAB

Tabela objetiva do que ainda falta para o sistema atingir um estado forte de producao. Esta lista deve ser atualizada a cada bloco entregue.

## Status

| ID | Bloco | Status | Prioridade | Observacao objetiva |
|---|---|---|---|---|
| 01 | Autenticacao com usuarios persistidos e JWT | Concluido | Alta | Entregue no backend, web e mobile. |
| 02 | Gestao real de usuarios e perfis no painel | Concluido | Alta | CRUD minimo entregue no backend e no painel, restrito ao perfil administrador. |
| 03 | Recuperacao de senha, bloqueio e ciclo de sessao | Concluido | Alta | Fluxo de reset, bloqueio por tentativas, logout, invalidacao por versao de token e auditoria de eventos de auth estao no backend. |
| 04 | App do morador | Pendente | Alta | Ainda nao existe como app real, so como escopo e referencias. |
| 05 | Fluxo real de alerta, panico, coacao e escolta | Pendente | Alta | Backend e UI ainda nao fecham a experiencia completa do morador. |
| 06 | Operacao de ronda com aceite, despacho e encerramento mais completos | Em andamento | Alta | Despacho, chegada ao local, encerramento e trilha de auditoria ja existem no backend; ainda falta UI especifica, notificacao e refinamento de regras operacionais. |
| 07 | Troca de turno com aceite duplo e trilha formal | Em andamento | Alta | Ja existe registro de passagem de responsabilidade, horario e observacao, mas ainda falta aceite duplo separado por perfil e historico mais claro. |
| 08 | Escala, falta, atraso e cobertura | Em andamento | Alta | Turno agora guarda inicio/fim previsto, status de presenca, atraso em minutos e agente coberto, mas ainda faltam workflows dedicados de supervisao e notificacao. |
| 09 | Quilometragem percorrida, checklist e manutencao mais profundos | Em andamento | Alta | Ja existe KM por turno, checklist basico, bloqueio de alocacao para viatura irregular e historico de manutencao, mas ainda falta ciclo completo de OS e relatorios operacionais da frota. |
| 10 | Relatorios executivos e prova de execucao para cliente | Pendente | Alta | Portal do cliente ainda esta superficial. |
| 11 | Exportacao e impressao de relatorios | Pendente | Media | Falta uso comercial e operacional mais forte. |
| 12 | Background tracking no mobile | Pendente | Alta | Hoje o GPS esta em foreground. Isso nao fecha operacao real. |
| 13 | Operacao offline degradada com sincronizacao posterior | Pendente | Alta | Requisito do PRD ainda nao implementado corretamente. |
| 14 | Antifraude de localizacao e contexto | Pendente | Alta | Falta deteccao de GPS falso, ausencia de sincronizacao e anomalias. |
| 15 | Evidencias de ocorrencia com fotos e anexos | Pendente | Media | Falta comprovar atendimento com mais robustez. |
| 16 | Notificacoes em tempo real | Pendente | Media | Hoje o sistema depende de polling em varios pontos. |
| 17 | Auditoria de acoes criticas | Concluido | Alta | Operacoes e auth registram eventos criticos em trilha de auditoria persistida. |
| 18 | Observabilidade, logs e monitoramento | Pendente | Alta | Falta endurecimento de producao. |
| 19 | LGPD e politicas de retencao | Pendente | Alta | Nao esta fechado para uso real com dados sensiveis. |
| 20 | Deploy definitivo na VPS com dominio fixo | Pendente | Alta | Estrutura existe, mas ainda nao foi concluida na VPS real. |
| 21 | Testes automatizados de backend | Pendente | Media | Falta cobertura minima confiavel. |
| 22 | Testes automatizados de frontend | Pendente | Media | Falta regressao basica da interface. |
| 23 | Refino visual final do painel web | Em andamento | Media | Ja melhorou, mas ainda pode ficar mais limpo e mais comercial. |
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
