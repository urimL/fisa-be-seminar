package dev.delivery.dto;

import lombok.Data;

@Data
public class ShopResponse {
    private Long shopId;
    private String shopName;
    private String category;
    private boolean open;
}