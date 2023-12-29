package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.stereotype.Service;
import vn.com.twendie.avis.data.model.SystemLog;
import vn.com.twendie.avis.mobile.api.repository.SystemLogRepo;
import vn.com.twendie.avis.mobile.api.service.SystemLogService;

@CacheConfig(cacheNames = "SystemLogService")
@Service
public class SystemLogServiceImpl implements SystemLogService {
    private final SystemLogRepo systemLogRepo;

    public SystemLogServiceImpl(SystemLogRepo systemLogRepo) {
        this.systemLogRepo = systemLogRepo;
    }
    @Override
    public SystemLog save(SystemLog systemLog){
        systemLogRepo.save(systemLog);
        return systemLog;
    }

}
