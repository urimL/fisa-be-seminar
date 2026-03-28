# Stage 2: WebFlux 병렬 호출

## 개요
Mono.zip으로 inventory, review, shop 서비스를 **동시에** 호출하는 구조.

## 서비스 구조
```
product-service (8080)
  → WebClient → inventory-service (8081) : 500ms  ↘
  → WebClient → review-service    (8082) : 300ms  → Mono.zip
  → WebClient → shop-service      (8083) : 400ms  ↗

총 응답시간: max(500, 300, 400) = ~500ms
```

## Stage 1 대비 개선

| | Stage 1 | Stage 2 |
|---|---|---|
| 호출 방식 | 순차 | 병렬 |
| 응답시간 | 합 (1200ms) | max (500ms) |
| 스레드 | Tomcat 스레드 점유 | 이벤트 루프 |

## 코드 핵심
```java
Mono.zip(inventoryMono, reviewMono, shopMono)
    .map(tuple -> ProductDetailResponse.builder()
        .stock(tuple.getT1().getStock())
        .rating(tuple.getT2().getRating())
        .shopName(tuple.getT3().getShopName())
        ...
        .build()
    )
```

## 실행
```bash
GET http://localhost:8080/product/1
```