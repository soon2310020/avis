package com.stg.controller;

import com.stg.config.security.auth.AccessToken;
import com.stg.service.ComboSuggestionService;
import com.stg.service.CrmAuthenticationService;
import com.stg.service.DownloadComboService;
import com.stg.service.dto.combo.ComboSuggestionDto;
import com.stg.service.dto.combo.ComboSuggestionResponse;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Validated
@RestController
@AllArgsConstructor
@Api(tags = "Bảng minh họa ngược")
public class ComboController {

	@Autowired
	private ComboSuggestionService comboSuggestionService;

	@Autowired
	private CrmAuthenticationService crmAuthenticationService;

	@Autowired
	private DownloadComboService downloadComboService;

	@PostMapping("/combo/suggestion")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<ComboSuggestionResponse> suggestCombo(@Valid @RequestBody ComboSuggestionDto dto) {
		log.info("ComboCode: " + dto.getComboCode());
		log.info("InsuranceAnnuallyAmount: " + dto.getInputAmount());
		return comboSuggestionService.suggestCombo(dto);
	}

	@GetMapping("/combo/{id}/download")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Resource> downloadPDF(@PathVariable("id") Long id) {
		return downloadComboService.downloadPDF(id, null);
	}

	@GetMapping("/combo/{id}/crm-download")
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Resource> downloadPDFByCrm(@PathVariable("id") Long id, @RequestParam String loginToken) {
		AccessToken accessToken = crmAuthenticationService.verifyToken(loginToken);
		return downloadComboService.downloadPDF(id, accessToken.getUsername());
	}

}
