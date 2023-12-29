package vn.com.twendie.avis.mobile.api.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.api.rest.exception.NotFoundException;
import vn.com.twendie.avis.data.model.CostType;
import vn.com.twendie.avis.locale.config.Translator;
import vn.com.twendie.avis.mobile.api.repository.CostTypeRepo;
import vn.com.twendie.avis.mobile.api.service.CostTypeService;

@Service
public class CostTypeServiceImpl implements CostTypeService {

    private final CostTypeRepo costTypeRepo;

    public CostTypeServiceImpl(CostTypeRepo costTypeRepo) {
        this.costTypeRepo = costTypeRepo;
    }

    @Override
    public CostType findByCode(String code) {

        return costTypeRepo.findByCode(code).orElseThrow(() ->
                new NotFoundException("Not found code type with code: " + code)
                        .displayMessage(Translator.toLocale("cost_type.invalid_cost_type")));

    }
}
