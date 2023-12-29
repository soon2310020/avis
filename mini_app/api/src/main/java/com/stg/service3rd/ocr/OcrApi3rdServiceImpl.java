package com.stg.service3rd.ocr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.service3rd.common.adapter.func.IFunction;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.ocr.adapter.OcrApiCaller;
import com.stg.service3rd.ocr.adapter.OcrFunctions;
import com.stg.service3rd.ocr.dto.error.OcrErrorObject;
import com.stg.service3rd.ocr.dto.req.OcrProcessReq;
import com.stg.service3rd.ocr.dto.resp.ProcessedOcrResp;
import com.stg.service3rd.ocr.dto.resp.VerifiedOcrResp;
import com.stg.service3rd.ocr.exception.OCRApi3rdException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrApi3rdServiceImpl implements OcrApi3rdService {
    public static final String SESSION_REQUEST_ID = "session_request_id";
    private final OcrApiCaller ocrApiCaller;

    @Value("${api3rd.ocr.from_system}")
    private String fromSystem;
    @Value("${api3rd.ocr.device_id}")
    private String deviceId;
    @Value("${api3rd.ocr.business}")
    private String business;
    @Value("${api3rd.ocr.user}")
    private String user;

    @Override
    public VerifiedOcrResp verify(String session, String exchangeKey, HttpHeaders headers) {
        IFunction reqUri = OcrFunctions.VERIFY_SESSION;

        try {

            headers.setContentType(APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> formRequest = new LinkedMultiValueMap<>();
            formRequest.add(SESSION_REQUEST_ID, session);
            formRequest.add("from_system", fromSystem);
            formRequest.add("device_id", deviceId);
            formRequest.add("exchange_public_key", exchangeKey);
            return ocrApiCaller.post(reqUri, headers, formRequest, VerifiedOcrResp.class);

        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, OcrErrorObject.class);
            throw new OCRApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }

    }

    @Override
    public ProcessedOcrResp ocrProcess(OcrProcessReq req, MultipartFile[] files, String sessionId, HttpHeaders headers) {
        IFunction reqUri = OcrFunctions.OCR_PROCESS;
        try {
            //HttpHeaders headers = ocrApiCaller.getAuthHeader();
            headers.setContentType(MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> ocrReqs = new LinkedMultiValueMap<>();
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonReq = objectMapper.writeValueAsString(req);
            ocrReqs.add("metadata", jsonReq);
            for (MultipartFile file : files) {
                ocrReqs.add("file_attachments", file.getResource());
            }
            ocrReqs.add(SESSION_REQUEST_ID, sessionId);

            return ocrApiCaller.post(reqUri, headers, ocrReqs, ProcessedOcrResp.class);

        } catch (Exception e) {
            IErrorObject errorObject = ApiUtil.parseErrorMessage(e, OcrErrorObject.class);
            throw new OCRApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
        }
    }

    public OcrProcessReq generateProcessReq( List<String> checksums) {
        OcrProcessReq processReq = new OcrProcessReq();
        processReq.setFile_checksums(checksums);
        processReq.setBusiness(business);
        processReq.setSystem(fromSystem);
        processReq.setUser(user);
        return processReq;
    }
}
