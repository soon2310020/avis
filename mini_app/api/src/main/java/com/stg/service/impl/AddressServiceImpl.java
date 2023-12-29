package com.stg.service.impl;

import com.stg.entity.address.AddressDistrict;
import com.stg.entity.address.AddressProvince;
import com.stg.entity.address.AddressWard;
import com.stg.repository.AddressDistrictRepository;
import com.stg.repository.AddressProvinceRepository;
import com.stg.repository.AddressWardRepository;
import com.stg.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AddressServiceImpl implements AddressService {
    private final AddressProvinceRepository addressProvinceRepository;
    private final AddressDistrictRepository addressDistrictRepository;
    private final AddressWardRepository addressWardRepository;

    @Override
    @Cacheable(value = "cache:searchProvinces", unless = "#result.size()==0")
    public List<AddressProvince> searchProvinces(String provinceCode, String provinceName) {
        return addressProvinceRepository.findByCodeAndName(provinceCode, provinceName);
    }

    @Override
    @Cacheable(value = "cache:searchDistricts", unless = "#result.size()==0")
    public List<AddressDistrict> searchDistricts(String provinceCode, String districtCode, String districtName) {
        return addressDistrictRepository.findByProvinceAndCodeAndName(provinceCode, districtCode, districtName);
    }

    @Override
    @Cacheable(value = "cache:searchWards", unless = "#result.size()==0")
    public List<AddressWard> searchWards(String districtCode, String wardCode, String wardName) {
        return addressWardRepository.findByDistrictAndCodeAndName(districtCode, wardCode, wardName);
    }
}
