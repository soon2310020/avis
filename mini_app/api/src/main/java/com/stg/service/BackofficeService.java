package com.stg.service;

import com.stg.service.dto.OCRDecryptedDto;
import com.stg.service.dto.RmInfoDto;
import com.stg.service.dto.quotation.crm.RmExcelInfoResp;
import com.stg.service.dto.quotation.crm.RmInfoRespDto;
import com.stg.service.dto.quotation.mbal.ICDataRespDto;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BackofficeService {
    List<OccupationResp> getOccupations();
    
    OccupationResp getOccupationById(long id);

    RmInfoRespDto searchCrm(String rmCode, String type);

    ICDataRespDto searchIc(String code, String type);

    List<ICDataRespDto> searchIcByBranchCode(String branchCode);

    RmExcelInfoResp readXlsxAndImportRmInfo(InputStream inputStream, String fileName) throws IOException;

    RmInfoDto getCurrentRmInfo();

    List<ICDataRespDto> searchIcReferList(String code);

    OCRDecryptedDto processOcr(MultipartFile[] files);
}
