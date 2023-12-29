package com.stg.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;

import com.stg.constant.ComboCode;
import com.stg.service.dto.combo.ComboSuggestionDto;
import com.stg.service.dto.combo.ComboSuggestionResponse;
import com.stg.service.dto.combo.UserComboSuggestionResp;

public interface ComboSuggestionService {

    ResponseEntity<ComboSuggestionResponse> suggestCombo(ComboSuggestionDto dto);

	ResponseEntity<ComboSuggestionResponse> suggestCombo(ComboSuggestionDto dto, boolean isDownload);

    List<UserComboSuggestionResp> suggestCombos(BigDecimal inputAmount, LocalDate dob, List<ComboCode> comboCodes);

}
