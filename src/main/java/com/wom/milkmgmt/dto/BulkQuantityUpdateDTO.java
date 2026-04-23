package com.wom.milkmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkQuantityUpdateDTO {

    private Long deliveryPersonId;
    private Long milkTypeId;
    private Integer askedQuantity;
}
