package saleson.api.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import saleson.api.auth.AuthService;
import saleson.api.category.CategoryService;
import saleson.api.category.payload.CategoryParam;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.counter.CounterService;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.integration.payload.TransactionPayload;
import saleson.api.location.LocationService;
import saleson.api.location.payload.LocationPayload;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartService;
import saleson.api.part.payload.PartPayload;
import saleson.api.role.RoleService;
import saleson.api.role.payload.RoleParam;
import saleson.api.statistics.StatisticsService;
import saleson.api.terminal.TerminalService;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.api.user.UserService;
import saleson.api.user.payload.UserParam;
import saleson.common.config.Const;
import saleson.common.enumeration.RoleType;
import saleson.common.payload.ApiResponse;
import saleson.common.payload.ApiResponseEntity;
import saleson.common.util.DataUtils;
import saleson.common.util.SecurityUtils;
import saleson.dto.*;
import saleson.model.*;
import saleson.model.data.TerminalData;
import saleson.service.transfer.CdataRepository;
import saleson.service.transfer.TransferRepository;
import saleson.service.transfer.TransferService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/integration")
public class IntegrationController {

    @Autowired
    IntergrationService intergrationService;

    @Autowired
    TerminalService terminalService;
    @Autowired
    AuthService authService;
    @Value("${customer.server.name}")
    private String serverName;

    @Lazy
    @Autowired
    MoldService moldService;
    @Autowired
    CounterService counterService;
    @Autowired
    CompanyService companyService;
    @Autowired
    LocationService locationService;
    @Autowired
    CategoryService categoryService;
    @Autowired
    PartService partService;
    @Autowired
    UserService userService;
    @Autowired
    RoleService roleService;


    @Autowired
    StatisticsService statisticsService;
    @Autowired
    TransferRepository transferRepository;

    @GetMapping("/terminals")
    public ResponseEntity getAllTerminals(HttpServletRequest httpServletRequest, TerminalPayload payload,
                                          @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                          Pageable pageable
//                                          @RequestParam(value = "allData", required = false) boolean allData
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);


        Page<TerminalData> pageContent = terminalService.findTerminalData(payload.getPredicate(),
                DataUtils.getPageable(pageIndex, pageSize, pageable));
        List<TerminalDTO> terminalDTOS = pageContent.getContent().stream().map(t -> TerminalDTO.convertToDTO(t.getTerminal())).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), terminalDTOS));
    }

    @GetMapping("/tooling")
    public ResponseEntity getAllMolds(HttpServletRequest httpServletRequest, MoldPayload payload,
                                      @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                      Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        payload.setServerName(serverName);
//        moldService.loadTreeCompanyForPayLoad(payload);

        Page<Mold> pageContent = moldService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<ToolingDTO> toolingDTOS = pageContent.getContent().stream().map(t -> ToolingDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), toolingDTOS));
    }

    @GetMapping("/counter")
    public ResponseEntity getAllCounters(HttpServletRequest httpServletRequest,
                                         CounterPayload payload,
                                         @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                         Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        Page<Counter> pageContent = counterService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<CounterDTO> counterDTOS = pageContent.getContent().stream().map(t -> CounterDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), counterDTOS));
    }

    @GetMapping("/company")
    public ResponseEntity getAllCompanies(HttpServletRequest httpServletRequest,
                                          CompanyPayload payload,
                                          @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                          Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        Page<Company> pageContent = companyService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<CompanyDTO> companyDTOS = pageContent.getContent().stream().map(t -> CompanyDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), companyDTOS));
    }

    @GetMapping("/location")
    public ResponseEntity getAllLocations(HttpServletRequest httpServletRequest,
                                          LocationPayload payload,
                                          @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                          @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                          Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        Page<Location> pageContent = locationService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<LocationDTO> locationDTOS = pageContent.getContent().stream().map(t -> LocationDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), locationDTOS));
    }

    @GetMapping("/category")
    public ResponseEntity getAllCategories(HttpServletRequest httpServletRequest,
                                           CategoryParam payload,
                                           @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                           @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                           Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null && SecurityUtils.getUserId()==null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);
        payload.setFetchingData(true);
        Page<Category> pageContent = categoryService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));
        List<CategoryDTO> categoryDTOS = pageContent.getContent().stream().map(t -> CategoryDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), categoryDTOS));
    }

    @GetMapping("/part")
    public ResponseEntity getAllParts(HttpServletRequest httpServletRequest,
                                      PartPayload payload,
                                      @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                      Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null && SecurityUtils.getUserId()==null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        Page<Part> pageContent = partService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<PartDTO> partDTOS = pageContent.getContent().stream().map(t -> PartDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), partDTOS));
    }

    @GetMapping("/user")
    public ResponseEntity users(HttpServletRequest httpServletRequest,
                                UserParam param,
                                @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        Page<User> pageContent = userService.findAll(param, DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<UserDTO> partDTOS = pageContent.getContent().stream().map(t -> {
            List<UserAlert> userAlerts = userService.getAlertStatus(t.getId(), t.getCompany() != null ? t.getCompany().getCompanyType() : null,
                    t.getRoles() != null ? t.getRoles().stream().map(r -> r.getId()).collect(Collectors.toList()) : null);
            return UserDTO.convertToDTO(t, userAlerts);
        }).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), partDTOS));
    }

    @GetMapping("/access-group")
    public ResponseEntity getAccessGroup(HttpServletRequest httpServletRequest,
                                         RoleParam param,
                                         @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                         @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                         Pageable pageable
    ) {
        User currentUser = authService.getCurrentUser(httpServletRequest);
        if (currentUser == null) return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);

        if (param.getRoleType() == null)
            param.setRoleType(RoleType.ROLE_GROUP);
        Page<Role> pageContent = roleService.findAll(param.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));

        List<RoleDTO> roleDTOS = pageContent.getContent().stream().map(t -> RoleDTO.convertToDTO(t)).collect(Collectors.toList());
        return ApiResponseEntity.ok(new RestData(pageContent.getTotalElements(), roleDTOS));
    }
    @GetMapping("/transaction")
    public ResponseEntity getAllTransaction(HttpServletRequest httpServletRequest,
//                                      TransactionPayload payload,
                                      @RequestParam(value = "date") String date,
                                      @RequestParam(value = "pageIndex", required = false) Integer pageIndex,
                                      @RequestParam(value = "pageSize", required = false) Integer pageSize,
                                      Pageable pageable
    ) {
        try
        {

            User currentUser = authService.getCurrentUser(httpServletRequest);
            if (currentUser == null)
            {
                return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);
            }

/*
        Page<Transfer> pageContent = transferService.findAll(payload.getPredicate(), DataUtils.getPageable(pageIndex, pageSize, pageable));
        List<TransactionDTO> partDTOS = pageContent.getContent().stream().map(t -> TransactionDTO.convertToDTO(t)).collect(Collectors.toList());
*/
            List<TransactionDTO> partDTOS = statisticsService.aggregateTransactionByDay(date);
            return ApiResponseEntity.ok(new RestData(partDTOS.size(), partDTOS));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail!"));
        }
    }

    @GetMapping("/last-sn")
    public ResponseEntity getSn(HttpServletRequest httpServletRequest,
                                            @RequestParam(value = "ci") String ci
    ) {
        try
        {

            User currentUser = authService.getCurrentUser(httpServletRequest);
            if (currentUser == null)
            {
                return ResponseEntity.badRequest().body(Const.ERROR_CODE.USER_OF_SESSION_NOT_FOUND);
            }
            int sn=0;
            Transfer transfer = transferRepository.findFirstByCiOrderBySnDesc(ci).orElse(null);
            if (transfer != null && transfer.getSn() != null)
                sn= transfer.getSn();

            return ApiResponseEntity.ok(sn);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error("Fail!"));
        }
    }

}
