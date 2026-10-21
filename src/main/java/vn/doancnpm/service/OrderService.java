package vn.doancnpm.service;

import java.util.List;
import vn.doancnpm.domain.Order;
import vn.doancnpm.dto.Response.OrderResponse;

public interface OrderService {
    List<OrderResponse> getHistory(Long customerId, Order.Status status);
    List<OrderResponse> getByStatus(Order.Status status);
    OrderResponse getById(Long orderId);
    OrderResponse updateStatus(Long orderId, Order.Status newStatus);
    OrderResponse cancel(Long orderId);

}


