package saleson.api.part;

import static saleson.service.util.NumberUtils.assertNumberOrNull;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import saleson.api.category.CategoryRepository;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.configuration.CustomFieldValueService;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.dataCompletionRate.DataCompletionRateService;
import saleson.api.dataRequest.DataRequestService;
import saleson.api.mold.MoldPartRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.payload.PartPayload;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.api.statistics.StatisticsPartRepository;
import saleson.api.statistics.StatisticsRepository;
import saleson.api.tabTable.TabTableDataRepository;
import saleson.api.tabTable.TabTableRepository;
import saleson.api.tabbedFilter.TabbedOverviewGeneralFilterService;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.api.versioning.service.VersioningService;
import saleson.common.constant.CommonMessage;
import saleson.common.constant.SpecialSortProperty;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.CountryCode;
import saleson.common.enumeration.DashboardChartType;
import saleson.common.enumeration.OTDStatus;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.OperatingStatus;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.StorageType;
import saleson.common.payload.ApiResponse;
import saleson.common.service.FileInfo;
import saleson.common.service.FileStorageService;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.ExcelUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.dto.BatchUpdateDTO;
import saleson.dto.CustomField.CustomFieldListDTO;
import saleson.model.Category;
import saleson.model.DashboardGeneralFilter;
import saleson.model.MoldPart;
import saleson.model.Part;
import saleson.model.PartCustomFieldValue;
import saleson.model.QPart;
import saleson.model.ResinCodeChange;
import saleson.model.TabTable;
import saleson.model.TabTableData;
import saleson.model.customField.CustomFieldValue;
import saleson.model.data.ChartData;
import saleson.model.data.CountryCodeData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.PartWithStatisticsData;
import saleson.model.data.StatisticsPartData;
import saleson.model.data.cycleTime.CycleTimeOverviewData;
import saleson.model.data.dashboard.DashboardTabData;
import saleson.model.data.dashboard.otd.OTDData;
import saleson.model.data.dashboard.otd.OTDDetailsData;
import saleson.model.data.dashboard.totalPart.ProductData;
import saleson.model.data.dashboard.totalPart.TotalPartChartData;
import saleson.model.data.productivity.ProductivityOverviewData;
import saleson.service.util.DateTimeUtils;
import saleson.service.util.NumberUtils;

@Service
public class PartService {
    static String SIZE_PATTERN ="^\\d*(\\.\\d+)?\\s*x\\s*\\d*(\\.\\d+)?\\s*x\\s*\\d*(\\.\\d+)?$";
    @Autowired
    PartRepository partRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MoldRepository moldRepository;

    @Autowired
    MoldPartRepository moldPartRepository;

    @Autowired
    StatisticsPartRepository statisticsPartRepository;
    @Autowired
    StatisticsRepository statisticsRepository;
    @Autowired
    ResinCodeChangeRepository resinCodeChangeRepository;

    @Autowired
    VersioningService versioningService;

    @Autowired
    ExcelUtils excelUtils;

    @Autowired
    CustomFieldValueService customFieldValueService;

    @Autowired
    DataCompletionRateService dataCompletionRateService;
    @Autowired
    FileStorageService fileStorageService;

    @Autowired
    DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Autowired
    private TabbedOverviewGeneralFilterService tabbedOverviewGeneralFilterService;

    @Autowired
    private DataRequestService dataRequestService;

    @Autowired
    private TabTableRepository tabTableRepository;
    @Autowired
    private TabTableDataRepository tabTableDataRepository;
    @Autowired
    private ColumnTableConfigService columnTableConfigService;

	@Transactional
	public Page<Part> findAll(Predicate predicate, Pageable pageable) {
		return partRepository.findAll(predicate, pageable);
	}

    /**
     * 통계 데이터 기준 PART 생산량 정보를 포함해서 조회
     *
     * @param payload
     * @param pageable
     * @return
     */
	@Transactional
    public Page<Part> findAllWithStatisticsPart(PartPayload payload, Pageable pageable) {
        BooleanBuilder predicate = new BooleanBuilder(payload.getPredicate());
        if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
            predicate.and(dashboardGeneralFilterUtils.getPartFilter(QPart.part));
        if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected()) {
            predicate.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getPartFilter());
        }
        String property[] = {""};
        Sort.Direction[] directions = {Sort.Direction.DESC};
        pageable.getSort().forEach(order -> {
            property[0] = order.getProperty();
            directions[0] = order.getDirection();
        });

        Page<Part> page;
        if (SpecialSortProperty.partSortProperties.contains(property[0])) {
            Page<PartWithStatisticsData> partWithStatisticsDataPage;
            if (property[0].equalsIgnoreCase("totalMolds") || property[0].equalsIgnoreCase("totalProduced")
                    || property[0].equalsIgnoreCase("weight")) {
                partWithStatisticsDataPage = partRepository.findWithSpecialSort(payload, pageable);
            } else {
                OperatingStatus op = OperatingStatus.WORKING;
                if (property[0].equalsIgnoreCase("inactiveMolds"))
                    op = OperatingStatus.NOT_WORKING;
                else if (property[0].equalsIgnoreCase("idleMolds"))
                    op = OperatingStatus.IDLE;
                else if (property[0].equalsIgnoreCase("disconnectedMolds"))
                    op = OperatingStatus.DISCONNECTED;

                Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
                if (directions[0].isDescending()) {
                    partWithStatisticsDataPage = new PageImpl<>(partRepository.findPartSortByMoldOPDesc(op, unsortedPageable), pageable, partRepository.count(predicate));
                } else {
                    partWithStatisticsDataPage = new PageImpl<>(partRepository.findPartSortByMoldOPAsc(op, unsortedPageable), pageable, partRepository.count(predicate));
                }
            }
            List<Part> partList = partWithStatisticsDataPage.getContent().stream().map(x -> x.getPart()).collect(Collectors.toList());
            page = new PageImpl<>(partList, partWithStatisticsDataPage.getPageable(), partWithStatisticsDataPage.getTotalElements());
        } else if (property[0].startsWith(SpecialSortProperty.customFieldSort)) {
            List<PartCustomFieldValue> partCustomFieldValues = partRepository.findPartCustomFieldValue(predicate, pageable);
            List<Part> parts = partCustomFieldValues.stream().map(PartCustomFieldValue::getPart).collect(Collectors.toList());
            page = new PageImpl<>(parts, pageable, partRepository.count(predicate));
        } else if (property[0].contains(SpecialSortProperty.machineCode)) {
            page = partRepository.getPartOrderByMachineId(predicate, pageable);
        } else if (property[0].contains(SpecialSortProperty.category)) {
            page = partRepository.getPartOrderByCategoryName(predicate, pageable);
        } else {
            page = partRepository.findAll(predicate, pageable);
        }

        if (AccessControlUtils.isAccessFilterRequired()) {
            List<Long> moldIds = moldRepository.getMoldIdsSubQuery();
            page.getContent().stream()
                    .forEach(p -> {
                        p.setMoldParts(p.getMoldParts().stream().filter(x -> moldIds.contains(x.getMoldId())).distinct().collect(Collectors.toSet()));
                    });
        }

        List<Long> partIds = page.getContent().stream()
                .map(p -> p.getId())
                .collect(Collectors.toList());

        List<StatisticsPartData> statisticsPartDataList = moldRepository.getStatisticsPartData(partIds, payload);

        // Compare with previous period
        if (payload != null && payload.getTimePeriod() != null) {
            PartPayload partPayload = new PartPayload();
            partPayload.setTimePeriod(DateTimeUtils.getPreviousTimePeriod(payload.getTimePeriod()));
            List<StatisticsPartData> previousStatisticsPartDataList = moldRepository.getStatisticsPartData(partIds, partPayload);
            statisticsPartDataList.forEach(statisticsPartData -> {
                StatisticsPartData previousData = previousStatisticsPartDataList.stream().filter(x -> x.getPartId().equals(statisticsPartData.getPartId())).findAny().orElse(null);
                if (previousData != null) {
                    if (statisticsPartData.getProducedQuantity() >= previousData.getProducedQuantity())
                        statisticsPartData.setIsUpper(true);
                    else statisticsPartData.setIsUpper(false);
                }
            });
        }
        AtomicReference<Set<MoldPart>> moldTemps = new AtomicReference<>(new HashSet<>());
        page.getContent().stream()
                .forEach(p -> {
                    if (p.getMoldParts()!=null)
                    {
                        moldTemps.set(p.getMoldParts().stream().filter(mp -> !mp.getMold().isDeleted()).collect(Collectors.toSet()));
                        p.setMoldParts(moldTemps.get());
                    }

                    statisticsPartDataList.stream()
                            .forEach(sp -> {
                                if (p.getId().equals(sp.getPartId())) {
                                    p.setStatisticsPartData(sp);
                                }
                            });
                });
        loadValueCustomField(page.getContent());
        return page;
    }

	@Transactional
    public List<Long> findAllIdWithStatisticsPart(PartPayload payload) {
        changeTabPayload(payload);
        BooleanBuilder predicate = new BooleanBuilder(payload.getPredicate());
        if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
            predicate.and(dashboardGeneralFilterUtils.getPartFilter(QPart.part));
        if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected()) {
            predicate.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getPartFilter());
        }
        BooleanBuilder builder = new BooleanBuilder(predicate);
        JPQLQuery<Long> query= BeanUtils.get(JPAQueryFactory.class)
                .select(Projections.constructor(Long.class, Q.part.id)).from(Q.part).where(builder);
        QueryResults<Long> results = query.fetchResults();
        return results.getResults();
    }

    private void loadValueCustomField(List<Part> list) {
        //get data for custom field
        Map<Long, Map<Long, List<CustomFieldValue>>> mapValueCustomField = customFieldValueService.mapValueCustomField(ObjectType.PART, list.stream().map(m -> m.getId()).collect(Collectors.toList()));
        list.stream().forEach(mold -> {
            if (mapValueCustomField.containsKey(mold.getId())) {
                mold.setCustomFieldValueMap(mapValueCustomField.get(mold.getId()));
            }
        });
    }

    public Part findById(Long id) {
        Optional<Part> category = partRepository.findById(id);
        if (category.isPresent()) {
            return category.get();
        } else {
            return null;
        }
    }

	@Transactional
    public Part findByPartCode(String partCode) {
        return partRepository.findFirstByPartCode(partCode);
    }

	@Transactional
    public List<MiniComponentData> findAllMiniDataByGeneralFilter() {
        //list ids from dashboard general filter
        List<Long> ids = dashboardGeneralFilterUtils.getPartIds_Old();
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }

        if (!SecurityUtils.isAdmin()) {
            PartPayload payload = new PartPayload();
            payload.setStatus("active");
            if (CollectionUtils.isNotEmpty(ids)) {
                payload.setIds(ids);
            }
            Page<Part> pageContent = findAll(payload.getPredicate(), DataUtils.getPageable(0, Integer.MAX_VALUE, null));
            return pageContent.stream().map(x -> {
                MiniComponentData component = new MiniComponentData();
                component.setId(x.getId());
                component.setCode(x.getPartCode());
                component.setName(x.getName());
                return component;
            }).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            return partRepository.findAllPartIdPartNameByIdIn(ids);
        }
        return partRepository.findAllPartIdPartName();
    }

	@Transactional
    public List<MiniComponentData> findAllMiniData() {

        if (!SecurityUtils.isAdmin()) {
            PartPayload payload = new PartPayload();
            payload.setStatus("active");
            Page<Part> pageContent = findAll(payload.getPredicate(), DataUtils.getPageable(0, Integer.MAX_VALUE, null));
            return pageContent.stream().map(x -> {
                MiniComponentData component = new MiniComponentData();
                component.setId(x.getId());
                component.setCode(x.getPartCode());
                component.setName(x.getName());
                return component;
            }).collect(Collectors.toList());
        }
        return partRepository.findAllPartIdPartName();
    }

	@Transactional
    public List<MiniComponentData> findAllMiniDataFiltered(boolean isAll) {
        List<Part> parts = partRepository.findAllByGeneralFilter(isAll);
        return parts.stream().map(x -> {
            MiniComponentData component = new MiniComponentData();
            component.setId(x.getId());
            component.setCode(x.getPartCode());
            component.setName(x.getName());
            return component;
        }).collect(Collectors.toList());
    }

	@Transactional
    public List<MiniComponentData> findAllMiniDataFilteredCustom(DashboardGeneralFilter filter) {
        List<Part> parts = partRepository.findAllByGeneralFilter(filter);
        return parts.stream().map(x -> {
            MiniComponentData component = new MiniComponentData();
            component.setId(x.getId());
            component.setCode(x.getPartCode());
            component.setName(x.getName());
            return component;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Part save(Part part) {
        PartPayload payload = null;
        return save(part, payload);
    }

	@Transactional
    public Part save(Part part, PartPayload payload) {
        Optional<Category> data = part.getCategoryId() != null ? categoryRepository.findById(part.getCategoryId()) : Optional.empty();

        if (data.isPresent()) {
            part.setCategory(data.get());
        }
        boolean newObj = part.getId() == null;
        partRepository.save(part);
        if (newObj) {
            versioningService.writeHistory(part);
        } else {
            dataRequestService.completeDataCompletion(part.getId(), ObjectType.PART);
        }

        //update company completion rate
        if (CollectionUtils.isNotEmpty(part.getMoldParts())) {
            part.getMoldParts().forEach(moldPart -> {
                if (moldPart.getMold() != null && moldPart.getMold().getLocation() != null && moldPart.getMold().getLocation().getCompanyId() != null)
                    dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, moldPart.getMold().getLocation().getCompanyId());
            });
        }
        if (payload != null && payload.getPartPictureFiles() != null) {
            fileStorageService.save(new FileInfo(StorageType.PART_PICTURE, part.getId(), payload.getPartPictureFiles()));
        }
        if (payload != null && payload.getDataRequestId() != null) {
            dataRequestService.saveDataRequestObject(payload.getDataRequestId(), part.getId(), ObjectType.PART);
        }

        //update data completion order
        dataCompletionRateService.updateCompletionOrder(SecurityUtils.getUserId(), ObjectType.PART, part.getId());
        return part;
    }

    @Transactional
    public void save(Part part, String previousPartCode) {
        save(part, previousPartCode, null);
    }

    @Transactional
    public void save(Part part, String previousPartCode, PartPayload payload) {
        save(part, payload);

        // 통계 쪽 PartCode를 신규 partCode로 일괄변경

        statisticsPartRepository.updatePartCodeAll(previousPartCode, part.getPartCode());

        //update company completion rate
        if (CollectionUtils.isNotEmpty(part.getMoldParts())) {
            part.getMoldParts().forEach(moldPart -> {
                dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, moldPart.getMold().getLocation().getCompanyId());
            });
        }

    }


    @Transactional
    public void deleteById(Long id) {
        Optional<Part> optional = partRepository.findById(id);

        if (optional.isPresent()) {
            Part part = optional.get();
/* M01

			boolean exists = moldRepository.existsByPartId(part.getId());
*/
            boolean exists = true;
            if (exists) {
                throw new RuntimeException("You can not delete it because it has a part in use.");
            } else {
                partRepository.deleteById(id);
            }
            customFieldValueService.deleteCustomFieldValueByObjectId(id);
        }
    }

	@Transactional
    public boolean existsPartCode(String partCode) {
        return partRepository.existsPartByPartCode(partCode);
    }

	@Transactional
    public boolean existsByCategoryId(Long categoryId) {
        return partRepository.existsByCategoryId(categoryId);
    }

    public ByteArrayOutputStream exportExcelPartList(PartPayload payload, Pageable pageable) {
        String timeRange = "All time";
        if (payload.getTimePeriod() != null) {
            if (payload.getTimePeriod().startsWith("W"))
                timeRange = "Week " + payload.getTimePeriod().substring(5) + " " + payload.getTimePeriod().substring(1, 5);
            else if (payload.getTimePeriod().startsWith("M"))
                timeRange = Month.of(Integer.valueOf(payload.getTimePeriod().substring(5))).getDisplayName(TextStyle.FULL, Locale.US) + " " + payload.getTimePeriod().substring(1, 5);
            else timeRange = payload.getTimePeriod().substring(1);
        } else if (payload.getStartDate() != null && payload.getEndDate() != null) {
            timeRange = payload.getStartDate() + " - " + payload.getEndDate();
        }
        Pageable pageableNew = Pageable.unpaged();
        if (pageable.getSort() != null) {
            pageableNew = PageRequest.of(0, Integer.MAX_VALUE, pageable.getSort());
        }
        Page<Part> pageContent = findAllWithStatisticsPart(payload, pageableNew);
        List<Part> parts = pageContent.getContent();
        return excelUtils.exportExcelPartListNew(parts, timeRange);
    }

    public ProductivityOverviewData findProductivityOverviewData(List<Long> moldIds, ProductivitySearchPayload payload) {
        return partRepository.findProducedQuantity(moldIds, payload);
    }

    public CycleTimeOverviewData findCycleTimeOverviewData(ProductivitySearchPayload payload) {
        return partRepository.findCycleTimeOverviewData(payload);
    }

	@Transactional
    public List<CountryCodeData> findCountriesByPartId(Long partId) {
        List<String> countryCodesString = partRepository.findCountriesByPartId(partId);
        List<CountryCode> contryCodes = Stream.of(CountryCode.values()).filter(x -> countryCodesString.contains(x.getCode())).collect(Collectors.toList());
        List<CountryCodeData> result = new ArrayList<>();
        contryCodes.forEach(data -> result.add(CountryCodeData.builder().code(data.getCode()).title(data.getTitle()).build()));
        return result;
    }

    public List<MiniComponentData> getExistsCodes(List<String> partCodes) {
        if (partCodes != null) {
            partCodes = partCodes.stream().map(c -> StringUtils.trimWhitespace(c)).collect(Collectors.toList());
        }
        return partRepository.findExistsPartCodes(partCodes);
    }

    public List<MiniComponentData> getExistsNames(List<String> names) {
        return partRepository.findExistsPartNames(names);
    }

    public OTDData findOtd(Long partId, String week, Pageable pageable) {
        Part part = findById(partId);
        if (part == null) return null;

        OTDData result = OTDData.builder().build();

        result.setDemand(part.getWeeklyDemand() != null ? part.getWeeklyDemand() : 1);
        result.setOutput(partRepository.findPartProducedByWeek(partId, week));
        result.setOverall(NumberUtils.roundOffNumber(result.getOutput() * 100.0 / result.getDemand()));

        OTDData details = getListDetailsOtd(partId, week, pageable); // weekly output list
        result.setTotalPages(details.getTotalPages());
        result.setNumber(details.getNumber());

        List<ChartData> supplierCavityParts = partRepository.findSupplierTotalCavityPart(partId);
        Integer totalCavityParts = supplierCavityParts.stream().map(x -> x.getData()).reduce(0, Integer::sum);
        List<OTDDetailsData> otdDetailsData = new ArrayList<>(); // final details list
        supplierCavityParts.forEach(data -> {
            OTDDetailsData detailsData = details.getDetails().stream().filter(x -> x.getCompanyId().equals(data.getCompanyId())).findAny().orElse(null);
            if (detailsData != null) {
                detailsData.setWeeklyDemand(data.getData() * result.getDemand() / (totalCavityParts != 0 ? totalCavityParts : 1));
                detailsData.setWeeklyOTD(
                        NumberUtils.roundOffNumber(detailsData.getWeeklyOutput() * 100.0 / (detailsData.getWeeklyDemand() != 0 ? detailsData.getWeeklyDemand() : 1)));
                detailsData.setRemainingCapacity(0);
            } else {
                detailsData = OTDDetailsData.builder()
                        .companyId(data.getCompanyId())
                        .companyName(data.getCompanyName())
                        .weeklyOutput(0)
                        .weeklyDemand(data.getData() * result.getDemand() / (totalCavityParts != 0 ? totalCavityParts : 1))
                        .weeklyOTD(0.0)
                        .remainingCapacity(0)
                        .build();
            }
            otdDetailsData.add(detailsData);
        });

        if (DateUtils.getYearWeek(DateUtils.getToday()).equals(week)) {
            int remainingDaysOfWeek = DateTimeUtils.getRemainingDaysOfWeek();
            if (remainingDaysOfWeek != 0) {
                List<ChartData> remainingCapacityList = partRepository.findWeekRemainingCapacityToolings(partId, remainingDaysOfWeek);
                otdDetailsData.forEach(data -> {
                    ChartData remainingCapacity = remainingCapacityList.stream()
                            .filter(x -> x.getCompanyId().equals(data.getCompanyId())).findAny().orElse(null);
                    if (remainingCapacity != null) {
                        data.setRemainingCapacity(remainingCapacity.getData());
                    }
                });
            }
        }

        otdDetailsData.forEach(data -> {
            if (data.getRemainingCapacity() >= data.getWeeklyDemand() - data.getWeeklyOutput())
                data.setOtdStatus(OTDStatus.LOW);
            else data.setOtdStatus(OTDStatus.HIGH);
        });

        result.setDetails(otdDetailsData);
        return result;
    }

    public OTDData getListDetailsOtd(Long partId, String week, Pageable pageable) {
        OTDData result = partRepository.findListDetailsOtd(partId, week, pageable);
        return result;
    }

    public List<ChartData> findToolingProducedPart(Long partId, String week, Long companyId) {
        List<ChartData> result = partRepository.findToolingProducedPart(partId, companyId, week);
        return result;
    }

    public List<MiniComponentData> getListPart(List<String> partCodeList, String searchText, Long page, Long size) {
        return partRepository.findListPart(partCodeList, searchText, page, size);
    }

    public List<MiniComponentData> getListPartName() {
        return partRepository.findListPartName();
    }

    public ByteArrayOutputStream exportExcelImportTemplate() {
        return excelUtils.exportExcelPartImportTemplate();
    }


    public boolean existsCode(String code, Long id) {
        if (id != null)
            return partRepository.existsPartByPartCodeAndIdNot(code, id);
        return partRepository.existsPartByPartCode(code);
    }

    public boolean existsName(String name, Long id) {
        if (id != null)
            return partRepository.existsPartByNameAndIdNot(name, id);
        return partRepository.existsPartByName(name);
    }

    public ResponseEntity valid(PartPayload payload, Long id) {
        ValueUtils.assertNotEmpty(payload.getPartCode(), "part_id");
        ValueUtils.assertNotEmpty(payload.getName(), "part_name");

        payload.setPartCode(StringUtils.trimWhitespace(payload.getPartCode()));
        payload.setName(StringUtils.trimWhitespace(payload.getName()));
        boolean existsCode = existsCode(payload.getPartCode(), id);


        if (existsCode) {
            return ResponseEntity.badRequest().body("Part ID is already registered in the system.");
        }
        if (!StringUtils.isEmpty(payload.getSize())) {
            if (NumberUtils.isNumberOrNull(payload.getSize()))
                NumberUtils.assertGtValue(Double.valueOf(payload.getSize()), "part_volume_w_d_h", 0);
            else {
                Pattern regex = Pattern.compile(SIZE_PATTERN);
                Matcher matcher = regex.matcher(payload.getSize().trim());
                if (!matcher.matches()) {
                    throw com.emoldino.framework.util.DataUtils.newDataValueInvalidException("part", "part_volume_w_d_h", payload.getSize());
                }
            }
        }
        assertNumberOrNull("part", payload.getWeight(), "part_weight");
/*
        if (!NumberUtils.isNumberOrNull(payload.getWeight()))
            throw com.emoldino.framework.util.DataUtils.newDataValueInvalidException("part", "part_weight", payload.getWeight());
*/
        if (!StringUtils.isEmpty(payload.getWeight())) {
            NumberUtils.assertGteValue(Double.valueOf(payload.getWeight()), "part_weight", 0);
        }
        // require for other field by configuration
        BeanUtils.get(GeneralConfigService.class).validRequiredField(payload, ConfigCategory.PART);

        return null;
    }

    public ApiResponse changeStatusInBatch(BatchUpdateDTO dto) {
        try {
            List<Part> parts = partRepository.findAllById(dto.getIds());
            parts.forEach(part -> {
                part.setEnabled(dto.isEnabled());
                save(part);
                Part partFinal = findById(part.getId());
                versioningService.writeHistory(partFinal);
            });
            return ApiResponse.success(CommonMessage.OK, parts);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    public void delete(Part part) {
        part.setDeleted(true);
        partRepository.save(part);
        //update company completion rate
        if (CollectionUtils.isNotEmpty(part.getMoldParts())) {
            part.getMoldParts().forEach(moldPart -> {
                dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, moldPart.getMold().getLocation().getCompanyId());
            });
        }
    }

    public ApiResponse delete(Long id) {
        Part part = partRepository.getOne(id);
        if (part != null) {
            List<MoldPart> moldParts = moldPartRepository.findByPart(part);
            if (CollectionUtils.isEmpty(moldParts)) {
                delete(part);
                return ApiResponse.success(CommonMessage.OK, part);
            } else {
                return ApiResponse.error(CommonMessage.PART_IS_LINKED_WITH_TOOLING);
            }
        } else {
            return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
        }
    }

    public void restore(Part part) {
        part.setDeleted(false);
        partRepository.save(part);
        //update company completion rate
        if (CollectionUtils.isNotEmpty(part.getMoldParts())) {
            part.getMoldParts().forEach(moldPart -> {
                dataCompletionRateService.updateCompanyCompletionRateByObjectType(ObjectType.PART, moldPart.getMold().getLocation().getCompanyId());
            });
        }
    }

    public ApiResponse restore(Long id) {
        Part part = partRepository.getOne(id);
        if (part != null) {
            restore(part);
            return ApiResponse.success(CommonMessage.OK, part);
        } else {
            return ApiResponse.error(CommonMessage.OBJECT_NOT_FOUND);
        }
    }

    private static final String DELETED = "DELETED";
    private static final String NOT_DELETED = "NOT_DELETED";

    public ApiResponse deleteInBatch(BatchUpdateDTO dto) {
        Map<String, List<Long>> map = new HashMap<>();
        map.put(DELETED, new ArrayList<>());
        map.put(NOT_DELETED, new ArrayList<>());
        List<Part> parts = partRepository.findAllById(dto.getIds());
        parts.forEach(part -> {
            List<MoldPart> moldParts = moldPartRepository.findByPart(part);
            if (CollectionUtils.isEmpty(moldParts)) {
                delete(part);
                map.get(DELETED).add(part.getId());
            } else {
                map.get(NOT_DELETED).add(part.getId());
            }
        });

        if (CollectionUtils.isNotEmpty(map.get(DELETED))) {
            if (CollectionUtils.isNotEmpty(map.get(NOT_DELETED))) {
                return ApiResponse.success(CommonMessage.SOME_PARTS_ARE_LINKED_WITH_TOOLING, map);
            } else {
                return ApiResponse.success(CommonMessage.OK, map);
            }
        } else {
            return ApiResponse.error(CommonMessage.ALL_PARTS_ARE_LINKED_WITH_TOOLING);
        }
    }

    public ApiResponse assignToProject(List<PartPayload> payload) {
        try {
            List<Part> partToUpdate = new ArrayList<>();
            payload.forEach(p -> {
                Part part = partRepository.getOne(p.getId());
                if (p.getCategoryId() != null) {
                    part.setCategoryId(p.getCategoryId());
                    part.setCategory(categoryRepository.getOne(p.getCategoryId()));
                    part.setQuantityRequired(p.getQuantityRequired());
                } else {
                    part.setCategoryId(null);
                    part.setCategory(null);
                    part.setQuantityRequired(0L);
                }
                partToUpdate.add(part);
            });

            partRepository.saveAll(partToUpdate);
            return ApiResponse.success(CommonMessage.OK, partToUpdate);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public ApiResponse removeFromProject(Long partId) {
        try {
            Part part = partRepository.getOne(partId);
            if (part.getCategoryId() != null) {
                part.setCategoryId(null);
                part.setCategory(null);
                partRepository.save(part);
            }
            return ApiResponse.success(CommonMessage.OK, part);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    public DashboardTabData getTotalPartTabData(TabbedOverviewGeneralFilterPayload payload) {
        try {
            Long totalPartCount = partRepository.countByFilter(payload.getPartFilter());
            DashboardTabData data = new DashboardTabData(DashboardChartType.TOTAL_PARTS, totalPartCount.doubleValue(), null);
            //calculate trend
            Long currentPeriodPartCount = partRepository.countByFilter(payload.getPartFilterWithPeriod(true));
            Long previousPeriodPartCount = partRepository.countByFilter(payload.getPartFilterWithPeriod(false));
            data.setTrend((double) (currentPeriodPartCount - previousPeriodPartCount));
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return new DashboardTabData(DashboardChartType.TOTAL_PARTS);
        }
    }

    public ApiResponse getTotalPartChartData(TabbedOverviewGeneralFilterPayload payload, Pageable pageable) {
        try {
            Long totalPartCount = partRepository.countByFilter(payload.getPartFilter());
            TotalPartChartData data = new TotalPartChartData();
            data.setLevel1(totalPartCount / 3);
            data.setLevel2((totalPartCount / 3) * 2);

            Page<ProductData> page = categoryRepository.getProductPartData(payload, pageable);
            page.getContent().forEach(p -> p.setPercentage(NumberUtils.roundOffNumber(((double) p.getPartCount() / totalPartCount) * 100)));
            data.setData(page);

            return ApiResponse.success(CommonMessage.OK, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    @Transactional
    public void savePartByImport(PartPayload payload) {
        payload.setEnabled(true);
        CustomFieldListDTO customFieldListDTO = new CustomFieldListDTO();
        customFieldListDTO.setCustomFieldDTOList(payload.getCustomFieldDTOList());
        Part part;
        if (payload.getId() == null) {
            part = save(payload.getModel());
        } else {
            part = findById(payload.getId());
            if (part != null) {
                save(payload.getModel(part), part.getPartCode());
            }
        }
        if (part != null)
            customFieldValueService.editListCustomField(part.getId(), customFieldListDTO);
    }

    public void saveResinCodeChange(Long partId, String oldCode, String newCode) {
        try {
            ResinCodeChange resinCodeChange = new ResinCodeChange();
            resinCodeChange.setPartId(partId);
            resinCodeChange.setOldResinCode(oldCode);
            resinCodeChange.setNewResinCode(newCode);
            resinCodeChange.setTime(Instant.now());
            resinCodeChange.setMonth(DateUtils2.format(resinCodeChange.getTime(), DateUtils2.DatePattern.yyyyMM, DateUtils2.Zone.SYS));
            resinCodeChange.setDay(DateUtils2.format(resinCodeChange.getTime(), DateUtils2.DatePattern.yyyyMMdd, DateUtils2.Zone.SYS));
            resinCodeChange.setHour(DateUtils2.format(resinCodeChange.getTime(), DateUtils2.DatePattern.yyyyMMddHH, DateUtils2.Zone.SYS));
            resinCodeChange.setWeek(DateUtils.getYearWeek(resinCodeChange.getDay(), DateUtils2.DatePattern.yyyyMMdd));

            String time = DateUtils2.format(resinCodeChange.getTime(), DateUtils2.DatePattern.yyyyMMddHHmmss, DateUtils2.Zone.SYS);
            Double wact = statisticsRepository.getPartWACTBefore(partId, time);
            resinCodeChange.setCurrentWact(wact);

            resinCodeChangeRepository.save(resinCodeChange);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ApiResponse changeMaterialCode(PartPayload payload) {
        try {
            Part part = partRepository.getOne(payload.getId());
            saveResinCodeChange(payload.getId(), part.getResinCode(), payload.getResinCode());

            part.setResinCode(payload.getResinCode());
            partRepository.save(part);

            return ApiResponse.success(CommonMessage.OK, part);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    public void changeTabPayload(PartPayload payload) {
        payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.PART_SETTING));
        payload.setIsDefaultTab(true);
        if (payload.getTabId() != null) {
            Optional<TabTable> tabTableOptional = tabTableRepository.findById(payload.getTabId());
            if (tabTableOptional.isPresent() && (tabTableOptional.get().getIsDefaultTab() == null || !tabTableOptional.get().getIsDefaultTab())) {
                List<TabTableData> tabTableDataList = tabTableDataRepository.findAllByTabTableId(payload.getTabId());
                List<Long> idList = tabTableDataList.stream().map(TabTableData::getRefId).collect(Collectors.toList());
                payload.setIds(idList);
                payload.setIsDefaultTab(tabTableOptional.get().getIsDefaultTab());
            }
        }
    }

}
