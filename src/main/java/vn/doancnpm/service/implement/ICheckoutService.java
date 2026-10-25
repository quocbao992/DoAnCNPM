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
import vn.doancnpm.dto.Response.OrderResponse;
import vn.doancnpm.repository.CartItemRepository;
import vn.doancnpm.repository.CartRepository;
import vn.doancnpm.repository.InventoryLogRepository;
//import vn.doancnpm.repository.InventoryLogRepository;
import vn.doancnpm.repository.OrderRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.service.CheckoutService;
import vn.doancnpm.service.OrderService;
import java.util.HashMap;
import java.util.Map;
import vn.doancnpm.domain.Product;
//import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.service.InventoryService;
@Service
@RequiredArgsConstructor
@Transactional
public class ICheckoutService implements CheckoutService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final OrderRepository orderRepo;
    private final OrderService orderService;
    //mới
    //private final ProductRepository productRepo;
    //private final InventoryLogRepository inventoryLogRepo;
    private final InventoryService inventoryService; //mới

    // TODO: khi có InventoryService: kiểm tra tồn kho ở checkout(), trừ kho ở confirmOrder()
    // TODO: khi có PaymentService : PaymentService gọi confirmOrder() khi thanh toán thành công
    // TODO: khi CartService  có hàm đóng giỏ: gọi hàm đó thay cho closeCart() bên dưới

    // Bước 1: chuyển giỏ hàng thành đơn PENDING (chưa trừ kho, chưa đóng giỏ)
    @Override
    public OrderResponse checkout(Long customerId) {
        Cart cart = findActiveCart(customerId);
        if (cart == null) {
            throw new IllegalStateException("Không có giỏ hàng đang dùng");
        }
        List<CartItem> cartItems = cartItemRepo.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }
         
    // Gộp số lượng theo sản phẩm (cùng món nhưng khác size/topping vẫn tính chung 1 kho)
    /* 
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
    }*/
        
        // Gộp số lượng cần theo sản phẩm và theo topping
        //mới
        Map<Long, Integer> needProduct = new HashMap<>();
        Map<Long, Integer> needTopping = new HashMap<>();
        for (CartItem ci : cartItems) {
            needProduct.merge(ci.getProduct().getId(), ci.getQuantity(), Integer::sum);
            if (ci.getToppings() != null) {
                for (CartItemTopping ct : ci.getToppings()) {
                    needTopping.merge(ct.getTopping().getId(), toppingAmount(ci, ct), Integer::sum);
                }
            }
        }
        for (Map.Entry<Long, Integer> e : needProduct.entrySet()) {
            if (!inventoryService.checkStockAvailable(e.getKey(), null, e.getValue())) {
                throw new IllegalStateException("Sản phẩm ID " + e.getKey()
                        + " không đủ hàng hoặc đã ngừng bán (cần " + e.getValue() + ")");
            }
        }
        for (Map.Entry<Long, Integer> e : needTopping.entrySet()) {
            if (!inventoryService.checkStockAvailable(null, e.getKey(), e.getValue())) {
                throw new IllegalStateException("Topping ID " + e.getKey()
                        + " không đủ hàng hoặc đã ngừng bán (cần " + e.getValue() + ")");
            }
        }
        //
        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setStatus(Order.Status.PENDING);
        order.setDiscountAmount(BigDecimal.ZERO); // giảm giá / dùng điểm: bổ sung sau
        order.setLoyaltyPointsUsed(0);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem ci : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(ci.getProduct());
            oi.setProductName(ci.getProduct().getName());   // sao chép tên lúc đặt
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
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()));

        Order saved = orderRepo.save(order); // cascade lưu luôn items + toppings
        return orderService.getById(saved.getId());
    }

    // Bước 2: gọi sau khi thanh toán thành công -> đơn chuyển sang PAID, đóng giỏ
    @Override
    public OrderResponse confirmOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn " + orderId));
        /* 
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
        } cũ*/ 
        // mới
        for (OrderItem oi : order.getItems()) {
            inventoryService.deductStock(oi.getProduct().getId(), null, order, oi.getQuantity());
            if (oi.getToppings() != null) {
                for (OrderItemTopping ot : oi.getToppings()) {
                    inventoryService.deductStock(null, ot.getTopping().getId(), order, ot.getQuantity());
                }
            }
        }//

        OrderResponse res = orderService.updateStatus(orderId, Order.Status.PAID);
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

    // Số topping cần kiểm tra/trừ kho cho một dòng giỏ hàng.
    // Đang theo cách tính tiền hiện tại: quantity là TỔNG cho cả dòng.
    // Nếu nhóm chốt quantity là "mỗi ly" thì đổi thành: ct.getQuantity() * ci.getQuantity()
    //mới
    private int toppingAmount(CartItem ci, CartItemTopping ct) {
        return ct.getQuantity();
    }//

    // Đánh dấu giỏ đã checkout (status = false)
    private void closeCart(Long customerId) {
        Cart cart = findActiveCart(customerId);
        if (cart != null) {
            cart.setStatus(false);
            cartRepo.save(cart);
        }
    }
}
