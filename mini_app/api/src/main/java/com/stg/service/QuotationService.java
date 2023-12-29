package com.stg.service;

import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service.dto.quotation.QuotationSearchDto;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

public interface QuotationService {

    QuotationDto createQuotation(QuotationHeaderDto reqDto);

    void saveQuotation(Long id);

    QuotationDto findQuotation(Long id);

    Page<QuotationSearchDto> search(Pageable pageable, String query, LocalDate date, LocalDate from, LocalDate to);

    void deleteQuotation(List<Long> ids);

    ResponseEntity<Resource> downloadPDF(Long id, String username);
}
