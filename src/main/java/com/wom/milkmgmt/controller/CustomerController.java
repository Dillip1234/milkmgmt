package com.wom.milkmgmt.controller;


import com.wom.milkmgmt.dto.CustomerRequestDTO;
import com.wom.milkmgmt.dto.CustomerResponseDTO;
import com.wom.milkmgmt.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // POST /api/customers
    @PostMapping("/register")
    public ResponseEntity<CustomerResponseDTO> createCustomer(
            @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(customerService.createCustomer(dto));
    }

    // PUT /api/customers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRequestDTO dto) {
        return ResponseEntity.ok(customerService.updateCustomer(id, dto));
    }

    // DELETE /api/customers/{id}  (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/customers/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomer(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    // GET /api/customers/delivery-person/{deliveryPersonId}
    @GetMapping("/delivery-person/{deliveryPersonId}")
    public ResponseEntity<List<CustomerResponseDTO>> getByDeliveryPerson(
            @PathVariable Long deliveryPersonId) {
        return ResponseEntity.ok(
                customerService.getCustomersByDeliveryPerson(deliveryPersonId));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
}