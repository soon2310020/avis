package com.emoldino.api.analysis.resource.composite.datcol;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

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
import com.emoldino.api.analysis.resource.composite.datcol.service.DatColService;
import com.emoldino.api.analysis.resource.composite.datcol.service.analysisdata.DatColAnalyisDataService;
import com.emoldino.api.analysis.resource.composite.datcol.service.analysisdata.DatColAnalyisDataService.AnalysisResult;
import com.emoldino.api.analysis.resource.composite.datcol.service.accelerationdata.DatColFillDataService;
import com.emoldino.api.analysis.resource.composite.datcol.service.adjust.cycletimedata.DatColAdjustCycleTimeDataService;
import com.emoldino.api.analysis.resource.composite.datcol.service.adjust.resetdata.DatColAdjustSensorResetDataService;
import com.emoldino.api.analysis.resource.composite.datcol.service.correctleafyeardata.DatColCorrectLeafYearDataService;
import com.emoldino.api.analysis.resource.composite.datcol.service.dist.DatColDistService;
import com.emoldino.api.analysis.resource.composite.datcol.service.resource.DatColResourceService;
import com.emoldino.api.analysis.resource.composite.datcol.service.software.DatColSoftwareService;
import com.emoldino.framework.dto.ListIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;

import saleson.service.data.service.DataMapper;

@RestController
public class DatColControllerImpl implements DatColController {
	@Autowired
	private DatColService service;

	@Override
	public DatColPostOut post(DatColPostIn input) {
		DatColPostOut output = service.post(input);
		output.setServerTime(DateUtils2.format(DateUtils2.newInstant(), DatePattern.yyyyMMddHHmmss, Zone.GMT));
		return output;
	}

	@Override
	public SuccessOut dist() {
		BeanUtils.get(DatColDistService.class).dist();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut forward() {
		BeanUtils.get(DatColDistService.class).forward();
		return SuccessOut.getDefault();
	}

	@Override
	public DatColCommandGetOut getCommands(DatColCommandGetIn input) {
		return service.getCommands(input);
	}

	@Override
	public SuccessOut releaseCommands(String deviceId, int lastCommandIndexNo) {
		service.releaseCommands(deviceId, lastCommandIndexNo);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postCommandsBatch() {
		BeanUtils.get(DatColSoftwareService.class).postCommandsBatch();
		return SuccessOut.getDefault();
	}

	@Override
	public ListOut<DatColSoftwareVersion> getSoftwareVersionsByDeviceType(String deviceType) {
		return BeanUtils.get(DatColSoftwareService.class).getSoftwareVersionsByDeviceType(deviceType);
	}

	@Override
	public Map<String, String> getResourcesUpdatedAt(DatColResourceType resourceType, DatColResourceLastUpdateIn input) {
		return BeanUtils.get(DatColResourceService.class).getUpdatedAt(resourceType, input);
	}

	public SuccessOut uploadResources() {
		BeanUtils.get(DatColResourceService.class).upload();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut getUploadResources() {
		BeanUtils.get(DatColResourceService.class).upload();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postResourcesCompany(ListIn<DatColResourceCompany> input) {
		BeanUtils.get(DatColResourceService.class).postCompanies(input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postResourcesPlant(ListIn<DatColResourcePlant> input) {
		BeanUtils.get(DatColResourceService.class).postPlants(input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postResourcesTerminal(ListIn<DatColResourceTerminal> input) {
		BeanUtils.get(DatColResourceService.class).postTerminals(input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut postResourcesSensor(ListIn<DatColResourceSensor> input) {
		BeanUtils.get(DatColResourceService.class).postSensors(input);
		return SuccessOut.getDefault();
	}

	@Deprecated
	@Override
	public void getFirmwareTest(String id, HttpServletResponse response) {
		BeanUtils.get(DatColSoftwareService.class).getFirmwareTest(id, response);
	}

	@Override
	public SuccessOut getShotCountCtt(DatColPostShotCountCttIn input) {
		service.postShotCountCtt(input);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut getCorrectLeafYearData() {
		BeanUtils.get(DatColCorrectLeafYearDataService.class).post();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut getAdjustBatch() {
		BeanUtils.get(DatColAdjustCycleTimeDataService.class).adjustBatch();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut getAdjustByDataId(Long id) {
		BeanUtils.get(DatColAdjustCycleTimeDataService.class).adjustByDataId(id);
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut getAdjustByCounter(String counterId) {
		BeanUtils.get(DatColAdjustCycleTimeDataService.class).findAndAdjustBatch(counterId);
		return SuccessOut.getDefault();
	}

	@Override
	public List<AnalysisResult> getAnalysisResultByDataId(Long dataId) {
		return BeanUtils.get(DatColAnalyisDataService.class).getAnalysisResultByDataId(dataId);
	}

	@Override
	public AnalysisResult getAnalysisResultByRawData(String rawData) {
		return BeanUtils.get(DatColAnalyisDataService.class).getAnalysisResult(rawData);
	}

	@Override
	public SuccessOut getAdjustSensorReset() {
		BeanUtils.get(DatColAdjustSensorResetDataService.class).adjustBatch();
		return SuccessOut.getDefault();
	}

	@Override
	public SuccessOut getFillMoldId() {
		BeanUtils.get(DatColFillDataService.class).fillMoldIdAtDataAcceleration();
		return SuccessOut.getDefault();
	}

	@Override
	public DataCounter getDataCounter(String rawData) {
		return BeanUtils.get(DataMapper.class).toDataCounter2(rawData);
	}

}
