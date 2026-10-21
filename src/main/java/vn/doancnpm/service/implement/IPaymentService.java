package vn.doancnpm.service.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Payment;
import vn.doancnpm.dto.Request.PaymentRequest;
import vn.doancnpm.dto.Response.PaymentResponse;
import vn.doancnpm.repository.OrderRepository;
import vn.doancnpm.repository.PaymentRepository;
import vn.doancnpm.service.PaymentService;

@Service
@RequiredArgsConstructor
@Transactional
public class IPaymentService implements PaymentService {
    private static final String PENDING = "PENDING";
    private static final String PROCESSING = "PROCESSING";
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILED = "FAILED";
    private static final String REFUNDED = "REFUNDED";

    private static final Map<String, Set<String>> TRANSITIONS = Map.of(
        PENDING, Set.of(PROCESSING, SUCCESS, FAILED),
        PROCESSING, Set.of(SUCCESS, FAILED),
        SUCCESS, Set.of(REFUNDED),
        FAILED, Set.of(),
        REFUNDED, Set.of()
    );

    private final PaymentRepository paymentRepo;
    private final OrderRepository orderRepo; // chỉ dùng để ĐỌC đơn hàng
 
    @Override
    public PaymentResponse createPayment(PaymentRequest rq) {
        if (rq.getIdempotencyKey() == null || rq.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey không được để trống");
        }
        if (rq.getMethod() == null) {
            throw new IllegalArgumentException("Phương thức thanh toán không được để trống");
        }
 
        // 1. Idempotency: gửi lại cùng key thì trả giao dịch cũ
        var existing = paymentRepo.findByIdempotencyKey(rq.getIdempotencyKey());
        if (existing.isPresent()) {
            Payment old = existing.get();
            if (!old.getOrder().getId().equals(rq.getOrderId())) {
                throw new IllegalStateException("idempotencyKey đã được dùng cho đơn hàng khác");
            }
            return mapToPaymentResponse(old);
        }
 
        // 2. Kiểm tra nghiệp vụ trên đơn hàng
        Order order = orderRepo.findById(rq.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng"));
 
        if (order.getStatus() != Order.Status.PENDING) {
            throw new IllegalStateException("Chỉ thanh toán được đơn hàng đang PENDING");
        }
        if (paymentRepo.existsByOrderIdAndStatus(order.getId(), SUCCESS)) {
            throw new IllegalStateException("Đơn hàng đã được thanh toán");
        }
 
        // 3. Tạo Payment: số tiền lấy từ Order, không nhận từ client
        Payment payment = Payment.builder()
                .order(order)
                .method(rq.getMethod())
                .amount(order.getTotalAmount())
                .status(PENDING)
                .idempotencyKey(rq.getIdempotencyKey())
                .createdAt(LocalDateTime.now())
                .build();
 
        return mapToPaymentResponse(paymentRepo.save(payment));
    }
 
    @Override
    public PaymentResponse updateStatus(Long paymentId, String newStatus, String transactionRef) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán"));
 
        String current = payment.getStatus();
 
        // Callback gửi trùng cùng trạng thái thì không báo lỗi
        
        /*if (current.equals(newStatus)) {
            return mapToPaymentResponse(payment);
        }
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(newStatus)) {
            throw new IllegalStateException("Không thể chuyển trạng thái " + current + " -> " + newStatus);
        }*/

        if (newStatus != null && !current.equals(newStatus)) {
            if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(newStatus)) {
                throw new IllegalStateException("Không thể chuyển trạng thái " + current + " -> " + newStatus);
            }
 
            payment.setStatus(newStatus);
        /*if (transactionRef != null) {
            payment.setTransactionRef(transactionRef);
        }*/
            if (SUCCESS.equals(newStatus)) {
                payment.setPaidAt(LocalDateTime.now());
            }
        }

        if (transactionRef != null && !transactionRef.isBlank()) {
            payment.setTransactionRef(transactionRef);
        }
        paymentRepo.save(payment);
        return mapToPaymentResponse(payment);
    }
 
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch thanh toán"));
        return mapToPaymentResponse(payment);
    }
 
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrder(Long orderId) {
        return paymentRepo.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }
 
    private PaymentResponse mapToPaymentResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .orderId(p.getOrder().getId())
                .method(p.getMethod().name())
                .amount(p.getAmount())
                .status(p.getStatus())
                .transactionRef(p.getTransactionRef())
                .paidAt(p.getPaidAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
