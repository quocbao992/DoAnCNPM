package vn.doancnpm.service.implement;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.repository.ToppingRepository;
import vn.doancnpm.service.ToppingService;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IToppingService implements ToppingService {

    private final ToppingRepository toppingRepository;

    @Override
    public List<Topping> getActiveToppings() {
        return toppingRepository.findByStatus(true);
    }

    @Override
    @Transactional
    public Topping createTopping(Topping topping) {
        if (topping.getStock() == null) {
            topping.setStock(0);
        }
        topping.setStatus(true);
        return toppingRepository.save(topping);
    }

    @Override
    @Transactional
    public Topping updateTopping(Long id, Topping newDetails) {
        Topping existingTopping = toppingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Topping với ID: " + id));

        existingTopping.setName(newDetails.getName());
        existingTopping.setUnitPrice(newDetails.getUnitPrice());

        return toppingRepository.save(existingTopping);
    }

    @Override
    @Transactional
    public void hideTopping(Long id) {
        Topping topping = toppingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Topping với ID: " + id));
        topping.setStatus(false);
        toppingRepository.save(topping);
    }
}