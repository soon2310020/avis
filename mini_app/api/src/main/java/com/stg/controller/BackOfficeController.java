package com.stg.controller;

import com.stg.common.Endpoints;
import com.stg.entity.address.AddressDistrict;
import com.stg.entity.address.AddressProvince;
import com.stg.entity.address.AddressWard;
import com.stg.errors.ApplicationException;
import com.stg.service.AddressService;
import com.stg.service.BackofficeService;
import com.stg.service.dto.OCRDecryptedDto;
import com.stg.service.dto.quotation.crm.RmExcelInfoResp;
import com.stg.service.dto.quotation.mbal.ICDataRespDto;
import com.stg.service3rd.crm.dto.resp.RmInfoResp;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import com.stg.utils.AppUtil;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

import static com.stg.common.Endpoints.BACKOFFICE;

@Validated
@RequiredArgsConstructor
@RestController
@Api(tags = "BackOffice Apis")
public class BackOfficeController {
    private final BackofficeService backofficeService;
    private final AddressService addressService;

    @GetMapping(BACKOFFICE.GET_OCCUPATIONS)
    @ResponseStatus(HttpStatus.OK)
    public List<OccupationResp> getOccupations() {
        return backofficeService.getOccupations();
    }

    @GetMapping(BACKOFFICE.SEARCH_CRM)
    @ResponseStatus(HttpStatus.OK)
    public RmInfoResp searchCrm(@RequestParam(value = "rmCode") String rmCode,
                                @RequestParam(value = "type", defaultValue = "RM") String type) {
        return backofficeService.searchCrm(rmCode, type);
    }

    @GetMapping(BACKOFFICE.SEARCH_IC)
    @ResponseStatus(HttpStatus.OK)
    public ICDataRespDto searchIc(@RequestParam(value = "icCode") String icCode,
                                  @RequestParam(value = "type", defaultValue = "IC") String type) {
        return backofficeService.searchIc(icCode, type);
    }

    /**
     * Import temporary RM-info
     * Remove when: have api
     */
    @PostMapping(BACKOFFICE.IMPORT_RM_INFO)
    @ResponseStatus(HttpStatus.OK)
    public RmExcelInfoResp readXlsxAndImportRmInfo(@RequestParam("file") MultipartFile parts) {
        String fileName = "";
        try {
            fileName = parts.getOriginalFilename();
            InputStream inputStream = parts.getInputStream();
            return backofficeService.readXlsxAndImportRmInfo(inputStream, fileName);
        } catch (Exception ex) {
            throw new ApplicationException("Error when read file " + fileName, ex);
        }
    }

    /*START::Address*/
    @GetMapping(Endpoints.ADDRESS.SEARCH_PROVINCES)
    @ResponseStatus(HttpStatus.OK)
    public List<AddressProvince> searchProvinces(@RequestParam(value = "provinceCode", required = false) String provinceCode,
                                                 @RequestParam(value = "provinceName", required = false) String provinceName) {
        return addressService.searchProvinces(provinceCode, provinceName);
    }

    @GetMapping(Endpoints.ADDRESS.SEARCH_DISTRICTS)
    @ResponseStatus(HttpStatus.OK)
    public List<AddressDistrict> searchDistricts(@RequestParam(value = "provinceCode", required = true) String provinceCode,
                                                 @RequestParam(value = "districtCode", required = false) String districtCode,
                                                 @RequestParam(value = "districtName", required = false) String districtName) {
        return addressService.searchDistricts(provinceCode, districtCode, districtName);
    }

    @GetMapping(Endpoints.ADDRESS.SEARCH_WARDS)
    @ResponseStatus(HttpStatus.OK)
    public List<AddressWard> searchWards(@RequestParam(value = "districtCode", required = true) String districtCode,
                                         @RequestParam(value = "wardCode", required = false) String wardCode,
                                         @RequestParam(value = "wardName", required = false) String wardName) {
        return addressService.searchWards(districtCode, wardCode, wardName);
    }

    /*END::Address*/

    /**
     * Get all sales by branch code
     */
    @GetMapping(BACKOFFICE.LIST_IC)
    @ResponseStatus(HttpStatus.OK)
    public List<ICDataRespDto> searchIcs() {
        String branchCode = AppUtil.getCurrentBranchCode();
        return backofficeService.searchIcByBranchCode(branchCode);
    }

    @PostMapping(BACKOFFICE.PROCESS_OCR)
    @ResponseStatus(HttpStatus.OK)
    public OCRDecryptedDto processedOcr(@RequestParam("files") MultipartFile[] files) {
        return backofficeService.processOcr(files);
    }

}
