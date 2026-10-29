package vn.doancnpm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.doancnpm.dto.Request.ToppingRequest;
import vn.doancnpm.dto.Response.ToppingResponse;
import vn.doancnpm.service.ToppingService;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.ExceptionHandler;
@RestController
@RequestMapping("/api/v1/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingService toppingService;
    
    @GetMapping
    public ResponseEntity<List<ToppingResponse>> getAllToppings(
            @RequestParam(required = false) Boolean status) {
        return ResponseEntity.ok(toppingService.getAllToppings(status));
    }

    // 1 Lấy chi tiết một topping theo ID
        @GetMapping("/{id}")
        public ResponseEntity<ToppingResponse> getToppingById(@PathVariable Long id) {
            return ResponseEntity.ok(toppingService.getToppingById(id));
        }
    // 2 Thêm mới Topping
    @PostMapping
    public ResponseEntity<ToppingResponse> createTopping(@RequestBody ToppingRequest request) {
        ToppingResponse created = toppingService.createTopping(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 3 Cập nhật tên và đơn giá Topping
    @PutMapping("/{id}")
    public ResponseEntity<ToppingResponse> updateTopping(
            @PathVariable Long id, 
            @RequestBody ToppingRequest request) {
        ToppingResponse updated = toppingService.updateTopping(id, request);
        return ResponseEntity.ok(updated);
    }
    // 4 cập nhật trạng thái
    @PatchMapping("/{id}/status")
        public ResponseEntity<ToppingResponse> updateToppingStatus(
                @PathVariable Long id,
                @RequestParam boolean status) {
            ToppingResponse updated = toppingService.updateStatus(id, status);
            return ResponseEntity.ok(updated);
        }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
    ex.printStackTrace(); // In lỗi đỏ ra terminal IDE
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", 500);
    body.put("error", "Internal Server Error");
    body.put("message", ex.getMessage()); // Hiển thị nguyên nhân lỗi
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
} 
}
