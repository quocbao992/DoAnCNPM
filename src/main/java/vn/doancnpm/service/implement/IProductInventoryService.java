package vn.doancnpm.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.InventoryProduct;
import vn.doancnpm.domain.Order;
import vn.doancnpm.domain.Product;
import vn.doancnpm.dto.Response.InventoryStockResponse;
import vn.doancnpm.repository.InventoryProductRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.service.InventoryLogService;
import vn.doancnpm.service.ProductInventoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IProductInventoryService implements ProductInventoryService {

    private final ProductRepository productRepository;
    private final InventoryProductRepository inventoryProductRepository;
    private final InventoryLogService inventoryLogService;
    

    @Override
    @Transactional
    public void deductStock(Long productId, Order order, int deductAmount) {
        if (deductAmount <= 0) {
            throw new IllegalArgumentException("Số lượng trừ phải lớn hơn 0");
        }

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
        inventoryLogService.logProductChange(product, order, -deductAmount, "ORDER_DEDUCT");
    }

    @Override
    @Transactional
    public void addStock(Long productId, Order order, int addAmount, String reason) {
        if (addAmount <= 0) {
            throw new IllegalArgumentException("Số lượng thêm phải lớn hơn 0");
        }

        Product product = findProductOrThrow(productId);

        InventoryProduct invProduct = inventoryProductRepository.findByProductId(productId)
                .orElseGet(() -> InventoryProduct.builder()
                        .product(product)
                        .stock(0)
                        .minThreshold(5)
                        .build());

        invProduct.setStock(invProduct.getStock() + addAmount);
        inventoryProductRepository.save(invProduct);
        inventoryLogService.logProductChange(product, order, addAmount, reason);
    }

    @Override
    public boolean checkStockAvailable(Long productId, int requiredAmount) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || !product.isStatus()) {
            return false;
        }
        return inventoryProductRepository.findByProductId(productId)
                .map(inv -> inv.getStock() >= requiredAmount)
                .orElse(false);
    }

    @Override
    public InventoryStockResponse getStock(Long productId) {
        InventoryProduct inv = inventoryProductRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy tồn kho cho Product ID: " + productId
                                + " (sản phẩm chưa được nhập kho)"));
        return InventoryStockResponse.builder()
                .itemId(inv.getProduct().getId())
                .itemName(inv.getProduct().getName())
                .stock(inv.getStock())
                .minThreshold(inv.getMinThreshold())
                .build();
    }
    
    @Override
    public int getAvailableStock(Long productId) {
        return inventoryProductRepository.findByProductId(productId)
                .map(InventoryProduct::getStock)
                .orElse(0);
    }

    @Override
    @Transactional
    public void setStock(Long productId, int stock, String reason) {
        InventoryProduct inv = inventoryProductRepository.findByProductId(productId)
                .orElseGet(() -> {
                    InventoryProduct newInv = new InventoryProduct();
                    newInv.setProduct(productRepository.getReferenceById(productId));
                    newInv.setMinThreshold(5);
                    newInv.setStock(0);
                    return newInv;
                });

        inv.setStock(stock);
        inventoryProductRepository.save(inv);
    }

        private Product findProductOrThrow(Long productId) {
            return productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Không tìm thấy sản phẩm với ID: " + productId));
        }
    }