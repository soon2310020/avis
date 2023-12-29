package com.stg.service.impl.potentialcustomer;

import static com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq.PRODUCT_DEFAULT;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.potentialcustomer.AppStatus;
import com.stg.entity.potentialcustomer.PotentialCustomer;
import com.stg.entity.potentialcustomer.PotentialCustomerRefer;
import com.stg.entity.potentialcustomer.PotentialCustomerReferState;
import com.stg.errors.ApplicationException;
import com.stg.repository.PotentialCustomerReferRepository;
import com.stg.repository.PotentialCustomerRepository;
import com.stg.service.BackofficeService;
import com.stg.service.PotentialCustomerReferService;
import com.stg.service.caching.RmInfoCaching;
import com.stg.service.caching.RmInfoCaching.RmCachingData;
import com.stg.service.dto.DeleteIdsReq;
import com.stg.service.dto.RmInfoDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferExportDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredDetailDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredHeaderDto;
import com.stg.service.dto.potentialcustomer.PotentialCustomerReferredReq;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.dto.FlexibleCommon.ReferrerInput;
import com.stg.service3rd.mbal.dto.MbalBranchDto;
import com.stg.service3rd.mbal.dto.MbalICDto;
import com.stg.service3rd.mbal.dto.MbalRMDto;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mbal.dto.req.SubmitPotentialCustomerReq;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import com.stg.utils.AppUtil;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PotentialCustomerReferServiceImpl implements PotentialCustomerReferService {

    private final PotentialCustomerReferRepository potentialCustomerReferRepository;
    private final PotentialCustomerRepository potentialCustomerRepository;

    private final BackofficeService backofficeService;
    private final MbalApi3rdService mbalApi3rdService;

    private final RmInfoCaching rmInfoCaching;

    @Override
    public Page<PotentialCustomerReferredHeaderDto> search(Pageable pageable, String query, LocalDate date,
            LocalDate from, LocalDate to, String createdBy) {
        return potentialCustomerReferRepository.search(pageable, createdBy, query, date, from, to)
                .map(PotentialCustomerReferredHeaderDto::of);
    }

    @Override
    public Page<PotentialCustomerReferredHeaderDto> searchByPotentialCustomer(Pageable pageable,
            Long potentialCustomerId) {
        PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(potentialCustomerId, null);
        return potentialCustomerReferRepository.search(pageable, null, null, null, null, null, potentialCustomer)
                .map(PotentialCustomerReferredHeaderDto::of);
    }

    @Override
    public PotentialCustomerReferredDetailDto get(Long id, String createdBy) {
        PotentialCustomerRefer refer = potentialCustomerReferRepository.getById(id, createdBy);

        PotentialCustomerReferredDetailDto dto = PotentialCustomerReferredDetailDto.of(refer);

        if (!StringUtils.hasText(createdBy)) {
            RmCachingData cachingData = rmInfoCaching.getData(refer.getRmCode());

            dto.setRmInfo(cachingData != null ? cachingData.toRmInfo() : null);

            ReferrerInput icInfo = new ReferrerInput();
            icInfo.setName(refer.getIcFullName());
            icInfo.setCode(refer.getIcCode());
            icInfo.setBranchCode(refer.getIcBranchCode());
            icInfo.setBranchName(refer.getIcBranchName());
            dto.setIcInfo(icInfo);
            
            OccupationResp occupation = backofficeService
                    .getOccupationById(dto.getPotentialCustomer().getOccupationId().longValue());
            dto.getPotentialCustomer().setOccupation(occupation.getNameVn());
        }

        return dto;
    }

    @Transactional
    @Override
    public void delete(Long id) {
        String username = AppUtil.getCurrentUsername();
        PotentialCustomerRefer refer = potentialCustomerReferRepository.getById(id, username);
        potentialCustomerReferRepository.delete(refer);
    }

    @Transactional
    @Override
    public void delete(DeleteIdsReq req) {
        String username = AppUtil.getCurrentUsername();
        List<PotentialCustomerRefer> refers = req.getIds().stream().map(id -> {
            return potentialCustomerReferRepository.getById(id, username);
        }).collect(Collectors.toList());
        potentialCustomerReferRepository.deleteAll(refers);
    }

    @Override
    @Transactional
    public PotentialCustomerReferredDetailDto referToSale(Long id, PotentialCustomerReferredReq request) {
        String username = AppUtil.getCurrentUsername();
        PotentialCustomer potentialCustomer = potentialCustomerRepository.getById(id, username);

        RmInfoDto rmInfo = backofficeService.getCurrentRmInfo();

        PotentialCustomerRefer potentialCustomerRefer = new PotentialCustomerRefer();
        potentialCustomerRefer.setPotentialCustomer(potentialCustomer);
        potentialCustomerRefer.setRmCode(AppUtil.getCurrentRmCode());
        potentialCustomerRefer.setIcCode(request.getIcCode());
        potentialCustomerRefer.setIcFullName(request.getName());
        potentialCustomerRefer.setIcPhoneNumber(request.getPhoneNumber());
        potentialCustomerRefer.setIcBranchCode(rmInfo.getBranchCode());
        potentialCustomerRefer.setIcBranchName(rmInfo.getBranchName());
        potentialCustomerRefer.setReason(request.getReason());
        potentialCustomerRefer.setState(PotentialCustomerReferState.SENT);
        potentialCustomerRefer.setAppStatus(AppStatus.DRAFT);

        potentialCustomerRefer = potentialCustomerReferRepository.save(potentialCustomerRefer);

        MbalRMDto rm = new MbalRMDto(rmInfo.getCode(), rmInfo.getFullName(), rmInfo.getPhoneNumber(), rmInfo.getEmail(),
                new MbalBranchDto(rmInfo.getBranchCode(), rmInfo.getBranchName()));

        MbalICDto ic = new MbalICDto(request.getIcCode(), request.getName(), request.getPhoneNumber(),
                new MbalBranchDto(rmInfo.getBranchCode(), rmInfo.getBranchName()));

        SubmitPotentialCustomerReq req = SubmitPotentialCustomerReq.of(potentialCustomerRefer, rm, ic, PRODUCT_DEFAULT);

        try {
            mbalApi3rdService.submitPotentialCustomer(req);
        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
            throw new ApplicationException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }

        return PotentialCustomerReferredDetailDto.of(potentialCustomerRefer);

    }

    @Override
    public Page<PotentialCustomerReferredHeaderDto> adminSearch(Pageable pageable, String query, LocalDate from,
            LocalDate to, AppStatus appStatus, String leadStatus) {
        return potentialCustomerReferRepository.search(pageable, null, query, null, from, to, appStatus, leadStatus)
                .map(r -> {
                    PotentialCustomerReferredHeaderDto dto = PotentialCustomerReferredHeaderDto.of(r);

                    RmCachingData cachingData = rmInfoCaching.getData(r.getRmCode());

                    dto.setRmInfo(cachingData != null ? cachingData.toRmInfo() : null);

                    ReferrerInput icInfo = new ReferrerInput();
                    icInfo.setName(r.getIcFullName());
                    icInfo.setCode(r.getIcCode());
                    icInfo.setBranchCode(r.getIcBranchCode());
                    icInfo.setBranchName(r.getIcBranchName());
                    dto.setIcInfo(icInfo);

                    return dto;
                });
    }

    @Override
    public void adminExport(Pageable pageable, String query, LocalDate from, LocalDate to, AppStatus appStatus,
            String leadStatus, ExportType type, HttpServletResponse response) {
        Page<PotentialCustomerReferredHeaderDto> page = adminSearch(
                PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort()), query, from, to, appStatus, leadStatus);

        try {
            List<PotentialCustomerReferExportDto> exportData = page.getContent().stream()
                    .map(PotentialCustomerReferExportDto::of).collect(Collectors.toList());
            if (ExportType.CSV.equals(type)) {
                OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Refer-List-", response);
                OpenCsvUtil.write(response.getWriter(), exportData, PotentialCustomerReferExportDto.class);
            } else {
                OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Refer-List-",
                        response);
                List<String> headers = Arrays.asList("Lead ID", "Tên KH", "Thông tin RM", "Thông tin IC", "GTTT",
                        "Trạng thái HSYCBH", "Trạng thái Lead", "Ngày tham gia", "Nguồn");
                Field[] fields = PotentialCustomerReferExportDto.class.getDeclaredFields();
                ExcelUtils.exportExcel(exportData, fields, headers, 2, "templates/refer_list.xlsx", response);
            }
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("[MINI]--Failed when export waiting payments. Detail {}", e.getMessage());
            throw new ApplicationException("[Failed when export refer");
        }
    }

}
