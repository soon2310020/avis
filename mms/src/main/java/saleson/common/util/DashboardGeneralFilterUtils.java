package saleson.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.accesscontrol.util.AccessControlUtils;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;

import saleson.api.company.CompanyService;
import saleson.api.filter.DashboardGeneralFilterRepository;
import saleson.api.filter.DashboardGeneralFilterService;
import saleson.api.filter.payload.DashboardGeneralFilterPayload;
import saleson.api.location.LocationService;
import saleson.api.part.PartService;
import saleson.api.user.UserRepository;
import saleson.model.Company;
import saleson.model.DashboardGeneralFilter;
import saleson.model.Location;
import saleson.model.Part;
import saleson.model.QCategory;
import saleson.model.QCompany;
import saleson.model.QLocation;
import saleson.model.QMachine;
import saleson.model.QMold;
import saleson.model.QMoldPart;
import saleson.model.QPart;
import saleson.model.User;
import saleson.model.data.MiniComponentData;

@Deprecated
@Service
public class DashboardGeneralFilterUtils {
    @Autowired
    private DashboardGeneralFilterRepository dashboardGeneralFilterRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    CompanyService companyService;
    @Autowired
    LocationService locationService;
    @Autowired
    PartService partService;
    @Autowired
    DashboardGeneralFilterService dashboardGeneralFilterService;

    public boolean checkExistedGeneralFilter() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        return optional.isPresent();
    }

    public DashboardGeneralFilter getDashboardGeneralFilter() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        return optional.orElseGet(null);
    }


    public List<Long> getPartIds() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllPart()) {
                return Lists.newArrayList();
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getParts().stream().map(Part::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getPartIdsCustom(DashboardGeneralFilter filter) {
        if (filter == null || filter.isAllPart()) {
            return Lists.newArrayList();
        }
        return filter.getParts().stream().map(Part::getId).collect(Collectors.toList());
    }

    public List<Long> getPartIds_Old() {
        return partService.findAllMiniDataFiltered(false)
                .stream()
                .map(MiniComponentData::getId)
                .collect(Collectors.toList());
    }

    public List<Long> getSupplierIds() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllSupplier()) {
                return Lists.newArrayList();
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getSuppliers().stream().map(Company::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getSupplierIdsCustom(DashboardGeneralFilter filter) {
        if (filter == null || filter.isAllSupplier()) {
            return Lists.newArrayList();
        }
        return filter.getSuppliers().stream().map(Company::getId).collect(Collectors.toList());
    }

    public List<Long> getSupplierIds_Old() {
        return companyService.findAllMiniDataFiltered(null, false)
                .stream()
                .map(MiniComponentData::getId)
                .collect(Collectors.toList());
    }

    public List<Long> getSupplierIdsOverview() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllSupplier()) {
                return null;
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getSuppliers().stream().map(Company::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getToolMakerIds_Old() {
        DashboardGeneralFilter filter = dashboardGeneralFilterService.get();
        DashboardGeneralFilterPayload payload = dashboardGeneralFilterService.bindDataForPayload(filter);

        return companyService.findAllMiniDataFiltered("ToolMaker", false)
                .stream()
                .map(MiniComponentData::getId)
                .collect(Collectors.toList());
    }

    public List<Long> getToolMakerIds() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllToolMaker()) {
                return Lists.newArrayList();
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getToolMakers().stream().map(Company::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getToolMakerIdsCustom(DashboardGeneralFilter filter) {
        if (filter == null || filter.isAllToolMaker()) {
            return Lists.newArrayList();
        }

        return filter.getToolMakers().stream().map(Company::getId).collect(Collectors.toList());
    }

    public List<Long> getToolMakerIdsOverview() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllToolMaker()) {
                return null;
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getToolMakers().stream().map(Company::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getLocationIds() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllLocation()) {
                return Lists.newArrayList();
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getLocations().stream().map(Location::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getLocationIdsCustom(DashboardGeneralFilter filter) {
        if (filter == null || filter.isAllLocation()) {
            return Lists.newArrayList();
        }

        return filter.getLocations().stream().map(Location::getId).collect(Collectors.toList());
    }

    public List<Long> getLocationIds_Old() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllLocation()) {
                return locationService.findAllMiniData().stream().map(MiniComponentData::getId).collect(Collectors.toList());
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getLocations().stream().map(Location::getId).collect(Collectors.toList()))
                .orElse(locationService.findAllMiniData().stream().map(MiniComponentData::getId).collect(Collectors.toList()));
    }

    public List<Long> getLocationIdsOverview() {
        User user = userRepository.getOne(Objects.requireNonNull(SecurityUtils.getUserId()));
        Optional<DashboardGeneralFilter> optional = dashboardGeneralFilterRepository.findByUser(user);
        if (optional.isPresent()) {
            if (optional.get().isAllLocation()) {
                return null;
            }
        }
        return optional
                .map(dashboardGeneralFilter -> dashboardGeneralFilter.getLocations().stream().map(Location::getId).collect(Collectors.toList()))
                .orElse(Lists.newArrayList());
    }

    public List<Long> getCompanyIds() {
        List<Long> ids = new ArrayList<>();
        ids.addAll(this.getSupplierIds_Old()); // keep using old function
        ids.addAll(this.getToolMakerIds_Old()); // keep using old function
        return ids;
    }

    public BooleanBuilder getMoldFilterCommon(QMold mold) {
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
        builder.and(mold.companyId.notIn(JPAExpressions
                .select(company.id)
                .from(company)
                .where(company.isEmoldino.isTrue())));
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
		}

        return builder;
    }

    public BooleanBuilder getMoldFilter(QMold mold) {
        QMoldPart moldPart = QMoldPart.moldPart;
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
        builder.and(mold.companyId.notIn(JPAExpressions
                .select(company.id)
                .from(company)
                .where(company.isEmoldino.isTrue())));
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
		}

        if (!checkExistedGeneralFilter())
            return builder;
        else {
            //get dashboard general filter
            List<Long> partIds = getPartIds();
            List<Long> supplierIds = getSupplierIds();
            List<Long> toolMakerIds = getToolMakerIds();
            List<Long> locationIds = getLocationIds();

            if (CollectionUtils.isNotEmpty(partIds))
                builder.and(mold.id.in(JPAExpressions
                        .select(moldPart.moldId).distinct()
                        .from(moldPart)
                        .where(moldPart.partId.in(partIds))));
            if (CollectionUtils.isNotEmpty(supplierIds))
                builder.and(mold.supplier.id.in(supplierIds));
            if (CollectionUtils.isNotEmpty(toolMakerIds))
                builder.and(mold.toolMaker.id.in(toolMakerIds));
            if (CollectionUtils.isNotEmpty(locationIds))
                builder.and(mold.location.id.in(locationIds));
        }

        return builder;
    }

    public BooleanBuilder getMoldFilterCustom(QMold mold, DashboardGeneralFilter filter) {
        QMoldPart moldPart = QMoldPart.moldPart;
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(mold.deleted.isNull().or(mold.deleted.isFalse()));
        builder.and(mold.companyId.notIn(JPAExpressions
                .select(company.id)
                .from(company)
                .where(company.isEmoldino.isTrue())));
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and((mold.id.in(JPQLQueryUtils.getMoldIdsSubquery())));
		}

        if (filter == null)
            return builder;
        else {
            //get dashboard general filter
            List<Long> partIds = getPartIdsCustom(filter);
            List<Long> supplierIds = getSupplierIdsCustom(filter);
            List<Long> toolMakerIds = getToolMakerIdsCustom(filter);
            List<Long> locationIds = getLocationIdsCustom(filter);

            if (CollectionUtils.isNotEmpty(partIds))
                builder.and(mold.id.in(JPAExpressions
                        .select(moldPart.moldId).distinct()
                        .from(moldPart)
                        .where(moldPart.partId.in(partIds))));
            if (CollectionUtils.isNotEmpty(supplierIds))
                builder.and(mold.supplier.id.in(supplierIds));
            if (CollectionUtils.isNotEmpty(toolMakerIds))
                builder.and(mold.toolMaker.id.in(toolMakerIds));
            if (CollectionUtils.isNotEmpty(locationIds))
                builder.and(mold.location.id.in(locationIds));
        }

        return builder;
    }

/*
    public BooleanBuilder getMoldFilter_Old(QMold mold) {
        BooleanBuilder builder = new BooleanBuilder();
        if (checkExistedGeneralFilter()) {
            //get dashboard general filter
            List<Long> supplierIds = getSupplierIds();
            List<Long> toolMakerIds = getToolMakerIds();
            List<Long> locationIds = getLocationIds();

            if (CollectionUtils.isNotEmpty(locationIds)
                    && CollectionUtils.isNotEmpty(supplierIds)
                    && CollectionUtils.isNotEmpty(toolMakerIds)) {// location + supplier + toolmaker
                builder.and(mold.location.id.in(locationIds)
                        .or(mold.supplier.id.in(supplierIds))
                        .or(mold.toolMaker.id.in(toolMakerIds)));
            } else if (CollectionUtils.isNotEmpty(locationIds)
                    && CollectionUtils.isEmpty(supplierIds)
                    && CollectionUtils.isNotEmpty(toolMakerIds)) {//location + toolmaker
                builder.and(mold.location.id.in(locationIds)
                        .or(mold.toolMaker.id.in(toolMakerIds)));
            } else if (CollectionUtils.isNotEmpty(locationIds)
                    && CollectionUtils.isNotEmpty(supplierIds)
                    && CollectionUtils.isEmpty(toolMakerIds)) {//location + supplier
                builder.and(mold.location.id.in(locationIds)
                        .or(mold.supplier.id.in(supplierIds)));
            } else if (CollectionUtils.isNotEmpty(locationIds)
                    && CollectionUtils.isEmpty(supplierIds)
                    && CollectionUtils.isEmpty(toolMakerIds)) {//location
                builder.and(mold.location.id.in(locationIds));
            } else if (CollectionUtils.isEmpty(locationIds)
                    && CollectionUtils.isNotEmpty(supplierIds)
                    && CollectionUtils.isNotEmpty(toolMakerIds)) {//supplier + toolmaker
                builder.and(mold.supplier.id.in(supplierIds)
                        .or(mold.toolMaker.id.in(toolMakerIds)));
            } else if (CollectionUtils.isEmpty(locationIds)
                    && CollectionUtils.isEmpty(supplierIds)
                    && CollectionUtils.isNotEmpty(toolMakerIds)) {//toolmaker
                builder.and(mold.toolMaker.id.in(toolMakerIds));
            } else if (CollectionUtils.isEmpty(locationIds)
                    && CollectionUtils.isNotEmpty(supplierIds)
                    && CollectionUtils.isEmpty(toolMakerIds)) {//supplier
                builder.and(mold.supplier.id.in(supplierIds));
            } else if (CollectionUtils.isEmpty(locationIds)
                    && CollectionUtils.isEmpty(supplierIds)
                    && CollectionUtils.isEmpty(toolMakerIds)) {//none
                builder.and(mold.id.isNull());//trick for none
            }
        }

        return builder;
    }
*/

    public BooleanBuilder getMoldOverviewFilter(QMold mold) {
        BooleanBuilder builder = new BooleanBuilder();
        if (checkExistedGeneralFilter()) {
            //get dashboard general filter
            List<Long> supplierIds = getSupplierIdsOverview();
            List<Long> toolMakerIds = getToolMakerIdsOverview();
            List<Long> locationIds = getLocationIdsOverview();

            if (locationIds != null
                    && supplierIds != null
                    && toolMakerIds != null) {// location + supplier + toolmaker
                builder.and(mold.location.id.in(locationIds)
                        .or(mold.supplier.id.in(supplierIds))
                        .or(mold.toolMaker.id.in(toolMakerIds)));
            }
        }

        return builder;
    }

    public BooleanBuilder getPartFilter(QPart part) {
        QMoldPart moldPart = QMoldPart.moldPart;
        QMold mold = QMold.mold;
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}
        builder.and(part.enabled.isTrue());
        if (!checkExistedGeneralFilter())
            return builder;
        else {
            BooleanBuilder subBuilder = new BooleanBuilder();
            List<Long> partIds = getPartIds();
            DashboardGeneralFilter filter = getDashboardGeneralFilter();

            subBuilder.and(part.id.notIn(JPAExpressions
                            .select(moldPart.partId)
                            .from(moldPart)
                            .where(part.moldParts.size().eq(1).and(mold.companyId.in(JPAExpressions
                                    .select(company.id)
                                    .from(company)
                                    .where(company.isEmoldino.isTrue())))))
                    .or(part.moldParts.isEmpty()));

            if (!filter.isAllLocation() || !filter.isAllSupplier() || !filter.isAllToolMaker()) {
                subBuilder.and(part.id.in(JPAExpressions
                        .select(moldPart.partId).distinct()
                        .from(moldPart)
                        .where(getMoldFilter(moldPart.mold))));
            }
            if (CollectionUtils.isNotEmpty(partIds))
                subBuilder.and(part.id.in(partIds));

            builder.and(subBuilder);
        }
        return builder;
    }

    public BooleanBuilder getPartFilterCustom(QPart part, DashboardGeneralFilter filter) {
        QMoldPart moldPart = QMoldPart.moldPart;
        QMold mold = QMold.mold;
        QCompany company = QCompany.company;
        BooleanBuilder builder = new BooleanBuilder();
		if (AccessControlUtils.isAccessFilterRequired()) {
			builder.and(part.id.in(JPQLQueryUtils.getPartIdsSubquery())//
//					.or(part.createdBy.eq(SecurityUtils.getUserId()))//
			);
		}
        builder.and(part.enabled.isTrue());
        if (filter == null)
            return builder;
        else {
            BooleanBuilder subBuilder = new BooleanBuilder();
            List<Long> partIds = getPartIdsCustom(filter);

            subBuilder.and(part.id.notIn(JPAExpressions
                            .select(moldPart.partId)
                            .from(moldPart)
                            .where(part.moldParts.size().eq(1).and(mold.companyId.in(JPAExpressions
                                    .select(company.id)
                                    .from(company)
                                    .where(company.isEmoldino.isTrue())))))
                    .or(part.moldParts.isEmpty()));

            if (!filter.isAllLocation() || !filter.isAllSupplier() || !filter.isAllToolMaker()) {
                subBuilder.and(part.id.in(JPAExpressions
                        .select(moldPart.partId).distinct()
                        .from(moldPart)
                        .where(getMoldFilter(moldPart.mold))));
            }
            if (CollectionUtils.isNotEmpty(partIds))
                subBuilder.and(part.id.in(partIds));

            builder.and(subBuilder);
        }
        return builder;
    }

    public BooleanBuilder getPartFilter_Old(QPart part) {
        BooleanBuilder builder = new BooleanBuilder();
        List<Long> ids = getPartIds();
        if (CollectionUtils.isNotEmpty(ids)) {
            builder.and(part.id.in(ids));
        } else {
            builder.and(part.id.isNull());//trick for none
        }
        return builder;
    }

    public BooleanBuilder getSupplierFilter(QCompany company) {
        QMold mold = QMold.mold;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(company.enabled.isTrue());
        builder.and(company.isEmoldino.isFalse());
        if (AccessControlUtils.isAccessFilterRequired()) {
            builder.and(company.id.in(AccessControlUtils.getAllAccessibleCompanyIds()));
        }
        if (!checkExistedGeneralFilter())
            return builder;
        else {
            builder.and(company.id.in(JPAExpressions
                    .select(mold.supplierCompanyId).distinct()
                    .from(mold)
                    .where(getMoldFilter(mold))));
        }
        return builder;
    }

    public BooleanBuilder getLocationFilter(QLocation location) {
        QMold mold = QMold.mold;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(location.enabled.isTrue());
        builder.and(location.company.isEmoldino.isFalse());
        if (!checkExistedGeneralFilter())
            return builder;
        else {
            builder.and(location.id.in(JPAExpressions.select(mold.locationId).distinct()
                    .from(mold)
                    .where(getMoldFilter(mold))));
        }
        return builder;
    }

    public BooleanBuilder getCompanyFilter(QCompany company) {
        BooleanBuilder builder = new BooleanBuilder();
        QLocation location = QLocation.location;
        List<Long> locationIds = getLocationIds();
        Set<Long> ids = new HashSet<>(getCompanyIds());

        BooleanBuilder subBuilder = new BooleanBuilder();
        if (CollectionUtils.isEmpty(ids) && CollectionUtils.isEmpty(locationIds)){
            builder.and(company.id.isNull());//trick for none
        } else {
            if (CollectionUtils.isNotEmpty(ids)) {
                subBuilder.and(company.id.in(ids));
            }
            if (CollectionUtils.isNotEmpty(locationIds)) {
                subBuilder.and(company.id.in(JPAExpressions
                        .select(location.companyId).distinct()
                        .from(location)
                        .where(location.id.in(locationIds))));
            }
            builder.and(subBuilder);
        }
        return builder;
    }

    public BooleanBuilder getCategoryFilter(QCategory category) {
        BooleanBuilder builder = new BooleanBuilder();
        QPart part = QPart.part;
        List<Long> partIds = getPartIds_Old();
        if (CollectionUtils.isNotEmpty(partIds)) {
            builder.and(category.id.in(JPAExpressions
                    .select(part.category.parentId)
                    .from(part)
                    .where(part.id.in(partIds).and(part.categoryId.isNotNull())))
            );
        }
        return builder;
    }

    public BooleanBuilder getMachineFilter(QMachine machine) {
        BooleanBuilder builder = new BooleanBuilder();
        List<Long> locationIds = getLocationIds();
        Set<Long> companyIds = new HashSet<>(getCompanyIds());

        BooleanBuilder subBuilder = new BooleanBuilder();

        if (CollectionUtils.isEmpty(companyIds) && CollectionUtils.isEmpty(locationIds)) {
            builder.and(machine.id.isNull());//trick for none
        } else {
            if (CollectionUtils.isNotEmpty(companyIds))
                subBuilder.or(machine.companyId.in(companyIds));
            if (CollectionUtils.isNotEmpty(locationIds))
                subBuilder.or(machine.locationId.in(locationIds));

            builder.and(subBuilder);
        }

        return builder;
    }
}
