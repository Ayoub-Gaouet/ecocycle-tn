# EcoCycle TN

EcoCycle TN is a Spring Boot service for a circular-economy recycling platform.
It currently contains user authentication, authenticated profile management, and
recyclable item announcements.

## Features

- `POST /api/auth/register`: register a user with BCrypt password hashing
- `POST /api/auth/login`: authenticate and receive a JWT bearer token
- `GET /api/users/me`: read the authenticated user profile
- `PUT /api/users/me`: update editable profile fields
- `POST /api/items`: create a recyclable item announcement
- `GET /api/items`: list announcements
- `GET /api/items?category=PLASTIC`: filter announcements by category
- `GET /api/items/{id}`: read announcement details
- `PUT /api/items/{id}`: update an owned announcement
- `DELETE /api/items/{id}`: delete an owned announcement

## Stack

- Java 17 source compatibility
- Spring Boot 3.5.13
- Spring Security with stateless JWT authentication
- Spring Data JPA
- MariaDB
- Docker and Docker Compose
- Spring Boot Actuator

## Local Development

Run tests:

```bash
./mvnw test
```

Run the application locally with MariaDB already available:

```bash
./mvnw spring-boot:run
```

Default datasource values:

- `DB_URL=jdbc:mariadb://localhost:3306/ecocycle_tn?createDatabaseIfNotExist=true`
- `DB_USERNAME=root`
- `DB_PASSWORD=`

If your MariaDB user cannot create databases automatically, create it manually:

```sql
CREATE DATABASE ecocycle_tn CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

For production-like runs, set `JWT_SECRET` to a secret with at least 32 bytes of
entropy.

## Docker

Build the image:

```bash
docker build -t ecocycle/user-service:1.0.0 .
```

Run only the application container, using a MariaDB instance reachable from the
container:

```bash
docker run --rm -p 8080:8080 \
  -e DB_URL="jdbc:mariadb://host.docker.internal:3306/ecocycle_tn?createDatabaseIfNotExist=true" \
  -e DB_USERNAME=root \
  -e DB_PASSWORD= \
  -e JWT_SECRET="MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=" \
  ecocycle/user-service:1.0.0
```

Run the complete stack with MariaDB:

```bash
docker compose up --build
```

The Compose database is available to the host on `localhost:3307` to avoid
conflicts with a local MariaDB server already listening on `3306`.

Stop and remove containers:

```bash
docker compose down
```

Remove the MariaDB volume when you need a fresh database:

```bash
docker compose down -v
```

## Continuous Integration

GitHub Actions runs the CI workflow on pull requests to `dev` or `main`, and on
pushes to `main`, `dev`, `feat/**`, `bugfix/**`, `hotfix/**`, and `EC-*`
branches.

The pipeline runs:

- Maven `clean verify` with JaCoCo coverage
- SonarCloud analysis with pull request quality gate feedback
- OWASP Dependency Check with HTML, JSON, and SARIF reports
- dependency tree export as a CI artifact
- Docker Buildx build from the shared JAR artifact
- Trivy image scan with SARIF upload
- GHCR push on non-PR events

Published GHCR tags are the short commit SHA, the sanitized branch name, and
`latest` for pushes to `dev` or `main`.

Required GitHub secret:

- `SONAR_TOKEN`: SonarCloud token used by the quality gate

Optional GitHub secret:

- `NVD_API_KEY`: NVD API key used to make OWASP Dependency Check faster and more stable

`NVD_API_KEY` is strongly recommended in GitHub Actions because public runners
can be rate-limited by the NVD API. The workflow reads it through
`nvdApiKeyEnvironmentVariable` so the key is not passed directly on the Maven
command line.

The default `GITHUB_TOKEN` is used for GHCR publishing, with workflow package
write permissions and SARIF upload permissions.

## DevSecOps

The CI pipeline fails when a security scanner finds a high-risk issue:

- OWASP Dependency Check fails the build for dependencies with CVSS `>= 7`.
- Trivy fails the build for Docker image vulnerabilities with severity `HIGH` or `CRITICAL`.

Security reports are uploaded as workflow artifacts. SARIF reports are also sent
to GitHub Code Scanning when the repository permissions allow it.

Accepted Trivy exceptions must be documented in `.trivyignore`, one CVE per
line, after risk acceptance.

## Health Checks

Actuator endpoints exposed for container and orchestration health checks:

- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`

Check the running service:

```bash
curl http://localhost:8080/actuator/health
```
