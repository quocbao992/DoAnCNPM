package vn.doancnpm.controller;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/mock-gateway")
public class MockGatewayController {

    @Value("${mock.gateway.delay-ms:3000}") private long delayMs;
    @Value("${mock.gateway.success-rate:0.8}") private double successRate;
    @Value("${mock.gateway.base-url:http://localhost:8082}") private String baseUrl;
    @Value("${payment.gateway.secret:mock-secret}") private String secret;

    private final RestClient restClient = RestClient.create();

    @RequestMapping(value = "/pay", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<Map<String, Object>> pay(
            @RequestParam Long orderId,
            @RequestParam(required = false) String amount,
            @RequestParam String method,
            @RequestParam String ref,
            @RequestParam(required = false) String result) {

        String status = (result != null && !result.isBlank())
                ? result.trim().toUpperCase()
                : (ThreadLocalRandom.current().nextDouble() < successRate ? "SUCCESS" : "FAILED");
        String transactionId = "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> body = Map.of(
                "orderId", orderId,
                "transactionId", transactionId,
                "status", status,
                "idempotencyKey", ref,
                "method", method);

        CompletableFuture.runAsync(() -> {
            try {
                restClient.post()
                        .uri(baseUrl + "/api/v1/payments/notify")
                        .header("X-Gateway-Secret", secret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .toBodilessEntity();
                log.info("Mock gateway đã báo order {} = {}", orderId, status);
            } catch (Exception e) {
                log.error("Mock gateway báo về backend thất bại: {}", e.getMessage());
            }
        }, CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS));

        return ResponseEntity.accepted().body(Map.of(
                "message", "Đang xử lý thanh toán...",
                "orderId", orderId,
                "transactionId", transactionId,
                "simulatedResult", status,
                "willNotifyInMs", delayMs));
    }
}