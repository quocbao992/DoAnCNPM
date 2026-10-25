package vn.doancnpm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doancnpm.domain.Inventory;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}