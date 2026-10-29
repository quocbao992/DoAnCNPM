package vn.doancnpm.service;

import vn.doancnpm.domain.Order;
import vn.doancnpm.dto.Response.InventoryStockResponse;

public interface ProductInventoryService {

    // Trừ tồn kho (khi khách chốt đơn)
    void deductStock(Long productId, Order order, int deductAmount);

    // Cộng tồn kho (nhập hàng mới hoặc khách hủy đơn)
    void addStock(Long productId, Order order, int addAmount, String reason);

    // Kiểm tra còn đủ hàng không (trước khi thanh toán)
    boolean checkStockAvailable(Long productId, int requiredAmount);

    InventoryStockResponse getStock(Long productId);

    // Trả về số lượng tồn (0 nếu sản phẩm chưa có dòng tồn kho) - dùng để hiển thị
    int getAvailableStock(Long productId);

    // Đặt tồn kho về đúng một con số (kiểm kê / admin chỉnh tay), ghi log phần chênh lệch
    void setStock(Long productId, int newStock, String reason);
}