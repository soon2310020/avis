package com.stg.service3rd.ocr;

import com.stg.service3rd.ocr.dto.req.OcrProcessReq;
import com.stg.service3rd.ocr.dto.resp.ProcessedOcrResp;
import com.stg.service3rd.ocr.dto.resp.VerifiedOcrResp;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface OcrApi3rdService {

    VerifiedOcrResp verify(String session, String exchangeKey, HttpHeaders headers);

    ProcessedOcrResp ocrProcess(OcrProcessReq req, MultipartFile[] files, String sessionId, HttpHeaders headers);

    OcrProcessReq generateProcessReq( List<String> checksums);
}
