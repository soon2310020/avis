package com.stg.service.impl;

import com.stg.entity.MicPackage;
import com.stg.repository.MicPackageRepository;
import com.stg.service.MicPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class MicPackageImpl implements MicPackageService {
    private final MicPackageRepository micPackageRepository;

    @Override
    @Cacheable(value = "cache:micPackagesOrderIdAsc", unless = "#result.size()==0")
    public List<MicPackage> list() {
        List<MicPackage> packages = micPackageRepository.findAllByOrderByIdAsc();
        return packages;
    }

    @Override
    @Cacheable(value = "cache:micPackages", unless = "#result.size()==0")
    public List<MicPackage> findAll() {
        return micPackageRepository.findAll();
    }

}
