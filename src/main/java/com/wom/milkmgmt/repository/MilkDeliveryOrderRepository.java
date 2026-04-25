package com.wom.milkmgmt.repository;

import com.wom.milkmgmt.entity.MilkDeliveryOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // find latest order by deliveryPersonId and milkTypeId — ordered by id desc
    @Query("SELECT o FROM MilkDeliveryOrder o WHERE o.deliveryPerson.id = :deliveryPersonId AND o.milkType.id = :milkTypeId ORDER BY o.id DESC")
    List<MilkDeliveryOrder> findByDeliveryPersonIdAndMilkTypeId(
            @Param("deliveryPersonId") Long deliveryPersonId,
            @Param("milkTypeId") Long milkTypeId);

    // find order by deliveryPersonId + milkTypeId + orderDate (for upsert)
    List<MilkDeliveryOrder> findByDeliveryPersonIdAndMilkTypeIdAndOrderDate(
            Long deliveryPersonId, Long milkTypeId, LocalDate orderDate);
}
