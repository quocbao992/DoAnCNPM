package vn.doancnpm.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.InventoryTopping;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.dto.Response.InventoryStockResponse;
import vn.doancnpm.repository.InventoryToppingRepository;
import vn.doancnpm.repository.ToppingRepository;
import vn.doancnpm.service.InventoryLogService;
import vn.doancnpm.service.ToppingInventoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IToppingInventoryService implements ToppingInventoryService {

    private final ToppingRepository toppingRepository;
    private final InventoryToppingRepository inventoryToppingRepository;
    private final InventoryLogService inventoryLogService;

    @Override
    @Transactional
    public void deductStock(Long toppingId, Order order, int deductAmount) {
        if (deductAmount <= 0) {
            throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        }

        Topping topping = findToppingOrThrow(toppingId);
        if (!Boolean.TRUE.equals(topping.getStatus())) {
            throw new IllegalArgumentException(
                    "Topping \"" + topping.getName() + "\" đã ngừng bán");
        }

        InventoryTopping invTopping = inventoryToppingRepository.findByToppingId(toppingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Chưa thiết lập kho cho topping ID: " + toppingId
                                + " (hãy nhập kho trước)"));

        if (invTopping.getStock() < deductAmount) {
            throw new IllegalArgumentException("Topping không đủ số lượng tồn kho!");
        }

        invTopping.setStock(invTopping.getStock() - deductAmount);
        inventoryToppingRepository.save(invTopping);
        inventoryLogService.logToppingChange(topping, order, -deductAmount, "ORDER_DEDUCT");
    }

    @Override
    @Transactional
    public void addStock(Long toppingId, Order order, int addAmount, String reason) {
        if (addAmount <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }

        Topping topping = findToppingOrThrow(toppingId);

        InventoryTopping invTopping = inventoryToppingRepository.findByToppingId(toppingId)
                .orElseGet(() -> InventoryTopping.builder()
                        .topping(topping)
                        .stock(0)
                        .minThreshold(10)
                        .build());

        invTopping.setStock(invTopping.getStock() + addAmount);
        inventoryToppingRepository.save(invTopping);
        inventoryLogService.logToppingChange(topping, order, addAmount, reason);
    }

    @Override
    public boolean checkStockAvailable(Long toppingId, int requiredAmount) {
        Topping topping = toppingRepository.findById(toppingId).orElse(null);
        if (topping == null || !Boolean.TRUE.equals(topping.getStatus())) {
            return false;
        }
        return inventoryToppingRepository.findByToppingId(toppingId)
                .map(inv -> inv.getStock() >= requiredAmount)
                .orElse(false);
    }

    @Override
    public InventoryStockResponse getStock(Long toppingId) {
        InventoryTopping inv = inventoryToppingRepository.findByToppingId(toppingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy tồn kho cho Topping ID: " + toppingId));
        return InventoryStockResponse.builder()
                .itemId(inv.getTopping().getId())
                .itemName(inv.getTopping().getName())
                .stock(inv.getStock())
                .minThreshold(inv.getMinThreshold())
                .build();
    }

    private Topping findToppingOrThrow(Long toppingId) {
        return toppingRepository.findById(toppingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy topping với ID: " + toppingId));
    }
}