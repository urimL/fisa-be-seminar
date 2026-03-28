package dev.delivery.domain;

import dev.delivery.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) {
        productRepository.saveAll(List.of(
                new Product(null, "떡볶이 세트", 12000, "매콤달콤한 국민 간식"),
                new Product(null, "치킨 한 마리", 20000, "바삭한 후라이드 치킨"),
                new Product(null, "짜장면", 8000, "부드러운 춘장 짜장면"),
                new Product(null, "피자 L사이즈", 25000, "치즈가 듬뿍 마르게리타"),
                new Product(null, "초밥 세트", 35000, "신선한 연어 초밥 12피스")
        ));
    }
}