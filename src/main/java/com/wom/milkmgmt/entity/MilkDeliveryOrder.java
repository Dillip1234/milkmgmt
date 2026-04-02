package com.wom.milkmgmt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "milk_delivery_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilkDeliveryOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delivery_person_id", nullable = false)
    private User deliveryPerson;

    @ManyToOne
    @JoinColumn(name = "milk_type_id", nullable = false)
    private MilkType milkType;

    @Column(name = "asked_quantity")
    private Integer askedQuantity;

    @Column(name = "unit_price_snapshot", precision = 10, scale = 2)
    private BigDecimal unitPriceSnapshot;


    // GENERATED column — tell JPA never to insert/update this
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "order_date")
    private LocalDate orderDate;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
