package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.ContractPeriodType;

public interface ContractPeriodTypeService {

    ContractPeriodType findByIdIgnoreDelete(Long id);
}
