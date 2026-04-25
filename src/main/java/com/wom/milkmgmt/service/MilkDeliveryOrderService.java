package com.wom.milkmgmt.service;

import com.wom.milkmgmt.dto.BulkQuantityUpdateDTO;
import com.wom.milkmgmt.dto.DeliveryPersonDTO;
import com.wom.milkmgmt.dto.MilkDeliveryOrderDTO;
import com.wom.milkmgmt.dto.MilkDeliveryOrderDetailDTO;
import com.wom.milkmgmt.entity.MilkDeliveryOrder;
import com.wom.milkmgmt.entity.MilkType;
import com.wom.milkmgmt.entity.User;
import com.wom.milkmgmt.repository.MilkDeliveryOrderRepository;
import com.wom.milkmgmt.repository.MilkTypeRepository;
import com.wom.milkmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilkDeliveryOrderService {

    private final MilkDeliveryOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MilkTypeRepository milkTypeRepository;

    // CREATE
    public MilkDeliveryOrder create(MilkDeliveryOrderDTO dto) {
        User deliveryPerson = userRepository.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + dto.getDeliveryPersonId()));

        MilkType milkType = milkTypeRepository.findById(dto.getMilkTypeId())
                .orElseThrow(() -> new RuntimeException(
                        "MilkType not found: " + dto.getMilkTypeId()));

        MilkDeliveryOrder order = new MilkDeliveryOrder();
        order.setDeliveryPerson(deliveryPerson);
        order.setMilkType(milkType);
        order.setUnitPriceSnapshot(milkType.getPricePerUnit());
        order.setAskedQuantity(dto.getAskedQuantity());
        // snapshot price now
        order.setOrderDate(LocalDate.now());                    // today's date
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(
                milkType.getPricePerUnit()
                        .multiply(BigDecimal.valueOf(dto.getAskedQuantity()))
        );

        return orderRepository.save(order);
    }

    // READ ALL
    public List<MilkDeliveryOrder> getAll() {
        return orderRepository.findAll();
    }

    // READ ONE
    public MilkDeliveryOrder getById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    // UPDATE
    public MilkDeliveryOrder update(Long id, MilkDeliveryOrderDTO dto) {
        MilkDeliveryOrder order = getById(id);

        User deliveryPerson = userRepository.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found: " + dto.getDeliveryPersonId()));

        MilkType milkType = milkTypeRepository.findById(dto.getMilkTypeId())
                .orElseThrow(() -> new RuntimeException(
                        "MilkType not found: " + dto.getMilkTypeId()));

        order.setDeliveryPerson(deliveryPerson);
        order.setMilkType(milkType);
        order.setUnitPriceSnapshot(milkType.getPricePerUnit());
        order.setAskedQuantity(dto.getAskedQuantity());
        order.setTotalPrice(
                milkType.getPricePerUnit()
                        .multiply(BigDecimal.valueOf(dto.getAskedQuantity()))
        );

        // re-snapshot on update
        // order_date and created_at are NOT changed on update

        return orderRepository.save(order);
    }

    // DELETE
    public void delete(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
    }

    public List<MilkDeliveryOrder> getOrdersByPersonAndDate(Long deliveryPersonId, LocalDate orderDate) {
        return orderRepository.findByDeliveryPersonIdAndOrderDate(deliveryPersonId, orderDate);
    }

    // GET ALL ORDERS BY DELIVERY PERSON ID WITH OPTIONAL DATE FILTER
    public List<MilkDeliveryOrderDetailDTO> getOrdersByDeliveryPerson(Long deliveryPersonId, LocalDate orderDate) {
        List<MilkDeliveryOrder> orders = orderDate != null
                ? orderRepository.findByDeliveryPersonIdAndOrderDate(deliveryPersonId, orderDate)
                : orderRepository.findByDeliveryPersonId(deliveryPersonId);

        return orders.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    // GET ALL DELIVERY PERSONS (DISTINCT FROM ORDERS)
    public List<DeliveryPersonDTO> getAllDeliveryPersons() {
        List<MilkDeliveryOrder> allOrders = orderRepository.findAll();
        
        return allOrders.stream()
                .map(MilkDeliveryOrder::getDeliveryPerson)
                .distinct()
                .map(this::convertToDeliveryPersonDTO)
                .collect(Collectors.toList());
    }

    // Helper method to convert entity to detailed DTO
    private MilkDeliveryOrderDetailDTO convertToDetailDTO(MilkDeliveryOrder order) {
        MilkDeliveryOrderDetailDTO dto = new MilkDeliveryOrderDetailDTO();
        dto.setOrderId(order.getId());
        dto.setDeliveryPersonId(order.getDeliveryPerson().getId());
        dto.setDeliveryPersonName(order.getDeliveryPerson().getUsername());
        dto.setDeliveryPersonEmail(order.getDeliveryPerson().getEmail());
        dto.setDeliveryPersonPhone(order.getDeliveryPerson().getPhoneNumber());
        dto.setMilkTypeId(order.getMilkType().getId());
        dto.setMilkTypeName(order.getMilkType().getName());
        dto.setAskedQuantity(order.getAskedQuantity());
        dto.setUnitPriceSnapshot(order.getUnitPriceSnapshot());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        return dto;
    }

    // Helper method to convert User to DeliveryPersonDTO
    private DeliveryPersonDTO convertToDeliveryPersonDTO(User user) {
        DeliveryPersonDTO dto = new DeliveryPersonDTO();
        dto.setDeliveryPersonId(user.getId());
        dto.setDeliveryPersonName(user.getUsername());
        dto.setDeliveryPersonEmail(user.getEmail());
        dto.setDeliveryPersonPhone(user.getPhoneNumber());
        return dto;
    }

    // BULK UPDATE - update askedQuantity for multiple delivery persons
    @Transactional
    public List<MilkDeliveryOrder> bulkUpdateQuantity(List<BulkQuantityUpdateDTO> updates) {
        List<MilkDeliveryOrder> updatedOrders = new ArrayList<>();

        for (BulkQuantityUpdateDTO update : updates) {
            // find the most recent order for this deliveryPerson + milkType combination
            List<MilkDeliveryOrder> orders = orderRepository
                    .findByDeliveryPersonIdAndMilkTypeId(update.getDeliveryPersonId(), update.getMilkTypeId());

            if (orders.isEmpty()) {
                throw new RuntimeException(
                        "No order found for deliveryPersonId=" + update.getDeliveryPersonId()
                        + " and milkTypeId=" + update.getMilkTypeId());
            }

            // take the latest order (last in list)
            MilkDeliveryOrder order = orders.get(orders.size() - 1);

            order.setAskedQuantity(update.getAskedQuantity());
            order.setOrderDate(update.getOrderDate() != null ? update.getOrderDate() : order.getOrderDate());
            order.setTotalPrice(
                    order.getUnitPriceSnapshot()
                            .multiply(BigDecimal.valueOf(update.getAskedQuantity()))
            );

            updatedOrders.add(orderRepository.save(order));
        }

        return updatedOrders;
    }
}
