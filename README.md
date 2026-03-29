# MSA 환경에서 WebFlux 적용하기
로 MSA의 fan-out 호출 구간에서의 자원 효율 높이기

---

## 주제

MSA 환경에서 여러 서비스를 동시에 호출할 때 발생하는 성능 문제를,
WebFlux가 어떤 방식으로 완화하는지 실제 데모와 k6 부하 테스트로 검증한다.

---

## 프로젝트 구조

```
product-service (8080)   ← 클라이언트 요청 진입점
  → inventory-service (8081) : 재고 조회, 500ms 지연
  → review-service    (8082) : 리뷰 조회, 300ms 지연
  → shop-service      (8083) : 가게 조회, 400ms 지연
```

product-service가 세 서비스를 호출해 하나의 응답을 조립하는 **fan-out 구조**다.

---

## 브랜치 구성

| 브랜치 | 구현 방식 | 핵심 |
|---|---|---|
| `rest-mvc` | RestTemplate 순차 호출 | 500 + 300 + 400 = 1200ms |
| `webflux-parallel` | WebClient + Mono.zip 병렬 호출 | max(500, 300, 400) = 500ms |
| `webflux-bounded` | RestTemplate + boundedElastic 격리 | 레거시 점진적 마이그레이션 |

---

## 문제 상황

MSA는 기능을 나누기 좋지만, 하나의 사용자 요청이 여러 내부 서비스 호출로 쪼개진다.
이 구조에서는 **하나의 downstream이 느려지면 전체 응답이 느려진다.**

MVC + RestTemplate 순차 호출 구조에서는:

- 응답시간이 각 호출의 합으로 누적된다
- I/O 대기 중에도 스레드가 계속 점유된다
- 동시 요청이 늘어나면 스레드 고갈 → 큐잉 → 타임아웃

```java
// rest-mvc: 순차 호출 — 각 응답을 기다린 후 다음 호출
InventoryResponse inventory = restTemplate.getForObject(...); // 500ms 대기
ReviewResponse review       = restTemplate.getForObject(...); // 300ms 대기
ShopResponse shop           = restTemplate.getForObject(...); // 400ms 대기
// 총 1200ms
```

---

## WebFlux로 완화하기

### 1. 병렬 호출 + Non-blocking I/O

WebClient와 Mono.zip을 사용해 세 서비스를 동시에 호출한다.
총 응답시간은 합이 아니라 **가장 느린 호출 기준**으로 수렴한다.

```java
Mono<InventoryResponse> inventoryMono = webClient.get().uri(".../inventory/" + id)
        .retrieve().bodyToMono(InventoryResponse.class);
Mono<ReviewResponse> reviewMono = webClient.get().uri(".../review/" + id)
        .retrieve().bodyToMono(ReviewResponse.class);
Mono<ShopResponse> shopMono = webClient.get().uri(".../shop/product/" + id)
        .retrieve().bodyToMono(ShopResponse.class);

return Mono.zip(inventoryMono, reviewMono, shopMono)
    .map(tuple -> ProductDetailResponse.builder()
        .stock(tuple.getT1().getStock())
        .rating(tuple.getT2().getRating())
        .shopName(tuple.getT3().getShopName())
        ...
        .build());
```

### 2. Blocking 코드 격리 — boundedElastic

JPA처럼 blocking이 불가피한 경우, 이벤트 루프에서 직접 실행하면 오히려 성능이 나빠진다.
`Mono.fromCallable + boundedElastic`으로 blocking 작업을 별도 스레드 풀에 위임해 이벤트 루프를 보호한다.

```java
Mono<Product> productMono = Mono.fromCallable(() ->
    productRepository.findById(id).orElseThrow(...)
).subscribeOn(Schedulers.boundedElastic());
```

> boundedElastic은 blocking을 없애는 게 아니라 이벤트 루프를 보호하는 타협안이다.
> 완전한 non-blocking을 원한다면 R2DBC가 필요하지만, JPA 생태계와의 트레이드오프가 있다.

### 3. 취소 전파

클라이언트가 요청을 취소했을 때, MVC 구조에서는 이미 시작된 downstream 호출이 계속 진행될 수 있다.
WebFlux는 취소 신호가 reactive chain을 따라 전파되어 불필요한 자원 점유를 줄인다.

---

## k6 부하 테스트 결과

### Ramp-up 시나리오 (50 → 100 → 200 → 0 VU)

| | rest-mvc (Stage 1) | webflux-parallel (Stage 2) |
|---|---|---|
| p50 | 13,000ms | 511ms |
| p95 | 21,000ms | 890ms |
| RPS | 6.2 | 85.6 |

### k6 실행

```bash
# rest-mvc
STAGE=1 k6 run k6/scenario1-ramp.js

# webflux-parallel
STAGE=2 k6 run k6/scenario1-ramp.js
```

---

## MVC도 병렬 호출이 가능한데, 왜 WebFlux인가?

MVC에서도 `@Async`, `CompletableFuture`로 병렬 호출이 가능하다.
차이는 **병렬 호출 가능 여부**가 아니라 **처리 방식의 자원 효율**이다.

| | MVC + CompletableFuture | WebFlux + Mono.zip |
|---|---|---|
| 병렬 호출 | 가능 | 가능 |
| I/O 대기 중 스레드 | 점유 | 해제 |
| 구성 방식 | 명령형 | 선언형 |

---

## WebFlux가 적합한 경우 / 신중해야 할 경우

### 적합한 경우
- I/O 대기가 많고 여러 서비스를 fan-out 호출하는 실시간 API
- 높은 동시성을 적은 스레드로 처리해야 하는 구간

### 신중해야 할 경우
- **배치/오프라인 처리**: Spring Batch는 WebFlux와 맞지 않는다
- **복잡한 트랜잭션**: 결제, 재고 차감처럼 순차 보장과 롤백이 중요한 경우 복잡도가 높아진다
- **레거시/JPA 중심 환경**: blocking 의존이 크면 WebFlux로 감싸도 병목이 그대로 남는다
- **러닝커브**: Reactor 체인은 디버깅이 어렵고 팀 학습 비용이 있다

---

## 결론

> 기술을 먼저 고르지 말고, 병목을 먼저 찾아라.
> WebFlux는 모든 상황의 정답이 아니라,
> I/O 대기가 많고 fan-out 호출이 많은 구간에서 강한 선택지다.
