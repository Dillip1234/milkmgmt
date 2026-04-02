package com.wom.milkmgmt.repository;

import com.wom.milkmgmt.entity.CustomerDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CustomerDeliveryRepository extends JpaRepository<CustomerDelivery, Long> {

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

    // All deliveries between two dates
    @Query("""
            SELECT d FROM CustomerDelivery d
            WHERE d.deliveryDate BETWEEN :from AND :to
            ORDER BY d.deliveryDate DESC
            """)
    List<CustomerDelivery> findBetweenDates(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}