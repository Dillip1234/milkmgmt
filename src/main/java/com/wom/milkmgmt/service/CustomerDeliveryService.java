package com.wom.milkmgmt.service;

import com.wom.milkmgmt.dto.CustomerDeliveryRequestDTO;
import com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO;
import com.wom.milkmgmt.dto.DeliverySubmitRequest;
import com.wom.milkmgmt.entity.*;
import com.wom.milkmgmt.repository.CustomerDeliveryRepository;
import com.wom.milkmgmt.repository.CustomerRepository;
import com.wom.milkmgmt.repository.MilkDeliveryOrderRepository;
import com.wom.milkmgmt.repository.MilkTypeRepository;
import com.wom.milkmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final MilkTypeRepository         milkTypeRepository;

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
        delivery.setMilkTypeId(milkType.getId());
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

    public List<CustomerDeliveryResponseDTO> getDeliveriesByPersonIdAndDate(Long deliveryPersonId, LocalDate date) {
        return deliveryRepo.findByDeliveryPersonIdAndDateFilter(deliveryPersonId, date);
    }

    public List<CustomerDeliveryResponseDTO> getReport(Long deliveryPersonId, LocalDate fromDate, LocalDate toDate) {
        //log.info("Generating report for deliveryPersonId: {}, from: {}, to: {}", deliveryPersonId, fromDate, toDate);
        return deliveryRepo.findReportByDeliveryPersonAndDateRange(deliveryPersonId, fromDate, toDate);
    }

    @Transactional
    public void submitDeliveries(DeliverySubmitRequest request) {
        Long deliveryPersonId = request.getDeliveryPersonId();
        LocalDate deliveryDate = request.getDeliveryDate();

        // Step 1: For each item in payload
        // customerDeliveryId = customer_id in customer_deliveries
        // Same date → update existing row | Different date → insert new row
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {

            // find customer_deliveries row by customer_id + delivery_person_id + delivery_date
            List<CustomerDelivery> cdList = deliveryRepo
                    .findByCustomerIdAndDeliveryPersonIdAndDeliveryDate(
                            item.getCustomerDeliveryId(), deliveryPersonId, deliveryDate);

            CustomerDelivery cd;
            if (cdList.isEmpty()) {
                // no row for this date — create new record from customers table
                Customer customer = customerRepo.findById(item.getCustomerDeliveryId())
                        .orElseThrow(() -> new RuntimeException(
                                "Customer not found: " + item.getCustomerDeliveryId()));

                User deliveryPersonUser = userRepo.findById(deliveryPersonId)
                        .orElseThrow(() -> new RuntimeException(
                                "Delivery person not found: " + deliveryPersonId));

                MilkType milkType = item.getMilkTypeId() != null
                        ? milkTypeRepository.findById(item.getMilkTypeId())
                            .orElseThrow(() -> new RuntimeException("MilkType not found: " + item.getMilkTypeId()))
                        : customer.getMilkType();

                cd = new CustomerDelivery();
                cd.setCustomer(customer);
                cd.setDeliveryPerson(deliveryPersonUser);
                cd.setCustomerName(customer.getCustomerName());
                cd.setDeliveryPersonName(deliveryPersonUser.getUsername());
                cd.setMilkTypeName(milkType.getName());
                cd.setMilkTypeId(milkType.getId());
                cd.setVolumeMl(milkType.getVolumeMl());
                cd.setUnitPriceSnapshot(milkType.getPricePerUnit());
                cd.setAskedQuantity(customer.getRegularQuantity());
                cd.setTotalPrice(milkType.getPricePerUnit()
                        .multiply(BigDecimal.valueOf(customer.getRegularQuantity())));
                cd.setCreatedAt(LocalDateTime.now());
//                log.info("Inserting new customer_deliveries row for customerId: {}, date: {}",
//                        item.getCustomerDeliveryId(), deliveryDate);
            } else {
                // row exists for this date — update it
                cd = cdList.get(0);
//                log.info("Updating existing customer_deliveries row id: {} for customerId: {}, date: {}",
//                        cd.getId(), item.getCustomerDeliveryId(), deliveryDate);

                // update milk type if provided
                if (item.getMilkTypeId() != null) {
                    MilkType newMilkType = milkTypeRepository.findById(item.getMilkTypeId())
                            .orElseThrow(() -> new RuntimeException("MilkType not found: " + item.getMilkTypeId()));
                    cd.setMilkTypeId(newMilkType.getId());
                    cd.setMilkTypeName(newMilkType.getName());
                    cd.setVolumeMl(newMilkType.getVolumeMl());
                    cd.setUnitPriceSnapshot(newMilkType.getPricePerUnit());
                    cd.setTotalPrice(newMilkType.getPricePerUnit()
                            .multiply(BigDecimal.valueOf(item.getDeliveredQuantity())));
                }
            }

            cd.setDeliveredQuantity(item.getDeliveredQuantity());
            cd.setDeliveryDate(deliveryDate);
            cd.setStatus("pending");

            deliveryRepo.save(cd);
        }

        // Step 2: Fetch ALL customer_deliveries for this delivery person on this delivery date
        // and aggregate total delivered_quantity per milk_type_id
        List<CustomerDelivery> allDeliveries = deliveryRepo
                .findByDeliveryPersonIdAndDeliveryDate(deliveryPersonId, deliveryDate);

        // key = milk_type_id, value = sum of delivered_quantity
        Map<Long, Integer> milkTypeTotals = new HashMap<>();
        for (CustomerDelivery cd : allDeliveries) {
            Long milkTypeId = cd.getMilkTypeId();
            if (milkTypeId == null) continue;
            int qty = cd.getDeliveredQuantity() != null ? cd.getDeliveredQuantity() : 0;
            milkTypeTotals.merge(milkTypeId, qty, Integer::sum);
        }

        // Step 3: Fetch delivery person entity
        User deliveryPerson = userRepo.findById(deliveryPersonId)
                .orElseThrow(() -> new RuntimeException("Delivery person not found: " + deliveryPersonId));

        // Step 4: Delete ALL existing milk_delivery_orders for this deliveryPerson + date
        // then insert fresh rows — one per milk type
        milkDeliveryOrderRepository.deleteByDeliveryPersonIdAndOrderDate(deliveryPersonId, deliveryDate);

        for (Map.Entry<Long, Integer> entry : milkTypeTotals.entrySet()) {
            Long milkTypeId = entry.getKey();
            Integer totalQuantity = entry.getValue();

            if (totalQuantity <= 0) continue; // skip — violates asked_quantity > 0 constraint

            MilkType milkType = milkTypeRepository.findById(milkTypeId)
                    .orElseThrow(() -> new RuntimeException("MilkType not found: " + milkTypeId));

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
            throw new RuntimeException(
                    "Date is not matching. You can only save deliveries for today's date.");
        }

        // Step 1: build map of customer_id → deliveredQuantity from payload
        Map<Long, Integer> payloadQuantityMap = new HashMap<>();
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            payloadQuantityMap.put(item.getCustomerDeliveryId(), item.getDeliveredQuantity());
        }

        // Step 2: fetch ALL customer_deliveries for this delivery person on this date
        List<CustomerDelivery> existingDeliveries = deliveryRepo
                .findByDeliveryPersonIdAndDeliveryDate(deliveryPersonId, deliveryDate);

        // Step 3: aggregate total delivered quantity per milk_type_id
        // use payload quantity if provided, else existing delivered_quantity
        Map<Long, Integer> milkTypeTotalMap = new HashMap<>();
        for (CustomerDelivery cd : existingDeliveries) {
            Long milkTypeId = cd.getMilkTypeId();
            if (milkTypeId == null) continue;

            Integer qty = payloadQuantityMap.getOrDefault(
                    cd.getCustomer().getId(),
                    cd.getDeliveredQuantity() != null ? cd.getDeliveredQuantity() : 0
            );
            milkTypeTotalMap.merge(milkTypeId, qty, Integer::sum);
        }

        // Step 4: fetch milk_delivery_orders for this delivery person on this date
        List<MilkDeliveryOrder> orders = milkDeliveryOrderRepository
                .findByDeliveryPersonIdAndOrderDate(deliveryPersonId, deliveryDate);

        if (orders.isEmpty()) {
            throw new RuntimeException(
                    "No milk delivery orders found for deliveryPersonId=" + deliveryPersonId
                    + " and date=" + deliveryDate + ". Please run /submit first.");
        }

        // Step 5: validate each milk_type_id total against asked_quantity in milk_delivery_orders
        for (MilkDeliveryOrder order : orders) {
            Long milkTypeId = order.getMilkType().getId();
            String milkTypeName = order.getMilkType().getName();
            Integer totalDelivered = milkTypeTotalMap.getOrDefault(milkTypeId, 0);

            if (!totalDelivered.equals(order.getAskedQuantity())) {
                throw new RuntimeException(
                        "Quantity mismatch for milk type '" + milkTypeName
                        + "' (id=" + milkTypeId + ")."
                        + " Expected: " + order.getAskedQuantity()
                        + ", Got: " + totalDelivered
                );
            }
        }

        // Validation passed — update customer_deliveries: delivered_quantity + status
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            List<CustomerDelivery> cdList = deliveryRepo
                    .findByCustomerIdAndDeliveryPersonId(item.getCustomerDeliveryId(), deliveryPersonId);

            if (cdList.isEmpty()) continue;

            CustomerDelivery cd = cdList.get(0);
            cd.setDeliveredQuantity(item.getDeliveredQuantity());
            cd.setStatus("delivered");

            deliveryRepo.save(cd);
        }
    }

}