# Plataforma de Seguranca Comunitaria

## Estado atual
Este repositorio saiu da fase de PRD e prototipo estatico e agora possui uma base executavel inicial.

### O que ja temos
- [PRD_Seguranca_Comunitaria_v3.md](C:\Users\G15\Documents\Segurança\PRD_Seguranca_Comunitaria_v3.md) com escopo consolidado.
- [mvp-estatico](C:\Users\G15\Documents\Segurança\mvp-estatico) como referencia visual e de navegacao.
- [backend](C:\Users\G15\Documents\Segurança\backend) em Spring Boot com dominio inicial:
  - agentes;
  - viaturas;
  - turnos;
  - ocorrencias;
  - resumo do dashboard.
- [dashboard-web](C:\Users\G15\Documents\Segurança\dashboard-web) em React/Vite:
  - cards de resumo;
  - listas de agentes, viaturas, turnos e ocorrencias;
  - cadastro inicial de agentes e viaturas;
  - consumo do backend por API.

### O que ainda falta
- persistencia real com PostgreSQL;
- autenticacao e autorizacao por perfil;
- portal do cliente contratante;
- mobile do morador;
- mobile da ronda;
- escala avancada, ponto oficial ou integracao de ponto;
- antifraude aprofundado;
- modulo financeiro de frota e contratos;
- observabilidade, logs e deploy.

## Como rodar
### Backend
```powershell
cd backend
.\gradlew.bat bootRun
```

### Painel web
```powershell
cd dashboard-web
npm install
npm run dev
```

Se necessario, configure:

```powershell
$env:VITE_API_BASE_URL="http://localhost:8080"
```

## Proximo ciclo recomendado
1. Colocar persistencia com PostgreSQL.
2. Implementar autenticacao e perfis.
3. Evoluir CRUD completo de agentes, viaturas, turnos e ocorrencias.
4. Criar o portal do cliente.
5. Iniciar o app mobile da ronda.
