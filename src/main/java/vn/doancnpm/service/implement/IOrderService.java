package vn.doancnpm.service.implement;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.OrderItem;
import vn.doancnpm.domain.OrderItemTopping;
import vn.doancnpm.dto.Response.OrderItemResponse;
import vn.doancnpm.dto.Response.OrderItemToppingResponse;
import vn.doancnpm.dto.Response.OrderResponse;
import vn.doancnpm.repository.OrderRepository;
import vn.doancnpm.service.OrderService;

@Service
@RequiredArgsConstructor
@Transactional
public class IOrderService implements OrderService {

    private final OrderRepository orderRepo;

    // ===================== Lịch sử mua hàng =====================
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getHistory(Long customerId, Order.Status status) {
        List<Order> orders = (status == null)
                ? orderRepo.findByCustomerIdOrderByCreatedAtDesc(customerId)
                : orderRepo.findByCustomerIdAndStatus(customerId, status);
        return orders.stream().map(this::mapToOrderResponse).toList();
    }

    // ===================== Danh sách đơn theo trạng thái (nhân viên) =====================
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getByStatus(Order.Status status) {
        return orderRepo.findByStatus(status).stream().map(this::mapToOrderResponse).toList();
    }

    // ===================== Chi tiết đơn =====================
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getById(Long orderId) {
        return mapToOrderResponse(findOrder(orderId));
    }

    // ===================== Cập nhật trạng thái =====================
    @Override
    public OrderResponse updateStatus(Long orderId, Order.Status newStatus) {
        Order order = findOrder(orderId);
        if (!canChange(order.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Không thể chuyển đơn từ " + order.getStatus() + " sang " + newStatus);
        }
        order.setStatus(newStatus);
        return mapToOrderResponse(orderRepo.save(order));
    }

    // ===================== Hủy đơn (chỉ khi PENDING hoặc PAYMENT_FAILED) =====================
    // Hoàn điểm do LoyaltyService, trả kho do InventoryService xử lý
    @Override
    public OrderResponse cancel(Long orderId) {
        Order order = findOrder(orderId);
        if (order.getStatus() != Order.Status.PENDING
                && order.getStatus() != Order.Status.PAYMENT_FAILED) {
            throw new IllegalStateException("Chỉ hủy được đơn PENDING hoặc PAYMENT_FAILED");
        }
        order.setStatus(Order.Status.CANCELLED);
        return mapToOrderResponse(orderRepo.save(order));
    }

    // ===================== Hàm phụ =====================
    private Order findOrder(Long orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng " + orderId));
    }

    // PENDING -> PAID -> PREPARING -> READY -> COMPLETED
    // PENDING -> PAYMENT_FAILED -> (thanh toán lại) PAID; hủy qua cancel()
    private boolean canChange(Order.Status from, Order.Status to) {
        return switch (from) {
            case PENDING -> to == Order.Status.PAID || to == Order.Status.PAYMENT_FAILED
                    || to == Order.Status.CANCELLED;
            case PAYMENT_FAILED -> to == Order.Status.PAID || to == Order.Status.CANCELLED;
            case PAID -> to == Order.Status.PREPARING;
            case PREPARING -> to == Order.Status.READY;
            case READY -> to == Order.Status.COMPLETED;
            default -> false; // COMPLETED, CANCELLED là trạng thái cuối
        };
    }

    private OrderItemToppingResponse mapToToppingResponse(OrderItemTopping t) {
        return OrderItemToppingResponse.builder()
                .toppingId(t.getTopping().getId())
                .name(t.getToppingName())
                .quantity(t.getQuantity())
                .unitPrice(t.getUnitPrice())
                .build();
    }

    private OrderItemResponse mapToItemResponse(OrderItem i) {
        List<OrderItemToppingResponse> toppings = (i.getToppings() == null)
                ? Collections.emptyList()
                : i.getToppings().stream().map(this::mapToToppingResponse).toList();
        return OrderItemResponse.builder()
                .id(i.getId())
                .productId(i.getProduct().getId())
                .productName(i.getProductName())
                .size(i.getSize())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .lineTotal(i.getLineTotal())
                .toppings(toppings)
                .build();
    }

    private OrderResponse mapToOrderResponse(Order o) {
        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomer().getId())
                .status(o.getStatus())
                .items(o.getItems().stream().map(this::mapToItemResponse).toList())
                .subtotal(o.getSubtotal())
                .discountAmount(o.getDiscountAmount())
                .loyaltyPointsUsed(o.getLoyaltyPointsUsed())
                .totalAmount(o.getTotalAmount())
                .createdAt(o.getCreatedAt())
                .updatedAt(o.getUpdatedAt())
                .build();
    }
}
