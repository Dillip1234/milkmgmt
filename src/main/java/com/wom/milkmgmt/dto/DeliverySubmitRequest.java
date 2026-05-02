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
        private Long customerDeliveryId;  // customer_id in customer_deliveries
        private Integer deliveredQuantity;
        private Long milkTypeId;          // optional — if milk type changed
    }
}
