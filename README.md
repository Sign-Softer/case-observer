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
<summary>### Current Business Logic</summary>

- **Create case from portal** (`CourtCaseService#createCase`)
  - Rejects duplicates by `caseNumber`.
  - Fetches external case details via `PortalQueryService.fetchCaseDetails(caseNumber, institution)`.
  - Maps external data to `CourtCase`, `Hearing` list, and `Party` list; saves atomically.
- **Associate user with case** (`saveUserCase`)
  - Finds `User` by username; creates `UserCase` link.
- **Refresh existing case** (`refetchAndUpdateCase`)
  - Re-fetches external data and updates key fields on the stored case.
- **Monitoring controls**
  - `activateMonitoring` / `deactivateMonitoring` toggle `monitoringEnabled`.
  - Placeholder to persist notification intervals/custom names in future settings.
- **User management** (`UserService`)
  - Basic CRUD: list, get by id, create, update key fields, delete.

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
- Controllers/DTOs/Security: not yet implemented (service references `controller.dto.CreateCaseRequestDto`).

</details>

<details>
<summary>### Model–Schema Alignment (important)</summary>

Flyway `V1__init.sql` defines initial tables, but several fields diverge from the current entities. You should add a `V2__...` migration to align the database schema with the entity model (or adjust entities to match V1). Key differences:
- `court_case`
  - V1: has `case_id`, `case_name`, `court_name`, `status`, `last_updated`, `monitoring_enabled`.
  - Entity: uses `number` (mapped as `caseNumber`), `imposed_name`, plus `department`, `procedural_stage`, `category`, `subject`.
- `hearing`
  - V1: `hearing_date`, `solution`, `description`.
  - Entity: adds `judicial_panel` and `pronouncement_date` (both non-null in entity).
- `party`
  - Present as entity/table but missing in V1 migration; add create-table in V2 with FK to `court_case`.

Action: create a `V2__align_schema_to_entities.sql` migration to:
- Add/rename columns in `court_case` to match the entity fields (or change the entity column names to match V1).
- Alter `hearing` to add `judicial_panel` and `pronouncement_date` (nullable initially if needed, then backfill). 
- Create `party` table with `id`, `name`, `role`, and `case_id` FK.
- Validate `user_case`, `notification`, and `user` match current entities (they largely do).

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
<summary>### Next Development Steps</summary>

1) Align DB schema and entities
   - Add `V2__align_schema_to_entities.sql` (rename/add fields, add `party`, alter `hearing`).
   - Alternatively, adjust entity column mappings to match `V1__init.sql`.
2) Define API surface (controllers + DTOs)
   - Endpoints for cases, hearings, users, notifications, and user-case links.
   - Example minimal endpoints:
     - `POST /api/cases` create by `caseNumber` + `institution` (calls portal fetch)
     - `GET /api/cases` list; `GET /api/cases/{id}` details
     - `POST /api/cases/{id}:refresh` refetch from portal
     - `POST /api/cases/{id}:monitor` toggle on/off
3) Validation and error handling
   - Bean Validation on DTOs; global `@ControllerAdvice` for consistent JSON errors.
4) External integration hardening
   - Implement/configure `PortalQueryService` and its DTOs; add retries/timeouts, clear exceptions (`PortalQueryException`).
   - Map external enums/strings into internal enums where noted (TODOs in entities).
5) Security
   - Add Spring Security (JWT/session); secure mutating endpoints; roles from `User.Role`.
6) Observability & ops
   - Logging (structured), request tracing, basic metrics/health (`spring-boot-actuator`).
7) Documentation
   - OpenAPI via `springdoc-openapi-starter-webmvc-ui`; include examples and error schemas.

Example dependencies to add:
```xml
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
  <version>2.6.0</version>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

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


