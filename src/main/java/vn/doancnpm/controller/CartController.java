package vn.doancnpm.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import vn.doancnpm.dto.Request.CartRequest;
import vn.doancnpm.service.CartService;

@RestController 
@RequestMapping (value ="/api/v1/cart")
@RequiredArgsConstructor 
public class CartController {
    private final CartService cartService;
    @PostMapping
    public ResponseEntity<String> addtoCart(@RequestBody CartRequest rq){
        cartService.addToCart(rq);
        return new ResponseEntity<>("Thêm món vào giỏ hàng thành công !",HttpStatus.CREATED);
    }
    
}
