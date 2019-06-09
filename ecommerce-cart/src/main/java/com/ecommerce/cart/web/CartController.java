package com.ecommerce.cart.web;

import com.ecommerce.cart.pojo.Cart;
import com.ecommerce.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    /*
     * Query cart list
     * @param:
     * @return ResponseEntity<List<Cart>>
     * @author dunklee
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> queryCartList() {
        return ResponseEntity.ok(cartService.queryCartList());
    }

    /*
     * Add goods to cart
     * @param: cart
     * @return org.springframework.http.ResponseEntity<java.lang.Void>
     * @author dunklee
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        this.cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /*
     * update number of goods in cart
     * @param: skuId
     * @param: num
     * @return ResponseEntity<Void>
     * @author dunklee
     */
    @PutMapping
    public ResponseEntity<Void> updateCartNum(@RequestParam("id") Long skuId, @RequestParam("num") Integer num) {
        cartService.updateNum(skuId, num);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /*
     * delete goods in cart
     * @param: skuId
     * @return ResponseEntity<Void>
     * @author dunklee
     */
    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteCartNum(@PathVariable("skuId") Long skuId) {
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
