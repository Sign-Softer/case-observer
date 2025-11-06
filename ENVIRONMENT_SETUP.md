# 🌍 Environment Configuration Guide

This guide explains how to use different environment configurations in the Case Observer application.

---

## 📋 **Available Environments**

### **Development (Default)**
- Database: Local MySQL on port 3306
- Logging: DEBUG level
- Hot reload: Enabled
- Profile: `dev`

### **Test**
- Database: In-memory H2
- Logging: WARN level
- Profile: `test`

### **Production**
- Database: Remote MySQL with connection pooling
- Logging: INFO level
- Security: Enhanced
- Profile: `prod`

---

## 🚀 **How to Use**

### **1. Run with Development Profile**

```bash
# Default (uses dev profile)
./mvnw spring-boot:run

# Explicitly specify dev profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Using environment variable
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

### **2. Run with Test Profile**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

### **3. Run with Production Profile**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Or with environment variables
export DB_HOST=your-db-host
export DB_USERNAME=your-username
export DB_PASSWORD=your-password
export JWT_SECRET=your-jwt-secret
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

---

## 🐳 **Docker Usage**

### **Development Environment**
```bash
# Start services
docker compose -f docker-compose.dev.yml up -d

# View logs
docker compose -f docker-compose.dev.yml logs -f backend-dev

# Stop services
docker compose -f docker-compose.dev.yml down
```

### **Production Environment**
```bash
# Create .env file with secrets
cat > .env << EOF
DB_ROOT_PASSWORD=secure_password
DB_NAME=observer_prod
DB_USERNAME=observer_user
DB_PASSWORD=secure_password
JWT_SECRET=your-32-character-secret-key-here
PORTAL_API_KEY=your-portal-api-key
EOF

# Start services
docker compose -f docker-compose.prod.yml up -d

# Stop services
docker compose -f docker-compose.prod.yml down
```

---

## 🔧 **Configuration Files Structure**

```
src/main/resources/
├── application.properties          # Base configuration
├── application-dev.properties     # Development overrides
├── application-test.properties    # Test overrides
└── application-prod.properties    # Production overrides
```

---

## 📝 **Environment Variables**

### **Common Variables**
- `SPRING_PROFILES_ACTIVE` - Active profile (dev/test/prod)
- `DB_HOST` - Database host
- `DB_PORT` - Database port
- `DB_NAME` - Database name
- `DB_USERNAME` - Database username
- `DB_PASSWORD` - Database password

### **Production-Specific Variables**
- `JWT_SECRET` - JWT signing secret (32+ characters)
- `PORTAL_API_KEY` - External portal API key
- `MAIL_HOST` - Email service host
- `MAIL_USERNAME` - Email service username
- `MAIL_PASSWORD` - Email service password

---

## ✅ **Verification**

### **Check Active Profile**
```bash
# The application logs will show the active profile:
# "The following profiles are active: dev"
```

### **Database Connection**
```bash
# Check if MySQL is running
docker compose -f docker-compose.dev.yml ps mysql-dev

# Connect to MySQL
docker compose -f docker-compose.dev.yml exec mysql-dev mysql -uroot -p12345
```

### **Application Health**
```bash
# Check backend health
curl http://localhost:8080/actuator/health
```

---

## 🐛 **Troubleshooting**

### **Common Issues:**

1. **Port Already in Use**
   ```bash
   # Change port in application.properties
   server.port=8081
   ```

2. **Database Connection Failed**
   ```bash
   # Ensure MySQL is running
   docker compose -f docker-compose.dev.yml up mysql-dev
   ```

3. **Profile Not Loading**
   ```bash
   # Check profile is specified
   echo $SPRING_PROFILES_ACTIVE
   ```

---

## 📚 **Next Steps**

1. ✅ Environment configuration setup
2. ✅ Docker Compose for different environments
3. 🔄 Test CI/CD pipeline
4. 🔄 Setup monitoring and logging
5. 🔄 Configure production deployment

**You're now ready to develop with environment-based configuration!**
