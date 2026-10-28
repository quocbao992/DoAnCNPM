package vn.doancnpm.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.domain.Topping;
import vn.doancnpm.repository.ToppingRepository;

@RestController
@RequestMapping("/api/v1/toppings")
@RequiredArgsConstructor
public class ToppingController {

    private final ToppingRepository toppingRepository;

    @GetMapping
    public ResponseEntity<List<Topping>> getAllToppings() {
        return ResponseEntity.ok(toppingRepository.findByStatus(true));
    }
}

