package com.emoldino.api.common.resource.base.version.service.app;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.version.dto.AppVersionGetPageIn;
import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion;
import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersion.AppVersionDetail;
import com.emoldino.api.common.resource.base.version.repository.appversion.AppVersionRepository;
import com.emoldino.api.common.resource.base.version.repository.appversion.QAppVersion;
import com.emoldino.api.common.resource.base.version.repository.appversionitem.AppVersionItem;
import com.emoldino.api.common.resource.base.version.repository.appversionitem.AppVersionItemRepository;
import com.emoldino.api.common.resource.base.version.repository.appversionitem.QAppVersionItem;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.QueryUtils;
import com.querydsl.core.BooleanBuilder;

@Transactional
@Service
public class AppVersionService {

	@Autowired
	AppVersionRepository repo;

	public Page<AppVersion> get(AppVersionGetPageIn input, Pageable pageable) {
		// Make Filter
		BooleanBuilder filter = new BooleanBuilder();
		{
			QAppVersion table = QAppVersion.appVersion;
			QueryUtils.eq(filter, table.appCode, input.getAppCode());
			QueryUtils.eq(filter, table.version, input.getVersion());
			if (input.getEnabled() != null) {
				filter.and(table.enabled.eq(input.getEnabled()));
			}
			if (!ObjectUtils.isEmpty(input.getUpdatedAtStrGt())) {
				Instant instant = DateUtils2.toInstant(input.getUpdatedAtStrGt(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
				filter.and(table.updatedAt.gt(instant));
			}
		}

		// Adjust Pageable
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Direction.DESC, "releaseDate");
		}

		Page<AppVersion> page = repo.findAll(filter, pageable);
		page.forEach(appVersion -> migrateAppVersionItem(appVersion));
		return page;
	}

	private void migrateAppVersionItem(AppVersion appVersion) {
		if (!ObjectUtils.isEmpty(appVersion.getItems())) {
			return;
		}

		QAppVersionItem table = QAppVersionItem.appVersionItem;
		BooleanBuilder subfilter = new BooleanBuilder().and(table.appVersionId.eq(appVersion.getId()));
		Iterable<AppVersionItem> items = BeanUtils.get(AppVersionItemRepository.class).findAll(subfilter, Sort.by("position"));
		if (ObjectUtils.isEmpty(items)) {
			return;
		}

		List<AppVersionDetail> details = new ArrayList<>();
		items.forEach(item -> details.add(new AppVersionDetail(item.getDescription())));
		appVersion.setItems(details);

		BeanUtils.get(AppVersionRepository.class).save(appVersion);
		BeanUtils.get(AppVersionItemRepository.class).deleteAll(items);
	}

	public void post(AppVersion data) {
		Assert.notNull(data, "data is required!!");
		Assert.notNull(data.getAppCode(), "appCode is required!!");
		Assert.notNull(data.getVersion(), "version is required!!");

		if (data.getId() != null && repo.existsById(data.getId())) {
			new BizException("DATA_ALREADY_EXIST", "AppVersion id:" + data.getId() + " is not found!!", new Property("model", "AppVersion"), new Property("id", data.getId()));
		}

		repo.save(data);
	}

	public void put(Long id, AppVersion data) {
		Assert.notNull(id, "id is required!!");
		Assert.notNull(data, "data is required!!");

		AppVersion oldData = findById(id);

		oldData.setVersion(data.getVersion());
		oldData.setReleaseDate(data.getReleaseDate());

		repo.save(oldData);
	}

	public void disable(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		for (Long id : ids) {
			AppVersion data = findById(id);
			data.setEnabled(false);
			repo.save(data);
		}
	}

	public void enable(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}
		for (Long id : ids) {
			AppVersion data = findById(id);
			data.setEnabled(true);
			repo.save(data);
		}
	}

	private AppVersion findById(Long id) {
		return repo.findById(id)
				.orElseThrow(() -> new BizException("DATA_NOT_FOUND", "AppVersion id:" + id + " is not found!!", new Property("model", "AppVersion"), new Property("id", id)));
	}

}
