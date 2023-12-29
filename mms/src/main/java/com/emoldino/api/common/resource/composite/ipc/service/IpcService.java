package com.emoldino.api.common.resource.composite.ipc.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.version.dto.AppVersionGetPageIn;
import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion;
import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion.AppVersionDetail;
import com.emoldino.api.common.resource.base.version.service.app.AppVersionService;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersion;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsIn;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsPullIn;
import com.emoldino.framework.dto.ListOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.HttpUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.onboarding.OnBoardingService;
import saleson.common.enumeration.OnBoardingFeature;

@Service
public class IpcService {

	public Map<String, Object> getAppVersionsLatest(String appCode) {
		ValueUtils.assertNotEmpty(appCode, "appCode");
		AppVersionGetPageIn reqin = new AppVersionGetPageIn();
		reqin.setAppCode(appCode);
		reqin.setEnabled(true);
		Page<AppVersion> page = BeanUtils.get(AppVersionService.class).get(reqin, PageRequest.of(0, 1));
		Map<String, Object> output = new LinkedHashMap<>();
		output.put("version", page.isEmpty() ? "0" : ValueUtils.toString(page.getContent().get(0).getVersion(), "0"));
		return output;
	}

	public Page<IpcAppVersion> getAppVersions(IpcAppVersionsIn input, Pageable pageable) {
		ValueUtils.assertNotEmpty(input.getAppCode(), "appCode");
		AppVersionGetPageIn reqin = new AppVersionGetPageIn();
		reqin.setAppCode(input.getAppCode());
		reqin.setUpdatedAtStrGt(input.getUpdatedAtStrGt());
		Page<IpcAppVersion> page = BeanUtils.get(AppVersionService.class).get(reqin, pageable).map(version -> ValueUtils.map(version, IpcAppVersion.class));
		return page;
	}

	public void pullAppVersions(IpcAppVersionsPullIn input) {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}

		JobUtils.runIfNotRunning("Ipc.appVersions", new JobOptions().setClustered(true), () -> {
			String appCode = "MMS";

			Instant lastUpdatedAt;
			{
				AppVersionGetPageIn reqin = new AppVersionGetPageIn();
				reqin.setAppCode(appCode);
				Page<AppVersion> page = BeanUtils.get(AppVersionService.class).get(reqin, PageRequest.of(0, 1, Direction.DESC, "updatedAt"));
				lastUpdatedAt = page.isEmpty() ? null : page.getContent().get(0).getUpdatedAt();
			}

			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.put("appCode", Arrays.asList(appCode));
			if (lastUpdatedAt != null) {
				lastUpdatedAt = lastUpdatedAt.minus(Duration.ofMinutes(1));
				params.put("updatedAtStrGt", Arrays.asList(DateUtils2.format(lastUpdatedAt, DatePattern.yyyyMMddHHmmss, Zone.GMT)));
			}
			List<AppVersion> list = HttpUtils.call(//
					HttpMethod.GET, //
					ConfigUtils.getCentralUrl() + "/api/common/ipc/app-versions", //
					null, params, null, null, null, //
					new ParameterizedTypeReference<ListOut<IpcAppVersion>>() {
					}, 20000).getContent().stream().map(version -> {
						AppVersion appVersion = ValueUtils.map(version, AppVersion.class);
						appVersion.setAppCode(appCode);
						List<AppVersionDetail> items;
						if (ObjectUtils.isEmpty(version.getItems())) {
							items = Collections.emptyList();
						} else {
							items = version.getItems().stream().map(item -> {
								AppVersionDetail appItem = new AppVersionDetail();
								ValueUtils.map(item, appItem);
								return appItem;
							}).collect(Collectors.toList());
						}
						appVersion.setItems(items);
						return appVersion;
					}).collect(Collectors.toList());
			if (ObjectUtils.isEmpty(list)) {
				return;
			}

			Collections.reverse(list);

			boolean[] newVersionAdded = { false };
			list.forEach(version -> {
				boolean added = TranUtils.doTran(() -> {
					AppVersionGetPageIn reqin = new AppVersionGetPageIn();
					reqin.setAppCode(appCode);
					reqin.setVersion(version.getVersion());
					Page<AppVersion> oldPage = BeanUtils.get(AppVersionService.class).get(reqin, PageRequest.of(0, 1));
					AppVersion oldVersion = oldPage.isEmpty() ? null : oldPage.getContent().get(0);
					if (oldVersion == null) {
						BeanUtils.get(AppVersionService.class).post(version);
						return true;
					} else {
						BeanUtils.get(AppVersionService.class).put(oldVersion.getId(), version);
						return false;
					}
				});
				newVersionAdded[0] = newVersionAdded[0] || added;
			});

			if (newVersionAdded[0]) {
				BeanUtils.get(OnBoardingService.class).unreadAll(OnBoardingFeature.VERSION_HISTORY);
			}
		});
	}

}
