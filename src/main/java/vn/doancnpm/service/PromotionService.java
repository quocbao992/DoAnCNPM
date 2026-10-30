package vn.doancnpm.service;

import java.math.BigDecimal;

public interface PromotionService {
    BigDecimal calculateDiscount(String code, BigDecimal subtotal);   // chỉ tính, không ghi
    void markUsed(String code);
}
