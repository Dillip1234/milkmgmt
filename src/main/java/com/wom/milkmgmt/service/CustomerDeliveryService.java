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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("Generating report for deliveryPersonId: {}, from: {}, to: {}", deliveryPersonId, fromDate, toDate);
        return deliveryRepo.findReportByDeliveryPersonAndDateRange(deliveryPersonId, fromDate, toDate);
    }

    public String getDeliveryPersonName(Long deliveryPersonId) {
        return userRepo.findById(deliveryPersonId)
                .map(u -> u.getUsername())
                .orElse("delivery_person_" + deliveryPersonId);
    }

    public byte[] downloadReportAsExcel(Long deliveryPersonId, LocalDate fromDate, LocalDate toDate) {
        log.info("Generating Excel report for deliveryPersonId: {}, from: {}, to: {}", deliveryPersonId, fromDate, toDate);

        List<CustomerDeliveryResponseDTO> data = deliveryRepo
                .findReportByDeliveryPersonAndDateRange(deliveryPersonId, fromDate, toDate);

        log.info("Excel report data size: {} rows", data.size());

        String deliveryPersonName = userRepo.findById(deliveryPersonId)
                .map(u -> u.getUsername())
                .orElse("Delivery Person " + deliveryPersonId);

        try (org.apache.poi.xssf.usermodel.XSSFWorkbook wb =
                     new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.xssf.usermodel.XSSFSheet sheet = wb.createSheet("Delivery Report");

            // ── Collect all distinct dates in range ──
            java.util.List<LocalDate> dates = new java.util.ArrayList<>();
            LocalDate d = fromDate;
            while (!d.isAfter(toDate)) {
                dates.add(d);
                d = d.plusDays(1);
            }

            // ── Collect distinct customers (name + milkType) ──
            // key = customerName + "|" + milkTypeName, value = map of date → deliveredQty
            java.util.LinkedHashMap<String, java.util.Map<LocalDate, Integer>> customerDateMap = new java.util.LinkedHashMap<>();
            java.util.Map<String, String> customerMilkTypeMap = new java.util.LinkedHashMap<>();
            java.util.Map<String, java.math.BigDecimal> customerUnitPriceMap = new java.util.LinkedHashMap<>();

            for (CustomerDeliveryResponseDTO dto : data) {
                String key = dto.getCustomerName() + "|" + dto.getMilkTypeName();
                customerMilkTypeMap.put(key, dto.getMilkTypeName());
                customerUnitPriceMap.put(key, dto.getUnitPriceSnapshot() != null ? dto.getUnitPriceSnapshot() : java.math.BigDecimal.ZERO);
                customerDateMap.computeIfAbsent(key, k -> new java.util.HashMap<>())
                        .put(dto.getDeliveryDate(), dto.getDeliveredQuantity() != null ? dto.getDeliveredQuantity() : 0);
            }

            // ── Styles ──
            // Green style for "Report"
            org.apache.poi.xssf.usermodel.XSSFCellStyle greenStyle = wb.createCellStyle();
            greenStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)0, (byte)128, (byte)0}, null));
            greenStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.xssf.usermodel.XSSFFont boldWhite = wb.createFont();
            boldWhite.setBold(true);
            boldWhite.setColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)255,(byte)255,(byte)255}, null));
            greenStyle.setFont(boldWhite);

            // Orange style
            org.apache.poi.xssf.usermodel.XSSFCellStyle orangeStyle = wb.createCellStyle();
            orangeStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)210,(byte)105,(byte)30}, null));
            orangeStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.xssf.usermodel.XSSFFont boldFont = wb.createFont();
            boldFont.setBold(true);
            orangeStyle.setFont(boldFont);

            // Yellow style for Month
            org.apache.poi.xssf.usermodel.XSSFCellStyle yellowStyle = wb.createCellStyle();
            yellowStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)255,(byte)255,(byte)0}, null));
            yellowStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

            // Orange header style (row 6)
            org.apache.poi.xssf.usermodel.XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)210,(byte)105,(byte)30}, null));
            headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.xssf.usermodel.XSSFFont headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Total style (orange-red)
            org.apache.poi.xssf.usermodel.XSSFCellStyle totalStyle = wb.createCellStyle();
            totalStyle.setFillForegroundColor(new org.apache.poi.xssf.usermodel.XSSFColor(new byte[]{(byte)255,(byte)140,(byte)0}, null));
            totalStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.xssf.usermodel.XSSFFont totalFont = wb.createFont();
            totalFont.setBold(true);
            totalStyle.setFont(totalFont);

            // ── Row 1: Report ──
            org.apache.poi.xssf.usermodel.XSSFRow row1 = sheet.createRow(0);
            org.apache.poi.xssf.usermodel.XSSFCell reportCell = row1.createCell(0);
            reportCell.setCellValue("Report");
            reportCell.setCellStyle(greenStyle);

            // ── Row 2: Start Date ──
            org.apache.poi.xssf.usermodel.XSSFRow row2 = sheet.createRow(1);
            org.apache.poi.xssf.usermodel.XSSFCell sdLabel = row2.createCell(1);
            sdLabel.setCellValue("Start Date");
            sdLabel.setCellStyle(orangeStyle);
            row2.createCell(2).setCellValue(fromDate.toString());
            row2.createCell(3).setCellValue(toDate.toString());

            // ── Row 3: Delivery Person ──
            org.apache.poi.xssf.usermodel.XSSFRow row3 = sheet.createRow(2);
            org.apache.poi.xssf.usermodel.XSSFCell dpLabel = row3.createCell(1);
            dpLabel.setCellValue("Delivery Person");
            dpLabel.setCellStyle(orangeStyle);
            row3.createCell(2).setCellValue(deliveryPersonName);

            // ── Row 4: Month ──
            org.apache.poi.xssf.usermodel.XSSFRow row4 = sheet.createRow(3);
            org.apache.poi.xssf.usermodel.XSSFCell monthLabel = row4.createCell(1);
            monthLabel.setCellValue("Month");
            monthLabel.setCellStyle(orangeStyle);
            org.apache.poi.xssf.usermodel.XSSFCell monthValue = row4.createCell(2);
            monthValue.setCellValue(fromDate.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH));
            monthValue.setCellStyle(yellowStyle);

            // ── Row 5: empty ──
            sheet.createRow(4);

            // ── Row 6: Header — Customer Name | Milk Type | date1 | date2 | ... | Total ──
            org.apache.poi.xssf.usermodel.XSSFRow headerRow = sheet.createRow(5);
            org.apache.poi.xssf.usermodel.XSSFCell cnHeader = headerRow.createCell(1);
            cnHeader.setCellValue("Customer Name");
            cnHeader.setCellStyle(headerStyle);
            org.apache.poi.xssf.usermodel.XSSFCell mtHeader = headerRow.createCell(2);
            mtHeader.setCellValue("Milk Type");
            mtHeader.setCellStyle(headerStyle);

            int colOffset = 3;
            for (int i = 0; i < dates.size(); i++) {
                org.apache.poi.xssf.usermodel.XSSFCell dateCell = headerRow.createCell(colOffset + i);
                dateCell.setCellValue(dates.get(i).getDayOfMonth()); // show day number
                dateCell.setCellStyle(headerStyle);
            }
            org.apache.poi.xssf.usermodel.XSSFCell totalHeader = headerRow.createCell(colOffset + dates.size());
            totalHeader.setCellValue("Total Qty");
            totalHeader.setCellStyle(totalStyle);
            org.apache.poi.xssf.usermodel.XSSFCell totalPriceHeader = headerRow.createCell(colOffset + dates.size() + 1);
            totalPriceHeader.setCellValue("Total Price");
            totalPriceHeader.setCellStyle(totalStyle);

            // ── Data rows ──
            int rowNum = 6;
            for (java.util.Map.Entry<String, java.util.Map<LocalDate, Integer>> entry : customerDateMap.entrySet()) {
                String key = entry.getKey();
                String[] parts = key.split("\\|", 2);
                String customerName = parts[0];
                String milkTypeName = parts.length > 1 ? parts[1] : "";
                java.util.Map<LocalDate, Integer> dateQtyMap = entry.getValue();
                java.math.BigDecimal unitPrice = customerUnitPriceMap.getOrDefault(key, java.math.BigDecimal.ZERO);

                org.apache.poi.xssf.usermodel.XSSFRow dataRow = sheet.createRow(rowNum++);
                dataRow.createCell(1).setCellValue(customerName);
                dataRow.createCell(2).setCellValue(milkTypeName);

                int total = 0;
                for (int i = 0; i < dates.size(); i++) {
                    int qty = dateQtyMap.getOrDefault(dates.get(i), 0);
                    dataRow.createCell(colOffset + i).setCellValue(qty);
                    total += qty;
                }
                org.apache.poi.xssf.usermodel.XSSFCell totalCell = dataRow.createCell(colOffset + dates.size());
                totalCell.setCellValue(total);
                totalCell.setCellStyle(totalStyle);

                // Total Price = total qty * unit price from milk_types
                java.math.BigDecimal totalPrice = unitPrice.multiply(java.math.BigDecimal.valueOf(total));
                org.apache.poi.xssf.usermodel.XSSFCell totalPriceCell = dataRow.createCell(colOffset + dates.size() + 1);
                totalPriceCell.setCellValue(totalPrice.doubleValue());
                totalPriceCell.setCellStyle(totalStyle);
            }

            // ── Auto-size columns ──
            int totalCols = colOffset + dates.size() + 2; // +2 for Total Qty and Total Price
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
            }

            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating Excel report: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel report: " + e.getMessage());
        }
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

            // allow 0 quantity — DB constraint has been updated to >= 0

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

        // Step 1: build map of customer_id → item (for quantity + optional milkTypeId override)
        Map<Long, DeliverySubmitRequest.CustomerDeliveryItem> payloadMap = new HashMap<>();
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            payloadMap.put(item.getCustomerDeliveryId(), item);
        }

        // Step 2: fetch ALL customer_deliveries for this delivery person on this date
        List<CustomerDelivery> existingDeliveries = deliveryRepo
                .findByDeliveryPersonIdAndDeliveryDate(deliveryPersonId, deliveryDate);

        // Step 3: aggregate total delivered quantity per milk_type_id
        // if payload has a milkTypeId override for this customer, use that milkTypeId for aggregation
        Map<Long, Integer> milkTypeTotalMap = new HashMap<>();
        for (CustomerDelivery cd : existingDeliveries) {
            Long customerId = cd.getCustomer().getId();
            DeliverySubmitRequest.CustomerDeliveryItem payloadItem = payloadMap.get(customerId);

            // determine effective milkTypeId — use payload override if provided, else existing
            Long effectiveMilkTypeId = (payloadItem != null && payloadItem.getMilkTypeId() != null)
                    ? payloadItem.getMilkTypeId()
                    : cd.getMilkTypeId();

            if (effectiveMilkTypeId == null) continue;

            // determine effective quantity — use payload if provided, else existing
            Integer qty = (payloadItem != null)
                    ? payloadItem.getDeliveredQuantity()
                    : (cd.getDeliveredQuantity() != null ? cd.getDeliveredQuantity() : 0);

            milkTypeTotalMap.merge(effectiveMilkTypeId, qty, Integer::sum);
        }

        // Step 4: fetch milk_delivery_orders for this delivery person on this date
        List<MilkDeliveryOrder> orders = milkDeliveryOrderRepository
                .findByDeliveryPersonIdAndOrderDate(deliveryPersonId, deliveryDate);

        if (orders.isEmpty()) {
            throw new RuntimeException(
                    "No milk delivery orders found for deliveryPersonId=" + deliveryPersonId
                    + " and date=" + deliveryDate + ". Please run /submit first.");
        }

        // Build map of milkTypeId → asked_quantity from milk_delivery_orders
        Map<Long, Integer> ordersAskedQuantityMap = new HashMap<>();
        for (MilkDeliveryOrder order : orders) {
            ordersAskedQuantityMap.put(order.getMilkType().getId(), order.getAskedQuantity());
        }

        // Step 5: validate — payload milkTypeId + total delivered quantity must match milk_delivery_orders
        // milkTypeTotalMap already uses payload milkTypeId as priority
        for (Map.Entry<Long, Integer> entry : milkTypeTotalMap.entrySet()) {
            Long milkTypeId = entry.getKey();
            Integer totalDelivered = entry.getValue();

            // check if this milkTypeId exists in milk_delivery_orders
            if (!ordersAskedQuantityMap.containsKey(milkTypeId)) {
                MilkType mt = milkTypeRepository.findById(milkTypeId)
                        .orElseThrow(() -> new RuntimeException("MilkType not found: " + milkTypeId));
                throw new RuntimeException(
                        "Milk type '" + mt.getName() + "' (id=" + milkTypeId + ")"
                        + " is not found in milk delivery orders for date=" + deliveryDate
                        + ". Milk type mismatch.");
            }

            Integer askedQuantity = ordersAskedQuantityMap.get(milkTypeId);
            if (!totalDelivered.equals(askedQuantity)) {
                MilkType mt = milkTypeRepository.findById(milkTypeId)
                        .orElseThrow(() -> new RuntimeException("MilkType not found: " + milkTypeId));
                throw new RuntimeException(
                        "Quantity mismatch for milk type '" + mt.getName()
                        + "' (id=" + milkTypeId + ")."
                        + " Expected: " + askedQuantity
                        + ", Got: " + totalDelivered
                );
            }
        }

        // Validation passed — persist milkTypeId, milkTypeName, deliveredQuantity, status = "delivered"
        for (DeliverySubmitRequest.CustomerDeliveryItem item : request.getDeliveries()) {
            // find the row for this customer + delivery person + delivery date specifically
            List<CustomerDelivery> cdList = deliveryRepo
                    .findByCustomerIdAndDeliveryPersonIdAndDeliveryDate(
                            item.getCustomerDeliveryId(), deliveryPersonId, deliveryDate);

            if (cdList.isEmpty()) {
                log.warn("No customer_deliveries row found for customerId: {}, deliveryPersonId: {}, date: {}",
                        item.getCustomerDeliveryId(), deliveryPersonId, deliveryDate);
                continue;
            }

            CustomerDelivery cd = cdList.get(0);

            // determine effective milkType — use payload if provided, else keep existing
            Long effectiveMilkTypeId = item.getMilkTypeId() != null ? item.getMilkTypeId() : cd.getMilkTypeId();

            if (effectiveMilkTypeId != null) {
                MilkType milkType = milkTypeRepository.findById(effectiveMilkTypeId)
                        .orElseThrow(() -> new RuntimeException("MilkType not found: " + effectiveMilkTypeId));
                cd.setMilkTypeId(milkType.getId());
                cd.setMilkTypeName(milkType.getName());
                cd.setVolumeMl(milkType.getVolumeMl());
                cd.setUnitPriceSnapshot(milkType.getPricePerUnit());
                cd.setTotalPrice(milkType.getPricePerUnit()
                        .multiply(BigDecimal.valueOf(item.getDeliveredQuantity())));
            }

            cd.setDeliveredQuantity(item.getDeliveredQuantity());
            cd.setStatus("delivered");

            log.info("Saving customerId: {}, milkTypeId: {}, deliveredQty: {}, status: delivered",
                    item.getCustomerDeliveryId(), effectiveMilkTypeId, item.getDeliveredQuantity());

            deliveryRepo.save(cd);
        }
    }
}