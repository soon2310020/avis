package vn.com.twendie.avis.api.service;

import vn.com.twendie.avis.data.model.RentalServiceType;

public interface RentalServiceTypeService {

    RentalServiceType findByCode(String code);

    RentalServiceType findByIdIgnoreDelete(Long id);

}
