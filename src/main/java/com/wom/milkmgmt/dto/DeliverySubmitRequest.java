package com.wom.milkmgmt.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DeliverySubmitRequest {
    private Long deliveryPersonId;
    private LocalDate deliveryDate;
    private List<CustomerDeliveryItem> deliveries;

    @Data
    public static class CustomerDeliveryItem {
        private Long customerDeliveryId;  // id in customer_deliveries
        private Integer deliveredQuantity;
    }
}
