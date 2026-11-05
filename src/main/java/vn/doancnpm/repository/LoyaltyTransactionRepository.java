package vn.doancnpm.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.doancnpm.domain.LoyaltyTransaction;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {
  boolean existsByOrderIdAndType(Long orderId, String type);

  Optional<LoyaltyTransaction> findByOrderIdAndType(Long orderId, String type);

  List<LoyaltyTransaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
