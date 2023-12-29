package com.stg.service;

import com.stg.entity.PackagePhoto;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.crm.RmExcelInfoResp;
import com.stg.service.dto.external.RmInfoResp;
import com.stg.service.dto.baas.ManualRequest;
import com.stg.service.dto.baas.PaymentCallbackManualResp;
import com.stg.service.dto.insurance.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface BackOfficeService {
    List<PackagePhoto> createPackagePhoto(Long userId, List<PackagePhotoDto> photoDtos);

    ResponseEntity<Map<String, String>> importIC(MultipartFile parts);

    PaginationResponse<ICImportDto> list(Long user, int page, int size, String query);

    PaginationResponse<ImportICHistoryDto> listHistory(Long user, int page, int size);

    ICImportDto getIcInformation(String cif, String icCode, String processId, String type);
    RmInfoResp getCrmData(String cif, String processId, String rmCode, String type);

    RmExcelInfoResp readXlsxAndImportRmInfo(InputStream inputStream, String fileName) throws IOException;

    PaymentCallbackManualResp manualCallbackTransaction(ManualRequest reqDto);

    PaymentDetailDto checkTransaction(ManualRequest reqDto);

}
