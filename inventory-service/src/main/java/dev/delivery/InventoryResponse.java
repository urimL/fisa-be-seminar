package dev.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryResponse {
    private Long productId;
    private int stock;
    private String status;
}