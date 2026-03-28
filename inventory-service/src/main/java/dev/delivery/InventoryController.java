package dev.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class InventoryController {

    @Value("${inventory.delay:500}")
    private long delay;

    @PostMapping("/inventory/delay")
    public ResponseEntity<String> setDelay(@RequestParam long ms) {
        this.delay = ms;
        log.info("[inventory] delay 변경: {}ms", ms);
        return ResponseEntity.ok("delay set to " + ms + "ms");
    }

    @GetMapping("/inventory/{productId}")
    public ResponseEntity<InventoryResponse> getInventory(@PathVariable("productId") Long productId)
            throws InterruptedException {
        log.info("[inventory] 재고 조회 시작 - productId: {}", productId);
        Thread.sleep(delay);
        log.info("[inventory] 재고 조회 완료 - productId: {}", productId);
        return ResponseEntity.ok(new InventoryResponse(productId, 42, "IN_STOCK"));
    }
}