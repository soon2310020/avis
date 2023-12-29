package vn.com.twendie.avis.api.converter;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.ContractCostType;

@Component
public class ContractCostType2CodeValueModel extends AbstractConverter<ContractCostType, CodeValueModel> {

    @Override
    public CodeValueModel convert(ContractCostType source) {
        return CodeValueModel.builder()
                .code(source.getId().getCostType().getCode())
                .value(source.getPrice())
                .build();
    }

}
