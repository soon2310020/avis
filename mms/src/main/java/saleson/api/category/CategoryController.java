package saleson.api.category;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import saleson.api.category.payload.CategoryParam;
import saleson.api.category.payload.CategoryRequest;
import saleson.api.category.payload.CategoryRequestLite;
import saleson.api.category.payload.ProfileFilter;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.part.PartService;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.PageType;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileStorageService;
import saleson.common.util.DataUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.dto.CategoryFullDTO;
import saleson.model.Category;
import saleson.model.data.completionRate.CompletionRateData;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
	@Autowired
	CategoryService categoryService;

	@Autowired
	PartService partService;

	@Autowired
	VersioningService versioningService;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	FileStorageService fileStorageService;

	@Autowired
	private ColumnTableConfigService columnTableConfigService;

	@GetMapping
	public ResponseEntity<Page<CategoryFullDTO>> getAllCategories(CategoryParam param, Pageable pageable, Model model) {
		// TODO Remove
//		if (!ObjectUtils.isEmpty(param.getPeriodValue()) && param.getPeriodValue().equals("202226")) {
//			param.setPeriodValue("202225");
//		}
		param.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.CATEGORY_SETTING));
		Page<CategoryFullDTO> page = categoryService.findAll(param.getPredicate(), pageable, param.getPeriodType(),
				param.getPeriodValue(), param.getProductId(), param.getEnabled(), param.getGrandChildId());
		categoryService.removeUnnecessaryInfo(page.getContent());
		model.addAttribute("pageContent", page);
		return new ResponseEntity<>(page, HttpStatus.OK);
	}

	@GetMapping("/uncompleted")
	public ResponseEntity<Page<CompletionRateData>> getUncompletedCategories(Pageable pageable) {
		Page<CompletionRateData> pageContent = categoryService.getUncompletedCategories(pageable);
		return new ResponseEntity<>(pageContent, HttpStatus.OK);
	}

	@PostMapping
	@Deprecated
	public ApiResponse newCategory(@RequestBody CategoryRequest payload) {
/*
		ApiResponse valid = categoryService.valid(payload, null);
		if (valid != null) {
			return valid;
		}

		Category result = categoryService.saveNew(payload.getModel(), payload);

		if (payload.getModel().getLevel() != null && payload.getModel().getLevel() == 2 && !ObjectUtils.isEmpty(payload.getPeriodValue())) {
			ProductUtils.saveDemand(payload.getModel().getId(), payload.getPeriodValue(), payload.getSpecificWeeklyProductionDemand());
		}

		return new ApiResponse(true, "성공.", result);
*/

		try {
			MultipartFormData multipartFormData = new MultipartFormData();
			multipartFormData.setPayload(objectMapper.writeValueAsString(payload));
			return newMultipartCategory(multipartFormData);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	@PostMapping("/add-multipart")
	public ApiResponse newMultipartCategory(MultipartFormData formData) {
		try {
			CategoryRequest payload = objectMapper.readValue(formData.getPayload(), CategoryRequest.class);
			payload.setProjectImage(formData.getThirdFiles());
			ApiResponse valid = categoryService.validateCreateBranchProduct(payload, null);
			if (valid != null) {
				return valid;
			}
			categoryService.save(payload.getModel(), payload);
			return ApiResponse.success("성공.", payload.getModelFull());
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			e.printStackTrace();
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	@GetMapping("{id}")
	public ResponseEntity<Category> getCategory(@PathVariable("id") Long id) {

		try {
			Category category = categoryService.findById(id);
			return new ResponseEntity<>(category, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@PutMapping("{id}")
	public ApiResponse editCategory(@PathVariable("id") Long id, @RequestBody CategoryRequestLite payload) {
		try {
			// TODO Remove
//			if (!ObjectUtils.isEmpty(payload.getPeriodValue()) && payload.getPeriodValue().contains("202226")) {
//				payload.getPeriodValue().remove("202226");
//				if (!payload.getPeriodValue().contains("202225")) {
//					payload.getPeriodValue().add("202225");
//				}
//			}
			MultipartFormData multipartFormData = new MultipartFormData();
			multipartFormData.setPayload(objectMapper.writeValueAsString(payload));
			return editMultipartCategory(id, multipartFormData);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	@PutMapping("/edit-product-demand/{id}")
	public ApiResponse editProductDemand(@PathVariable("id") Long id, @RequestBody CategoryRequestLite payload) {
		try {
			Category category = categoryService.findById(id);
			if (category == null) {
				new ApiResponse(true, "ERROR");
			}
			category.setWeeklyProductionDemand(payload.getWeeklyProductionDemand());
			categoryService.save(category, null);
			if (category.getLevel() == 3 && !ObjectUtils.isEmpty(payload.getPeriodValue())) {
				ProductUtils.saveDemand(category.getId(), payload.getPeriodValue(), payload.getSpecificWeeklyProductionDemand());
			}
			return ApiResponse.success(CommonMessage.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	@PutMapping("/edit-multipart/{id}")
	public ApiResponse editMultipartCategory(@PathVariable("id") Long id, MultipartFormData formData) {
		try {
			CategoryRequest payload = objectMapper.readValue(formData.getPayload(), CategoryRequest.class);
			payload.setProjectImage(formData.getThirdFiles());
			Category category = categoryService.findById(id);
			Category oldToCheck= DataUtils.deepCopyJackSon(category,Category.class);

/*
			Category clonedCategory = new Category();
			BeanUtils.copyProperties(category, clonedCategory);
			Category updated = payload.getModelFull();
			clonedCategory.setChildren(null);
			DataUtils.mapCreateAndUpdateInfo(updated, clonedCategory);
			boolean isIdentical = DataUtils.deepCompare(clonedCategory, updated);
*/

			if (category == null) {
				new ApiResponse(true, "ERROR");
			}
			if (payload != null && category.getLevel() != null)
				payload.setLevel(category.getLevel());
			ApiResponse valid = categoryService.validateCreateBranchProduct(payload, id);
			if (valid != null) {
				return valid;
			}

/*
			// 1차 카테고리로 설정하는 경우 해당 카테고리가 사용되지 않아야 한다.
			if (payload.getParentId() == null) {
				boolean exists = partService.existsByCategoryId(category.getId());
				if (exists) {
					return new ApiResponse(false, "1차 카테고리로 이동할 수 없습니다. (카테고리 사용 중)");
				}
			}
*/
			payload.getModel(category);
/*
			category.setName(StringUtils.trimWhitespace(payload.getName()));
			category.setDescription(StringUtils.trimWhitespace(payload.getDescription()));
			category.setDivision(payload.getDivision());
			category.setParentId(payload.getParentId());
			category.setEnabled(payload.isEnabled());
			category.setWeeklyProductionDemand(payload.getWeeklyProductionDemand());
*/

			categoryService.save(category, payload);
			if (category.getLevel() == 3 && !ObjectUtils.isEmpty(payload.getPeriodValue())) {
				ProductUtils.saveDemand(category.getId(), payload.getPeriodValue(), payload.getSpecificWeeklyProductionDemand());
			}
			Category categoryFinal = categoryService.findById(id);
			Category newToCheck = DataUtils.deepCopyJackSon(categoryFinal, Category.class);
			DataUtils.mapCreateAndUpdateInfo(oldToCheck, newToCheck);
			boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);

			if (!isIdentical) {
				versioningService.writeHistory(categoryFinal);
			}
			return new ApiResponse(true, "성공.");
		} catch (Exception e) {
			LogUtils.saveErrorQuietly(e);
			e.printStackTrace();
			return ApiResponse.error(CommonMessage.SOMETHING_WENT_WRONG);
		}
	}

	/**
	 * 계정 활성 / 비활성 처리.
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody CategoryRequest payload) {

		if (!id.equals(payload.getId())) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Category category = categoryService.findById(id);

		category.setEnabled(payload.isEnabled());

		// 자식이 있는 경우 자식 까지 같이 변경
		if (!CollectionUtils.isEmpty(category.getChildren()) || !CollectionUtils.isEmpty(category.getGrandchildren())) {
			for (Category child : category.getChildren()) {
				child.setEnabled(payload.isEnabled());
				categoryService.save(child);
				for (Category grandChild: child.getChildren()) {
					grandChild.setEnabled(payload.isEnabled());
					categoryService.save(grandChild);
				}
			}
			for (Category grandChild : category.getGrandchildren()) {
				grandChild.setEnabled(payload.isEnabled());
				categoryService.save(grandChild);
			}

		}
		if (payload.isEnabled()) {
			if (category.getGrandParent() != null)
				category.getGrandParent().setEnabled(true);
			if (category.getParent() != null)
				category.getParent().setEnabled(true);
		}
		categoryService.save(category);
		Category categoryFinal = categoryService.findById(id);
		versioningService.writeHistory(categoryFinal);
		return new ApiResponse(true, "OK!!");
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteCategory(@PathVariable("id") Long id) {
		try {
			categoryService.deleteById(id);
		} catch (RuntimeException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "성공.");
	}

	@PostMapping("/delete-in-batch")
	public ApiResponse deleteInBatch(@RequestBody BatchUpdateDTO dto) {
		return categoryService.deleteInBatch(dto);
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return categoryService.changeStatusInBatch(dto);
	}

	@PostMapping("/upload-photo/{id}")
	public ApiResponse uploadPhoto(@PathVariable("id") Long id, MultipartFormData formData) {
		return categoryService.uploadProjectImage(id, formData);
	}

	@GetMapping("/category-library/{id}/{enabled}")
	public ApiResponse getCategoryLibrary(@PathVariable("id") Long id, @PathVariable("enabled") Boolean enabled) {
		return categoryService.getCategoryLibrary(id, enabled);
	}

	@GetMapping("/project-profile/{id}")
	public ApiResponse getProjectProfile(@PathVariable("id") Long id, @RequestParam(name = "periodType", required = false) String periodType,
			@RequestParam(name = "periodValue", required = false) String periodValue) {
		try {
			return ApiResponse.success(CommonMessage.OK, categoryService.getProjectProfile(id, periodType, periodValue));
		} catch (Exception e) {
			return ApiResponse.error();
		}
	}

	@GetMapping("/brand-profile/{id}/{enabled}")
	public ApiResponse getBrandProfile(@PathVariable("id") Long id, @PathVariable("enabled") Boolean enabled) {
		return categoryService.getBrandLibrary(id, enabled);
	}

	@GetMapping("/no-brand-profile/{id}/{enabled}")
	public ApiResponse getNoBrandProfile(@PathVariable("id") Long grandParentId, @PathVariable("enabled") Boolean enabled) {
		return categoryService.getNoBrandProfile(grandParentId, enabled);
	}

	@GetMapping("/part-by-project")
	public ApiResponse getPartByProject(ProfileFilter filter, Pageable pageable) {
		// TODO Remove
//		if (!ObjectUtils.isEmpty(filter.getPeriodValue()) && filter.getPeriodValue().equals("202226")) {
//			filter.setPeriodValue("202225");
//		}
		return categoryService.getPartByProject(filter, pageable);
	}

	@GetMapping("/part-by-brand")
	public ApiResponse getPartByBrand(ProfileFilter filter, Pageable pageable) {
		return categoryService.getPartByBrand(filter, pageable);
	}

	@GetMapping("/supplier-by-project")
	public ApiResponse getSupplierByProject(ProfileFilter filter, Pageable pageable) {
		// TODO Remove
//		if (!ObjectUtils.isEmpty(filter.getPeriodValue()) && filter.getPeriodValue().equals("202226")) {
//			filter.setPeriodValue("202225");
//		}
		return categoryService.getSupplierByProject(filter, pageable);
	}

	@GetMapping("/supplier-by-brand")
	public ApiResponse getSupplierByBrand(ProfileFilter filter, Pageable pageable) {
		return categoryService.getSupplierByBrand(filter, pageable);
	}

	@GetMapping("/mold-by-project")
	public ApiResponse getMoldByProject(ProfileFilter filter, Pageable pageable) {
		// TODO Remove
//		if (!ObjectUtils.isEmpty(filter.getPeriodValue()) && filter.getPeriodValue().equals("202226")) {
//			filter.setPeriodValue("202225");
//		}
		return categoryService.getMoldByProject(filter, pageable);
	}

	@GetMapping("/mold-by-brand")
	public ApiResponse getMoldByBrand(ProfileFilter filter, Pageable pageable) {
		return categoryService.getMoldByBrand(filter, pageable);
	}
}
