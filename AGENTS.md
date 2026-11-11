# Java Starter Kit Backend — Agent Guide

## Stack pinada

- Java 25 LTS
- Spring Boot 4.1.x (GA; sem RC)
- Spring Security 7, Spring Data JPA / Hibernate 7, Jackson 3, JUnit 6
- PostgreSQL + Flyway, Redis, RabbitMQ, Mail (Mailpit em dev)
- springdoc OpenAPI

## Arquitetura hexagonal (obrigatória)

- `domain` — entidades, eventos, ports (interfaces). **Sem** Spring/JPA.
- `application` — use cases (`@Service`). Orquestra domínio + ports.
- `adapters.in` — controllers REST, filters JWT, listeners Rabbit.
- `adapters.out` — JPA, SMTP, Rabbit publisher, bcrypt/JWT, clock.
- Controllers finos: validam DTO e chamam use case. Sem regra de negócio.

## Comandos

```bash
./mvnw -DskipTests compile
./mvnw test
docker compose -f compose.dev.yaml up --build -d
docker compose -f compose.dev.yaml up --build --watch   # live rebuild (docs.docker.com/guides/java)
```

Dockerfile: stages `deps` → `package` → `extract` → `development` | `final` (guia Docker Java). Compose de dev usa `target: development` + porta JDWP `8000`.

## Convenções

- Respostas e mensagens de erro de domínio em português quando voltadas ao usuário.
- Commits: Conventional Commits em português (regra do usuário).
- Não misturar MVC anêmico no núcleo: sem “Service gordão” no controller.
- E-mail sempre via fila Rabbit (`DomainEventPublisherPort`), nunca SMTP no request HTTP.

## Skills

Ative a skill correspondente ao domínio da tarefa (hexagonal, JWT, Rabbit, Flyway, Testcontainers).
