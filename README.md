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
- GitOps manifest image tag updates on `dev` and `main` pushes

Published GHCR tags are the short commit SHA, the sanitized branch name, and
`latest` for pushes to `dev` or `main`.

Required GitHub secrets:

- `SONAR_TOKEN`: SonarCloud token used by the quality gate
- `GITOPS_PAT`: fine-grained token with contents write access to
  `Ayoub-Gaouet/ecocycle-tn-gitops`

Optional GitHub secret:

- `NVD_API_KEY`: NVD API key used to make OWASP Dependency Check faster and more stable

`NVD_API_KEY` is strongly recommended in GitHub Actions because public runners
can be rate-limited by the NVD API. The workflow reads it through
`nvdApiKeyEnvironmentVariable` so the key is not passed directly on the Maven
command line.

The default `GITHUB_TOKEN` is used for GHCR publishing, with workflow package
write permissions and SARIF upload permissions.

## GitOps Deployment

The deployment source of truth is the separate GitOps repository:

- `Ayoub-Gaouet/ecocycle-tn-gitops`
- ArgoCD application: `ecocycle-user-service`
- Kubernetes namespace: `ecocycle`
- Manifest path: `k8s/`

After a successful non-PR CI run on `dev` or `main`, the `update-gitops` job
checks out the GitOps repository, updates the image tag in
`k8s/kustomization.yaml` with `yq`, commits the change, and pushes it to
`main`. ArgoCD then detects the Git change and reconciles the cluster.

Initial local setup is intentionally manual only once. After cloning
`Ayoub-Gaouet/ecocycle-tn-gitops`, run these commands from the GitOps
repository root:

```bash
minikube start --cpus=4 --memory=8192
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl apply -f argocd/ecocycle-user-service-application.yaml
```

After the ArgoCD application is created, deployment changes should flow through
Git and the CI `update-gitops` job rather than repeated manual `kubectl apply`
commands.

## Execution Commands and Capture Checklist

Use this checklist to reproduce the project demo and prepare screenshots for the
Notion report.

### 1. Repository and branch state

```bash
git status --short --branch
git log --oneline --decorate -n 5
git branch -vv
```

Capture to add:

- Backend branch `EC-8-Deployement-continu-GitOps-avec-ArgoCD` tracking origin.
- Latest backend commit `feat(EC-8): wire GitOps deployment handoff`.
- GitOps branch pushed with commit `feat(EC-8): add ArgoCD GitOps manifests`.

### 2. Maven tests

Windows:

```powershell
.\mvnw.cmd "-Dmaven.repo.local=.m2/repository" test
```

Linux/macOS:

```bash
./mvnw test
```

Capture to add:

- Maven `BUILD SUCCESS`.
- Summary `Tests run: 31, Failures: 0, Errors: 0, Skipped: 0`.

### 3. Docker build and local execution

Build the application image:

```bash
docker build -t ecocycle/user-service:1.0.0 .
```

Run the complete local stack:

```bash
docker compose up --build -d
docker compose ps
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

Inspect logs and stop the stack:

```bash
docker compose logs app --tail=80
docker compose logs mariadb --tail=80
docker compose down
```

Capture to add:

- Successful `docker build`.
- `docker compose ps` showing MariaDB and the app healthy.
- `/actuator/health` response with status `UP`.
- Docker Desktop or terminal showing the running containers.

### 4. CI, quality and DevSecOps

Useful commands before opening the pull request:

```bash
git diff --check
git status --short --branch
git push -u origin EC-8-Deployement-continu-GitOps-avec-ArgoCD
```

Evidence to capture from GitHub:

- GitHub Actions workflow with jobs `build-test-sonar`, `dependency-check`,
  `docker-build-scan-push`, and `update-gitops`.
- SonarCloud Quality Gate passed.
- OWASP Dependency Check report artifact.
- Trivy SARIF/report artifact.
- GHCR image tags generated from the branch or short SHA.

### 5. GitOps and ArgoCD

Validate the GitOps manifests locally:

```bash
cd ../ecocycle-tn-gitops
kubectl kustomize k8s
git status --short --branch
```

Initial ArgoCD setup:

```bash
minikube start --cpus=4 --memory=8192
kubectl create namespace argocd
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml
kubectl apply -f argocd/ecocycle-user-service-application.yaml
```

Follow the deployment:

```bash
kubectl -n argocd get applications
kubectl -n ecocycle get pods,svc,ingress
kubectl -n ecocycle rollout status deployment/ecocycle-user-service
kubectl -n ecocycle port-forward svc/ecocycle-user-service 8080:80
curl http://localhost:8080/actuator/health
```

Capture to add:

- ArgoCD application `ecocycle-user-service`.
- ArgoCD status moving from `OutOfSync` to `Syncing` to `Synced`.
- Kubernetes pods and services in namespace `ecocycle`.
- Updated image tag in `ecocycle-tn-gitops/k8s/kustomization.yaml`.

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
