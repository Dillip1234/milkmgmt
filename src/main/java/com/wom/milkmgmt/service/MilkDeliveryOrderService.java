package com.wom.milkmgmt.service;

import com.wom.milkmgmt.dto.MilkDeliveryOrderDTO;
import com.wom.milkmgmt.entity.MilkDeliveryOrder;
import com.wom.milkmgmt.entity.MilkType;
import com.wom.milkmgmt.entity.User;
import com.wom.milkmgmt.repository.MilkDeliveryOrderRepository;
import com.wom.milkmgmt.repository.MilkTypeRepository;
import com.wom.milkmgmt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
}
