package vn.com.twendie.avis.api.converter;

import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import vn.com.twendie.avis.data.model.CodeValueModel;
import vn.com.twendie.avis.data.model.JourneyDiaryDailyCostType;

@Component
public class JourneyDiaryDailyCostType2CodeValueModel extends AbstractConverter<JourneyDiaryDailyCostType, CodeValueModel> {

    @Override
    protected CodeValueModel convert(JourneyDiaryDailyCostType source) {
        return CodeValueModel.builder()
                .code(source.getCostType().getCode())
                .value(source.getValue())
                .link(source.getImageCostLink())
                .id(source.getId())
                .build();
    }

}
