package saleson.api.tabbedFilter;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import saleson.api.category.CategoryRepository;
import saleson.api.company.CompanyRepository;
import saleson.api.mold.MoldRepository;
import saleson.api.part.PartRepository;
import saleson.api.tabbedFilter.payload.TabbedOverviewGeneralFilterPayload;
import saleson.api.user.UserRepository;
import saleson.common.constant.CommonMessage;
import saleson.common.payload.ApiResponse;
import saleson.common.util.SecurityUtils;
import saleson.model.Category;
import saleson.model.Company;
import saleson.model.Mold;
import saleson.model.Part;
import saleson.model.User;
import saleson.model.filter.TabbedOverviewGeneralFilter;

@Service
public class TabbedOverviewGeneralFilterService {
    @Autowired
    private TabbedOverviewGeneralFilterRepository tabbedOverviewGeneralFilterRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PartRepository partRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MoldRepository moldRepository;
    @Autowired
    private CompanyRepository companyRepository;

    public TabbedOverviewGeneralFilter get() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<TabbedOverviewGeneralFilter> optional = tabbedOverviewGeneralFilterRepository.findByUser(user);
        return optional.orElse(new TabbedOverviewGeneralFilter(true, true, true, true));
    }

    public TabbedOverviewGeneralFilterPayload getPayloadFromSavedFilter(){
        TabbedOverviewGeneralFilter filter = get();
        TabbedOverviewGeneralFilterPayload payload = new TabbedOverviewGeneralFilterPayload();
        bindDataToPayload(filter, payload);
        return payload;
    }

    public ApiResponse save(TabbedOverviewGeneralFilterPayload payload) {
        try {
            User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
            Optional<TabbedOverviewGeneralFilter> optional = tabbedOverviewGeneralFilterRepository.findByUser(user);
            TabbedOverviewGeneralFilter data;
            if (!optional.isPresent()) {
                data = new TabbedOverviewGeneralFilter();
                data.setUser(user);
            } else {
                data = optional.get();
            }
/*

            TabbedOverviewGeneralFilter tempFilter = new TabbedOverviewGeneralFilter();
            bindData(tempFilter, payload);

            TabbedOverviewGeneralFilterPayload newPayload = applyDependentFilterToPayload(payload, tempFilter);
*/

            bindData(data, payload);
            tabbedOverviewGeneralFilterRepository.save(data);

            return ApiResponse.success(CommonMessage.OK, data);
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error();
        }
    }
/*

    private TabbedOverviewGeneralFilterPayload applyDependentFilterToPayload(TabbedOverviewGeneralFilterPayload payload, TabbedOverviewGeneralFilter tempFilter) {
        TabbedOverviewGeneralFilterPayload result = DataUtils.deepCopy(payload, TabbedOverviewGeneralFilterPayload.class);
        //TODO: processing
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
*/

/*
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
*/
/*

    public TabbedOverviewGeneralFilterPayload bindDataForPayload(TabbedOverviewGeneralFilter data) {
        TabbedOverviewGeneralFilterPayload payload = new TabbedOverviewGeneralFilterPayload();
        payload.setAllPart(data.isAllPart());
        payload.setAllSupplier(data.isAllSupplier());

        if (!data.isAllPart())
            payload.setPartIds(data.getParts().stream().map(Part::getId).collect(Collectors.toList()));
        if (!data.isAllSupplier())
            payload.setSupplierIds(data.getSuppliers().stream().map(Company::getId).collect(Collectors.toList()));

        return payload;
    }
*/

    private void bindData(TabbedOverviewGeneralFilter data, TabbedOverviewGeneralFilterPayload payload) {
        data.setAllProduct(payload.isAllProduct());
        data.setAllPart(payload.isAllPart());
        data.setAllTooling(payload.isAllTooling());
        data.setAllSupplier(payload.isAllSupplier());
        data.setDuration(payload.getDuration());

        if (!payload.isAllProduct()) {
            if (CollectionUtils.isNotEmpty(payload.getProductIds())) {
                data.setProducts(categoryRepository.findAllById(payload.getProductIds()));
            } else {
                data.setProducts(null);
            }
        }

        if (!payload.isAllPart()) {
            if (CollectionUtils.isNotEmpty(payload.getPartIds())) {
                data.setParts(partRepository.findAllById(payload.getPartIds()));
            } else {
                data.setParts(null);
            }
        }
        if (!payload.isAllTooling()) {
            if (CollectionUtils.isNotEmpty(payload.getToolingIds())) {
                data.setToolings(moldRepository.findAllById(payload.getToolingIds()));
            } else {
                data.setToolings(null);
            }
        }

        if (!payload.isAllSupplier()) {
            if (CollectionUtils.isNotEmpty(payload.getSupplierIds())) {
                data.setSuppliers(companyRepository.findAllById(payload.getSupplierIds()));
            } else {
                data.setSuppliers(null);
            }
        }

    }

    private void bindDataToPayload(TabbedOverviewGeneralFilter data, TabbedOverviewGeneralFilterPayload payload) {

        payload.setAllProduct(data.isAllProduct());
        payload.setAllPart(data.isAllPart());
        payload.setAllTooling(data.isAllTooling());
        payload.setAllSupplier(data.isAllSupplier());
        payload.setDuration(data.getDuration());

        if (!data.isAllProduct()) {
            if (CollectionUtils.isNotEmpty(data.getProducts())) {
                payload.setProductIds(data.getProducts().stream().map(Category::getId).collect(Collectors.toList()));
            } else {
                payload.setProductIds(null);
            }
        }

        if (!data.isAllPart()) {
            if (CollectionUtils.isNotEmpty(data.getParts())) {
                payload.setPartIds(data.getParts().stream().map(Part::getId).collect(Collectors.toList()));
            } else {
                payload.setPartIds(null);
            }
        }
        if (!data.isAllTooling()) {
            if (CollectionUtils.isNotEmpty(data.getToolings())) {
                payload.setToolingIds(data.getToolings().stream().map(Mold::getId).collect(Collectors.toList()));
            } else {
                payload.setToolingIds(null);
            }
        }

        if (!data.isAllSupplier()) {
            if (CollectionUtils.isNotEmpty(data.getSuppliers())) {
                payload.setSupplierIds(data.getSuppliers().stream().map(Company::getId).collect(Collectors.toList()));
            } else {
                payload.setSupplierIds(null);
            }
        }
    }
}
