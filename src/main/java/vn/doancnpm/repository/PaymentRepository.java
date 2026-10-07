package vn.doancnpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doancnpm.domain.Payment;
import java.util.List;

@Repository 
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    Payment findByIdempotencyKey(String idempotencyKey);
    Payment findByTransactionRef(String transactionRef);
    boolean existsByIdempotencyKey(String idempotencyKey);
} 