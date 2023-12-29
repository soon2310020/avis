package com.emoldino.api.analysis.resource.composite.datcol;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.emoldino.api.analysis.resource.base.data.repository.datacounter.DataCounter;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColCommandGetIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColCommandGetOut;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostOut;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColPostShotCountCttIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceCompany;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceLastUpdateIn;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourcePlant;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceSensor;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColResourceTerminal;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColSoftwareVersion;
import com.emoldino.api.analysis.resource.composite.datcol.enumeration.DatColResourceType;
import com.emoldino.api.analysis.resource.composite.datcol.service.analysisdata.DatColAnalyisDataService.AnalysisResult;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(protocols = "http, https", tags = "Analysis / Data Collection")
@RequestMapping("/api/analysis/dat-col")
public interface DatColController {

	@ApiOperation("Post Device Data(such as an IoT Sensor Data) from or via Terminal")
	@PostMapping
	DatColPostOut post(@RequestBody DatColPostIn input);

	@ApiOperation("Dist Device Data(such as an IoT Sensor Data) Manually")
	@GetMapping(path = "/dist")
	SuccessOut dist();

	@ApiOperation("Forward Device Data(such as an IoT Sensor Data) Manually")
	@GetMapping(path = "/forward")
	SuccessOut forward();

	@ApiOperation("Get Commands")
	@GetMapping("/commands")
	DatColCommandGetOut getCommands(DatColCommandGetIn input);

	@ApiOperation("Release Commands")
	@PutMapping("/commands/{deviceId}/release")
	SuccessOut releaseCommands(//
			@PathVariable(name = "deviceId", required = true) String deviceId, //
			@RequestParam(name = "lastIndexNo", required = true) int lastIndexNo);

	@ApiOperation("Post Commands Batch")
	@PostMapping("/commands-batch")
	SuccessOut postCommandsBatch();

	@ApiOperation("Get Software Versions by Device Type")
	@GetMapping("/software-versions-by-device-type/{deviceType}")
	ListOut<DatColSoftwareVersion> getSoftwareVersionsByDeviceType(@PathVariable(name = "deviceType", required = true) String deviceType);

	@ApiOperation("Get Resource Exits")
	@GetMapping("/resources/{resourceType}/updated-at")
	Map<String, String> getResourcesUpdatedAt(@PathVariable(name = "resourceType", required = true) DatColResourceType resourceType, DatColResourceLastUpdateIn input);

	@ApiOperation("Upload Resources")
	@PostMapping("/resources/upload")
	SuccessOut uploadResources();

	@ApiOperation("Upload Resources")
	@GetMapping("/resources/upload")
	SuccessOut getUploadResources();

	@ApiOperation("Post Companies")
	@PostMapping("/resources/COMPANY")
	SuccessOut postResourcesCompany(@RequestBody ListIn<DatColResourceCompany> input);

	@ApiOperation("Post Plants")
	@PostMapping("/resources/PLANT")
	SuccessOut postResourcesPlant(@RequestBody ListIn<DatColResourcePlant> input);

	@ApiOperation("Post Terminals")
	@PostMapping("/resources/TERMINAL")
	SuccessOut postResourcesTerminal(@RequestBody ListIn<DatColResourceTerminal> input);

	@ApiOperation("Post Sensors")
	@PostMapping("/resources/SENSOR")
	SuccessOut postResourcesSensor(@RequestBody ListIn<DatColResourceSensor> input);

	@Deprecated
	@ApiOperation("Download File")
	@GetMapping("/firmwares/{id:.+}")
	void getFirmwareTest(@PathVariable(name = "id", required = true) String id, HttpServletResponse response);

	@ApiOperation("Call All Shot Count Ctt Again")
	@GetMapping("/shot-count-ctt")
	SuccessOut getShotCountCtt(DatColPostShotCountCttIn input);

	@GetMapping("/correct-leaf-year-data")
	SuccessOut getCorrectLeafYearData();

	@GetMapping("/adjust-sensor-reset")
	SuccessOut getAdjustSensorReset();

	@GetMapping("/adjust-batch")
	SuccessOut getAdjustBatch();

	@GetMapping("/adjust-by-data-id/{id:.+}")
	SuccessOut getAdjustByDataId(@PathVariable(name = "id", required = true) Long id);

	@GetMapping("/adjust-by-counter/{counterId:.+}")
	SuccessOut getAdjustByCounter(@PathVariable(name = "counterId", required = true) String counterId);

	@GetMapping("/analysis-raw-data/{dataId:.+}")
	List<AnalysisResult> getAnalysisResultByDataId(@PathVariable(name = "dataId", required = true) Long dataId);

	@PostMapping("/analysis-raw-data")
	AnalysisResult getAnalysisResultByRawData(@RequestBody String rawData);

	@ApiOperation("Fill Mold ID in DataAcceleration")
	@GetMapping("/fill-mold-id")
	SuccessOut getFillMoldId();

	@PostMapping("/generate-data-counter")
	DataCounter getDataCounter(@RequestBody String rawData);

}
