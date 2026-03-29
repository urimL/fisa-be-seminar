package dev.delivery.dto;

import lombok.Data;

@Data
public class ReviewResponse {
    private Long productId;
    private double rating;
    private int reviewCount;
    private String latestReview;
}