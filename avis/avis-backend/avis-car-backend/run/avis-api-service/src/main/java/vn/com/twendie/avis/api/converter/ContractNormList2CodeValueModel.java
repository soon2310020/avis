package vn.com.twendie.avis.api.converter;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.ContractNormList;

@Component
public class ContractNormList2CodeValueModel extends AbstractConverter<ContractNormList, CodeValueModel> {

    @Override
    public CodeValueModel convert(ContractNormList source) {
        return CodeValueModel.builder()
                .code(source.getId().getNormList().getCode())
                .value(source.getQuota())
                .build();
    }

}