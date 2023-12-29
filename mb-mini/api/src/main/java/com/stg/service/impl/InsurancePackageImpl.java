package com.stg.service.impl;

import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import com.stg.entity.InsurancePackage;
import com.stg.errors.InsurancePackageNotFoundException;
import com.stg.repository.InsurancePackageRepository;
import com.stg.service.InsurancePackageService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.insurance.InsurancePackageDto;
import com.stg.service.dto.insurance.InsurancePackageListExportDTO;
import com.stg.utils.DateUtil;
import com.stg.utils.csv.OpenCsvUtil;
import com.stg.utils.excel.ExcelUtils;
import com.stg.utils.excel.ExportType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InsurancePackageImpl implements InsurancePackageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignImpl.class);
    private final InsurancePackageRepository insurancePackageRepository;

    @Autowired
    private final ModelMapper mapper;

    @Override
    public PaginationResponse<InsurancePackageDto> list(Long user, int page, int size, String query, String category, String packageName) {
        PaginationResponse<InsurancePackageDto> response = new PaginationResponse<>();
        List<InsurancePackage> insurancePackages = insurancePackageRepository.listInsurancePackage(page, size, query, category, packageName);
        long total = insurancePackageRepository.totalInsurancePackage(query, category, packageName);
        List<InsurancePackageDto> packageDtos = insurancePackages.stream().map(o -> mapper.map(o, InsurancePackageDto.class)).collect(Collectors.toList());
        response.setData(packageDtos);
        response.setTotalItem(total);
        response.setPageSize(size);
        return response;
    }

    @Override
    public InsurancePackageDto insuranceDetail(Long id, Integer packageId) {
        Optional<InsurancePackage> aPackage = insurancePackageRepository.findById(packageId);
        if (aPackage.isPresent()) {
            return mapper.map(aPackage, InsurancePackageDto.class);
        }
        throw new InsurancePackageNotFoundException("Không tồn tại gói bảo hiểm.");
    }

    @Override
    public void exportList(Long idUser, String query, String type, String category, HttpServletResponse response, String packageName) throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {
        LOGGER.info("Starting exportList InsurancePackage with user=" + idUser + ", query=" + query);

        List<InsurancePackage> insurancePackages = insurancePackageRepository.listNoPaging(query, category, packageName);
        List<InsurancePackageDto> packageDtos = insurancePackages.stream().map(o -> mapper.map(o, InsurancePackageDto.class)).collect(Collectors.toList());

        List<InsurancePackageListExportDTO> csvDTOList = mapListToCSV(packageDtos);

        if (ExportType.CSV.name().equalsIgnoreCase(type)){
            OpenCsvUtil.setResponseCSV(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-goi-bao-hiem-csv-", response);
            OpenCsvUtil.write(response.getWriter(), csvDTOList, InsurancePackageListExportDTO.class);
        } else {
            OpenCsvUtil.setResponseExcel(DateUtil.DATE_YYYYMMDD_HHMMSS, "Danh-sach-goi-bao-hiem-excel-", response);
            List<String> headers = Arrays.asList("ID", "Tên gói", "Thời gian đóng phí", "Tống phí bảo hiểm", "Tổng quyền lợi", "Đơn vị phát hành bảo hiểm");
            Field[] fields = InsurancePackageListExportDTO.class.getDeclaredFields();
            ExcelUtils.exportExcel(csvDTOList, fields, headers, 2, "templates/list_insurance_package.xlsx", response);
        }
    }

    public List<InsurancePackageListExportDTO> mapListToCSV(List<InsurancePackageDto> dtoList) {
        List<InsurancePackageListExportDTO> csvDTOList = new ArrayList<>();
        for (InsurancePackageDto detailDto : dtoList) {
            InsurancePackageListExportDTO exportDTO = new InsurancePackageListExportDTO();
            exportDTO.id = detailDto.getId();
            exportDTO.packageName = detailDto.getPackageName();
            exportDTO.mbalFeePaymentTime = detailDto.getMbalFeePaymentTime();
            exportDTO.totalFee = "".equals(detailDto.getTotalFee()) ? "MBAL + MIC" : detailDto.getTotalFee();
            exportDTO.totalBenefit = "".equals(detailDto.getTotalBenefit()) ? "Tính Theo Tuổi NĐBH" : detailDto.getTotalBenefit();
            exportDTO.issuer = detailDto.getIssuer();
            csvDTOList.add(exportDTO);
        }
        return csvDTOList;
    }
}
