/*
package vn.doancnpm.service.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.*;
import vn.doancnpm.dto.Response.InventoryLogResponse;
import vn.doancnpm.dto.Response.InventoryStockResponse;
import vn.doancnpm.repository.*;
import vn.doancnpm.service.InventoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IInventoryService implements InventoryService {

    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final InventoryLogRepository inventoryLogRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryToppingRepository inventoryToppingRepository;
    private final InventoryRepository inventoryRepository;

    //Trừ kho

    @Override
    @Transactional
    public void deductStock(Long productId, Long toppingId, Order order, int deductAmount) {
        if (deductAmount <= 0) {
            throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        }

        if (productId == null && toppingId == null) {
        throw new IllegalArgumentException("Phải truyền productId hoặc toppingId");
        }

        if (productId != null) {
            Product product = findProductOrThrow(productId);
            if (!product.isStatus()) {
                throw new IllegalArgumentException(
                        "Sản phẩm \"" + product.getName() + "\" đã ngừng bán");
            }
            InventoryProduct invProduct = inventoryProductRepository.findByProductId(productId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Chưa thiết lập kho cho sản phẩm ID: " + productId
                                    + " (hãy nhập kho trước)"));

            if (invProduct.getStock() < deductAmount) {
                throw new IllegalArgumentException("Sản phẩm không đủ số lượng tồn kho!");
            }

            invProduct.setStock(invProduct.getStock() - deductAmount);
            inventoryProductRepository.save(invProduct);
            createLog(product, null, order, -deductAmount, "ORDER_DEDUCT");
        }

        if (toppingId != null) {
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
            createLog(null, topping, order, -deductAmount, "ORDER_DEDUCT");
        }
    }

    //Nhập kho / hoàn kho

    @Override
    @Transactional
    public void addStock(Long productId, Long toppingId, Order order, int addAmount, String reason) {// updateproductrequest rq
        if (addAmount <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }

        if (productId == null && toppingId == null) {
        throw new IllegalArgumentException("Phải truyền productId hoặc toppingId");
        }

        if (productId != null) {
            Product product = findProductOrThrow(productId);

            InventoryProduct invProduct = inventoryProductRepository.findByProductId(productId)
                    .orElseGet(() -> InventoryProduct.builder()
                            .inventory(getOrCreateDefaultInventory())
                            .product(product)
                            .stock(0)
                            .minThreshold(5)
                            .build());

            invProduct.setStock(invProduct.getStock() + addAmount);
            inventoryProductRepository.save(invProduct);
            createLog(product, null, order, addAmount, reason);
        }

        if (toppingId != null) {
            Topping topping = findToppingOrThrow(toppingId);

            InventoryTopping invTopping = inventoryToppingRepository.findByToppingId(toppingId)
                    .orElseGet(() -> InventoryTopping.builder()
                            .inventory(getOrCreateDefaultInventory())
                            .topping(topping)
                            .stock(0)
                            .minThreshold(10)
                            .build());

            invTopping.setStock(invTopping.getStock() + addAmount);
            inventoryToppingRepository.save(invTopping);
            createLog(null, topping, order, addAmount, reason);
        }
    }

    @Override
    public boolean checkStockAvailable(Long productId, Long toppingId, int requiredAmount) {//

        if (productId != null) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || !product.isStatus()) {
                return false;
            }
            InventoryProduct invProduct = inventoryProductRepository.findByProductId(productId).orElse(null);
            if (invProduct == null || invProduct.getStock() < requiredAmount) {
                return false;
            }
        }

        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId).orElse(null);
            if (topping == null || !Boolean.TRUE.equals(topping.getStatus())) {
                return false;
            }
            InventoryTopping invTopping = inventoryToppingRepository.findByToppingId(toppingId).orElse(null);
            if (invTopping == null || invTopping.getStock() < requiredAmount) {
                return false;
            }
        }
        return true;
    }

    //Hàm trả DTO cho controller

    @Override
    public InventoryStockResponse getToppingStock(Long toppingId) {
        InventoryTopping inv = inventoryToppingRepository.findByToppingId(toppingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy tồn kho cho Topping ID: " + toppingId));
        return InventoryStockResponse.builder()
                .inventoryId(inv.getInventory() != null ? inv.getInventory().getId() : null)
                .inventoryName(inv.getInventory() != null ? inv.getInventory().getName() : null)
                .itemId(inv.getTopping().getId())
                .itemName(inv.getTopping().getName())
                .stock(inv.getStock())
                .minThreshold(inv.getMinThreshold())
                .build();
    }

    @Override
    public InventoryStockResponse getProductStock(Long productId) {
        InventoryProduct inv = inventoryProductRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy tồn kho cho Product ID: " + productId
                                + " (sản phẩm chưa được nhập kho)"));
        return InventoryStockResponse.builder()
                .inventoryId(inv.getInventory() != null ? inv.getInventory().getId() : null)
                .inventoryName(inv.getInventory() != null ? inv.getInventory().getName() : null)
                .itemId(inv.getProduct().getId())
                .itemName(inv.getProduct().getName())
                .stock(inv.getStock())
                .minThreshold(inv.getMinThreshold())
                .build();
    }

    @Override
    public List<InventoryLogResponse> getToppingLogs(Long toppingId) {
        return inventoryLogRepository.findByToppingIdOrderByCreatedAtDesc(toppingId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }//

    @Override
    public List<InventoryLogResponse> getProductLogs(Long productId) {
        return inventoryLogRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }//

    //Hàm nội bộ

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + productId));
    }

    private Topping findToppingOrThrow(Long toppingId) {
        return toppingRepository.findById(toppingId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy topping với ID: " + toppingId));
    }

    private Inventory getOrCreateDefaultInventory() {
        return inventoryRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> inventoryRepository.save(
                        Inventory.builder()
                                .name("Kho Tổng")
                                .location("Chi nhánh chính")
                                .status(true)
                                .build()));
    }

    private void createLog(Product product, Topping topping, Order order, int amount, String type) {
        InventoryLog log = InventoryLog.builder()
                .product(product)
                .topping(topping)
                .order(order)
                .quantityChange(amount)
                .changeType(type)
                .createdAt(LocalDateTime.now())
                .build();
        inventoryLogRepository.save(log);
    }

    private InventoryLogResponse mapToLogResponse(InventoryLog log) {
        return InventoryLogResponse.builder()
                .id(log.getId())
                .productId(log.getProduct() != null ? log.getProduct().getId() : null)
                .productName(log.getProduct() != null ? log.getProduct().getName() : null)
                .toppingId(log.getTopping() != null ? log.getTopping().getId() : null)
                .toppingName(log.getTopping() != null ? log.getTopping().getName() : null)
                .quantityChange(log.getQuantityChange())
                .changeType(log.getChangeType())
                .createdAt(log.getCreatedAt())
                .build();
    }
}*/
