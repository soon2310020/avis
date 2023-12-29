package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.api.model.response.ContractTypeDTO;
import vn.com.twendie.avis.data.model.ContractType;

import java.util.List;

public interface ContractTypeService {

    ContractType findById(Long id);

    List<ContractType> findAll();

}
