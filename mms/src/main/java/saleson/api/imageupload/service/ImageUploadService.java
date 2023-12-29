package saleson.api.imageupload.service;

import com.emoldino.api.common.resource.composite.flt.dto.*;
import com.emoldino.framework.repository.Q;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import saleson.api.category.CategoryRepository;
import saleson.api.category.payload.CategoryParam;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.api.counter.CounterRepository;
import saleson.api.counter.payload.CounterPayload;
import saleson.api.imageupload.dto.*;
import saleson.api.location.LocationRepository;
import saleson.api.location.LocationService;
import saleson.api.location.payload.LocationPayload;
import saleson.api.machine.MachineRepository;
import saleson.api.machine.payload.MachinePayload;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartRepository;
import saleson.api.part.payload.PartPayload;
import saleson.api.terminal.TerminalRepository;
import saleson.api.terminal.payload.TerminalPayload;
import saleson.common.enumeration.StorageType;
import saleson.common.service.FileStorageService;
import saleson.common.util.StringUtils;
import saleson.model.*;
import saleson.model.data.CounterToolingCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImageUploadService {
    @Autowired
    FileStorageService fileStorageService;

    public Page<MoldDto> getTooling(ImageUploadIn input, Pageable pageable) {
        MoldPayload payload = new MoldPayload();
        payload.setServerName(ConfigUtils.getServerName());
        payload.setCompanyId(input.getCompanyId());
        payload.setLocationId(input.getPlantId());
        payload.setEquipmentCode(input.getEquipmentCode());
        payload.setToolingOrSensorCode(input.getToolingOrSenSorCode());
//        Page<Mold> page = BeanUtils.get(MoldRepository.class).findAll(payload.getPredicate(), pageable);

        JPQLQuery<Mold> query = BeanUtils.get(JPAQueryFactory.class)//
                .select(Q.mold)//
                .distinct()//
                .from(Q.mold)
                .leftJoin(Q.counter).on(Q.mold.counterId.eq(Q.counter.id))
                .where(payload.getPredicate())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        JPQLQuery<Long> queryCount = BeanUtils.get(JPAQueryFactory.class)//
                .select(Q.mold.id.countDistinct())//
                .distinct()//
                .from(Q.mold)
                .leftJoin(Q.counter).on(Q.mold.counterId.eq(Q.counter.id))
                .where(payload.getPredicate());
       List<Mold> molds =  query.fetch();
       long totalElement = queryCount.fetchCount();

        List<MoldDto> content = molds.stream().map(mold ->
            new MoldDto(mold, fileStorageService.countAll(StorageType.MOLD_CONDITION, mold.getId()))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, totalElement);

    }

    public Page<MachineDto> getMachine(ImageUploadIn input, Pageable pageable) {
        MachinePayload payload = new MachinePayload();
        payload.setSelectedFields(new ArrayList<>());
        payload.setCompanyId(input.getCompanyId());
        payload.setLocationId(input.getPlantId());
        payload.setQueryMobile(input.getEquipmentCode());

        Page<Machine> page = BeanUtils.get(MachineRepository.class).findAll(payload.getPredicate(), pageable);

        List<MachineDto> content = page.getContent().stream().map(machine ->
            new MachineDto(machine,fileStorageService.countAll(StorageType.MACHINE_PICTURE, machine.getId()))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<TerminalDto> getTerminal(ImageUploadIn input, Pageable pageable) {
        TerminalPayload payload = new TerminalPayload();
        payload.setSelectedFields(new ArrayList<>());
        payload.setCompanyId(input.getCompanyId());
        payload.setLocationId(input.getPlantId());
        payload.setQuery(input.getEquipmentCode());

        Page<Terminal> page = BeanUtils.get(TerminalRepository.class).findAll(payload.getPredicate(), pageable);

        List<TerminalDto> content = page.getContent().stream().map(model ->
            new TerminalDto(model,fileStorageService.countAll(StorageType.TERMINAL, model.getId()))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<SensorDto> getSensor(ImageUploadIn input, Pageable pageable) {
        CounterPayload payload = new CounterPayload();
        payload.setSelectedFields(new ArrayList<>());
        payload.setCompanyId(input.getCompanyId());
        payload.setLocationId(input.getPlantId());
        payload.setQuery(input.getEquipmentCode());
        // todo: company, location for filter
        Page<Counter> page = BeanUtils.get(CounterRepository.class).findAll(payload.getPredicate(), pageable);

        //todo: terminal list
        List<CounterToolingCode> tici = BeanUtils.get(TerminalRepository.class).findAllLastStatisticsTerminalCounter();

        List<SensorDto> content = page.getContent().stream().map(model ->
           new SensorDto(model, fileStorageService.countAll(StorageType.MOLD_COUNTER, model.getId()),
               tici.stream().filter(t-> model.getEquipmentCode().equals(t.getCounterCode())).map(t->t.getTerminalCode()).findFirst().orElse(null))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<CompanyDto> getCompany(ImageUploadIn input, Pageable pageable) {
        CompanyPayload payload = new CompanyPayload();
        payload.setEnabled(true);
        payload.setName(input.getCompanyName());

        if (input.getCompanyId() != null)
            payload.setIds(Arrays.asList(input.getCompanyId()));
        // todo:plantId

        Page<Company> page = BeanUtils.get(CompanyService.class).findAll(payload.getPredicate(), pageable);
        List<CompanyDto> content = page.getContent().stream().map(company -> {
           List<Location> locations= BeanUtils.get(LocationRepository.class).findByCompanyIdAndEnabledIsTrue(company.getId());
           return new CompanyDto(company
               ,fileStorageService.countAll(StorageType.COMPANY_PICTURE, company.getId())
               ,locations.stream().map(location -> new FltPlant(location)).collect(Collectors.toList()));
        }).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<PlantDto> getPlant(ImageUploadIn input, Pageable pageable) {
        LocationPayload payload = new LocationPayload();
        payload.setSelectedFields(new ArrayList<>());
        payload.setCompanyId(input.getCompanyId());
        payload.setName(input.getPlantName());
        if (input.getPlantId() != null)
            payload.setIds(Arrays.asList(input.getPlantId()));
        Page<Location> page = BeanUtils.get(LocationService.class).findLocationData(payload.getPredicate(), pageable, null);
        List<PlantDto> content = page.getContent().stream().map(model ->
            new PlantDto(model,fileStorageService.countAll(StorageType.LOCATION_PICTURE, model.getId()))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<ProductDto> getProduct(ImageUploadIn input, Pageable pageable) {
        CategoryParam payload = new CategoryParam();
        payload.setEnabled(true);
        payload.setLevel(3);
        //todo: search by company, location

        Page<Category> page = BeanUtils.get(CategoryRepository.class).findAll(payload.getPredicateProduct(), pageable);
        List<ProductDto> content = page.getContent().stream().map(product ->
            new ProductDto(product,fileStorageService.countAll(StorageType.PROJECT_IMAGE, product.getId()))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<PartDto> getPart(ImageUploadIn input, Pageable pageable) {
        PartPayload payload = new PartPayload();
        payload.setStatus("active");
        payload.setName(input.getPartName());
        if (input.getEquipmentCode()!=null&& !StringUtils.isEmpty(input.getEquipmentCode())){
            payload.setQuery(input.getEquipmentCode());
            payload.setSearchByTooling(true);
        }
        //todo: search by company, location
        Page<Part> page = BeanUtils.get(PartRepository.class).findAll(payload.getPredicate(), pageable);
        List<PartDto> content = page.getContent().stream().map(part -> new PartDto(part
            ,fileStorageService.countAll(StorageType.PART_PICTURE, part.getId()))).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public Page<BrandDto> getBrand(ImageUploadIn input, Pageable pageable) {
        CategoryParam payload = new CategoryParam();
        payload.setEnabled(true);
        payload.setLevel(2);
        //todo: search by company, location
        Page<Category> page = BeanUtils.get(CategoryRepository.class).findAll(payload.getPredicateBrand(), pageable);
        List<BrandDto> content = page.getContent().stream().map(product ->
            new BrandDto(product,fileStorageService.countAll(StorageType.PROJECT_IMAGE, product.getId()))
        ).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }
}
