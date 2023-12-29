package com.emoldino.api.common.resource.base.code.service.data;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.code.dto.CodeDataGetIn;
import com.emoldino.api.common.resource.base.code.dto.CodeType;
import com.emoldino.api.common.resource.base.code.repository.codedata.CodeData;
import com.emoldino.api.common.resource.base.code.repository.codedata.CodeDataRepository;
import com.emoldino.api.common.resource.base.code.repository.codedata.QCodeData;
import com.emoldino.api.common.resource.base.code.service.type.CodeTypeService;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpDataItem;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpGroupItem;
import com.emoldino.api.common.resource.composite.codstp.dto.CodStpGroupItemIn;
import com.emoldino.framework.exception.BizException;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.Property;
import com.emoldino.framework.util.SyncCtrlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.company.CompanyRepository;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;

@Transactional
@Service
public class CodeDataService {

	@Autowired
	public CodeDataService(CodeDataRepository codeDataRepo, CompanyRepository companyRepo, JPAQueryFactory queryFactory) {
		this.codeDataRepo = codeDataRepo;
		this.companyRepo = companyRepo;
		this.queryFactory = queryFactory;
	}
	
	final private CodeDataRepository codeDataRepo;
	final private CompanyRepository companyRepo;
	final private JPAQueryFactory queryFactory;
	
	public Page<CodeData> getPage(CodeDataGetIn input, Pageable pageable) {
		Assert.notNull(input.getCompanyId(), "companyId is required!!");

		// Default Sorting "position", "code"
		if (pageable.getSort() == null || pageable.getSort().isUnsorted()) {
			pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("position", "code"));
		}

		// 
		initGroup1CodeData(input.getCompanyId());

		Map<String, Optional<String>> titleCache = new HashMap<>();
		Page<CodeData> page = codeDataRepo.findAll(input.getPredicate(), pageable);
		page.forEach(item -> {
			CodeType codeType = BeanUtils.get(CodeTypeService.class).get(item.getCodeType());
			if (codeType == null) {
				return;
			}
			if (!ObjectUtils.isEmpty(item.getGroup1Code()) && codeType.getGroup1Field() != null && !ObjectUtils.isEmpty(codeType.getGroup1Field().getCodeType())) {
				String groupType = codeType.getGroup1Field().getCodeType();
				String groupCode = item.getGroup1Code();				
				String title = CacheUtils.getNullable(titleCache, groupType + "," + groupCode, () -> {
					return codeDataRepo.findByCodeTypeAndCodeAndCompanyId(groupType, groupCode,input.getCompanyId()).orElse(new CodeData()).getTitle();
				});
				item.setGroup1Title(title);
			}
			if (!ObjectUtils.isEmpty(item.getGroup2Code()) && codeType.getGroup2Field() != null && !ObjectUtils.isEmpty(codeType.getGroup2Field().getCodeType())) {
				String groupType = codeType.getGroup2Field().getCodeType();
				String groupCode = item.getGroup2Code();
				String title = CacheUtils.getNullable(titleCache, groupType + "," + groupCode, () -> {
					return codeDataRepo.findByCodeTypeAndCodeAndCompanyId(groupType, groupCode, input.getCompanyId()).orElse(new CodeData()).getTitle();
				});
				item.setGroup2Title(title);
			}
		});
		return page;
	}

	private void save(CodeData data) {
		Instant now = Instant.now();
		CodeData oldData = codeDataRepo.findByCodeTypeAndCodeAndCompanyId(data.getCodeType(), data.getCode(), data.getCompanyId()).orElse(null);

		if (oldData == null) {
			data.setCreatedBy(SecurityUtils.getUserId());
			data.setCreatedAt(now);
		} else {
			if (oldData.getId().equals(data.getId())) {
				// Same
			} else if (oldData.isDeleted()) {
				data.setId(oldData.getId());
			} else if (!oldData.isEnabled()) {
				throw new BizException("DATA_DISABLED", "Code:" + data.getCodeType() + "." + data.getCode() + " is Disabled!!");
			} else {
				throw new LogicException("UNIQUE_INDEX_VIOLATION", "Code:" + data.getCodeType() + "." + data.getCode() + " already Exist!!");
			}
		}
		data.setUpdatedBy(SecurityUtils.getUserId());
		data.setUpdatedAt(now);
		codeDataRepo.save(data);
	}

	//	private void deleteList(List<CodeData> list) {
	//		if (list == null) {
	//			return;
	//		}
	//		list.forEach(data -> {
	//			CodeData oldData = repo.findByCodeTypeAndCode(data.getCodeType(), data.getCode()).orElse(null);
	//			if (oldData == null || !oldData.isDeleted()) {
	//				throw new BizException("DATA_NOT_FOUND", "Code:" + data.getCodeType() + "." + data.getCode() + " is Not Found!!");
	//			} else if (oldData.isDeleted()) {
	//				throw new BizException("DATA_ALREADY_DELETED", "Code:" + data.getCodeType() + "." + data.getCode() + " is already Deleted!!");
	//			} else if (!oldData.getId().equals(data.getId())) {
	//				throw new LogicException("UNIQUE_INDEX_VIOLATION", "Code:" + data.getCodeType() + "." + data.getCode() + " already Exist!!");
	//			}
	//			data.setDeleted(true);
	//		});
	//		repo.saveAll(list);
	//	}

	//	private void delete(CodeData data) {
	//		if (data == null) {
	//			return;
	//		}
	//		deleteList(Arrays.asList(data));
	//	}

	@Value("${customer.server.name:null}")
	private String customerServerName;

	private void initGroup1CodeData(Long companyId) {
		if (customerServerName == null) {
			return;
		}

		QCodeData table = QCodeData.codeData;

		// MACHINE_DOWNTIME_REASON_GROUP1
		if (!codeDataRepo.exists(new BooleanBuilder().and(table.companyId.eq(companyId)).and(table.codeType.eq("MACHINE_DOWNTIME_REASON_GROUP1")))) {
			SyncCtrlUtils.wrap("CodeDataService.MOCK_DATA.MACHINE_DOWNTIME_REASON_GROUP1", () -> {
				save(new CodeData(companyId, "MACHINE_DOWNTIME_REASON_GROUP1", "PLANNED", "Planned Downtime", null));
				save(new CodeData(companyId, "MACHINE_DOWNTIME_REASON_GROUP1", "UNPLANNED", "Unplanned Downtime", null));
			});
		}
	}

	public CodStpDataItem get(Long id) {
		CodeData data = findById(id);

		CodStpDataItem output = new CodStpDataItem();
		output.setId(id);
		output.setCode(data.getCode());
		output.setCodeType(data.getCodeType());
		output.setCompanyId(data.getCompanyId());
		output.setCompanyCode(data.getCompany().getCompanyCode());
		output.setDescription(data.getDescription());
		output.setTitle(data.getTitle());
		output.setGroup1Code(data.getGroup1Code());
		output.setGroup2Code(data.getGroup2Code());

		return output;
	}

	public void post(CodStpDataItem data) {
		// TODO : DataUtils.populate4Insert(data); 
		Instant now = Instant.now();

		Assert.notNull(data, "data is required!!");
		Assert.notNull(data.getCompanyId(), "companyId is required!!");
		Assert.notNull(data.getCode(), "code is required!!");
		Assert.notNull(data.getCodeType(), "codeType is required!!");

		CodeData oldData = codeDataRepo.findByCodeTypeAndCodeAndCompanyId(data.getCodeType(), data.getCode(), data.getCompanyId()).orElse(null);

		if (oldData != null) {
			throw new BizException("DATA_ALREADY_EXIST", "CodeData id:" + oldData.getId() + " is alery exist!!", new Property("model", "CodeData"),
					new Property("id", data.getId()));
		}

		CodeData input = new CodeData();

		Optional<Company> company = companyRepo.findById(data.getCompanyId());

		input.setCompanyId(data.getCompanyId());
		input.setCompany(company.get());
		input.setCodeType(data.getCodeType());
		input.setCode(data.getCode());
		input.setTitle(data.getTitle());
		input.setDescription(data.getDescription());
		input.setGroup1Code(data.getGroup1Code());
		input.setGroup2Code(data.getGroup2Code());

		input.setEnabled(true);
		input.setDeleted(false);
		
		DataUtils.populate4Insert(input);
		
		codeDataRepo.save(input);
	}

	public void put(Long id, CodStpDataItem data) {
		// TODO : DataUtils.populate4Update(data); 
		Instant now = Instant.now();

		Assert.notNull(data, "data is required!!");
		Assert.notNull(data.getCompanyId(), "companyId is required!!");
		Assert.notNull(data.getCode(), "code is required!!");
		Assert.notNull(data.getCodeType(), "codeType is required!!");

		Optional<CodeData> oldData = codeDataRepo.findById(id);
		Optional<Company> company = companyRepo.findById(data.getCompanyId());

		if (!ObjectUtils.isEmpty(oldData) && !oldData.get().isEnabled()) {
			throw new BizException("DATA_DISABLED", "Code:" + data.getCodeType() + "." + data.getCode() + " is Disabled!!");
		}

		CodeData input = new CodeData();
		input.setId(data.getId());
		input.setCompanyId(data.getCompanyId());
		input.setCompany(company.get());
		input.setCodeType(data.getCodeType());
		input.setCode(data.getCode());
		input.setTitle(data.getTitle());
		input.setDescription(data.getDescription());
		input.setGroup1Code(data.getGroup1Code());
		input.setGroup2Code(data.getGroup2Code());

		input.setEnabled(true);
		input.setDeleted(false);
		DataUtils.populate4Update(input);
		codeDataRepo.save(input);
	}

	public void disableList(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}

		for (Long id : ids) {
			CodeData codeData = findById(id);
			codeData.setEnabled(false);
			codeDataRepo.save(codeData);
		}
	}

	public void enableList(List<Long> ids) {
		if (ObjectUtils.isEmpty(ids)) {
			return;
		}

		for (Long id : ids) {
			CodeData codeData = findById(id);
			codeData.setEnabled(true);
			codeDataRepo.save(codeData);
		}
	}

	private CodeData findById(Long id) {
		return codeDataRepo.findById(id)
				.orElseThrow(() -> new BizException("DATA_NOT_FOUND", "CodeData id: " + id + "is not found!!", new Property("model", "CodeData"), new Property("id", id)));
	}

	public List<CodStpGroupItem> getGroupItemList(String codeType, CodStpGroupItemIn input) {

		Assert.notNull(codeType, "Code Type is required!!");
		Assert.notNull(input.getCompanyId(), "Comaney is required!!");
		QCodeData table = QCodeData.codeData;

		// If company don't have group1 code, Create group1 code...
		if ("MACHINE_DOWNTIME_REASON_GROUP1".equals(codeType)) {
			if (queryFactory.selectFrom(table).where(table.codeType.eq("MACHINE_DOWNTIME_REASON_GROUP1").and(table.companyId.eq(input.getCompanyId()))).fetchCount() == 0) {
				initGroup1CodeData(input.getCompanyId());
			}
		}

		List<CodStpGroupItem> result = queryFactory.select(Projections.constructor(CodStpGroupItem.class, table.id, table.code, table.title, table.codeType, table.companyId)).from(table)
				.where(table.codeType.eq(codeType).and(table.companyId.eq(input.getCompanyId()))).fetch();

		return result;
	}

}