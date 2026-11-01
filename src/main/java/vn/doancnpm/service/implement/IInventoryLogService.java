/* 
package vn.doancnpm.service.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.InventoryLog;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Product;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.dto.Response.InventoryLogResponse;
import vn.doancnpm.dto.Response.InventoryStockResponse;
import vn.doancnpm.repository.InventoryLogRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.repository.ToppingRepository;
import vn.doancnpm.service.InventoryLogService;
//import vn.doancnpm.service.InventoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IInventoryLogService implements InventoryLogService {

    private final InventoryLogRepository inventoryLogRepository;
    
    // Propagation mặc định (REQUIRED): log chạy CHUNG transaction với việc đổi tồn kho.
    // => nếu trừ kho bị rollback thì log cũng rollback, không bị lệch dữ liệu.
    @Override
    @Transactional
    public void logProductChange(Product product, Order order, int quantityChange, String changeType) {
        save(product, null, order, quantityChange, changeType);
    }

    @Override
    @Transactional
    public void logToppingChange(Topping topping, Order order, int quantityChange, String changeType) {
        save(null, topping, order, quantityChange, changeType);
    }

    @Override
    public List<InventoryLogResponse> getProductLogs(Long productId) {
        return inventoryLogRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryLogResponse> getToppingLogs(Long toppingId) {
        return inventoryLogRepository.findByToppingIdOrderByCreatedAtDesc(toppingId).stream()
                .map(this::mapToLogResponse)
                .collect(Collectors.toList());
    }

    private void save(Product product, Topping topping, Order order, int amount, String type) {
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

package vn.doancnpm.service.implement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Product;
import vn.doancnpm.domain.ProductLog;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.domain.ToppingLog;
import vn.doancnpm.dto.Response.InventoryLogResponse;
import vn.doancnpm.repository.ProductLogRepository;
import vn.doancnpm.repository.ToppingLogRepository;
import vn.doancnpm.service.InventoryLogService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IInventoryLogService implements InventoryLogService {

    private final ProductLogRepository productLogRepository;
    private final ToppingLogRepository toppingLogRepository;

    // Propagation mặc định (REQUIRED): log chạy CHUNG transaction với việc đổi tồn kho.
    // => nếu trừ kho bị rollback thì log cũng rollback, không bị lệch dữ liệu.
    @Override
    @Transactional
    public void logProductChange(Product product, Order order, int quantityChange, String changeType) {
        ProductLog log = ProductLog.builder()
                .product(product)
                .order(order)
                .quantityChange(quantityChange)
                .changeType(changeType)
                .createdAt(LocalDateTime.now())
                .build();
        productLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logToppingChange(Topping topping, Order order, int quantityChange, String changeType) {
        ToppingLog log = ToppingLog.builder()
                .topping(topping)
                .order(order)
                .quantityChange(quantityChange)
                .changeType(changeType)
                .createdAt(LocalDateTime.now())
                .build();
        toppingLogRepository.save(log);
    }

    @Override
    public List<InventoryLogResponse> getProductLogs(Long productId) {
        return productLogRepository.findByIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapProductLog)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryLogResponse> getToppingLogs(Long toppingId) {
        return toppingLogRepository.findByIdOrderByCreatedAtDesc(toppingId).stream()
                .map(this::mapToppingLog)
                .collect(Collectors.toList());
    }

    private InventoryLogResponse mapProductLog(ProductLog log) {
        return InventoryLogResponse.builder()
                .id(log.getId())
                .productId(log.getProduct().getId())
                .productName(log.getProduct().getName())
                .quantityChange(log.getQuantityChange())
                .changeType(log.getChangeType())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private InventoryLogResponse mapToppingLog(ToppingLog log) {
        return InventoryLogResponse.builder()
                .id(log.getId())
                .toppingId(log.getTopping().getId())
                .toppingName(log.getTopping().getName())
                .quantityChange(log.getQuantityChange())
                .changeType(log.getChangeType())
                .createdAt(log.getCreatedAt())
                .build();
    }
}