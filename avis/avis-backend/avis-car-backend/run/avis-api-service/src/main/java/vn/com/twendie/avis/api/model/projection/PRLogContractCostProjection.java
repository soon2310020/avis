package vn.com.twendie.avis.api.model.projection;

import java.sql.Timestamp;

public interface PRLogContractCostProjection {

    Long getLogId();
    Timestamp getFromDate();
    Timestamp getToDate();
    Long getPrice();
    String getCostCode();
}
