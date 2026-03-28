package dev.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class InventoryController {

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable Long productId)
            throws InterruptedException {
        log.info("[inventory] 재고 조회 요청 - productId: {}", productId);
        Thread.sleep(500);
        return ResponseEntity.ok(new InventoryResponse(productId, 42, "IN_STOCK"));
    }
}