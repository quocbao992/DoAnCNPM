package vn.doancnpm.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doancnpm.domain.Payment;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
  @NotNull(message = "Đơn hàng không được để trống")
  private Long orderId;

  @NotNull(message = "Phương thức thanh toán không được để trống")
  private Payment.Method method;

  @NotBlank(message = "idempotencyKey không được để trống")
  private String idempotencyKey;
}
