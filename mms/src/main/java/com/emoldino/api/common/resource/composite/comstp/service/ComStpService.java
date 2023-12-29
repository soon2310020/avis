package com.emoldino.api.common.resource.composite.comstp.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import com.emoldino.api.common.resource.base.tab.util.TabUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.note.dto.NoteIn;
import com.emoldino.api.common.resource.base.note.util.NoteUtils;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetIn;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpGetOut;
import com.emoldino.api.common.resource.composite.comstp.dto.ComStpItem;
import com.emoldino.api.common.resource.composite.comstp.repository.ComStpRepository;
import com.emoldino.api.common.resource.composite.datexp.util.DatExpUtils;
import com.emoldino.api.common.resource.composite.flt.dto.FltCompany;
import com.emoldino.framework.dto.BatchIn;
import com.emoldino.framework.dto.Tab;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.Closure1ParamNoReturn;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.QueryUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.LogicUtils;
import com.emoldino.framework.util.ValueUtils;

import saleson.api.accessHierachy.AccessHierarchyRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.versioning.service.VersioningService;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.model.AccessCompanyRelation;
import saleson.model.AccessHierarchy;
import saleson.model.Company;
import saleson.model.TabTable;

@Service
public class ComStpService {

	@Transactional
	public ComStpGetOut get(ComStpGetIn input, Pageable pageable) {
		Page<ComStpItem> page = BeanUtils.get(ComStpRepository.class).findAll(input, null, pageable);
		page.getContent().forEach(company -> {
			Optional<AccessHierarchy> optional = BeanUtils.get(AccessHierarchyRepository.class).findFirstByCompanyId(company.getId());
			if (optional.isPresent()) {
				List<Long> parentIds = optional.get().getAccessCompanyParentRelations().stream().map(AccessCompanyRelation::getCompanyParentId).collect(Collectors.toList());
				company.setUpperTierCompanies(BeanUtils.get(CompanyRepository.class).findByIdIn(parentIds).stream().map(FltCompany::new).collect(Collectors.toList()));
			}
		});

		List<Tab> tabs = QueryUtils.findTabs(ObjectType.COMPANY, input, page, //
				countin -> BeanUtils.get(ComStpRepository.class).count(countin));

		return new ComStpGetOut(page, tabs);
	}

	private Page<ComStpItem> get(ComStpGetIn input, BatchIn batchin, Pageable pageable) {
		return BeanUtils.get(ComStpRepository.class).findAll(input, batchin, pageable);
	}

	private Company findById(Long id) {
		return BeanUtils.get(CompanyRepository.class).findById(id).orElseThrow(() -> DataUtils.newDataNotFoundException(Company.class, "id", id));
	}

	private void save(Company data) {
		BeanUtils.get(CompanyService.class).save(data);
		Company dataGet = BeanUtils.get(CompanyService.class).findById(data.getId());
		BeanUtils.get(VersioningService.class).writeHistory(dataGet);
	}

	public void disableBatch(ComStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, false);
	}

	public void enableBatch(ComStpGetIn input, BatchIn batchin) {
		saveEnabledBatch(input, batchin, true);
	}

	private void saveEnabledBatch(ComStpGetIn input, BatchIn batchin, boolean enabled) {
		ValueUtils.assertNotEmpty(enabled, "enabled");

		runBatch(input, batchin, item -> {
			Company company = findById(item.getId());
			company.setEnabled(enabled);
			save(company);
		});
	}

	public void postNoteBatch(ComStpGetIn input, BatchIn batchin, NoteIn body) {
		runBatch(input, batchin, item -> NoteUtils.post(PageType.COMPANY_SETTING, item.getId(), body));
	}

	private void runBatch(ComStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<ComStpItem> closure) {
		DataUtils.runBatch(Sort.unsorted(), 100, true, //
				pageable -> BeanUtils.get(ComStpRepository.class).findAll(input, batchin, pageable), //
				item -> TranUtils.doNewTran(() -> closure.execute(item))//
		);
	}

	public void export(ComStpGetIn input, BatchIn batchin, Sort sort, HttpServletResponse response) {
		DatExpUtils.exportByJxls("COMPANY", //
				pageable2 -> BeanUtils.get(ComStpService.class).get(input, batchin, pageable2), //
				100, sort == null || sort.isUnsorted() ? Sort.by("companyCode") : sort, //
				"Company", response//
		);
	}

	public void deleteTabItemsBatch(ComStpGetIn input, BatchIn batchin) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.COMPANY, input.getTabName());
		runContentBatch(input, batchin, itemIds -> TabUtils.deleteAllData(itemIds, fromTab), false);
	}

	private void runContentBatch(ComStpGetIn input, BatchIn batchin, Closure1ParamNoReturn<List<Long>> closure, boolean pageUpRequired) {
		DataUtils.runContentBatch(Sort.unsorted(), 100, pageUpRequired, //
				pageable -> BeanUtils.get(ComStpRepository.class).findAll(input, batchin, pageable), //
				items -> TranUtils.doNewTran(() -> closure.execute(items.stream().map(ob -> ob.getId()).collect(Collectors.toList())))//
		);
	}

	public void moveTabItemsBatch(ComStpGetIn input, BatchIn batchin, String toTabName) {
		LogicUtils.assertNotNull(input.getTabName(), "tabName");
		TabTable fromTab = QueryUtils.findTabTable(ObjectType.COMPANY, input.getTabName());
		TabTable toTab = QueryUtils.findTabTable(ObjectType.COMPANY, toTabName);
		if (toTab == null) {
			return;
		}
		runContentBatch(input, batchin, itemIds -> TabUtils.moveAllData(itemIds, fromTab, toTab), false);
	}
}
