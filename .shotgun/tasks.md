# Task Management

## Instructions for AI Coding Agents

When working on these tasks:
1. Focus on ONE stage at a time, completing all tasks in that stage before moving to the next.
2. Mark each task complete by replacing `[ ]` with `[X]` as you finish it.
3. Do NOT modify any other content in this file unless explicitly instructed by the user.
4. Tasks without an `[X]` are not finished yet.


### Stage 1: Critical Characterization Tests (PhotoSessionService, ClientController, Security)
- [ ] In `src/test/java/org/ks/photoapp/domain/photosession/PhotoSessionServiceTest.java`, create unit tests for `PhotoSessionService` covering: creating a new session from a valid DTO, updating an existing session (happy path only), and querying unfinished sessions via the existing public methods. Use mocks or in-memory repositories as appropriate and assert current behavior based on existing implementation.
- [ ] In `src/test/java/org/ks/photoapp/web/client/ClientControllerMvcTest.java`, create Spring MVC tests for `ClientController` (temporarily using its current package if not yet moved) covering: list all clients view, show single client details, create client (GET form + POST submit, happy path), and delete client verifying redirect and flash attribute behavior.
- [ ] In `src/test/java/org/ks/photoapp/config/security/CustomSecurityConfigTest.java`, add tests verifying that: the login page is accessible, static resources paths are accessible without authentication, and a sample critical application URL is reachable with current `.anyRequest().permitAll()` configuration.

### Stage 2: Move Controllers to `web.<feature>` and Fix Basic Layering
- [ ] In `src/main/java/org/ks/photoapp/web/client/ClientController.java`, move `ClientController` class from `org.ks.photoapp.domain.client` to this new package, updating the `package` declaration, imports, and any references (including test packages). Ensure that `@Controller` and `@RequestMapping` annotations remain functionally equivalent and that Stage 1 MVC tests are updated and still pass.
- [ ] In `src/main/java/org/ks/photoapp/web/photosession/PhotoSessionController.java`, move any existing photo session-related controller(s) found under `org.ks.photoapp.domain.photoSession` (or similarly named packages) into `org.ks.photoapp.web.photosession`, adjusting package declaration, imports, and updating or adding MVC tests to keep behavior unchanged.
- [ ] In `src/main/java/org/ks/photoapp/web`, scan for any remaining `@Controller` or `@RestController` classes still living under `org.ks.photoapp.domain..*`; for each, create a corresponding `web.<feature>` package (e.g., `web.payment`, `web.user`) and move the controllers there, ensuring they no longer access repositories directly (they must use existing services from `domain.<feature>`).

### Stage 3: Refactor PhotoSessionService and ClientController Hotspots
- [ ] In `src/main/java/org/ks/photoapp/domain/photosession/PhotoSessionService.java`, refactor the most complex public methods (especially `createNewSession` and `updateSession`) by extracting private helper methods such as `loadSessionOrThrow`, `loadClientOrThrow`, `updatePaymentFromDto`, and `updatePhotosFromDto`, without changing observable behavior. Ensure Stage 1 tests remain green.
- [ ] In `src/main/java/org/ks/photoapp/domain/photosession/PhotoSession.java` and related entities (`Payment`, `Photos`), introduce small domain methods where it clearly simplifies `PhotoSessionService` (e.g., `applyUpdateFrom(dto, client)` or `updatePaymentFrom(dto)`) and adjust the service to use them, keeping logic equivalent and tests passing.
- [ ] In `src/main/java/org/ks/photoapp/web/client/ClientController.java`, simplify controller methods so that each handler delegates to services, prepares the model, and returns a view or redirect. Extract repeated logic (e.g., notification/flash attribute setup, common model attributes) into private methods within the controller or a small helper component. Keep all existing routes and view names intact according to tests.

### Stage 4: Implement Feature-Specific Exceptions and Centralized ControllerAdvice
- [ ] In `src/main/java/org/ks/photoapp/domain/client/exception/ClientNotFoundException.java` and `src/main/java/org/ks/photoapp/domain/photosession/exception/PhotoSessionNotFoundException.java`, create runtime exception classes for not-found scenarios, following the naming and package conventions (simple constructors accepting message and/or ID).
- [ ] In `src/main/java/org/ks/photoapp/domain/photosession/exception/SessionUpdateNotAllowedException.java`, create a domain-specific runtime exception to signal business rule violations when updating sessions.
- [ ] In `src/main/java/org/ks/photoapp/domain/client/ClientService.java` and `src/main/java/org/ks/photoapp/domain/photosession/PhotoSessionService.java`, replace usages of generic exceptions (`NullPointerException`, `IllegalArgumentException`, `IllegalStateException`) used for domain errors with the new feature-specific exceptions, preserving current conditions and messages.
- [ ] In `src/main/java/org/ks/photoapp/web/GlobalExceptionHandler.java`, implement a `@ControllerAdvice` that maps the new feature-specific exceptions to consistent HTTP responses or view names (e.g., not-found page or redirect with error flash attribute). Ensure it covers at least `ClientNotFoundException`, `PhotoSessionNotFoundException`, and `SessionUpdateNotAllowedException`.
- [ ] In `src/test/java/org/ks/photoapp/web/GlobalExceptionHandlerTest.java`, add MVC tests or slice tests verifying that when the services throw the new exceptions, the `@ControllerAdvice` produces the expected responses (status codes, view names, and model/flash attributes).

### Stage 5: Standardize DTO Mapping, Naming, and Package Conventions
- [ ] In `src/main/java/org/ks/photoapp/domain/photosession/dto/PhotoSessionDto.java` and `src/main/java/org/ks/photoapp/domain/photosession/mapper/PhotoSessionDtoMapper.java`, ensure DTO and mapper exist in these packages (create or move as needed) and centralize all `PhotoSession`↔DTO mapping logic into the mapper, removing duplicate mapping code from `PhotoSessionService` and controllers.
- [ ] In `src/main/java/org/ks/photoapp/domain/client/dto/ClientDto.java` and `src/main/java/org/ks/photoapp/domain/client/mapper/ClientDtoMapper.java`, ensure these DTO and mapper classes exist and that `ClientController` and `ClientService` use them for mapping instead of manual field-by-field mapping.
- [ ] In `src/main/java/org/ks/photoapp/domain/photosession`, rename the `photoSession` package to `photosession` (all lowercase) and update all package declarations and imports accordingly across the project, including tests and configuration. Run the full test suite to confirm no breakage.
- [ ] In `src/main/java/org/ks/photoapp/domain/photosession/PhotoSessionService.java` and `src/main/java/org/ks/photoapp/web/client/ClientController.java`, rename any generic methods like `getAll()` or `findAllUnfinishedPhotoSession` to more intention-revealing names (e.g., `getAllSessions`, `getAllUnfinishedSessions`) and update all call sites and tests.
- [ ] In entity classes such as `src/main/java/org/ks/photoapp/domain/photosession/PhotoSession.java`, replace any public mutable fields (e.g., `public boolean isContractFinished`) with private fields and appropriate getters/setters or domain methods, ensuring serialization and JPA annotations still work and that existing behavior remains unchanged.

### Stage 6: Extend Refactoring Pattern to Other Features and Increase Coverage
- [ ] In `src/test/java/org/ks/photoapp/domain/payment/PaymentServiceTest.java`, add unit tests for `PaymentService` covering typical CRUD operations and any business rules inferred from the current implementation, following the same style as `PhotoSessionServiceTest`.
- [ ] In `src/test/java/org/ks/photoapp/web/photosession/PhotoSessionControllerMvcTest.java`, add MVC tests for the photo session web controller covering list, detail, create, update, and delete flows, similar to `ClientControllerMvcTest`.
- [ ] In `src/test/java/org/ks/photoapp/domain/user/UserServiceTest.java` and `src/test/java/org/ks/photoapp/web/user/UserControllerMvcTest.java`, introduce basic service and controller tests for user management flows, aligning with the established testing and layering standards.
- [ ] In the build configuration (e.g., `pom.xml` or `build.gradle`), optionally add or configure static analysis tools or plugins (e.g., Checkstyle, PMD, or SpotBugs) with rules to prevent `@Controller` classes under `org.ks.photoapp.domain..*` and to flag overly complex methods in services and controllers.
