package com.wom.milkmgmt.service;

import com.wom.milkmgmt.dto.CustomerDeliveryRequestDTO;
import com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO;
import com.wom.milkmgmt.dto.DeliverySubmitRequest;
import com.wom.milkmgmt.entity.*;
import com.wom.milkmgmt.repository.CustomerDeliveryRepository;
import com.wom.milkmgmt.repository.CustomerRepository;
import com.wom.milkmgmt.repository.MilkDeliveryOrderRepository;
import com.wom.milkmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomerDeliveryService {

    private final CustomerDeliveryRepository deliveryRepo;
    private final CustomerRepository         customerRepo;
    private final UserRepository             userRepo;
    private final ModelMapper                modelMapper;
    private final MilkDeliveryOrderRepository milkDeliveryOrderRepository;

    // ─── CREATE ────────────────────────────────────────────────────────────────

    public CustomerDeliveryResponseDTO create(CustomerDeliveryRequestDTO dto) {

        // 1. Load customer (carries milk_type and regular_quantity)
        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException(
                        "Customer not found: " + dto.getCustomerId()));

        // 2. Get milk type from customer's subscription
        MilkType milkType = customer.getMilkType();

        // 3. Load delivery person from users table
        User deliveryPerson = userRepo.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new RuntimeException(
                        "Delivery person not found: " + dto.getDeliveryPersonId()));

        // 4. Build the transaction record
        CustomerDelivery delivery = new CustomerDelivery();

        // FK references
        delivery.setCustomer(customer);
        delivery.setDeliveryPerson(deliveryPerson);

        // Snapshots — captured now so historical records stay accurate
        delivery.setCustomerName(customer.getCustomerName());
        delivery.setDeliveryPersonName(deliveryPerson.getUsername());
        delivery.setMilkTypeName(milkType.getName());
       // delivery.setAnimal(milkType.getAnimal());
        delivery.setVolumeMl(milkType.getVolumeMl());
        delivery.setUnitPriceSnapshot(milkType.getPricePerUnit());

        // Quantities
        delivery.setAskedQuantity(customer.getRegularQuantity());  // from customers table
        delivery.setDeliveredQuantity(dto.getDeliveredQuantity());

        // total_price = asked_quantity * unit_price_snapshot
        delivery.setTotalPrice(
                milkType.getPricePerUnit()
                        .multiply(BigDecimal.valueOf(customer.getRegularQuantity()))
        );

        // Date defaults to today, status auto-resolved
        delivery.setDeliveryDate(LocalDate.now());
        delivery.setStatus(resolveStatus(
                customer.getRegularQuantity(), dto.getDeliveredQuantity()));
        delivery.setCreatedAt(LocalDateTime.now());

        return toDTO(deliveryRepo.save(delivery));
    }

    // ─── READ ALL ──────────────────────────────────────────────────────────────

    public List<CustomerDeliveryResponseDTO> getAll() {
        return deliveryRepo.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─── READ ONE ──────────────────────────────────────────────────────────────

    public CustomerDeliveryResponseDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    // ─── READ BY CUSTOMER ──────────────────────────────────────────────────────

    public List<CustomerDeliveryResponseDTO> getByCustomer(Long customerId) {
        return deliveryRepo.findByCustomerId(customerId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─── READ BY DELIVERY PERSON ───────────────────────────────────────────────

    public List<CustomerDeliveryResponseDTO> getByDeliveryPerson(Long deliveryPersonId) {
        return deliveryRepo.findByDeliveryPersonId(deliveryPersonId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─── READ BY DATE ──────────────────────────────────────────────────────────

    public List<CustomerDeliveryResponseDTO> getByDate(LocalDate date) {
        return deliveryRepo.findByDeliveryDate(date)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─── TODAY'S PENDING FOR A DELIVERY PERSON ─────────────────────────────────

    public List<CustomerDeliveryResponseDTO> getPendingToday(Long deliveryPersonId) {
        return deliveryRepo.findByDeliveryPersonIdAndStatusAndDeliveryDate(
                        deliveryPersonId, "pending", LocalDate.now())
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─── UPDATE ────────────────────────────────────────────────────────────────

    public CustomerDeliveryResponseDTO update(Long id, CustomerDeliveryRequestDTO dto) {
        CustomerDelivery delivery = findOrThrow(id);

        // Only delivered_quantity and status are updatable after creation
        delivery.setDeliveredQuantity(dto.getDeliveredQuantity());
        delivery.setStatus(resolveStatus(
                delivery.getAskedQuantity(), dto.getDeliveredQuantity()));

        return toDTO(deliveryRepo.save(delivery));
    }

    // ─── DELETE ────────────────────────────────────────────────────────────────

    public void delete(Long id) {
        if (!deliveryRepo.existsById(id))
            throw new RuntimeException("Delivery not found: " + id);
        deliveryRepo.deleteById(id);
    }

    // ─── HELPERS ───────────────────────────────────────────────────────────────

    private CustomerDelivery findOrThrow(Long id) {
        return deliveryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Delivery not found: " + id));
    }

    // Auto-resolve status from quantities
    private String resolveStatus(int asked, int delivered) {
        if (delivered == 0)      return "missed";
        if (delivered < asked)   return "partial";
        return "delivered";
    }

    // Entity → DTO using ModelMapper
    private CustomerDeliveryResponseDTO toDTO(CustomerDelivery delivery) {
        return modelMapper.map(delivery, CustomerDeliveryResponseDTO.class);
    }

    public List<CustomerDeliveryResponseDTO> getDeliveries(String name, LocalDate date) {
        return deliveryRepo.findByFilters(name, date);
    }

    @Transactional
    public void submitDeliveries(DeliverySubmitRequest request) {
        Long deliveryPersonId = request.getDeliveryPersonId();
        LocalDate deliveryDate = request.getDeliveryDate();

        // Step 1: Update each customer_delivery row and collect saved records
        List<CustomerDelivery> savedDeliveries = new ArrayList<>();

        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            CustomerDelivery cd = deliveryRepo.findById(item.getCustomerDeliveryId())
                    .orElseThrow(() -> new RuntimeException(
                            "CustomerDelivery not found: " + item.getCustomerDeliveryId()));

            cd.setDeliveredQuantity(item.getDeliveredQuantity());
            cd.setDeliveryDate(deliveryDate);
            cd.setStatus("pending");

            savedDeliveries.add(deliveryRepo.save(cd));
        }

        // Step 2: Aggregate total deliveredQuantity per MilkType directly from saved records
        Map<MilkType, Integer> milkTypeTotals = new HashMap<>();

        for (CustomerDelivery cd : savedDeliveries) {
            // resolve MilkType from the customer FK
            MilkType milkType = cd.getCustomer().getMilkType();
            int qty = cd.getDeliveredQuantity() != null ? cd.getDeliveredQuantity() : 0;
            milkTypeTotals.merge(milkType, qty, Integer::sum);
        }

        // Step 3: Fetch delivery person entity
        User deliveryPerson = userRepo.findById(deliveryPersonId)
                .orElseThrow(() -> new RuntimeException("Delivery person not found: " + deliveryPersonId));

        // Step 4: Save one MilkDeliveryOrder row per milk type — skip if total is 0
        for (Map.Entry<MilkType, Integer> entry : milkTypeTotals.entrySet()) {
            MilkType milkType = entry.getKey();
            Integer totalQuantity = entry.getValue();

            if (totalQuantity <= 0) continue; // skip — would violate asked_quantity > 0 constraint

            MilkDeliveryOrder order = new MilkDeliveryOrder();
            order.setDeliveryPerson(deliveryPerson);
            order.setMilkType(milkType);
            order.setAskedQuantity(totalQuantity);
            order.setUnitPriceSnapshot(milkType.getPricePerUnit());
            order.setTotalPrice(milkType.getPricePerUnit().multiply(BigDecimal.valueOf(totalQuantity)));
            order.setOrderDate(deliveryDate);
            order.setStatus("pending");
            order.setCreatedAt(LocalDateTime.now());

            milkDeliveryOrderRepository.save(order);
        }
    }

    // ─── SAVE DELIVERIES (date + quantity validation, only updates customer_deliveries) ──

    public void saveDeliveries(DeliverySubmitRequest request) {
        Long deliveryPersonId = request.getDeliveryPersonId();
        LocalDate deliveryDate = request.getDeliveryDate();

        // Scenario 1: date must be today
        if (!LocalDate.now().equals(deliveryDate)) {
            throw new RuntimeException("Date is not matching. You can only save deliveries for today's date.");
        }

        // Scenario 2: validate total quantity per milk type against milk_delivery_orders

        // Step 2a: build a map of customerDeliveryId → deliveredQuantity from the payload
        Map<Long, Integer> payloadQuantityMap = new HashMap<>();
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            payloadQuantityMap.put(item.getCustomerDeliveryId(), item.getDeliveredQuantity());
        }

        // Step 2b: fetch existing customer_deliveries for this delivery person on this date
        List<CustomerDelivery> existingDeliveries = deliveryRepo
                .findByDeliveryPersonIdAndDeliveryDate(deliveryPersonId, deliveryDate);

        // Step 2c: aggregate total delivered quantity per milk type name
        Map<String, Integer> milkTypeTotalMap = new HashMap<>();
        for (CustomerDelivery cd : existingDeliveries) {
            // use payload quantity if provided, else existing delivered quantity
            Integer qty = payloadQuantityMap.getOrDefault(
                    cd.getId(),
                    cd.getDeliveredQuantity() != null ? cd.getDeliveredQuantity() : 0
            );
            milkTypeTotalMap.merge(cd.getMilkTypeName(), qty, Integer::sum);
        }

        // Step 2d: fetch milk_delivery_orders for this delivery person on this date
        List<MilkDeliveryOrder> orders = milkDeliveryOrderRepository
                .findByDeliveryPersonIdAndOrderDate(deliveryPersonId, deliveryDate);

        // Step 2e: validate each milk type total against asked_quantity in milk_delivery_orders
        for (MilkDeliveryOrder order : orders) {
            String milkTypeName = order.getMilkType().getName();
            Integer totalDelivered = milkTypeTotalMap.getOrDefault(milkTypeName, 0);

            if (!totalDelivered.equals(order.getAskedQuantity())) {
                throw new RuntimeException(
                        "Milk type quantity is not matching for '" + milkTypeName
                        + "'. Expected: " + order.getAskedQuantity()
                        + ", Got: " + totalDelivered
                );
            }
        }

        // Validation passed — update only customer_deliveries: delivered_quantity + status resolved from quantities
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            CustomerDelivery cd = deliveryRepo.findById(item.getCustomerDeliveryId())
                    .orElseThrow(() -> new RuntimeException(
                            "CustomerDelivery not found: " + item.getCustomerDeliveryId()));

            cd.setDeliveredQuantity(item.getDeliveredQuantity());
            cd.setStatus(resolveStatus(cd.getAskedQuantity(), item.getDeliveredQuantity()));

            deliveryRepo.save(cd);
        }
    }

}