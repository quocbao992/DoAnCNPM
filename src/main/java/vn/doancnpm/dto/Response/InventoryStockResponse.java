package vn.doancnpm.dto.Response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryStockResponse {
    private Long inventoryId;
    private String inventoryName;
    private Long itemId;       // ID của Product hoặc Topping
    private String itemName;   // Tên của Product hoặc Topping
    private Integer stock;     // Số lượng tồn kho hiện tại
    private Integer minThreshold;
}