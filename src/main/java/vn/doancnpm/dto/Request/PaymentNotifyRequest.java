package vn.doancnpm.dto.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.doancnpm.domain.Payment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentNotifyRequest {
  @NotNull private Long orderId;

  private String transactionId;

  @NotBlank private String status; // SUCCESS hoặc FAILED

  @NotBlank private String idempotencyKey;

  @NotNull private Payment.Method method;
}
