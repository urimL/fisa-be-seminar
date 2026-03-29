package dev.delivery.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDetailResponse {
    private Long productId;
    private String name;
    private int price;
    private String description;
    private int stock;
    private String stockStatus;
    private double rating;
    private int reviewCount;
    private String latestReview;
    private String shopName;
    private String category;
    private boolean shopOpen;
    private long elapsedMs;
    private String stage;
}