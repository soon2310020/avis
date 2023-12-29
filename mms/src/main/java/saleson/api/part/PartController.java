package saleson.api.part;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.emoldino.api.supplychain.resource.base.product.dto.PartLite;
import com.emoldino.api.supplychain.resource.base.product.dto.PartLiteGetIn;
import com.emoldino.api.supplychain.resource.base.product.service.ProductService;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.part.payload.PartPayload;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.PageType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.data.MiniComponentData;

@RestController
@RequestMapping("/api/parts")
public class PartController {

	@Autowired
	PartService partService;

	@Autowired
	VersioningService versioningService;

	@Autowired
	CustomFieldValueService customFieldValueService;
	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	ColumnTableConfigService columnTableConfigService;

	@Autowired
	GeneralConfigService generalConfigService;

	@GetMapping
	public Page<Part> getAllParts(PartPayload payload, Pageable pageable) {
//		Sort sort = pageable.getSort();
//		final Pageable[] newPageable = {null};
//		pageable.getSort().stream().forEach(order -> {
//			if(order.getProperty().equalsIgnoreCase("total")){
//				newPageable[0] = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), new Sort(Sort.Direction.DESC, "moldParts.count"));
//			}
//		});
	    partService.changeTabPayload(payload);
		Page<Part> page = partService.findAllWithStatisticsPart(payload, pageable);
		return page;
	}

	@GetMapping("/lite")
	public Page<PartLite> getPageLite(PartLiteGetIn input, Pageable pageable) {
		return BeanUtils.get(ProductService.class).getPartPageLite(input, pageable);
	}

	@PostMapping
	public ResponseEntity<?> newPart(@RequestBody PartPayload payload) {
		try {
			MultipartFormData multipartFormData= new MultipartFormData();
			multipartFormData.setPayload(objectMapper.writeValueAsString(payload));
		return newPartMultipart(multipartFormData);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Fail!");
		}
	}

	@PostMapping("add-multipart")
	public ResponseEntity<?> newPartMultipart(MultipartFormData formData) {
		try {
			PartPayload payload = objectMapper.readValue(formData.getPayload(), PartPayload.class);
			payload.setPartPictureFiles(formData.getThirdFiles());
			ResponseEntity valid = partService.valid(payload, null);
			if (valid != null) return valid;

//		boolean existsPartCode = partService.existsPartCode(payload.getPartCode());
//
//		if (existsPartCode) {
////			return new ApiResponse(false, "This part is already registered");
//			return ResponseEntity.badRequest().body("This part is already registered");
//
//		}

		Part p=partService.save(payload.getModel(),payload);
//		return new ApiResponse(true, "성공.");
		return ResponseEntity.ok(p);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Fail!");
		}

	}


	@GetMapping("{id}")
	public ResponseEntity<Part> getPart(@PathVariable("id") Long id) {

		try {
			Part part = partService.findById(id);
			return new ResponseEntity<>(part, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@GetMapping("/export-excel")
	public void printExcel(HttpServletResponse response,
						   PartPayload payload, Pageable pageable){
		try {
			OutputStream outputStream = response.getOutputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=tooling-report-"+new Date().getTime()+".xlsx");
			outputStream.write(partService.exportExcelPartList(payload,pageable).toByteArray());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@PutMapping("{id}")
	public ResponseEntity<?>  editPart(@PathVariable("id") Long id,
								@RequestBody PartPayload payload) {
		try {
			MultipartFormData multipartFormData= new MultipartFormData();
			multipartFormData.setPayload(objectMapper.writeValueAsString(payload));
			return editPartMultipart(id,multipartFormData);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Fail!");
		}
	}
	@PutMapping("/edit-multipart/{id}")
	public ResponseEntity<?>  editPartMultipart(@PathVariable("id") Long id,
												MultipartFormData formData) {
		try {
			PartPayload payload = objectMapper.readValue(formData.getPayload(), PartPayload.class);
			payload.setPartPictureFiles(formData.getThirdFiles());

		Part part = partService.findById(id);
		Part oldToCheck= DataUtils.deepCopyJackSon(part,Part.class);

		Part updated = payload.getModel();
		if (!ValueUtils.equals(part.getResinCode(), updated.getResinCode())) {
			partService.saveResinCodeChange(part.getId(), part.getResinCode(), updated.getResinCode());
		}
/*
		updated.setId(part.getId());
		DataUtils.mapCreateAndUpdateInfo(updated, part);
		boolean isIdentical = DataUtils.deepCompare(part, updated);
*/

		if (part == null) {
//			return new ApiResponse(false, "ERROR");
			return ResponseEntity.badRequest().body("ERROR");
		}
			ResponseEntity valid = partService.valid(payload, id);
			if (valid != null) return valid;

		// 2019-05-29 PartCode가 변경된 경우
		if (!payload.getPartCode().equals(part.getPartCode())) {
			String previousPartCode = part.getPartCode();

//			boolean existsPartCode = partService.existsPartCode(payload.getPartCode());
//
//			if (existsPartCode) {
////				return new ApiResponse(false, "Part code is already in use.");
//				return ResponseEntity.badRequest().body("Part code is already in use.");
//
//			}

			partService.save(payload.getModel(part), previousPartCode,payload);

		} else {
			partService.save(payload.getModel(part),payload);
		}
		Part partFinal = partService.findById(id);
		Part newToCheck = DataUtils.deepCopyJackSon(partFinal, Part.class);
		DataUtils.mapCreateAndUpdateInfo(oldToCheck, newToCheck);
		boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);
		if (!isIdentical) {
			versioningService.writeHistory(partFinal);
		}
//		return new ApiResponse(true, "성공.");
		return ResponseEntity.ok(partFinal);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ResponseEntity.badRequest().body(ae.getCodeMessage());
		}catch (Exception e){
			e.printStackTrace();
			return ResponseEntity.badRequest().body("Fail!");
		}

	}


	/**
	 * 계정 활성 / 비활성 처리.
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/enable")
	public ApiResponse user(@PathVariable(value = "id", required = true) Long id, @RequestBody PartPayload payload) {

		if (!id.equals(payload.getId())) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Part part = partService.findById(id);

		part.setEnabled(payload.isEnabled());
		partService.save(part);
		Part partFinal = partService.findById(id);
		versioningService.writeHistory(partFinal);
		return new ApiResponse(true, "OK!!");
	}


	@DeleteMapping("{id}")
	public ApiResponse deletePart(@PathVariable("id") Long id) {

		try {
			partService.deleteById(id);
		} catch (RuntimeException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "Success.");
	}

	@GetMapping("/countries")
	public ResponseEntity findCountriesByPartId(@RequestParam(required = false) Long partId){
		return ResponseEntity.ok(partService.findCountriesByPartId(partId));
	}

	@PostMapping("/import-parts")
	public ResponseEntity<?> newImportParts(@RequestBody List<PartPayload> payloadList){
		for (PartPayload payload: payloadList) {
			try {
/*
				payload.setEnabled(true);
				CustomFieldListDTO customFieldListDTO = new CustomFieldListDTO();
				customFieldListDTO.setCustomFieldDTOList(payload.getCustomFieldDTOList());
				Part part;
				if(payload.getId() == null) {
					part = partService.save(payload.getModel());
				}else{
					part = partService.findById(payload.getId());
					if(part != null){
						partService.save(payload.getModel(part), part.getPartCode());
					}
				}
				if (part != null)
					customFieldValueService.editListCustomField(part.getId(), customFieldListDTO);
*/
				partService.savePartByImport(payload);
			} catch (BizException e) {
				LogUtils.saveErrorQuietly(e);
				AbstractException ae = ValueUtils.toAe(e, null);
				return ResponseEntity.ok(new ApiResponse(false, ae.getCodeMessage(), payload.getPartCode()));
			} catch (Exception exception) {
				return ResponseEntity.ok(new ApiResponse(false, "FAIL", payload.getPartCode()));
			}

		}

		return ResponseEntity.ok(ApiResponse.success());
	}

	@GetMapping("/check-exists")
	public List<MiniComponentData> checkExistsPartCode(@RequestParam List<String> partCodes){
		List<MiniComponentData> existsToolingCodes = partService.getExistsCodes(partCodes);
		return existsToolingCodes;
	}

	@PostMapping("/check-exists-v2")
	public List<MiniComponentData> checkExistsPartCodev2(@RequestBody List<String> partCodes){
//		List<String> partCodesElements = Arrays.asList(partCodes.split(","));
		List<MiniComponentData> existsToolingCodes = partService.getExistsCodes(partCodes);
		return existsToolingCodes;
	}

	@GetMapping("/list-part")
	public ResponseEntity<?> getListPart(@RequestParam(value = "partCodeList", required = false) List<String> partCodeList,
										 @RequestParam(value = "page", required = false) Long page,
										 @RequestParam(value = "size", required = false) Long size,
										 @RequestParam(value = "searchText", required = false) String searchText){
		return ResponseEntity.ok(partService.getListPart(partCodeList, searchText, page, size));
	}
	@GetMapping("/list-part-name")
	public ResponseEntity<?> getListPartName(){
		return ResponseEntity.ok(partService.getListPartName());
	}

	@GetMapping("/exportExcelImportTemplate")
	public void exportExcelImportTemplate(HttpServletResponse response){
		try {
			OutputStream outputStream = response.getOutputStream();
			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=part-import-template.xlsx");
			outputStream.write(partService.exportExcelImportTemplate().toByteArray());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto){
		return partService.changeStatusInBatch(dto);
	}

	@DeleteMapping("/delete/{id}")
	public ApiResponse delete(@PathVariable("id") Long id){
		return partService.delete(id);
	}

	@PostMapping("/restore/{id}")
	public ApiResponse restore(@PathVariable("id") Long id) {
		return partService.restore(id);
	}

	@PostMapping("/delete-in-batch")
	public ApiResponse deleteInBatch(@RequestBody BatchUpdateDTO dto){
		return partService.deleteInBatch(dto);
	}

	@PostMapping("/assign-to-project")
	public ApiResponse assignToProject(@RequestBody List<PartPayload> payload) {
		return partService.assignToProject(payload);
	}

	@GetMapping("/remove-from-project/{id}")
	public ApiResponse removeFromProject(@PathVariable("id") Long id) {
		return partService.removeFromProject(id);
	}

	@PostMapping("/change-material-code")
	public ApiResponse changeMaterialCode(@RequestBody PartPayload payload) {
		return partService.changeMaterialCode(payload);
	}

}
