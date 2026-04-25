package com.wom.milkmgmt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkQuantityUpdateDTO {

    private Long deliveryPersonId;
    private Long milkTypeId;
    private Integer askedQuantity;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate orderDate;
}
