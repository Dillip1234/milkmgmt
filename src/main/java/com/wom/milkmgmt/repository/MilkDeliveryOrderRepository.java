package com.wom.milkmgmt.repository;

import com.wom.milkmgmt.entity.MilkDeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MilkDeliveryOrderRepository
        extends JpaRepository<MilkDeliveryOrder, Long> {

    // find all orders by a specific delivery person
    List<MilkDeliveryOrder> findByDeliveryPersonId(Long deliveryPersonId);

    // find all orders by status
    List<MilkDeliveryOrder> findByStatus(String status);

    List<MilkDeliveryOrder> findByDeliveryPersonIdAndOrderDate(Long deliveryPersonId, LocalDate orderDate);
}
