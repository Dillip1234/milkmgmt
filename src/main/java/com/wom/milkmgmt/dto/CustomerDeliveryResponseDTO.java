package com.wom.milkmgmt.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerDeliveryResponseDTO {
    private Long id;
    private String customerName;
    private String deliveryPersonName;
    private String milkTypeName;
    private String animal;
    private Integer volumeMl;
    private Integer askedQuantity;
    private Integer deliveredQuantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal totalPrice;
    private LocalDate deliveryDate;
    private String status;
}

