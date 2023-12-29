package vn.com.twendie.avis.api.model.projection;

import java.math.BigDecimal;

public interface JDDVehicleInfoProjection {

    Long getId();
    String getCustomNumberPlate();
    String getNumberPlate();
    BigDecimal getCostValue();
    String getCostCode();
    String getAccountantName();
}
