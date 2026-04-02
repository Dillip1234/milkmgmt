package com.wom.milkmgmt.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_deliveries")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDelivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_person_id", nullable = false)
    private User deliveryPerson;

    // Snapshots — never read from joined tables on report queries
    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "delivery_person_name")
    private String deliveryPersonName;

    @Column(name = "milk_type_name")
    private String milkTypeName;

    private String animal;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "unit_price_snapshot", precision = 8, scale = 2)
    private BigDecimal unitPriceSnapshot;

    @Column(name = "asked_quantity")
    private Integer askedQuantity;

    @Column(name = "delivered_quantity")
    private Integer deliveredQuantity = 0;

    @Column(name = "total_price", precision = 15, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "delivery_date")
    private LocalDate deliveryDate;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
