package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.FuelType;

import java.util.List;

public interface FuelTypeService {

    FuelType findByCode(String code);

    FuelType findByIdIgnoreDelete(Long id);

    List<FuelType> findByGroupId(Long groupId);

}
