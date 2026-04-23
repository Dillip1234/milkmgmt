package com.wom.milkmgmt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryPersonDTO {

    private Long deliveryPersonId;
    private String deliveryPersonName;
    private String deliveryPersonEmail;
    private String deliveryPersonPhone;
}
