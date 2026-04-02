package com.wom.milkmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MilkDeliveryOrderDTO {

    private Long deliveryPersonId;   // FK → users.id
    private Long milkTypeId;         // FK → milk_types.id
    private Integer askedQuantity;
    // unit_price_snapshot is fetched from milk_types automatically
    // order_date defaults to today
    // status defaults to 'pending'
}
