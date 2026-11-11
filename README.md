# java-starter-kit-backend

Starter de API em **Spring Boot 4.1 + Java 25** com **arquitetura hexagonal**, JWT, perfis/permissões, auditoria e e-mail assíncrono via **RabbitMQ**.

Painel Angular (repo separado): [java-starter-kit-frontend](https://github.com/brayanmonteiroo/java-starter-kit-frontend) → http://localhost:4200

Este README orienta o **ambiente de desenvolvimento**: o que sobe no Docker, onde logar, onde ver e-mail, fila e documentação da API.

---

## O que sobe com o Compose

Projeto Compose: `java-starter-kit-backend` (`compose.dev.yaml`).

| Serviço | Para quê | URL / host |
|---------|----------|------------|
| **api** (`jsk-api`) | API Spring Boot (dev + JDWP) | http://localhost:8098 |
| **postgres** | Banco principal | `localhost:5438` |
| **redis** | Cache / suporte | `localhost:6380` |
| **rabbitmq** | Fila de e-mail e eventos | AMQP `5672` · UI http://localhost:15672 |
| **mailpit** | Caixa de e-mail de desenvolvimento | UI http://localhost:8025 · SMTP `1025` |

Health: http://localhost:8098/api/health  
Swagger UI: http://localhost:8098/api/swagger-ui.html  
OpenAPI JSON: http://localhost:8098/api/v3/api-docs  
Debug JVM (JDWP): `localhost:8000`

---

## Primeiro acesso (seed)

Na subida da API, se `SEED_ENABLED=true`, o bootstrap cria o admin (se ainda não existir).

| Campo | Valor padrão |
|-------|----------------|
| E-mail | `admin@java-starter.test` |
| Senha | `password` |
| Nome | `Admin` |

Onde configurar / ver a origem:

- `.env.example` → `SEED_ADMIN_EMAIL`, `SEED_ADMIN_PASSWORD`, `SEED_ADMIN_NAME`, `SEED_ENABLED`
- `src/main/resources/application.yml` → bloco `app.seed`
- Código: `bootstrap/DataSeeder.java`

Use essas credenciais no login do frontend ou no Swagger (`POST /api/login`).

---

## Mailpit (e-mail em dev)

Em desenvolvimento **não** enviamos e-mail real. A API publica eventos na fila Rabbit; o consumer SMTP aponta para o **Mailpit**.

1. Dispare um fluxo que manda e-mail (ex.: *Esqueci a senha*).
2. Abra http://localhost:8025
3. Veja a mensagem (assunto, corpo, link de reset).

SMTP interno (containers): host `mailpit`, porta `1025`.  
Do host: `localhost:1025`.

---

## RabbitMQ

Fila usada para desacoplar o envio de e-mail do request HTTP.

- **Management UI:** http://localhost:15672  
- **Login padrão da imagem:** `guest` / `guest`  
- Variáveis: `RABBITMQ_*` no `.env.example` / Compose

Na UI dá para inspecionar filas, mensagens e conexões enquanto testa forgot-password etc.

---

## Swagger / OpenAPI

Documentação interativa da API:

1. Suba o stack e abra http://localhost:8098/api/swagger-ui.html
2. Faça `POST /api/login` com o usuário seed
3. Copie o `accessToken` e use **Authorize** (Bearer) para chamar rotas admin

Rotas principais:

- Auth: `/api/login`, `/api/logout`, `/api/auth/refresh`, `/api/forgot-password`, `/api/reset-password`, `/api/user`
- Admin: `/api/admin/dashboard/stats`, `/users`, `/roles`, `/permissions/catalog`, `/audits`

---

## Setup rápido

```bash
cp .env.example .env
docker compose -f compose.dev.yaml up --build -d
```

Live reload da API (Compose Watch):

```bash
docker compose -f compose.dev.yaml up --build --watch
```

Frontend (outro repo / outro projeto Compose):

```bash
cd ../java-starter-kit-frontend
docker compose -f compose.dev.yaml up --build --watch
```

Depois:

1. Login no painel → http://localhost:4200 (`admin@java-starter.test` / `password`)
2. Ou explore a API → Swagger
3. Teste e-mail → Mailpit
4. Inspecione fila → RabbitMQ UI

---

## Stack

- Spring Boot 4.1.2 · Java 25 LTS · Spring Security 7 · JPA/Hibernate 7
- PostgreSQL + Flyway · Redis · RabbitMQ · Mailpit
- springdoc OpenAPI · Testcontainers

Docker alinhado ao [guia oficial Java](https://docs.docker.com/guides/java/) (multi-stage, `jarmode=tools`, stage `development` + JDWP).

---

## Arquitetura hexagonal

```
domain/          # regras e ports (sem Spring/JPA)
application/     # use cases
adapters.in/     # REST + consumers Rabbit
adapters.out/    # JPA, mail, messaging, security
bootstrap/       # seed do admin
```

---

## Testes

```bash
export JAVA_HOME=...   # JDK 25
./mvnw test            # precisa de Docker (Testcontainers)
```

---

## Comandos úteis

```bash
docker compose -f compose.dev.yaml logs -f api
docker compose -f compose.dev.yaml down
./mvnw spring-boot:run   # API no host; infra ainda via Compose
```

Credenciais de infra locais (Postgres/Redis/Rabbit) estão em `.env.example` — copie para `.env` e ajuste se precisar.
