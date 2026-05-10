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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilkDeliveryOrderService {

    private final MilkDeliveryOrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MilkTypeRepository milkTypeRepository;

    public MilkDeliveryOrder create(MilkDeliveryOrderDTO dto) {
        log.info("Creating order for deliveryPersonId: {}, milkTypeId: {}", dto.getDeliveryPersonId(), dto.getMilkTypeId());
        User deliveryPerson = userRepository.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getDeliveryPersonId()));
        MilkType milkType = milkTypeRepository.findById(dto.getMilkTypeId())
                .orElseThrow(() -> new RuntimeException("MilkType not found: " + dto.getMilkTypeId()));
        MilkDeliveryOrder order = new MilkDeliveryOrder();
        order.setDeliveryPerson(deliveryPerson);
        order.setMilkType(milkType);
        order.setUnitPriceSnapshot(milkType.getPricePerUnit());
        order.setAskedQuantity(dto.getAskedQuantity());
        order.setOrderDate(LocalDate.now());
        order.setStatus("pending");
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalPrice(milkType.getPricePerUnit().multiply(BigDecimal.valueOf(dto.getAskedQuantity())));
        MilkDeliveryOrder saved = orderRepository.save(order);
        log.info("Order created with id: {}", saved.getId());
        return saved;
    }

    public List<MilkDeliveryOrder> getAll() {
        log.info("Fetching all milk delivery orders");
        return orderRepository.findAll();
    }

    public MilkDeliveryOrder getById(Long id) {
        log.info("Fetching order id: {}", id);
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public MilkDeliveryOrder update(Long id, MilkDeliveryOrderDTO dto) {
        log.info("Updating order id: {}", id);
        MilkDeliveryOrder order = getById(id);
        User deliveryPerson = userRepository.findById(dto.getDeliveryPersonId())
                .orElseThrow(() -> new RuntimeException("User not found: " + dto.getDeliveryPersonId()));
        MilkType milkType = milkTypeRepository.findById(dto.getMilkTypeId())
                .orElseThrow(() -> new RuntimeException("MilkType not found: " + dto.getMilkTypeId()));
        order.setDeliveryPerson(deliveryPerson);
        order.setMilkType(milkType);
        order.setUnitPriceSnapshot(milkType.getPricePerUnit());
        order.setAskedQuantity(dto.getAskedQuantity());
        order.setTotalPrice(milkType.getPricePerUnit().multiply(BigDecimal.valueOf(dto.getAskedQuantity())));
        MilkDeliveryOrder updated = orderRepository.save(order);
        log.info("Order updated id: {}", id);
        return updated;
    }

    public void delete(Long id) {
        log.info("Deleting order id: {}", id);
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found: " + id);
        }
        orderRepository.deleteById(id);
        log.info("Order deleted id: {}", id);
    }

    @Transactional
    public List<MilkDeliveryOrder> bulkUpdateQuantity(List<BulkQuantityUpdateDTO> updates) {
        log.info("Bulk updating {} orders", updates.size());
        List<MilkDeliveryOrder> updatedOrders = new ArrayList<>();
        for (BulkQuantityUpdateDTO update : updates) {
            List<MilkDeliveryOrder> orders = orderRepository
                    .findByDeliveryPersonIdAndMilkTypeId(update.getDeliveryPersonId(), update.getMilkTypeId());
            if (orders.isEmpty()) {
                throw new RuntimeException(
                        "No order found for deliveryPersonId=" + update.getDeliveryPersonId()
                        + " and milkTypeId=" + update.getMilkTypeId());
            }
            MilkDeliveryOrder order = orders.get(0);
            order.setAskedQuantity(update.getAskedQuantity());
            order.setOrderDate(update.getOrderDate() != null ? update.getOrderDate() : order.getOrderDate());
            order.setTotalPrice(order.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(update.getAskedQuantity())));
            updatedOrders.add(orderRepository.save(order));
        }
        log.info("Bulk update completed for {} orders", updatedOrders.size());
        return updatedOrders;
    }

    public List<MilkDeliveryOrder> getOrdersByPersonAndDate(Long deliveryPersonId, LocalDate orderDate) {
        log.info("Fetching orders for deliveryPersonId: {}, date: {}", deliveryPersonId, orderDate);
        return orderRepository.findByDeliveryPersonIdAndOrderDate(deliveryPersonId, orderDate);
    }

    public List<MilkDeliveryOrderDetailDTO> getOrdersByDate(LocalDate orderDate) {
        log.info("Fetching orders by date: {}", orderDate);
        List<MilkDeliveryOrder> orders = orderDate != null
                ? orderRepository.findByOrderDate(orderDate)
                : orderRepository.findAll();
        return orders.stream().map(this::convertToDetailDTO).collect(Collectors.toList());
    }

    public List<MilkDeliveryOrderDetailDTO> getOrdersByDeliveryPerson(Long deliveryPersonId, LocalDate orderDate) {
        log.info("Fetching orders for deliveryPersonId: {}, date: {}", deliveryPersonId, orderDate);
        List<MilkDeliveryOrder> orders = orderDate != null
                ? orderRepository.findByDeliveryPersonIdAndOrderDate(deliveryPersonId, orderDate)
                : orderRepository.findByDeliveryPersonId(deliveryPersonId);
        return orders.stream().map(this::convertToDetailDTO).collect(Collectors.toList());
    }

    public List<DeliveryPersonDTO> getAllDeliveryPersons() {
        log.info("Fetching all distinct delivery persons from orders");
        return orderRepository.findAll().stream()
                .map(MilkDeliveryOrder::getDeliveryPerson)
                .distinct()
                .map(this::convertToDeliveryPersonDTO)
                .collect(Collectors.toList());
    }

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

    private DeliveryPersonDTO convertToDeliveryPersonDTO(User user) {
        DeliveryPersonDTO dto = new DeliveryPersonDTO();
        dto.setDeliveryPersonId(user.getId());
        dto.setDeliveryPersonName(user.getUsername());
        dto.setDeliveryPersonEmail(user.getEmail());
        dto.setDeliveryPersonPhone(user.getPhoneNumber());
        return dto;
    }
}
