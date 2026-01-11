# Specification: PhotoApp Clean Code Standards

## TLDR

**Key Points:**
- Define pragmatic, enforceable clean code standards tailored to PhotoApp’s current Spring Boot MVC setup.
- Focus on consistent layering, naming, complexity limits, DTO mapping, error handling, and minimal test expectations.

**Major Features:**
- Layering and package conventions for web, application/services, and domain.
- Naming rules for packages, classes, methods, and fields.
- Function size/complexity and responsibility guidelines, especially for controllers/services.
- DTO mapping and usage rules across layers.
- Exception and error-handling patterns suitable for MVC with Thymeleaf.
- Minimal test expectations that must be met before refactors.

## 1. Layering and Package Conventions

### 1.1 Target high-level architecture

PhotoApp should follow a simple 3-layer structure within `org.ks.photoapp`:

- **Web layer (`web`)**
  - Spring MVC controllers (`@Controller`, `@RestController`).
  - Request/response handling, view names, and HTTP-specific concerns.
- **Application layer (`application` or `domain.<feature>.service`)**
  - Use case orchestration, transactional boundaries, calling repositories.
  - Mapping between domain entities and DTOs.
- **Domain & persistence layer (`domain`)**
  - JPA entities, value objects, enums, repositories.
  - Domain behavior on entities where it simplifies services.

Short term, we keep entities and repositories under `domain.<feature>` and move HTTP controllers into `web`.

### 1.2 Package structure conventions

- **Base package:** `org.ks.photoapp` for all application code.
- **Feature-oriented subpackages** under `domain` (already present): `client`, `photoSession`, `photos`, `payment`, `sessionType`, `user`.
- **Web controllers:**
  - Move or create controllers under `org.ks.photoapp.web.<feature>`.
  - Example: `ClientController` → `org.ks.photoapp.web.client.ClientController`.
- **Services & mappers:**
  - Keep feature services under `org.ks.photoapp.domain.<feature>` for now (e.g., `domain.client.ClientService`).
  - DTO mappers live next to services: `domain.<feature>.mapper` or `domain.<feature>.dto`.
- **Config:** framework/infrastructure config stays in `org.ks.photoapp.config` and subpackages (e.g., `config.security`).

**Rules:**
- No controller class is allowed in `org.ks.photoapp.domain..*` packages.
- Repositories (`JpaRepository` etc.) remain in `domain.<feature>` and are only used by services, not by controllers.

## 2. Naming Rules

### 2.1 Package names

- Use **lowercase only**, no camelCase:
  - `org.ks.photoapp.domain.photosession` instead of `photoSession`.
- Feature packages should be **noun-based** and singular when referring to an aggregate:
  - `client`, `photosession`, `payment`, `user`, `sessiontype`.
- Web feature packages mirror domain feature names:
  - `web.client`, `web.photosession`, etc.

### 2.2 Class names

- **Controllers:** `XxxController` (MVC, view-based) or `XxxRestController` (JSON API).
- **Services:** `XxxService` for application services.
- **Repositories:** `XxxRepository` for Spring Data repositories.
- **DTOs:** `XxxDto` (e.g., `PhotoSessionDto`).
- **Mappers:** `XxxDtoMapper` or `XxxMapper` colocated with the feature.
- **Config:** `XxxConfig` or `XxxSecurityConfig`.

Guideline: class names should express role + concept, e.g., `PhotoSessionService`, not `SessionManager`.

### 2.3 Method names

- Use **verb + object** for operations: `createClient`, `updateSession`, `findClientByEmail`.
- Avoid generic names like `getAll()`; include the subject and filter:
  - Prefer `getAllClients`, `getAllUnfinishedSessions`.
- Boolean-returning methods start with `is`, `has`, `can`: `isContractFinished`, `hasOutstandingPayment`.
- Controller method names should reflect use case, not HTTP verb: `showClientList`, `processClientForm`, `deleteClient`.

### 2.4 Field and variable names

- **No public mutable fields** on entities or DTOs; use private + getters/setters.
- Names must be descriptive, not encoded:
  - `photoSessionDate` instead of `psd`.
- Use English consistently for code, even if labels are Polish.
- Constants use upper snake case and live at top of class:
  - `private static final String NOTIFICATION_ATTRIBUTE = "notification";`.

## 3. Function Size, Complexity, and Responsibilities

### 3.1 Controllers

- Each controller method should:
  - Handle **one logical action** (e.g., show form, submit form, list items).
  - Contain **minimal logic**: parameter binding, simple validations, calling a service, selecting a view name.
- Limit **controller method size** to roughly:
  - Max ~30 lines of code.
  - At most a couple of simple `if` branches; anything more moves to a service.
- Controllers **must not**:
  - Access repositories directly.
  - Contain business rules or entity graph manipulation.
  - Build entities manually; they work with DTOs provided by services.

### 3.2 Services

- Service methods represent **use cases**: `createPhotoSession`, `finishSession`, `assignClientToSession`.
- Each service method should typically:
  - Validate input (or delegate to domain methods).
  - Load necessary aggregates via repositories.
  - Coordinate domain methods and save via repositories.
  - Map to/from DTOs.
- Complexity limits:
  - Prefer service methods under ~40–50 lines.
  - If a method needs to touch multiple aggregates (like `PhotoSession`, `Payment`, `Photos`), extract private helper methods:
    - `loadSessionOrThrow`, `updatePaymentFromDto`, `updatePhotosFromDto`.
- Avoid more than **2–3 levels of nested `if`/`for`** in a single method. Extract methods instead.

### 3.3 Domain entities

- Entities can expose **domain behavior** to reduce service complexity, e.g.:
  - `photoSession.applyUpdateFrom(dto, client)`.
- Keep entity methods side-effecting only their own state, not orchestrating repositories or services.

## 4. DTO Mapping Approach

### 4.1 General principles

- **Web layer ↔ DTOs**, **Services ↔ entities + DTOs**, **Repositories ↔ entities only**.
- Controllers should not use JPA entities directly in method signatures or views; they work with DTOs.
- DTOs are **shallow, web-facing models** designed for forms and views.

### 4.2 Location of DTOs and mappers

- DTOs live in `org.ks.photoapp.domain.<feature>.dto`.
- Mappers live in `org.ks.photoapp.domain.<feature>.mapper` or in the same package as DTOs.
- Service classes use these mappers; controllers do not perform mapping manually.

### 4.3 Mapping style

- For now, use **explicit mapper classes** instead of embedding mapping logic in services:
  - `PhotoSessionDtoMapper.toDto(entity)`
  - `PhotoSessionDtoMapper.toEntity(dto, relatedEntities...)`
- Avoid duplicating mapping logic in multiple places. If you see the same mapping in more than one method, extract/extend the mapper.

## 5. Exception and Error-Handling Patterns

### 5.1 Domain and application exceptions

- Replace generic `NullPointerException`, `IllegalArgumentException`, `IllegalStateException` for business errors with **feature-specific runtime exceptions**, e.g.:
  - `ClientNotFoundException`, `PhotoSessionNotFoundException`, `SessionUpdateNotAllowedException`.
- These exceptions live in `domain.<feature>.exception` packages.

### 5.2 Web error handling

- Use a **central `@ControllerAdvice`** in `org.ks.photoapp.web` to map domain/app exceptions to HTTP responses or error views.
- Controllers and services **should not** catch and handle every domain exception individually unless they need special behavior.

### 5.3 Validation and user feedback

- Use Bean Validation (`@Valid`, constraint annotations on DTOs) for input validation where possible.
- For view-based controllers:
  - On validation errors, return to the form view with the same DTO and error messages.
  - Use a limited, consistent set of flash attribute keys for success/error notifications (e.g., `NOTIFICATION_ATTRIBUTE`).

## 6. Minimal Testing Expectations Before Refactors

### 6.1 General rules

Before significant refactors (renames, moving packages, splitting methods), ensure there are at least **characterization tests** that:

- Cover the main happy paths of the affected public methods.
- Assert current behavior of key branches and edge cases that are about to be touched.

### 6.2 Priority areas in PhotoApp

- **PhotoSessionService**
  - Tests for `createNewSession`, `updateSession`, and queries like `findAllUnfinishedPhotoSession`.
- **ClientController** (after it is moved to `web.client`)
  - Spring MVC tests for list view, detail view, create, update, delete flows.
- **Security configuration** (`CustomSecurityConfig`)
  - Tests verifying that login, logout, and static resources behave as expected.

### 6.3 Test style

- Use `@SpringBootTest` or slice tests (`@WebMvcTest`, `@DataJpaTest`) depending on the layer:
  - Controllers: `@WebMvcTest` with mocked services.
  - Services: plain unit tests with mocked repositories or `@DataJpaTest` for persistence logic.
- At minimum, write tests that would **fail** if a refactor breaks routes, view names, or core service behavior.
