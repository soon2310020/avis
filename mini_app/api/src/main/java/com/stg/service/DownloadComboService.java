package com.stg.service;

import com.stg.service.dto.quotation.QuotationSearchDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface DownloadComboService {
	ResponseEntity<Resource> downloadPDF(Long id, String username);
}
