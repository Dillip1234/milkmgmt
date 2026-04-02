package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.dto.MilkDeliveryOrderDTO;
import com.wom.milkmgmt.entity.MilkDeliveryOrder;
import com.wom.milkmgmt.service.MilkDeliveryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class MilkDeliveryOrderController {

    private final MilkDeliveryOrderService orderService;

    @PostMapping
    public ResponseEntity<MilkDeliveryOrder> create(
            @RequestBody MilkDeliveryOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<MilkDeliveryOrder>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MilkDeliveryOrder> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MilkDeliveryOrder> update(
            @PathVariable Long id,
            @RequestBody MilkDeliveryOrderDTO dto) {
        return ResponseEntity.ok(orderService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
