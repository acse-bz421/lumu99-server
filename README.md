# lumu99-server

Backend service for the Lumu99 forum/community platform.

Tech stack:
- Java 17
- Spring Boot 3
- MySQL 8
- Flyway
- Spring Security + JWT
- springdoc-openapi

## Local Requirements

- JDK 17
- Maven 3.9+
- MySQL 8 (local)

## Database Setup

Use local MySQL (example: `root / 123456`).

Create databases:

```sql
CREATE DATABASE IF NOT EXISTS lumu99_forum
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS lumu99_forum_test
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

Optional environment variables:

```bash
DB_USERNAME=root
DB_PASSWORD=123456
DB_URL=jdbc:mysql://localhost:3306/lumu99_forum?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai

TEST_DB_USERNAME=root
TEST_DB_PASSWORD=123456
TEST_DB_URL=jdbc:mysql://localhost:3306/lumu99_forum_test?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
```

## Run Application

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Server default URL:
- `http://localhost:8080`

Note:
- API prefix does not include `/api`.

## Flyway Migration

Flyway is enabled in `dev` profile and runs automatically at startup.

Migration scripts:
- `src/main/resources/db/migration`

Current baseline migration:
- `V1__init_forum_schema.sql`

## Test and Verification

Run full tests:

```bash
mvn clean test
```

Run full verification (includes tests and packaging):

```bash
mvn verify
```

## API Docs

OpenAPI JSON:
- `http://localhost:8080/v3/api-docs`

Swagger UI:
- `http://localhost:8080/swagger-ui/index.html`

Grouped OpenAPI examples:
- `http://localhost:8080/v3/api-docs/auth`
- `http://localhost:8080/v3/api-docs/admin`
- `http://localhost:8080/v3/api-docs/forum`
- `http://localhost:8080/v3/api-docs/review`
- `http://localhost:8080/v3/api-docs/content`
- `http://localhost:8080/v3/api-docs/message`
