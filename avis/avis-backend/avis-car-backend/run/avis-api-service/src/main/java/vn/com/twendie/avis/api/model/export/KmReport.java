package vn.com.twendie.avis.api.model.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KmReport {

    private static final String DEFAULT_TRACKING_SERVICE_PROVIDER = "Bình Anh";

    private String customerName;

    private Long vehicleId;

    private String vehicleNumberPlate;

    private String vehicleType;

    private String vehicleOwner;

    private String vehicleOperationAdmin;

    private Long driverId;

    private String driverName;

    private BigDecimal totalKm;

    private BigDecimal emptyKm;

    private BigDecimal usedKm;

    @Builder.Default
    private String trackingServiceProvider = DEFAULT_TRACKING_SERVICE_PROVIDER;

}
