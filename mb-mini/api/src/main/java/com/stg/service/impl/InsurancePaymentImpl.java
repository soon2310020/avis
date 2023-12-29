package com.stg.service.impl;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.InsuranceContract;
import com.stg.entity.InsurancePayment;
import com.stg.entity.PrimaryProduct;
import com.stg.errors.ApplicationException;
import com.stg.errors.InsurancePaymentNotFoundException;
import com.stg.errors.MiniApiException;
import com.stg.repository.InsuranceContractRepository;
import com.stg.repository.InsurancePaymentRepository;
import com.stg.repository.PrimaryProductRepository;
import com.stg.service.InsurancePaymentService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.external.ControlStateEnum;
import com.stg.service.dto.insurance.*;
import com.stg.utils.DateUtil;
import com.stg.utils.PaymentType;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.stg.utils.Common.*;
import static com.stg.utils.CommonMessageError.MSG36;
import static com.stg.utils.Constants.MAX_PAGE_SIZE;
import static com.stg.utils.Constants.PackageType.FLEXIBLE;
import static com.stg.utils.DateUtil.DATE_YMD_HMS_01;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InsurancePaymentImpl implements InsurancePaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InsurancePaymentImpl.class);

    private final InsurancePaymentRepository insurancePaymentRepository;
    private final PrimaryProductRepository primaryProductRepository;
    private final InsuranceContractRepository insuranceContractRepository;

    @Autowired
    private final ModelMapper mapper;

    @Override
    public PaginationResponse<InsurancePaymentListDTO> list(Long userId, int page, int size, String query, String dateFrom, String dateTo,
                                                            String status, String micStatus, String mbalStatus, String controlState, String category, String packageName,
                                                            String installmentStatus, String paymentType, String autoPayStatus) {
        PaginationResponse<InsurancePaymentListDTO> response = new PaginationResponse<>();
        page = page - 1;
        Pageable pageable = PageRequest.of(page, size);
        List<Boolean> mbalStatusFilter = convertStatusFilter(mbalStatus);

        List<InsurancePaymentVo> insurancePayments = insurancePaymentRepository.listInsurancePayments(query, dateFrom, dateTo,
                status, micStatus, mbalStatusFilter, controlState, category, packageName, installmentStatus, paymentType, autoPayStatus, pageable);

        // update thời gian đóng phí và định kỳ đóng phí Flexible
        Map<Long, PrimaryProduct> mapPrimaryProduct = getMapPrimaryProduct(insurancePayments);

        List<InsurancePaymentListDTO> data = new ArrayList<>();
        for (InsurancePaymentVo paymentVo : insurancePayments) {
            InsurancePaymentListDTO paymentDetailDTO = buildInsurancePaymentList(paymentVo);

            PrimaryProduct primaryProduct = mapPrimaryProduct.get(paymentDetailDTO.getInsuranceRequestId());
            if (primaryProduct != null) {
                paymentDetailDTO.setMbalFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
                paymentDetailDTO.setPeriodicFee(primaryProduct.getPaymentPeriod().getVal());
            }

            data.add(paymentDetailDTO);
        }

        long total = insurancePaymentRepository.totalPayments(query, dateFrom, dateTo, status, micStatus, mbalStatusFilter, controlState, category, packageName, installmentStatus, paymentType, autoPayStatus);

        response.setData(data);
        response.setTotalItem(total);
        response.setPageSize(size);
        response.setPage(page);
        return response;
    }

    private InsurancePaymentListDTO buildInsurancePaymentList(InsurancePaymentVo paymentVo) {
        InsurancePaymentListDTO paymentDetailDTO = mapper.map(paymentVo, InsurancePaymentListDTO.class);

        paymentDetailDTO.setPaymentType(paymentVo.isNormal() ? PaymentType.NORMAL : PaymentType.INSTALLMENT);


        // la tra gop
        if (!paymentVo.isNormal() && paymentVo.getTotalFee() != null && paymentVo.getPeriod() != null && paymentVo.getPeriodicConversionFee() != null) {
            double totalFee = Double.parseDouble(paymentVo.getTotalFee().replace(".", "").split(" VND")[0]);
            int periodFee = Integer.parseInt(paymentVo.getPeriod());
            BigDecimal monthlyPaymentFee = BigDecimal.valueOf(totalFee * (Double.parseDouble(paymentVo.getPeriodicConversionFee()) / 100) / periodFee);
            monthlyPaymentFee = monthlyPaymentFee.add(BigDecimal.valueOf(totalFee / periodFee)).setScale(2, RoundingMode.HALF_UP);
            paymentDetailDTO.setFeesPayable(monthlyPaymentFee.toString());
            paymentDetailDTO.setInstallmentErrorCode(paymentVo.getInstallmentErrorCode());
        }

        paymentDetailDTO.setManagingUnit(paymentVo.getManagingUnit());

        return paymentDetailDTO;
    }

    private InsurancePaymentDetailDTO buildInsurancePaymentDetail(List<InsurancePaymentVo> paymentVos) {
        InsurancePaymentVo insurancePaymentVo = paymentVos.get(0);
        InsurancePaymentDetailDTO paymentDetailDTO = mapper.map(insurancePaymentVo, InsurancePaymentDetailDTO.class);

        List<InsurancePaymentDetailDTO.MicInfo> listMicInfo = new ArrayList<>();
        for (InsurancePaymentVo eachPaymentVo : paymentVos) {
            InsurancePaymentDetailDTO.MicInfo micInfo = new InsurancePaymentDetailDTO.MicInfo();
            micInfo.setMicInsuranceFee(eachPaymentVo.getMicInsuranceFee());
            micInfo.setMicContractNum(eachPaymentVo.getMicContractNum());
            micInfo.setMicFtNumber(eachPaymentVo.getMicFtNumber());
            micInfo.setBaasMicStatus(eachPaymentVo.getBaasMicStatus());

            listMicInfo.add(micInfo);
        }
        paymentDetailDTO.setListMicInfo(listMicInfo);

        paymentDetailDTO.setPaymentType(insurancePaymentVo.isNormal() ? PaymentType.NORMAL : PaymentType.INSTALLMENT);

        // la tra gop
        if (!insurancePaymentVo.isNormal() && insurancePaymentVo.getTotalFee() != null && insurancePaymentVo.getPeriod() != null && insurancePaymentVo.getPeriodicConversionFee() != null) {
            double totalFee = Double.parseDouble(insurancePaymentVo.getTotalFee().replace(".", "").split(" VND")[0]);
            int periodFee = Integer.parseInt(insurancePaymentVo.getPeriod());
            BigDecimal monthlyPaymentFee = BigDecimal.valueOf(totalFee * (Double.parseDouble(insurancePaymentVo.getPeriodicConversionFee()) / 100) / periodFee);
            monthlyPaymentFee = monthlyPaymentFee.add(BigDecimal.valueOf(totalFee / periodFee)).setScale(2, RoundingMode.HALF_UP);
            paymentDetailDTO.setFeesPayable(monthlyPaymentFee.toString());
        }

        return paymentDetailDTO;
    }


    @Override
    public InsurancePaymentDetailDTO paymentDetail(Long id, Long requestId) {
        List<InsurancePaymentVo> payment = insurancePaymentRepository.detailPayment(requestId);
        if (payment == null || payment.isEmpty()) {
            throw new InsurancePaymentNotFoundException(MSG36);
        }

        InsurancePaymentDetailDTO paymentDetailDTO = buildInsurancePaymentDetail(payment);
        if (payment.get(0).getPackageType().equals(FLEXIBLE.name())) {
            PrimaryProduct primaryProduct = primaryProductRepository.findByInsuranceRequestId(payment.get(0).getInsuranceRequestId());
            paymentDetailDTO.setMbalFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
            paymentDetailDTO.setPeriodicFee(primaryProduct.getPaymentPeriod().getVal());
        }

        return paymentDetailDTO;
    }

    @Override
    public InsurancePaymentDto updateControlState(Long userId, Long paymentId, UpdateControlStateReqDto updateControlStateReqDto) {
        Optional<InsurancePayment> paymentOptional = insurancePaymentRepository.findById(paymentId);
        if (!paymentOptional.isPresent()) {
            throw new InsurancePaymentNotFoundException(MSG36);
        }

        InsurancePayment payment = paymentOptional.get();
        if (payment.getControlState().equals(ControlStateEnum.COMPLETED)) {
            throw new ApplicationException("Giao dịch thanh toán này đã ở trạng thái hoàn thành đối soát");
        }
        if (payment.getControlState().equals(ControlStateEnum.IN_PROGRESS) &&
                (updateControlStateReqDto.getControlState().equals(ControlStateEnum.WAITING) || updateControlStateReqDto.getControlState().equals(ControlStateEnum.IN_PROGRESS))) {
            throw new ApplicationException("Giao dịch thanh toán này đang tiến hành đối soát");
        }

        payment.setControlState(updateControlStateReqDto.getControlState());
        InsurancePayment paymentSaved = insurancePaymentRepository.save(payment);

        return mapper.map(paymentSaved, InsurancePaymentDto.class);
    }

    @Override
    public PaginationResponse<InsurancePaymentDto> listWaitingPayment(String query, String dateFrom, String dateTo, String status, int page, int size) {
        PaginationResponse<InsurancePaymentDto> response = new PaginationResponse<>();
        if (MAX_PAGE_SIZE < size) {
            size = MAX_PAGE_SIZE;
        }
        page = page - 1;
        Pageable pageable = PageRequest.of(page, size);
        List<InsurancePayment> insurancePayments = insurancePaymentRepository.retrievedInsurancePaymentWaiting(query, dateFrom, dateTo, status, pageable);
        long total = insurancePaymentRepository.countInsurancePaymentWaiting(query, dateFrom, dateTo, status);
        List<MicPaymentVo> retrieveMicFee = insurancePaymentRepository.retrieveMicFee(insurancePayments.stream()
                .map(InsurancePayment::getTransactionId).collect(Collectors.toList()));
        Map<String, MicPaymentVo> paymentVoMap = retrieveMicFee
                .stream()
                .collect(Collectors.toMap(MicPaymentVo::getMbTransactionId, Function.identity(), (a, b) -> a));
        List<InsurancePaymentDto> paymentListDTOS = insurancePayments.stream()
                .map(o -> mapper.map(o, InsurancePaymentDto.class))
                .collect(Collectors.toList());
        // update thời gian đóng phí và định kỳ đóng phí Flexible
        Map<Long, PrimaryProduct> mapPrimaryProduct = getMapPrimaryProductWaiting(insurancePayments);
        paymentListDTOS.forEach(o -> {
            MicPaymentVo micPaymentVo = paymentVoMap.get(o.getTransactionId());
            PrimaryProduct primaryProduct = mapPrimaryProduct.get(o.getInsuranceRequestId());
            if (primaryProduct != null) {
                o.setFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
                o.setPeriodicFee(primaryProduct.getPaymentPeriod().getVal());
            }
            String comboMicFee = micPaymentVo.getComboMicFee();
            Long flexibleMicFee = micPaymentVo.getFlexibleMicFee();
            o.setMicInsuranceFee(comboMicFee);
            if (comboMicFee == null) {
                o.setMicInsuranceFee(flexibleMicFee == null ? null : formatCurrency(BigDecimal.valueOf(flexibleMicFee)));
            }
        });
        response.setData(paymentListDTOS);
        response.setTotalItem(total);
        response.setPageSize(size);
        response.setPage(page);
        return response;
    }

    @Override
    public InsurancePaymentDto detailWaitingPayment(Long paymentId) {
        InsurancePayment insurancePayment = insurancePaymentRepository.findById(paymentId)
                .orElseThrow(() -> new InsurancePaymentNotFoundException(MSG36));
        List<MicPaymentVo> retrieveMicFee = insurancePaymentRepository.retrieveMicFee(Collections.singletonList(insurancePayment.getTransactionId()));
        PrimaryProduct primaryProduct = primaryProductRepository.findByInsuranceRequestId(insurancePayment.getInsuranceRequest().getId());
        InsurancePaymentDto paymentDto = mapper.map(insurancePayment, InsurancePaymentDto.class);
        String comboMicFee = retrieveMicFee.get(0).getComboMicFee();
        Long flexibleMicFee = retrieveMicFee.get(0).getFlexibleMicFee();
        paymentDto.setMicInsuranceFee(comboMicFee);
        if (comboMicFee == null) {
            paymentDto.setMicInsuranceFee(flexibleMicFee == null ? null : formatCurrency(BigDecimal.valueOf(flexibleMicFee)));
        }

        if(primaryProduct != null) {
            paymentDto.setFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
            paymentDto.setPeriodicFee(primaryProduct.getPaymentPeriod().getVal());
        }
        InsuranceContract insuranceContract = insuranceContractRepository.findByTransactionId(insurancePayment.getTransactionId());
        if (insuranceContract != null) {
            paymentDto.setMbFt(insuranceContract.getMbReferenceNumber());
        }
        return paymentDto;
    }

    @Override
    public void exportWaitingPayment(ExportType type, String query, String dateFrom, String dateTo, String status, HttpServletResponse response) {
        List<InsurancePayment> insurancePayments = insurancePaymentRepository.retrievedInsurancePaymentWaitingExport(query, dateFrom, dateTo, status);
        List<MicPaymentVo> retrieveMicFee = insurancePaymentRepository.retrieveMicFee(insurancePayments.stream()
                .map(InsurancePayment::getTransactionId).collect(Collectors.toList()));
        Map<String, MicPaymentVo> paymentVoMap = retrieveMicFee.stream().collect(Collectors.toMap(MicPaymentVo::getMbTransactionId, Function.identity(), (a, b) -> a));
// update thời gian đóng phí và định kỳ đóng phí Flexible
        Map<Long, PrimaryProduct> mapPrimaryProduct = getMapPrimaryProductWaiting(insurancePayments);
        insurancePayments.forEach(o -> {
            MicPaymentVo micPaymentVo = paymentVoMap.get(o.getTransactionId());
            PrimaryProduct primaryProduct = mapPrimaryProduct.get(o.getInsuranceRequest().getId());
            String comboMicFee = micPaymentVo.getComboMicFee();
            Long flexibleMicFee = micPaymentVo.getFlexibleMicFee();
            o.setMicInsuranceFee(comboMicFee);
            if (comboMicFee == null) {
                o.setMicInsuranceFee(flexibleMicFee == null ? null : formatCurrency(BigDecimal.valueOf(flexibleMicFee)));
            }
            if (primaryProduct != null) {
                o.setFeePaymentTime(primaryProduct.getPremiumTerm() + " năm");
                o.setPeriodicFee(primaryProduct.getPaymentPeriod().getVal());
            }
        });
        try {
            List<InsurancePaymentWaitingExportDto> waitingExportDtos = mapWaitingPaymentToCSV(insurancePayments);
            if (ExportType.CSV.equals(type)) {
                OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-thanh-toan-cho-csv-", response);
                OpenCsvUtil.write(response.getWriter(), waitingExportDtos, InsurancePaymentWaitingExportDto.class);
            } else {
                OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-thanh-toan-cho-excel-", response);
                List<String> headers = Arrays.asList("MB ID", "Họ và tên", "Loại giấy tờ", "Số giấy tờ", "Thời gian", "Tổng phí BH",
                        "Mã giao dịch", "Phí BH MIC", "Số HSYCBH", "Phí BH MBAL", "Trạng thái thanh toán");
                Field[] fields = InsurancePaymentWaitingExportDto.class.getDeclaredFields();
                ExcelUtils.exportExcel(waitingExportDtos, fields, headers, 2, "templates/list_waiting_insurance_payment.xlsx", response);
            }
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            log.error("[MINI]--Failed when export waiting payments. Detail {}", e.getMessage());
            throw new MiniApiException("[Failed when export waiting payments");
        }
    }

    @Override
    public void exportList(Long userId, String type, String query, String dateFrom, String dateTo,
                           String status, String micStatus, String mbalStatus, String controlState, String category, HttpServletResponse response, String packageName,
                           String installmentStatus, String paymentType, String autoPayStatus) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        List<InsurancePaymentVo> insurancePaymentVos = insurancePaymentRepository.listPaymentVosNoPaging(query, dateFrom, dateTo, status, micStatus, convertStatusFilter(mbalStatus),
                controlState, category, packageName, installmentStatus, paymentType, autoPayStatus);
        List<InsurancePaymentExportListDTO> csvDTOList = mapListToCSV(insurancePaymentVos);
        if (ExportType.CSV.name().equalsIgnoreCase(type)) {
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-thanh-toan-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, InsurancePaymentExportListDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-thanh-toan-excel-", response);
            List<String> headers = Arrays.asList("MB ID", "Họ và tên", "Thời gian", "Tổng phí BH", "Trạng thái thanh toán", "Mã giao dịch",
                    "MB FT", "MIC FT", "GCNBH", "Phí BH MIC", "Chi hộ MIC", "MBAL FT", "Số HSYCBH", "Phí BH MBAL",
                    "Chi hộ MBAL", "Thời gian đóng phí", "Trạng thái đối soát", "LLBH (RM)",
                    "Tư vấn viên (IC)", "Người hỗ trợ", "Chi nhánh (RM)", "Phòng giao dịch");
            Field[] fields = InsurancePaymentExportListDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_insurance_payment.xlsx", response);
        }
    }

    private List<Boolean> convertStatusFilter(String mbalStatus) {
        List<Boolean> mbalStatusFilter = new ArrayList<>();
        if (mbalStatus.equals("FAIL")) {
            mbalStatusFilter.add(false);
        } else if (mbalStatus.equals("SUCCESS")) {
            mbalStatusFilter.add(true);
        } else {
            mbalStatusFilter.add(false);
            mbalStatusFilter.add(true);
        }
        return mbalStatusFilter;
    }

    public List<InsurancePaymentExportListDTO> mapListToCSV(List<InsurancePaymentVo> listDtos) {
        // update thời gian đóng phí
        Map<Long, PrimaryProduct> mapPrimaryProduct = getMapPrimaryProduct(listDtos);

        List<InsurancePaymentExportListDTO> csvDTOList = new ArrayList<>();
        for (InsurancePaymentVo eachDto : listDtos) {
            InsurancePaymentExportListDTO exportDTO = new InsurancePaymentExportListDTO();
            exportDTO.mbId = eachDto.getMbId();
            exportDTO.fullName = eachDto.getFullName();
            exportDTO.paymentTime = eachDto.getPaymentTime();
            exportDTO.presenter = (eachDto.getRmCode() == null ? "" : eachDto.getRmCode()) + " - " + (eachDto.getRmName() == null ? "" : eachDto.getRmName());
            exportDTO.icStaff = (eachDto.getIcCode() == null ? "" : eachDto.getIcCode()) + " - " + (eachDto.getIcName() == null ? "" : eachDto.getIcName());
            exportDTO.totalInsuranceFee = String.valueOf(convertToLong(eachDto.getMbalInsuranceFee()) + convertToLong(eachDto.getMicInsuranceFee()));
            exportDTO.tranStatus = eachDto.getTranStatus();
            exportDTO.mbTransactionId = eachDto.getMbTransactionId();
            exportDTO.mbFt = eachDto.getMbFt();
            exportDTO.micFt = eachDto.getMicFtNumber();
            exportDTO.micContractNum = eachDto.getMicContractNum();
            exportDTO.micInsuranceFee = eachDto.getMicInsuranceFee();
            exportDTO.baasMicStatus = eachDto.getBaasMicStatus();
            exportDTO.mbalFt = eachDto.getMbalFtNumber();
            exportDTO.mbalAppNo = eachDto.getMbalAppNo();
            exportDTO.mbalInsuranceFee = eachDto.getMbalInsuranceFee();
            exportDTO.baasMbalStatus = eachDto.getBaasMbalStatus();
            exportDTO.mbalFeePaymentTime = eachDto.getMbalFeePaymentTime();
            if (eachDto.getPackageType().equals(FLEXIBLE.name()) &&
                    mapPrimaryProduct.get(eachDto.getInsuranceRequestId()) != null) {
                exportDTO.mbalFeePaymentTime = mapPrimaryProduct.get(eachDto.getInsuranceRequestId()).getPremiumTerm() + " năm";
            }

            exportDTO.controlState = eachDto.getControlState() != null ? eachDto.getControlState() : "";
            String supporter = eachDto.getSupportCode() != null ? eachDto.getSupportCode() : "";
            String supportName = eachDto.getSupportName() != null ? eachDto.getSupportName() : "";
            exportDTO.supporter = supporter  + " - " + supportName;
            String branchCode = eachDto.getBranchCode() != null ? eachDto.getBranchCode() : "";
            String branchName = eachDto.getBranchName() != null ? eachDto.getBranchName() : "";
            exportDTO.branch = branchCode + " - " + branchName;
            exportDTO.department = (eachDto.getDepartmentCode() == null ? "" : eachDto.getDepartmentCode()) + " - " + (eachDto.getDepartmentName() == null ? "" : eachDto.getDepartmentName());
            csvDTOList.add(exportDTO);
        }
        return csvDTOList;
    }

    private Map<Long, PrimaryProduct> getMapPrimaryProduct(List<InsurancePaymentVo> listDtos) {
        List<InsurancePaymentVo> paymentsFlexible = listDtos.stream().filter(vo -> vo.getPackageType().equals(FLEXIBLE.name()))
                .collect(Collectors.toList());
        List<Long> requestIds = paymentsFlexible.stream().map(InsurancePaymentVo::getInsuranceRequestId)
                .filter(Objects::nonNull).collect(Collectors.toList());
        List<PrimaryProduct> primaryProducts = primaryProductRepository.findPrimaryProductByRequestIds(requestIds);
        Map<Long, PrimaryProduct> mapPrimaryProduct = new HashMap<>();
        for (PrimaryProduct primaryProduct : primaryProducts) {
            if (primaryProduct.getInsuranceRequest() != null) {
                mapPrimaryProduct.put(primaryProduct.getInsuranceRequest().getId(), primaryProduct);
            }
        }
        return mapPrimaryProduct;
    }

    public List<InsurancePaymentWaitingExportDto> mapWaitingPaymentToCSV(List<InsurancePayment> payments) {
        List<InsurancePaymentWaitingExportDto> csvDTOList = new ArrayList<>();
        for (InsurancePayment payment : payments) {
            InsurancePaymentWaitingExportDto waitingExportDto = new InsurancePaymentWaitingExportDto();
            waitingExportDto.mbId = payment.getCustomer().getMbId();
            waitingExportDto.fullName = payment.getCustomer().fullNameOrDefaultT24();
            waitingExportDto.idCardType= payment.getCustomer().getIdCardType();
            waitingExportDto.idCardNo= payment.getCustomer().getIdentification();
            waitingExportDto.mbalInsuranceFee = payment.getMbalInsuranceFee();
            waitingExportDto.paymentTime = DateUtil.localDateTimeToString(DATE_YMD_HMS_01, payment.getPaymentTime());
            waitingExportDto.totalInsuranceFee = String.valueOf(convertToLong(payment.getMbalInsuranceFee()) + convertToLong(payment.getMicInsuranceFee()));
            waitingExportDto.mbTransactionId = payment.getTransactionId();
            waitingExportDto.micInsuranceFee = payment.getMicInsuranceFee();
            waitingExportDto.mbalAppNo = payment.getMbalAppNo();
            waitingExportDto.mbalInsuranceFee = payment.getMbalInsuranceFee();
            waitingExportDto.tranStatus = payment.getTranStatus();

            csvDTOList.add(waitingExportDto);
        }
        return csvDTOList;
    }

    private Map<Long, PrimaryProduct> getMapPrimaryProductWaiting(List<InsurancePayment> insurancePayments) {

        List<Long> requestIds = insurancePayments.stream()
                .filter(insuranceRequest -> insuranceRequest.getInsuranceRequest() != null
                        && FLEXIBLE.equals(insuranceRequest.getInsuranceRequest().getPackageType()))
                .map(o -> o.getInsuranceRequest().getId())
                .collect(Collectors.toList());
        List<PrimaryProduct> primaryProducts = primaryProductRepository.findPrimaryProductByRequestIds(requestIds);
        Map<Long, PrimaryProduct> mapPrimaryProduct = new HashMap<>();
        for (PrimaryProduct primaryProduct : primaryProducts) {
            if (primaryProduct.getInsuranceRequest() != null) {
                mapPrimaryProduct.put(primaryProduct.getInsuranceRequest().getId(), primaryProduct);
            }
        }
        return mapPrimaryProduct;
    }
}
