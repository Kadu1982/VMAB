# LGPD e Retencao Tecnica

Este documento descreve a politica tecnica implementada no backend da VMAB para reduzir risco de acumulacao de dados sensiveis, manter trilha auditavel e dar um caminho minimo para exportacao e marcacao de exclusao.

## O que o backend limpa automaticamente

- `password_reset_tokens` expirados e antigos.
- `resident_sessions` expiradas ou revogadas e ja fora da janela de retencao.
- evidencias de ocorrencia antigas, com remocao do registro no banco e dos arquivos em disco.
- arquivos de evidencia orfaos no diretorio de armazenamento, mesmo quando o registro ja nao existe.

## Configuracoes tecnicas

As configuracoes ficam em `backend/src/main/resources/application.properties` e podem ser sobrescritas por variaveis de ambiente.

- `vmab.retention.enabled`
- `vmab.retention.cleanup-cron`
- `vmab.retention.password-reset-token-retention-hours`
- `vmab.retention.resident-session-retention-days`
- `vmab.retention.incident-evidence-retention-days`
- `vmab.retention.remove-orphan-evidence-files`

## Endpoints de LGPD

Disponiveis apenas para `ADMIN`:

- `GET /api/privacy/requests`
- `POST /api/privacy/requests`
- `PUT /api/privacy/requests/{id}`
- `GET /api/privacy/exports/{subjectType}/{subjectId}`

## O que a exportacao entrega

- Morador: ficha, alertas e sessoes sem expor tokens brutos.
- Usuario administrativo: dados cadastrais e pedidos de reset ativos sem hash ou codigo.
- Vigilante/agente: cadastro e documentos principais.

## O que ainda exige decisao humana

O sistema nao apaga de forma automatica os registros operacionais que podem ter valor contratual, juridico ou de auditoria. Esses casos precisam ser avaliados pela operacao e pela base legal aplicavel.

## Limite objetivo

Isso nao substitui parecer juridico. O backend apenas fornece:

- limpeza tecnica configuravel;
- trilha de auditoria;
- pedidos de exportacao e exclusao;
- uma base consistente para a governanca de dados.
