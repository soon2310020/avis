package vn.com.twendie.avis.api.model.projection;

import java.sql.Timestamp;

public interface LogContractNormProjection {

    Long getNormListId();
    Timestamp getFromDate();
    Long getQuota();
    Timestamp getCreatedAt();
    String getUserName();
    String getNormName();
    String getUnit();
}
