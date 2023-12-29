package vn.com.twendie.avis.api.model.response;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Builder
public class PaymentRequestAdditionDataWrapper {
    private String title;
    private String customerName;
    private String adminName;
    private String customerAddress;
    private Timestamp from;
    private Timestamp to;
    private String driverName;
    private String vehicleNumberPlate;
    private String vehicleModel;
    private String totalPriceInWords;
    private String nameFinds;
}
