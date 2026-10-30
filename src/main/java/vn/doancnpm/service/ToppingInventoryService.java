package vn.doancnpm.service;

import vn.doancnpm.domain.Order;
import vn.doancnpm.dto.Response.InventoryStockResponse;

public interface ToppingInventoryService {

    void deductStock(Long toppingId, Order order, int deductAmount);

    void addStock(Long toppingId, Order order, int addAmount, String reason);

    boolean checkStockAvailable(Long toppingId, int requiredAmount);

    InventoryStockResponse getStock(Long toppingId);
}