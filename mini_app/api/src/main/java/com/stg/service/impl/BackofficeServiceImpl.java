package com.stg.service.impl;

import com.stg.utils.ocr.OcrDecryptedDataUtil;
import com.stg.utils.ocr.ECDHUtils;
import com.stg.config.security.auth.UserCredential;
import com.stg.errors.ValidationException;
import com.stg.repository.AddressProvinceRepository;
import com.stg.service.BackofficeService;
import com.stg.service.caching.RmExcelDataCaching;
import com.stg.service.dto.OCRDecryptedDto;
import com.stg.service.dto.RmInfoDto;
import com.stg.service.dto.address.AddressDTO;
import com.stg.service.dto.quotation.crm.RmExcelInfo;
import com.stg.service.dto.quotation.crm.RmExcelInfoResp;
import com.stg.service.dto.quotation.crm.RmInfoRespDto;
import com.stg.service.dto.quotation.mbal.ICDataRespDto;
import com.stg.service3rd.crm.CrmApi3rdService;
import com.stg.service3rd.crm.dto.resp.RmInfoResp;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.dto.resp.ICDataResp;
import com.stg.service3rd.mbal.dto.resp.ICInfoResp;
import com.stg.service3rd.mbal.dto.resp.OccupationResp;
import com.stg.service3rd.ocr.OcrApi3rdService;
import com.stg.service3rd.ocr.adapter.OcrApiCaller;
import com.stg.service3rd.ocr.dto.req.OcrProcessReq;
import com.stg.service3rd.ocr.dto.resp.ProcessedOcrResp;
import com.stg.service3rd.ocr.dto.resp.VerifiedOcrResp;
import com.stg.service3rd.ocr.exception.OCRApi3rdException;
import com.stg.utils.AppUtil;
import com.stg.utils.FileUtil;
import com.stg.utils.address.AddressInfo;
import com.stg.utils.address.AddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.*;
import java.util.stream.Collectors;

import static com.stg.utils.ocr.ECDHUtils.ENCRYPTED_ARRAYS;
import static com.stg.config.redis.CacheConfiguration.RAM_CACHING;
import static com.stg.service3rd.mbal.constant.Common.generateUUIDId;

@Slf4j
@Service
@RequiredArgsConstructor
public class BackofficeServiceImpl implements BackofficeService {
    private final ModelMapper modelMapper;

    private final CrmApi3rdService crmApi3rdService;
    private final MbalApi3rdService mbalApi3rdService;
    private final RmExcelDataCaching rmExcelDataCaching;
    private final OcrApi3rdService ocrApi3rdService;
    private final OcrApiCaller ocrApiCaller;
    private final AddressProvinceRepository addressProvinceRepository;


    @Override
    public List<OccupationResp> getOccupations() {
        return mbalApi3rdService.getOccupations();
    }
    
    @Override
    @Cacheable(value = "cache:occupation", cacheManager = RAM_CACHING)
    public OccupationResp getOccupationById(long id) {
        List<OccupationResp> occupations = getOccupations();
        return occupations.stream().filter(o -> o.getId() == id).findFirst().orElse(null);
    }

    /***/
    @Override
    public RmInfoRespDto searchCrm(String rmCode, String type) {
        if (!StringUtils.hasText(rmCode)) {
            throw new ValidationException("Vui lòng nhập dữ liệu để tìm kiếm");
        }
        RmInfoResp resp = crmApi3rdService.getCrmData(rmCode);

        RmInfoRespDto respDto;
        if (resp == null) {
            respDto = new RmInfoRespDto();
        } else {
            respDto = modelMapper.map(resp, RmInfoRespDto.class);
            respDto.setMbCode(rmCode);
        }

        return respDto;
    }

    /***/
    @Override
    public ICDataRespDto searchIc(String code, String type) {
        if (!StringUtils.hasText(code)) {
            throw new ValidationException("Vui lòng nhập dữ liệu để tìm kiếm");
        }
        ICInfoResp resp = mbalApi3rdService.getIcData(code);

        ICDataRespDto respDto;
        if (resp == null) {
            respDto = new ICDataRespDto();
        } else {
            respDto = new ICDataRespDto(code, resp.getName(), null);
        }

        return respDto;
    }

    @Override
    public RmExcelInfoResp readXlsxAndImportRmInfo(InputStream inputStream, String fileName) throws IOException {
        RmExcelInfoResp resp = new RmExcelInfoResp();

        log.info("[readXlsxAndImportRmInfo] Starting read file {}", fileName);
        List<RmExcelInfo> successData = new ArrayList<>();
        Map<Integer, String> errorData = new HashMap<>();

        Workbook workbook = new XSSFWorkbook(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        rowIterator.next(); // next header

        int rowIdx = 1; // start from 1
        while (rowIterator.hasNext()) {
            rowIdx++;
            Row row = rowIterator.next();
            try {
                RmExcelInfo rmExcelInfo = new RmExcelInfo();
                // Mã Hris
                Cell cell = row.getCell(0);
                String cellValue = cell == null ? null : cell.getStringCellValue();
                if (!StringUtils.hasText(cellValue))
                    continue;
                rmExcelInfo.setMbCode(cellValue.trim());

                // Họ và tên
                cell = row.getCell(1);
                cellValue = cell == null ? null : cell.getStringCellValue();
                if (StringUtils.hasText(cellValue)) {
                    rmExcelInfo.setFullName(cellValue.trim());
                }

                // Điện thoại
                cell = row.getCell(2);
                cellValue = cell == null ? null : cell.getStringCellValue();
                if (StringUtils.hasText(cellValue)) {
                    cellValue = cellValue.trim().replaceAll("[. ]", "");
                    String[] values = cellValue.split("[-/]");
                    rmExcelInfo.setPhone(values[0].trim());
                    if (values.length > 1)
                        rmExcelInfo.setPhone2(values[1].trim());
                }

                // Email
                cell = row.getCell(3);
                cellValue = cell == null ? null : cell.getStringCellValue();
                if (StringUtils.hasText(cellValue)) {
                    rmExcelInfo.setEmail(cellValue.trim());
                }

                successData.add(rmExcelInfo); // add data
            } catch (Exception ex) {
                errorData.put(rowIdx, ex.getMessage());
                log.info("[readXlsxAndImportRmInfo] Error when read file {" + fileName + "} at row: " + rowIdx, ex);
            }
        }

        // SAVE DATA
        if (!successData.isEmpty()) {
            rmExcelDataCaching.setData(successData);
        }

        // RESPONSE
        if (errorData.isEmpty()) {
            resp.setSuccess(true);
            resp.setErrorData(null);
        } else {
            resp.setSuccess(false);
            resp.setErrorData(errorData);
        }

        log.info("[readXlsxAndImportRmInfo] Total record: {} ", rowIdx - 1);
        log.info("[readXlsxAndImportRmInfo] End read file {}", fileName);
        return resp;
    }

    @Override
    public List<ICDataRespDto> searchIcByBranchCode(String branchCode) {
        if (!StringUtils.hasText(branchCode)) {
            throw new ValidationException("Vui lòng nhập dữ liệu để tìm kiếm");
        }
        List<ICDataResp> resp = mbalApi3rdService.searchIcByBranchCode(branchCode);

        return resp.stream().map(ICDataRespDto::of).collect(Collectors.toList());
    }

    @Override
    public RmInfoDto getCurrentRmInfo() {
        UserCredential credential = AppUtil.getCurrentCredentials();
        if (credential != null) {
            return new RmInfoDto(credential.getRmCode(), credential.getFullName(), credential.getPhoneNumber(),
                    credential.getEmail(), credential.getIcCode(), credential.getBranchCode(),
                    credential.getBranchName());
        }
        return new RmInfoDto();
    }

    @Override
    public List<ICDataRespDto> searchIcReferList(String icCode) {
        if (!StringUtils.hasText(icCode)) {
            throw new ValidationException("Vui lòng nhập dữ liệu để tìm kiếm");
        }
        List<ICDataResp> resp = mbalApi3rdService.searchIcByIcCode(icCode);

        return resp.stream().map(ICDataRespDto::of).collect(Collectors.toList());
    }


    @Override
    public OCRDecryptedDto processOcr(MultipartFile[] files) {
        //Check file type
        for (MultipartFile file : files){
            if (!FileUtil.OCR_ALLOWED_IMAGE_TYPES.contains(FileUtil.getRealMimeType(file))){
                throw new OCRApi3rdException("File type is wrong, only accept file type is png, jpg, jpeg");
            }
            if (!FileUtil.OCR_ALLOWED_IMAGE_EXTENSIONS.contains(FileUtil.getFileExtension(file))){
                throw new OCRApi3rdException("File extension is wrong, only accept file extension is png, jpg, jpeg");
            }
        }

        try {
            //generate token and get md5checksum of those files
            String session = generateUUIDId(20);
            List<String> calculatedMD5Checksums = ECDHUtils.calculateMD5Checksums(files);

            //Get authenticated headers
            HttpHeaders headers = ocrApiCaller.getAuthHeader();

            //Generate public and private key
            KeyPair keyPair = ECDHUtils.generateECKeyPair();
            byte[] publicKeyByte = Arrays.copyOfRange(keyPair.getPublic().getEncoded(), 23, keyPair.getPublic().getEncoded().length);
            String publicKey = Base64.getEncoder().encodeToString(publicKeyByte);
            log.debug("The public key used to send to the OCR system : {}", publicKey);

            //invoke verify session api of ocr system
            VerifiedOcrResp verifiedOcrRes = ocrApi3rdService.verify(session, publicKey, headers);
            log.info("OCR Verify session response : {}", verifiedOcrRes);
            byte[] sharedKey = ECDHUtils.generateSharedSecret(keyPair.getPrivate(), verifiedOcrRes.getData().getResult().getOcrPublicKeyExchange());
            log.debug("Calculated share key for ocr system : {}", Base64.getEncoder().encodeToString(sharedKey));

            //Initialize and encrypt process ocr request
            OcrProcessReq processReq = ocrApi3rdService.generateProcessReq(calculatedMD5Checksums);
            log.info("Ocr process request before encrypting : {}", processReq);
            //add list of fields need encrypt
            ECDHUtils.encryptFieldsInObject(processReq, sharedKey, ENCRYPTED_ARRAYS);
            log.debug("Ocr process request encrypted : {}", processReq);

            //invoke ocr processes api
            ProcessedOcrResp processedOcrResp = ocrApi3rdService.ocrProcess(processReq, files, verifiedOcrRes.getData().getResult().getSessionRequestId(), headers);
            if (processedOcrResp.getData() == null || CollectionUtils.isEmpty(processedOcrResp.getData().getResult().getDocs())) {
                throw new OCRApi3rdException("Wrong processed OCR Response");
            }
            List<ProcessedOcrResp.Doc> docs = processedOcrResp.getData().getResult().getDocs();
            log.debug("Ocr process response before decrypting : {}", processedOcrResp);

            //Decrypt response data of ocr processes api
            OCRDecryptedDto ocrDecryptedDto = OcrDecryptedDataUtil.decryptData(docs.get(0), sharedKey);

            //Address segregation
            String address = ocrDecryptedDto.getAddress();
            if (StringUtils.hasText(address)) {
                AddressInfo addressInfo = AddressUtil.splitOCRAddress(address);
                String subAddressUnknown = addressInfo.getSubAddressUnknown() == null ? "" : addressInfo.getSubAddressUnknown().toUpperCase();
                String district = addressInfo.getDistrictName() == null ? subAddressUnknown : addressInfo.getDistrictName().toUpperCase();
                String province = addressInfo.getProvinceName() == null ? subAddressUnknown : addressInfo.getProvinceName().toUpperCase();
                String ward = addressInfo.getWardName() == null ? subAddressUnknown : addressInfo.getWardName().toUpperCase();

                Optional<AddressDTO> addressProvince = addressProvinceRepository.findByName(district, province, ward);
                addressProvince.ifPresent(v -> {
                    ocrDecryptedDto.setProvinceName(v.getProvinceName());
                    ocrDecryptedDto.setDistrictName(v.getDistrictName());
                    ocrDecryptedDto.setWardName(v.getWardName());
                    ocrDecryptedDto.setLine1(addressInfo.getStreetName());
                });
            }

            log.info("Ocr process response decrypted : {}", ocrDecryptedDto);
            return ocrDecryptedDto;
        } catch (Exception e) {
            log.error("OCRDecryptedResp processOcr error!", e);
            throw new OCRApi3rdException(e.getMessage(), e);
        }
    }
}
