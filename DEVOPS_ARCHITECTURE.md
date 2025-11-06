# 🚀 Case Observer - DevOps Architecture & Integration Strategy

## Overview

This document outlines the complete DevOps strategy for the Case Observer application, including the Spring Boot backend infrastructure, planned frontend integration, CI/CD pipeline, and deployment architecture.

**Current Status:**
- ✅ **Backend**: Spring Boot 3.4.3 (Java 17) - Fully implemented
- ✅ **Docker**: Multi-stage builds and compose files configured
- ✅ **Database**: MySQL 8 + Redis configured
- ✅ **Monitoring**: Scheduled case monitoring implemented
- ✅ **CI/CD**: Backend CI pipeline implemented (`.github/workflows/backend-ci.yml`)
- ❌ **Frontend**: Not yet implemented (planned)
- ❌ **Deployment Pipeline**: Not yet implemented (needs deploy workflow)
- ⚠️ **Actuator**: Configuration exists but dependency missing in pom.xml

---

## 🏗️ **Architecture Overview**

### **System Components:**
```
┌─────────────────────────────────────────────────────────┐
│                    Client Layer                         │
│  ┌────────────────┐  ┌────────────────┐                 │
│  │   Web App      │  │  Mobile App    │                 │
│  │  (Next.js)     │  │ (React Native) │                 │
│  │  🔮 PLANNED   │  │  🔮 FUTURE     │                 │
│  └────────────────┘  └────────────────┘                 │
└─────────────────────────────────────────────────────────┘
                         ↓ HTTPS/WebSocket
┌─────────────────────────────────────────────────────────┐
│                    Gateway Layer                        │
│  ┌────────────────────────────────────────────────┐     │
│  │  Nginx/CloudFlare  (Load Balancer + CDN)       │     │
│  │  - SSL Termination                             │     │
│  │  - Rate Limiting                               │     │
│  │  - Static File Serving                         │     │
│  └────────────────────────────────────────────────┘     │
└─────────────────────────────────────────────────────────┘
                         ↓ API Routes
┌─────────────────────────────────────────────────────────┐
│                    Application Layer                    │
│  ┌────────────────┐  ┌────────────────┐                 │
│  │  Spring Boot   │  │  External      │                 │
│  │  Backend API   │◄─┤  Portal API    │                 │
│  │  (Port 8080)   │  │  (Romanian     │                 │
│  │                │  │   Justice)     │                 │
│  └────────────────┘  └────────────────┘                 │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│                    Data Layer                           │
│  ┌────────────────┐  ┌────────────────┐                 │
│  │   MySQL 8      │  │     Redis      │                 │
│  │  ✅ CONFIGURED │  │  ✅ CONFIGURED │                 │
│  │  (Persistent)  │  │   (Cache)      │                 │
│  └────────────────┘  └────────────────┘                 │
└─────────────────────────────────────────────────────────┘
```

**Legend:**
- ✅ Implemented
- 🔮 Planned/Future
- ⚠️ Needs attention

---

## 🔌 **Frontend-Backend Communication Strategy**

> **Note**: Frontend is not yet implemented. This section outlines the planned integration strategy.

### **1. API Communication Layer**

#### **Option A: REST + React Query (Recommended)**

**Implementation:**
```typescript
// lib/api/cases.ts
import { useQuery, useMutation } from '@tanstack/react-query';
import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add JWT token to requests
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// API Functions
export const getCases = () => api.get('/api/cases');
export const getCaseById = (id: number) => api.get(`/api/cases/${id}`);
export const createCase = (data: CreateCaseRequest) => api.post('/api/cases', data);
export const startMonitoring = (id: number, interval: number) => 
  api.post(`/api/monitoring/cases/${id}/start?intervalMinutes=${interval}`);
export const stopMonitoring = (id: number) => 
  api.post(`/api/monitoring/cases/${id}/stop`);

// React Query Hooks
export const useCases = () => {
  return useQuery({
    queryKey: ['cases'],
    queryFn: getCases,
  });
};

export const useMonitorCase = () => {
  return useMutation({
    mutationFn: ({ id, interval }: { id: number; interval: number }) =>
      startMonitoring(id, interval),
  });
};
```

**Why React Query?**
- ✅ Automatic caching and deduplication
- ✅ Background refetching
- ✅ Optimistic updates
- ✅ Built-in loading/error states
- ✅ Excellent TypeScript support
- ✅ DevTools for debugging

---

### **3. Authentication Strategy**

#### **JWT with HTTP-Only Cookies**
```typescript
// lib/auth.ts
export const login = async (username: string, password: string) => {
  const response = await axios.post('/auth/login', { username, password });
  
  // Store JWT in memory (not localStorage for security)
  sessionStorage.setItem('jwt_token', response.data.token);
  
  return response.data;
};

export const getAuthToken = () => {
  return sessionStorage.getItem('jwt_token');
};

export const isAuthenticated = () => {
  return !!getAuthToken();
};
```

**Next.js Middleware for Protected Routes:**
```typescript
// middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const token = request.cookies.get('jwt_token')?.value;
  
  if (!token && request.nextUrl.pathname.startsWith('/dashboard')) {
    return NextResponse.redirect(new URL('/login', request.url));
  }
  
  return NextResponse.next();
}
```

---

## 🛠️ **Development Tools & Workflow**

### **1. Repository Strategy**

**Recommended: Monorepo with Turborepo** (Best for this project)
```
case-observer/
├── apps/
│   ├── backend/          # Spring Boot
│   ├── frontend/         # Next.js
│   └── mobile/           # React Native (future)
├── packages/
│   ├── api-client/       # Typed API client (shared)
│   ├── types/            # Shared TypeScript types
│   └── ui/               # Shared UI components
└── tools/
    └── ci/               # CI/CD scripts
```

---

### **2. API Proxy for Development**

**Next.js API Proxy:**
```typescript
// next.config.js
module.exports = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/api/:path*',
      },
    ];
  },
};
```

**Benefits:**
- ✅ No CORS issues in development
- ✅ Same origin for API calls
- ✅ Automatic fallback in production

---

### **3. Environment-Based Configuration**

**What It Means:**
Environment-based configuration allows you to run the same codebase in different environments (development, staging, production) with different settings.

**Frontend Environment Files:**

**`.env.local` (Development):**
```env
# API Configuration
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws

# Feature Flags
NEXT_PUBLIC_ENABLE_ANALYTICS=false
NEXT_PUBLIC_ENABLE_WEBSOCKET=true

# Third-party Services
NEXT_PUBLIC_SENTRY_DSN=your-dev-sentry-dsn
NEXT_PUBLIC_LOG_LEVEL=debug
```

**`.env.production` (Production):**
```env
# API Configuration
NEXT_PUBLIC_API_URL=https://api.caseobserver.com
NEXT_PUBLIC_WS_URL=wss://api.caseobserver.com/ws

# Feature Flags
NEXT_PUBLIC_ENABLE_ANALYTICS=true
NEXT_PUBLIC_ENABLE_WEBSOCKET=true

# Third-party Services
NEXT_PUBLIC_SENTRY_DSN=your-prod-sentry-dsn
NEXT_PUBLIC_LOG_LEVEL=error
```

**Backend Configuration (Spring Boot):**
```properties
# application-dev.properties
spring.datasource.url=jdbc:mysql://localhost:3306/observer_dev
spring.datasource.username=root
spring.datasource.password=dev_password

logging.level.ro.signsofter=DEBUG
logging.level.org.springframework.web=DEBUG

# Feature flags
monitoring.enabled=true
monitoring.interval-minutes=60

# External APIs
portal.api.url=http://localhost:8081/mock-portal
portal.api.retry.count=3
```

```properties
# application-prod.properties
spring.datasource.url=jdbc:mysql://prod-db:3306/observer_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

logging.level.ro.signsofter=INFO
logging.level.org.springframework.web=WARN

# Feature flags
monitoring.enabled=true
monitoring.interval-minutes=30

# External APIs
portal.api.url=https://portal.justice.ro/api
portal.api.retry.count=5
```

**How It Works:**
```bash
# Development
npm run dev                   # Uses .env.local
./mvnw spring-boot:run        # Uses application-dev.properties

# Production
NODE_ENV=production npm run build                     # Uses .env.production
./mvnw spring-boot:run -Dspring.profiles.active=prod  # Uses application-prod.properties
```

**Benefits:**
- ✅ Same codebase, different configurations
- ✅ No hardcoded URLs or credentials
- ✅ Easy to switch environments
- ✅ Secure secrets management
- ✅ Feature flags without code changes

---

### **4. Type Safety Bridge**

Use Zod for types in frontend

---

## 🚀 **CI/CD Pipeline**

> **Status**: ✅ **CI Pipeline Implemented** - Backend CI workflow exists and is functional

### **1. GitHub Actions Workflow**

**Current Implementation** (`.github/workflows/backend-ci.yml`):

The CI pipeline is fully implemented and includes 5 jobs:
1. **test** - Runs tests with MySQL service
2. **build** - Builds JAR artifact
3. **code-quality** - Checkstyle and dependency checks
4. **security-scan** - OWASP Dependency Check
5. **docker-build** - Builds and pushes Docker images

See the detailed breakdown in the "CI/CD Pipeline Details" section below.

**Deployment Pipeline** (`.github/workflows/deploy.yml` - ❌ **NOT YET IMPLEMENTED**):
```yaml
name: Deploy Pipeline

on:
  push:
    branches: [main]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup JDK
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Build Docker image
        run: docker build -t case-observer-backend:${{ github.sha }} -f Dockerfile.backend .
      
      - name: Push to Docker Registry
        run: |
          echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
          docker push case-observer-backend:${{ github.sha }}
      
      - name: Deploy to production
        run: |
          ssh ${{ secrets.HOST_USER }}@${{ secrets.HOST }} \
            "docker pull case-observer-backend:${{ github.sha } && \
             docker stop case-observer-api || true && \
             docker run -d --name case-observer-api case-observer-backend:${{ github.sha }}"
  
  deploy-frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '20'
      
      - name: Build Next.js app
        run: npm ci && npm run build
        working-directory: frontend
        env:
          NEXT_PUBLIC_API_URL: ${{ secrets.API_URL }}
      
      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v20
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.VERCEL_ORG_ID }}
          vercel-project-id: ${{ secrets.VERCEL_PROJECT_ID }}
          vercel-args: '--prod'
```

---

## 🐳 **Containerization Strategy**

### **Dockerfile for Backend:**
**Current Implementation** (`Dockerfile.backend`):
```dockerfile
# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline

# Copy source code
COPY src ./src

# Build application
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8080

# Health check (⚠️ Requires Actuator dependency)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

# Run with production profile by default
CMD ["--spring.profiles.active=prod"]
```

**Key Features:**
- ✅ Multi-stage build (smaller final image)
- ✅ Non-root user for security
- ✅ Health check configured
- ⚠️ Health check requires Actuator dependency (missing in pom.xml)

### **Dockerfile for Frontend:**
> **Status**: 🔮 Not yet implemented - Planned for Next.js frontend

```dockerfile
# Dockerfile.frontend (PLANNED)
FROM node:20-alpine AS builder

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine
WORKDIR /app
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package*.json ./
RUN npm ci --only=production

EXPOSE 3000
CMD ["npm", "start"]
```

### **Docker Compose for Development:**
**Current Implementation** (`docker-compose.dev.yml`):
```yaml
version: '3.8'

services:
  # MySQL Database for Development
  mysql-dev:
    image: mysql:8
    container_name: case-observer-mysql-dev
    environment:
      MYSQL_ROOT_PASSWORD: 12345
      MYSQL_DATABASE: observer_dev
    ports:
      - "3307:3306"  # Use different port to avoid conflicts
    volumes:
      - mysql_dev_data:/var/lib/mysql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - case-observer-network

  # Redis for Caching (future use)
  redis-dev:
    image: redis:7-alpine
    container_name: case-observer-redis-dev
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - case-observer-network

  # Spring Boot Application (optional - run locally for hot reload)
  backend-dev:
    build:
      context: .
      dockerfile: Dockerfile.backend
    container_name: case-observer-backend-dev
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - DB_HOST=mysql-dev
      - DB_PORT=3306
      - DB_NAME=observer_dev
      - DB_USERNAME=root
      - DB_PASSWORD=12345
      - JWT_SECRET=dev-secret-key-change-in-production-minimum-32-characters-long
    ports:
      - "8080:8080"
    depends_on:
      mysql-dev:
        condition: service_healthy
      redis-dev:
        condition: service_healthy
    networks:
      - case-observer-network
    command: ["--spring.profiles.active=dev"]

volumes:
  mysql_dev_data:
    driver: local

networks:
  case-observer-network:
    driver: bridge
```

**Production Compose** (`docker-compose.prod.yml`) also exists with:
- MySQL production configuration
- Redis with persistence
- Environment variable support
- Health checks
- Restart policies

---

## 📊 **Development Workflow**

### **1. Branching Strategy (Git Flow)**
```
master (production)
  ↓
develop (staging)
  ↓
feature/* (development)
```

### **2. Local Development Setup**
```bash
# Terminal 1: Start Backend
cd backend
./mvnw spring-boot:run

# Terminal 2: Start Frontend
cd frontend
npm install
npm run dev

# Terminal 3: Start Services
docker compose up -d
```

### **3. Testing Strategy**
```bash
# Backend Tests
mvn test                    # Unit tests
mvn verify                  # Integration tests
mvn sonar:sonar             # Code quality

# Frontend Tests
npm run test               # Unit tests
npm run test:e2e           # E2E tests (Playwright)
npm run lint               # Code quality
```

---

## 🔒 **Security Considerations**

### **1. CORS Configuration**

**Current Status**: ⚠️ CORS configuration exists in properties but not implemented in SecurityConfig

**Configuration in `application-dev.properties`:**
```properties
cors.frontend.origin=http://localhost:3000,http://localhost:5173
```

**Required Implementation** (to be added to `SecurityConfig.java`):
```java
// Spring Boot Security Config - CORS Bean needed
@Configuration
public class SecurityConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",        // Dev
            "http://localhost:5173",        // Dev (Vite)
            "https://caseobserver.com"      // Prod
        ));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
    
    // Update filterChain to use CORS
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // ... rest of configuration
    }
}
```

**Note**: Currently, CORS is handled via Nginx configuration (`nginx.conf`), but Spring Boot CORS should be implemented for direct API access.

### **2. Rate Limiting**
```java
// API Rate Limiting
@Configuration
public class RateLimitConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.of("api", RateLimiterConfig.custom()
            .limitForPeriod(100)
            .limitRefreshPeriod(Duration.ofMinutes(1))
            .build());
    }
}
```

---

## 📈 **Monitoring & Observability**

### **1. Application Monitoring**

**Current Status:**
- ⚠️ **Spring Boot Actuator** - Configuration exists but dependency missing in `pom.xml`
- ❌ **Prometheus** - Not configured
- ❌ **Grafana** - Not configured
- ❌ **Sentry** - Not configured
- ❌ **LogRocket** - Not configured (frontend not implemented)

**Actuator Configuration** (exists in `application-prod.properties`):
```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when-authorized
management.metrics.export.prometheus.enabled=true
```

**⚠️ CRITICAL**: Add Actuator dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Health Check Endpoints** (when Actuator is added):
- `/actuator/health` - Basic health check
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics endpoint

### **2. Logging Strategy**
```java
// Structured Logging
@RestController
public class MonitoringController {
    private static final Logger log = LoggerFactory.getLogger(MonitoringController.class);
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        log.info("Health check requested", 
            kv("timestamp", Instant.now()),
            kv("component", "monitoring"));
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
```

---

## 🎯 **Recommended Tech Stack**

### **Frontend:**
- Next.js 14 (React 18)
- TypeScript
- React Query (TanStack Query)
- Tailwind CSS + Headless UI
- Axios
- Zustand (state management)
- React Hook Form
- Playwright (E2E testing)

### **Backend:**
- Spring Boot 3
- Spring Data JPA
- MySQL 8
- Redis
- Spring Security + JWT
- Actuator

### **DevOps:**
- Docker + Docker Compose
- GitHub Actions
- Nginx (reverse proxy)
- Vercel (frontend deployment)
- AWS EC2/RDS (backend deployment)
- CloudFlare (CDN + DDoS protection)

---

## 🚀 **Next Steps**

1. ✅ Setup backend API with REST endpoints
2. ✅ Implement JWT authentication
3. ✅ Create Docker configurations
4. ✅ Setup GitHub Actions CI pipeline
5. ⚠️ Add Spring Boot Actuator dependency (CRITICAL)
6. ⚠️ Configure CORS in Spring Security
7. ❌ Create deployment pipeline workflow
8. ❌ Configure monitoring and logging (Prometheus/Grafana)
9. ⚠️ Deploy to production (infrastructure ready, needs deploy workflow)

---

## 📋 **Detailed Implementation Steps**

### **Phase 1: Critical Fixes (Week 1)**

#### **Step 1.1: Add Spring Boot Actuator Dependency**
**Priority**: 🔴 **CRITICAL**

**Action Items:**
1. Open `pom.xml`
2. Add dependency in `<dependencies>` section:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
3. Run `./mvnw clean install` to verify
4. Test health endpoint: `curl http://localhost:8080/actuator/health`
5. Verify Docker health checks work

**Expected Outcome:**
- Health checks return 200 OK
- Docker health checks pass
- Metrics endpoint accessible

---

#### **Step 1.2: Implement CORS Configuration**
**Priority**: 🟡 **HIGH** (needed for frontend integration)

**Action Items:**
1. Open `src/main/java/ro/signsofter/caseobserver/security/SecurityConfig.java`
2. Add CORS configuration bean:
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",
        "http://localhost:5173"
    ));
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```
3. Update `filterChain` method to include CORS:
```java
http
    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
    .csrf(csrf -> csrf.disable())
    // ... rest of config
```
4. Test CORS headers: `curl -H "Origin: http://localhost:3000" -v http://localhost:8080/api/cases`

**Expected Outcome:**
- CORS headers present in API responses
- Frontend can make API calls without CORS errors

---

### **Phase 2: CI/CD Pipeline Setup (Week 2)**

#### **Step 2.1: CI Pipeline Status**
**Current Status**: ✅ **IMPLEMENTED**

The backend CI pipeline (`.github/workflows/backend-ci.yml`) is already implemented and includes:
- Test execution with MySQL service
- JAR artifact building
- Code quality checks
- Security vulnerability scanning
- Docker image building and pushing

**No action needed** - CI pipeline is functional.

#### **Step 2.2: Create Deployment Pipeline**
**Status**: ❌ **NOT IMPLEMENTED** - Needs to be created

**Action Items:**
1. Create `.github/workflows/deploy.yml` file (see template below)
2. Configure GitHub secrets for deployment
3. Test deployment workflow

**Implemented CI Pipeline** (`.github/workflows/backend-ci.yml`):

**Features Implemented:**
- ✅ **Test Job**: Runs tests with MySQL 8 service
- ✅ **Build Job**: Builds JAR artifact (depends on test)
- ✅ **Code Quality**: Checkstyle and dependency tree checks
- ✅ **Security Scan**: OWASP Dependency Check for vulnerabilities
- ✅ **Docker Build**: Builds and pushes Docker images to Docker Hub (on main branch)
- ✅ **Path-based Triggers**: Only runs on relevant file changes
- ✅ **Artifact Upload**: Test results and JAR artifacts preserved

**Key Features:**
- Path-based workflow triggers (only runs when `src/`, `pom.xml`, or workflow file changes)
- MySQL service container for integration tests
- Maven caching for faster builds
- Docker image building with metadata extraction
- Security vulnerability scanning
- Code quality checks

**Workflow Jobs:**
1. **test** - Runs unit and integration tests
2. **build** - Builds application JAR (depends on test)
3. **code-quality** - Runs Checkstyle and dependency analysis
4. **security-scan** - OWASP Dependency Check
5. **docker-build** - Builds and pushes Docker image (only on main branch push)

**Deploy Pipeline** (`.github/workflows/deploy.yml` - ❌ **NOT YET IMPLEMENTED**):
```yaml
name: Deploy Pipeline

on:
  push:
    branches: [main]

jobs:
  deploy-backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build Docker image
        run: docker build -t case-observer-backend:${{ github.sha }} -f Dockerfile.backend .
      
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Push Docker image
        run: docker push case-observer-backend:${{ github.sha }}
      
      - name: Deploy to production server
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.HOST_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            docker pull case-observer-backend:${{ github.sha }}
            docker stop case-observer-backend-prod || true
            docker rm case-observer-backend-prod || true
            docker run -d \
              --name case-observer-backend-prod \
              --env-file /path/to/.env.prod \
              -p 8080:8080 \
              case-observer-backend:${{ github.sha }}
```

**Required GitHub Secrets (for CI):**
- ✅ `DOCKER_USERNAME` - Docker Hub username (required for docker-build job)
- ✅ `DOCKER_PASSWORD` - Docker Hub password (required for docker-build job)

**Required GitHub Secrets (for Deploy - when implemented):**
- `HOST` - Production server IP/hostname
- `HOST_USER` - SSH username
- `SSH_PRIVATE_KEY` - SSH private key for deployment

**Current CI Pipeline Status:**
- ✅ Tests run automatically on push/PR to main/master/develop
- ✅ Docker images built and pushed on main branch push
- ✅ Code quality checks run on every PR
- ✅ Security scans run automatically
- ❌ Automated deployment to production (needs deploy workflow)

---

### **Phase 3: Monitoring Setup (Week 3)**

#### **Step 3.1: Configure Prometheus**
**Action Items:**
1. Verify Actuator dependency is added (from Phase 1)
2. Verify Prometheus endpoint is enabled in `application-prod.properties`
3. Create `docker-compose.monitoring.yml`:
```yaml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
    networks:
      - case-observer-network

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana_data:/var/lib/grafana
    networks:
      - case-observer-network

volumes:
  prometheus_data:
  grafana_data:

networks:
  case-observer-network:
    external: true
```

4. Create `prometheus.yml`:
```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'case-observer'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend-prod:8080']
```

**Expected Outcome:**
- Prometheus scraping metrics from Actuator
- Grafana dashboard accessible
- Metrics visualization working

---

### **Phase 4: Frontend Integration (Week 4+)**

#### **Step 4.1: Initialize Next.js Frontend**
**Action Items:**
1. Create `frontend/` directory in project root
2. Initialize Next.js project:
```bash
cd frontend
npx create-next-app@latest . --typescript --tailwind --app --no-src-dir
```
3. Install dependencies:
```bash
npm install @tanstack/react-query axios zustand
npm install -D @types/node
```
4. Create API client (see Frontend-Backend Communication section)
5. Update `docker-compose.dev.yml` to include frontend service
6. Update `nginx.conf` to proxy frontend requests

**Expected Outcome:**
- Next.js app running on port 3000
- API calls working with CORS
- JWT authentication integrated

---

### **Phase 5: Production Deployment (Week 5)**

#### **Step 5.1: Production Environment Setup**
**Action Items:**
1. Set up production server (AWS EC2, DigitalOcean, etc.)
2. Install Docker and Docker Compose
3. Create `.env.prod` file with production secrets
4. Set up SSL certificates (Let's Encrypt)
5. Configure Nginx for SSL termination
6. Update `docker-compose.prod.yml` with production settings
7. Set up database backups
8. Configure monitoring alerts

**Production Checklist:**
- [ ] Server provisioned and secured
- [ ] Docker installed
- [ ] Environment variables configured
- [ ] SSL certificates installed
- [ ] Database backups scheduled
- [ ] Monitoring alerts configured
- [ ] CI/CD pipeline tested
- [ ] Health checks passing

---

## 📊 **Implementation Status Tracker**

| Phase | Task | Status | Priority |
|-------|------|--------|----------|
| Phase 1 | Add Actuator dependency | ⚠️ Pending | 🔴 Critical |
| Phase 1 | Implement CORS configuration | ⚠️ Pending | 🟡 High |
| Phase 2 | CI pipeline | ✅ **COMPLETE** | ✅ Done |
| Phase 2 | Create Deploy pipeline | ❌ Not Started | 🟡 High |
| Phase 3 | Configure Prometheus | ❌ Not Started | 🟢 Medium |
| Phase 3 | Set up Grafana | ❌ Not Started | 🟢 Medium |
| Phase 4 | Initialize Next.js frontend | ❌ Not Started | 🟡 High |
| Phase 4 | Integrate API client | ❌ Not Started | 🟡 High |
| Phase 5 | Production deployment | ⚠️ Partial | 🟡 High |

---

**Last Updated**: 2024-12-19  
**Next Review**: After Phase 1 completion

---

## 📝 **CI/CD Pipeline Details**

### **Current CI Pipeline** (`.github/workflows/backend-ci.yml`)

**Workflow Triggers:**
- Push to `main`, `master`, or `develop` branches
- Pull requests to `main`, `master`, or `develop` branches
- Only triggers on changes to:
  - `src/**` (source code)
  - `pom.xml` (dependencies)
  - `.github/workflows/backend-ci.yml` (workflow itself)

**Jobs Overview:**

1. **test** (Run Tests)
   - Sets up MySQL 8 service container
   - Runs Maven tests with test profile
   - Uploads test results as artifacts
   - Runs on every push/PR

2. **build** (Build Application)
   - Depends on `test` job success
   - Builds JAR artifact
   - Uploads JAR as artifact
   - Runs on every push/PR

3. **code-quality** (Code Quality Check)
   - Runs Checkstyle checks
   - Generates dependency tree
   - Non-blocking (continues on error)
   - Runs on every push/PR

4. **security-scan** (Security Vulnerability Scan)
   - Runs OWASP Dependency Check
   - Uploads vulnerability report
   - Non-blocking (continues on error)
   - Runs on every push/PR

5. **docker-build** (Build Docker Image)
   - Only runs on push to `main` branch
   - Depends on `test` and `build` jobs
   - Builds Docker image using `Dockerfile.backend`
   - Pushes to Docker Hub with tags:
     - Branch name tag
     - SHA-prefixed tag
     - `latest` tag (for main branch)
   - Uses GitHub Actions cache for faster builds

**Required Secrets:**
- `DOCKER_USERNAME` - For Docker Hub authentication
- `DOCKER_PASSWORD` - For Docker Hub authentication

**Missing Components:**
- ❌ Deployment workflow (needs to be created)
- ❌ Frontend CI pipeline (when frontend is implemented)
