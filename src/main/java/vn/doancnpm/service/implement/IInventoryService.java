/*
package vn.doancnpm.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.InventoryLog;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Product;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.repository.InventoryLogRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.repository.ToppingRepository;
import vn.doancnpm.service.InventoryService;

@Service
@RequiredArgsConstructor
public class IInventoryService implements InventoryService {

    // Nhờ có @RequiredArgsConstructor ở trên, Spring sẽ tự tiêm 3 thằng này vào
    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional // Bắt buộc có để bảo vệ dữ liệu nếu đang trừ kho bị lỗi mạng
    public void deductStock(Long productId, Long toppingId, Order order, int deductAmount) {
        if (deductAmount <= 0) {
            throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        }

        // Nếu truyền vào ID Sản phẩm -> Trừ kho Sản phẩm
        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            if (product.getStock() < deductAmount) {
                throw new RuntimeException("Sản phẩm không đủ số lượng!");
            }
            
            product.setStock(product.getStock() - deductAmount);
            productRepository.save(product);
            
            // Gọi hàm ghi nhật ký ở cuối file
            createLog(product, null, order, -deductAmount, "ORDER_DEDUCT");
        }

        // Nếu truyền vào ID Topping -> Trừ kho Topping
        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy topping"));
            
            if (topping.getQuantity() < deductAmount) {
                throw new RuntimeException("Topping không đủ số lượng!");
            }
            
            topping.setQuantity(topping.getQuantity() - deductAmount);
            toppingRepository.save(topping);
            
            // Gọi hàm ghi nhật ký ở cuối file
            createLog(null, topping, order, -deductAmount, "ORDER_DEDUCT");
        }
    }

    @Override
    @Transactional
    public void addStock(Long productId, Long toppingId, Order order, int addAmount, String reason) {
        if (addAmount <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            product.setStock(product.getStock() + addAmount);
            productRepository.save(product);
            createLog(product, null, order, addAmount, reason);
        }

        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy topping"));
            topping.setQuantity(topping.getQuantity() + addAmount);
            toppingRepository.save(topping);
            createLog(null, topping, order, addAmount, reason);
        }
    }

    @Override
    public boolean checkStockAvailable(Long productId, Long toppingId, int requiredAmount) {
        if (productId != null) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || product.getStock() < requiredAmount) return false;
        }

        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId).orElse(null);
            if (topping == null || !topping.getStatus() || topping.getQuantity() < requiredAmount) return false;
        }
        return true;
    }

    // --- HÀM NỘI BỘ DÙNG ĐỂ TỰ ĐỘNG SINH RA NHẬT KÝ ---
    private void createLog(Product product, Topping topping, Order order, int amount, String type) {
        InventoryLog log = new InventoryLog();
        log.setProduct(product);

        log.setTopping(topping); 
        log.setOrder(order);
        log.setQuantityChange(amount);
        log.setChangeType(type); 
        // Thời gian createdAt đã được tự động lấy bằng LocalDateTime.now() trong class Domain
        
        inventoryLogRepository.save(log);
    }
}
    */
package vn.doancnpm.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.InventoryLog;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Product;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.repository.InventoryLogRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.repository.ToppingRepository;
import vn.doancnpm.service.InventoryService;

@Service
@RequiredArgsConstructor
public class IInventoryService implements InventoryService {

    private final ProductRepository productRepository;
    private final ToppingRepository toppingRepository;
    private final InventoryLogRepository inventoryLogRepository;

    @Override
    @Transactional
    public void deductStock(Long productId, Long toppingId, Order order, int deductAmount) {
        if (deductAmount <= 0) {
            throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        }

        // Nếu truyền vào ID Sản phẩm -> Trừ kho Sản phẩm
        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            
            if (product.getStock() < deductAmount) {
                throw new RuntimeException("Sản phẩm không đủ số lượng!");
            }
            
            product.setStock(product.getStock() - deductAmount);
            productRepository.save(product);
            
            createLog(product, null, order, -deductAmount, "ORDER_DEDUCT");
        }

        // Nếu truyền vào ID Topping -> Trừ kho Topping
        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy topping"));
            
            if (topping.getStock() < deductAmount) {
                throw new RuntimeException("Topping không đủ số lượng!");
            }
            
            topping.setStock(topping.getStock() - deductAmount);
            toppingRepository.save(topping);
            
            createLog(null, topping, order, -deductAmount, "ORDER_DEDUCT");
        }
    }

    @Override
    @Transactional
    public void addStock(Long productId, Long toppingId, Order order, int addAmount, String reason) {
        if (addAmount <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }

        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
            product.setStock(product.getStock() + addAmount);
            productRepository.save(product);
            createLog(product, null, order, addAmount, reason);
        }

        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy topping"));
            topping.setStock(topping.getStock() + addAmount);
            toppingRepository.save(topping);
            createLog(null, topping, order, addAmount, reason);
        }
    }

    @Override
    public boolean checkStockAvailable(Long productId, Long toppingId, int requiredAmount) {
        if (productId != null) {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null || product.getStock() < requiredAmount) return false;
        }

        if (toppingId != null) {
            Topping topping = toppingRepository.findById(toppingId).orElse(null);
            if (topping == null || !topping.getStatus() || topping.getStock() < requiredAmount) return false;
        }
        return true;
    }

    // --- HÀM NỘI BỘ DÙNG ĐỂ TỰ ĐỘNG SINH RA NHẬT KÝ ---
    private void createLog(Product product, Topping topping, Order order, int amount, String type) {
        InventoryLog log = new InventoryLog();
        log.setProduct(product);
        log.setTopping(topping); 
        log.setOrder(order);
        log.setQuantityChange(amount);
        log.setChangeType(type);
        
        inventoryLogRepository.save(log);
    }
}