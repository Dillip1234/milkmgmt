package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.dto.BulkQuantityUpdateDTO;
import com.wom.milkmgmt.dto.DeliveryPersonDTO;
import com.wom.milkmgmt.dto.MilkDeliveryOrderDTO;
import com.wom.milkmgmt.dto.MilkDeliveryOrderDetailDTO;
import com.wom.milkmgmt.entity.MilkDeliveryOrder;
import com.wom.milkmgmt.service.MilkDeliveryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class MilkDeliveryOrderController {

    private final MilkDeliveryOrderService orderService;

    @PostMapping("/register")
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

    @PutMapping("/bulk-update-quantity")
    public ResponseEntity<List<MilkDeliveryOrder>> bulkUpdateQuantity(
            @RequestBody List<BulkQuantityUpdateDTO> updates) {
        return ResponseEntity.ok(orderService.bulkUpdateQuantity(updates));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/delivery-person")
    public ResponseEntity<List<DeliveryPersonDTO>> getAllDeliveryPersons() {
        return ResponseEntity.ok(orderService.getAllDeliveryPersons());
    }

    @GetMapping("/delivery-person/{deliveryPersonId}")
    public ResponseEntity<List<MilkDeliveryOrderDetailDTO>> getOrdersByDeliveryPerson(
            @PathVariable Long deliveryPersonId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate) {
        return ResponseEntity.ok(orderService.getOrdersByDeliveryPerson(deliveryPersonId, orderDate));
    }

//    @GetMapping
//    public ResponseEntity<List<MilkDeliveryOrder>> getOrders(
//            @RequestParam Long deliveryPersonId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate) {
//
//        List<MilkDeliveryOrder> orders = orderService.getOrdersByPersonAndDate(deliveryPersonId, orderDate);
//        return ResponseEntity.ok(orders);
//    }
}
