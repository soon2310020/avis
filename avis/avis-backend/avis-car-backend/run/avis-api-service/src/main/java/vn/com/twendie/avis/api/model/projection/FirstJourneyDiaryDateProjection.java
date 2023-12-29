package vn.com.twendie.avis.api.model.projection;


import java.sql.Timestamp;

public interface FirstJourneyDiaryDateProjection {
    Long getContractId();
    Timestamp getFirstJourneyDiaryDate();
}
