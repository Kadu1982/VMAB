# Deploy VPS - axiumsistemas.com

## Subdominios recomendados
- `painel.axiumsistemas.com`
- `api.axiumsistemas.com`

## DNS na Hostinger
No print atual, o dominio raiz `@` e o `www` ainda apontam para `connect.hostinger.com`.

Para este sistema, crie ou ajuste:

```text
Tipo: A
Nome: painel
Aponta para: IP_PUBLICO_DA_VPS
TTL: 300
```

```text
Tipo: A
Nome: api
Aponta para: IP_PUBLICO_DA_VPS
TTL: 300
```

Se quiser que o dominio raiz tambem abra o painel:

```text
Tipo: A
Nome: @
Aponta para: IP_PUBLICO_DA_VPS
TTL: 300
```

Se quiser manter o site atual da Hostinger no dominio raiz, deixe `@` e `www` como estao e use apenas os subdominios `painel` e `api`.

## Arquivos ja prontos no projeto
- [compose.prod.yml](C:\Users\G15\Documents\Segurança\compose.prod.yml)
- [.env.production.example](C:\Users\G15\Documents\Segurança\.env.production.example)
- [Caddyfile](C:\Users\G15\Documents\Segurança\infra\caddy\Caddyfile)

## Observacao importante para esta VPS
Existe outra aplicacao ja usando `80/443` com `saude_nginx`.
Para nao quebrar esse ambiente, o VMAB deve subir sem o container Caddy proprio e deve entrar na rede Docker compartilhada `saude_saude_network` com os aliases:
- `vmab-backend`
- `vmab-dashboard`

Nesse cenario, o proxy externo continua sendo o `saude_nginx` existente, que encaminha as requisicoes para o VMAB.
O banco do VMAB tambem deve usar um alias proprio na rede compartilhada:
- `vmab-postgres`

## Observabilidade em producao
O backend expõe endpoints uteis para verificacao operacional e monitoramento:
- `GET /actuator/health`
- `GET /actuator/health/readiness`
- `GET /actuator/health/liveness`
- `GET /actuator/info`
- `GET /actuator/metrics`
- `GET /actuator/prometheus`
- `GET /actuator/loggers`

Os logs ficam persistidos no container em `/app/logs/vmab.log` e carregam `X-Correlation-Id` para rastrear a mesma requisicao do navegador ou do mobile ate o backend.

## Arquivo de ambiente na VPS
Na VPS:

```bash
cp .env.production.example .env.production
```

Revise:
- `APP_DOMAIN=painel.axiumsistemas.com`
- `API_DOMAIN=api.axiumsistemas.com`
- `ACME_EMAIL=contato@axiumsistemas.com`
- `POSTGRES_PASSWORD=troque-esta-senha`

## Subida da stack
Na pasta do projeto:

```bash
docker compose --env-file .env.production -f compose.prod.yml up -d --build
```

## Resultado esperado
- painel: `https://painel.axiumsistemas.com`
- api: `https://api.axiumsistemas.com`

## Mobile
No `ronda-mobile`, a URL da API deve ficar:

```text
https://api.axiumsistemas.com
```

## Portas que a VPS deve liberar
- `80/tcp`
- `443/tcp`

## Observacao
O HTTPS so vai fechar automaticamente depois que:
1. o DNS dos subdominios propagar
2. a VPS estiver acessivel externamente nas portas 80 e 443
