package dev.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewResponse {
    private Long productId;
    private double rating;
    private int reviewCount;
    private String latestReview;
}