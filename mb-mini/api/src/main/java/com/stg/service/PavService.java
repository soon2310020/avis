package com.stg.service;

import com.stg.entity.PavImport;
import com.stg.service.dto.external.requestV2.MbalIllustrationBoardV2ReqDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface PavService {

    ResponseEntity<Map<String, String>> importPav(MultipartFile parts);

    List<PavImport> filterList(MbalIllustrationBoardV2ReqDto illustrationBoardDto);
}
