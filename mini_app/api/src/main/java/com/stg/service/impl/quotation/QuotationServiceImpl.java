package com.stg.service.impl.quotation;

import com.google.gson.Gson;
import com.stg.common.Jackson;
import com.stg.constant.quotation.QuotationState;
import com.stg.entity.quotation.QuotationHeader;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;
import com.stg.errors.quote.CreateQuoteException;
import com.stg.repository.QuotationHeaderRepository;
import com.stg.service.FlexQuotationService;
import com.stg.service.QuotationService;
import com.stg.service.dto.mbal.GenerateQuotation;
import com.stg.service.dto.mbal.QuotationModel;
import com.stg.service.dto.quotation.FlexibleSubmitQuestionInput;
import com.stg.service.dto.quotation.QuotationDto;
import com.stg.service.dto.quotation.QuotationHeaderDto;
import com.stg.service.dto.quotation.QuotationSearchDto;
import com.stg.service3rd.common.dto.error.IErrorObject;
import com.stg.service3rd.common.utils.ApiUtil;
import com.stg.service3rd.mbal.MbalApi3rdService;
import com.stg.service3rd.mbal.adapter.MbalFunctions;
import com.stg.service3rd.mbal.dto.error.MbalErrorObject;
import com.stg.service3rd.mbal.exception.MbalApi3rdException;
import com.stg.utils.AppUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.TypeToken;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.stg.utils.quotation.QuoteMapperUtil.quotationHeaderToEntity;
import static com.stg.utils.quotation.QuoteMapperUtil.quotationModelToEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotationServiceImpl implements QuotationService {
	private final Jackson jackson;
	private final Gson gson;

	private final QuotationHeaderRepository quotationHeaderRepository;
	private final FlexQuotationService flexQuotationService;
	private final MbalApi3rdService mbalApi3rdService;

	@Transactional
	@Override
	public void saveQuotation(Long id) {
		String username = AppUtil.getCurrentUsername();
		QuotationHeader quotation = quotationHeaderRepository.getById(id, true, username);
		quotation.setRaw(false);
		quotationHeaderRepository.save(quotation);
	}

	@Override
	public QuotationDto findQuotation(Long id) {
		String username = AppUtil.getCurrentUsername();
		QuotationHeader quotation = quotationHeaderRepository.getById(id, false, username);
		if (quotation == null) {
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Not found"));
		}

		QuotationDto quotationDto;
		if (StringUtils.hasText(quotation.getRawData())) {
			quotationDto = jackson.fromJson(quotation.getRawData(), QuotationDto.class);
			quotationDto.setState(quotation.getState());
		} else {
			quotationDto = new QuotationDto(quotation);
		}

		if (StringUtils.hasText(quotation.getHealths())) {
			List<FlexibleSubmitQuestionInput> healths = gson.fromJson(quotation.getHealths(), new TypeToken<List<FlexibleSubmitQuestionInput>>() {
			}.getType());
			quotationDto.setHealths(healths);

			quotationDto.setHealthsTxt(quotation.getHealths());
		}

		return quotationDto;
	}

	@Transactional
	@Override
	public QuotationDto createQuotation(QuotationHeaderDto reqDto) {
		MbalFunctions endpoint;
		switch (reqDto.getType()) {
			case ULSP:
				endpoint = MbalFunctions.GEN_QUOTE_ULSP;
				break;
			case ULRP:
				endpoint = MbalFunctions.GEN_QUOTE_ULRP;
				break;
			case ULRP_3_0:
				/* @return [flexCreateQuote] */
				return flexQuotationService.flexCreateQuote(reqDto);

			default:
				throw new CreateQuoteException("QuotationType", new ErrorDto(HttpStatus.BAD_REQUEST, "Unsupport"));
		}

		try {
			QuotationHeader quotation = quotationHeaderToEntity(reqDto);
			GenerateQuotation generateQuotation = new GenerateQuotation(quotation);

			long startTime = System.currentTimeMillis();
			QuotationModel response = mbalApi3rdService.generateQuotation(endpoint, generateQuotation);
			log.debug("Total time Generate Quotation: {}", System.currentTimeMillis() - startTime);
			if (response == null) {
				throw new CreateQuoteException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Body is empty"));
			}

			quotation.setQuotationId(response.getQuotationId());
			quotation.setSubmissionId(response.getSubmissionId());
			quotation.setDetails(quotationModelToEntity(response, quotation));
			quotation.setRaw(false);
			quotation.setUuid(UUID.randomUUID());
			quotation.setState(QuotationState.CREATED);
			QuotationHeader quotationDb = quotationHeaderRepository.save(quotation);
			return new QuotationDto(quotationDb);
		} catch (Exception ex) {
			IErrorObject errorObject = ApiUtil.parseErrorMessage(ex, MbalErrorObject.class);
			throw new CreateQuoteException(errorObject.getErrorMessage(), errorObject.toErrorDto());
		}
	}

	@Override
	public Page<QuotationSearchDto> search(Pageable pageable, String query, LocalDate date, LocalDate from, LocalDate to) {
		String username = AppUtil.getCurrentUsername();

		Sort sort = Sort.by(new Sort.Order(Direction.DESC, "createdDate"));

		return quotationHeaderRepository.search(PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort), username, query, date, from, to).map(q -> {
			QuotationSearchDto dto = new QuotationSearchDto();
			dto.setId(q.getId());
			dto.setName(q.getCustomer().getFullName());
			dto.setPhoneNumber(q.getCustomer().getPhoneNumber());
			dto.setCreatedDate(q.getCreatedDate().toLocalDate());
			dto.setQuotationState(q.getState());
			return dto;
		});
	}

	@Transactional
	@Override
	public void deleteQuotation(List<Long> ids) {
		if (ids.isEmpty()) {
			return;
		}
		String username = AppUtil.getCurrentUsername();
		int updated = quotationHeaderRepository.updateToRaw(ids, username);
		if (updated != ids.size()) {
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Delete failed"));
		}
	}

	@Override
	public ResponseEntity<Resource> downloadPDF(Long id, String username) {
		if (username == null) {
			username = AppUtil.getCurrentUsername();
		}

		QuotationHeader quotation = quotationHeaderRepository.getById(id, username);

		try {
			MbalFunctions endpoint;
			switch (quotation.getType()) {
				case ULSP:
					endpoint = MbalFunctions.DOWNLOAD_QUOTE_ULSP;
					break;
				case ULRP:
					endpoint = MbalFunctions.DOWNLOAD_QUOTE_ULRP;
					break;
				case ULRP_3_0:
					endpoint = MbalFunctions.DOWNLOAD_QUOTE_ULRP_3_0_PDF;
					break;

				default:
					throw new ApplicationException("QuotationType", new ErrorDto(HttpStatus.BAD_REQUEST, "Unsupport"));
			}

			byte[] response;
			if (MbalFunctions.DOWNLOAD_QUOTE_ULRP_3_0_PDF.equals(endpoint)) {
				response = mbalApi3rdService.flexDownloadQuotationPdf(quotation.getProcessId());
			} else {
				response = mbalApi3rdService.downloadQuotationPdf(endpoint, quotation.getQuotationId(), quotation.getSubmissionId());
			}

			if (response == null || response.length == 0) {
				throw new ApplicationException("Download pdf, body is empty", new ErrorDto(HttpStatus.BAD_REQUEST, "Download pdf, body is empty"));
			}
			ByteArrayResource resource = new ByteArrayResource(response);

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);

		} catch (Exception e) {
			IErrorObject errorObject = ApiUtil.parseErrorMessage(e, MbalErrorObject.class);
			throw new MbalApi3rdException(errorObject.getErrorMessage(), errorObject.toErrorDto());
		}

	}

}
