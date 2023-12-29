package com.stg.service.impl.combo;

import com.stg.common.HTMLtoPDFUtils;
import com.stg.entity.combo.UserCombo;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.UserComboRepository;
import com.stg.service.ComboSuggestionService;
import com.stg.service.DownloadComboService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class DownloadComboServiceImpl implements DownloadComboService {

	@Autowired
	private UserComboRepository userComboRepository;

	@Autowired
	private HTMLtoPDFUtils HTMLtoPdfUtils;

	public ResponseEntity<Resource> downloadPDF(Long id, String username) {

		if (username == null) {
			username = getCurrentUsername();
		}
		
		Optional<UserCombo> userComboOpt = userComboRepository.findByIdAndCreatedBy(id, username);

		try {

			if(!userComboOpt.isPresent()){
				throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Not found"));
			}

			HTMLtoPDFUtils thymeleaf2Pdf = new HTMLtoPDFUtils();
			ByteArrayResource resource = new ByteArrayResource(thymeleaf2Pdf
					.generatePdfFromHtml(userComboOpt.get().getComboCode().toString().toLowerCase(), HTMLtoPdfUtils.getAttributesFromString(userComboOpt.get().getAttributes())));

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
					.contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);

		}  catch (FileNotFoundException e) {
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()));
		} catch (IOException e) {
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()));
		}

	}

	private String getCurrentUsername() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			return (((UserDetails) principal).getUsername());
		}
		throw new ApplicationException("", new ErrorDto(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"));
	}

}
