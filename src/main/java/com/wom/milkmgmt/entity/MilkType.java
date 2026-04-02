package com.wom.milkmgmt.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "milk_types")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MilkType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String animal;

    @Column(name = "volume_ml", nullable = false)
    private Integer volumeMl;

    @Column(name = "price_per_unit", nullable = false, precision = 8, scale = 2)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private Boolean active = true;
}