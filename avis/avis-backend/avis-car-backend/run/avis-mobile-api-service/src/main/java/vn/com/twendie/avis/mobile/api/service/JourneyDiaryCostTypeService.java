package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.data.model.JourneyDiaryCostType;

import java.math.BigDecimal;

public interface JourneyDiaryCostTypeService {

    JourneyDiaryCostType findById(long id);

    JourneyDiaryCostType save(JourneyDiaryCostType journeyDiaryCostType);

    BigDecimal getTotalCostByJourneyDiaryId(Long journeyDiaryId);

}
