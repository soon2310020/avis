package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.VehicleSupplierGroup;

import java.util.List;

public interface VehicleSupplierGroupService {

    VehicleSupplierGroup findById(Long id);

    List<VehicleSupplierGroup> findByDeletedFalse();
}
