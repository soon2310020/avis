package vn.com.twendie.avis.api.model.projection;

import java.sql.Timestamp;

public interface LogContractCostProjection {

    Long getCostTypeId();
    Timestamp getFromDate();
    Long getPrice();
    Timestamp getCreatedAt();
    String getUserName();
    String getCostTypeName();
}
