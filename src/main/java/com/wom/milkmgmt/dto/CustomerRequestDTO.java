package com.wom.milkmgmt.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerRequestDTO {
    private String customerName;
    private String mobileNumber;
    private String address;
    private Long districtId;
    private Long milkTypeId;
    private Integer regularQuantity;
    private Long deliveryPersonId;
}

