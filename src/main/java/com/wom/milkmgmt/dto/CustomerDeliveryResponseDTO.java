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
    private Long customerId;
    private String customerName;
    private Long deliveryPersonId;
    private String deliveryPersonName;
    private Long milkTypeId;
    private String milkTypeName;
    private Integer volumeMl;
    private Integer askedQuantity;
    private Integer regularQuantity;
    private Integer deliveredQuantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal totalPrice;
    private LocalDate deliveryDate;
    private String status;

}

