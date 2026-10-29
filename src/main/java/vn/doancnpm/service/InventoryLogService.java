package vn.doancnpm.service;

import java.util.List;

import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Product;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.dto.Response.InventoryLogResponse;

public interface InventoryLogService {

    // Ghi log - được ProductInventoryService / ToppingInventoryService gọi
    void logProductChange(Product product, Order order, int quantityChange, String changeType);

    void logToppingChange(Topping topping, Order order, int quantityChange, String changeType);

    // Đọc log - controller gọi
    List<InventoryLogResponse> getProductLogs(Long productId);

    List<InventoryLogResponse> getToppingLogs(Long toppingId);
}