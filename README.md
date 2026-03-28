# Stage 1: REST MVC 순차 호출

## 개요
하나의 상품 상세 조회 요청을 처리하기 위해 3개의 외부 서비스를 **순차적으로** 호출하는 구조.

## 서비스 구조
```
product-service (8080)
  → RestTemplate → inventory-service (8081) : 500ms
  → RestTemplate → review-service    (8082) : 300ms
  → RestTemplate → shop-service      (8083) : 400ms

총 응답시간: 500 + 300 + 400 = 1200ms+
```

## 문제점

- 각 서비스 응답시간이 **합산**됨
- inventory 응답 기다리는 동안 review, shop 호출 불가
- 트래픽 증가 시 Tomcat 스레드 고갈

## 실행
```bash
# 4개 서비스 모두 실행 후
GET http://localhost:8080/product/1
```

## 응답 예시
```json
{
  "productId": 1,
  "name": "떡볶이 세트",
  "price": 12000,
  "description": "매콤달콤한 국민 간식",
  "stock": 42,
  "stockStatus": "IN_STOCK",
  "rating": 4.5,
  "reviewCount": 128,
  "latestReview": "정말 좋아요!",
  "shopName": "우아한 가게",
  "category": "음식",
  "shopOpen": true,
  "elapsedMs": 1280,
  "stage": "REST-MVC"
}
```