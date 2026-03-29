package dev.delivery.controller;

import dev.delivery.domain.Product;
import dev.delivery.dto.InventoryResponse;
import dev.delivery.dto.ProductDetailResponse;
import dev.delivery.dto.ReviewResponse;
import dev.delivery.dto.ShopResponse;
import dev.delivery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;

    @GetMapping("/product/{id}")
    public ResponseEntity<ProductDetailResponse> getProduct(@PathVariable("id") Long id) {
        long start = System.currentTimeMillis();
        log.info("[product] 상품 조회 요청 - id: {}", id);

        /**
         * ProductController (rest-mvc)
         * 순차 호출 - 각 응답 기다린 후 다음 호출
         */

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id));

        InventoryResponse inventory = restTemplate.getForObject(
                "http://localhost:8081/inventory/" + id, InventoryResponse.class);

        ReviewResponse review = restTemplate.getForObject(
                "http://localhost:8082/review/" + id, ReviewResponse.class);

        ShopResponse shop = restTemplate.getForObject(
                "http://localhost:8083/shop/product/" + id, ShopResponse.class);

        long elapsed = System.currentTimeMillis() - start;

        return ResponseEntity.ok(ProductDetailResponse.builder()
                .productId(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .stock(inventory.getStock())
                .stockStatus(inventory.getStatus())
                .rating(review.getRating())
                .reviewCount(review.getReviewCount())
                .latestReview(review.getLatestReview())
                .shopName(shop.getShopName())
                .category(shop.getCategory())
                .shopOpen(shop.isOpen())
                .elapsedMs(elapsed)
                .stage("REST-MVC")
                .build());
    }
}