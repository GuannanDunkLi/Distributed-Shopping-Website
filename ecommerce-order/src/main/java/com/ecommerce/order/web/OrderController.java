package com.ecommerce.order.web;

import com.ecommerce.order.dto.OrderDto;
import com.ecommerce.order.pojo.Order;
import com.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /*
     * Create order
     * @param: orderDto
     * @return org.springframework.http.ResponseEntity<java.lang.Long>
     * @author dunklee
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody @Valid OrderDto orderDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(orderDto));
    }

    /*
     * Generate payment url
     * @param: orderId
     * @return ResponseEntity<String>
     * @author dunklee
     */
    @GetMapping("url/{id}")
    public ResponseEntity<String> generateUrl(@PathVariable("id") Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.generateUrl(orderId));
    }

    /*
     * Query order details based on order ID
     * @param: orderId
     * @return ResponseEntity<Order>
     * @author dunklee
     */
    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrderById(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryById(orderId));
    }

    /*
     * Check order payment status
     * @param: orderId
     * @return ResponseEntity<Integer>
     * @author dunklee
     */
    @GetMapping("state/{id}")
    public ResponseEntity<Integer> queryOrderStateByOrderId(@PathVariable("id") Long orderId) {
        return ResponseEntity.ok(orderService.queryOrderState(orderId).getValue());
    }
}
