package dev.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShopResponse {
    private Long shopId;
    private String shopName;
    private String category;
    private boolean open;
}