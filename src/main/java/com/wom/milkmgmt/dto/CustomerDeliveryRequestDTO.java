package com.wom.milkmgmt.dto;

import lombok.Data;

@Data
public class CustomerDeliveryRequestDTO {
    private Long customerId;           // all other data is derived from this + users
    private Long deliveryPersonId;
    private Integer deliveredQuantity; // filled in by delivery person
}

