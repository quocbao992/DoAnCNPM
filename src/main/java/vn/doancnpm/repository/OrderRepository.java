package vn.doancnpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doancnpm.domain.Order;
import java.util.List;

@Repository 
public interface OrderRepository extends JpaRepository<Order,Long>{
    List<Order> findByStatus(Order.Status status);
    List<Order> findByCustomerIdAndStatus(Long customerId, Order.Status status);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
