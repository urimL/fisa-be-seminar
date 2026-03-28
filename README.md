# Stage 3: WebFlux + boundedElastic 격리

## 개요
병렬 호출 구조는 유지하되, blocking 라이브러리를 사용하는 서비스(inventory)를
boundedElastic 스레드 풀로 격리하는 구조.

## 서비스 구조
```
product-service (8080)
  → RestTemplate → inventory-service (8081) : 500ms  ← boundedElastic 격리
  → WebClient   → review-service    (8082) : 300ms  ↗ 동시 호출
  → WebClient   → shop-service      (8083) : 400ms  ↗

총 응답시간: ~500ms (max 기준, 병렬 유지)
```

## 핵심 포인트

- inventory-service가 blocking SDK를 쓴다는 가정 (우리가 못 바꾸는 외부 라이브러리)
- RestTemplate 호출을 `Mono.fromCallable + subscribeOn(Schedulers.boundedElastic())`으로 감싸서 이벤트 루프 블로킹 방지
- review, shop은 WebClient 그대로 유지

## 한계

- boundedElastic도 결국 스레드 풀 → 고트래픽 시 스레드 고갈 가능
- 취소 전파 안 됨 → 클라이언트가 연결 끊어도 RestTemplate은 계속 실행

## 코드 핵심
```java
// inventory: blocking → boundedElastic으로 격리
Mono<InventoryResponse> inventoryMono = Mono.fromCallable(() ->
    restTemplate.getForObject("http://localhost:8081/inventory/" + id, InventoryResponse.class)
).subscribeOn(Schedulers.boundedElastic());

// review, shop: WebClient 그대로
Mono<ReviewResponse> reviewMono = webClient.get()...
Mono<ShopResponse> shopMono = webClient.get()...

Mono.zip(inventoryMono, reviewMono, shopMono)...
```

## 실행
```bash
GET http://localhost:8080/product/1
```