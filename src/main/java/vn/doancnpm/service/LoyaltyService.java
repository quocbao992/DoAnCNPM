package vn.doancnpm.service;

import java.math.BigDecimal;

public interface LoyaltyService {
    int calculatePoints(BigDecimal totalAmount);
    int earnPoints(Long orderId);
    int revertPoints(Long orderId);
}
