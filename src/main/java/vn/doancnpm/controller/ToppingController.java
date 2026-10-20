package vn.doancnpm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.service.ToppingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingService toppingService;

    // 1. Lấy danh sách topping đang hoạt động (status = true)
    @GetMapping
    public ResponseEntity<List<Topping>> getActiveToppings() {
        return ResponseEntity.ok(toppingService.getActiveToppings());
    }

    // 2. Thêm mới Topping
    @PostMapping
    public ResponseEntity<Topping> createTopping(@RequestBody Topping topping) {
        Topping created = toppingService.createTopping(topping);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 3. Cập nhật tên và đơn giá Topping
    @PutMapping("/{id}")
    public ResponseEntity<Topping> updateTopping(@PathVariable Long id, @RequestBody Topping topping) {
        Topping updated = toppingService.updateTopping(id, topping);
        return ResponseEntity.ok(updated);
    }

    // 4. Ẩn Topping (Soft delete - chuyển status thành false)
    @PatchMapping("/{id}/hide")
    public ResponseEntity<String> hideTopping(@PathVariable Long id) {
        toppingService.hideTopping(id);
        return ResponseEntity.ok("Topping với ID " + id + " đã được ẩn thành công.");
    }
}