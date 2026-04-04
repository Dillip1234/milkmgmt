package com.wom.milkmgmt.repository;


import com.wom.milkmgmt.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Get all customers by delivery person ID
    @Query("SELECT c FROM Customer c " +
            "JOIN FETCH c.district d " +
            "JOIN FETCH c.milkType m " +
            "JOIN FETCH c.deliveryPerson u " +
            "WHERE u.id = :deliveryPersonId AND c.active = true")
    List<Customer> findByDeliveryPersonId(@Param("deliveryPersonId") Long deliveryPersonId);

    // Check duplicate mobile (exclude self on update)
    boolean existsByMobileNumberAndIdNot(String mobileNumber, Long id);

    boolean existsByMobileNumber(String mobileNumber);
    @Query("SELECT c FROM Customer c " +
            "JOIN FETCH c.district " +
            "JOIN FETCH c.milkType " +
            "JOIN FETCH c.deliveryPerson")
    List<Customer> findAllWithDetails();
}
