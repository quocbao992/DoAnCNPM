package vn.doancnpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doancnpm.domain.Order;
import java.util.List;

@Repository 
public interface OrderRepository extends JpaRepository<Order,Long>{
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(String status);
    List<Order> findByCustomerIdAndStatus(Long customerId, String status);
    boolean existsByCustomerId(Long customerId);
    List<Order> findByTotalAmountGreaterThan(java.math.BigDecimal amount);
}
