package dev.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class ShopController {

    @GetMapping("/shop/product/{productId}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable("productId") Long productId)
            throws InterruptedException {
        log.info("[shop] 가게 조회 요청 - productId: {}", productId);
        Thread.sleep(400);
        return ResponseEntity.ok(new ShopResponse(1L, "우아한 가게", "음식", true));
    }
}