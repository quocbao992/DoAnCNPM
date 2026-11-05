package vn.doancnpm.dto.Request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryActionRequest {
  private String type; // "PRODUCT" hoặc "TOPPING"
  private Long id; // productId hoặc toppingId
  private Integer amount;
  private String reason;
}
