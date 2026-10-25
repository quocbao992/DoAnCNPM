
package vn.doancnpm.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import vn.doancnpm.dto.Request.StockUpdateRequest;
import vn.doancnpm.dto.Response.InventoryLogResponse;
import vn.doancnpm.dto.Response.InventoryStockResponse;
import vn.doancnpm.service.InventoryService;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    //PHẦN TOPPING

    // 1. Xem số lượng tồn kho hiện tại của Topping
    @GetMapping("/toppings/{toppingId}")
    public ResponseEntity<InventoryStockResponse> getToppingStock(@PathVariable Long toppingId) {
        return ResponseEntity.ok(inventoryService.getToppingStock(toppingId));
    }
    // thêm topping
    @PostMapping("/toppings/import")
    public ResponseEntity<String> importTopping(@RequestBody StockUpdateRequest request) {
        requireId(request.getToppingId(), "toppingId");
        int amount = requireAmount(request);
        String reason = (request.getReason() != null && !request.getReason().isEmpty())
                        ? request.getReason() : "IMPORT";
        inventoryService.addStock(null, request.getToppingId(), null, amount, reason);
        return ResponseEntity.ok("Nhập kho Topping thành công! Số lượng nhập: " + amount);
    }
    //trừ topping
    @PostMapping("/toppings/deduct")
    public ResponseEntity<String> deductTopping(@RequestBody StockUpdateRequest request) {
        requireId(request.getToppingId(), "toppingId");
        int amount = requireAmount(request);
        inventoryService.deductStock(null, request.getToppingId(), null, amount);
        return ResponseEntity.ok("Trừ kho Topping thành công! Đã trừ: " + amount);
    }

    // 4. Xem lịch sử biến động kho của Topping
    @GetMapping("/logs/toppings/{toppingId}")
    public ResponseEntity<List<InventoryLogResponse>> getToppingLogs(@PathVariable Long toppingId) {
        return ResponseEntity.ok(inventoryService.getToppingLogs(toppingId));
    }

    // 5. Kiểm tra Topping còn đủ số lượng để bán không
    @GetMapping("/toppings/check")
    public ResponseEntity<Boolean> checkToppingStock(
            @RequestParam ("productId") Long toppingId,
            @RequestParam ("amount") int amount) {
        if (amount <=0){
            throw new IllegalArgumentException ("amout phải lớn hơn 0");
        }
        boolean available = inventoryService.checkStockAvailable(null, toppingId, amount);
        return ResponseEntity.ok(available);
    }

    //PHẦN PRODUCT

    // 1 Xem tồn kho Product
    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryStockResponse> getProductStock(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductStock(productId));
    }
    // 2 Thêm product
    @PostMapping("/products/import")
    public ResponseEntity<String> importProduct(@RequestBody StockUpdateRequest request) {
        requireId(request.getProductId(), "productId");
        int amount = requireAmount(request);
        String reason = (request.getReason() != null && !request.getReason().isEmpty())
                        ? request.getReason() : "IMPORT";
        inventoryService.addStock(request.getProductId(), null, null, amount, reason);
        return ResponseEntity.ok("Nhập kho Product thành công! Số lượng nhập: " + amount);
    }

    //3 trừ kho product
    @PostMapping("/products/deduct")
    public ResponseEntity<String> deductProduct(@RequestBody StockUpdateRequest request) {
        requireId(request.getProductId(), "productId");
        int amount = requireAmount(request);
        inventoryService.deductStock(request.getProductId(), null, null, amount);
        return ResponseEntity.ok("Trừ kho Product thành công! Đã trừ: " + amount);
    }

    // 4 Xem lịch sử biến động kho của Product
    @GetMapping("/logs/products/{productId}")
    public ResponseEntity<List<InventoryLogResponse>> getProductLogs(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryService.getProductLogs(productId));
    }
        // ======================== Hàm kiểm tra đầu vào ========================

    private void requireId(Long id, String fieldName) {
        if (id == null) {
            throw new IllegalArgumentException(fieldName + " không được để trống");
        }
    }

    private int requireAmount(StockUpdateRequest request) {
        Integer amount = request.getAmount();
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("amount phải lớn hơn 0");
        }
        return amount;
    }
    // 5 Kiểm tra Product còn đủ số lượng để bán không
    @GetMapping("/products/check")
    public ResponseEntity<Boolean> checkProductStock(
            @RequestParam("productId") Long productId,
            @RequestParam("amount") int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount phải lớn hơn 0");
        }
        boolean available = inventoryService.checkStockAvailable(productId, null, amount);
        return ResponseEntity.ok(available);
    }
        // ======================== Xử lý lỗi ========================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return buildError(HttpStatus.BAD_REQUEST,
                "Tham số '" + ex.getName() + "' có giá trị không hợp lệ: " + ex.getValue());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Thiếu tham số: " + ex.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadJson(HttpMessageNotReadableException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "Body JSON không hợp lệ");
    }

    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}