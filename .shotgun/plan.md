# PhotoApp Refactoring Plan

## Stage 1: Establish Minimal Safety Net with Tests

### Purpose
Create basic characterization tests around the most critical flows so that subsequent refactors are safer.

### Prerequisites
- Existing PhotoApp builds and runs successfully.

### Scope & Focus
- `PhotoSessionService` core use cases
- `ClientController` key MVC flows (current placement in `domain.client`)
- Security login/logout and access to main pages

### Key Actions (for task agents)
- Add service-level tests for `PhotoSessionService` covering:
  - Creating a new session from a valid DTO
  - Updating an existing session (happy path)
  - Query for unfinished sessions
- Add MVC tests for `ClientController` covering:
  - List all clients view
  - Show single client details
  - Create client (GET form + POST submit, basic happy path)
  - Delete client and verify redirect
- Add security tests verifying `CustomSecurityConfig`:
  - Login page is accessible
  - Static resources are accessible without authentication
  - Critical app URLs are at least reachable (current behavior is effectively open access)

### Success Criteria
- Tests run and pass in CI or local environment.
- Failures would indicate breaking changes in controllers, service methods, or security config.

---

## Stage 2: Clarify Layering and Move Controllers to `web.<feature>`

### Purpose
Enforce a clear separation between web layer and domain/application logic according to the clean code standards.

### Prerequisites
- Stage 1 tests in place for `ClientController` and security.

### Scope & Focus
- Physical package restructuring for controllers.
- Basic cleanup of direct repository usage in controllers (if any).

### Key Actions (for task agents)
- Create feature-specific web packages:
  - `org.ks.photoapp.web.client`
  - `org.ks.photoapp.web.photosession`
  - Other web feature packages as needed during later work.
- Move `ClientController` from `org.ks.photoapp.domain.client` to `org.ks.photoapp.web.client.ClientController`:
  - Adjust `@RequestMapping` and imports as needed.
  - Ensure tests from Stage 1 are updated to new package and still pass.
- Identify any other controllers living under `domain.*` and move them to corresponding `web.<feature>` package.
- Verify controllers do not depend directly on repositories; if they do, introduce or use existing services in `domain.<feature>`.

### Success Criteria
- All HTTP controllers reside under `org.ks.photoapp.web.<feature>`.
- Stage 1 tests still pass without behavioral changes.
- No `@Controller` or `@RestController` classes are present in `org.ks.photoapp.domain..*`.

---

## Stage 3: Refactor Hotspots (PhotoSessionService, ClientController)

### Purpose
Reduce complexity, improve responsibilities, and better align key classes with clean code standards.

### Prerequisites
- Stage 2 completed (controllers in `web.<feature>` packages).
- Tests from Stage 1 passing after the move.

### Scope & Focus
- `PhotoSessionService` in `domain.photoSession` (or future `domain.photosession`).
- `ClientController` in `web.client`.

### Key Actions (for task agents)
- For `PhotoSessionService`:
  - Identify the most complex methods (e.g., `updateSession`, `createNewSession`).
  - Extract private helper methods to improve readability and reduce branching, such as:
    - `loadSessionOrThrow`
    - `loadClientOrThrow`
    - `updatePaymentFromDto`
    - `updatePhotosFromDto`
  - Consider moving pure entity state changes into domain methods on `PhotoSession`, `Payment`, `Photos` where it clearly simplifies the service.
  - Replace generic `NullPointerException`, `IllegalArgumentException`, `IllegalStateException` in business logic with temporary dedicated helper methods, preparing for Stage 4 exception types.
- For `ClientController`:
  - Ensure each handler method does only one thing: delegate to service, prepare model, return view/redirect.
  - Extract repeated patterns into private methods or helper components (e.g., common model attributes, notification handling).
  - Fix obvious naming inconsistencies (e.g., redirect paths) while keeping existing behavior as seen in tests.

### Success Criteria
- `PhotoSessionService` methods are shorter and more focused, with most complexity hidden behind private helpers or domain methods.
- `ClientController` methods remain under reasonable length and rely on services instead of embedding domain logic.
- Existing tests remain green; new tests can be added if useful to capture newly factored behavior.

---

## Stage 4: Introduce Feature-Specific Exceptions and Centralized `@ControllerAdvice`

### Purpose
Standardize error handling and domain-level failures to support consistent user-facing behavior.

### Prerequisites
- Stage 3 refactors in `PhotoSessionService` and `ClientController`.
- Clear understanding of typical failure modes (from audit and tests).

### Scope & Focus
- Domain and application-level exceptions for main features.
- Global exception handling in the web layer.

### Key Actions (for task agents)
- Create feature-specific exception packages:
  - `org.ks.photoapp.domain.client.exception`
  - `org.ks.photoapp.domain.photosession.exception`
  - Additional feature exception packages as needed.
- Introduce custom runtime exceptions, for example:
  - `ClientNotFoundException`
  - `PhotoSessionNotFoundException`
  - `SessionUpdateNotAllowedException`
- Update services to throw these exceptions instead of generic ones for domain-level failures.
- Implement a centralized `@ControllerAdvice` in `org.ks.photoapp.web` that:
  - Translates these exceptions to appropriate HTTP status codes and/or error views.
  - Sets standardized model or flash attributes for error messages.
- Update/extend tests to verify that:
  - Exceptions raised in services are mapped to the expected views or redirects.
  - Error responses remain user-friendly and predictable.

### Success Criteria
- No business logic paths rely on generic exceptions like `NullPointerException` for control flow.
- All major domain features have specific exception types for not-found and business rule violations.
- A single `@ControllerAdvice` (or a small, well-organized set) handles mapping exceptions to web responses.

---

## Stage 5: Standardize DTO Mapping, Naming, and Package Conventions

### Purpose
Align DTO usage, mappers, and naming with the clean code standards across the codebase.

### Prerequisites
- Earlier stages complete so that tests and exception handling are stable.

### Scope & Focus
- DTO and mapper classes for core features (client, photosession, photos, payment, user).
- Package naming and method naming clean-up.

### Key Actions (for task agents)
- DTO and mapper organization:
  - Ensure DTOs live in `org.ks.photoapp.domain.<feature>.dto`.
  - Ensure mappers live in `org.ks.photoapp.domain.<feature>.mapper` or alongside DTOs.
  - Centralize mapping logic inside mappers (e.g., `PhotoSessionDtoMapper`) rather than duplicating in services or controllers.
- Controller and service contracts:
  - Ensure controllers use DTOs in signatures and models, not JPA entities.
  - Services handle mapping between DTOs and entities using the standardized mappers.
- Naming normalization:
  - Normalize package names to lower case without camelCase (e.g., `photoSession` to `photosession`) where feasible.
  - Adjust method names in services/controllers to be intention-revealing (e.g., `getAllUnfinishedPhotoSession` to `getAllUnfinishedSessions`) while keeping tests updated.
  - Remove public mutable fields from entities; replace with private fields and getters/setters, or domain methods.
- Add or update tests as needed when renaming or moving DTOs/mappers to protect behavior.

### Success Criteria
- DTO and mapper classes follow a consistent naming and package convention across features.
- Controllers work exclusively with DTOs at the boundary; entities are hidden behind services and mappers.
- Package naming is standardized, and method/field names are clear and intention-revealing.
- All characterization and new tests are green after these structural cleanups.

---

## Stage 6: Continuous Hardening and Coverage Expansion

### Purpose
Leverage the improved structure to gradually increase test coverage and enforce clean code standards going forward.

### Prerequisites
- Stages 15 completed for the most critical modules.

### Scope & Focus
- Expand tests and refactors to remaining features (payment, photos, sessionType, user).
- Introduce lightweight enforcement mechanisms for the standards.

### Key Actions (for task agents)
- Add tests around remaining services and controllers similar to those in Stages 1 and 3.
- Incrementally refactor other services/controllers following the same patterns as for `PhotoSessionService` and `ClientController`.
- Optionally introduce static analysis or code style tools that enforce basic rules, such as:
  - No `@Controller` classes in `domain.*` packages.
  - Limits on method length or cyclomatic complexity (if a tool is available).
- Periodically review the codebase for lingering usages of generic exceptions, DTO mapping in controllers, or entities exposed to the web layer and refactor them.

### Success Criteria
- Key features all have at least basic test coverage protecting main flows.
- Codebase largely adheres to defined clean code standards.
- Future changes can be made more confidently thanks to tests and clearer architecture boundaries.
