package vn.doancnpm.controller;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.service.PromotionService;

@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
public class PromotionController {
    private final PromotionService promotionService;

    // GET /api/promotions/validate?code=GIAM10&amount=200000  -> số tiền được giảm
    @GetMapping("/validate")
    public ResponseEntity<BigDecimal> validate(@RequestParam String code,
                                               @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(promotionService.calculateDiscount(code, amount));
    }
}
