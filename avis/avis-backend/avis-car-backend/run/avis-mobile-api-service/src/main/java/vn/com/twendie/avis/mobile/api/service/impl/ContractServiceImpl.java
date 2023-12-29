package vn.com.twendie.avis.mobile.api.service.impl;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.api.rest.model.GeneralPageResponse;
import vn.com.twendie.avis.data.enumtype.ContractStatusEnum;
import vn.com.twendie.avis.data.enumtype.DriverContractStatusEnum;
import vn.com.twendie.avis.data.model.Contract;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.adapter.ContractDetailDTOAdapter;
import vn.com.twendie.avis.mobile.api.constant.MobileConstant;
import vn.com.twendie.avis.mobile.api.model.projection.ContractDetail;
import vn.com.twendie.avis.mobile.api.model.response.ContractDetailDTO;
import vn.com.twendie.avis.mobile.api.repository.ContractRepo;
import vn.com.twendie.avis.mobile.api.service.ContractService;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ContractServiceImpl implements ContractService {

    private final ContractRepo contractRepo;

    private final DateUtils dateUtils;

    @Autowired
    public ContractServiceImpl(ContractRepo contractRepo, DateUtils dateUtils) {
        this.contractRepo = contractRepo;
        this.dateUtils = dateUtils;
    }

    @Override
    public Contract findById(Long id) {
        if (Objects.nonNull(id)) {
            return contractRepo.findByIdAndDeletedFalse(id)
                    .orElseThrow(() -> new NotFoundException("Not found contract with id: " + id));
        } else {
            return null;
        }
    }

    @Override
    public GeneralPageResponse<?> findAllContractByDriverId(Long driverId, int page,
                                                            Long timestamp, String status) {
        int pageOffset = page > 1 ? page - 1 : 0;
        Page<ContractDetail> contractDetails;
        Pageable pageable = PageRequest.of(pageOffset, MobileConstant.DEFAULT_CONTRACTS_PAGE_SIZE);
        if (StringUtils.isNotBlank(status)) {
            contractDetails = getContractDetailsFilterByStatus(status, driverId, pageable);
        } else {
            Timestamp filterByTime = Objects.isNull(timestamp) ? null :
                    dateUtils.startOfDay(new Timestamp(timestamp));
            contractDetails = contractRepo.findAllByDriverId(driverId,
                    filterByTime,
                    pageable);
        }

        List<ContractDetailDTO> finalResult = Lists.transform(contractDetails.getContent(),
                new ContractDetailDTOAdapter(dateUtils));

        return GeneralPageResponse.toResponse(new PageImpl<>(finalResult, pageable,
                contractDetails.getTotalElements()));
    }

    @Override
    public ContractDetailDTO getDetailByContractIdAndDriverID(Long driverId, Long contractId) {
        ContractDetail queryResult = contractRepo.findByContractId(driverId, contractId);

        if (Objects.isNull(queryResult)) {
            throw new NotFoundException("Cannot find contract with driverId: " + driverId
                    + " and contractId: " + contractId)
                    .displayMessage(Translator.toLocale("contract.not_found"));
        }

        return new ContractDetailDTOAdapter(dateUtils).apply(queryResult);
    }

    private Page<ContractDetail> getContractDetailsFilterByStatus(String status, Long driverId,
                                                                  Pageable pageable) {
        switch (status) {
            case (MobileConstant.DriverContractStatus.IN_PROGRESS): {
                return contractRepo.findInProgressContracts(driverId,
                        ContractStatusEnum.IN_PROGRESS.getCode(),
                        DriverContractStatusEnum.POSTPONE.code(),
                        pageable);
            }
//            case (MobileConstant.DriverContractStatus.UPCOMING): {
//                return contractRepo.findUpComingContracts(driverId,
//                        DateUtils.dateWithTimeZone(new Date(), DateUtils.MEDIUM_PATTERN,
//                                DateUtils.UTC_TIME_ZONE));
//            }
//            case (MobileConstant.DriverContractStatus.FINISHED): {
//                List<ContractDetail> queryResult = contractRepo.findFinishedContracts(driverId,
//                        DateUtils.dateWithTimeZone(new Date(), DateUtils.MEDIUM_PATTERN,
//                                DateUtils.UTC_TIME_ZONE));
//                return CollectionUtils.isEmpty(queryResult) ? Collections.emptyList()
//                        : getNoneCanceledOrPostPoneContracts(queryResult);
//            }
//            case (MobileConstant.DriverContractStatus.CANCELED): {
//                return contractRepo.findAllByDriverIdFilterByStatus(driverId,
//                        ContractStatusEnum.CANCELED.getCode());
//            }
//            case (MobileConstant.DriverContractStatus.POST_PONE): {
//                return contractRepo.findAllByDriverIdFilterByDriverContractStatus(driverId,
//                        DriverContractStatusEnum.POSTPONE.code(),
//                        ContractStatusEnum.CANCELED.getCode(),
//                        DateUtils.dateWithTimeZone(new Date(), DateUtils.MEDIUM_PATTERN,
//                                DateUtils.UTC_TIME_ZONE));
//            }
            default:
                return Page.empty();
        }
    }

    private List<ContractDetail> getNoneCanceledOrPostPoneContracts(List<ContractDetail> queryResult) {
        return queryResult.stream()
                .filter(contractDetail -> contractDetail.getStatus() != 5)
                .filter(c -> (c.getDriverContractStatus() == null
                        || c.getDriverContractStatus() != 0)
                        && (!c.getVehicleLend() && !c.getDriverLend()))
                .collect(Collectors.toList());
    }
}
