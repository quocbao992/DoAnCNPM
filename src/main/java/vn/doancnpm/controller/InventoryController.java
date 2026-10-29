package vn.doancnpm.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.doancnpm.dto.Request.ProductStockRequest;
import vn.doancnpm.dto.Request.ToppingStockRequest;
import vn.doancnpm.dto.Response.InventoryLogResponse;
import vn.doancnpm.dto.Response.InventoryStockResponse;
import vn.doancnpm.service.InventoryLogService;
import vn.doancnpm.service.ProductInventoryService;
import vn.doancnpm.service.ToppingInventoryService;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final ProductInventoryService productInventoryService;
    private final ToppingInventoryService toppingInventoryService;
    private final InventoryLogService inventoryLogService;

    // ======================== TOPPING ========================

    @GetMapping("/toppings/{toppingId}")
    public ResponseEntity<InventoryStockResponse> getToppingStock(@PathVariable Long toppingId) {
        return ResponseEntity.ok(toppingInventoryService.getStock(toppingId));
    }

    @PostMapping("/toppings/import")
    public ResponseEntity<String> importTopping(@Valid @RequestBody ToppingStockRequest request) {
        toppingInventoryService.addStock(request.getToppingId(), null, request.getAmount(),
                reasonOrDefault(request.getReason()));
        return ResponseEntity.ok("Nhập kho Topping thành công! Số lượng nhập: " + request.getAmount());
    }

    @PostMapping("/toppings/deduct")
    public ResponseEntity<String> deductTopping(@Valid @RequestBody ToppingStockRequest request) {
        toppingInventoryService.deductStock(request.getToppingId(), null, request.getAmount());
        return ResponseEntity.ok("Trừ kho Topping thành công! Đã trừ: " + request.getAmount());
    }

    @GetMapping("/logs/toppings/{toppingId}")
    public ResponseEntity<List<InventoryLogResponse>> getToppingLogs(@PathVariable Long toppingId) {
        return ResponseEntity.ok(inventoryLogService.getToppingLogs(toppingId));
    }

    @GetMapping("/toppings/check")
    public ResponseEntity<Boolean> checkToppingStock(
            @RequestParam("toppingId") Long toppingId,
            @RequestParam("amount") int amount) {
        requirePositive(amount);
        return ResponseEntity.ok(toppingInventoryService.checkStockAvailable(toppingId, amount));
    }

    // ======================== PRODUCT ========================

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryStockResponse> getProductStock(@PathVariable Long productId) {
        return ResponseEntity.ok(productInventoryService.getStock(productId));
    }

    @PostMapping("/products/import")
    public ResponseEntity<String> importProduct(@Valid @RequestBody ProductStockRequest request) {
        productInventoryService.addStock(request.getProductId(), null, request.getAmount(),
                reasonOrDefault(request.getReason()));
        return ResponseEntity.ok("Nhập kho Product thành công! Số lượng nhập: " + request.getAmount());
    }

    @PostMapping("/products/deduct")
    public ResponseEntity<String> deductProduct(@Valid @RequestBody ProductStockRequest request) {
        productInventoryService.deductStock(request.getProductId(), null, request.getAmount());
        return ResponseEntity.ok("Trừ kho Product thành công! Đã trừ: " + request.getAmount());
    }

    @GetMapping("/logs/products/{productId}")
    public ResponseEntity<List<InventoryLogResponse>> getProductLogs(@PathVariable Long productId) {
        return ResponseEntity.ok(inventoryLogService.getProductLogs(productId));
    }

    @GetMapping("/products/check")
    public ResponseEntity<Boolean> checkProductStock(
            @RequestParam("productId") Long productId,
            @RequestParam("amount") int amount) {
        requirePositive(amount);
        return ResponseEntity.ok(productInventoryService.checkStockAvailable(productId, amount));
    }

    // ======================== Hàm phụ ========================

    private String reasonOrDefault(String reason) {
        return (reason != null && !reason.isBlank()) ? reason : "IMPORT";
    }

    private void requirePositive(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount phải lớn hơn 0");
        }
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

    // Lỗi từ @Valid trong ProductStockRequest / ToppingStockRequest
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Dữ liệu không hợp lệ");
        return buildError(HttpStatus.BAD_REQUEST, message);
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