package vn.doancnpm.dto.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockUpdateRequest {
    private Long productId; // Truyền null nếu đang thao tác với Topping
    private Long toppingId; // Truyền null nếu đang thao tác với Product
    private int amount;     // Số lượng muốn cộng thêm hoặc trừ đi
    private String reason;  // Lý do (VD: "IMPORT", "DAMAGED", "ORDER_DEDUCT")
}