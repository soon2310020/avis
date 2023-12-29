package saleson.api.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import saleson.api.company.CompanyRepository;
import saleson.api.company.CompanyService;
import saleson.api.filter.payload.DashboardGeneralFilterPayload;
import saleson.api.location.LocationRepository;
import saleson.api.location.LocationService;
import saleson.api.mold.MoldService;
import saleson.api.part.PartRepository;
import saleson.api.part.PartService;
import saleson.api.user.UserRepository;
import saleson.common.constant.CommonMessage;
import saleson.common.payload.ApiResponse;
import saleson.common.util.DataUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.Company;
import saleson.model.DashboardGeneralFilter;
import saleson.model.Location;
import saleson.model.Part;
import saleson.model.User;
import saleson.model.data.MiniComponentData;

@Service
public class DashboardGeneralFilterService {
    @Autowired
    private DashboardGeneralFilterRepository dashboardGeneralFilterRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private CompanyRepository companyRepository;

    @Lazy
    @Autowired
    private MoldService moldService;
    @Autowired
    private PartService partService;
    @Autowired
    private CompanyService companyService;
    @Autowired
    private LocationService locationService;

    public DashboardGeneralFilter get() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        return optional.orElse(new DashboardGeneralFilter(true, true, true, true));
    }

    public ApiResponse save(DashboardGeneralFilterPayload payload) {
        try {
            User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
            Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
            DashboardGeneralFilter data;
            if (!optional.isPresent()) {
                data = new DashboardGeneralFilter();
                data.setUser(user);
            } else {
                data = optional.get();
            }

            DashboardGeneralFilter tempFilter = new DashboardGeneralFilter();
            bindData(tempFilter, payload);

            DashboardGeneralFilterPayload newPayload = applyDependentFilterToPayload(payload, tempFilter);

            bindData(data, newPayload);
            dashboardGeneralFilterRepository.save(data);

            return ApiResponse.success(CommonMessage.OK, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }

    private DashboardGeneralFilterPayload applyDependentFilterToPayload(DashboardGeneralFilterPayload payload, DashboardGeneralFilter tempFilter) {
        DashboardGeneralFilterPayload result = DataUtils.deepCopy(payload, DashboardGeneralFilterPayload.class);
        if (!payload.isAllSupplier()) {
            if (CollectionUtils.isNotEmpty(payload.getSupplierIds())) {
                List<Long> supplierIds = companyService.findAllMiniDataFilteredCustom(null, tempFilter)
                        .stream()
                        .map(MiniComponentData::getId)
                        .collect(Collectors.toList());
                result.setSupplierIds(supplierIds);
            } else {
                result.setAllSupplier(true);
            }
        }
        if (!payload.isAllToolMaker()) {
            if (CollectionUtils.isNotEmpty(payload.getToolMakerIds())) {
                List<Long> toolMakerIds = companyService.findAllMiniDataFilteredCustom("ToolMaker", tempFilter)
                        .stream()
                        .map(MiniComponentData::getId)
                        .collect(Collectors.toList());
                result.setToolMakerIds(toolMakerIds);
            } else {
                result.setAllToolMaker(true);
            }
        }
        if (!payload.isAllLocation()) {
            if (CollectionUtils.isNotEmpty(payload.getLocationIds())) {
                List<Long> locationIds = locationService.findAllMiniDataFilteredCustom(tempFilter)
                        .stream()
                        .map(MiniComponentData::getId)
                        .collect(Collectors.toList());
                result.setLocationIds(locationIds);
            } else {
                result.setAllLocation(true);
            }
        }
        if (!payload.isAllPart()) {
            if (CollectionUtils.isNotEmpty(payload.getPartIds())) {
                List<Long> partIds = partService.findAllMiniDataFilteredCustom(tempFilter)
                        .stream()
                        .map(MiniComponentData::getId)
                        .collect(Collectors.toList());
                result.setPartIds(partIds);
            } else {
                result.setAllPart(true);
            }
        }

        return result;
    }

    public ApiResponse getToolingAndPartByGeneralFilter() {
        try {
            List<MiniComponentData> parts = partService.findAllMiniDataFiltered(false);
            List<MiniComponentData> molds = moldService.findAllMiniDataByGeneralFilter();
            Map<String, List<MiniComponentData>> map = new HashMap<>();
            map.put("parts", parts);
            map.put("molds", molds);

            return ApiResponse.success(CommonMessage.OK, map);
        } catch (Exception e) {
            return ApiResponse.error();
        }
    }

    public DashboardGeneralFilterPayload bindDataForPayload(DashboardGeneralFilter data) {
        DashboardGeneralFilterPayload payload = new DashboardGeneralFilterPayload();
        payload.setAllPart(data.isAllPart());
        payload.setAllSupplier(data.isAllSupplier());
        payload.setAllToolMaker(data.isAllToolMaker());
        payload.setAllLocation(data.isAllLocation());

        if (!data.isAllPart())
            payload.setPartIds(data.getParts().stream().map(Part::getId).collect(Collectors.toList()));
        if (!data.isAllSupplier())
            payload.setSupplierIds(data.getSuppliers().stream().map(Company::getId).collect(Collectors.toList()));
        if (!data.isAllToolMaker())
            payload.setToolMakerIds(data.getToolMakers().stream().map(Company::getId).collect(Collectors.toList()));
        if (!data.isAllLocation())
            payload.setLocationIds(data.getLocations().stream().map(Location::getId).collect(Collectors.toList()));

        return payload;
    }

    private void bindData(DashboardGeneralFilter data, DashboardGeneralFilterPayload payload) {
        data.setAllPart(payload.isAllPart());
        data.setAllSupplier(payload.isAllSupplier());
        data.setAllToolMaker(payload.isAllToolMaker());
        data.setAllLocation(payload.isAllLocation());

        if (!payload.isAllPart()) {
            if (CollectionUtils.isEmpty(payload.getPartIds())) {
                data.setAllPart(true);
            } else {
                data.setParts(partRepository.findAllById(payload.getPartIds()));
            }
        }

        if (!payload.isAllSupplier()) {
            if (CollectionUtils.isEmpty(payload.getSupplierIds())) {
                data.setAllSupplier(true);
            } else {
                data.setSuppliers(companyRepository.findAllById(payload.getSupplierIds()));
            }
        }

        if (!payload.isAllToolMaker()) {
            if (CollectionUtils.isEmpty(payload.getToolMakerIds())) {
                data.setAllToolMaker(true);
            } else {
                data.setToolMakers(companyRepository.findAllById(payload.getToolMakerIds()));
            }
        }

        if (!payload.isAllLocation()) {
            if (CollectionUtils.isEmpty(payload.getLocationIds())) {
                data.setAllLocation(true);
            } else {
                data.setLocations(locationRepository.findAllById(payload.getLocationIds()));
            }
        }
    }
}
