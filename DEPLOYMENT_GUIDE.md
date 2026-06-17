# Deployment Guide - Environment Configuration

## Quick Start

### Local Development
```bash
# 1. Copy environment template
cp .env.example .env

# 2. Update .env with your local values
nano .env

# 3. Run the application
./mvnw spring-boot:run
```

### Docker Compose (Local/Staging)
```bash
# 1. Update docker-compose.yml with your environment values
nano docker-compose.yml

# 2. Start containers
docker-compose up -d

# 3. View logs
docker-compose logs -f app
```

### Production Deployment

#### Option 1: Using Environment Variables (Recommended)

**1. Set environment variables on your server:**
```bash
export DB_URL="jdbc:postgresql://your-prod-host:5432/db?sslmode=require"
export DB_USERNAME="prod_user"
export DB_PASSWORD="secure_password_here"
export JWT_SECRET="your_production_jwt_secret"
export TWILIO_ACCOUNT_SID="your_account_sid"
export TWILIO_AUTH_TOKEN="your_auth_token"
export TWILIO_PHONE_NUMBER="+1234567890"
```

**2. Run the application:**
```bash
java -jar application.jar
```

#### Option 2: Kubernetes

**1. Create a secret:**
```bash
kubectl create secret generic waitlist-secrets \
  --from-literal=db-url="jdbc:postgresql://your-host:5432/db" \
  --from-literal=db-username="username" \
  --from-literal=db-password="password" \
  --from-literal=jwt-secret="your-secret" \
  --from-literal=twilio-sid="sid" \
  --from-literal=twilio-token="token"
```

**2. Update your deployment manifest:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: waitlist-app
spec:
  template:
    spec:
      containers:
      - name: app
        image: your-repo/waitlist-app:latest
        ports:
        - containerPort: 8080
        env:
        - name: SERVER_PORT
          value: "8080"
        - name: DB_URL
          valueFrom:
            secretKeyRef:
              name: waitlist-secrets
              key: db-url
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: waitlist-secrets
              key: db-username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: waitlist-secrets
              key: db-password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: waitlist-secrets
              key: jwt-secret
        - name: TWILIO_ACCOUNT_SID
          valueFrom:
            secretKeyRef:
              name: waitlist-secrets
              key: twilio-sid
        - name: TWILIO_AUTH_TOKEN
          valueFrom:
            secretKeyRef:
              name: waitlist-secrets
              key: twilio-token
        - name: TWILIO_PHONE_NUMBER
          value: "+1234567890"
        - name: LOG_LEVEL_ROOT
          value: "WARN"
        - name: LOG_LEVEL_APP
          value: "INFO"
```

#### Option 3: Docker

**1. Build image:**
```bash
docker build -t waitlist-app:1.0 .
```

**2. Run with environment variables:**
```bash
docker run -d \
  -p 8080:8080 \
  -e DB_URL="jdbc:postgresql://host:5432/db" \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="password" \
  -e JWT_SECRET="your-secret" \
  -e TWILIO_ACCOUNT_SID="sid" \
  -e TWILIO_AUTH_TOKEN="token" \
  -e TWILIO_PHONE_NUMBER="+1234567890" \
  waitlist-app:1.0
```

#### Option 4: Cloud Platforms

**AWS Elastic Beanstalk:**
1. Create `.ebextensions/java.config`:
```yaml
option_settings:
  aws:elasticbeanstalk:application:environment:
    DB_URL: jdbc:postgresql://rds-host:5432/db
    DB_USERNAME: username
    JWT_SECRET: your-secret
    TWILIO_ACCOUNT_SID: sid
    TWILIO_AUTH_TOKEN: token
```

**Google Cloud Run:**
```bash
gcloud run deploy waitlist-app \
  --image gcr.io/project/waitlist-app \
  --set-env-vars DB_URL="...",DB_USERNAME="...",JWT_SECRET="..."
```

**Azure App Service:**
```bash
az webapp config appsettings set \
  --resource-group myResourceGroup \
  --name myWebApp \
  --settings DB_URL="..." JWT_SECRET="..."
```

## Configuration Priority

Spring Boot reads configuration in this order:
1. Environment variables
2. System properties
3. Application properties files
4. Default values in application.properties

This means environment variables always override hardcoded defaults.

## Secure Practices Checklist

- [ ] All sensitive values are in environment variables, not in code
- [ ] `.env` file is in `.gitignore` and not committed
- [ ] Production credentials are different from development
- [ ] JWT_SECRET is at least 64 characters and randomly generated
- [ ] Database connections use SSL/TLS
- [ ] Twilio credentials are from production account
- [ ] Email credentials use app-specific passwords, not account passwords
- [ ] Logs don't contain sensitive information (check LOG_LEVEL_APP)
- [ ] Database backups are configured
- [ ] Regular credential rotation is scheduled
- [ ] Access to deployment systems is restricted
- [ ] Monitoring and alerting are configured

## Troubleshooting

**Problem: Application won't start, saying missing DB_PASSWORD**
- Solution: Ensure `.env` file exists and DB_PASSWORD is set
- Or: Set environment variable: `export DB_PASSWORD="..."`

**Problem: Twilio SMS not working**
- Solution: Check TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, TWILIO_PHONE_NUMBER
- Verify phone numbers are in international format: +1234567890

**Problem: JWT errors in production**
- Solution: Ensure JWT_SECRET is set and is the same on all instances
- Don't change JWT_SECRET while there are valid tokens in use

**Problem: Email not sending**
- Solution: Verify MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD
- Check that port 587 is not blocked by firewall

## Support

For issues with configuration, refer to `ENV_CONFIGURATION.md` for detailed documentation.

