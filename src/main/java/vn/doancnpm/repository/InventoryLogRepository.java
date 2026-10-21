package vn.doancnpm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doancnpm.domain.InventoryLog;
import java.util.List;
@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    List<InventoryLog> findByProductId(Long productId);
    List<InventoryLog> findByProductIdOrderByCreatedAtDesc(Long productId);
}