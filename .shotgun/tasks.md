# Task Management

## Instructions for AI Coding Agents

When working on these tasks:
1. Focus on ONE stage at a time, completing all tasks in that stage before moving to the next.
2. Mark each task complete by replacing `[ ]` with `[X]` as you finish it.
3. Do NOT modify any other content in this file unless explicitly instructed by the user.
4. Tasks without an `[X]` are not finished yet.


### Stage 1: REST API skeleton for PATCH /api/payments/{paymentId}
- [ ] In `src/main/java/.../payment/api/PaymentRestController.java`, create `PaymentRestController` class annotated with `@RestController` and `@RequestMapping("/api/payments")`.
- [ ] In `PaymentRestController`, add `patchPayment` method mapped to `PATCH /{paymentId}` with `@PatchMapping("/{paymentId}")`, parameters: `@PathVariable Long paymentId`, `@RequestBody PaymentPatchRequest request`.
- [ ] In `PaymentRestController`, ensure method returns `ResponseEntity<PaymentDto>` and delegates to `PaymentService.patchPayment(paymentId, request)`.
- [ ] In `src/main/java/.../payment/api/dto/PaymentPatchRequest.java`, create DTO class `PaymentPatchRequest` with nullable `Boolean` fields: `isDepositPaid`, `isBasePaid`, `isAdditionalPaid`, plus standard getters/setters.
- [ ] In `PaymentPatchRequest`, configure JSON mapping to respect missing fields vs `null` values (e.g. use Jackson, no default values). Field names must exactly match `PaymentPatchRequest` schema in `contracts/payment_patch_api.json`.

### Stage 2: PaymentDto and mapping
- [ ] In `src/main/java/.../payment/api/dto/PaymentDto.java`, create or update `PaymentDto` to match `PaymentDto` schema from `contracts/payment_patch_api.json` (fields: `id`, `deposit`, `basePayment`, `additionalPayment`, `isDepositPaid`, `isBasePaid`, `isAdditionalPaid`, `photoSessionId`, `isContractFinished`).
- [ ] In `PaymentDto`, ensure JSON property names exactly match OpenAPI schema (check `contracts/payment_patch_api.json`).
- [ ] In `src/main/java/.../payment/api/mapper/PaymentMapper.java`, create mapper interface/class with method `PaymentDto toDto(Payment payment)` that maps all fields from `Payment` and related `PhotoSession` (`photoSessionId`, `isContractFinished`).
- [ ] In `PaymentMapper.toDto`, ensure `isContractFinished` is taken from `payment.getPhotoSession().isContractFinished()` and `photoSessionId` from `payment.getPhotoSession().getId()`.

### Stage 3: Service layer and business logic
- [ ] In `src/main/java/.../payment/service/PaymentService.java`, add public method `PaymentDto patchPayment(Long paymentId, PaymentPatchRequest request)`.
- [ ] In `PaymentService.patchPayment`, load `Payment` by `paymentId` from `PaymentRepository`; if not found, throw domain-specific `PaymentNotFoundException`.
- [ ] In `PaymentService.patchPayment`, validate `PaymentPatchRequest`: if all three fields are `null` and no updatable data is present, throw `InvalidPaymentPatchRequestException` for HTTP 400 ("Request body must contain at least one updatable field").
- [ ] In `PaymentService.patchPayment`, apply partial update rules: for each non-null field in `request`, set corresponding boolean flag on `Payment`; leave other flags unchanged.
- [ ] In `PaymentService.patchPayment`, before applying updates, check if related `PhotoSession.isContractFinished == true`; if yes, throw `ContractAlreadyFinishedException` for HTTP 409 with message "Contract already finished for this payment" and do not modify any data.
- [ ] In `PaymentService.patchPayment`, after applying flag updates, recompute contract state: if all three `Payment` flags are `true`, set `photoSession.setContractFinished(true)`; never set it back to `false` if it is already `true`.
- [ ] In `PaymentService.patchPayment`, ensure the whole operation (updating `Payment` flags and potentially `PhotoSession.isContractFinished`) is executed in a single transaction (e.g. annotate method with `@Transactional`).
- [ ] In `PaymentService.patchPayment`, save updated `Payment` (and `PhotoSession` if needed) via repository and return mapped `PaymentDto` using `PaymentMapper`.

### Stage 4: Error handling and ControllerAdvice
- [ ] In `src/main/java/.../payment/api/PaymentErrorHandler.java` or existing global `@ControllerAdvice`, add handler for `PaymentNotFoundException` returning HTTP 404 with body `ErrorResponse` matching schema in `contracts/payment_patch_api.json` (`status`, `error`, `message`, optional `timestamp`, `path`).
- [ ] In error handler for `PaymentNotFoundException`, set `status = 404` and `error = "Not Found"` and a clear `message` (e.g. "Payment with id {id} not found").
- [ ] Add handler for `InvalidPaymentPatchRequestException` mapping to HTTP 400 with `ErrorResponse` and a message like "Request body must contain at least one updatable field" or detailed validation error.
- [ ] Add handler for `ContractAlreadyFinishedException` mapping to HTTP 409 with `ErrorResponse` and message "Contract already finished for this payment".
- [ ] Ensure existing/global handlers cover JSON parsing errors, invalid JSON type, unexpected fields and map them to 400 with `ErrorResponse` consistent with `specification.md` (e.g. unexpected fields, wrong types, null values).

### Stage 5: Integration and validation tests
- [ ] In `src/test/java/.../payment/api/PaymentRestControllerIT.java`, create integration tests class for `PATCH /api/payments/{paymentId}` using test framework used in PhotoApp (e.g. Spring Boot Test + MockMvc/WebTestClient).
- [ ] Add happy-path test: existing payment, partial update with `{"isDepositPaid": true}`; assert 200 OK and response body `PaymentDto` updated only for `isDepositPaid`, other flags unchanged, `isContractFinished` remains `false` if not all flags are `true`.
- [ ] Add test for closing contract: PATCH with all three flags `true` on payment whose `PhotoSession.isContractFinished` is `false`; assert 200 OK, all flags `true`, `isContractFinished` `true` in response and in DB.
- [ ] Add test for idempotency: call PATCH twice with the same body that sets all three flags to `true`; assert both responses are the same and DB state is not duplicated/changed unexpectedly.
- [ ] Add test for 400 when body is empty or `{}`; assert `ErrorResponse.status == 400` and meaningful `message`.
- [ ] Add test for 400 when body contains unexpected fields (e.g. `{ "foo": true }`); assert 400 and message about unexpected field.
- [ ] Add test for 400 when field has wrong type or `null` (e.g. `{ "isDepositPaid": "true" }` or `{ "isDepositPaid": null }`).
- [ ] Add test for 404 when `paymentId` does not exist; assert 404 and correct error payload.
- [ ] Add test for 409 when trying to PATCH payment whose `PhotoSession.isContractFinished == true`; assert 409 and correct error payload.
- [ ] Optionally, add test for invalid `paymentId` format in path (depending on existing global behavior: 400 or 404) to keep consistency with other endpoints.
