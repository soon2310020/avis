package com.stg.controller;

import com.stg.config.auth.impl.CustomUserDetails;
import com.stg.entity.PackagePhoto;
import com.stg.entity.address.AddressDistrict;
import com.stg.entity.address.AddressProvince;
import com.stg.entity.address.AddressWard;
import com.stg.entity.user.CustomerIdentifier;
import com.stg.service.AddressService;
import com.stg.errors.ApplicationException;
import com.stg.service.BaasService;
import com.stg.service.BackOfficeService;
import com.stg.service.card.CardInstallmentService;
import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.baas.BaasPayOnBehalfManualReqDto;
import com.stg.service.dto.baas.BaasPayOnBehalfRespDto;
import com.stg.service.dto.baas.ManualRequest;
import com.stg.service.dto.baas.InstallmentManualResp;
import com.stg.service.dto.crm.RmExcelInfoResp;
import com.stg.service.dto.baas.PaymentCallbackManualResp;
import com.stg.service.dto.external.RmInfoResp;
import com.stg.service.dto.insurance.*;
import com.stg.service.lock.PaymentLockService;
import com.stg.utils.Constants;
import com.stg.utils.Endpoints;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static com.stg.utils.Constants.MIC;


@RestController
@Slf4j
@Validated
@AllArgsConstructor
@Tag(name = "BackOffice Apis")
public class BackOfficeController {
    private final BackOfficeService backOfficeService;
    private final AddressService addressService;
    private final BaasService baasService;
    private final CardInstallmentService installmentService;
    private final PaymentLockService paymentLockService;

    @PostMapping(Endpoints.URL_PACKAGE_PHOTO)
    @ResponseStatus(HttpStatus.OK)
    public List<PackagePhoto> createPackagePhoto(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody List<PackagePhotoDto> photoDtos) {
        return backOfficeService.createPackagePhoto(Long.valueOf(user.getUserId()), photoDtos);
    }

    @PostMapping(Endpoints.URL_PAY_ON_BEHALF_MANUAL)
    @ResponseStatus(HttpStatus.OK)
    public BaasPayOnBehalfRespDto manualPayOnBehalf(@AuthenticationPrincipal CustomUserDetails user,
                                                    @Valid @RequestBody BaasPayOnBehalfManualReqDto reqDto) {
        baasService.checkPermission(Long.valueOf(user.getUserId()));

        String paymentTransId = reqDto.getMbTransactionId();
        return paymentLockService.lockPayOnBehalf(paymentTransId, () -> {
            try {
                if (reqDto.getType().equals(MIC)) {
                    log.info("[MINI]--Admin với ID {} thực hiện chi hộ manual cho MIC với giao dịch {}", user.getUserId(), paymentTransId);
                    return baasService.micManualPayOnBehalf(paymentTransId);
                }
                log.info("[MINI]--Admin với ID {} thực hiện chi hộ manual cho MBAL với giao dịch {}", user.getUserId(), paymentTransId);
                return baasService.mbalManualPayOnBehalf(paymentTransId);
            } catch (Exception ex) {
                log.error("[MANUAL-PAY_ON_BEHALF][ERROR] transactionId=" + paymentTransId, ex);
                return new BaasPayOnBehalfRespDto().setStatus(Constants.Status.FAIL).setMessage(ex.getMessage());
            }
        });
    }

//    @PostMapping(Endpoints.URL_IMPORT_IC)
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<Map<String, String>> importIC(@RequestParam("file") MultipartFile parts) {
//        return backOfficeService.importIC(parts);
//    }
//
//    @GetMapping(Endpoints.URL_LIST_IC)
//    @ResponseStatus(HttpStatus.OK)
//    public PaginationResponse<ICImportDto> filterList(@AuthenticationPrincipal CustomUserDetails user,
//                                                      @RequestParam(name = "page", required = false, defaultValue = "1") int page,
//                                                      @RequestParam(name = "size", required = false, defaultValue = "10") int size,
//                                                      @RequestParam(name = "query", required = false, defaultValue = "") String query) {
//        return backOfficeService.list(Long.valueOf(user.getUserId()), page, size, query);
//    }
//
//    @GetMapping(Endpoints.URL_LIST_IC_HISTORY)
//    @ResponseStatus(HttpStatus.OK)
//    public PaginationResponse<ImportICHistoryDto> listHistory(@AuthenticationPrincipal CustomUserDetails user,
//                                                              @RequestParam(name = "page", required = false, defaultValue = "1") int page,
//                                                              @RequestParam(name = "size", required = false, defaultValue = "10") int size) {
//        return backOfficeService.listHistory(Long.valueOf(user.getUserId()), page, size);
//    }

    @GetMapping(Endpoints.EXTERNAL_GET_IC)
    @ResponseStatus(HttpStatus.OK)
    public ICImportDto getIcInformation(@AuthenticationPrincipal CustomerIdentifier identifier,
                                        @RequestParam(value = "icCode") String icCode,
                                        @RequestParam(value = "processId") String processId,
                                        @RequestParam(value = "type", defaultValue = "IC") String type) {
        return backOfficeService.getIcInformation(identifier.getMbId(), icCode, processId, type);
    }

    @GetMapping(Endpoints.EXTERNAL_CRM_GET_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public RmInfoResp getCrmValue(@AuthenticationPrincipal CustomerIdentifier identifier,
                                  @RequestParam(value = "rmCode") String rmCode,
                                  @RequestParam(value = "processId") String processId,
                                  @RequestParam(value = "type", defaultValue = "RM") String type) {
        return backOfficeService.getCrmData(identifier.getMbId(), processId, rmCode, type);
    }

    /**
     * Import temporary RM-info
     * Remove when: have api
     */
    @PostMapping(Endpoints.BACKOFFICE.IMPORT_RM_INFO)
    @ResponseStatus(HttpStatus.OK)
    public RmExcelInfoResp readXlsxAndImportRmInfo(@RequestParam("file") MultipartFile parts) {
        String fileName = "";
        try {
            fileName = parts.getOriginalFilename();
            InputStream inputStream = parts.getInputStream();
            return backOfficeService.readXlsxAndImportRmInfo(inputStream, fileName);
        } catch (Exception ex) {
            throw new ApplicationException("Error when read file " + fileName, ex);
        }
    }

    @PostMapping(Endpoints.URL_CREATE_INSTALLMENT_MANUAL)
    @ResponseStatus(HttpStatus.OK)
    public InstallmentManualResp manualInstallment(@AuthenticationPrincipal CustomUserDetails user,
                                                   @Valid @RequestBody ManualRequest reqDto) {

        baasService.checkPermission(Long.valueOf(user.getUserId()));

        return installmentService.createInstallmentNoOtpManual(reqDto);
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

    @PostMapping(Endpoints.URL_CALLBACK_MANUAL)
    @ResponseStatus(HttpStatus.OK)
    public PaymentCallbackManualResp manualCallbackTransaction(@AuthenticationPrincipal CustomUserDetails user,
                                                               @Valid @RequestBody ManualRequest reqDto) {
        baasService.checkPermission(Long.valueOf(user.getUserId()));
        return backOfficeService.manualCallbackTransaction(reqDto);
    }

    @PostMapping(Endpoints.URL_CHECK_TRANSACTION)
    @ResponseStatus(HttpStatus.OK)
    public PaymentDetailDto checkTransaction(@AuthenticationPrincipal CustomUserDetails user,
                                             @Valid @RequestBody ManualRequest reqDto) {
        baasService.checkPermission(Long.valueOf(user.getUserId()));
        return backOfficeService.checkTransaction(reqDto);
    }

}
