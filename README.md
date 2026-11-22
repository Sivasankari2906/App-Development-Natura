# Natura – Plant Care Scheduler

Full-stack plant care app: schedule care tasks, track plant health and monitor environment data.

- **Frontend:** React 18 (`frontend/natura`)
- **Backend:** Spring Boot 2.7, Spring Security, JWT, MySQL (`backend`)
- **API docs:** Swagger, enabled through `enable.swagger.plugin` in `application.properties`

## Configuration

Secrets are not stored in the repo. The backend reads them from environment variables (without `DB_PASSWORD` it cannot connect, and without `JWT_SECRET` it will not start):

| Variable | Required | Description |
|---|---|---|
| `DB_PASSWORD` | yes | MySQL password for the database user |
| `JWT_SECRET` | yes | Base64-encoded key (32+ bytes) used to sign JWTs |
| `DB_USERNAME` | no | MySQL user, defaults to `root` |
| `DB_URL` | no | JDBC URL, defaults to `jdbc:mysql://localhost:3306/natura?createDatabaseIfNotExist=true` |
| `ADMIN_USERNAME`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` | no | If all three are set, an admin account is created at startup. Admin cannot be chosen at registration. |
| `CORS_ALLOWED_ORIGINS` | no | Comma-separated browser origins, defaults to `http://localhost:3000,http://127.0.0.1:3000` |
| `FRONTEND_URL` | no | Base URL used in password reset links, defaults to `http://localhost:3000` |
| `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `MAIL_FROM` | no | SMTP settings. Without `SPRING_MAIL_HOST` no email is sent. |
| `APP_MAIL_LOG_BODY` | no | Set to `true` for local development only: with no SMTP, print emails (including reset links) to the server log instead of dropping them. |

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

## Password reset and reminders

- **Password reset:** `POST /api/auth/forgot-password` emails a single-use link that expires in 30 minutes. Only a hash of the token is stored, and the response is the same whether or not the email exists. The `/reset-password?token=...` page sets the new password.
- **Care reminders:** a scheduled job runs hourly and emails each user one digest of pending care tasks due in the next 24 hours or overdue by up to 3 days, once per task. Users can turn email notifications and care reminders off in Settings (`PUT /api/auth/me/notifications`). Tune it with `app.reminders.*` in `application.properties`.
- **Local development:** with no SMTP configured, set `APP_MAIL_LOG_BODY=true` to see both emails in the backend log. Otherwise they are not sent.
- **Sessions:** a password reset also invalidates existing refresh tokens. Access tokens stay valid until they expire (1 hour).

## Data access

Plants, care tasks, health records and environment readings belong to the owner of the plant. Other users get 404 for them, and admins can see everything. Species data is readable by every user and writable only by admins.

## Tests

Backend unit tests cover JWT handling, password reset, care task scheduling, statistics and access control, and reminders:

```bash
cd backend
./mvnw test
```

## Features

- Register, login and password reset with JWT access and refresh tokens
- Role-based access, with admin-only endpoints
- Plant and species management
- Care schedule generation (30 days from a default plan), upcoming and overdue tasks, calendar view, completion statistics, and email reminders
- Plant health records (create, list, update). The photo, treatment, recovery-status, symptom-checker, diagnose and trend endpoints are placeholders and not implemented yet.
- Environment readings (manual and sensor data) and trends. Alerts, sensor calibration and optimal-conditions endpoints are placeholders and not implemented yet.

## Keeping secrets out of the repo

```bash
brew install gitleaks                 # open-source secret scanner
git config core.hooksPath .githooks   # run once per clone
```

Every commit is scanned, and `.gitleaks.toml` also catches literal passwords in config files.
