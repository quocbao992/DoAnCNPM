package vn.doancnpm.service;

import java.util.List;
import vn.doancnpm.domain.Order;
import vn.doancnpm.dto.Response.InventoryLogResponse;
import vn.doancnpm.dto.Response.InventoryStockResponse;

public interface InventoryService {
    
    // 1. Trừ tồn kho (Dùng khi khách chốt đơn)
    void deductStock(Long productId, Long toppingId, Order order, int deductAmount);

    // 2. Cộng tồn kho (Dùng khi nhập hàng mới hoặc khách hủy đơn)
    void addStock(Long productId, Long toppingId, Order order, int addAmount, String reason);

    // 3. Kiểm tra xem kho còn đủ hàng không (Dùng trước khi thanh toán)
    boolean checkStockAvailable(Long productId, Long toppingId, int requiredAmount);

    InventoryStockResponse getToppingStock(Long toppingId);
    InventoryStockResponse getProductStock(Long productId);
    List<InventoryLogResponse> getToppingLogs(Long toppingId);
    List<InventoryLogResponse> getProductLogs(Long productId);
}