package vn.com.twendie.avis.api.service.impl;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.repository.*;
import vn.com.twendie.avis.api.service.*;
import vn.com.twendie.avis.data.model.CostType;
import vn.com.twendie.avis.data.model.NormList;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class CacheServiceImpl implements CacheService {

    private final BranchRepo branchRepo;
    private final ContractPeriodTypeRepo contractPeriodTypeRepo;
    private final ContractTypeRepo contractTypeRepo;
    private final FuelTypeRepo fuelTypeRepo;
    private final RentalServiceTypeRepo rentalServiceTypeRepo;
    private final WorkingDayRepo workingDayRepo;
    private final CostTypeRepo costTypeRepo;
    private final NormListRepo normListRepo;
    private final WorkingCalendarRepo workingCalendarRepo;

    private final CacheManager cacheManager;

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheServiceImpl(BranchRepo branchRepo,
                            ContractPeriodTypeRepo contractPeriodTypeRepo,
                            ContractTypeRepo contractTypeRepo,
                            FuelTypeRepo fuelTypeRepo,
                            RentalServiceTypeRepo rentalServiceTypeRepo,
                            WorkingDayRepo workingDayRepo,
                            CostTypeRepo costTypeRepo,
                            NormListRepo normListRepo,
                            WorkingCalendarRepo workingCalendarRepo,
                            CacheManager cacheManager,
                            RedisTemplate<String, Object> redisTemplate) {
        this.branchRepo = branchRepo;
        this.contractPeriodTypeRepo = contractPeriodTypeRepo;
        this.contractTypeRepo = contractTypeRepo;
        this.fuelTypeRepo = fuelTypeRepo;
        this.rentalServiceTypeRepo = rentalServiceTypeRepo;
        this.workingDayRepo = workingDayRepo;
        this.costTypeRepo = costTypeRepo;
        this.normListRepo = normListRepo;
        this.workingCalendarRepo = workingCalendarRepo;
        this.cacheManager = cacheManager;
        this.redisTemplate = redisTemplate;
    }

    @Override
//    @PostConstruct
    public void init() {
        branchRepo.findAll().forEach(branch ->
                Objects.requireNonNull(cacheManager.getCache("Branch"))
                        .put(branch.getCode(), branch));

        contractPeriodTypeRepo.findAll().forEach(contractPeriodType ->
                Objects.requireNonNull(cacheManager.getCache("ContractPeriodType"))
                        .put(contractPeriodType.getId(), contractPeriodType));

        contractTypeRepo.findAll().forEach(contractType ->
                Objects.requireNonNull(cacheManager.getCache("ContractType"))
                        .put(contractType.getId(), contractType));

        fuelTypeRepo.findAll().forEach(fuelType ->
                Objects.requireNonNull(cacheManager.getCache("FuelType"))
                        .put(fuelType.getCode(), fuelType));

        rentalServiceTypeRepo.findAll().forEach(rentalServiceType ->
                Objects.requireNonNull(cacheManager.getCache("RentalServiceType"))
                        .put(rentalServiceType.getCode(), rentalServiceType));

        workingDayRepo.findAll().forEach(workingDay ->
                Objects.requireNonNull(cacheManager.getCache("WorkingDay"))
                        .put(workingDay.getCode(), workingDay));

        List<CostType> costTypes = costTypeRepo.findAll();
        Cache costTypeCache = Objects.requireNonNull(cacheManager.getCache("CostType"));
        costTypeCache.put("all", costTypes);
        costTypes.forEach(costType -> {
            costTypeCache.put(costType.getId(), costType);
            costTypeCache.put(costType.getCode(), costType);
        });

        List<NormList> normLists = normListRepo.findAll();
        Cache normListCache = Objects.requireNonNull(cacheManager.getCache("NormList"));
        normListCache.put("all", normLists);
        normLists.forEach(normList -> {
            normListCache.put(normList.getId(), normList);
            normListCache.put(normList.getCode(), normList);
        });

        Cache workingCalendarCache = Objects.requireNonNull(cacheManager.getCache("WorkingCalendar"));
        workingCalendarCache.put("all", workingCalendarRepo.findAll());
    }

    @Override
    public void clearAll() {
        redisTemplate.delete(ObjectUtils.defaultIfNull(redisTemplate.keys("*"), Collections.emptySet()));
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (Objects.nonNull(cache)) {
                        cache.clear();
                    }
                });
    }

}
