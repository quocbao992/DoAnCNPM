package vn.doancnpm.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.doancnpm.domain.Topping;
import java.util.List;
@Repository
public interface ToppingRepository extends JpaRepository<Topping, Long> {
    List<Topping> findByStatus(Boolean status);
    List<Topping> findByProducts_Id(Long productId);
    List<Topping> findByProducts_IdAndStatus(Long productId, Boolean status);
}