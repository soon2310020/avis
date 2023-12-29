package vn.com.twendie.avis.api.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.core.util.DateUtils;
import vn.com.twendie.avis.api.model.payload.CreateOrUpdateJDDLockPayload;
import vn.com.twendie.avis.api.repository.JourneyDiaryDailyLockRepo;
import vn.com.twendie.avis.api.service.JourneyDiaryDailyLockService;
import vn.com.twendie.avis.data.model.JourneyDiaryDailyLock;
import vn.com.twendie.avis.data.model.User;

import java.sql.Timestamp;
import java.util.Objects;

@Service
public class JourneyDiaryDailyLockServiceImpl implements JourneyDiaryDailyLockService {

    private final JourneyDiaryDailyLockRepo journeyDiaryDailyLockRepo;
    private final DateUtils dateUtils;
    private final ModelMapper modelMapper;
    private final CacheManager cacheManager;

    public JourneyDiaryDailyLockServiceImpl(JourneyDiaryDailyLockRepo journeyDiaryDailyLockRepo,
                                            DateUtils dateUtils,
                                            ModelMapper modelMapper,
                                            CacheManager cacheManager) {
        this.journeyDiaryDailyLockRepo = journeyDiaryDailyLockRepo;
        this.dateUtils = dateUtils;
        this.modelMapper = modelMapper;
        this.cacheManager = cacheManager;
    }

    @Override
    @Cacheable(cacheNames = "JourneyDiaryDailyLock", key = "#root.method.name")
    public JourneyDiaryDailyLock find() {
        return journeyDiaryDailyLockRepo.findFirstByDeletedFalse();
    }

    @Override
    public JourneyDiaryDailyLock createOrUpdate(CreateOrUpdateJDDLockPayload payload, User user) {
        JourneyDiaryDailyLock journeyDiaryDailyLock = find();
        Timestamp newLockTime = dateUtils.getFirstDayOfMonth(payload.getLockTime());

        if (Objects.isNull(journeyDiaryDailyLock)) {
            journeyDiaryDailyLock = JourneyDiaryDailyLock.builder()
                    .lockTime(newLockTime)
                    .updatedBy(user)
                    .build();

            clearFilterOptionsCache();
            return journeyDiaryDailyLockRepo.save(journeyDiaryDailyLock);
        } else if (!journeyDiaryDailyLock.getLockTime().equals(newLockTime)){
            journeyDiaryDailyLock.setLockTime(newLockTime);
        }

        journeyDiaryDailyLock.setUpdatedBy(user);
        clearFilterOptionsCache();
        return journeyDiaryDailyLock;
    }

    private void clearFilterOptionsCache() {
        Cache filterCache = cacheManager.getCache("JDDFilterOptions");
        Cache cache = cacheManager.getCache("JourneyDiaryDailyLock");

        if (Objects.nonNull(cache)) {
            cache.clear();
        }

        if (Objects.nonNull(filterCache)) {
            filterCache.clear();
        }
    }
}
