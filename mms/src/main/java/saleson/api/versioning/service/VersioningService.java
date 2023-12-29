package saleson.api.versioning.service;

import org.dozer.DozerBeanMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import saleson.api.category.CategoryService;
import saleson.api.checklist.ChecklistRepository;
import saleson.api.checklist.ChecklistService;
import saleson.api.checklist.ChecklistVersionRepository;
import saleson.api.company.CompanyService;
import saleson.api.counter.CounterService;
import saleson.api.location.LocationService;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartRepository;
import saleson.api.part.PartService;
import saleson.api.role.RoleGraphService;
import saleson.api.role.RoleService;
import saleson.api.terminal.TerminalService;
import saleson.api.user.UserService;
import saleson.api.versioning.repositories.*;
import saleson.common.enumeration.*;
import saleson.common.service.FileStorageRepository;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.*;
import saleson.model.checklist.CheckListObjectType;
import saleson.model.checklist.Checklist;
import saleson.model.checklist.ChecklistType;
import saleson.model.clone.*;
import saleson.model.clone.dto.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VersioningService {
    @Autowired
    UserService userService;

    @Autowired
    RoleService roleService;

    @Lazy
    @Autowired
    MoldService moldService;

    @Autowired
    TerminalService terminalService;

    @Autowired
    LocationService locationService;

    @Autowired
    CounterService counterService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    CompanyService companyService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    PartService partService;

    @Autowired
    UserVersionRepository userVersionRepository;

    @Autowired
    ReversionHistoryRepository reversionHistoryRepository;

    @Autowired
    PartVersionRepository partVersionRepository;

    @Autowired
    CategoryVersionRepository categoryVersionRepository;

    @Autowired
    private CompanyVersionRepository companyVersionRepository;

    @Autowired
    LocationVersionRepository locationVersionRepository;

    @Autowired
    FileStorageRepository fileStorageRepository;

    @Autowired
    CounterVersionRepository counterVersionRepository;

    @Autowired
    MachineVersionRepository machineVersionRepository;

    @Autowired
    RoleVersionRepository roleVersionRepository;

    @Autowired
    TerminalVersionRepository terminalVersionRepository;

    @Autowired
    MoldVersionRepository moldVersionRepository;

    @Autowired
    PartRepository partRepository;

    @Autowired
    DozerBeanMapper dozerBeanMapper;

    @Autowired
    RoleGraphService roleGraphService;

    @Value("${customer.server.name}")
    private String customServerName;

    @Autowired
    ChecklistVersionRepository checklistVersionRepository;
    @Autowired
    ChecklistRepository checklistRepository;
    @Autowired
    ChecklistService checklistService;

    private final int PAGE_SIZE = 10;
//User
    public UserVersion cloneData(User user){
        UserVersion userVersion = UserVersion.builder()
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .position(user.getPosition())
                .contactDialingCode(user.getContactDialingCode())
                .contactNumber(user.getContactNumber())
                .mobileDialingCode(user.getMobileDialingCode())
                .mobileNumber(user.getMobileNumber())
                .memo(user.getMemo())
//                .enabled(user.isEnabled())
                .admin(user.isAdmin())
//                .requested(user.getRequested())
                .companyId(user.getCompanyId())
                .accessRequest(user.getAccessRequest())
                .build();
        userVersion.setOriginId(user.getId());

        if(user.getRequested() != null && user.getRequested() == true)
            userVersion.setEnabled("Requested");
        else if(user.isEnabled())
            userVersion.setEnabled("Enable");
        else
            userVersion.setEnabled("Disable");

        String[] roleIdList = user.getRoleIds();
        List<Long> roleIdNumberList = new ArrayList<>();
        if(roleIdList != null && roleIdList.length > 0){
            String roleIds = roleIdList[0];
            roleIdNumberList.add(Long.valueOf(roleIdList[0]));
            for(int i = 1; i < roleIdList.length; i++){
                roleIds += "," + roleIdList[i];
                roleIdNumberList.add(Long.valueOf(roleIdList[i]));
            }
            userVersion.setRoleIds(roleIds);
        }

        List<UserAlert> userAlertList = userService.getAlertStatus(user.getId(),
                user.getCompany() != null ? user.getCompany().getCompanyType() : null , roleIdNumberList);
        if(userAlertList != null && userAlertList.size() > 0){
            String[] alertStatus = {""};
            userAlertList.forEach(userAlert -> {
                alertStatus[0] += userAlert.getAlertType().name() + ":" + userAlert.getEmail() + ",";
            });
            alertStatus[0] = alertStatus[0].substring(0, alertStatus[0].length() - 1);
            userVersion.setAlertStatus(alertStatus[0]);
        }

        return userVersion;
    }

    public User convertToObject(UserVersion userVersion, User user){
        if(userVersion == null) return null;
//        UserPayload payload = UserPayload.builder()
//                .id(userVersion.getOriginId())
//                .name(userVersion.getName())
//                .email(userVersion.getEmail())
//                .department(userVersion.getDepartment())
//                .position(userVersion.getPosition())
//                .contactDialingCode(userVersion.getContactDialingCode())
//                .contactNumber(userVersion.getContactNumber())
//                .mobileDialingCode(userVersion.getMobileDialingCode())
//                .mobileNumber(userVersion.getMobileNumber())
//                .memo(userVersion.getMemo())
//                .enabled(userVersion.getEnabled())
//                .admin(userVersion.getAdmin())
//                .requested(userVersion.getRequested())
//                .companyId(userVersion.getCompanyId())
//                .accessRequest(userVersion.getAccessRequest())
//                .build();
//        User user = new User();
        user.setId(userVersion.getOriginId());
        user.setName(userVersion.getName());
        user.setEmail(userVersion.getEmail());
        user.setDepartment(userVersion.getDepartment());
        user.setPosition(userVersion.getPosition());
        user.setContactDialingCode(userVersion.getContactDialingCode());
        user.setContactNumber(userVersion.getContactNumber());
        user.setMobileDialingCode(userVersion.getMobileDialingCode());
        user.setMobileNumber(userVersion.getMobileNumber());
        user.setMemo(userVersion.getMemo());
        user.setEnabled(userVersion.getEnabled().equalsIgnoreCase("Enable") ? true : false);
        user.setRequested(userVersion.getEnabled().equalsIgnoreCase("Requested") ? true : false);
        user.setAdmin(userVersion.getAdmin());
//        user.setRequested(userVersion.getRequested());
        user.setCompanyId(userVersion.getCompanyId());
        user.setAccessRequest(userVersion.getAccessRequest());

        if(userVersion.getCompanyId() != null){
            Company company = new Company();
            company.setId(userVersion.getCompanyId());
            user.setCompany(company);
        }
//            String[] roleIdStringList = userVersion.getRoleIds() != null ? userVersion.getRoleIds().split(",") : new String[0];
//            payload.setRoleIds(new Long[0]);
//            if(roleIdStringList.length > 0){
//                Long[] roleIds = new Long[roleIdStringList.length];
//                for(int i = 0; i < roleIdStringList.length; i++){
//                    roleIds[i] = Long.valueOf(roleIdStringList[i]);
//                }
//                payload.setRoleIds(roleIds);
//            }

        user.setRoles(getRolesByString(userVersion.getRoleIds()));

//            String[] alertStatusStringList = userVersion.getAlertStatus() != null ? userVersion.getAlertStatus().split(",") : new String[0];
//            payload.setAlertStatus(new HashMap<>());
//            if(alertStatusStringList.length > 0){
//                Map<AlertType, Boolean> alertStatus = new HashMap<>();
//                for(int i = 0; i < alertStatusStringList.length; i++){
//                    String[] elements = alertStatusStringList[i].split(":");
//                    if(elements.length == 2){
//                        alertStatus.put(AlertType.valueOf(elements[0]), elements[1].equals("true") ? true : false);
//                    }
//                }
//                payload.setAlertStatus(alertStatus);
//            }

//        }
        return user;
    }

    private Set<Role> getRolesByString(String rolesStr){
        Set<Role> roles = new HashSet<>();
        if(rolesStr != null){
            String [] rolesArr =  rolesStr.split(",");
            for(String roleId : rolesArr){
                Role roleObj = new Role();
                roleObj.setId(Long.parseLong(roleId));
                roles.add(roleObj);
            }
        }
        return roles;
    }

    public Map<AlertType, Boolean> getAlertStatusFromUserVersion(UserVersion userVersion){
        Map alertStatus = new HashMap();
        if(userVersion.getAlertStatus() != null){
            String [] arrAlertStatus = userVersion.getAlertStatus().split(",");
            for(String alertStatusKV : arrAlertStatus){
                try {
                    String[] kv = alertStatusKV.split(":");
                    alertStatus.put(AlertType.valueOf(kv[0]), "true".equals(kv[1]));
                }catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
        }
        return alertStatus;
    }

    public void writeHistory(User user){
        UserVersion userVersion = userVersionRepository.save(cloneData(user));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(userVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.USER);
        revisionHistory.setOriginId(user.getId());
//        revisionHistory.setRevisionHistoryId(userVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }

//Category
    public CategoryVersion clone(Category category){
        CategoryVersion categoryVersion = CategoryVersion.builder()
                .name(category.getName())
                .description(category.getDescription())
                .enabled(category.isEnabled())
                .parentId(category.getParent() != null ? category.getParent().getId() :  null)
                .build();
        categoryVersion.setOriginId(category.getId());

        return categoryVersion;
    }

    public Category convertToObject(CategoryVersion categoryVersion, Category category){
        if(categoryVersion == null || category ==null) return null;

//        Category category =  new Category();
//        category.setId(categoryVersion.getOriginId());
        category.setName(categoryVersion.getName());
        category.setDescription(categoryVersion.getDescription());
        category.setEnabled(categoryVersion.isEnabled());

        Category categoryParent = new Category();
        categoryParent.setId(categoryVersion.getParentId());

        category.setParent(categoryParent);

        return category;
    }

    public CategoryVersion writeHistory(Category category){
        CategoryVersion categoryVersion = categoryVersionRepository.save(clone(category));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(categoryVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.CATEGORY);
        revisionHistory.setOriginId(category.getId());
//        revisionHistory.setRevisionHistoryId(categoryVersion.getId());
        reversionHistoryRepository.save(revisionHistory);

        return categoryVersion;
    }

//Part
    public PartVersion clone(Part part){
        PartVersion partVersion = PartVersion.builder()
                .name(part.getName())
                .partCode(part.getPartCode())
                .resinCode(part.getResinCode())
                .resinGrade(part.getResinGrade())
                .designRevision(part.getDesignRevision())
                .size(part.getSize())
                .weightUnit(part.getWeightUnit())
                .sizeUnit(part.getSizeUnit())
                .weight(part.getWeight())
                .categoryId(part.getCategoryId())
                .enabled(part.isEnabled())
                .build();
        partVersion.setOriginId(part.getId());
        return partVersion;
    }

    public Part convertToObject(PartVersion partVersion, Part part){
        if(partVersion == null || part == null) return null;
//        Part part= new Part();
//        part.setId(partVersion.getOriginId());
        part.setName(partVersion.getName());
        part.setPartCode(partVersion.getPartCode());
        part.setResinCode(partVersion.getResinCode());
        part.setResinGrade(partVersion.getResinGrade());
        part.setDesignRevision(partVersion.getDesignRevision());
        part.setSize(partVersion.getSize());
        part.setWeight(partVersion.getWeight());
        part.setCategoryId(partVersion.getCategoryId());
        part.setEnabled(partVersion.isEnabled());
        part.setSizeUnit(partVersion.getSizeUnit());
        part.setWeightUnit(partVersion.getWeightUnit());

        if(partVersion.getCategoryId() != null){
            Category category = new Category();
            category.setId(partVersion.getCategoryId());
            part.setCategoryId(partVersion.getCategoryId());
            part.setCategory(category);
        }
        return part;
    }

    public void writeHistory(Part part){
        PartVersion partVersion = partVersionRepository.save(clone(part));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionId(partVersion.getId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.PART);
        revisionHistory.setOriginId(part.getId());
//        revisionHistory.setRevisionHistoryId(partVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }
//Location
    public LocationVersion clone(Location location){

        LocationVersion locationVersion = LocationVersion.builder()
                .name(location.getName())
                .locationCode(location.getLocationCode())
                .address(location.getAddress())
                .memo(location.getMemo())
                .enabled(location.isEnabled())
                .companyId(location.getCompanyId())
                .build();
        locationVersion.setOriginId(location.getId());
        return locationVersion;
    }

    public Location convertToObject(LocationVersion locationVersion,  Location location){
        if(locationVersion == null || location ==null) return null;

//        Location location = new Location();
//        location.setId(locationVersion.getOriginId());
        location.setName(locationVersion.getName());
        location.setLocationCode(locationVersion.getLocationCode());
        location.setAddress(locationVersion.getAddress());
        location.setMemo(locationVersion.getMemo());
        location.setEnabled(locationVersion.isEnabled());
        location.setCompanyId(locationVersion.getCompanyId());
        if(locationVersion.getCompanyId() != null){
            Company company = new Company();
            company.setId(locationVersion.getCompanyId());
            location.setCompany(company);
        }
        return location;
    }

    public void writeHistory(Location location){
        LocationVersion locationVersion = locationVersionRepository.save(clone(location));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(locationVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.LOCATION);
        revisionHistory.setOriginId(location.getId());
//        revisionHistory.setRevisionHistoryId(locationVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }

//Company
    public CompanyVersion clone(Company company){
        CompanyVersion companyVersion = CompanyVersion.builder()
                .companyType(company.getCompanyType())
                .companyCode(company.getCompanyCode())
                .name(company.getName())
                .address(company.getAddress())
                .manager(company.getManager())
                .phone(company.getPhone())
                .email(company.getEmail())
                .memo(company.getMemo())
                .enabled(company.isEnabled())
                .build();
        companyVersion.setOriginId(company.getId());
        return companyVersion;
    }

    public Company convertToObject(CompanyVersion companyVersion, Company company){
        if(companyVersion == null || company == null) return null;
//        Company company = new Company();
//        company.setId(companyVersion.getOriginId());
        company.setCompanyType(companyVersion.getCompanyType());
        company.setCompanyCode(companyVersion.getCompanyCode());
        company.setName(companyVersion.getName());
        company.setAddress(companyVersion.getAddress());
        company.setManager(companyVersion.getManager());
        company.setPhone(companyVersion.getPhone());
        company.setEmail(companyVersion.getEmail());
        company.setMemo(companyVersion.getMemo());
        company.setEnabled(companyVersion.isEnabled());

        return company;
    }

    public void writeHistory(Company company){
        CompanyVersion companyVersion = companyVersionRepository.save(clone(company));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(companyVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.COMPANY);
        revisionHistory.setOriginId(company.getId());

//        revisionHistory.setRevisionHistoryId(companyVersion.getId());
        reversionHistoryRepository.save(revisionHistory);

    }
//Counter
    private CounterVersion clone(Counter counter){
        CounterVersion counterVersion = CounterVersion.builder()
                .counterId(counter.getEquipmentCode())
                .toolingId(counter.getMold() != null ? counter.getMold().getId() : null)
                .presetCount(counter.getPresetCount())
                .installedAt(counter.getInstalledAt())
                .installedBy(counter.getInstalledBy())
                .memo(counter.getMemo())
                .equipmentStatus(counter.getEquipmentStatus())
                .build();
        counterVersion.setOriginId(counter.getId());
        List<FileStorage> fileStorage = fileStorageRepository.findByRefId2(counter.getId());
        if(fileStorage != null){
            StringBuilder saveLocations = new StringBuilder();
            fileStorage.forEach(file -> saveLocations.append(file.getSaveLocation()).append("^"));
            if(saveLocations.toString().length() > 0) saveLocations.deleteCharAt(saveLocations.toString().length() - 1);
//            counterVersion.setPhoto(saveLocations.toString());
        }
        return counterVersion;
    }

    //Machine
    private MachineVersion clone(Machine machine) {
        MachineVersion machineVersion = MachineVersion.builder()
                .machineId(machine.getMachineCode())
                .companyId(machine.getCompanyId())
                .locationId(machine.getLocationId())
                .line(machine.getLine())
                .machineMaker(machine.getMachineMaker())
                .machineType(machine.getMachineType())
                .machineModel(machine.getMachineModel())
                .machineTonnage(machine.getMachineTonnage())
                .enabled(machine.isEnabled())
                .build();
        machineVersion.setOriginId(machine.getId());
        return machineVersion;
    }

    public Counter convertToObject(CounterVersion counterVersion, Counter counter){
        if(counterVersion == null || counter == null) return null;
//        Counter counter = new Counter();
        counter.setEquipmentCode(counterVersion.getCounterId());
//        counter.setId(counterVersion.getOriginId());
//        Mold mold = new Mold();
//        mold.setId(counterVersion.getToolingId());
//        counter.setMold(mold);
        if(counterVersion.getToolingId() != null){
            Mold mold = moldService.findById(counterVersion.getToolingId());
            counter.setMold(mold);
        }
        counter.setEquipmentStatus(counterVersion.getEquipmentStatus());
        counter.setPresetCount(counterVersion.getPresetCount());
        counter.setInstalledAt(counterVersion.getInstalledAt());
        counter.setInstalledBy(counterVersion.getInstalledBy());
        counter.setMemo(counterVersion.getMemo());
        return counter;
    }

//    public List<String> getListPhotoFromCounterVersion(CounterVersion counterVersion){
//        if(counterVersion == null || counterVersion.getPhoto() == null) return null;
//        String [] photos = counterVersion.getPhoto().split("\\^");
//        return Arrays.asList(photos);
//    }

    public void writeHistory(Counter counter){
        CounterVersion counterVersion = counterVersionRepository.save(clone(counter));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(counterVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.COUNTER);
        revisionHistory.setOriginId(counter.getId());

//        revisionHistory.setRevisionHistoryId(counterVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }

    public void writeHistory(Machine machine) {
        MachineVersion machineVersion = machineVersionRepository.save(clone(machine));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(machineVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.MACHINE);
        revisionHistory.setOriginId(machine.getId());

        reversionHistoryRepository.save(revisionHistory);
    }
//Role
    public RoleVersion clone(Role role){
        if(role == null) return null;
        RoleVersion roleVersion = RoleVersion.builder()
                .authority(role.getAuthority())
                .name(role.getName())
                .description(role.getDescription())
                .build();
        roleVersion.setOriginId(role.getId());

        List<RoleGraph> roleGraphs = roleGraphService.findByRoleId(role.getId());
        if(roleGraphs != null){
            StringBuilder roleGraphNames = new StringBuilder();
            roleGraphs.forEach(roleGraph -> roleGraphNames.append(roleGraph.getGraphType()).append(","));

            if(roleGraphNames.length() > 0){
                roleGraphNames.deleteCharAt(roleGraphNames.length() - 1);
            }
            roleVersion.setRoleGraphs(roleGraphNames.toString());
        }

        if(role.getMenus() != null){
            StringBuilder roleMenus = new StringBuilder();
            StringBuilder roleMenuIds = new StringBuilder();

            for (Menu menu : role.getMenus()) {
                if(menu.getMenuKey().equalsIgnoreCase("/admin/molds") || menu.getMenuKey().equalsIgnoreCase("/admin/parts")){
                    roleMenus.append(menu.getMenuName() + " Admin").append(",");
                }else if(menu.getMenuKey().equalsIgnoreCase("/admin/alerts/tooling")){
                    roleMenus.append(menu.getMenuName() + " Admin Alert").append(",");
                }else {
                    roleMenus.append(menu.getMenuName()).append(",");
                }
                roleMenuIds.append(menu.getId()).append(",");
            }
            if(roleMenus.length() > 0) {
                roleMenus.deleteCharAt(roleMenus.length() - 1);
                roleMenuIds.deleteCharAt(roleMenuIds.length() -1);
            }
            roleVersion.setRoleMenus(roleMenus.toString());
            roleVersion.setRoleMenuIds(roleMenuIds.toString());
        }

        return roleVersion;
    }

    public Role convertToObject(RoleVersion roleVersion, Role role){
        if(roleVersion == null || role == null) return null;
//        Role role = new Role();
//        role.setId(roleVersion.getOriginId());
        role.setAuthority(roleVersion.getAuthority());
        role.setName(roleVersion.getName());
        role.setDescription(roleVersion.getDescription());
        roleGraphService.deleteByRoleId(role.getId());
        if(roleVersion.getRoleGraphs() != null){
            List<RoleGraph> roleGraphs = new ArrayList<>();
            List<String> roleGraphName = getListFromLine(",", roleVersion.getRoleGraphs());
            if(roleGraphName != null){
                roleGraphName.forEach(name -> {
                    RoleGraph roleGraph = new RoleGraph();
                    roleGraph.setRoleId(role.getId());
                    roleGraph.setGraphType(GraphType.valueOf(name));
                    roleGraphs.add(roleGraph);
                });
            }
            if(roleGraphs.size() > 0) roleGraphService.save(roleGraphs);
        }

        if(roleVersion.getRoleMenuIds() != null){
            List<String> menuIds = getListFromLine(",", roleVersion.getRoleMenuIds());
            Set<Menu> menus = new HashSet<>();
            if(menuIds != null){
                menuIds.forEach(id ->{
                    Menu menu = new Menu();
                    menu.setId(Long.parseLong(id));
                    menus.add(menu);
                });
            }
            role.setMenus(menus);
        }

        return role;
    }

    public void writeHistory(Role role){
        RoleVersion roleVersion = roleVersionRepository.save(clone(role));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(roleVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.ROLE);
        revisionHistory.setOriginId(role.getId());
//        revisionHistory.setRevisionHistoryId(roleVersion.getId());
        reversionHistoryRepository.save(revisionHistory);

    }
//Terminal
    public TerminalVersion clone(Terminal terminal){
        if(terminal == null) return null;
        TerminalVersion terminalVersion = TerminalVersion.builder()
                .terminalId(terminal.getEquipmentCode())
                .purchasedAt(terminal.getPurchasedAt())
                .memo(terminal.getMemo())
                .location(terminal.getLocation() != null ? terminal.getLocation().getId() : null)
                .installationArea(terminal.getInstallationArea())
                .installedAt(terminal.getInstalledAt())
                .installedBy(terminal.getInstalledBy())
                .ipAddress(terminal.getIpAddress())
                .build();
        terminalVersion.setOriginId(terminal.getId());
//        if(terminal.getFiles() != null){
//            StringBuilder files = new StringBuilder();
//            StringBuilder fileNames = new StringBuilder();
//            terminal.getFiles().forEach(file -> {
//                files.append(file.getId()).append(",");
//                fileNames.append(file.getFileName()).append(",");
//            });
//            if(files.toString().length() > 0) {
//                files.deleteCharAt(files.toString().length() -1);
//                fileNames.deleteCharAt(fileNames.length() - 1);
//            }
//            terminalVersion.setFileNames(fileNames.toString());
//            terminalVersion.setFiles(files.toString());
//        }
        return terminalVersion;
    }

    public TerminalVersion clone(Terminal terminal,List<String> fileNamesArr){
        if(terminal == null) return null;
        TerminalVersion terminalVersion = TerminalVersion.builder()
                .terminalId(terminal.getEquipmentCode())
                .purchasedAt(terminal.getPurchasedAt())
                .memo(terminal.getMemo())
                .location(terminal.getLocation() != null ? terminal.getLocation().getId() : null)
                .installationArea(terminal.getInstallationArea())
                .installedAt(terminal.getInstalledAt())
                .installedBy(terminal.getInstalledBy())
                .ipAddress(terminal.getIpAddress())
                .ipType(terminal.getIpType())
                .subnetMask(terminal.getSubnetMask())
                .dns(terminal.getDns())
                .gateway(terminal.getGateway())
                .build();
        terminalVersion.setOriginId(terminal.getId());
//        if(terminal.getFiles() != null){
//            StringBuilder files = new StringBuilder();
//            StringBuilder fileNames = new StringBuilder();
//            terminal.getFiles().forEach(file -> {
//                files.append(file.getId()).append(",");
//                fileNames.append(file.getFileName()).append(",");
//            });
//            if(fileNamesArr !=null){
//                fileNamesArr.forEach(name -> {
//                    fileNames.append(name).append(",");
//                });
//            }
//            if(files.toString().length() > 0) {
//                files.deleteCharAt(files.toString().length() -1);
//                fileNames.deleteCharAt(fileNames.length() - 1);
//            }
//            terminalVersion.setFileNames(fileNames.toString());
//            terminalVersion.setFiles(files.toString());
//        }
        return terminalVersion;
    }


    public Terminal convertToObject(TerminalVersion terminalVersion,  Terminal terminal){
        if(terminalVersion == null || terminal == null) return null;
//        Terminal terminal = new Terminal();
        terminal.setEquipmentCode(terminalVersion.getTerminalId());
        terminal.setPurchasedAt(terminalVersion.getPurchasedAt());
        terminal.setMemo(terminalVersion.getMemo());
        Location location = new Location();
        location.setId(terminalVersion.getLocation());
        terminal.setLocation(location);
        terminal.setInstallationArea(terminalVersion.getInstallationArea());
        terminal.setInstalledAt(terminalVersion.getInstalledAt());
        terminal.setInstalledBy(terminalVersion.getInstalledBy());
        terminal.setIpType(terminalVersion.getIpType());
        terminal.setIpAddress(terminalVersion.getIpAddress());
        terminal.setSubnetMask(terminalVersion.getSubnetMask());
        terminal.setGateway(terminalVersion.getGateway());
        terminal.setDns(terminalVersion.getDns());

//        if(terminalVersion.getFiles() != null && terminalVersion.getFiles().length() > 0){
//            List<FileStorage> fileStorages = new ArrayList<>();
//            Arrays.asList(terminalVersion.getFiles().split(",")).forEach(file -> {
//                FileStorage fileStorage = new FileStorage();
//                fileStorage.setId(Long.parseLong(file));
//                fileStorages.add(fileStorage);
//            });
//            terminal.setFiles(fileStorages);
//        }
        return terminal;
    }

    public void writeHistory(Terminal terminal){
        TerminalVersion terminalVersion = terminalVersionRepository.save(clone(terminal));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(terminalVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.TERMINAL);
        revisionHistory.setOriginId(terminal.getId());
//        revisionHistory.setRevisionHistoryId(terminalVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }

    public void writeHistory(Terminal terminal, List<String> fileNames){
        TerminalVersion terminalVersion = terminalVersionRepository.save(clone(terminal, fileNames));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(terminalVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.TERMINAL);
        revisionHistory.setOriginId(terminal.getId());
//        revisionHistory.setRevisionHistoryId(terminalVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }
//Tooling
    public MoldVersion clone(Mold mold){
        if(mold == null) return null;
        MoldVersion moldVersion = MoldVersion.builder()
                .toolingId(mold.getEquipmentCode())
                .toolingLetter(mold.getToolingLetter())
                .toolingType(mold.getToolingType())
                .toolingComplexity(mold.getToolingComplexity())
                .familyTool(mold.getFamilyTool())
                .designedShot(mold.getDesignedShot())
                .madeYear(mold.getMadeYear())
                .contractedCycleTime(mold.getContractedCycleTime())
                .location(mold.getLocation() != null ? mold.getLocation().getId() : null)
                .toolDescription(mold.getToolDescription())

                .size(mold.getSize())
                .weight(mold.getWeight())
                .shotSize(mold.getShotSize())
                .toolMaker(mold.getToolMaker() != null ? mold.getToolMaker().getId() : null)
                .injectionMachineId(mold.getInjectionMachineId())
                .quotedMachineTonnage(mold.getQuotedMachineTonnage())
                .currentMachineTonnage(mold.getCurrentMachineTonnage())

                .runnerType(mold.getRunnerType())
                .runnerMaker(mold.getRunnerMaker())
                .weightRunner(mold.getWeightRunner())
                .hotRunnerDrop(mold.getHotRunnerDrop())
                .hotRunnerZone(mold.getHotRunnerZone())

                .preventCycle(mold.getPreventCycle())
                .preventUpcoming(mold.getPreventUpcoming())
                .preventOverdue(mold.getPreventOverdue())
                .cycleTimeLimit1(mold.getCycleTimeLimit1())
                .cycleTimeLimit2(mold.getCycleTimeLimit2())

                .supplier(mold.getSupplier() != null ? mold.getSupplier().getId() : null)
                .uptimeTarget(mold.getUptimeTarget())
                .uptimeLimitL1(mold.getUptimeLimitL1())
                .uptimeLimitL2(mold.getUptimeLimitL2())
                .labour(mold.getLabour())
                .shiftsPerDay(mold.getShiftsPerDay())
                .productionDays(mold.getProductionDays())
                .maxCapacityPerWeek(mold.getMaxCapacityPerWeek())
                .cycleTimeLimit1Unit(mold.getCycleTimeLimit1Unit())
                .cycleTimeLimit2Unit(mold.getCycleTimeLimit2Unit())
                .cost(mold.getCost())
                .memo(mold.getMemo())
                .totalCavities(mold.getTotalCavities())
                .toolmakerContractedCycleTime(mold.getToolmakerContractedCycleTime())
                .build();
        moldVersion.setOriginId(mold.getId());
        List<FileStorage> fileStorages = fileStorageRepository.findByRefIdAndStorageType(mold.getId(), StorageType.MOLD_CONDITION);
        if(fileStorages != null){
            StringBuilder conditionToolingList = new StringBuilder();
            fileStorages.forEach(file -> conditionToolingList.append(file.getId()).append(","));
            if(conditionToolingList.toString().length() > 0) conditionToolingList.deleteCharAt(conditionToolingList.toString().length() -1);
            moldVersion.setConditionOfTooling(conditionToolingList.toString());
        }

        List<FileStorage> fileStorageMaintenanceDocumentList = fileStorageRepository.findByRefIdAndStorageType(mold.getId(), StorageType.MOLD_MAINTENANCE_DOCUMENT);
        if(fileStorageMaintenanceDocumentList != null &&fileStorageMaintenanceDocumentList.size() > 0){
            StringBuilder maintenanceDocumentList = new StringBuilder();
            fileStorageMaintenanceDocumentList.forEach(file -> maintenanceDocumentList.append(file.getId()).append(","));
            if(maintenanceDocumentList.toString().length() > 0) maintenanceDocumentList.deleteCharAt(maintenanceDocumentList.toString().length() - 1);
            moldVersion.setMaintenanceDocuments(maintenanceDocumentList.toString());
        }

        List<FileStorage> fileStorageInstructionVideoList = fileStorageRepository.findByRefIdAndStorageType(mold.getId(),
                StorageType.MOLD_INSTRUCTION_VIDEO);
        if(fileStorageInstructionVideoList != null && fileStorageInstructionVideoList.size() > 0){
            StringBuilder instructionVideo = new StringBuilder();
            fileStorageInstructionVideoList.forEach(file -> instructionVideo.append(file.getId()).append(","));
            if(instructionVideo.toString().length() > 0) instructionVideo.deleteCharAt(instructionVideo.toString().length() -1);
            moldVersion.setInstructionVideo(instructionVideo.toString());
        }
//        System.out.println("MOLD  PART LOG: "+mold.getCurrentMoldParts().toString());
//        List<MoldPart> moldPartList = moldService.findMoldPartsById(mold.getId());
        if(mold.getCurrentMoldParts() != null && mold.getCurrentMoldParts().size() > 0){
//        if(moldPartList != null && moldPartList.size() > 0){
            StringBuilder moldParts = new StringBuilder();
            StringBuilder moldPartsName = new StringBuilder();
            mold.getCurrentMoldParts().forEach(moldPart -> {
                if(moldPart.getPart() != null){
                    moldParts.append(moldPart.getPart().getId()).append(",").append(moldPart.getCavity()).append(",").append(moldPart.getTotalCavities()).append(";");
                    moldPartsName.append(moldPart.getPart().getName()).append(",");
                }
            });
            if(moldParts.toString().length() > 0) moldParts.deleteCharAt(moldParts.toString().length() -1);
            if(moldPartsName.length() > 0) moldPartsName.deleteCharAt(moldPartsName.length() -1);
            moldVersion.setParts(moldParts.toString());
            moldVersion.setPartsName(moldPartsName.toString());
        }

        if(mold.getMoldAuthorities() != null && mold.getMoldAuthorities().size() > 0){
            StringBuilder moldAuthorities = new StringBuilder();
            mold.getMoldAuthorities().forEach(auth -> moldAuthorities.append(auth.getAuthority()).append(","));
            if(moldAuthorities.toString().length() > 0) moldAuthorities.deleteCharAt(moldAuthorities.toString().length() -1);
            moldVersion.setMoldAuthorities(moldAuthorities.toString());
        }

        if(mold.getEngineersInCharge() != null){
            StringBuilder engineerName = new StringBuilder();
            StringBuilder engineerId = new StringBuilder();
            mold.getEngineersInCharge().forEach(u -> {
                engineerName.append(u.getName()).append(",");
                engineerId.append(u.getId()).append(",");
            });

            if(engineerId.length() > 0) engineerId.deleteCharAt(engineerId.length() -1);
            if(engineerName.length() > 0) engineerName.deleteCharAt(engineerName.length() -1);

            moldVersion.setEngineerNames(engineerName.toString());
            moldVersion.setEngineerIds(engineerId.toString());
        }

        return moldVersion;

    }



    public Mold convertToObject(MoldVersion moldVersion, Mold mold){
        if(moldVersion == null || mold == null) return null;
//        Mold mold = new Mold();
//        moldService.deleteMoldPartByMoldId(mold.getId());
        mold.setEquipmentCode(moldVersion.getToolingId());
        mold.setToolingLetter(moldVersion.getToolingLetter());
        mold.setToolingType(moldVersion.getToolingType());
        mold.setToolingComplexity(moldVersion.getToolingComplexity());
        mold.setFamilyTool(moldVersion.getFamilyTool());
        mold.setDesignedShot(moldVersion.getDesignedShot());
        mold.setMadeYear(moldVersion.getMadeYear());
        mold.setContractedCycleTime(moldVersion.getContractedCycleTime());
        mold.setToolmakerContractedCycleTime(moldVersion.getToolmakerContractedCycleTime());
        Location location = new Location();
        location.setId(moldVersion.getLocation());
        mold.setLocation(moldVersion.getLocation() != null ? location : null);
        mold.setToolDescription(moldVersion.getToolDescription());

        mold.setSize(moldVersion.getSize());
        mold.setWeight(moldVersion.getWeight());
        mold.setShotSize(moldVersion.getShotSize());
        Company company = new Company();
        company.setId(moldVersion.getToolMaker());
        mold.setToolMaker(moldVersion.getToolMaker() != null ? company : null);
        mold.setInjectionMachineId(moldVersion.getInjectionMachineId());
        mold.setQuotedMachineTonnage(moldVersion.getQuotedMachineTonnage());
        mold.setCurrentMachineTonnage(moldVersion.getCurrentMachineTonnage());

        mold.setRunnerType(moldVersion.getRunnerType());
        mold.setRunnerMaker(moldVersion.getRunnerMaker());
        mold.setWeightRunner(moldVersion.getWeightRunner());
        mold.setHotRunnerDrop(moldVersion.getHotRunnerDrop());
        mold.setHotRunnerZone(moldVersion.getHotRunnerZone());

        mold.setPreventCycle(moldVersion.getPreventCycle());
        mold.setPreventUpcoming(moldVersion.getPreventUpcoming());
        mold.setPreventOverdue(moldVersion.getPreventOverdue());
        mold.setCycleTimeLimit1(moldVersion.getCycleTimeLimit1());
        mold.setCycleTimeLimit2(moldVersion.getCycleTimeLimit2());

        Company supplier = new Company();
        supplier.setId(moldVersion.getSupplier());
        mold.setSupplier(moldVersion.getSupplier() != null ? supplier : null);
        mold.setUptimeTarget(moldVersion.getUptimeTarget());
        mold.setUptimeLimitL1(moldVersion.getUptimeLimitL1());
        mold.setUptimeLimitL2(moldVersion.getUptimeLimitL2());
        mold.setLabour(moldVersion.getLabour());
        mold.setShiftsPerDay(moldVersion.getShiftsPerDay());
        mold.setProductionDays(moldVersion.getProductionDays());
        mold.setMaxCapacityPerWeek(moldVersion.getMaxCapacityPerWeek());
//        mold.setMoldAuthorities(getListMoldAuthorityFromMoldVersion(moldVersion));
//        mold.setMoldParts(getListMoldPartFromMoldVersion(moldVersion.getParts()));
        mold.setSizeUnit(moldVersion.getSizeUnit());
        mold.setWeightUnit(moldVersion.getWeightUnit());
        mold.setCycleTimeLimit1Unit(moldVersion.getCycleTimeLimit1Unit());
        mold.setCycleTimeLimit2Unit(moldVersion.getCycleTimeLimit2Unit());
        mold.setCost(moldVersion.getCost());
        mold.setMemo(moldVersion.getMemo());
        return mold;
    }

    public void writeHistory(Mold mold){
        MoldVersion moldVersion = moldVersionRepository.save(clone(mold));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(moldVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(RevisionObjectType.MOLD);
        revisionHistory.setOriginId(mold.getId());
        reversionHistoryRepository.save(revisionHistory);

    }

    public List<FileStorage> getListFileStorageFromMolVersion(String textId) {
        List<String> conditionOfTooling = getListFromLine(",", textId);
        List<FileStorage> fileStorages = new ArrayList<>();
        if (conditionOfTooling != null) {
            conditionOfTooling.forEach(id -> {
                FileStorage fileStorage = new FileStorage();
                fileStorage.setId(Long.parseLong(id));
                fileStorages.add(fileStorage);
            });
        }
        return fileStorages;
    }
    public Set<MoldPart> getListMoldPartFromMoldVersion(String parts){
        List<String> partObj = getListFromLine(";", parts);
        Set<MoldPart> moldParts = new HashSet<>();
        if(partObj != null){
            partObj.forEach(partObjStr ->{
                try{
                List<String> partAndCavity = getListFromLine(",", partObjStr);
                if(partAndCavity.size() == 2){
                        MoldPart moldPart = new MoldPart();
                        moldPart.setPartId(Long.parseLong(partAndCavity.get(0)));
                        moldPart.setCavity(Integer.parseInt(partAndCavity.get(1)));
                        moldParts.add(moldPart);
                }
                if(partAndCavity.size() == 3){
                    MoldPart moldPart = new MoldPart();
                    moldPart.setPartId(Long.parseLong(partAndCavity.get(0)));
                    moldPart.setCavity(Integer.parseInt(partAndCavity.get(1)));
                    if (!"null".equals(partAndCavity.get(2))) {
                        moldPart.setTotalCavities(Integer.parseInt(partAndCavity.get(2)));
                    }
                    moldParts.add(moldPart);
                }
                }catch (Exception e){
                    e.printStackTrace();
                }

            });
        }
        return moldParts;
    }

    public Set<MoldAuthority> getListMoldAuthorityFromMoldVersion(MoldVersion moldVersion){
        List<String> moldAuthoritiesId = getListFromLine(",", moldVersion.getMoldAuthorities());
        Set<MoldAuthority> moldAuthorities = new HashSet<>();
        if(moldAuthoritiesId != null){
            moldAuthoritiesId.forEach(id ->{
                MoldAuthority moldAuthority = new MoldAuthority();
                moldAuthority.setId(Long.parseLong(id));
                moldAuthorities.add(moldAuthority);
            });
        }
        return moldAuthorities;
    }

    private List<String> getListFromLine(String regex, String text){
        if(text == null || regex == null || text.length() < 1) return new ArrayList<>();
        return Arrays.asList(text.split(regex));
    }

    public void restore(MoldVersion moldVersion){
        if(moldVersion == null) return;

        Mold mold = moldService.findById(moldVersion.getOriginId());
        if(mold == null) return;
        MoldPayload moldPayload  = new MoldPayload();
        Set<MoldPart> moldParts = getListMoldPartFromMoldVersion(moldVersion.getParts());
        moldPayload.setMoldParts(moldParts != null ? moldParts.toArray(new MoldPart[0]) : null);

//        List<String> moldAuthorities = getListFromLine(",", moldVersion.getMoldAuthorities());
//        moldPayload.setAuthorities(moldAuthorities != null ? moldAuthorities.toArray(new String[0]) : null);

        Mold moldConvert = convertToObject(moldVersion, mold);
        processMoldAuthorities(moldVersion.getMoldAuthorities(), moldConvert);
        processEngineer(moldVersion.getEngineerIds(), mold);
        moldService.save(moldConvert, moldPayload);
        Mold moldFinal = moldService.findById(moldVersion.getOriginId());

        writeHistory(moldFinal);
    }

    private void processMoldAuthorities(String moldAuth, Mold mold){
        Set<MoldAuthority> moldAuthorities = new HashSet<>();
        List<String> moldAuthStr = getListFromLine(",", moldAuth);
        if(moldAuthStr != null) {
            for (String authority : moldAuthStr.toArray(new String[0])) {
                MoldAuthority moldAuthority = new MoldAuthority(authority);

                mold.getMoldAuthorities().removeIf(ma -> true);

                moldAuthorities.add(moldAuthority);
            }
            mold.setMoldAuthorities(moldAuthorities);
        }
    }

    private void processEngineer(String engineerStr, Mold mold){
        List<String> engineerIdList = getListFromLine(",", engineerStr);
        if(engineerIdList != null){
            List<Long> engineerIds = engineerIdList.stream().map(Long::parseLong).collect(Collectors.toList());
            if (engineerIds.size() > 0) {
                List<User> engineerSet = new ArrayList<>();

                for (Long id : engineerIds) {
                    User engineer = new User();
                    engineer.setId(id);
                    engineerSet.add(engineer);
                }
                mold.setEngineersInCharge(engineerSet);
            }
        }
    }

    public void restore(CategoryVersion categoryVersion){
        if(categoryVersion == null) return;

        Category category = categoryService.findById(categoryVersion.getOriginId());
        if(category == null) return;

        categoryService.save(convertToObject(categoryVersion, category));
        writeHistory(category);

    }

    public void restore(CounterVersion counterVersion){
        if(counterVersion == null) return;

        Counter counter = counterService.findById(counterVersion.getOriginId());
        if(counter == null) return;
        counterService.save(convertToObject(counterVersion, counter));
        writeHistory(counter);

    }

    public void restore(CompanyVersion companyVersion){
        if(companyVersion == null) return;
        Company company = companyService.findById(companyVersion.getOriginId());
        if(company == null) return;

        companyService.save(convertToObject(companyVersion, company));
        writeHistory(company);
    }

    public void restore(TerminalVersion terminalVersion){
        if(terminalVersion == null) return;

        Terminal terminal = terminalService.findById(terminalVersion.getOriginId());
        if(terminal == null) return;

        terminalService.save(convertToObject(terminalVersion, terminal));
        writeHistory(terminal);
    }

    public void restore(LocationVersion locationVersion){
        if(locationVersion == null) return;

        Location location = locationService.findById(locationVersion.getOriginId());
        if(location == null) return;

        locationService.save(convertToObject(locationVersion, location));
        writeHistory(location);

    }

    public void restore(PartVersion partVersion){
        if(partVersion == null) return;

        Part part = partService.findById(partVersion.getOriginId());
        if(part == null) return;

        partService.save(convertToObject(partVersion, part));
        writeHistory(part);
    }

    public void restore(UserVersion userVersion){
        if(userVersion == null) return;

        User user = userService.getUserById(userVersion.getOriginId());
        if(user == null) return;

        User finalUser = userService.save(convertToObject(userVersion, user));
        writeHistory(finalUser);
    }


    public void restore(RoleVersion roleVersion){
        if(roleVersion == null) return;

        Role role = roleService.findById(roleVersion.getOriginId());
        if(role == null) return;

        Role finalRole = roleService.save(convertToObject(roleVersion, role));
        writeHistory(finalRole);
    }

    public List<Object> test(){
//        Part category = partService.findById(152002L);
//        writeHistory(category);
        PartVersion categoryVersion= partVersionRepository.findById(175609L).orElse(null);
        restore(categoryVersion);
        return null;
    }

    public ResponseEntity<?> restore(Long revisionId, String objectType){
        if(revisionId == null || objectType == null){
            return ResponseEntity.ok("CANT_FIND_REVISION_ID_OR_REVISION_OBJECT_TYPE");
        }
        RevisionHistory revisionHistory = reversionHistoryRepository.findById(revisionId).orElse(null);
        if(revisionHistory == null){
            return ResponseEntity.ok("CANT_FIND_REVISION");
        }
        if(RevisionObjectType.USER.name().equals(objectType)){
            restore(userVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.CATEGORY.name().equals(objectType)){
            restore(categoryVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.COMPANY.name().equals(objectType)){
            restore(companyVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.COUNTER.name().equals(objectType)){
            restore(counterVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.LOCATION.name().equals(objectType)){
            restore(locationVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.MOLD.name().equals(objectType)){
            restore(moldVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.PART.name().equals(objectType)){
            restore(partVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.ROLE.name().equals(objectType)){
            restore(roleVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.TERMINAL.name().equals(objectType)){
            restore(terminalVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }else if(RevisionObjectType.CHECKLIST_MAINTENANCE.name().equals(objectType)){
            restore(checklistVersionRepository.findById(revisionHistory.getRevisionId()).orElse(null));
        }
        return ResponseEntity.ok("SUCCESS");
    }

    private RevisionHistoryDto convertToDto(RevisionHistory revisionHistory){
        RevisionHistoryDto revisionHistoryDto = dozerBeanMapper.map(revisionHistory, RevisionHistoryDto.class);
        if (revisionHistory != null)
            revisionHistoryDto.setCreatedAtValue(revisionHistory.getCreatedAt().getEpochSecond());
        User user = revisionHistory.getEditedBy()==null ? null : userService.findById(revisionHistory.getEditedBy());
        revisionHistoryDto.setEditedByName(user != null ? user.getName() : "");
        revisionHistoryDto.setRevisionObjectType(revisionHistory.getRevisionObjectType().name());
        return revisionHistoryDto;
    }

    private List<RevisionHistoryDto> convertToDto(List<RevisionHistory> revisionHistories, boolean isFirstPage){
        List<RevisionHistoryDto> revisionHistoryDtos = new ArrayList<>();
        if(revisionHistories !=null && revisionHistories.size() > 0){
//            revisionHistories.forEach(revisionHistory -> revisionHistoryDtos.add(convertToDto(revisionHistory)));
            for(int i = 0; i < revisionHistories.size(); i++){
                if(i == 0 && isFirstPage){
                    RevisionHistoryDto revisionHistoryDto = convertToDto(revisionHistories.get(i));
                    revisionHistoryDto.setCurrentVersion(true);
                    revisionHistoryDtos.add(revisionHistoryDto);
                    continue;
                }
                revisionHistoryDtos.add(convertToDto(revisionHistories.get(i)));
            }
        }
        return revisionHistoryDtos;
    }


    public List<RevisionHistoryDto> getRevisionHistory(Long reversionId, String objectType, int page){

        Pageable pageable = PageRequest.of(page -1, this.PAGE_SIZE);
        Page<RevisionHistory> res = reversionHistoryRepository.getRevision(reversionId, objectType, pageable);
        return convertToDto(res.getContent(), page == 1);
    }

    public Map<String, Object> getRevisionHistoriesAndTotalPageApi(Long originId, String objectType, int page){

        Pageable pageable = PageRequest.of(page -1, this.PAGE_SIZE);
        Page<RevisionHistory> res = reversionHistoryRepository.getRevision(originId, objectType, pageable);
        Map<String, Object> response = new HashMap<>();
        response.put("revisionHistories", convertToDto(res.getContent(), page == 1));
        response.put("totalPage", generateTotalPage(reversionHistoryRepository.getTotalElements(originId, objectType)));
//        System.out.println(res.getTotalElements());
        return response;
    }

    private int generateTotalPage(int totalElement){
        int totalPage = totalElement / this.PAGE_SIZE;
        if(totalElement % this.PAGE_SIZE != 0) totalPage ++;
        return totalPage;
    }

    private Map<String, Object> pushFullData(List<RevisionHistory> revisionHistories, String objectType){
        Map<String, Object> result =  new HashMap<>();
        if(RevisionObjectType.USER.name().equals(objectType)){
            result.put("after",  convertToDto(userVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null)));
            result.put("before",  convertToDto(userVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null)));
        }else if(RevisionObjectType.ROLE.name().equals(objectType)){
            RoleVersionDto roleVersionDtoAfter = convertToDto(roleVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            if(roleVersionDtoAfter != null) {
                roleVersionDtoAfter.setMmsMenuAccessDto(convertToMMSMenuAccessDto(roleVersionDtoAfter.getRoleGraphs(), roleVersionDtoAfter.getRoleMenus()));
                roleVersionDtoAfter.setAdminMenuAccessDto(convertToAdminMenuAccessDto(roleVersionDtoAfter.getRoleMenus()));
            }
            result.put("after",  roleVersionDtoAfter);

            RoleVersionDto roleVersionDtoBefore = convertToDto(roleVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            if(roleVersionDtoBefore != null) {
                roleVersionDtoBefore.setMmsMenuAccessDto(convertToMMSMenuAccessDto(roleVersionDtoBefore.getRoleGraphs(), roleVersionDtoBefore.getRoleMenus()));
                roleVersionDtoBefore.setAdminMenuAccessDto(convertToAdminMenuAccessDto(roleVersionDtoBefore.getRoleMenus()));
            }
            result.put("before",  roleVersionDtoBefore);
        }else if(RevisionObjectType.COMPANY.name().equals(objectType)){
            CompanyVersionDto companyVersionAfter = convertToDto(companyVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after",  companyVersionAfter);
            CompanyVersionDto companyVersionBefore = convertToDto(companyVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("before",  companyVersionBefore);
        }else if(RevisionObjectType.LOCATION.name().equals(objectType)){
            LocationVersionDto locationVersionAfterDto = convertToDto(locationVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            LocationVersionDto locationVersionBeforeDto = convertToDto(locationVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", locationVersionAfterDto);
            result.put("before", locationVersionBeforeDto);
        }else if(RevisionObjectType.CATEGORY.name().equals(objectType)){
            CategoryVersionDto categoryVersionAfterDto = convertToDto(categoryVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            CategoryVersionDto categoryVersionBeforeDto = convertToDto(categoryVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", categoryVersionAfterDto);
            result.put("before", categoryVersionBeforeDto);
        }else if(RevisionObjectType.PART.name().equals(objectType)){
            PartVersionDto partVersionAfterDto = convertToDto(partVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            PartVersionDto partVersionBeforeDto = convertToDto(partVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", partVersionAfterDto);
            result.put("before", partVersionBeforeDto);
        }else if(RevisionObjectType.TERMINAL.name().equals(objectType)){
            TerminalVersionDto terminalVersionAfterDto = convertToDto(terminalVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            TerminalVersionDto terminalVersionBeforeDto = convertToDto(terminalVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", terminalVersionAfterDto);
            result.put("before", terminalVersionBeforeDto);
        }else if(RevisionObjectType.MOLD.name().equals(objectType)){
            MoldVersionDto moldVersionAfterDto = convertToDto(moldVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            MoldVersionDto moldVersionBeforeDto = convertToDto(moldVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", moldVersionAfterDto);
            result.put("before", moldVersionBeforeDto);
            result.put("customServerName", this.customServerName);
        }else if(RevisionObjectType.COUNTER.name().equals(objectType)){
            CounterVersionDto counterVersionAfterDto = convertToDto(counterVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            CounterVersionDto counterVersionBeforeDto = convertToDto(counterVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", counterVersionAfterDto);
            result.put("before", counterVersionBeforeDto);
        }else if(Arrays.asList(RevisionObjectType.CHECKLIST_MAINTENANCE.name(),
                RevisionObjectType.CHECKLIST_REJECT_RATE.name(),
                RevisionObjectType.CHECKLIST_GENERAL.name(),
                RevisionObjectType.CHECKLIST_REFURBISHMENT.name(),
                RevisionObjectType.CHECKLIST_DISPOSAL.name(),
                RevisionObjectType.PICK_LIST.name()
        ).contains(objectType)){
            ChecklistVersion counterVersionAfterDto = checklistVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null);
            ChecklistVersion counterVersionBeforeDto = checklistVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null);
            result.put("after", counterVersionAfterDto);
            result.put("before", counterVersionBeforeDto);
        } else if(RevisionObjectType.MACHINE.name().equals(objectType)){
            MachineVersionDto machineVersionAfterDto = convertToDto(machineVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            MachineVersionDto machineVersionBeforeDto = convertToDto(machineVersionRepository.findById(revisionHistories.get(1).getRevisionId()).orElse(null));
            result.put("after", machineVersionAfterDto);
            result.put("before", machineVersionBeforeDto);
        }
        return result;
    }

    private Map<String, Object> pushAData(List<RevisionHistory> revisionHistories, String objectType){
        Map<String, Object> result =  new HashMap<>();
        if(RevisionObjectType.USER.name().equals(objectType)){
            result.put("after",  convertToDto(userVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null)));
            result.put("before",  convertToDto(userVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null)));
        }else if(RevisionObjectType.ROLE.name().equals(objectType)){
            RoleVersionDto roleVersionDto = convertToDto(roleVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            if(roleVersionDto !=null) {
                roleVersionDto.setMmsMenuAccessDto(convertToMMSMenuAccessDto(roleVersionDto.getRoleGraphs(), roleVersionDto.getRoleMenus()));
                roleVersionDto.setAdminMenuAccessDto(convertToAdminMenuAccessDto(roleVersionDto.getRoleMenus()));
            }
            result.put("after",  roleVersionDto);
            result.put("before",  roleVersionDto);
        }else if(RevisionObjectType.COMPANY.name().equals(objectType)){
            CompanyVersionDto companyVersion = convertToDto(companyVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after",  companyVersion);
            result.put("before",  companyVersion);
        }else if(RevisionObjectType.LOCATION.name().equals(objectType)){
            LocationVersionDto locationVersionAfterDto = convertToDto(locationVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", locationVersionAfterDto);
            result.put("before", locationVersionAfterDto);
        }else if(RevisionObjectType.CATEGORY.name().equals(objectType)){
            CategoryVersionDto categoryVersionAfterDto = convertToDto(categoryVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", categoryVersionAfterDto);
            result.put("before", categoryVersionAfterDto);
        }else if(RevisionObjectType.PART.name().equals(objectType)){
            PartVersionDto partVersionAfterDto = convertToDto(partVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", partVersionAfterDto);
            result.put("before", partVersionAfterDto);
        }else if(RevisionObjectType.TERMINAL.name().equals(objectType)){
            TerminalVersionDto terminalVersionAfterDto = convertToDto(terminalVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", terminalVersionAfterDto);
            result.put("before", terminalVersionAfterDto);
        }else if(RevisionObjectType.MOLD.name().equals(objectType)){
            MoldVersionDto moldVersionDto = convertToDto(moldVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", moldVersionDto);
            result.put("before", moldVersionDto);
            result.put("customServerName", this.customServerName);
        }else if(RevisionObjectType.COUNTER.name().equals(objectType)){
            CounterVersionDto counterVersionDto = convertToDto(counterVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", counterVersionDto);
            result.put("before", counterVersionDto);
        }else if(RevisionObjectType.CHECKLIST_MAINTENANCE.name().equals(objectType)){
            ChecklistVersion counterVersionDto = checklistVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null);
            result.put("after", counterVersionDto);
            result.put("before", counterVersionDto);
        } else if(RevisionObjectType.MACHINE.name().equals(objectType)){
            MachineVersionDto machineVersionAfterDto = convertToDto(machineVersionRepository.findById(revisionHistories.get(0).getRevisionId()).orElse(null));
            result.put("after", machineVersionAfterDto);
            result.put("before", machineVersionAfterDto);
        }
        return result;
    }

    public Map<String, Object> getRevisionAfterAndBefore(Long id, String objectType, Long originId){
        List<RevisionHistory> revisionHistories = reversionHistoryRepository.getTop2RevisionHistories(id, objectType, originId);
        Map<String, Object> result =  new HashMap<>();
        if(revisionHistories != null && revisionHistories.size() >= 2){
            result = pushFullData(revisionHistories, objectType);
        }else if(revisionHistories != null && revisionHistories.size() == 1){
            result = pushAData(revisionHistories, objectType);
        }
        return result;
    }

    private RoleVersionDto convertToDto(RoleVersion roleVersion){
        if(roleVersion == null) return null;
        return dozerBeanMapper.map(roleVersion,RoleVersionDto.class);
    }

    private TerminalVersionDto convertToDto(TerminalVersion terminalVersion){
        TerminalVersionDto terminalVersionDto = dozerBeanMapper.map(terminalVersion, TerminalVersionDto.class);
//        if(terminalVersion.getFiles() != null){
//            terminalVersionDto.setFileNameArray(getListFromLine(",",terminalVersion.getFileNames()));
//        }
        if(terminalVersion.getLocation() !=  null){
            Location location = locationService.findById(terminalVersion.getLocation());
            terminalVersionDto.setLocationName(location != null ? location.getName() : "");
        }
        terminalVersionDto.setIpTypeString(terminalVersion.getIpType() != null ? terminalVersion.getIpType().getTitle() : "");
        return terminalVersionDto;
    }

    private CategoryVersionDto convertToDto(CategoryVersion categoryVersion){
        CategoryVersionDto categoryVersionDto = dozerBeanMapper.map(categoryVersion, CategoryVersionDto.class);
        if(categoryVersion.getParentId() != null){
            Category category = categoryService.findById(categoryVersion.getParentId());
            categoryVersionDto.setParentName(category != null ? category.getName() : "");
        }
        return categoryVersionDto;
    }

    private PartVersionDto convertToDto(PartVersion partVersion){
        PartVersionDto partVersionDto = dozerBeanMapper.map(partVersion, PartVersionDto.class);
        if(partVersion.getCategoryId() != null){
            Category category = categoryService.findById(partVersion.getCategoryId());
            partVersionDto.setCategoryName(category != null ? category.getName() : "");
        }
        partVersionDto.setSizeUnit(partVersion.getSizeUnit() != null  ? partVersion.getSizeUnit().getTitle() : "");
        partVersionDto.setWeightUnit(partVersion.getWeightUnit() != null ? partVersion.getWeightUnit().getTitle() : "");
        return partVersionDto;
    }

    private CounterVersionDto convertToDto(CounterVersion counterVersion){
        CounterVersionDto counterVersionDto = dozerBeanMapper.map(counterVersion, CounterVersionDto.class);
        if(counterVersion.getToolingId() != null){
            Mold mold = moldService.findById(counterVersion.getToolingId());
            counterVersionDto.setToolingName(mold !=null ? mold.getEquipmentCode() : "");
        }
//        counterVersionDto.setPhotos(getListFromLine("\\^", counterVersion.getPhoto()));
        return counterVersionDto;
    }

    private MachineVersionDto convertToDto(MachineVersion machineVersion){
        MachineVersionDto machineVersionDto = dozerBeanMapper.map(machineVersion, MachineVersionDto.class);
        Company company = companyService.findById(machineVersion.getCompanyId());
        machineVersionDto.setCompanyName(company != null ? company.getName() : "");

        Location location = locationService.findById(machineVersion.getLocationId());
        machineVersionDto.setLocationName(location != null ? location.getName() : "");

        return machineVersionDto;
    }

    private LocationVersionDto convertToDto(LocationVersion locationVersion){
        LocationVersionDto locationVersionDto = dozerBeanMapper.map(locationVersion, LocationVersionDto.class);
        Company company = companyService.findById(locationVersion.getCompanyId());
        locationVersionDto.setCompanyName(company != null ? company.getName() : "");

        return locationVersionDto;
    }

    private MoldVersionDto convertToDto(MoldVersion moldVersion){
        MoldVersionDto moldVersionDto = dozerBeanMapper.map(moldVersion, MoldVersionDto.class);
        if(moldVersion.getLocation() != null){
            Location location = locationService.findById(moldVersion.getLocation());
            moldVersionDto.setLocationName(location != null ? location.getName() : "");
        }

        if(moldVersion.getSupplier() != null){
            Company company = companyService.findById(moldVersion.getSupplier());
            moldVersionDto.setSupplierName(company != null ? company.getName() : "");
        }

        if(moldVersion.getToolMaker() != null){
            Company company = companyService.findById(moldVersion.getToolMaker());
            moldVersionDto.setToolMakerName(company != null ? company.getName() : "");
        }

        moldVersionDto.setWeightUnitStr(moldVersion.getWeightUnit() != null ? moldVersion.getWeightUnit().getTitle() : "");
        moldVersionDto.setSizeUnitStr(moldVersion.getSizeUnit() != null ? moldVersion.getSizeUnit().getTitle() : "");
        moldVersionDto.setCycleTimeLimit1UnitStr(moldVersion.getCycleTimeLimit1Unit() != null ? moldVersion.getCycleTimeLimit1Unit().getTitle() : "");
        moldVersionDto.setCycleTimeLimit2UnitStr(moldVersion.getCycleTimeLimit2Unit() != null ? moldVersion.getCycleTimeLimit2Unit().getTitle() : "");
        moldVersionDto.setEngineerNameArr(moldVersion.getEngineerNames() !=  null ? getListFromLine(",", moldVersion.getEngineerNames()) : new ArrayList<>());
        moldVersionDto.setAuthorities(getListFromLine(",", moldVersion.getMoldAuthorities()));

//        if(moldVersion.getParts() !=null){
//            List<String> idOfPart = getListFromLine(",", moldVersion.getParts());
//            if(idOfPart != null){
//                List<MoldPart> moldParts = moldService.findMoldPartByIdIn(idOfPart.stream().map(Long::parseLong).collect(Collectors.toList()));
//                moldVersionDto.setMoldPartNames(convert(moldParts));
//            }
//        }

        moldVersionDto.setMoldPartNames(getListFromLine(",", moldVersion.getPartsName()));

        return moldVersionDto;

    }

    private List<String> convert(List<MoldPart> moldParts){
        List<String> moldPartName = new ArrayList<>();
        if(moldParts != null){
            moldParts.forEach(mold -> moldPartName.add(mold.getPartName()));
        }
        return moldPartName;
    }

    private UserVersionDto convertToDto(UserVersion userVersion){
        if(userVersion == null) return null;
        UserVersionDto userVersionDto = dozerBeanMapper.map(userVersion, UserVersionDto.class);
        Company company = companyService.findById(userVersion.getCompanyId());
        userVersionDto.setCompanyName(company != null ? company.getName() : "");
        if(userVersion.getContactNumber() != null && userVersion.getContactDialingCode() != null){
            userVersionDto.setContactNumber(userVersion.getContactDialingCode() + userVersion.getContactNumber());
        }else{
            userVersionDto.setContactNumber("");
        }
        if(userVersion.getMobileNumber() != null && userVersion.getMobileDialingCode() != null){
            userVersionDto.setMobileNumber(userVersion.getMobileDialingCode() + userVersion.getMobileNumber());
        }else{
            userVersionDto.setMobileNumber("");
        }

        userVersionDto.setAccessLevel(userVersion.getAdmin() ? "Admin" : "Regular");
        Set<Role> roles = getRolesByString(userVersion.getRoleIds());
        List<String> accessFeature = new ArrayList<>();
        List<String> accessGroup = new ArrayList<>();
        roles.forEach(role ->{
            Role roleData = roleService.findById(role.getId());
            if(RoleType.ROLE_MENU.equals(roleData.getRoleType())){
                accessFeature.add(roleData.getAuthority());
            }else if(RoleType.ROLE_GROUP.equals(roleData.getRoleType())){
                accessGroup.add(roleData.getAuthority());
            }
        });
        userVersionDto.setAccessFeature(accessFeature);
        userVersionDto.setAccessGroup(accessGroup);

        return userVersionDto;
    }

    private CompanyVersionDto convertToDto(CompanyVersion companyVersion){
        CompanyVersionDto companyVersionDto = dozerBeanMapper.map(companyVersion, CompanyVersionDto.class);
        companyVersionDto.setCompanyType(companyVersion.getCompanyType().name());
        companyVersionDto.setCompanyTypeTitle(companyVersion.getCompanyType() != null? companyVersion.getCompanyType().getTitle() : "");
        return companyVersionDto;
    }

    private AdminMenuAccessDto convertToAdminMenuAccessDto(String roleMenu){
        AdminMenuAccessDto adminMenuAccessDto = new AdminMenuAccessDto();
        if(roleMenu !=null){
            List<String> menuAccessName = getListFromLine(",", roleMenu);
            if(menuAccessName != null){
                menuAccessName.forEach(name -> {
                    if("Companies".equals(name)) adminMenuAccessDto.setCompanies(true);
                    else if("Locations".equals(name)) adminMenuAccessDto.setLocations(true);
                    else if("Category".equals(name)) adminMenuAccessDto.setCategory(true);
                    else if("Parts Admin".equals(name)) adminMenuAccessDto.setParts(true);
                    else if("Terminals".equals(name)) adminMenuAccessDto.setTerminals(true);
                    else if("Tooling Admin".equals(name)) adminMenuAccessDto.setTooling(true);
                    else if("Counters".equals(name)) adminMenuAccessDto.setCounters(true);
                    else if("Reset".equals(name)) adminMenuAccessDto.setReset(true);
                    else if("Terminal".equalsIgnoreCase(name)) adminMenuAccessDto.setTerminalAlert(true);
                    else if("Tooling Admin Alert".equalsIgnoreCase(name)) adminMenuAccessDto.setToolingAlert(true);
                    else if("Counter".equalsIgnoreCase(name)) adminMenuAccessDto.setCounterAlert(true);

//                    if("Corrective".equals(name)){
//                        adminMenuAccessDto.setCorrective(true);
//                    }
                });
            }
        }
        return adminMenuAccessDto;
    }

    private MMSMenuAccessDto convertToMMSMenuAccessDto(String roleGraphType, String roleMenu){
        MMSMenuAccessDto mmsMenuAccessDto = new MMSMenuAccessDto();
        if(roleGraphType != null) {
            List<String> roleGraphTypes = getListFromLine(",", roleGraphType);
            roleGraphTypes.forEach(roleGraph -> {
                if (GraphType.QUICK_STATS.name().equals(roleGraph)) mmsMenuAccessDto.setQuickStats(true);
                else if (GraphType.DISTRIBUTION.name().equals(roleGraph)) mmsMenuAccessDto.setDistribute(true);
                else if (GraphType.HIERARCHY.name().equals(roleGraph)) mmsMenuAccessDto.setProjectHierarchy(true);
                else if (GraphType.CYCLE_TIME_STATUS.name().equals(roleGraph))
                    mmsMenuAccessDto.setCycleTimeStatus(true);
                else if (GraphType.CAPACITY.name().equals(roleGraph)) mmsMenuAccessDto.setCapacityUtilization(true);
                else if (GraphType.TOOLING.name().equals(roleGraph)) mmsMenuAccessDto.setToolingStatus(true);
                else if (GraphType.MAINTENANCE_STATUS.name().equals(roleGraph))
                    mmsMenuAccessDto.setPreventiveMaintenance(true);
                else if (GraphType.UPTIME_STATUS.name().equals(roleGraph)) mmsMenuAccessDto.setUptimeStatus(true);
                else if (GraphType.DOWNTIME.name().equals(roleGraph)) mmsMenuAccessDto.setDowntime(true);
//                else if (GraphType.OEE.name().equals(roleGraph))
//                    mmsMenuAccessDto.setOverallEquipmentEffectiveness(true);
                else if (GraphType.PRODUCTION_RATE.name().equals(roleGraph)) mmsMenuAccessDto.setProductionRate(true);
                else if (GraphType.UTILIZATION_RATE.name().equals(roleGraph)) mmsMenuAccessDto.setUtilizationRate(true);
            });

            List<String> menuAccessName = getListFromLine(",", roleMenu);
            menuAccessName.forEach(name -> {
                if("Parts".equalsIgnoreCase(name)) mmsMenuAccessDto.setParts(true);
                else if("Tooling".equalsIgnoreCase(name)) mmsMenuAccessDto.setTooling(true);
                else if("Relocation".equalsIgnoreCase(name)) mmsMenuAccessDto.setRelocation(true);
                else if("Disconnection".equalsIgnoreCase(name)) mmsMenuAccessDto.setDisconnection(true);
                else if("Cycle Time".equalsIgnoreCase(name)) mmsMenuAccessDto.setCycleTime(true);
                else if("Maintenance".equalsIgnoreCase(name)) mmsMenuAccessDto.setMaintenance(true);
                else if("Uptime".equalsIgnoreCase(name)) mmsMenuAccessDto.setUptime(true);
                else if("Reset".equalsIgnoreCase(name)) mmsMenuAccessDto.setReset(true);
                else if("Data Submission".equalsIgnoreCase(name)) mmsMenuAccessDto.setDataSubmission(true);
                else if("Tooling Benchmarking".equalsIgnoreCase(name)) mmsMenuAccessDto.setToolingBenchmarking(true);
                else if("Advanced Search".equalsIgnoreCase(name)) mmsMenuAccessDto.setAdvancedSearch(true);
            });
        }

        return mmsMenuAccessDto;
    }

    //checklist
    public ChecklistVersion clone(Checklist checklist){
        ChecklistVersion companyVersion = ChecklistVersion.builder()
                .company(checklist.getCompany())
                .checklistCode(checklist.getChecklistCode())
                .checklistItemStr(checklist.getChecklistItemStr())
                .enabled(checklist.isEnabled())
                .build();
        companyVersion.setOriginId(checklist.getId());
        return companyVersion;
    }

    public Checklist convertToObject(ChecklistVersion companyVersion, Checklist checklist){
        if(companyVersion == null || checklist == null) return null;
        checklist.setChecklistCode(companyVersion.getChecklistCode());
        checklist.setCompany(companyVersion.getCompany());
        checklist.setCompanyId(companyVersion.getCompanyId());
        checklist.setChecklistItemStr(companyVersion.getChecklistItemStr());
        checklist.setEnabled(companyVersion.isEnabled());

        return checklist;
    }

    public void writeHistory(Checklist checklist){
        ChecklistVersion companyVersion = checklistVersionRepository.save(clone(checklist));
        RevisionHistory revisionHistory = new RevisionHistory();
        revisionHistory.setRevisionId(companyVersion.getId());
        revisionHistory.setEditedBy(SecurityUtils.getUserId());
        revisionHistory.setRevisionObjectType(getChecklistType(checklist.getChecklistType(), checklist.getObjectType()));
        revisionHistory.setOriginId(checklist.getId());

//        revisionHistory.setRevisionHistoryId(companyVersion.getId());
        reversionHistoryRepository.save(revisionHistory);
    }

    private RevisionObjectType getChecklistType(ChecklistType checklistType, CheckListObjectType objectType) {
        if (CheckListObjectType.PICK_LIST.equals(objectType)) {
            return RevisionObjectType.PICK_LIST;
        } else {
            if (checklistType == null) {
                return RevisionObjectType.CHECKLIST_MAINTENANCE;
            } else {
                switch (checklistType) {
                    case REJECT_RATE:
                        return RevisionObjectType.CHECKLIST_REJECT_RATE;
                    case GENERAL:
                        return RevisionObjectType.CHECKLIST_GENERAL;
                    case REFURBISHMENT:
                        return RevisionObjectType.CHECKLIST_REFURBISHMENT;
                    case DISPOSAL:
                        return RevisionObjectType.CHECKLIST_DISPOSAL;
                    case QUALITY_ASSURANCE:
                        return RevisionObjectType.CHECKLIST_QUALITY_ASSURANCE;
                    default:
                        return RevisionObjectType.CHECKLIST_MAINTENANCE;
                }
            }
        }
    }

    public void restore(ChecklistVersion checklistVersion){
        if(checklistVersion == null) return;
        Checklist checklist = checklistRepository.findById(checklistVersion.getOriginId()).orElse(null);
        if(checklist == null) return;

        checklistService.save(convertToObject(checklistVersion, checklist),null);
        writeHistory(checklist);
    }

    public List<RevisionHistory> getListHistoryInRange(Long originId, RevisionObjectType type, String start, String end) {
        Instant startDate = start != null ? DateUtils.getInstant(start, DateUtils.DEFAULT_DATE_FORMAT) : DateUtils.getInstant("20000101000000", DateUtils.DEFAULT_DATE_FORMAT);
        Instant endDate = end  != null ? DateUtils.getInstant(end, DateUtils.DEFAULT_DATE_FORMAT) : Instant.now();

        Optional<List<RevisionHistory>> optional = reversionHistoryRepository.findByOriginIdAndRevisionObjectTypeAndCreatedAtBetweenOrderByCreatedAtAsc(originId, type, startDate, endDate);
        return optional.orElseGet(ArrayList::new);
    }

}
