package vn.doancnpm.service.implement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Promotion;
import vn.doancnpm.repository.PromotionRepository;
import vn.doancnpm.service.PromotionService;

@Service
@RequiredArgsConstructor
@Transactional
public class IPromotionService implements PromotionService {
    private final PromotionRepository repo;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateDiscount(String code, BigDecimal subtotal) {
        Promotion p = repo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không tồn tại"));
        LocalDateTime now = LocalDateTime.now();

        if (!p.isActive() || now.isBefore(p.getStartDate()) || now.isAfter(p.getEndDate())) {
            throw new IllegalStateException("Mã khuyến mãi không còn hiệu lực");
        }
        if (p.getUsageLimit() != null && p.getUsedCount() >= p.getUsageLimit()) {
            throw new IllegalStateException("Mã khuyến mãi đã hết lượt dùng");
        }
        if (p.getMinOrderAmount() != null && subtotal.compareTo(p.getMinOrderAmount()) < 0) {
            throw new IllegalStateException("Đơn hàng chưa đạt giá trị tối thiểu");
        }

        BigDecimal discount = "PERCENT".equals(p.getDiscountType())
                ? subtotal.multiply(p.getDiscountValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN)
                : p.getDiscountValue();

        if (p.getMaxDiscount() != null && discount.compareTo(p.getMaxDiscount()) > 0) {
            discount = p.getMaxDiscount();
        }
        return discount.min(subtotal);          // không giảm quá tổng tiền
    }

    @Override
    public void markUsed(String code) {
        Promotion p = repo.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không tồn tại"));
        p.setUsedCount(p.getUsedCount() + 1);
        repo.save(p);
    }
}
