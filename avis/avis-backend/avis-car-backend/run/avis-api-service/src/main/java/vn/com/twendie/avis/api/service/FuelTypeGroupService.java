package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.FuelTypeGroup;

import java.util.List;

public interface FuelTypeGroupService {

    FuelTypeGroup findById(Long id);

    List<FuelTypeGroup> findByDeletedFalse();
}
