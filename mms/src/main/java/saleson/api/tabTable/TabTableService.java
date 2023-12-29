package saleson.api.tabTable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import com.emoldino.api.asset.resource.base.mold.enumeration.ToolingStatus;
import com.emoldino.framework.enumeration.ActiveStatus;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.querydsl.core.BooleanBuilder;

import saleson.api.company.CompanyRepository;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.configuration.ColumnTableConfigService;
import saleson.api.configuration.GeneralConfigService;
import saleson.api.counter.CounterRepository;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.location.LocationRepository;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.MachineRepository;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartRepository;
import saleson.api.part.payload.PartPayload;
import saleson.api.tabTable.payload.SaveTabTablePayload;
import saleson.api.tabTable.payload.UpdateTabTablePayload;
import saleson.api.tabbedFilter.TabbedOverviewGeneralFilterService;
import saleson.api.terminal.TerminalRepository;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.user.UserRepository;
import saleson.api.user.payload.UserParam;
import saleson.common.config.Const;
import saleson.common.constant.CommonMessage;
import saleson.common.enumeration.CompanyType;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PageType;
import saleson.common.enumeration.TabType;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DashboardGeneralFilterUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.QPart;
import saleson.model.TabTable;
import saleson.model.TabTableData;

@Service
public class TabTableService {

    @Autowired
    private TabTableRepository tabTableRepository;

    @Autowired
    private TabTableDataRepository tabTableDataRepository;

    @Autowired
    private MoldService moldService;

    @Autowired
    private ColumnTableConfigService columnTableConfigService;

    @Autowired
    private MoldRepository moldRepository;

    @Autowired
    private GeneralConfigService generalConfigService;

    @Autowired
    private DashboardGeneralFilterUtils dashboardGeneralFilterUtils;

    @Autowired
    private TabbedOverviewGeneralFilterService tabbedOverviewGeneralFilterService;

    @Autowired
    private PartRepository partRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private CounterRepository counterRepository;

    @Value("${customer.server.name}")
    private String serverName;


    public ApiResponse getAllTabByCurrentUser(ObjectType objectType) {
        List<TabTable> tabTableList = tabTableRepository.findAllByUserIdAndDeletedFalseAndObjectType(SecurityUtils.getUserId(), objectType);
        Map<String, TabTable> tabNameSavedMap = tabTableList.stream()
                .filter(tabTable -> tabTable.getIsDefaultTab() != null && tabTable.getIsDefaultTab())
                .collect(Collectors.toMap(TabTable::getName, Function.identity(), (o,n) -> n));

        List<TabTable> defaultTabList = getDefaultTabList(objectType, tabNameSavedMap);
        defaultTabList.addAll(tabTableList.stream()
                .filter(tabTable -> tabTable.getIsDefaultTab() != null && !tabTable.getIsDefaultTab())
                .collect(Collectors.toList()));
        return ApiResponse.success(CommonMessage.OK, defaultTabList);
    }

    private List<TabTable> getDefaultTabList(ObjectType objectType, Map<String, TabTable> tabNameSavedMap) {
        List<TabTable> notSaveTabList = Lists.newArrayList();
        switch (objectType) {
		case TOOLING: {
			if ("dyson".equalsIgnoreCase(serverName)) {
				Const.TAB_TABLE_DEFAULT_TAB.TOOLING_DYSON.forEach(tabName -> {
					MoldPayload moldPayload = new MoldPayload();
					moldPayload.setServerName(this.serverName);
					moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TOOLING_SETTING));
					TabTable tabTable;
					if (tabNameSavedMap.containsKey(tabName)) {
						tabTable = tabNameSavedMap.get(tabName);
					} else {
						tabTable = new TabTable(tabName, objectType, true);
					}
					moldPayload.setStatus(tabName);
                    ActiveStatus activeStatus = ActiveStatus.ENABLED;
					if ("DELETED".equals(tabName)) {
						moldPayload.setDeleted(true);
                        activeStatus = ActiveStatus.DISABLED;
					}
					if (TabType.DISPOSED.name().equals(tabName)) {
//						moldPayload.setTabType(TabType.DISPOSED);
                        activeStatus = ActiveStatus.DISPOSED;

					}

//					Long total = moldRepository.count(moldPayload.getPredicate());
					Long total = moldRepository.countByMasterFilter(moldPayload.getPredicate(),activeStatus);
					tabTable.setTotalItem(total);
					notSaveTabList.add(tabTable);
				});
			} else {
				Const.TAB_TABLE_DEFAULT_TAB.TOOLING_OTHER.forEach(tabName -> {
					MoldPayload moldPayload = new MoldPayload();
					moldPayload.setServerName(this.serverName);
					moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TOOLING_SETTING));
					TabTable tabTable;
					if (tabNameSavedMap.containsKey(tabName)) {
						tabTable = tabNameSavedMap.get(tabName);
					} else {
						tabTable = new TabTable(tabName, objectType, true);
					}
					moldPayload.setStatus(tabName);
					if (Arrays.asList("DIGITAL", "NON_DIGITAL").contains(tabName)) {
						moldPayload.setTabType(TabType.valueOf(tabName));
					}
                    ActiveStatus activeStatus = ActiveStatus.ENABLED;

					if ("DELETED".equals(tabName)) {
						moldPayload.setDeleted(true);
                        activeStatus = ActiveStatus.DISABLED;
					}
                    if (TabType.DISPOSED.name().equals(tabName)) {
//                        moldPayload.setTabType(TabType.DISPOSED);
                        activeStatus = ActiveStatus.DISPOSED;
                    }
//					Long total = moldRepository.count(moldPayload.getPredicate());
					Long total = moldRepository.countByMasterFilter(moldPayload.getPredicate(), activeStatus);
					tabTable.setTotalItem(total);
					notSaveTabList.add(tabTable);
				});
			}
			break;
		}
            case PART: {
                Const.TAB_TABLE_DEFAULT_TAB.PART
                        .forEach(tabName -> {
                            TabTable tabTable;
                            if (tabNameSavedMap.containsKey(tabName)) {
                                tabTable = tabNameSavedMap.get(tabName);
                            } else {
                                tabTable = new TabTable(tabName, objectType, true);
                            }
                            PartPayload payload = new PartPayload();
//                            payload.setAccessType(AccessType.ADMIN_MENU);
                            payload.setPageType(PageType.PART_SETTING);
                            if("Enabled".equals(tabName)) {
                                payload.setStatus("active");
                            } else {
                                payload.setStatus("disabled");
                            }
                            payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.PART_SETTING));
                            BooleanBuilder predicate = new BooleanBuilder(payload.getPredicate());
                            if (payload.getDashboardRedirected() != null && payload.getDashboardRedirected())
                                predicate.and(dashboardGeneralFilterUtils.getPartFilter(QPart.part));
                            if (payload.getTabbedDashboardRedirected() != null && payload.getTabbedDashboardRedirected()) {
                                predicate.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getPartFilter());
                            }
                            long total = partRepository.count(predicate);
                            tabTable.setTotalItem(total);
                            notSaveTabList.add(tabTable);
                        });
                break;
            }
            case LOCATION: {
                List<String> tabNameList = Const.TAB_TABLE_DEFAULT_TAB.LOCATION;
                if (SecurityUtils.isInHouse()) {
                    tabNameList = Arrays.stream(CompanyType.values()).filter(CompanyType::isEnabled).map(CompanyType::getTitle).collect(Collectors.toList());
                    tabNameList.add(0, "Enabled");
                    tabNameList.add("Disabled");
                }
                tabNameList
                        .forEach(tabName -> {
                            TabTable tabTable;
                            if (tabNameSavedMap.containsKey(tabName)) {
                                tabTable = tabNameSavedMap.get(tabName);
                            } else {
                                tabTable = new TabTable(tabName, objectType, true);
                            }
                            LocationPayload locationPayload = new LocationPayload();
                            switch (tabName){
                                case "Enabled":
                                    locationPayload.setStatus("active");
                                    break;
                                case "In-house":
                                    locationPayload.setStatus("IN_HOUSE");
                                    break;
                                case "Supplier":
                                    locationPayload.setStatus("SUPPLIER");
                                    break;
                                case "Toolmaker":
                                    locationPayload.setStatus("TOOL_MAKER");
                                    break;
                                case "Disabled":
                                    locationPayload.setStatus("disabled");
                                    break;
                            }
                            long total = locationRepository.count(locationPayload.getPredicate());
                            tabTable.setTotalItem(total);
                            notSaveTabList.add(tabTable);
                        });
                break;
            }
            case COMPANY: {
                List<String> tabNameList = Const.TAB_TABLE_DEFAULT_TAB.COMPANY;
                if (SecurityUtils.isInHouse()) {
                    tabNameList = Arrays.stream(CompanyType.values()).filter(CompanyType::isEnabled).map(CompanyType::getTitle).collect(Collectors.toList());
                    tabNameList.add(0, "Enabled");
                    tabNameList.add("Disabled");
                }
                tabNameList.forEach(tabName -> {
                    TabTable tabTable;
                    if (tabNameSavedMap.containsKey(tabName)) {
                        tabTable = tabNameSavedMap.get(tabName);
                    } else {
                        tabTable = new TabTable(tabName, objectType, true);
                    }
                    CompanyPayload companyPayload = new CompanyPayload();
                    switch (tabName){
                        case "Enabled":
                            companyPayload.setStatus("active");
                            break;
                        case "In-house":
                            companyPayload.setStatus("IN_HOUSE");
                            break;
                        case "Supplier":
                            companyPayload.setStatus("SUPPLIER");
                            break;
                        case "Toolmaker":
                            companyPayload.setStatus("TOOL_MAKER");
                            break;
                        case "Disabled":
                            companyPayload.setStatus("disabled");
                            break;
                    }
                    long total = companyRepository.count(companyPayload.getPredicate());
                    tabTable.setTotalItem(total);
                    notSaveTabList.add(tabTable);
                });
                break;
            }
            case MACHINE: {
                List<String> tabNameList = Const.TAB_TABLE_DEFAULT_TAB.Machine;
                tabNameList.forEach(tabName -> {
                    TabTable tabTable;
                    if (tabNameSavedMap.containsKey(tabName)) {
                        tabTable = tabNameSavedMap.get(tabName);
                    } else {
                        tabTable = new TabTable(tabName, objectType, true);
                    }
                    MachinePayload payload = new MachinePayload();
                    payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MACHINE_SETTING));

                    switch (tabName){
                        case "All":
                            payload.setStatus("enabled");
                            break;
                        case "Matched":
                            payload.setMatchedWithTooling(true);
                            payload.setStatus("enabled");
                            break;
                        case "Un-matched":
                            payload.setMatchedWithTooling(false);
                            payload.setStatus("enabled");
                            break;
                        case "Disabled":
                            payload.setStatus("disabled");
                            break;
                    }
                    long total = machineRepository.count(payload.getPredicate());
                    tabTable.setTotalItem(total);
                    notSaveTabList.add(tabTable);
                });

                break;
            }
            case USER: {
                List<String> tabNameList = Const.TAB_TABLE_DEFAULT_TAB.User;
                tabNameList.forEach(tabName -> {
                    TabTable tabTable;
                    if (tabNameSavedMap.containsKey(tabName)) {
                        tabTable = tabNameSavedMap.get(tabName);
                    } else {
                        tabTable = new TabTable(tabName, objectType, true);
                    }
                    UserParam payload = new UserParam();

                    switch (tabName){
                        case "Enabled":
                            payload.setStatus("active");
                            payload.setCompanyType("admin");
                            break;
                        case "In-house":
                            payload.setCompanyType("in_house");
                            break;
                        case "Supplier":
                            payload.setCompanyType("supplier");
                            break;
                        case "Toolmaker":
                            payload.setCompanyType("tool_maker");
                            break;
                        case "Disabled":
                            payload.setStatus("disabled");
                            break;
                    }
                    long total = userRepository.countAll(payload);
                    tabTable.setTotalItem(total);
                    notSaveTabList.add(tabTable);
                });
                break;
            }
            case TERMINAL:{
                List<String> tabNameList = Const.TAB_TABLE_DEFAULT_TAB.Terminal;
                tabNameList.forEach(tabName -> {
                    TabTable tabTable;
                    if (tabNameSavedMap.containsKey(tabName)) {
                        tabTable = tabNameSavedMap.get(tabName);
                    } else {
                        tabTable = new TabTable(tabName, objectType, true);
                    }
                    TerminalPayload payload = new TerminalPayload();
                    payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TERMINAL_SETTING));

                    switch (tabName){
                        case "All":
                            payload.setStatus("all");
                            break;
                        case "In-house":
                            payload.setStatus("IN_HOUSE");
                            break;
                        case "Supplier":
                            payload.setStatus("SUPPLIER");
                            break;
                        case "Toolmaker":
                            payload.setStatus("TOOL_MAKER");
                            break;
                        case "Disabled":
                            payload.setStatus("disabled");
                            break;
                    }
                    long total = terminalRepository.count(payload.getPredicate());
                    tabTable.setTotalItem(total);
                    notSaveTabList.add(tabTable);
                });
                break;
            }
            case COUNTER:{
                List<String> tabNameList = Const.TAB_TABLE_DEFAULT_TAB.Counter;
                tabNameList.forEach(tabName -> {
                    TabTable tabTable;
                    if (tabNameSavedMap.containsKey(tabName)) {
                        tabTable = tabNameSavedMap.get(tabName);
                    } else {
                        tabTable = new TabTable(tabName, objectType, true);
                    }
                    CounterPayload payload = new CounterPayload();
                    payload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.COUNTER_SETTING));
                    switch (tabName){
                        case "All":
                            payload.setStatus("all");
                            break;
                        case "In-house":
                            payload.setStatus("IN_HOUSE");
                            break;
                        case "Supplier":
                            payload.setStatus("SUPPLIER");
                            break;
                        case "Toolmaker":
                            payload.setStatus("TOOL_MAKER");
                            break;
                        case "Disabled":
                            payload.setStatus("disabled");
                            break;
                    }
                    long total = counterRepository.count(payload.getPredicate());
                    tabTable.setTotalItem(total);
                    notSaveTabList.add(tabTable);
                });
                break;
            }
            default:
        }

        return notSaveTabList;
    }

    @Transactional
    public ApiResponse saveTabTableMold(MoldPayload moldPayload, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            moldPayload.setServerName(this.serverName);
            moldPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TOOLING_SETTING));
            List<Long> moldIdList = moldRepository.findAllIdByPredicate(moldPayload.getPredicate());
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(moldIdList);
            } else {
                saveTabTablePayload.setAddItemIdList(moldIdList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.TOOLING);
    }


    public ApiResponse saveTabTable(SaveTabTablePayload saveTabTablePayload, ObjectType objectType) {
        TabTable tabTable;
        if (saveTabTablePayload.getId() != null) {
            Optional<TabTable> tabTableOptional = tabTableRepository.findById(saveTabTablePayload.getId());
            tabTable = tabTableOptional.orElseThrow(RuntimeException::new);
            if(StringUtils.isNoneBlank(saveTabTablePayload.getName())) {
                tabTable.setName(saveTabTablePayload.getName());
                tabTableRepository.save(tabTable);
            }
        } else {
            tabTable = new TabTable();
            tabTable.setObjectType(objectType);
            tabTable.setName(saveTabTablePayload.getName());
            tabTable.setUserId(SecurityUtils.getUserId());
            tabTableRepository.save(tabTable);
        }

        if (CollectionUtils.isNotEmpty(saveTabTablePayload.getRemoveItemIdList())) {
            tabTableDataRepository.deleteAllByRefIdInAndAndTabTableId(saveTabTablePayload.getRemoveItemIdList(), tabTable.getId());
        }

        List<Long> currentRefId = tabTableDataRepository.findAllRefIdByTabTableId(tabTable.getId());

        if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
            List<TabTableData> saveTabTableDataList = saveTabTablePayload.getAddItemIdList().stream()
                    .filter(refId -> !currentRefId.contains(refId))
                    .map(refId -> new TabTableData(refId, tabTable.getId(), tabTable)).collect(Collectors.toList());
            tabTableDataRepository.saveAll(saveTabTableDataList);
        }



        return ApiResponse.success(CommonMessage.OK, tabTable);
    }

    public ApiResponse updateTabTable(List<UpdateTabTablePayload> updateTabTablePayloadList) {
        List<TabTable> tabTableList = Lists.newArrayList();
        for (UpdateTabTablePayload updateTabTablePayload : updateTabTablePayloadList) {
            TabTable tabTable = null;
            if (updateTabTablePayload.getId() != null) {
                Optional<TabTable> tabTableOptional = tabTableRepository.findById(updateTabTablePayload.getId());
                if(tabTableOptional.isPresent()) {
                    tabTable = tabTableOptional.get();
                    if (updateTabTablePayload.getIsShow() != null) {
                        tabTable.setShow(updateTabTablePayload.getIsShow());
                    }
                    if (updateTabTablePayload.getDeleted() != null) {
                        tabTable.setDeleted(updateTabTablePayload.getDeleted());
                    }
                }
            } else {
                tabTable = updateTabTablePayload.getTabTable();
                tabTable.setUserId(SecurityUtils.getUserId());
            }
            if(tabTable != null) {
                tabTableList.add(tabTable);
            }
        }
        tabTableRepository.saveAll(tabTableList);


        return ApiResponse.success();
    }

    @Transactional
    public ApiResponse saveTabTablePart(PartPayload partPayload, SaveTabTablePayload saveTabTablePayload) {

        if (saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            partPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.PART_SETTING));
            BooleanBuilder predicate = new BooleanBuilder(partPayload.getPredicate());
            if (partPayload.getDashboardRedirected() != null && partPayload.getDashboardRedirected())
                predicate.and(dashboardGeneralFilterUtils.getPartFilter(QPart.part));
            if (partPayload.getTabbedDashboardRedirected() != null && partPayload.getTabbedDashboardRedirected()) {
                predicate.and(tabbedOverviewGeneralFilterService.getPayloadFromSavedFilter().getPartFilter());
            }
            List<Long> partIdList = partRepository.findAllIdByPredicate(predicate);
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(partIdList);
            } else {
                saveTabTablePayload.setAddItemIdList(partIdList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.PART);
    }

    @Transactional
    public ApiResponse saveTabTableLocation(LocationPayload locationPayload, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            List<Long> idList = locationRepository.findAllIdByPredicate(locationPayload.getPredicate());
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(idList);
            } else {
                saveTabTablePayload.setAddItemIdList(idList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.LOCATION);
    }

    @Transactional
    public ApiResponse saveTabTableCompany(CompanyPayload companyPayload, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            List<Long> idList = companyRepository.findAllIdByPredicate(companyPayload.getPredicate());
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(idList);
            } else {
                saveTabTablePayload.setAddItemIdList(idList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.COMPANY);
    }

    @Transactional
    public ApiResponse saveTabTableMachine(MachinePayload machinePayload, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            machinePayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.MACHINE_SETTING));
            List<Long> idList = machineRepository.findAllIdByPredicate(machinePayload.getPredicate());
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(idList);
            } else {
                saveTabTablePayload.setAddItemIdList(idList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.MACHINE);
    }

    @Transactional
    public ApiResponse saveTabTableUser(UserParam userParam, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            List<Long> idList = userRepository.findAllIdByParam(userParam);
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(idList);
            } else {
                saveTabTablePayload.setAddItemIdList(idList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.USER);
    }

    @Transactional
    public ApiResponse saveTabTableTerminal(TerminalPayload terminalPayload, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            terminalPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.TERMINAL_SETTING));
            List<Long> idList = terminalRepository.findAllIdByPredicate(terminalPayload.getPredicate());
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(idList);
            } else {
                saveTabTablePayload.setAddItemIdList(idList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.TERMINAL);
    }

    @Transactional
    public ApiResponse saveTabTableCounter(CounterPayload counterPayload, SaveTabTablePayload saveTabTablePayload) {

        if(saveTabTablePayload.getIsDuplicate() != null && saveTabTablePayload.getIsDuplicate()) {
            counterPayload.setSelectedFields(columnTableConfigService.getSelectedFields(PageType.COUNTER_SETTING));
            List<Long> idList = counterRepository.findAllIdByPredicate(counterPayload.getPredicate());
            if (CollectionUtils.isNotEmpty(saveTabTablePayload.getAddItemIdList())) {
                saveTabTablePayload.getAddItemIdList().addAll(idList);
            } else {
                saveTabTablePayload.setAddItemIdList(idList);
            }
        }

        return saveTabTable(saveTabTablePayload, ObjectType.COUNTER);
    }
}
