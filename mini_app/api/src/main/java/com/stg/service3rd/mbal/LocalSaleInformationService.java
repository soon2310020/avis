package com.stg.service3rd.mbal;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.stg.repository.sale.IcInformationRepository;
import com.stg.repository.sale.RmInformationRepository;
import com.stg.service3rd.mbal.adapter.MbalApiCaller;
import com.stg.service3rd.mbal.dto.resp.ICDataResp;
import com.stg.utils.AppUtil;

@Service
@Primary
public class LocalSaleInformationService extends MbalApi3rdServiceImpl {

    private final RmInformationRepository rmInformationRepository;
    private final IcInformationRepository icInformationRepository;

    public LocalSaleInformationService(MbalApiCaller mbalConnector, RmInformationRepository rmInformationRepository,
            IcInformationRepository icInformationRepository) {
        super(mbalConnector);
        this.icInformationRepository = icInformationRepository;
        this.rmInformationRepository = rmInformationRepository;
    }

    @Override
    public List<ICDataResp> searchIcByBranchCode(String branchCode) {
        String icCode = AppUtil.getCurrentIcCode();
        return icInformationRepository.findAllByBranchCodeAndIcCodeNot(branchCode, icCode != null ? icCode : "")
                .stream().map(ic -> {
                    ICDataResp resp = new ICDataResp();
                    resp.setBranchCode(ic.getBranchCode());
                    resp.setBranchName(ic.getBranchName());
                    resp.setCode(ic.getIcCode());
                    resp.setName(ic.getFullName());
                    resp.setPhoneNumber(ic.getPhoneNumber());
                    resp.setEmail(ic.getEmail());
                    return resp;
                }).collect(Collectors.toList());
    }

    @Override
    public ICDataResp getIcByRmCode(String rmCode) {
        return rmInformationRepository.findByRmCode(rmCode).map(rm -> {
            ICDataResp resp = new ICDataResp();
            resp.setBranchCode(rm.getBranchCode());
            resp.setBranchName(rm.getBranchName());
            resp.setCode(rm.getIcCode());
            resp.setName(rm.getFullName());
            resp.setPhoneNumber(rm.getPhoneNumber());
            return resp;
        }).orElse(null);
    }

    @Override
    public List<ICDataResp> searchIcByIcCode(String icCode) {
        return icInformationRepository.findAllByIcCode(icCode).stream().map(ic -> {
            ICDataResp resp = new ICDataResp();
            resp.setBranchCode(ic.getBranchCode());
            resp.setBranchName(ic.getBranchName());
            resp.setCode(ic.getIcCode());
            resp.setName(ic.getFullName());
            resp.setPhoneNumber(ic.getPhoneNumber());
            resp.setEmail(ic.getEmail());
            return resp;
        }).collect(Collectors.toList());
    }

}
