package vn.doancnpm.service.implement;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.Category;
import vn.doancnpm.domain.InventoryProduct;
import vn.doancnpm.domain.Product;
import vn.doancnpm.dto.Request.ProductRequest;
import vn.doancnpm.dto.Response.PageResponse;
import vn.doancnpm.dto.Response.ProductResponse;
import vn.doancnpm.repository.InventoryProductRepository;
import vn.doancnpm.repository.ProductRepository;
import vn.doancnpm.service.CategoryService;
import vn.doancnpm.service.ProductInventoryService;
import vn.doancnpm.service.ProductService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IProductService implements ProductService {

    private static final int MAX_NAME_LENGTH = 255;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_FEATURED_LIMIT = 6;
    private static final int MAX_FEATURED_LIMIT = 50;

    private final ProductRepository productRepository;

    private final ProductInventoryService productInventoryService;

    private final CategoryService categoryService;
    private ProductResponse convertToResponse(Product product) {
        Category category = product.getCategory();
        return new ProductResponse(
                product.getId(),
                category.getId(),
                category.getName(),
                product.getName(),
                product.getImgURL(),
                // Tồn kho thật nằm ở inventory_product, không đọc Product.stock nữa
                productInventoryService.getAvailableStock(product.getId()),
                product.getUnitPrice(),
                product.isStatus());
    }

    private List<ProductResponse> convertToResponses(List<Product> products) {
        return products.stream().map(this::convertToResponse).toList();
    }

    private Product findProductOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy sản phẩm với ID: " + productId));
    }

    private BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }
        return price;
    }

    private void validateStock(Integer stock) {
        if (stock != null && stock < 0) {
            throw new IllegalArgumentException("Tồn kho không được âm");
        }
    }

    private void validate(ProductRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (request.getName().trim().length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Tên sản phẩm không được vượt quá " + MAX_NAME_LENGTH + " ký tự");
        }
    }

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        validate(request);
        validatePrice(request.getUnitPrice());
        validateStock(request.getStock());

        String name = request.getName().trim();
        String imgURL = request.getImgURL() != null ? request.getImgURL().trim() : null;

        Product product = new Product();
        product.setName(name);
        product.setImgURL(imgURL);
        product.setUnitPrice(request.getUnitPrice());
        product.setStatus(request.getStatus() != null ? request.getStatus() : true);
        product.setCategory(categoryService.getActiveEntityById(request.getCategoryId()));

        Product saved = productRepository.save(product);

        // Tạo dòng tồn kho trong inventory_product (ghi log nếu có tồn đầu kỳ > 0)
        
        int initialStock = request.getStock() == null ? 0 : request.getStock();
        productInventoryService.setStock(saved.getId(), initialStock, "INITIAL_STOCK");
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long productId, ProductRequest request) {
        validate(request);
        validatePrice(request.getUnitPrice());
        validateStock(request.getStock());

        String name = request.getName().trim();
        String imgURL = request.getImgURL() != null ? request.getImgURL().trim() : null;

        Product product = findProductOrThrow(productId);

        if (productRepository.existsByNameIgnoreCaseAndIdNot(name, productId)) {
            throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
        }

        product.setName(name);
        product.setImgURL(imgURL);
        product.setUnitPrice(request.getUnitPrice());
        product.setCategory(categoryService.getActiveEntityById(request.getCategoryId()));

        Product saved = productRepository.save(product);

        // Chỉ chỉnh tồn kho khi request có gửi stock
        if (request.getStock() != null) {
            productInventoryService.setStock(productId, request.getStock(), "ADMIN_UPDATE");
        }

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse updatePrice(Long productId, BigDecimal newPrice) {
        validatePrice(newPrice);
        Product product = findProductOrThrow(productId);
        product.setUnitPrice(newPrice);
        return convertToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateStatus(Long productId, boolean status) {
        Product product = findProductOrThrow(productId);
        product.setStatus(status);
        return convertToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateStock(Long productId, int stock) {
        validateStock(stock);
        Product product = findProductOrThrow(productId);
        productInventoryService.setStock(productId, stock, "ADMIN_ADJUST");
        return convertToResponse(product);
    }

    // Xóa mềm: chỉ ẩn sản phẩm, giữ lại dữ liệu để đơn hàng cũ vẫn tra được
    @Override
    @Transactional
    public void delete(Long productId) {
        Product product = findProductOrThrow(productId);
        product.setStatus(false);
        productRepository.save(product);
    }

    @Override
    public ProductResponse getById(Long productId) {
        return convertToResponse(findProductOrThrow(productId));
    }

    @Override
    public PageResponse<ProductResponse> search(Long categoryId, String keyword, Boolean status,
                                                int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = (size <= 0) ? DEFAULT_PAGE_SIZE : Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("id").ascending());

        String key = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();

        Page<Product> result = productRepository.search(categoryId, key, status, pageable);

        return new PageResponse<>(
                convertToResponses(result.getContent()),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    @Override
    public List<ProductResponse> getByCategory(Long categoryId, Boolean status) {
        if (!categoryService.existsById(categoryId)) {
            throw new EntityNotFoundException("Không tìm thấy danh mục với ID: " + categoryId);
        }
        Pageable all = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("name").ascending());
        return convertToResponses(
                productRepository.search(categoryId, null, status, all).getContent());
    }

    @Override
    public List<ProductResponse> getFeatured(int limit) {
        int safeLimit = (limit <= 0) ? DEFAULT_FEATURED_LIMIT : Math.min(limit, MAX_FEATURED_LIMIT);
        return convertToResponses(
                productRepository.findByStatusOrderByIdDesc(true, PageRequest.of(0, safeLimit)));
    }

    @Override
    public List<ProductResponse> getMenu() {
        return convertToResponses(productRepository.findMenu());
    }

    // ======================== Hàm dùng chung cho module khác ========================

    @Override
    public boolean existsById(Long productId) {
        return productRepository.existsById(productId);
    }

    @Override
    public boolean isActive(Long productId) {
        return findProductOrThrow(productId).isStatus();
    }

    @Override
    public Product getEntityById(Long productId) {
        return findProductOrThrow(productId);
    }

    @Override
    public Product getActiveEntityById(Long productId) {
        Product product = findProductOrThrow(productId);
        if (!product.isStatus()) {
            throw new IllegalArgumentException(
                    "Sản phẩm \"" + product.getName() + "\" đã ngừng bán");
        }
        return product;
    }

    @Override
    public BigDecimal getUnitPrice(Long productId) {
        return getActiveEntityById(productId).getUnitPrice();
    }
}