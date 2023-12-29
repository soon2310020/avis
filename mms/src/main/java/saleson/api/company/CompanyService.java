package saleson.api.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.emoldino.framework.repository.Q;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.api.supplychain.resource.base.product.repository.partstat.PartStatRepository;
import com.emoldino.api.supplychain.resource.base.product.util.ProductUtils;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DataUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.types.Predicate;

import io.jsonwebtoken.lang.Assert;
import saleson.api.accessHierachy.AccessHierarchyRepository;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.company.payload.SupplierGetIn;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataRequest.DataRequestService;
import saleson.api.location.LocationRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.StorageType;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.JPQLQueryUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.model.*;
import saleson.model.data.CompanyLiteData;
import saleson.model.data.GroupCompanyData;
import saleson.model.data.MiniComponentData;

@Service
public class CompanyService {

	@Autowired
	private CompanyRepository repo;

	@Autowired
	private DataRequestService dataRequestService;
	@Autowired
	private TabTableDataRepository tabTableDataRepository;

	@Autowired
	private TabTableRepository tabTableRepository;

	@Value("${customer.server.name}")
	private String serverName;

	public Page<Company> findAll(Predicate predicate, Pageable pageable) {
		Page<Company> page = repo.findAllWithTotalMoldCount(predicate, pageable);
		loadUpperTierCompanies(page.getContent());
		return page;
	}
	public List<Long> findAllIds(CompanyPayload payload){
		payload.setIsDefaultTab(true);
		if(payload.getTabId() != null) {
			Optional<TabTable> tabTableOptional = tabTableRepository.findById(payload.getTabId());
			if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
				List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payload.getTabId());
				List<Long> idList = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
				payload.setIds(idList);
				payload.setIsDefaultTab(tabTableOptional.get().getIsDefaultTab());
			}
		}
		BooleanBuilder builder = new BooleanBuilder(payload.getPredicate());
		JPQLQuery<Long> query = BeanUtils.get(JPAQueryFactory.class)
				.select(Projections.constructor(Long.class, Q.company.id))
				.from(Q.company);
		query.where(builder);
		QueryResults<Long> results = query.fetchResults();
		return results.getResults();
	}

	public Page<Company> findAllOrderByName(CompanyPayload payload, Pageable pageable) {
		return repo.findAllOrderByName(payload.getPredicate(), pageable);
	}

	public List<MiniComponentData> findAllMiniData(String type) {
		List<Company> companies = new ArrayList<>();
		if (type == null)
			companies = repo.findAll();
		else {
			CompanyType companyType = CompanyType.IN_HOUSE;
			if (type.equalsIgnoreCase("Supplier"))
				companyType = CompanyType.SUPPLIER;
			else if (type.equalsIgnoreCase("ToolMaker"))
				companyType = CompanyType.TOOL_MAKER;
			companies = repo.findByCompanyType(companyType);
		}
		//filter by company tree
		if (AccessControlUtils.isAccessFilterRequired()) {
			List<Long> companyIdAccessList = AccessControlUtils.getAllAccessibleCompanyIds();
			if (!companyIdAccessList.isEmpty())
				companies = companies.stream().filter(c -> companyIdAccessList.contains(c.getId())).collect(Collectors.toList());
		}
		List<MiniComponentData> miniComponentData = companies.stream().filter(c -> !c.isEmoldino() && c.isEnabled()).map(x -> {
			MiniComponentData component = new MiniComponentData();
			component.setId(x.getId());
			component.setName(x.getName());
			return component;
		}).collect(Collectors.toList());
		return miniComponentData;
	}

	public List<MiniComponentData> findAllMiniDataFiltered(String type, boolean isAll) {
		return findAllMiniDataFilteredCustom(type != null, null, isAll);
	}

	public List<MiniComponentData> findAllMiniDataFilteredCustom(String type, DashboardGeneralFilter filter) {
		return findAllMiniDataFilteredCustom(type != null, filter, false);
	}

	private List<MiniComponentData> findAllMiniDataFilteredCustom(boolean isToolmaker, DashboardGeneralFilter filter, boolean isAll) {
		List<Company> companies = repo.findAllByGeneralFilter(isToolmaker, filter, isAll);
		//filter by company tree
		if (AccessControlUtils.isAccessFilterRequired()) {
			List<Long> companyIdAccessList = AccessControlUtils.getAllAccessibleCompanyIds();
			if (!companyIdAccessList.isEmpty())
				companies = companies.stream().filter(c -> companyIdAccessList.contains(c.getId())).collect(Collectors.toList());
		}
		List<MiniComponentData> miniComponentData = companies.stream().map(x -> {
			MiniComponentData component = new MiniComponentData();
			component.setId(x.getId());
			component.setName(x.getName());
			return component;
		}).collect(Collectors.toList());
		return miniComponentData;
	}

	public Company findById(Long id) {
		Optional<Company> category = repo.findById(id);
		if (category.isPresent()) {
			return category.get();
		} else {
			return null;
		}

	}

	@Transactional
	public void save(Company company) {
		save(company, null);
	}

	@Transactional
	public Company save(Company company, Long dataRequestId) {
		boolean newObj = company.getId() == null;
		Company companyNew = repo.save(company);

		if (dataRequestId != null) {
			dataRequestService.saveDataRequestObject(dataRequestId, companyNew.getId(), ObjectType.COMPANY);

		}
//		Write history update add
		if (newObj) {
			BeanUtils.get(VersioningService.class).writeHistory(companyNew);
		} else {
			dataRequestService.completeDataCompletion(company.getId(), ObjectType.COMPANY);
		}
		//update data completion rate
		BeanUtils.get(DataCompletionRateService.class).updateCompanyCompletionRateByObjectType(ObjectType.COMPANY, company.getId());
		//update data completion order
		BeanUtils.get(DataCompletionRateService.class).updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.COMPANY, company.getId());

		return companyNew;
	}

	@Transactional
	public void saveMultipart(Company company, CompanyPayload payload) {
		save(company);
		if(payload.getFiles() != null) {
			BeanUtils.get(FileStorageService.class).save(new FileInfo(StorageType.COMPANY_PICTURE, company.getId(), payload.getFiles()));
		}
	}

	@Transactional
	public void deleteById(Long id) {
		Optional<Company> optional = repo.findById(id);

		if (optional.isPresent()) {
			Company company = optional.get();

			boolean exists = BeanUtils.get(LocationRepository.class).existsByCompanyId(company.getId());

			if (exists) {
				throw new RuntimeException("You can not delete it because it has a company in use.");
			} else {
				repo.deleteById(id);
				//update data completion rate
				BeanUtils.get(DataCompletionRateService.class).updateCompanyCompletionRateByObjectType(ObjectType.COMPANY, company.getId());
			}
		}
	}

	/**
	 * 회사코드 중복 조회
	 * @param companyCode
	 * @return
	 */
	public boolean existsCompanyCode(String companyCode, Long id) {
		if (id != null)
			return repo.existsCompaniesByCompanyCodeAndIdNot(companyCode, id);
		return repo.existsCompaniesByCompanyCode(companyCode);

	}

	public boolean existsCompanyName(String companyName, Long id) {
		if (id != null)
			return repo.existsCompaniesByNameAndIdNot(companyName, id);
		return repo.existsCompaniesByName(companyName);
	}

	public ApiResponse validCompany(CompanyPayload payload, Long id) {
		ValueUtils.assertNotEmpty(payload.getCompanyType(), "company_type");
		ValueUtils.assertNotEmpty(payload.getName(), "company_name");
		ValueUtils.assertNotEmpty(payload.getCompanyCode(), "company_code");

		payload.setCompanyCode(StringUtils.trimWhitespace(payload.getCompanyCode()));
		payload.setName(StringUtils.trimWhitespace(payload.getName()));
		boolean existsCompanyCode = existsCompanyCode(payload.getCompanyCode(), id);
//		boolean existsCompanyName = existsCompanyName(payload.getName(), id);
		boolean isDyson = serverName.equalsIgnoreCase("dyson");

		if (existsCompanyCode) {
			return new ApiResponse(false, "Company ID is already registered in the system");
		}
/*
		if (!isDyson && existsCompanyName) {
			return new ApiResponse(false, "Company Name is already registered in the system");
		}
*/
		if(!StringUtils.isEmpty(payload.getPhone()) && !saleson.common.util.DataUtils.isPhoneNumberFull(payload.getPhone())){
			throw DataUtils.newDataValueInvalidException("Company", "phone_number", payload.getPhone());
		}
		if(!StringUtils.isEmpty(payload.getEmail()) && !EmailValidator.getInstance().isValid(payload.getEmail())){
			throw DataUtils.newDataValueInvalidException(Company.class, "email", payload.getEmail());
		}

		return null;
	}

	public GroupCompanyData findAllCompaniesGroupBy() {
		List<Company> companies = repo.findByEnabled(true);
		List<Company> companiesInHouse = companies.stream().filter(x -> x.getCompanyType().equals(CompanyType.IN_HOUSE)).collect(Collectors.toList());
		List<Company> companiesSupplier = companies.stream().filter(x -> x.getCompanyType().equals(CompanyType.SUPPLIER)).collect(Collectors.toList());
		List<Company> companiesToolMaker = companies.stream().filter(x -> x.getCompanyType().equals(CompanyType.TOOL_MAKER)).collect(Collectors.toList());
		return GroupCompanyData.builder().companiesInHouse(companiesInHouse).companiesSupplier(companiesSupplier).companiesToolMaker(companiesToolMaker).build();
	}

	public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
		try {
			List<Company> companies = repo.findByIdIn(dto.getIds());
			companies.forEach(company -> {
				company.setEnabled(dto.isEnabled());
				save(company);
				BeanUtils.get(VersioningService.class).writeHistory(findById(company.getId()));
			});
			return ApiResponse.success(CommonMessage.OK, companies);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	public ApiResponse getCompanyList(List<String> companyCodeList, Pageable pageable, String searchText) {
		try {
			List<Company> companies = repo.getListCompany(companyCodeList, pageable, searchText);
			return ApiResponse.success(CommonMessage.OK, companies);
		} catch (Exception e) {
			return ApiResponse.error(e.getMessage());
		}
	}

	public Page<Company> getSuppliers(SupplierGetIn input, Pageable pageable) {
		Assert.notNull(input.getProductId(), "productId is required!!");
		Assert.notNull(input.getPeriodValue(), "periodValue is required!!");
		Long productId = input.getProductId();

		Page<Company> page = repo.findByProject(productId, input.getPartId(), input.getMoldId(), pageable);
		page.getContent().forEach(supplier -> {
			List<Long> supplierId = Arrays.asList(supplier.getId());

			// Counts
			supplier.setPartCount(BeanUtils.get(PartRepository.class).countByProduct(productId, supplierId, null));
			supplier.setMoldCount(BeanUtils.get(MoldRepository.class).countByProduct(productId, null, supplierId));

			// Production
			supplier.setTotalProduced(0L);
			BeanUtils.get(PartStatRepository.class).findAllWeeklyByProduct(input.getPeriodValue(), productId, input.getPartId(), supplierId, input.getMoldId())//
					.forEach(stat -> {
						supplier.setTotalProduced(supplier.getTotalProduced() + stat.getProducedVal());
					});
			supplier.setTotalMaxCapacity(
					ProductUtils.getWeeklyCapa(input.getPeriodValue(), productId, null, Arrays.asList(supplier.getId()), ValueUtils.toLong(supplier.getTotalProduced(), 0L)));
			ProductUtils.setPredicted(supplier, input.getPeriodValue(), supplier.getTotalProduced());

//			Page<Part> partPage = partRepository.findAllByProduct(filter.getProjectId(), supplier.getId(), null, PageRequest.of(0, 99999));
//
//			List<Long> partIds = partPage.getContent().stream().map(p -> p.getId()).collect(Collectors.toList());
//			List<StatisticsPartData> statisticsPartDataList = partProjectProducedRepository.getPartProducedByProjectId(filter.getProjectId(), partIds);
//			partPage.getContent().stream().forEach(part -> {
//				statisticsPartDataList.stream().forEach(sp -> {
//					if (part.getId().equals(sp.getPartId())) {
//						part.setStatisticsPartData(sp);
//					}
//				});
//			});
//			List<Part> parts = partPage.getContent();
//			// TODO
//			Integer produced = parts.stream().mapToInt(Part::getTotalProduced).sum();
//			supplier.setTotalProduced(produced);
//			supplier.setTotalMaxCapacity(produced + 230);
//			ProductUtils.setPredicted(supplier, null);
//			supplier.setPartCount(parts.size());
//			supplier.setMoldCount(moldRepository.countByProduct(filter.getProjectId(), null, supplier.getId()));
		});
		return page;
	}

	public Page<Company> getSuppliersByBrand(SupplierGetIn input, Pageable pageable) {
		Assert.notNull(input.getProductId(), "brandId is required!!");
		Assert.notNull(input.getPeriodValue(), "periodValue is required!!");
		Long brandId = input.getProductId();

		Page<Company> page = repo.findByBrand(brandId, input.getPartId(), input.getMoldId(), pageable);
		page.getContent().forEach(supplier -> {
			List<Long> supplierId = Arrays.asList(supplier.getId());

			// Counts
			supplier.setPartCount(BeanUtils.get(PartRepository.class).countByBrand(brandId, supplierId, null));
			supplier.setMoldCount(BeanUtils.get(MoldRepository.class).countByBrand(brandId, null, supplierId));

			// Production
			supplier.setTotalProduced(0L);
			BeanUtils.get(PartStatRepository.class).findAllWeeklyByBrand(input.getPeriodValue(), brandId, input.getPartId(), supplierId, input.getMoldId()).forEach(stat -> {
				supplier.setTotalProduced(supplier.getTotalProduced() + stat.getProducedVal());
			});
			supplier.setTotalMaxCapacity(
					ProductUtils.getWeeklyCapaByBrand(input.getPeriodValue(), brandId, null, supplier.getId(), ValueUtils.toLong(supplier.getTotalProduced(), 0L)));
			ProductUtils.setPredicted(supplier, input.getPeriodValue(), supplier.getTotalProduced());
		});
		return page;
	}

	private void loadUpperTierCompanies(List<Company> companies) {
		companies.forEach(company -> {
			Optional<AccessHierarchy> optional = BeanUtils.get(AccessHierarchyRepository.class).findFirstByCompanyId(company.getId());
			if (optional.isPresent()) {
				List<Long> parentIds = optional.get().getAccessCompanyParentRelations().stream().map(AccessCompanyRelation::getCompanyParentId).collect(Collectors.toList());
				company.setUpperTierCompanies(repo.findByIdIn(parentIds).stream().map(CompanyLiteData::new).collect(Collectors.toList()));
			}
		});
	}

}
