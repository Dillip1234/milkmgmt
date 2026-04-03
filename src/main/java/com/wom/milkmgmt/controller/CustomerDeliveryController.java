package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.dto.CustomerDeliveryRequestDTO;
import com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO;
import com.wom.milkmgmt.service.CustomerDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class CustomerDeliveryController {

    private final CustomerDeliveryService service;

    @PostMapping("/register")
    public ResponseEntity<CustomerDeliveryResponseDTO> create(
            @RequestBody CustomerDeliveryRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

//    @GetMapping
//    public ResponseEntity<List<CustomerDeliveryResponseDTO>> getAll() {
//        return ResponseEntity.ok(service.getAll());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDeliveryResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDeliveryResponseDTO> update(
            @PathVariable Long id,
            @RequestBody CustomerDeliveryRequestDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<CustomerDeliveryResponseDTO> getDeliveries(
            @RequestParam(required = false) String deliveryPersonName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate) {

        return service.getDeliveries(deliveryPersonName, deliveryDate);
    }
}

