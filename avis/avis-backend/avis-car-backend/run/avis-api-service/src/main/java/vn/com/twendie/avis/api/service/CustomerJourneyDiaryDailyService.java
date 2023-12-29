package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.CustomerJourneyDiaryDaily;

import java.util.List;

public interface CustomerJourneyDiaryDailyService {

    List<CustomerJourneyDiaryDaily> saveAll(List<CustomerJourneyDiaryDaily> customerJourneyDiaryDailies);
}
