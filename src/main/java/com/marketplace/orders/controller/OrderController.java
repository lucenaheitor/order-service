package com.marketplace.orders.controller;

import com.marketplace.orders.dto.OrderRequest;
import com.marketplace.orders.dto.OrderResponse;
import com.marketplace.orders.dto.PageResponse;
import com.marketplace.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> findAll(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "3") int size
    ) {
        return ResponseEntity.ok(orderService.findAll(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> update(@PathVariable Long id, @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id) {

        return ResponseEntity.status(HttpStatus.OK).body(orderService.cancelOrder(id));

    }

    @PatchMapping("/{id}/confirmed")
    public ResponseEntity<OrderResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.confirmOrder(id));
    }

    @PatchMapping("/shipped")
    public ResponseEntity<OrderResponse> ship(@RequestParam Long orderId) {
        return ResponseEntity.status(HttpStatus.OK).body(orderService.shipOrder(orderId));
    }



}
