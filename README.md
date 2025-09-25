## Case Observer

A Spring Boot application for observing and managing court cases. It integrates with an external portal to fetch case details, stores structured data in MySQL via JPA, and is prepared for building a clean REST API layer.

<details>
<summary>### Key Features (current)</summary>

- **Spring Boot 3.4 (Java 17)** with Web + Data JPA foundations
- **Flyway** database migrations (versioned schema)
- **MySQL 8** via Docker Compose; optional **H2** for local/dev
- **Lombok** for concise entities
- Domain model for Users, Court Cases, Parties, Hearings, Notifications, and User–Case links

</details>

<details>
<summary>### Architecture Overview</summary>

- **Domain layer (entities)**: JPA entities model the court domain: `User`, `CourtCase`, `Party`, `Hearing`, `Notification`, `UserCase`.
- **Data access (repositories)**: Spring Data JPA repositories for CRUD, lookups, and relationships.
- **Service layer (business logic)**:
  - `CourtCaseService`: orchestrates case creation from an external portal, avoids duplicates, maps external data to internal entities, supports monitoring toggles and refresh.
  - `UserService`: basic CRUD and user retrieval.
- **External integration**: `PortalQueryService` (referenced) fetches case details (e.g., via SOAP/HTTP) into `CaseDetailsDto`; results are mapped to entities.
- **Infrastructure**: Dockerized MySQL; Flyway migrations in `db/migration` applied on startup.

</details>

<details>
<summary>### Business Logic & Use Cases</summary>

## 🎯 **Core Business Purpose**
**Automated Court Case Monitoring System** for Romanian legal professionals to track cases, receive notifications, and stay updated on case progress without manual checking.

## 👥 **User Roles & Permissions**

### **USER Role**
- ✅ **Register/Login** with JWT authentication
- ✅ **Search cases** on Romanian Justice Portal
- ✅ **Add cases** to personal monitoring list
- ✅ **View own cases** only (user-specific data isolation)
- ✅ **Enable/disable monitoring** per case
- ✅ **Set notification intervals** (TODO: implementation)
- ✅ **Refresh case data** from portal
- ✅ **Custom case titles** and notes (TODO: implementation)

### **ADMIN Role** (Future)
- 🔮 **View all users** and their cases
- 🔮 **System administration** features
- 🔮 **Analytics** and reporting

## 📋 **Core Business Workflows**

### **1. User Registration & Authentication**
```java
POST /auth/register
{
  "username": "lawyer1",
  "email": "lawyer@firm.com", 
  "password": "secure123"
}
```
**Business Rules:**
- ✅ **Unique usernames** and emails (validated)
- ✅ **BCrypt password** hashing for security
- ✅ **Default USER role** assignment
- ✅ **JWT tokens** for stateless authentication
- ✅ **Refresh token** mechanism for long sessions

### **2. Case Discovery & Validation**
```java
GET /api/cases/fetch?caseNumber=12345/2025&institution=TRIBUNALUL_BUCURESTI
```
**Business Logic:**
- 🔍 **Portal Integration**: Queries Romanian Justice Portal via SOAP
- ✅ **Real-time Validation**: Verifies case exists before adding
- ✅ **Data Preview**: Shows case details without saving
- ✅ **Error Handling**: Graceful failure if case not found

**Portal Data Retrieved:**
- Case number, institution, department
- Procedural stage (Fond, Procedura, etc.)
- Case category and subject
- **All parties** (plaintiff, defendant, lawyers)
- **All hearings** (dates, times, judicial panels, solutions)

### **3. Case Addition & User Linking**
```java
POST /api/cases
{
  "caseNumber": "12345/2025",
  "institution": "TRIBUNALUL_BUCURESTI", 
  "caseName": "Popescu vs Ionescu",
  "user": "lawyer1"  // Optional - links to user
}
```
**Business Rules:**
- ✅ **Duplicate Prevention**: Case numbers must be unique system-wide
- ✅ **Portal Sync**: Fetches complete case data from official source
- ✅ **User Association**: Links case to specific user via `UserCase` entity
- ✅ **Data Enrichment**: Maps portal data to internal entities
- ✅ **Automatic Monitoring**: Cases start with monitoring enabled

**Data Mapping Process:**
```java
Portal Data → Internal Entities
├── CaseDetailsDto → CourtCase
├── HearingDto[] → Hearing[] (with date parsing)
├── PartyDto[] → Party[]
└── User + CourtCase → UserCase (many-to-many)
```

### **4. User-Specific Case Management**
```java
GET /api/cases  // Returns only current user's cases
```
**Security & Business Logic:**
- 🔒 **User Isolation**: Users only see their own cases
- ✅ **JWT Authentication**: Required for access
- ✅ **Dynamic Filtering**: Uses `SecurityContext` to get current user
- ✅ **Repository Query**: `findByUserUsername()` for efficient filtering

### **5. Case Monitoring & Updates**
```java
POST /api/cases/{id}/refetch  // Refresh case data
POST /api/cases/{id}/monitoring/activate?interval=60  // Enable monitoring
POST /api/cases/{id}/monitoring/deactivate  // Disable monitoring
```

**Business Rules:**
- ✅ **Real-time Sync**: Updates case with latest portal data
- ✅ **Status Tracking**: Monitors procedural stage changes
- ✅ **Hearing Updates**: Tracks new hearings, date changes
- ✅ **Flexible Monitoring**: Users can enable/disable per case
- ✅ **Notification Intervals**: Configurable (TODO: implementation)

## 🔄 **Business Rules & Constraints**

### **Data Integrity:**
- ✅ **Unique Case Numbers**: System-wide uniqueness enforced
- ✅ **User Isolation**: Users cannot access other users' cases
- ✅ **Portal Validation**: Cases must exist in official portal
- ✅ **Referential Integrity**: Cascade deletes for related entities

### **Security Rules:**
- ✅ **JWT Authentication**: Required for all case operations
- ✅ **Password Security**: BCrypt hashing with salt
- ✅ **Input Validation**: Bean validation on all DTOs
- ✅ **SQL Injection Prevention**: JPA/Hibernate parameterized queries

### **Business Logic Constraints:**
- ✅ **Monitoring Limits**: Notification intervals must be positive
- ✅ **Case Existence**: Cannot refetch non-existent cases
- ✅ **User Existence**: Cannot link cases to non-existent users
- ✅ **Portal Availability**: Graceful handling of portal downtime

## 🚀 **Future Business Features** (TODOs in Code)

### **Notification System:**
- 🔮 **Scheduled Monitoring**: Background jobs to check case updates
- 🔮 **Email/SMS Notifications**: Alert users of changes
- 🔮 **Custom Intervals**: Per-case notification frequency
- 🔮 **Notification History**: Track all sent notifications

### **Enhanced User Experience:**
- 🔮 **Custom Case Titles**: User-friendly names for cases
- 🔮 **Case Notes**: Personal annotations per case
- 🔮 **Bulk Operations**: Add multiple cases at once
- 🔮 **Case Categories**: User-defined case grouping

### **Analytics & Reporting:**
- 🔮 **Case Statistics**: Track case progress over time
- 🔮 **User Analytics**: Monitor user engagement
- 🔮 **Portal Health**: Track portal availability metrics
- 🔮 **Performance Metrics**: Response times, error rates

## 💼 **Business Value Proposition**

### **For Legal Professionals:**
- ⏰ **Time Savings**: No manual portal checking
- 📱 **Real-time Updates**: Immediate notifications of changes
- 📊 **Case Management**: Centralized case tracking
- 🔒 **Data Security**: Secure, user-isolated case access

### **For Law Firms:**
- 👥 **Multi-user Support**: Team case management
- 📈 **Scalability**: Monitor hundreds of cases
- 🔄 **Automation**: Reduce manual administrative work
- 📋 **Compliance**: Official government data source

**This is essentially a "Court Case CRM" system for Romanian legal professionals!** ⚖️🏛️

</details>

<details>
<summary>### Codebase State (what exists today)</summary>

- `entity/`
  - `User`: username/email unique, password, role enum (`ADMIN|USER`), timestamps; one-to-many with `UserCase`.
  - `CourtCase`: `caseNumber`, `imposedName`, `department`, `proceduralStage`, `category`, `subject`, `courtName`, `status`, `monitoringEnabled`; one-to-many `hearings` and `parties`.
  - `Hearing`: linked to `CourtCase`, includes `hearingDate`, `pronouncementDate`, `judicialPanel`, `solution`, `description`.
  - `Party`: name and role, linked to `CourtCase`.
  - `Notification`: user + case + message, `sentAt` timestamp.
  - `UserCase`: composite key mapping `User` to `CourtCase` with optional `customTitle`, `notes`, `monitoringStartedAt`.
- `repository/`
  - `CourtCaseRepository`: CRUD, `findByCaseNumber`, `existsByCaseNumber`.
  - `UserRepository`: `findByUsername`, `findByEmail`.
  - `HearingRepository`: find by `courtCaseId`.
  - `NotificationRepository`: find by `userId`.
  - `UserCaseRepository`: CRUD for composite key.
- `service/`
  - `CourtCaseService`: orchestrates portal fetch, entity mapping, monitoring toggles, and refresh.
  - `UserService`: user CRUD.
- Controllers/DTOs/Security: ✅ **COMPLETED** - Full REST API with JWT security implemented.

</details>

<details>
<summary>### Tech Stack</summary>

- **Language**: Java 17
- **Framework**: Spring Boot 3.4.3
- **Persistence**: Spring Data JPA (Hibernate)
- **Database**: MySQL 8 (runtime), H2 (optional)
- **Migrations**: Flyway
- **Build**: Maven (wrapper included)

</details>

<details>
<summary>### Project Structure</summary>

```
case-observer/
  src/
    main/
      java/ro/signsofter/caseobserver/
        CaseObserverApplication.java
        entity/            # User, CourtCase, Hearing, Party, Notification, UserCase
        repository/        # Spring Data repositories
        service/           # Business services
        controller/        # (to be added)
        external/          # PortalQueryService + DTOs
      resources/
        db/migration/      # Flyway SQL scripts (V1__init.sql, V2__...)
  compose.yaml             # MySQL 8 service
  pom.xml                  # Maven config & dependencies
  mvnw, mvnw.cmd           # Maven wrapper scripts
```

</details>

<details>
<summary>### Prerequisites</summary>

- JDK 17+
- Docker Desktop (for MySQL via compose)

</details>

<details>
<summary>### Quick Start</summary>

1) Start MySQL locally
```bash
docker compose up -d
```
2) Run the app (applies Flyway migrations)
```bash
./mvnw spring-boot:run
# or
./mvnw clean package && java -jar target/case-observer-0.0.1-SNAPSHOT.jar
```

Suggested properties for MySQL (e.g., in `application.properties`):
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/observer_all
spring.datasource.username=root
spring.datasource.password=12345
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.flyway.enabled=true
```

Optional H2 settings (local only):
```properties
spring.datasource.url=jdbc:h2:mem:observer;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=validate
spring.h2.console.enabled=true
spring.flyway.enabled=true
```

</details>

<details>
<summary>### Development Progress & Next Steps</summary>

## ✅ **COMPLETED FEATURES**

1) ✅ **Database Schema Alignment**
   - ✅ Added `V2__align_schema_to_entities.sql` migration
   - ✅ Aligned all entities with database schema

2) ✅ **REST API Implementation**
   - ✅ `POST /api/cases` - Create case with portal integration
   - ✅ `GET /api/cases` - List user-specific cases
   - ✅ `GET /api/cases/{id}` - Get case details
   - ✅ `GET /api/cases/fetch` - Preview case from portal
   - ✅ `POST /api/cases/{id}/refetch` - Refresh case data
   - ✅ `POST /api/cases/{id}/monitoring/activate|deactivate` - Toggle monitoring
   - ✅ `PUT /api/cases/{id}/notification-settings` - Update settings

3) ✅ **Authentication & Security**
   - ✅ `POST /auth/register` - User registration
   - ✅ `POST /auth/login` - JWT authentication
   - ✅ `POST /auth/refresh` - Token refresh
   - ✅ BCrypt password hashing
   - ✅ JWT-based stateless authentication
   - ✅ User-specific data isolation

4) ✅ **External Integration**
   - ✅ Romanian Justice Portal SOAP integration
   - ✅ Retry logic and timeout handling
   - ✅ Error handling with `PortalQueryException`
   - ✅ Data mapping from portal to internal entities

5) ✅ **Validation & Error Handling**
   - ✅ Bean Validation on all DTOs
   - ✅ Global `@ControllerAdvice` exception handler
   - ✅ Consistent JSON error responses

6) ✅ **Comprehensive Testing**
   - ✅ Controller tests with `@WebMvcTest`
   - ✅ Service tests with Mockito
   - ✅ Security tests with authentication
   - ✅ User-specific functionality tests

## 🔄 **IN PROGRESS**

7) 🔄 **Observability & Operations**
   - 🔄 Spring Boot Actuator health checks
   - 🔄 Structured logging and metrics
   - 🔄 Request tracing and monitoring

## ❌ **CANCELLED**

8) ❌ **OpenAPI Documentation**
   - ❌ Swagger UI implementation (skipped per user request)

## 🚀 **CURRENT STATUS**

**Production-Ready API** with:
- ✅ Complete REST API with all CRUD operations
- ✅ JWT Security with user authentication
- ✅ External portal integration for case data
- ✅ Comprehensive test coverage
- ✅ Global exception handling
- ✅ Database migrations and schema alignment

**Only observability remains for full production deployment!**

</details>

<details>
<summary>### Common Maven Commands</summary>

```bash
./mvnw clean           # Clean build artifacts
./mvnw test            # Run tests
./mvnw package         # Build jar in target/
./mvnw spring-boot:run # Run the app in dev mode
```

</details>

<details>
<summary>### Contributing</summary>

- Keep entities and migrations in sync (prefer additive `Vx__...` migrations).
- Add tests around service logic (portal mapping, monitoring toggles, refresh).
- Document new endpoints in OpenAPI and provide sample requests.

</details>

<details>
<summary>### Docker</summary>

`compose.yaml` provisions MySQL 8 with a persistent volume.
```yaml
services:
  mysql-db:
    image: mysql:8
    environment:
      MYSQL_DATABASE: observer_all
      MYSQL_ROOT_PASSWORD: 12345
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
```
Stop services when done:
```bash
docker compose down
```

</details>

<details>
<summary>### License</summary>

Add your preferred license in `LICENSE`.

</details>


