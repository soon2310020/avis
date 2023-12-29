package vn.com.twendie.avis.mobile.api.model.projection;

import java.sql.Timestamp;

public interface ContractDetail {

    Long getId();
    String getCode();
    Timestamp getFromDatetime();
    Integer getStatus();
    Timestamp getToDatetime();
    String getCustomerName();
    String getMobile();
    String getMemberName();
    String getMemberMobile();
    String getAddress();

    String getVehicleType();
    String getVehicleColor();
    Integer getVehicleNumberSeat();
    String getNumberPlate();

    String getPeriodTypeName();
    Integer getDriverContractStatus();
    Boolean getVehicleLend();
    Boolean getDriverLend();
    String getCountryCode();
    String getMemberCountryCode();
}
