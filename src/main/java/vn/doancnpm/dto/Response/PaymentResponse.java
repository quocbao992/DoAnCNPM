package vn.doancnpm.dto.Response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor

public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String method;
    private BigDecimal amount;
    private String status;
    private String transactionRef;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
