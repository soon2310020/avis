package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.data.model.WorkingDay;

import java.sql.Timestamp;

public interface WorkingDayService {

    WorkingDay findByCode(String code);

    WorkingDay findByIdIgnoreDelete(Long id);

    WorkingDay getContractWorkingDayAtTime(Contract contract, Timestamp timestamp);

    long countContractWorkingDays(WorkingDay workingDay, Integer workingDayValue, Timestamp month);
}
