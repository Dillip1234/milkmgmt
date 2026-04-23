package com.wom.milkmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilkDeliveryOrderDetailDTO {

    private Long orderId;
    private Long deliveryPersonId;
    private String deliveryPersonName;
    private String deliveryPersonEmail;
    private String deliveryPersonPhone;
    private Long milkTypeId;
    private String milkTypeName;
    private Integer askedQuantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal totalPrice;
    private LocalDate orderDate;
    private String status;
    private LocalDateTime createdAt;
}
