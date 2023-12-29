package saleson.api.location;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.emoldino.api.common.resource.base.accesscontrol.annotation.DataLeakDetector;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.ValueUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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

import com.emoldino.api.common.resource.composite.pltstp.service.PltStpService;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import saleson.api.location.payload.LocationPayload;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.domain.MultipartFormData;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.Location;
import saleson.model.Part;
import saleson.model.TabTable;
import saleson.model.TabTableData;
import saleson.model.data.TerminalLocationCode;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

	@Autowired
	LocationService locationService;

	@Autowired
	ModelMapper modelMapper;

	@Autowired
	VersioningService versioningService;
	@Autowired
	ObjectMapper objectMapper;

	@GetMapping
	public ResponseEntity<Page<Location>> getAllLocations(LocationPayload payload, Pageable pageable, Model model,
			@RequestParam(value = "locationCodeList", required = false) List<String> locationCodeList) {
//		Page<Location> pageContent = locationService.findAll(payload.getPredicate(), pageable);
		locationService.changeTabPayload(payload);
		Page<Location> page = locationService.findLocationData(payload.getPredicate(), pageable, locationCodeList);
		page.forEach(location -> location.setAreas(BeanUtils.get(PltStpService.class).getAreas(location.getId()).getContent()));
		model.addAttribute("pageContent", page);
		return new ResponseEntity<>(page, HttpStatus.OK);
	}

	@PostMapping
	public ApiResponse newLocation(@RequestBody LocationPayload payload) {
		try {
			ApiResponse valid = locationService.validLocation(payload, null);
			if (valid != null) {
				return valid;
			}
			//locationService.save(payload.getModel());
			Location locationNew = locationService.save(modelMapper.map(payload, Location.class), payload.getDataRequestId());
			return ApiResponse.success(CommonMessage.OK, locationNew);
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
			LocationPayload payload = objectMapper.readValue(formData.getPayload(), LocationPayload.class);
			payload.setFiles(formData.getFiles());
			ApiResponse valid = locationService.validLocation(payload, null);
			if (valid != null) {
				return valid;
			}
			locationService.saveMultipart(modelMapper.map(payload, Location.class), payload);
		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (IOException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "Success.");
	}


	@DataLeakDetector(disabled = true)
	@GetMapping("{id}")
	public ResponseEntity<Location> getLocation(@PathVariable("id") Long id) {
		try {
			Location location = locationService.findById(id);
			if (location != null) {
				location.setAreas(BeanUtils.get(PltStpService.class).getAreas(location.getId()).getContent());
			}
			return new ResponseEntity<>(location, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(null);
		}
	}

	@PutMapping("{id}")
	public ApiResponse editLocation(@PathVariable("id") Long id, @RequestBody LocationPayload payload) {
		try {
			Location location = locationService.findById(id);
			Location oldToCheck= DataUtils.deepCopyJackSon(location,Location.class);

			if (location == null) {
				new ApiResponse(true, "ERROR");
			}
/*
			Location updated = payload.getModel();
			updated.setId(location.getId());
			updated.setCompany(location.getCompany());
			boolean isIdentical = DataUtils.deepCompare(location, updated);
*/
			ApiResponse valid = locationService.validLocation(payload, id);
			if (valid != null) {
				return valid;
			}
			//modelMapper.map(payload, location);
//		Write log
			locationService.save(payload.getModel(location));
			locationService.save(location);
			Location locationFinal = locationService.findById(id);
			Location newToCheck = DataUtils.deepCopyJackSon(locationFinal, Location.class);
			DataUtils.mapCreateAndUpdateInfo(oldToCheck, newToCheck);
			boolean isIdentical = DataUtils.deepCompare(oldToCheck, newToCheck);

			if (!isIdentical) {
				versioningService.writeHistory(locationFinal);
			}

			return new ApiResponse(true, "Success.");
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
			Location location = locationService.findById(id);

			if (location == null) {
				new ApiResponse(true, "ERROR");
			}
			LocationPayload payload = objectMapper.readValue(formData.getPayload(), LocationPayload.class);
			payload.setFiles(formData.getFiles());
			Location updated = payload.getModel();
			updated.setId(location.getId());
			updated.setCompany(location.getCompany());
			boolean isIdentical = DataUtils.deepCompare(location, updated);
			ApiResponse valid = locationService.validLocation(payload, id);
			if (valid != null) {
				return valid;
			}

			//copy logic from edit api
			locationService.saveMultipart(payload.getModel(location), payload);
			locationService.save(location);
			Location locationFinal = locationService.findById(id);

			if (!isIdentical) {
				versioningService.writeHistory(locationFinal);
			}

			return new ApiResponse(true, "Success.");

		} catch (BizException e) {
			LogUtils.saveErrorQuietly(e);
			AbstractException ae = ValueUtils.toAe(e, null);
			return ApiResponse.error(ae.getCodeMessage());
		} catch (IOException e) {
			return ApiResponse.error();
		}
	}

	/**
	 *  활성 / 비활성 처리.
	 * @param id
	 * @param payload
	 * @return
	 */
	@PutMapping("/{id}/enable")
	public ApiResponse enable(@PathVariable(value = "id", required = true) Long id, @RequestBody LocationPayload payload) {
		if (!payload.getId().equals(id)) {
			return new ApiResponse(false, "요청 데이터 오류");
		}

		Location location = locationService.findById(id);
		location.setEnabled(payload.getEnabled());
		locationService.save(location);
		Location locationFinal = locationService.findById(id);
		versioningService.writeHistory(locationFinal);
		return new ApiResponse(true, "OK!!");
	}

	@DeleteMapping("{id}")
	public ApiResponse deleteLocation(@PathVariable("id") Long id) {
		try {
			locationService.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			return new ApiResponse(false, "You can not delete it because it has a location in use.");
		} catch (RuntimeException e) {
			return new ApiResponse(false, e.getMessage());
		}
		return new ApiResponse(true, "Success");
	}

	@GetMapping("/get-terminals/{terminalId}")
	public ResponseEntity<?> getCounters(@PathVariable(value = "terminalId") Long id) {
		List<TerminalLocationCode> counterToolingCodes = locationService.getTerminalIdsByLocationId(id);
		return new ResponseEntity<>(counterToolingCodes, HttpStatus.OK);
	}

	@PostMapping("/change-status-in-batch")
	public ApiResponse changeStatusInBatch(@RequestBody BatchUpdateDTO dto) {
		return locationService.changeStatusInBatch(dto);
	}

	@GetMapping("/get-mold-number")
	public ApiResponse getMoldNumber() {
		return locationService.getMoldNumber();
	}

}
