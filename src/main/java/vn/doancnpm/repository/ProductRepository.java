package vn.doancnpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doancnpm.domain.Product;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStatus(String status);
    List<Product> findByCategoryIdAndStatus(Long categoryId, String status);
}