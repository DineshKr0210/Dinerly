# Docker Setup Guide - Waitlist Management System

## Overview

This project includes a complete Docker setup with:
- **Multi-stage Dockerfile** - Optimized Spring Boot container
- **docker-compose.yml** - Orchestrates backend, MySQL database, and phpMyAdmin
- **.dockerignore** - Optimizes build context
- **.env.example** - Configuration template

## Prerequisites

- Docker 20.10+ ([Install Docker](https://docs.docker.com/get-docker/))
- Docker Compose 2.0+ ([Install Docker Compose](https://docs.docker.com/compose/install/))
- 2GB+ free disk space
- 512MB+ available RAM

## Quick Start

### 1. Clone/Navigate to Project
```bash
cd /Users/dineshkumar/Downloads/backend
```

### 2. Configure Environment Variables
```bash
# Copy example file
cp .env.example .env

# Edit .env with your Twilio credentials
nano .env
```

Update these critical values:
```env
TWILIO_ACCOUNT_SID=your_actual_account_sid
TWILIO_AUTH_TOKEN=your_actual_auth_token
TWILIO_PHONE_NUMBER=+1234567890
```

### 3. Start All Services
```bash
docker-compose up -d
```

**Output:**
```
[+] Running 4/4
 ✔ Network waitlist-network  Created
 ✔ Container waitlist-db      Started
 ✔ Container waitlist-backend Started
 ✔ Container waitlist-phpmyadmin Started
```

### 4. Verify Services
```bash
# Check all containers are running
docker-compose ps

# View backend logs
docker-compose logs -f backend

# Check health
curl http://localhost:8080/api/restaurants
```

### 5. Access Services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Backend API** | http://localhost:8080 | None (public endpoints) |
| **phpMyAdmin** | http://localhost:8081 | User: `waitlist_user` / Pass: `waitlist_password` |
| **MySQL** | localhost:3306 | User: `waitlist_user` / Pass: `waitlist_password` |

## Docker Images Used

### Backend
```dockerfile
# Multi-stage build
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
FROM eclipse-temurin:17-jre-alpine
```

**Image Details:**
- **Builder**: Alpine-based Maven 3.9 with Java 17
- **Runtime**: Alpine-based OpenJDK 17 JRE (minimal ~300MB)
- **Total Size**: ~69MB JAR + ~350MB image = ~420MB

### Database
```yaml
image: mysql:8.0
```

**Configuration:**
- Version: 8.0 (stable)
- Port: 3306 (internal), 3306 (host)
- Database: `waitlist_db`
- User: `waitlist_user`
- Storage: Named volume `mysql_data` (persistent)

### phpMyAdmin (Optional)
```yaml
image: phpmyadmin:latest
```

**Purpose**: GUI database management at http://localhost:8081

## Project Structure

```
backend/
├── Dockerfile                 # Multi-stage build
├── docker-compose.yml         # Service orchestration
├── .dockerignore              # Build optimization
├── .env.example               # Config template
├── pom.xml                    # Maven dependencies
├── src/
│   ├── main/
│   │   ├── java/             # Application source
│   │   └── resources/         # Config files
│   └── test/                  # Test files (excluded from Docker)
└── target/
    └── backend-0.0.1-SNAPSHOT.jar  # Built JAR
```

## Common Commands

### Start Services
```bash
# Start in background
docker-compose up -d

# Start with logs visible
docker-compose up

# Start specific service
docker-compose up -d backend
docker-compose up -d database
```

### Stop Services
```bash
# Stop all services (keep data)
docker-compose stop

# Stop and remove containers
docker-compose down

# Stop and remove volumes (DELETE data)
docker-compose down -v
```

### View Logs
```bash
# View all logs
docker-compose logs

# Follow backend logs
docker-compose logs -f backend

# View last 50 lines
docker-compose logs --tail=50 backend

# Follow database logs
docker-compose logs -f database
```

### Rebuild Application
```bash
# Rebuild backend image
docker-compose build --no-cache backend

# Rebuild and restart
docker-compose up -d --build backend
```

### Execute Commands
```bash
# Access backend container shell
docker-compose exec backend sh

# Database shell
docker-compose exec database mysql -u waitlist_user -p waitlist_db

# List running processes
docker-compose exec backend ps aux

# Check database connections
docker-compose exec database mysql -u waitlist_user -p -e "SHOW PROCESSLIST;"
```

### Database Backup/Restore
```bash
# Backup database to file
docker-compose exec database mysqldump -u waitlist_user -p --databases waitlist_db > backup.sql

# Restore database from file
docker-compose exec -T database mysql -u waitlist_user -p < backup.sql
```

## Environment Variables

### Required Variables

#### Twilio SMS Configuration
```env
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_auth_token_here
TWILIO_PHONE_NUMBER=+1234567890  # Twilio phone number
```

#### Database Configuration
```env
SPRING_DATASOURCE_URL=jdbc:mysql://database:3306/waitlist_db?useSSL=false&serverTimezone=UTC
SPRING_DATASOURCE_USERNAME=waitlist_user
SPRING_DATASOURCE_PASSWORD=waitlist_password
```

### Optional Variables

```env
# Server
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# JPA/Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false

# Java Memory
JAVA_OPTS=-Xms256m -Xmx512m
```

## Health Checks

### Backend Health
```bash
curl -v http://localhost:8080/api/restaurants
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Restaurants retrieved",
  "data": []
}
```

### Database Health
```bash
docker-compose exec database mysqladmin -u waitlist_user -p ping
```

**Expected Response:**
```
mysqld is alive
```

### Docker Compose Status
```bash
docker-compose ps
```

**Expected Output:**
```
NAME                    COMMAND                  SERVICE             STATUS
waitlist-backend        "sh -c 'java $JAVA_..."  backend             Up (healthy)
waitlist-db             "docker-entrypoint.s..." database            Up (healthy)
waitlist-phpmyadmin     "/docker-entrypoint..." phpmyadmin          Up
```

## Performance Optimization

### Memory Configuration
Adjust `JAVA_OPTS` in `.env`:
```env
# Minimum system
JAVA_OPTS=-Xms128m -Xmx256m

# Standard system
JAVA_OPTS=-Xms256m -Xmx512m

# Production system
JAVA_OPTS=-Xms512m -Xmx1024m
```

### Volume Optimization
Speed up database with:
```bash
# Enable innodb_buffer_pool
docker-compose exec database mysql -u root -p -e "SET GLOBAL innodb_buffer_pool_size = 512000000;"
```

## Networking

### Network Details
```yaml
networks:
  waitlist-network:
    driver: bridge
```

**DNS Resolution:**
- Backend → Database: `http://database:3306`
- Frontend → Backend: `http://localhost:8080` or `http://backend:8080`
- Host → Services: `http://localhost:PORT`

### Port Mapping
| Service | Container Port | Host Port | Access |
|---------|------------------|-----------|--------|
| Backend | 8080 | 8080 | http://localhost:8080 |
| Database | 3306 | 3306 | mysql://localhost:3306 |
| phpMyAdmin | 80 | 8081 | http://localhost:8081 |

## Troubleshooting

### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process (macOS)
kill -9 <PID>

# Or use different port
docker-compose up -d -e SERVER_PORT=9090
```

### Database Connection Issues
```bash
# Check database health
docker-compose exec database mysqladmin -u root -p ping

# View database logs
docker-compose logs database

# Verify credentials
docker-compose exec database mysql -u waitlist_user -p -e "SELECT 1;"
```

### Backend Not Starting
```bash
# View backend logs
docker-compose logs backend

# Check if port 8080 is available
netstat -an | grep 8080

# Restart backend
docker-compose restart backend
```

### Memory Issues
```bash
# Check Docker resources
docker stats

# Increase Docker memory allocation
# (Settings → Resources → Memory slider)

# Reduce Java heap
JAVA_OPTS=-Xms128m -Xmx256m
```

### Cannot Connect to Database
```bash
# Verify network connectivity
docker-compose exec backend ping database

# Check database is running
docker-compose ps database

# Verify credentials
docker-compose exec database mysql -u waitlist_user -p -e "SHOW DATABASES;"
```

## Production Deployment

### Environment Setup
```bash
# Create .env for production
cp .env.example .env.prod

# Update with production values
nano .env.prod

# Use production compose file
docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

### Scaling
```bash
# Run multiple backend instances
docker-compose up -d --scale backend=3

# Load balance with nginx (create nginx service)
```

### Persistent Backups
```bash
# Automated daily backup
0 2 * * * docker-compose exec -T database mysqldump -u waitlist_user -p waitlist_db > /backups/db_$(date +\%Y\%m\%d).sql
```

### Monitoring
```bash
# View resource usage
docker stats

# View logs with timestamps
docker-compose logs --timestamps backend

# Setup log aggregation (ELK Stack, etc.)
```

## Cleanup

### Remove Everything
```bash
# Stop and remove containers
docker-compose down

# Remove images
docker rmi waitlist-backend:latest mysql:8.0 phpmyadmin:latest

# Remove named volumes (DELETE data!)
docker volume rm backend_mysql_data

# Remove network
docker network rm waitlist-network
```

### Free Up Space
```bash
# Remove all unused Docker resources
docker system prune -a

# Remove unused volumes
docker volume prune
```

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [MySQL Docker Image](https://hub.docker.com/_/mysql)
- [Java Docker Best Practices](https://docs.docker.com/language/java/build-images/)

## Support

For issues:
1. Check logs: `docker-compose logs`
2. Verify environment: `docker-compose config`
3. Reset and rebuild: `docker-compose down -v && docker-compose up --build`
4. Review troubleshooting section above


