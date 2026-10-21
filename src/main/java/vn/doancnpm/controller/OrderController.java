package vn.doancnpm.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Order;
import vn.doancnpm.dto.Request.CheckoutRequest;
import vn.doancnpm.dto.Request.UpdateOrderStatusRequest;
import vn.doancnpm.dto.Response.OrderResponse;
import vn.doancnpm.service.CheckoutService;
import vn.doancnpm.service.OrderService;


@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CheckoutService checkoutService;

    // Lịch sử mua hàng: GET /api/v1/orders?customerId=1  (có thể thêm &status=COMPLETED)
    @GetMapping
    public ResponseEntity<List<OrderResponse>> history(@RequestParam Long customerId,
                                                       @RequestParam(required = false) Order.Status status) {
        return ResponseEntity.ok(orderService.getHistory(customerId, status));
    }

    // Nhân viên xem đơn theo trạng thái: GET /api/v1/orders/status/PENDING
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> byStatus(@PathVariable Order.Status status) {
        return ResponseEntity.ok(orderService.getByStatus(status));
    }

    // Chi tiết đơn: GET /api/v1/orders/5
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> detail(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    // Checkout: POST /api/v1/orders/checkout  body {"customerId": 1}
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest rq) {
        return new ResponseEntity<>(checkoutService.checkout(rq.getCustomerId()), HttpStatus.CREATED);
    }

    // Đơn đã thanh toán -> PAID (PaymentService sẽ gọi; tạm để test bằng Postman)
    @PostMapping("/{id}/confirm")
    public ResponseEntity<OrderResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(checkoutService.confirmOrder(id));
    }

    // Cập nhật trạng thái: PUT /api/v1/orders/5/status  body {"status": "PREPARING"}
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
    @Valid @RequestBody UpdateOrderStatusRequest rq) {
        return ResponseEntity.ok(orderService.updateStatus(id, rq.getStatus()));
    }

    // Hủy đơn: PUT /api/v1/orders/5/cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancel(id));
    }
}