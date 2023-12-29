package com.emoldino.api.analysis.resource.composite.datcol.service.software;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import com.emoldino.api.analysis.resource.base.command.dto.DeviceCommandDto;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.DeviceCommandRepository;
import com.emoldino.api.analysis.resource.base.command.repository.devicecommand.QDeviceCommand;
import com.emoldino.api.analysis.resource.base.command.service.device.DeviceCommandService;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3Collected;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.Data3CollectedRepository;
import com.emoldino.api.analysis.resource.base.data.repository.data3collected.QData3Collected;
import com.emoldino.api.analysis.resource.composite.datcol.dto.DatColSoftwareVersion;
import com.emoldino.api.common.resource.base.file.dto.FileGroupGetIn;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupStatus;
import com.emoldino.api.common.resource.base.file.enumeration.FileGroupType;
import com.emoldino.api.common.resource.base.file.service.group.FileGroupService;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.exception.AbstractException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import saleson.api.counter.CounterRepository;
import saleson.model.Counter;
import saleson.model.QCounter;

@Service
public class DatColSoftwareService {

	private Set<String> rcAllowedDevices = Collections.synchronizedSet(new HashSet<>());

	@Value("${app.rc-allowed-devices:EMA2201J0TEST}")
	private void setRcAllowedDevices(String value) {
		for (String str : StringUtils.tokenizeToStringArray(value, ",")) {
			rcAllowedDevices.add(str);
		}
	}

	public void postCommandsBatch() {
		JobUtils.runIfNotRunning("DatColSoftwareService.postCommandsBatch", new JobOptions().setClustered(true), () -> {
			String deviceType = "SS3";
			String cmd = "0";

			// 1. Get Versions Recently Released
			DatColSoftwareVersion official = null;
			DatColSoftwareVersion candidate = null;
			{
				ListOut<DatColSoftwareVersion> output = HttpUtils.call(//
						HttpMethod.GET, //
						ConfigUtils.getCentralUrl() + "/api/analysis/dat-col/software-versions-by-device-type/{deviceType}", //
						Arrays.asList(deviceType), null, null, null, null, //
						new ParameterizedTypeReference<ListOut<DatColSoftwareVersion>>() {
						}, 20000);

				for (DatColSoftwareVersion item : output.getContent()) {
					if ("OFFICIAL".equals(item.getReleaseType())) {
						official = item;
						break;
					} else if (candidate == null) {
						candidate = item;
					}
				}
			}
			if (official == null && candidate == null) {
				return;
			}

			// 2. Apply Versions at All Sensors
			int i = 0;
			Page<Counter> page;
			DatColSoftwareVersion version = official;
			DatColSoftwareVersion rcVersion = candidate;
			while (i < 1000 && !(page = BeanUtils.get(CounterRepository.class).findAll(//
					new BooleanBuilder().and(QCounter.counter.enabled.eq(true)), PageRequest.of(i++, 100, Direction.ASC, "id"))).isEmpty()) {
				page.forEach(counter -> {
					String deviceId = counter.getEquipmentCode();

					// 2.1 Get Current Sensor SW Version
					String swVersion;
					{
						QData3Collected table = QData3Collected.data3Collected;
						List<Data3Collected> lastData = BeanUtils.get(Data3CollectedRepository.class).findAll(new BooleanBuilder()//
								.and(table.deviceType.eq(deviceType))//
								.and(table.deviceId.eq(deviceId)//
						), PageRequest.of(0, 1, Direction.DESC, "id")).getContent();
						if (lastData.isEmpty()) {
							return;
						}
						swVersion = lastData.get(0).getDeviceSwVersion();
					}
					//if (ObjectUtils.isEmpty(swVersion) || swVersion.compareTo("3") <= 0) {
					if (ObjectUtils.isEmpty(swVersion)) {
						return;
					}

					// 2.2 Get New Version to Apply
					DatColSoftwareVersion newVersion = null;
					if (rcAllowedDevices.contains(deviceId)//
							&& rcVersion != null && !ObjectUtils.isEmpty(rcVersion.getVersion()) //
							&& ValueUtils.toVersionNo(rcVersion.getVersion()) > ValueUtils.toVersionNo(swVersion)) {
						newVersion = rcVersion;
					} else if (version != null && !ObjectUtils.isEmpty(version.getVersion()) //
							&& ValueUtils.toVersionNo(version.getVersion()) > ValueUtils.toVersionNo(swVersion)) {
						newVersion = version;
					}
					if (newVersion == null) {
						return;
					}

					// 2.3 Check Command Whether Already Post or not
					QDeviceCommand table = QDeviceCommand.deviceCommand;
					if (BeanUtils.get(DeviceCommandRepository.class).exists(new BooleanBuilder()//
							.and(table.deviceType.eq(deviceType))//
							.and(table.deviceId.eq(deviceId))//
							.and(table.command.eq(cmd))//
							.and(table.data.startsWith(newVersion.getVersion() + ",")))) {
						return;
					}

					// 2.4 Post OTA Command
					DeviceCommandDto command = new DeviceCommandDto();
					command.setDeviceType(deviceType);
					command.setDeviceId(deviceId);
					command.setCommand(cmd);
					command.setComment("OTA");
					command.setData(newVersion.getVersion() + "," + ConfigUtils.getCentralUrl() + newVersion.getFileRefs().get(0).getFilePath());
					BeanUtils.get(DeviceCommandService.class).post(command);
				});
			}
		});
	}

	@Transactional
	public ListOut<DatColSoftwareVersion> getSoftwareVersionsByDeviceType(String deviceType) {
		FileGroupGetIn reqin = new FileGroupGetIn();
		reqin.setFileGroupType(FileGroupType.SOFTWARE);
		reqin.setFileGroupCode(deviceType);
		reqin.setFileGroupStatus(Arrays.asList(FileGroupStatus.RELEASED));
		reqin.setGroupByCode(false);
		reqin.setEnabled(true);
		Pageable pageable = PageRequest.of(0, 10, Direction.DESC, "versionNo");
		Page<DatColSoftwareVersion> page = BeanUtils.get(FileGroupService.class)//
				.get(reqin, pageable)//
				.map(fileGroup -> {
					DatColSoftwareVersion item = ValueUtils.map(fileGroup, DatColSoftwareVersion.class);
					item.setDeviceType(deviceType);
					return item;
				});
		return new ListOut<>(page.getContent());
	}

	public void getFirmwareTest(String id, HttpServletResponse response) {
		String contentType = "application/octet-stream";
		String fileName = id;
		InputStream is = null;
		try {
			is = new ClassPathResource("temp/firmwares/" + fileName).getInputStream();
		} catch (Exception e) {
			ValueUtils.closeQuietly(is);
			throw ValueUtils.toAe(e, null);
		}

		OutputStream os = null;
		try {
			os = response.getOutputStream();
			response.setContentType(contentType);
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			IOUtils.copy(is, os);
		} catch (IOException e) {
			AbstractException ae = ValueUtils.toAe(e, "FILE_DOWN_FAIL");
			throw ae;
		} finally {
			ValueUtils.closeQuietly(is);
			ValueUtils.closeQuietly(os);
		}
	}

}
