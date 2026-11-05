package vn.doancnpm.dto.Request;

import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToppingRequest {
  private String name;
  private BigDecimal unitPrice;
}
