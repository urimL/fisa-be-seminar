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
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    private final WebClient webClient;

    @GetMapping("/product/{id}")
    public Mono<ResponseEntity<ProductDetailResponse>> getProduct(@PathVariable Long id) {
        long start = System.currentTimeMillis();
        log.info("[product] 상품 조회 요청 - id: {}", id);

        Mono<Product> productMono = Mono.fromCallable(() ->
                productRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("상품 없음: " + id))
        ).subscribeOn(Schedulers.boundedElastic());

        Mono<InventoryResponse> inventoryMono = Mono.fromCallable(() ->
                restTemplate.getForObject("http://localhost:8081/inventory/" + id, InventoryResponse.class)
        ).subscribeOn(Schedulers.boundedElastic());

        Mono<ReviewResponse> reviewMono = webClient.get()
                .uri("http://localhost:8082/review/" + id)
                .retrieve()
                .bodyToMono(ReviewResponse.class);

        Mono<ShopResponse> shopMono = webClient.get()
                .uri("http://localhost:8083/shop/product/" + id)
                .retrieve()
                .bodyToMono(ShopResponse.class);

        return productMono.flatMap(product ->
                Mono.zip(inventoryMono, reviewMono, shopMono)
                        .map(tuple -> {
                            long elapsed = System.currentTimeMillis() - start;
                            return ResponseEntity.ok(ProductDetailResponse.builder()
                                    .productId(product.getId())
                                    .name(product.getName())
                                    .price(product.getPrice())
                                    .description(product.getDescription())
                                    .stock(tuple.getT1().getStock())
                                    .stockStatus(tuple.getT1().getStatus())
                                    .rating(tuple.getT2().getRating())
                                    .reviewCount(tuple.getT2().getReviewCount())
                                    .latestReview(tuple.getT2().getLatestReview())
                                    .shopName(tuple.getT3().getShopName())
                                    .category(tuple.getT3().getCategory())
                                    .shopOpen(tuple.getT3().isOpen())
                                    .elapsedMs(elapsed)
                                    .stage("WEBFLUX-PARALLEL")
                                    .build());
                        })
        );
    }
}