package dev.delivery;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "*")
public class ReviewController {

    @GetMapping("/review/{productId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable("productId") Long productId)
            throws InterruptedException {
        log.info("[review] 리뷰 조회 요청 - productId: {}", productId);
        Thread.sleep(300);
        return ResponseEntity.ok(new ReviewResponse(productId, 4.5, 128, "정말 좋아요!"));
    }
}