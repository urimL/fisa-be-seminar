package dev.delivery.dto;

import lombok.Data;

@Data
public class InventoryResponse {
    private Long productId;
    private int stock;
    private String status;
}