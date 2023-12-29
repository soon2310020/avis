package com.stg.service.impl.combo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.repository.MicFeeRepository;
import com.stg.repository.UserComboRepository;
import com.stg.service.ComboSearchService;
import com.stg.service.dto.quotation.QuotationSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class ComboSearchServiceImpl implements ComboSearchService {

	@Autowired
	private UserComboRepository userComboRepository;

	private String getCurrentUsername() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
			return (((UserDetails) principal).getUsername());
		}
		throw new ApplicationException("", new ErrorDto(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED"));
	}

	@Override
	public Page<QuotationSearchDto> searchCombo(Pageable pageable, String query, LocalDate from, LocalDate to) {
		String username = getCurrentUsername();

		Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "createdDate"));

		return userComboRepository
				.search(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort), username, query, from, to)
				.map(q -> {
					QuotationSearchDto dto = new QuotationSearchDto();
					dto.setId(q.getId());
					dto.setComboName(q.getComboName());
					dto.setCreatedDate(q.getCreatedDate().toLocalDate());
					return dto;
				});
	}

	@Transactional
	@Override
	public void deleteCombo(List<Long> ids) {
		if (ids.isEmpty()) {
			return;
		}
		String username = getCurrentUsername();
		int updated = userComboRepository.updateToRaw(ids, username);
		log.info("updated: " + updated);
		log.info("ids.size(): " + ids.size());
		if (updated != ids.size()) {
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Delete failed"));
		}
	}
}
