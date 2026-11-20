# Natura – Plant Care Scheduler

Full-stack plant care app: schedule care tasks, track plant health and monitor environment data.

- **Frontend:** React 18 (`frontend/natura`)
- **Backend:** Spring Boot 2.7, Spring Security, JWT, MySQL (`backend`)
- **API docs:** Swagger, enabled through `enable.swagger.plugin` in `application.properties`

## Configuration

Secrets are not stored in the repo. The backend reads them from environment variables:

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | yes | MySQL password for the database user |
| `JWT_SECRET` | yes | Base64-encoded key (32+ bytes) used to sign JWTs |
| `DB_USERNAME` | no | MySQL user, defaults to `root` |
| `DB_URL` | no | JDBC URL, defaults to `jdbc:mysql://localhost:3306/natura?createDatabaseIfNotExist=true` |

Copy `.env.example` to `.env` and fill in the values. `.env` is git-ignored.
Generate a JWT secret with `openssl rand -base64 48`.

## Run locally

Backend (needs Java 17 specifically, since newer JDKs break the old Lombok version, and a running MySQL):

```bash
cd backend
export DB_PASSWORD='your-mysql-password'
export JWT_SECRET="$(openssl rand -base64 48)"
./mvnw spring-boot:run
```

Frontend:

```bash
cd frontend/natura
npm install
npm start
```

The frontend calls the API at `http://localhost:8080/api`.

## Tests

Backend unit tests cover JWT creation and validation (valid, expired, wrong-secret and malformed tokens):

```bash
cd backend
./mvnw test
```

## Features

- Register, login and password reset with JWT access and refresh tokens
- Role-based access, with admin-only endpoints
- Plant and species management
- Recurring care schedule generation, upcoming and overdue tasks, calendar view and statistics
- Plant health records, symptom checker and treatments
- Environment readings (manual and sensor data) with alerts
