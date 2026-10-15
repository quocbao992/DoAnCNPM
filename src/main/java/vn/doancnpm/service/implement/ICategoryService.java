package vn.doancnpm.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import vn.doancnpm.domain.Category;
import vn.doancnpm.dto.Request.CategoryRequest;
import vn.doancnpm.dto.Response.CategoryResponse;
import vn.doancnpm.repository.CategoryRepository;
import vn.doancnpm.service.CategoryService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ICategoryService implements CategoryService {

    private final CategoryRepository categoryRepository;

    private CategoryResponse convertToResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getStatus(),
                category.getDisplayOrder());
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Không tìm thấy danh mục với ID: " + categoryId));
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        return name.trim();
    }

    @Override
    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = validateName(request.getName());

        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        Category category = Category.builder()
                .name(name)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0L)
                .status(request.getStatus() != null ? request.getStatus() : true)
                .build();

        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse update(Long categoryId, CategoryRequest request) {
        Category category = findCategoryOrThrow(categoryId);

        if (request.getName() != null) {
            String name = validateName(request.getName());
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, categoryId)) {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại");
            }
            category.setName(name);
        }
        if (request.getDisplayOrder() != null) {
            category.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse hide(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        category.setStatus(false);
        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse show(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        category.setStatus(true);
        return convertToResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse getById(Long categoryId) {
        return convertToResponse(findCategoryOrThrow(categoryId));
    }

    @Override
    public List<CategoryResponse> getAll(Boolean status) {
        List<Category> categories = (status == null)
                ? categoryRepository.findAllByOrderByDisplayOrderAsc()
                : categoryRepository.findByStatusOrderByDisplayOrderAsc(status);

        return categories.stream().map(this::convertToResponse).toList();
    }

    @Override
    public List<CategoryResponse> getActiveForMenu() {
        return categoryRepository.findByStatusOrderByDisplayOrderAsc(true)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public List<CategoryResponse> searchByName(String keyword, Boolean status) {
        String key = (keyword == null) ? "" : keyword.trim();

        List<Category> categories = (status == null)
                ? categoryRepository.findByNameContainingIgnoreCaseOrderByDisplayOrderAsc(key)
                : categoryRepository.findByNameContainingIgnoreCaseAndStatusOrderByDisplayOrderAsc(key, status);

        return categories.stream().map(this::convertToResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateDisplayOrder(Long categoryId, Long newDisplayOrder) {
        if (newDisplayOrder == null || newDisplayOrder < 0) {
            throw new IllegalArgumentException("Thứ tự hiển thị không hợp lệ");
        }
        Category category = findCategoryOrThrow(categoryId);
        category.setDisplayOrder(newDisplayOrder);
        return convertToResponse(categoryRepository.save(category));
    }

    // ===== Các hàm dùng chung cho module khác =====

    @Override
    public boolean existsById(Long categoryId) {
        return categoryRepository.existsById(categoryId);
    }

    @Override
    public boolean isActive(Long categoryId) {
        return Boolean.TRUE.equals(findCategoryOrThrow(categoryId).getStatus());
    }

    @Override
    public Category getEntityById(Long categoryId) {
        return findCategoryOrThrow(categoryId);
    }

    @Override
    public Category getActiveEntityById(Long categoryId) {
        Category category = findCategoryOrThrow(categoryId);
        if (!Boolean.TRUE.equals(category.getStatus())) {
            throw new IllegalArgumentException(
                    "Danh mục \"" + category.getName() + "\" đang bị ẩn");
        }
        return category;
    }
}