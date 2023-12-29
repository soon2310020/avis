package com.stg.service;

import com.stg.entity.address.AddressDistrict;
import com.stg.entity.address.AddressProvince;
import com.stg.entity.address.AddressWard;

import java.util.List;

public interface AddressService {
    List<AddressProvince> searchProvinces(String provinceCode, String provinceName);

    List<AddressDistrict> searchDistricts(String provinceCode, String districtCode, String districtName);

    List<AddressWard> searchWards(String districtCode, String wardCode, String wardName);
}
