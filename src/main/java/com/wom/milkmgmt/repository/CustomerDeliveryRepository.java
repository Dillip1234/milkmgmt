package com.wom.milkmgmt.repository;

import com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO;
import com.wom.milkmgmt.entity.CustomerDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerDeliveryRepository extends JpaRepository<CustomerDelivery, Long> {

    // All deliveries for a specific customer by delivery person
    List<CustomerDelivery> findByCustomerIdAndDeliveryPersonId(Long customerId, Long deliveryPersonId);

    // All deliveries for a specific customer
    List<CustomerDelivery> findByCustomerId(Long customerId);

    // All deliveries handled by a specific delivery person
    List<CustomerDelivery> findByDeliveryPersonId(Long deliveryPersonId);

    // All deliveries for a specific date
    List<CustomerDelivery> findByDeliveryDate(LocalDate deliveryDate);

    // All deliveries for a specific customer on a specific date
    List<CustomerDelivery> findByCustomerIdAndDeliveryDate(Long customerId, LocalDate deliveryDate);

    // All deliveries by a delivery person on a specific date
    List<CustomerDelivery> findByDeliveryPersonIdAndDeliveryDate(Long deliveryPersonId, LocalDate deliveryDate);

    // All deliveries by status (pending / delivered / partial / missed)
    List<CustomerDelivery> findByStatus(String status);

    // Today's pending deliveries for a delivery person
    List<CustomerDelivery> findByDeliveryPersonIdAndStatusAndDeliveryDate(
            Long deliveryPersonId, String status, LocalDate deliveryDate);

    // Total revenue for a date range
    @Query("""
            SELECT COALESCE(SUM(d.totalPrice), 0)
            FROM CustomerDelivery d
            WHERE d.deliveryDate BETWEEN :from AND :to
            """)
    java.math.BigDecimal sumTotalPriceBetween(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    // Filter by deliveryPersonId and/or deliveryDate
    @Query("SELECT NEW com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO(" +
            "cd.id, " +
            "cd.customerName, " +
            "cd.deliveryPerson.id, " +
            "cd.deliveryPersonName, " +
            "cd.milkTypeName, " +
            "cd.volumeMl, " +
            "cd.askedQuantity, " +
            "cd.deliveredQuantity, " +
            "cd.unitPriceSnapshot, " +
            "cd.totalPrice, " +
            "cd.deliveryDate, " +
            "cd.status) " +
            "FROM CustomerDelivery cd " +
            "WHERE (cast(:deliveryPersonName as string) IS NULL OR cd.deliveryPersonName = :deliveryPersonName) " +
            "AND (cast(:deliveryDate as localdate) IS NULL OR cd.deliveryDate = :deliveryDate)")
    List<CustomerDeliveryResponseDTO> findByFilters(
            @Param("deliveryPersonName") String deliveryPersonName,
            @Param("deliveryDate") LocalDate deliveryDate);

    @Query("SELECT NEW com.wom.milkmgmt.dto.CustomerDeliveryResponseDTO(" +
            "cd.id, " +
            "cd.customerName, " +
            "cd.deliveryPerson.id, " +
            "cd.deliveryPersonName, " +
            "cd.milkTypeName, " +
            "cd.volumeMl, " +
            "cd.askedQuantity, " +
            "cd.deliveredQuantity, " +
            "cd.unitPriceSnapshot, " +
            "cd.totalPrice, " +
            "cd.deliveryDate, " +
            "cd.status) " +
            "FROM CustomerDelivery cd " +
            "WHERE (cast(:deliveryPersonId as long) IS NULL OR cd.deliveryPerson.id = :deliveryPersonId) " +
            "AND (cast(:deliveryDate as localdate) IS NULL OR cd.deliveryDate = :deliveryDate)")
    List<CustomerDeliveryResponseDTO> findByDeliveryPersonIdAndDateFilter(
            @Param("deliveryPersonId") Long deliveryPersonId,
            @Param("deliveryDate") LocalDate deliveryDate);


}