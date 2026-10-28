package vn.doancnpm.controller;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
 
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import vn.doancnpm.dto.Request.PaymentRequest;
import vn.doancnpm.dto.Response.PaymentResponse;
import vn.doancnpm.service.PaymentService;
 
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor

public class PaymentController {
    private final PaymentService paymentService;
 
    // Tạo giao dịch thanh toán cho đơn hàng
    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest rq) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(rq));
    }
 
    // Cập nhật trạng thái: PUT /api/payments/5/status?status=SUCCESS&transactionRef=ABC123
    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updateStatus(
            @PathVariable Long paymentId,
            @RequestParam String status,
            @RequestParam(required = false) String transactionRef) {
        return ResponseEntity.ok(paymentService.updateStatus(paymentId, status, transactionRef));
    }
 
    // Xem một giao dịch
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.getPayment(paymentId));
    }
 
    // Xem các giao dịch của một đơn hàng
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrder(orderId));
    }
}
