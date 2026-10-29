package vn.doancnpm.dto.Response;

import java.time.LocalDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLogResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Long toppingId;
    private String toppingName;
    private Integer quantityChange;
    private String changeType;
    private LocalDateTime createdAt;
}