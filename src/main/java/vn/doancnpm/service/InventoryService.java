package vn.doancnpm.service;

import vn.doancnpm.domain.Order;

public interface InventoryService {
    
    // 1. Trừ tồn kho (Dùng khi khách chốt đơn)
    void deductStock(Long productId, Long toppingId, Order order, int deductAmount);

    // 2. Cộng tồn kho (Dùng khi nhập hàng mới hoặc khách hủy đơn)
    void addStock(Long productId, Long toppingId, Order order, int addAmount, String reason);

    // 3. Kiểm tra xem kho còn đủ hàng không (Dùng trước khi thanh toán)
    boolean checkStockAvailable(Long productId, Long toppingId, int requiredAmount);
}