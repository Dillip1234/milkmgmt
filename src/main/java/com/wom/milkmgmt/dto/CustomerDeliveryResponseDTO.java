package com.wom.milkmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeliveryResponseDTO {

    private Long id;
    private String customerName;
    private String deliveryPersonName;
    private String milkTypeName;
    private Integer volumeMl;
    private Integer regularQuantity;
    private Integer deliveredQuantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal totalPrice;
    private LocalDate deliveryDate;
    private String status;

}

