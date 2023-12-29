package saleson.api.company;

import java.io.IOException;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.framework.util.BeanUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.mold.MoldRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.common.util.SecurityUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Company;
import saleson.model.QMold;
import saleson.model.TabTable;
import saleson.model.TabTableData;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

	@Autowired
	CompanyService companyService;

	@Autowired
	VersioningService versioningService;

	@Autowired
	private TabTableDataRepository tabTableDataRepository;

	@Autowired
	private TabTableRepository tabTableRepository;

	@Autowired
	ObjectMapper objectMapper;

	/**
	 * 목록 조회
	 * @param payload
	 * @param pageable
	 * @param model
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping
	public ResponseEntity<Page<Company>> getAllCompanies(CompanyPayload payload, Pageable pageable, Model model) {

		Page<Company> page;
		payload.setIsDefaultTab(true);
		if(payload.getTabId() != null) {
			Optional<TabTable> tabTableOptional = tabTableRepository.findById(payload.getTabId());
			if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
				List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payload.getTabId());
				List<Long> idList = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
				payload.setIds(idList);
				payload.setIsDefaultTab(tabTableOptional.get().getIsDefaultTab());
			}
		}
		page = companyService.findAll(payload.getPredicate(), pageable);

//		String referer = HttpUtils.getReferer(HttpUtils.getRequest());
//		if (referer != null && referer.endsWith("/admin/companies")) {
////			List<String> list = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.COMPANY_SETTING);
////			if (list.contains("totalMolds")) {
//			page.forEach(item -> item.setTotalMoldCount(getNumberOfMolds(item.getId())));
////			}
//		}

		model.addAttribute("pageContent", page);
		return new ResponseEntity<>(page, HttpStatus.OK);
	}

	private long getNumberOfMolds(Long companyId) {
		long numberOfMolds = BeanUtils.get(MoldRepository.class).count(new BooleanBuilder().and(QMold.mold.companyId.eq(companyId)));
		return numberOfMolds;
	}

	@GetMapping("/get-all-order-by-name")
	public ResponseEntity<Page<Company>> getAllCompaniesOrderByName(CompanyPayload payload, Pageable pageable, Model model) {

		Page<Company> pageContent = companyService.findAllOrderByName(payload, pageable);

		model.addAttribute("pageContent", pageContent);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	/**
	 * 등록
	 * @param payload
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@PostMapping
	public ApiResponse newCompany(@RequestBody CompanyPayload payload) {
//		boolean existsCompanyCode = companyService.existsCompanyCode(payload.getCompanyCode());
//		boolean existsCompanyName = companyService.existsCompanyName(payload.getName());
//
//		if (existsCompanyCode) {
//			return new ApiResponse(false, "The company ID already exists.");
//		}
//		if (existsCompanyName) {
//			return new ApiResponse(false, "The company name already exists.");
//		}
		try{

		ApiResponse vaid = companyService.validCompany(payload, null);
		if (vaid != null) {
			return vaid;
		}
		Company newObj = companyService.save(payload.getModel(), payload.getDataRequestId());
		return ApiResponse.success(CommonMessage.OK, newObj);

		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	@PostMapping("add-multipart")
	public ApiResponse newCompanyMultipart(MultipartFormData formData) {
		try {
			CompanyPayload payload = objectMapper.readValue(formData.getPayload(), CompanyPayload.class);
			payload.setFiles(formData.getFiles());
			ApiResponse vaid = companyService.validCompany(payload, null);
			if (vaid != null) {
				return vaid;
			}
			companyService.saveMultipart(payload.getModel(), payload);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (IOException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "Success.");
	}
	/**
	 * 조회
	 * @param id
	 * @return
	 */
	@DataLeakDetector(disabled = true)
	@GetMapping("{id}")
	public ResponseEntity<Company> getCompany(@PathVariable("id") Long id) {
		try {
			Company company = companyService.findById(id);
			if (company != null) {
//				List<String> list = BeanUtils.get(ColumnTableConfigService.class).getSelectedFields(PageType.COMPANY_SETTING);
//				if (list.contains("moldCount")) {
//					company.setTotalMoldCount(getNumberOfMolds(id));
//				}
				company.setMoldCount(getNumberOfMolds(id));
			}
			return new ResponseEntity<>(company, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@PutMapping("{id}")
	public ApiResponse editCompany(@PathVariable("id") Long id, @RequestBody CompanyPayload payload) {

		try {
			Company company = companyService.findById(id);
			Company oldToCheck= DataUtils.deepCopy(company,Company.class);
/*
			Company updated = payload.getModel();
			updated.setId(company.getId());
			DataUtils.mapCreateAndUpdateInfo(updated, company);
			boolean isIdentical = DataUtils.deepCompare(company, updated);
*/

			if (company == null) {
				new ApiResponse(true, "ERROR");
			}
			ApiResponse vaid = companyService.validCompany(payload, id);
			if (vaid != null)
				return vaid;
			companyService.save(payload.getModel(company));
//		Write log
			DataUtils.mapCreateAndUpdateInfo(oldToCheck, company);
			boolean isIdentical = DataUtils.deepCompare(oldToCheck, company);

			if (!isIdentical) {
				versioningService.writeHistory(companyService.findById(id));
			}
			return new ApiResponse(true, "성공.");
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error();
		}
	}

	@PutMapping("/edit-multipart/{id}")
	public ApiResponse  editMultipart(@PathVariable("id") Long id,
												MultipartFormData formData) {
		try {
			Company company = companyService.findById(id);
			CompanyPayload payload = objectMapper.readValue(formData.getPayload(), CompanyPayload.class);
			payload.setFiles(formData.getFiles());
			Company updated = payload.getModel();
			updated.setId(company.getId());
			DataUtils.mapCreateAndUpdateInfo(updated, company);
			boolean isIdentical = DataUtils.deepCompare(company, updated);


			ApiResponse vaid = companyService.validCompany(payload, id);
			if (vaid != null)
				return vaid;
			companyService.saveMultipart(payload.getModel(company), payload);
//		Write log
			if (!isIdentical) {
				versioningService.writeHistory(companyService.findById(id));
			}
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (IOException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "Success.");
	}

	/**
	 *  활성 / 비활성 처리.
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody CompanyPayload payload) {

		if (!payload.getId().equals(id)) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Company company = companyService.findById(id);
		company.setEnabled(payload.isEnabled());
		companyService.save(company);
		versioningService.writeHistory(companyService.findById(id));

		return new ApiResponse(true, "OK!!");
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteCompany(@PathVariable("id") Long id) {

		try {
			companyService.deleteById(id);
		} catch (RuntimeException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "성공.");
	}

	@GetMapping("/my")
	public ResponseEntity getMyCompany() {
		return ResponseEntity.ok(companyService.findById(SecurityUtils.getCompanyId()));
	}

	@GetMapping("/companies-group")
	public ApiResponse getAllCompaniesGroupBy() {
		ApiResponse response = ApiResponse.success();
		response.setData(companyService.findAllCompaniesGroupBy());
		return response;
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return companyService.changeStatusInBatch(dto);
	}

	@GetMapping(value = "/get-company-list")
	public ApiResponse getCompanyList(@RequestParam(value = "companyCodeList", required = false) List<String> companyCodeList, Pageable pageable,
			@RequestParam(value = "searchText", required = false) String searchText) {
		return companyService.getCompanyList(companyCodeList, pageable, searchText);
	}
}
