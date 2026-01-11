# PhotoApp – model domenowy płatności i kontraktów

## 1. Encje i powiązania

### 1.1. `Payment`
Lokalizacja: `src/main/java/org/ks/photoapp/domain/payment/Payment.java`

```java
@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Float deposit;
    Float basePayment;
    Float additionalPayment;
    Boolean isDepositPaid;
    Boolean isBasePaid;
    Boolean isAdditionalPaid;
    @OneToOne(mappedBy = "payment", cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    PhotoSession photoSession;
}
```

**Charakterystyka:**
- Klasa JPA oznaczona `@Entity`, z Lombokowym `@Data` (gettery/settery, `equals`, `hashCode`, `toString`).
- Identyfikator typu `Long` z auto‑inkrementacją (`GenerationType.IDENTITY`).
- Kwoty:
  - `deposit` – zaliczka,
  - `basePayment` – płatność bazowa,
  - `additionalPayment` – płatność dodatkowa.
- Flagi stanu płatności:
  - `isDepositPaid` – czy zaliczka opłacona,
  - `isBasePaid` – czy płatność podstawowa opłacona,
  - `isAdditionalPaid` – czy płatność dodatkowa opłacona.
- Powiązanie 1‑1 z `PhotoSession`:
  - Strona odwrotna relacji (`mappedBy = "payment"`),
  - Kaskady: `REFRESH`, `PERSIST`.

**Wnioski dla nowego endpointu PATCH /api/payments/{paymentId}:**
- Status płatności jest reprezentowany **zestawem flag Boolean**, brak osobnej enum/klasy `PaymentStatus`.
- Aktualizacja statusu płatności będzie najprawdopodobniej operować na:
  - `isDepositPaid`,
  - `isBasePaid`,
  - `isAdditionalPaid`.
- Brak informacji o dacie płatności, metodzie płatności itp. – model jest prosty.

### 1.2. `PhotoSession`
Lokalizacja: `src/main/java/org/ks/photoapp/domain/photoSession/PhotoSession.java`

```java
@Entity
@Data
public class PhotoSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    Client client;
    LocalDateTime sessionDate;
    @Enumerated(EnumType.STRING)
    SessionType sessionType;
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "payment_id")
    Payment payment;
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "photos_id")
    Photos photos;
    Boolean isContractFinished;

    public PhotoSession() {
    }
}
```

**Charakterystyka powiązań z płatnościami i kontraktem:**
- `@OneToOne` do `Payment` z kolumną `payment_id` – to jest **strona właścicielska** relacji 1‑1.
- Pole `isContractFinished` reprezentuje **stan kontraktu** z klientem dla danej sesji.
- Brak osobnej encji `Contract` – kontrakt jest konceptem zaszytym w `PhotoSession`.

**Wnioski domenowe:**
- Model domenowy nie ma dedykowanej encji „Contract”; 
  - status kontraktu to `isContractFinished` na `PhotoSession`.
- Zamykanie kontraktu ("contract closing") jest naturalnie związane z konkretną sesją (`PhotoSession`), a pośrednio z płatnością (`Payment`).
- Jeśli nowy endpoint ma „automatycznie zamykać kontrakt”, będzie musiał dotykać **PhotoSession.isContractFinished** powiązanej z danym `Payment`.

### 1.3. Relacja `Payment` – `PhotoSession` – `Client`

- `PhotoSession`:
  - ma `Client client` (wiele sesji na klienta),
  - ma `Payment payment` (1‑1 na sesję),
  - ma `Boolean isContractFinished`.
- `Payment`:
  - ma `PhotoSession photoSession` jako stronę odwrotną.

W praktyce ścieżka domenowa jest następująca:

```text
Client 1<--* PhotoSession 1<-->1 Payment
                         \
                          \--> Photos
```

Kontrakt dotyczy **PhotoSession** (sesji klienta), a płatność jest przypisana 1‑1 do sesji.

## 2. Aktualna logika płatności i kontraktu

### 2.1. `PaymentService`
Lokalizacja: `src/main/java/org/ks/photoapp/domain/payment/PaymentService.java`

```java
@Service
public class PaymentService {

    PaymentRepository paymentRepository;
    PhotoSessionRepository photoSessionRepository;


    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public void updatePayment(long paymentId, Client client) {
        Payment paymentToUpdate = paymentRepository.findPaymentById(paymentId).orElseThrow();
        PhotoSession photoSession = photoSessionRepository.findPhotoSessionByClient(client).orElseThrow();
        paymentToUpdate.setDeposit(photoSession.getPayment().getDeposit());
        paymentToUpdate.setBasePayment(photoSession.getPayment().getBasePayment());
        paymentToUpdate.setAdditionalPayment(photoSession.getPayment().getAdditionalPayment());
        paymentToUpdate.setIsDepositPaid(photoSession.getPayment().getIsDepositPaid());
        paymentToUpdate.setIsBasePaid(photoSession.getPayment().getIsBasePaid());
        paymentToUpdate.setIsAdditionalPaid(photoSession.getPayment().getIsAdditionalPaid());
        paymentRepository.save(paymentToUpdate);
    }
}
```

**Obserwacje:**
- Serwis ma dwa pola repozytoriów, ale wstrzykiwane jest **tylko** `PaymentRepository` przez konstruktor.
  - `PhotoSessionRepository` pozostanie `null`, jeśli nie ma wstrzykiwania przez np. `@Autowired` na polu/setterze – to potencjalny bug.
- `updatePayment` przyjmuje `paymentId` i `Client`:
  - pobiera `Payment` po `id`,
  - pobiera `PhotoSession` po `Client` (`photoSessionRepository.findPhotoSessionByClient(client)`),
  - kopiuje wszystkie pola płatności z `photoSession.getPayment()` do `paymentToUpdate`,
  - zapisuje payment.

**Znaczenie domenowe `updatePayment`:**
- To bardziej **synchronizacja** płatności przypisanej do sesji z innym obiektem `Payment` niż typowa aktualizacja statusu.
- Nie ma tutaj logiki kontraktu (`isContractFinished` nie jest ruszane).
- Brak jakichkolwiek reguł domenowych typu:
  - „gdy wszystkie płatności opłacone, zamknij kontrakt”,
  - „nie można oznaczyć kontraktu jako zakończonego, jeśli są nieopłacone części”.

### 2.2. `PaymentController`
Lokalizacja: `src/main/java/org/ks/photoapp/domain/payment/PaymentController.java`

```java
@Controller
public class PaymentController {
    PaymentService paymentService;

    @GetMapping("/update-paid")
    public String updatePaid(long paymentId, Client client) {
        paymentService.updatePayment(paymentId,client);
        return "redirect:/payments";
    }
}
```

**Charakterystyka:**
- Spring MVC `@Controller` (nie REST), więc ten endpoint służy raczej do obsługi widoków (HTML, redirect) niż API JSON.
- Endpoint HTTP:
  - `GET /update-paid`
  - parametry: `long paymentId`, `Client client` (domyślne bindowanie Springa z requestu/formularza).
  - zwraca `String` – redirect na `"redirect:/payments"`.

**Wnioski dla projektowania PATCH /api/payments/{paymentId}:**
- Obecna warstwa płatności **nie ma** REST‑owego API (`@RestController`, DTO JSON itd.).
- Nowy endpoint:
  - powinien zostać umieszczony w bardziej REST‑owym stylu, np. w nowym lub przebudowanym `PaymentController` oznaczonym `@RestController` i w odpowiednim pakiecie (`domain.payment`).
  - prawdopodobnie powinien pracować na DTO zamiast na encjach JPA (`Client` w parametrach kontrolera jest anty‑wzorcem).

### 2.3. `PaymentRepository`
Lokalizacja: `src/main/java/org/ks/photoapp/domain/payment/PaymentRepository.java`

```java
@Repository
public interface PaymentRepository extends CrudRepository<Payment, Long> {

    Optional<Payment> findPaymentById(Long id);
}
```

**Obserwacje:**
- Standardowy Spring Data `CrudRepository`.
- Dodatkowa metoda wyszukiwania po id (`findPaymentById`) – semantycznie to samo co `findById`, ale nazwa jest spójna z resztą kodu.

### 2.4. `PhotoSessionRepository` i kontrakt
Dla pełnego obrazu stanu kontraktu warto znać repozytorium `PhotoSessionRepository`:

```java
public interface PhotoSessionRepository extends CrudRepository<PhotoSession, Long> {

    Optional<PhotoSession> findPhotoSessionById(Long id);
    Optional<PhotoSession> findPhotoSessionByClient(Client client);
}
```

**Powiązanie z kontraktem:**
- Kontrakt (czyli `isContractFinished`) można modyfikować przez `PhotoSessionService` (do weryfikacji w implementacji), ale w kodzie z repozytorium nie ma bezpośrednich metod w stylu `finishContract`.
- Brak dedykowanego serwisu „ContractService”; logika kontraktu, jeśli istnieje, jest w `PhotoSessionService` lub rozproszona po controllerach.

## 3. Statusy i flagi

### 3.1. Płatności
Status płatności jest modelowany wyłącznie przez **flagi Boolean** na encji `Payment`:

- `isDepositPaid`
- `isBasePaid`
- `isAdditionalPaid`

Brak:
- pól typu „łącznie zapłacono”, „pozostało do zapłaty”,
- enuma `PaymentStatus` (np. PENDING, PARTIALLY_PAID, PAID),
- informacji o dacie/czasie płatności, sposobie płatności itp.

Dlatego nowy endpoint PATCH najpewniej będzie:
- aktualizował wybrane flagi,
- ewentualnie na ich podstawie wnioskował, czy kontrakt może zostać zamknięty (wszystkie `true`).

### 3.2. Kontrakt
Kontrakt jest reprezentowany przez pole:

- `PhotoSession.isContractFinished` – `Boolean`.

Brak:
- osobnej encji `Contract`,
- historii zmian kontraktu,
- dat rozpoczęcia/zakończenia kontraktu,
- enumu statusu kontraktu.

Potencjalna logika domenowa dla nowego endpointu (na razie niezaimplementowana w kodzie):
- Po ustawieniu wszystkich flag płatności na `true`, system mógłby automatycznie wywołać logikę:
  - `photoSession.setIsContractFinished(true)`.

## 4. Wzorce walidacji i obsługi błędów w API

W obszarze `domain.payment` **brak jest** złożonej walidacji i obsługi błędów:

- `updatePayment` używa `orElseThrow()` bez własnego typu wyjątku ani własnej wiadomości.
- `PaymentController` nie stosuje żadnej dedykowanej obsługi błędów (brak `@ExceptionHandler`, `@ControllerAdvice` itp.).

Aby zachować spójność z resztą aplikacji, warto sprawdzić, jak wygląda to w innych modułach (np. `client`, `photoSession`, `user`).

### 4.1. Wstępna ocena globalnych wzorców (do pogłębienia)
Po szybkim przejrzeniu struktury pakietów (`domain.client`, `domain.photoSession`, `domain.user`) można założyć, że:
- Aplikacja jest klasyczną aplikacją Spring MVC z widokami (prawdopodobnie Thymeleaf).
- Błędy domenowe częściej kończą się wyjątkami runtime i standardowymi stronami błędów niż JSON‑owymi odpowiedziami API.

Dla projektowania REST‑owego PATCH `/api/payments/{paymentId}`:
- warto rozważyć **wydzielenie dedykowanego API** (np. pakiet `web.api` albo `domain.payment.api` z `@RestController`),
- i zdefiniowanie spójnej polityki błędów (np. `404` gdy brak płatności, `400` dla błędów walidacji itd.).

## 5. Implikacje dla nowego endpointu PATCH /api/payments/{paymentId}

Na podstawie aktualnego modelu i kodu:

1. **Identyfikacja płatności**
   - Będziemy używać `PaymentRepository.findPaymentById(paymentId)` lub `findById(paymentId)`.

2. **Powiązanie z kontraktem**
   - Po znalezieniu `Payment` należy użyć `payment.getPhotoSession()` aby dobrać się do `PhotoSession.isContractFinished`.

3. **Reguły biznesowe (propozycja na bazie modelu)**
   - Kontrakt można zamknąć (`isContractFinished = true`) **tylko**, gdy wszystkie trzy flagi płatności są `true`.
   - Jeśli endpoint ma „automatycznie zamykać kontrakt”, to po aktualizacji flag płatności należy:
     - przeliczyć, czy wszystkie są `true`,
     - jeśli tak – ustawić `photoSession.isContractFinished = true` i zapisać `PhotoSession`.

4. **Spójność nazewnictwa**
   - Pola w modelu są w stylu `isXxxPaid` i `isContractFinished`.
   - Dla DTO i JSON można zachować podobne nazwy, np.:
     - `depositPaid`, `basePaid`, `additionalPaid`, `contractFinished` **albo** dokładnie jak w encjach (`isDepositPaid` itd.).

5. **Warstwa API**
   - Obecna logika jest oparta na MVC i redirectach.
   - Nowy endpoint powinien być w stylu REST (`@RestController`, `@PatchMapping("/api/payments/{paymentId}")`), pracować na DTO i zwracać JSON.

## 6. Rekomendacje dalszej analizy

Aby w pełni dopasować nowy endpoint do istniejącej architektury, warto dodatkowo:

1. **Przejrzeć `PhotoSessionService` i `PhotoSessionController`**
   - sprawdzić, czy gdzieś już modyfikowane jest `isContractFinished`,
   - zrozumieć, w jakich momentach sesja/kontrakt jest tworzony i finalizowany.

2. **Przejrzeć moduł `client` i `user`**
   - zobaczyć, jak obecnie obsługiwane są błędy, walidacja, zwracane statusy HTTP (jeśli są endpointy REST).

3. **Zweryfikować konfigurację globalnego exception handlingu**
   - czy istnieje `@ControllerAdvice`, który mapuje wyjątki typu `NoSuchElementException` na jakieś konkretne odpowiedzi.

Te dodatkowe informacje pozwolą uściślić: 
- jaki dokładnie kontrakt API przyjąć dla PATCH `/api/payments/{paymentId}` (schemat request/response),
- jakie kody statusu i format błędów zwracać,
- gdzie umieścić logikę zamykania kontraktu, aby była spójna z resztą systemu.
