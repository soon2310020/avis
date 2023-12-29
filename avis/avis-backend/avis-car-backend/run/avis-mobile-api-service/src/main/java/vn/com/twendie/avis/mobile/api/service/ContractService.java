package vn.com.twendie.avis.mobile.api.service;

import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.mobile.api.model.response.ContractDetailDTO;

public interface ContractService {

    Contract findById(Long id);

    GeneralPageResponse<?> findAllContractByDriverId(Long driverId, int page,
                                                     Long timestamp, String status) throws Exception;

    ContractDetailDTO getDetailByContractIdAndDriverID(Long driverId, Long contractId) throws Exception;
}
