# Specification: PATCH /api/payments/{paymentId} ￼f PhotoApp

## TLDR

**Key Points:**
- Endpoint PATCH `/api/payments/{paymentId}` umożliwia częściową aktualizację flag płatności `isDepositPaid`, `isBasePaid`, `isAdditionalPaid`.
- Po aktualizacji, jeżeli wszystkie trzy flagi są równe `true`, powiązana `PhotoSession.isContractFinished` jest automatycznie ustawiana na `true`.

**Major Features:**
- Ciało żądania JSON z opcjonalnymi polami boolean dla trzech flag płatności.
- Idempotentne PATCH: wielokrotne wywołanie z tymi samymi wartościami nie zmienia stanu.
- Brak zmian dla pól pominiętych w body; brak wsparcia dla częściowych dopłat kwotowych.
- Odpowiedź zawiera `PaymentDto` z aktualnym stanem płatności oraz `isContractFinished` powiązanej sesji.

## 1. Zakres funkcjonalny endpointu

Endpoint PATCH `/api/payments/{paymentId}` służy do aktualizacji stanu opłacenia poszczególnych części płatności (zaliczka, część bazowa, część dodatkowa) dla istniejącej encji `Payment`. Operacja jest częściowa (PATCH):

- klient może przesłać dowolną kombinację pól `isDepositPaid`, `isBasePaid`, `isAdditionalPaid`;
- tylko przesłane pola są aktualizowane;
- pola nieobecne w JSON pozostają bez zmian.

Dodatkowo, po każdej aktualizacji flag:
- system sprawdza stan trzech flag na `Payment`;
- jeżeli wszystkie trzy są `true`, to na powiązanej `PhotoSession` (relacja 1–1) pole `isContractFinished` jest ustawiane na `true`;
- jeżeli co najmniej jedna flaga jest `false`, kontrakt pozostaje otwarty (`isContractFinished = false` lub bez zmian, w zależności od istniejącej logiki).

## 2. Kontrakt API (OpenAPI)

Formalny kontrakt request/response, kody odpowiedzi oraz przykłady JSON są zdefiniowane w pliku OpenAPI:

- **Zobacz:** `contracts/payment_patch_api.json`

Ten plik zawiera:
- definicję ścieżki `/api/payments/{paymentId}` z operacją `patch`;
- schemat `PaymentPatchRequest` (body żądania);
- schemat `PaymentDto` (ciało odpowiedzi 200);
- schemat `ErrorResponse` dla błędów 4xx;
- przykładowe payloady JSON dla typowych scenariuszy.

## 3. Semantyka request body

### 3.1. Pola w `PaymentPatchRequest`

Body żądania jest obiektem JSON z trzema opcjonalnymi polami typu boolean:

- `isDepositPaid` – flaga opłacenia zaliczki;
- `isBasePaid` – flaga opłacenia części bazowej;
- `isAdditionalPaid` – flaga opłacenia części dodatkowej.

**Zasady:**
- Każdym z pól **może, ale nie musi** pojawić się w body.
- Jeżeli pole jest obecne z wartością `true` lub `false`, system ustawia odpowiadającą flagę na tę wartość.
- Jeżeli pole **nie jest obecne** w JSON, aktualna wartość flagi w bazie **pozostaje bez zmian**.
- Jeżeli pole jest przesłane z wartością `null`, jest to traktowane jako błąd walidacji (400 Bad Request); po stronie API można to zmapować na typy nieakceptujące `null`.

### 3.2. Przykłady request body

1. **Oznaczenie wszystkich części jako opłacone (zamyka kontrakt):**
   - Body:
     - `{"isDepositPaid": true, "isBasePaid": true, "isAdditionalPaid": true}`

2. **Częściowa aktualizacja (tylko zaliczka):**
   - Body:
     - `{"isDepositPaid": true}`

3. **Zmiana tylko jednej flagi na `false` (bez zmiany pozostałych):**
   - Body:
     - `{"isBasePaid": false}`

Szczegółowe przykłady znajdują się w `contracts/payment_patch_api.json` w sekcji `examples`.

## 4. Semantyka odpowiedzi i PaymentDto

Odpowiedź 200 OK zwraca aktualny stan płatności, zgodny z modelem domenowym `Payment` oraz powiązaną sesją:

Pola w `PaymentDto` (opisowo):
- `id` – identyfikator płatności (`Payment.id`);
- `deposit`, `basePayment`, `additionalPayment` – kwoty poszczególnych części płatności;
- `isDepositPaid`, `isBasePaid`, `isAdditionalPaid` – aktualne flagi opłacenia;
- `photoSessionId` – identyfikator powiązanej `PhotoSession`;
- `isContractFinished` – aktualny stan kontraktu dla powiązanej sesji.

Po stronie implementacji należy zadbać, aby wartość `isContractFinished` odzwierciedlała wynik automatycznej logiki domykania kontraktu po aktualizacji flag płatności.

## 5. Kody odpowiedzi i błędy

- **200 OK** – udana aktualizacja płatności. Zwracane jest `PaymentDto`.
- **400 Bad Request** – błąd walidacji, np.:
  - body jest puste;
  - żaden z dozwolonych kluczy nie został przesłany;
  - pola mają nieprawidłowy typ (np. string zamiast boolean) lub `null`.
- **404 Not Found** – płatność o podanym `paymentId` nie istnieje.
- **409 Conflict** – jeżeli kontrakt powiązany z daną płatnością jest już zamknięty (`PhotoSession.isContractFinished = true`) i nastąpi próba modyfikacji flag.

Struktura błędu jest opisana schematem `ErrorResponse` w `contracts/payment_patch_api.json` (pola: `status`, `error`, `message`, opcjonalnie `timestamp`, `path`).

## 6. Idempotencja i zachowanie przy wielokrotnych PATCH

- Operacja PATCH jest **idempotentna**: wielokrotne wywołanie z tym samym body prowadzi do tego samego końcowego stanu `Payment` oraz `isContractFinished`.
- Aktualizacja polega na ustawieniu konkretnej wartości boolean dla wskazanych flag, więc ponowne ustawienie `isDepositPaid = true` nie zmienia stanu po pierwszym razie.
- Automatyczne zamykanie kontraktu jest również idempotentne: jeżeli kontrakt został już oznaczony jako zakończony (`isContractFinished = true`), kolejne wywołania, które potencjalnie mogłyby go zamknąć, nie zmieniają stanu (zostaje `true`).

## 7. Zachowanie przy nieprzesłanych polach

- Jeżeli dane pole (np. `isAdditionalPaid`) **nie występuje** w body, jego aktualna wartość w bazie danych **nie jest modyfikowana**.
- To zachowanie musi być spójne z obsługą mapowania JSON → DTO w warstwie implementacji (np. odróżnienie `null` od "brak pola").
- Endpoint nie umożliwia resetowania pól do stanu "nieznany" – flagi zawsze przechowują wartość boolean.

## 8. Zasady domykania kontraktu

- Po wykonaniu logiki PATCH (zapisaniu zaktualizowanych flag w encji `Payment`):
  - system odczytuje aktualne wartości `isDepositPaid`, `isBasePaid`, `isAdditionalPaid`;
  - jeżeli wszystkie trzy są `true`, powiązana `PhotoSession.isContractFinished` zostaje ustawiona na `true`;
  - jeżeli którakolwiek z flag jest `false`, kontrakt pozostaje otwarty – tj. `isContractFinished` nie jest ustawiane na `true` przez ten endpoint.

- Po jednorazowym ustawieniu `PhotoSession.isContractFinished = true` (czyli zamknięciu kontraktu):
  - kontrakt **nie jest już nigdy automatycznie otwierany** przez ten endpoint;
  - żadne kolejne wywołanie PATCH (nawet z flagami ustawionymi na `false`) nie powoduje zmiany `isContractFinished` z `true` na `false`.

Implementacja powinna zapewnić, że aktualizacja `Payment` oraz ustawienie `isContractFinished` odbywają się w sposób spójny (najlepiej w jednej transakcji).

## 9. Walidacja i obsługa błędów

Ta sekcja doprecyzowuje, **kiedy** i **jakie** błędy HTTP zwraca endpoint PATCH `/api/payments/{paymentId}`, oraz w jaki sposób walidowane jest body żądania. Zasady są spójne z kontraktem w `contracts/payment_patch_api.json` (schematy `PaymentPatchRequest`, `PaymentDto`, `ErrorResponse`).

### 9.1. Walidacja wejścia (request)

Walidacja odbywa się w następujących etapach:

1. **Parsowanie JSON:**
   - Jeżeli body nie jest poprawnym JSON-em (np. błąd składni, ucięty dokument), endpoint zwraca **400 Bad Request** z `ErrorResponse`:
     - `status = 400`
     - `error = "Bad Request"`
     - `message` zawiera informację o problemie z parsowaniem (np. "Malformed JSON").

2. **Sprawdzenie typu struktury głównej:**
   - Body musi być **obiektem JSON** zgodnym ze schematem `PaymentPatchRequest`.
   - Jeżeli root nie jest obiektem (np. tablica, liczba, string) → **400 Bad Request**.

3. **Sprawdzenie dozwolonych pól (additionalProperties = false):**
   - Dozwolone są wyłącznie klucze: `isDepositPaid`, `isBasePaid`, `isAdditionalPaid`.
   - Jeżeli body zawiera **jakiekolwiek inne pola** (np. `foo`, `amount`) → **400 Bad Request**.
   - `ErrorResponse.message` powinno wskazywać, które pola są nieobsługiwane (np. "Unexpected field 'foo'").

4. **Sprawdzenie typów wartości:**
   - Dla każdego z dozwolonych kluczy, jeżeli występuje:
     - poprawne wartości: `true` lub `false` (boolean);
     - **niepoprawne wartości**: string (`"true"`), liczba (`1`), obiekt, tablica itd. → **400 Bad Request**.
   - Choć w OpenAPI typ jest zapisany jako `["boolean", "null"]` (w celu doprecyzowania semantyki), 
     po stronie walidacji API traktujemy **`null` jako błąd**:
     - jeżeli pole występuje z wartością `null` → **400 Bad Request**;
     - `ErrorResponse.message` powinien wskazywać, że wartość nie może być `null`.

5. **Puste lub semantycznie puste body:**
   - Jeżeli:
     - body jest **całkowicie puste** (brak treści HTTP body), lub
     - body po sparsowaniu jest pustym obiektem (`{}`), lub
     - body zawiera wyłącznie pola z wartością `null` (po odrzuceniu, zasadne jest traktowanie tego jak brak aktualizowalnych danych),
   - to nie ma **żadnych danych do aktualizacji**.
   - W takim przypadku endpoint zwraca **400 Bad Request** z komunikatem w `message` w stylu:
     - "Request body must contain at least one updatable field".

6. **Brak zmian vs. walidacja:**
   - Walidacja **nie sprawdza**, czy nowe wartości różnią się od dotychczasowych (np. ustawienie `isDepositPaid` na `true`, gdy już jest `true` jest **dozwolone**).
   - Taki przypadek jest obsługiwany jako poprawna, idempotentna operacja i prowadzi do odpowiedzi **200 OK**.

### 9.2. Walidacja ścieżki i autoryzacja

1. **Parametr `paymentId` (path):**
   - Musi być poprawnym identyfikatorem liczbowym (zgodnie z `int64` w OpenAPI).
   - Jeżeli nie można sparsować `paymentId` do typu liczbowego (np. `/api/payments/abc`) →
     - zależnie od ogólnych zasad API może to być traktowane jako **400 Bad Request** lub **404 Not Found**;
     - rekomendacja: spójność z resztą PhotoApp (należy zastosować istniejący wzorzec globalny).

2. **Autoryzacja i autentykacja:**
   - Endpoint stosuje **standardowy mechanizm autoryzacji API PhotoApp** (np. token, sesja), ale specyfikacja nie wchodzi w szczegóły implementacji.
   - Możliwe scenariusze błędów:
     - **401 Unauthorized** – brak ważnych danych uwierzytelniających (np. brak tokena, wygasły token);
     - **403 Forbidden** – użytkownik jest uwierzytelniony, ale nie ma prawa modyfikować płatności (np. nie jest właścicielem danej sesji/klienta lub nie ma roli uprawniającej do edycji).
   - W obu przypadkach zwracany jest `ErrorResponse` z:
     - `status = 401` lub `403`;
     - `error = "Unauthorized"` lub `"Forbidden"` (zgodnie z globalnym stylem API);
     - `message` z krótkim opisem przyczyny (np. "Authentication required" / "Insufficient permissions").

### 9.3. Walidacja istnienia Payment (404 Not Found)

Po pomyślnej walidacji wejścia i autoryzacji, system wyszukuje encję `Payment` o podanym `paymentId`:

- Jeżeli `Payment` **nie istnieje**:
  - endpoint zwraca **404 Not Found**;
  - ciało odpowiedzi to `ErrorResponse` z:
    - `status = 404`;
    - `error = "Not Found"`;
    - `message` np. "Payment with id {paymentId} not found".

### 9.4. Konflikt stanu przy zamkniętym kontrakcie (409 Conflict)

Specjalny przypadek dotyczy prób modyfikacji płatności, dla której powiązany kontrakt (sesja) jest już zamknięty:

- Jeżeli dla `Payment` powiązana `PhotoSession.isContractFinished == true` **przed rozpoczęciem aktualizacji**:
  - każda próba wywołania PATCH (niezależnie od treści body) powinna zostać odrzucona;
  - endpoint **nie modyfikuje żadnych flag** ani stanu kontraktu;
  - zwracany jest **409 Conflict** z `ErrorResponse`:
    - `status = 409`;
    - `error = "Conflict"`;
    - `message` np. "Contract already finished for this payment".

To zachowanie zapewnia, że po zamknięciu kontraktu stan płatności i sesji jest niezmienny z perspektywy tego endpointu.

### 9.5. Błędy serwera i transakcyjność (500 Internal Server Error)

Operacja PATCH na `Payment` oraz ewentualna zmiana `PhotoSession.isContractFinished` powinny być **realizowane w jednej transakcji** bazodanowej lub równoważnym mechanizmie spójności:

- Aktualizacja flag `isDepositPaid`, `isBasePaid`, `isAdditionalPaid` **oraz** ewentualne ustawienie `isContractFinished = true` muszą być **atomowe**:
  - albo wszystkie zmiany zostaną trwale zapisane,
  - albo żadna z nich nie zostanie zastosowana.

- W przypadku błędów systemowych lub problemów transakcyjnych (np. błąd bazy danych, deadlock, przerwanie transakcji):
  - transakcja powinna zostać wycofana (rollback);
  - endpoint zwraca **500 Internal Server Error** z `ErrorResponse`:
    - `status = 500`;
    - `error = "Internal Server Error"` (lub inny zgodny z globalną konwencją);
    - `message` ogólny komunikat, bez ujawniania szczegółów wewnętrznych (np. "Unexpected server error" lub komunikat z globalnego handlera błędów).

### 9.6. Podsumowanie mapowania błędów na kody HTTP

- **400 Bad Request**
  - niepoprawny JSON (błąd parsowania);
  - body nie jest obiektem JSON;
  - obecność pól spoza kontraktu (`additionalProperties`);
  - niezgodność typów (np. string, number, object zamiast boolean);
  - użycie `null` jako wartości któregokolwiek z pól;
  - puste lub semantycznie puste body (brak jakichkolwiek danych do aktualizacji).

- **401 Unauthorized**
  - brak lub nieważne dane uwierzytelniające, zgodnie z globalną polityką API.

- **403 Forbidden**
  - użytkownik uwierzytelniony, ale bez wystarczających uprawnień do modyfikacji danej płatności.

- **404 Not Found**
  - brak encji `Payment` o podanym `paymentId`.

- **409 Conflict**
  - próba modyfikacji płatności powiązanej z już zakończonym kontraktem (`PhotoSession.isContractFinished = true`).

- **500 Internal Server Error**
  - nieoczekiwany błąd serwera lub problem transakcyjny; żadna część operacji (ani na `Payment`, ani na `PhotoSession`) nie powinna zostać trwale zastosowana.
