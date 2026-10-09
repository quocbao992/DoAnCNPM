package vn.doancnpm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.doancnpm.domain.Payment;

@Repository 
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByOrderId(Long orderId);
    Payment findByIdempotencyKey(String idempotencyKey);
    Payment findByTransactionRefer(String transactionRefer);
    boolean existsByIdempotencyKey(String idempotencyKey);
} 