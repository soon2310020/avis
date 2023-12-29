package com.stg.service;

import com.stg.service.dto.PaginationResponse;
import com.stg.service.dto.occupation.ImportOccupationHistoryDto;
import com.stg.service.dto.occupation.OccupationDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface OccupationService {

    PaginationResponse<OccupationDTO> list(Long user);
    ResponseEntity<Map<String, String>> importList(MultipartFile parts);
    PaginationResponse<ImportOccupationHistoryDto> listHistory(Long user, int page, int size);

}
