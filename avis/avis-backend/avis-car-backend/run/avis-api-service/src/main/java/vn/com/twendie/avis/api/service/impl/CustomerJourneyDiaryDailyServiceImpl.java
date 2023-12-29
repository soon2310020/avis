package vn.com.twendie.avis.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.CustomerJourneyDiaryDailyRepo;
import vn.com.twendie.avis.api.service.CustomerJourneyDiaryDailyService;
import vn.com.twendie.avis.data.model.CustomerJourneyDiaryDaily;

import java.util.List;

@Service
public class CustomerJourneyDiaryDailyServiceImpl implements CustomerJourneyDiaryDailyService {

    private final CustomerJourneyDiaryDailyRepo customerJourneyDiaryDailyRepo;

    public CustomerJourneyDiaryDailyServiceImpl(CustomerJourneyDiaryDailyRepo customerJourneyDiaryDailyRepo) {
        this.customerJourneyDiaryDailyRepo = customerJourneyDiaryDailyRepo;
    }

    @Override
    public List<CustomerJourneyDiaryDaily> saveAll(List<CustomerJourneyDiaryDaily> customerJourneyDiaryDailies) {
        return customerJourneyDiaryDailyRepo.saveAll(customerJourneyDiaryDailies);
    }
}
