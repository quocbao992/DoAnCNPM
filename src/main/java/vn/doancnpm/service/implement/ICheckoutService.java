package vn.doancnpm.service.implement;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Cart;
import vn.doancnpm.domain.CartItem;
import vn.doancnpm.domain.CartItemTopping;
import vn.doancnpm.domain.InventoryLog;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.OrderItem;
import vn.doancnpm.domain.OrderItemTopping;
import vn.doancnpm.dto.Request.CheckoutRequest;
import vn.doancnpm.dto.Response.OrderResponse;
import vn.doancnpm.repository.CartItemRepository;
import vn.doancnpm.repository.CartRepository;
import vn.doancnpm.repository.InventoryLogRepository;
import vn.doancnpm.repository.OrderRepository;
import vn.doancnpm.service.CheckoutService;
import vn.doancnpm.service.OrderService;
import vn.doancnpm.service.LoyaltyService;
import vn.doancnpm.service.PromotionService;
import java.util.HashMap;
import java.util.Map;
import vn.doancnpm.domain.Product;
import vn.doancnpm.repository.ProductRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class ICheckoutService implements CheckoutService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final OrderRepository orderRepo;
    private final OrderService orderService;
    private final ProductRepository productRepo;
    private final InventoryLogRepository inventoryLogRepo;
    private final LoyaltyService loyaltyService;
    private final PromotionService promotionService;


    // Bước 1: chuyển giỏ hàng thành đơn PENDING (chưa trừ kho, chưa đóng giỏ)
    @Override
    public OrderResponse checkout(CheckoutRequest rq) {
        Long customerId = rq.getCustomerId();
        Cart cart = findActiveCart(customerId);
     
        if (cart == null) {
            throw new IllegalStateException("Không có giỏ hàng đang dùng");
        }
        List<CartItem> cartItems = cartItemRepo.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }
         
    // Gộp số lượng theo sản phẩm (cùng món nhưng khác size/topping vẫn tính chung 1 kho)
    Map<Long, Integer> need = new HashMap<>();
    for (CartItem ci : cartItems) {
        need.merge(ci.getProduct().getId(), ci.getQuantity(), (a, b) -> a + b);
    }
    for (Map.Entry<Long, Integer> e : need.entrySet()) {
        Product p = productRepo.findById(e.getKey())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm " + e.getKey()));
        int stock = p.getStock() == null ? 0 : p.getStock();
        if (stock < e.getValue()) {
            throw new IllegalStateException("Không đủ hàng: " + p.getName()
                    + " (còn " + stock + ", cần " + e.getValue() + ")");
        }
    }

        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setStatus(Order.Status.PENDING);
        order.setOrderType(rq.getOrderType());
        order.setTableNumber(rq.getTableNumber());
        order.setPaymentMethod(rq.getPaymentMethod());
        order.setNote(rq.getNote());
        

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setProductName(ci.getProduct().getName());   // sao chép tên lúc đặt
            oi.setProductImage(ci.getProduct().getImgURL()); // sao chép ảnh lúc đặt
            oi.setSize(ci.getSize().name());
            oi.setQuantity(ci.getQuantity());
            oi.setUnitPrice(ci.getUnitPrice());              // sao chép giá lúc đặt

            // topping của 1 ly
            BigDecimal toppingPerUnit = BigDecimal.ZERO;
            if (ci.getToppings() != null) {
                for (CartItemTopping ct : ci.getToppings()) {
                    OrderItemTopping ot = new OrderItemTopping();
                    ot.setOrderItem(oi);
                    ot.setTopping(ct.getTopping());
                    ot.setToppingName(ct.getTopping().getName());
                    ot.setQuantity(ct.getQuantity());
                    ot.setUnitPrice(ct.getPrice());
                    oi.getToppings().add(ot);

                    toppingPerUnit = toppingPerUnit.add(
                            ct.getPrice().multiply(BigDecimal.valueOf(ct.getQuantity())));
                }
            }
/* 
            // lineTotal = (giá món + topping của 1 ly) x số lượng
            oi.setLineTotal(ci.getUnitPrice().add(toppingPerUnit)
                    .multiply(BigDecimal.valueOf(ci.getQuantity()))); */
            // (Giá món * số lượng ly) + Tổng tiền topping cố định
            BigDecimal baseTotal = ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity()));
            oi.setLineTotal(baseTotal.add(toppingPerUnit)); 


            order.getItems().add(oi);
            subtotal = subtotal.add(oi.getLineTotal());
        }

        order.setSubtotal(subtotal);

        // 1. Mã khuyến mãi
        String code = rq.getVoucherCode();
        BigDecimal promoDiscount = BigDecimal.ZERO;
        if (code != null && !code.isBlank()) {
            promoDiscount = promotionService.calculateDiscount(code, subtotal);
        }
        BigDecimal afterPromo = subtotal.subtract(promoDiscount);

        // 2. Điểm quy đổi
        int points = rq.getLoyaltyPointsUsed() == null ? 0 : rq.getLoyaltyPointsUsed();
        BigDecimal pointsDiscount = BigDecimal.ZERO;
        if (points > 0) {
            loyaltyService.validateRedeem(customerId, points);
            pointsDiscount = loyaltyService.calculateDiscountFromPoints(points);
            if (pointsDiscount.compareTo(afterPromo) > 0) {
                throw new IllegalStateException("Số điểm quy đổi vượt quá giá trị đơn hàng");
            }
        }

        order.setLoyaltyPointsUsed(points);
        order.setDiscountAmount(promoDiscount.add(pointsDiscount));
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()));

        Order saved = orderRepo.save(order);
        if (code != null && !code.isBlank()) {
            promotionService.markUsed(code);   // tăng lượt dùng mã
        }

        saved.setOrderCode("BL"
        + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE)
        + String.format("%05d", saved.getId()));
        return orderService.getById(saved.getId());
    }

    // Bước 2: gọi sau khi thanh toán thành công -> đơn chuyển sang PAID, đóng giỏ
    @Override
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn " + orderId));

        for (OrderItem oi : order.getItems()) {
            // 1. Trừ tồn kho
            int updated = productRepo.deductStock(oi.getProduct().getId(), oi.getQuantity());
            if (updated == 0) {
                throw new IllegalStateException("Không đủ hàng: " + oi.getProductName());
            }

            // 2. Ghi nhật ký vào inventory_log
            InventoryLog log = new InventoryLog();
            log.setChangeType("EXPORT"); 
            log.setQuantityChange(-oi.getQuantity()); // Số lượng âm thể hiện xuất bán
            log.setOrder(order);
            log.setProduct(oi.getProduct());
            log.setCreatedAt(LocalDateTime.now());
            inventoryLogRepo.save(log);
        }

        loyaltyService.redeemPoints(orderId);
        OrderResponse res = orderService.updateStatus(orderId, Order.Status.PAID, null);
        closeCart(res.getCustomerId());
        return res;
    }


    // ===================== Hàm phụ =====================
    // Giỏ đang dùng của khách (status = true), không có thì trả null
    private Cart findActiveCart(Long customerId) {
        return cartRepo.findByCustomerId(customerId)
                .filter(c -> Boolean.TRUE.equals(c.getStatus()))
                .orElse(null);
    }

    // Đánh dấu giỏ đã checkout (status = false)
    private void closeCart(Long customerId) {
        Cart cart = findActiveCart(customerId);
        if (cart != null) {
            cart.setStatus(false);
            cartRepo.save(cart);
        }
    }
}
