package com.wom.milkmgmt.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomerResponseDTO {
    private Long id;
    private String customerName;
    private String mobileNumber;
    private String address;
    private Long districtId;
    private String districtName;
    private Long milkTypeId;
    private String milkTypeName;
    private Integer regularQuantity;
    private Long deliveryPersonId;
    private String deliveryPersonName;
    private String createdAt;
    private Boolean active;
}
