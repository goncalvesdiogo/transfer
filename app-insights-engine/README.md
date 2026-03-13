# app-insights-engine

Motor de insights e notificações baseado em árvore de decisão.

## Stack

| Item | Versão |
|---|---|
| Java | 21 (LTS) |
| Kotlin | 1.9.25 |
| Spring Boot | 3.3.4 |
| Build | Maven |
| Testes | Kotest 5.9 + MockK 1.13 |

## Como rodar

```bash
# Build completo com testes
mvn clean verify

# Rodar a aplicação
mvn spring-boot:run

# Apenas testes
mvn test
```

A aplicação sobe em: `http://localhost:8080/api`

## Endpoints

```
POST /api/insights/process        — processa um compromisso
POST /api/insights/process/batch  — processa um lote
GET  /api/actuator/health         — health check
```

## Estrutura de pacotes

```
br.com.insights/
├── domain/           Compromisso, Boleto, Parcela, Notification, ProcessingContext
├── usecase/          Strategy pattern — boleto (D0/D4/D10) e parcela (D0/D3/D7)
├── decision/         Árvore de decisão DSL + factories por tipo
├── routing/          DecisionTreeRouter — despacha pelo tipo do compromisso
├── gateway/          Port & Adapter para envio de notificações
├── infra/            JPA audit, configurações de infraestrutura
├── api/              Controllers REST, DTOs, exception handler
└── config/           AppConfig, EngineProperties (@ConfigurationProperties)
```

## Perfis

| Perfil | Banco | Descrição |
|---|---|---|
| default (dev) | H2 in-memory | dry-run ativado, console H2 em `/h2-console` |
| prod | PostgreSQL | configurado via env vars `DATABASE_URL`, `DATABASE_USER`, `DATABASE_PASSWORD` |
| test | H2 in-memory | usado nos testes automatizados |

## Adicionando novo tipo de compromisso

1. Implementar `Compromisso` na camada `domain/`
2. Criar `Base<Tipo>UseCase` e os use cases concretos em `usecase/<tipo>/`
3. Criar `<Tipo>DecisionTreeFactory` em `decision/`
4. Registrar no `DecisionTreeRouter` via `@PostConstruct`
5. Adicionar o novo valor em `TipoCompromisso`
