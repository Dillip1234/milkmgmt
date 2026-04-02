package com.wom.milkmgmt.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MilkTypeRequestDTO {

    private String name;
    private String animal;
    private Integer volumeMl;
    private BigDecimal pricePerUnit;
}