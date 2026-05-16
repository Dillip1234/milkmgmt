package com.wom.milkmgmt.controller;

import com.wom.milkmgmt.dto.CustomerDeliveryRequestDTO;
import com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO;
import com.wom.milkmgmt.dto.DeliverySubmitRequest;
import com.wom.milkmgmt.service.CustomerDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class CustomerDeliveryController {

    private final CustomerDeliveryService service;

    @PostMapping("/register")
    public ResponseEntity<CustomerDeliveryResponseDTO> create(@RequestBody CustomerDeliveryRequestDTO dto) {
        log.info("Registering delivery for customerId: {}", dto.getCustomerId());
        CustomerDeliveryResponseDTO response = service.create(dto);
        log.info("Delivery registered with id: {}", response.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerDeliveryResponseDTO> getById(@PathVariable Long id) {
        log.info("Fetching delivery id: {}", id);
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerDeliveryResponseDTO> update(@PathVariable Long id, @RequestBody CustomerDeliveryRequestDTO dto) {
        log.info("Updating delivery id: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting delivery id: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<CustomerDeliveryResponseDTO> getDeliveries(
            @RequestParam(required = false) Long deliveryPersonId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate) {
        log.info("Fetching deliveries for deliveryPersonId: {}, date: {}", deliveryPersonId, deliveryDate);
        return service.getDeliveriesByPersonIdAndDate(deliveryPersonId, deliveryDate);
    }

    @GetMapping("/report")
    public ResponseEntity<List<CustomerDeliveryResponseDTO>> getReport(
            @RequestParam Long deliveryPersonId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Report request for deliveryPersonId: {}, from: {}, to: {}", deliveryPersonId, fromDate, toDate);
        return ResponseEntity.ok(service.getReport(deliveryPersonId, fromDate, toDate));
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadReport(
            @RequestParam Long deliveryPersonId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        log.info("Excel download for deliveryPersonId: {}, from: {}, to: {}", deliveryPersonId, fromDate, toDate);
        byte[] excelBytes = service.downloadReportAsExcel(deliveryPersonId, fromDate, toDate);
        return ResponseEntity.ok()
                .header("Content-Disposition",
                        "attachment; filename=delivery_report_" + deliveryPersonId + "_" + fromDate + "_to_" + toDate + ".xlsx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excelBytes);
    }

    @PostMapping("/submit")
    public ResponseEntity<String> submitDeliveries(@RequestBody DeliverySubmitRequest request) {
        log.info("Submitting deliveries for deliveryPersonId: {}, date: {}", request.getDeliveryPersonId(), request.getDeliveryDate());
        service.submitDeliveries(request);
        log.info("Deliveries submitted successfully for deliveryPersonId: {}", request.getDeliveryPersonId());
        return ResponseEntity.ok("Deliveries updated successfully");
    }

    @PutMapping("/save")
    public ResponseEntity<String> saveDeliveries(@RequestBody DeliverySubmitRequest request) {
        log.info("Saving deliveries for deliveryPersonId: {}, date: {}", request.getDeliveryPersonId(), request.getDeliveryDate());
        service.saveDeliveries(request);
        log.info("Deliveries saved successfully for deliveryPersonId: {}", request.getDeliveryPersonId());
        return ResponseEntity.ok("Deliveries saved successfully");
    }
}
