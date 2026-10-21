package vn.doancnpm.service.implement;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Customer;
import vn.doancnpm.domain.Order;
import vn.doancnpm.repository.CustomerRepository;
import vn.doancnpm.repository.OrderRepository;
import vn.doancnpm.repository.PaymentRepository;
import vn.doancnpm.service.LoyaltyService;

@Service
@RequiredArgsConstructor
@Transactional
public class ILoyaltyService implements LoyaltyService {

    // Quy tắc ví dụ: 10.000đ = 1 điểm (cả nhóm cần chốt)
    private static final BigDecimal AMOUNT_PER_POINT = new BigDecimal("10000");

    private final OrderRepository orderRepo;       // chỉ ĐỌC
    private final PaymentRepository paymentRepo;
    private final CustomerRepository customerRepo; // thay cho CustomerService

    @Override
    public int calculatePoints(BigDecimal totalAmount) {
        if (totalAmount == null) {
            return 0;
        }
        return totalAmount.divide(AMOUNT_PER_POINT, 0, RoundingMode.DOWN).intValue();
    }

    @Override
    public int earnPoints(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        // Chỉ đơn đã thanh toán thành công mới được cộng điểm
        if (!paymentRepo.existsByOrderIdAndStatus(orderId, "SUCCESS")) {
            throw new IllegalStateException("Đơn hàng chưa thanh toán thành công, không thể cộng điểm");
        }

        int points = calculatePoints(order.getTotalAmount());
        if (points > 0) {
            changePoints(order.getCustomer().getId(), points);
        }
        return points;
    }

    @Override
    public int revertPoints(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));

        if (!"CANCELLED".equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ hoàn điểm khi đơn hàng đã bị hủy");
        }

        int points = calculatePoints(order.getTotalAmount());
        if (points > 0) {
            changePoints(order.getCustomer().getId(), -points);
        }
        return points;
    }

    // delta dương = cộng điểm, delta âm = trừ điểm (không để điểm bị âm)
    private void changePoints(Long customerId, int delta) {
        Customer customer = customerRepo.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));
        customer.setLoyaltyP(Math.max(0, customer.getLoyaltyP() + delta));
        customerRepo.save(customer);
    }
}