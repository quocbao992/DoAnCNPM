package vn.doancnpm.service;

import vn.doancnpm.dto.Response.OrderResponse;

public interface CheckoutService {
    OrderResponse checkout(Long customerId);   // giỏ hàng -> đơn PENDING
    OrderResponse confirmOrder(Long orderId);  // sau khi thanh toán thành công -> PAID
}
