# PhotoApp Code Quality Audit

## Scope

Quick audit of `D:\Appki\Kamka\PhotoApp` focusing on:
- Overall architecture and layering
- Module boundaries and dependencies
- Naming quality (classes, methods, variables, files)
- Long or messy functions / obvious code smells
- Duplication and tight coupling
- Test coverage/structure

Assessment is based on project structure, representative files, and key domain modules.

## High-level architecture and layering

### What exists

- **Framework:** Spring Boot 3.3.2, Java 17, Thymeleaf, Spring Security, Spring Data JPA, Liquibase, H2/MySQL.
- **Top-level packages:**
  - `org.ks.photoapp`
    - `config` (with `security` subpackage)
    - `domain` (subpackages: `client`, `payment`, `photos`, `photoSession`, `sessionType`, `user`)
    - `web` (thin MVC controllers: `HomeController`, `LoginController`, `RegistrationController`)
- **Domain subpackages:** follow a somewhat feature-oriented structure (per aggregate: client, payment, photoSession, etc.) with `Controller`, `Service`, `Repository`, `DtoMapper` within some of them.

### Observations / issues

1. **Mixed architectural styles, but overall MVC-ish**
   - Follows a classic Spring MVC layering (Controller → Service → Repository + JPA entities + DTO mappers).
   - "Domain" package is used as a broad bucket for both **true domain model** (entities) and **application/service layer** (services, controllers, mappers) which blurs boundaries.

2. **Domain vs web responsibilities are muddled**
   - Example: `ClientController` is inside `org.ks.photoapp.domain.client`, while there is also a separate `org.ks.photoapp.web` package.
   - This mixes **feature-level HTTP concerns** with **domain logic**, instead of keeping HTTP controllers together in a `web` / `api` layer.

3. **Feature boundaries are implicit, not explicit**
   - Packages like `photoSession`, `client`, `payment` align with features, which is good, but there is no explicit separation between:
     - API models vs. domain models vs. persistence models
     - Application services vs. domain services vs. infrastructure
   - Repositories and DTO mappers live in the same package as entities and controllers, suggesting low conceptual cohesion.

4. **Security config is mostly centralized but simplistic**
   - `CustomSecurityConfig` defines `SecurityFilterChain`, ignores static resources and some paths, and sets CSRF exceptions for specific endpoints.
   - Currently `authorizeHttpRequests` uses `.anyRequest().permitAll()`, so authentication/authorization is effectively disabled (except for form login mechanics).

### Priority architecture issues

- **Clarify layering:** distinguish web/API layer, application/service layer, and domain model.
- **Move feature controllers out of `domain` into `web` or `web.<feature>` packages** for clear HTTP boundary.
- **Define clear DTO vs entity usage per layer** and avoid leaking JPA entities into the view layer.

## Module boundaries and dependencies

### Example: `ClientController`

- Lives in `org.ks.photoapp.domain.client` but annotated as `@Controller` (web concern).
- Depends on `ClientService` and `PhotoSessionService`.
- It handles:
  - HTTP endpoints (`/client/current`, `/client/all`, `/client/{id}`, etc.).
  - View model preparation: populating `Model` with `clients`, `photoSession`, `heading`.
  - Redirect targets and flash attributes for notifications.

This implies **two-way coupling** between `client` and `photoSession` features via services, and tight coupling of presentation and domain concerns in the same package.

### Example: `PhotoSessionService`

- Lives in `org.ks.photoapp.domain.photoSession` and is annotated `@Service`.
- Dependencies:
  - `PhotoSessionRepository` (same package)
  - `ClientRepository` from `domain.client`
  - Domain entities: `PhotoSession`, `Payment`, `Photos`, `Client`
  - DTOs and mappers: `PhotoSessionDto`, `PhotoSessionDtoMapper`.

Responsibilities:
- CRUD-style operations: `getAll`, `findById`, `createNewSession`, `deleteSession`, `updateSession`, `findAllUnfinishedPhotoSession`.
- Querying by client and date.
- Building and wiring together related entities (`Payment`, `Photos`, `Client`) when creating a new session.
- Mapping between entities and DTOs.

Issues:
- **Service does a lot:** persistence orchestration, validation, entity aggregation, DTO mapping.
- **Direct manipulation of related aggregates** (payments, photos) tightly couples `PhotoSessionService` to those internals.
- **Error handling style:** uses `NullPointerException` and `IllegalStateException` for business-level errors (not ideal for clearer flow / API responses).

### Security configuration (`CustomSecurityConfig`)

- Single class for most security configuration.
- Uses a mix of:
  - `SecurityFilterChain` bean with request authorization, login/logout, CSRF.
  - `WebSecurityCustomizer` to ignore static resource paths.

Issues:
- `.anyRequest().permitAll()` means no actual protection at the moment; may be temporary but important to flag.
- CSRF ignores specific paths; worth reviewing for correctness and whether those endpoints are idempotent.

### Priority boundary issues

- **Feature coupling:** `client` and `photoSession` modules depend on each other via services and repositories; there is a risk of circular logic and making changes difficult.
- **DTOs and entities mixed:** mapping logic in services and mappers is tightly bound to entity structure, limiting evolution of domain independently from API/UI.

## Naming quality

### Package and class names

- **Good:**
  - Packages: `domain.client`, `domain.photoSession`, `config.security` are clear and domain oriented.
  - Class names like `ClientService`, `ClientRepository`, `PhotoSessionService`, `PhotoSessionRepository`, `CustomSecurityConfig` are conventional and understandable.

- **Inconsistent/problematic:**
  - Package `photoSession` uses camelCase in package name (`photoSession`) instead of all-lowercase (`photosession` or `photo_session`), which breaks typical Java conventions.
  - `Photos` entity vs `PhotoSession` vs `SessionType` – acceptable but might benefit from more precise naming if responsibilities evolve.
  - View templates referenced (e.g. `"current-client"`, `"all-clients"`) use hyphenated English while labels in Polish (`heading` values): mixed language usage is okay but should be consistent.
  - Constants: `NOTIFICATION_ATTRIBUTE` in `ClientController` is good, but other repeated attribute names are inline string literals.

### Method and variable names

- Generally descriptive and readable: `createNewSession`, `updateSession`, `getAllCurrentClients`, `getPhotoSessionByClientId`.
- Some issues:
  - Boolean flags such as `isContractFinished` are **public field** on `PhotoSession` and used as `photoSession.isContractFinished` rather than via accessors.
  - Methods like `getAll()` in `PhotoSessionService` lack context (`getAll` what?); better would be `getAllActiveSessions` or similar to match filter.

### Priority naming fixes

- Normalize **package naming** (lowercase, no camelCase) if possible.
- Make service/API methods more intention-revealing (`getAllUnfinishedSessions` vs `getAll`).
- Replace public fields with proper accessors on entities and consistently use getters/setters.

## Long or messy functions / code smells

### `PhotoSessionService`

- Methods are not huge, but `updateSession` is a notable hotspot:
  - Multi-step process: load session, validate client, update multiple aggregates (`PhotoSession`, `Payment`, `Photos`), and save.
  - Mixed responsibilities (validation, orchestration, mapping).
  - Uses generic `IllegalArgumentException` and `IllegalStateException` for domain errors.
  - Some repeated null checks and direct field assignments.

Refactoring ideas (for future plan):
- Extract smaller private methods: e.g. `loadSessionOrThrow`, `updatePayment`, `updatePhotos`, `updateClient`.
- Introduce dedicated domain or application-level exception types.
- Possibly use a domain method on `PhotoSession` like `applyUpdateFrom(dto, client)` to encapsulate updating logic.

### `ClientController`

- Overall size is moderate (~4kB file) but responsibilities include:
  - Multiple endpoints for list, detail, form, update, delete.
  - Both navigation and notification strings inline.
- Some path inconsistencies:
  - Delete redirects to `"redirect:/all-clients"` but list mapping is `"/client/all"` (inconsistency / probably a bug or requires alias route).

### General smells inferred from structure

Without parsing every file, the following are likely across the codebase:
- **Anemic domain model**: entities likely mostly getters/setters with logic pushed into services.
- **Overloaded service layer**: services manage entity graph wiring, validation, and persistence in procedural style.
- **Tight coupling between layers**: views/controllers probably depend directly on services that know about JPA entities.

## Duplication and cross-cutting issues

### Mapping and CRUD patterns

- Each domain feature (`client`, `payment`, `photoSession`, `user`) has its own `DtoMapper` and service methods that likely repeat similar CRUD/validation patterns.
- Potential duplication in:
  - Creating new entities from DTOs.
  - Mapping entities to DTOs.
  - Setting notification/flash attributes and redirects in controllers.

### Error handling

- Mixed use of `NullPointerException`, `IllegalArgumentException`, `IllegalStateException` as control flow.
- Lack of centralized error handling (e.g., `@ControllerAdvice`) suggests repeated exception-to-response mapping.

## Test coverage and structure

### Structure

- `src/test/java/org/ks/photoapp` directory exists but **no test files were listed** under it in the directory scan.
- `spring-boot-starter-test` is present in `pom.xml` but not used yet.

### Assessment

- **Effective test coverage: 0%** at this point based on repository contents.
- No unit tests, no integration tests, no MVC tests, no security tests.

### Impact

- Makes refactoring risky, especially around:
  - `PhotoSessionService` and related domain logic.
  - Controllers and view mappings.
  - Security configuration changes.

## Key hotspots and refactor priorities

### 1. Layering and package structure

- **Move controllers in `domain` packages into a dedicated `web` (or `web.client`, `web.photoSession`) layer.**
- Clarify responsibilities:
  - Web layer: HTTP, request/response, view mapping.
  - Application/service layer: use cases, transactions, coordination.
  - Domain layer: entities and domain logic.

### 2. Service complexity and coupling

- `PhotoSessionService` and similar services should be treated as **primary refactoring targets**:
  - Break down complex methods like `updateSession` into composable steps.
  - Encapsulate entity update logic in domain methods where appropriate.
  - Introduce domain-specific exceptions and centralize error-to-response mapping.

### 3. Naming and conventions

- Normalize Java package naming (no camelCase) and ensure consistent REST path naming vs. view names.
- Improve method names to clearly express intent.
- Remove or refactor public mutable fields on entities.

### 4. Introduce tests around critical flows

- Before deeper refactors, add **characterization tests** for:
  - `PhotoSessionService` (session create/update, unfinished session queries).
  - `ClientController` basic flows (list, detail, create, delete, update).
  - Security configuration behavior for login/logout and static resources.

### 5. Reduce duplication in mappers and controllers

- Standardize DTO mapping patterns (e.g., use MapStruct or consistent manual mappers) to reduce repeated mapping logic.
- Introduce base helper for flash notifications or a consistent pattern to avoid scattered string literals.

## Summary

- The PhotoApp codebase uses a conventional Spring Boot structure with reasonably named classes and a feature-oriented package structure.
- Main quality issues:
  - **Blurry architecture boundaries** between web, service, and domain layers.
  - **Overloaded services** doing validation, mapping, and entity graph management.
  - **Package and REST naming inconsistencies** and some direct public field access.
  - **No automated tests**, making changes risky.
- First refactoring steps should focus on clarifying layers and boundaries, extracting smaller service/domain methods, cleaning up naming/conventions, and adding tests around the most important use cases (photo sessions, clients, and security/login).
