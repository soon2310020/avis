package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.Branch;
import vn.com.twendie.avis.api.model.response.BranchDTO;
import vn.com.twendie.avis.data.model.Contract;

import java.sql.Timestamp;
import java.util.List;

public interface BranchService {

    Branch findByCode(String code);

    List<BranchDTO> getBranchInfos();

    Branch findByIdIgnoreDelete(Long id);

    Branch findById(Long id);

    Branch getContractBranchAtTime(Contract contract, Timestamp timestamp);
}
