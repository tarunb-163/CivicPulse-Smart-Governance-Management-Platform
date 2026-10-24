# CivicPulse — Production Deployment Guide

This document outlines the exact procedures for deploying CivicPulse to cloud production environments (Docker, Render, Railway, AWS EC2 / ECS).

---

## 1. Environment Secrets & Variables

In production, all sensitive credentials must be supplied via environment variables.

| Variable | Description | Production Example |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `SPRING_DATASOURCE_URL` | JDBC URL for MySQL | `jdbc:mysql://db.host:3306/civicplus?useSSL=true&requireSSL=true` |
| `SPRING_DATASOURCE_USERNAME` | Database username | `civic_prod` |
| `SPRING_DATASOURCE_PASSWORD` | Strong DB password | *[Secure Secret]* |
| `JWT_SECRET` | 512-bit Base64 secret key | *[Cryptographic 64+ char key]* |
| `UPLOAD_DIR` | Persistent storage directory | `/var/data/civicpulse/uploads` |
| `APP_PUBLIC_BASE_URL` | Public HTTPS domain | `https://civicpulse.gov.in` |
| `SPRING_MAIL_HOST` | Production SMTP server | `smtp.sendgrid.net` |
| `SPRING_MAIL_PORT` | SMTP Port | `587` |
| `SPRING_MAIL_USERNAME` | SMTP Username | `apikey` |
| `SPRING_MAIL_PASSWORD` | SMTP API Key | *[Secret]* |
| `VISION_PROVIDER` | Optional AI vision provider | `gemini` or `mock` |
| `VISION_API_KEY` | Vision API key | *[Secret]* |

---

## 2. Docker & Docker Compose Deployment (Self-Hosted / VPS)

### Prerequisites
- Docker Engine 24+ & Docker Compose v2+
- Domain with DNS A record pointing to server IP
- Nginx or Caddy for SSL termination (Let's Encrypt)

### Step-by-Step
1. Clone the repository to the host server:
   ```bash
   git clone https://github.com/your-org/CivicPulse.git /opt/civicpulse
   cd /opt/civicpulse
   ```
2. Create production `.env`:
   ```bash
   cp .env.example .env
   # Edit .env with production passwords and domain
   ```
3. Start the containers:
   ```bash
   docker compose -f docker-compose.yml up -d --build
   ```
4. Verify health status:
   ```bash
   curl http://localhost:8080/actuator/health
   # Returns: {"status":"UP"}
   ```

---

## 3. Deployment on Render / Railway

### Railway Deployment
1. Create a new Railway project and link the repository.
2. Add a **MySQL** database plugin in Railway.
3. Railway automatically sets `DATABASE_URL` (or provide `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` referencing the MySQL plugin).
4. Add a persistent **Volume** mounted at `/app/uploads` and set `UPLOAD_DIR=/app/uploads`.
5. Add `JWT_SECRET` and `APP_PUBLIC_BASE_URL`.
6. Railway detects the `Dockerfile` and deploys automatically.

### Render Deployment (`render.yaml`)
```yaml
services:
  - type: web
    name: civicpulse-portal
    env: docker
    dockerfilePath: ./Dockerfile
    plan: standard
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: prod
      - key: APP_PUBLIC_BASE_URL
        value: https://civicpulse.onrender.com
      - key: UPLOAD_DIR
        value: /var/data/uploads
      - key: JWT_SECRET
        generateValue: true
    disk:
      name: uploads-disk
      mountPath: /var/data/uploads
      sizeGB: 10
```

---

## 4. Backups and Database Maintenance

- **MySQL Backups:** Set up nightly `mysqldump` cron jobs:
  ```bash
  mysqldump -u root -p civicplus | gzip > /opt/backups/civicplus_$(date +%F).sql.gz
  ```
- **Uploads Retention:** Replicate `/app/uploads` to AWS S3 or Cloudflare R2 bucket for disaster recovery.

---

## 5. Live Smoke Test Validation
After deployment, verify the live URL using the automated test script:
```bash
./scripts/smoke-test.sh https://your-domain.gov.in
# Or on Windows PowerShell:
.\scripts\smoke-test.ps1 -BaseUrl "https://your-domain.gov.in"
```
