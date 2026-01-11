# Research Index

## PhotoApp – model domenowy płatności i kontraktów
**Question:** Jaki jest aktualny model domenowy Payment/Contract w PhotoApp, jak wygląda nazewnictwo, istniejące endpointy i reguły biznesowe, aby dopasować nowy endpoint PATCH /api/payments/{paymentId}?  
**Finding:** W PhotoApp istnieje encja `Payment` powiązana relacją 1‑1 z `PhotoSession`, która zawiera flagi płatności (`isDepositPaid`, `isBasePaid`, `isAdditionalPaid`), natomiast stan kontraktu jest reprezentowany przez pole `isContractFinished` w `PhotoSession`. Aktualnie istnieje tylko prosty kontroler `PaymentController` z endpointem GET `/update-paid`, bez REST‑owego API JSON ani obiektowego modelu statusu płatności.
**Details:** See `.shotgun/research/payment-contract-domain-photoapp.md`.
