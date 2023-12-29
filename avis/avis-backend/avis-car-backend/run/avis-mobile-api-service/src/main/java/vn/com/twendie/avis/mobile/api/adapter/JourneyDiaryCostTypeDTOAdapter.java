package vn.com.twendie.avis.mobile.api.adapter;

import vn.com.twendie.avis.data.model.JourneyDiaryCostType;
import vn.com.twendie.avis.mobile.api.model.response.JourneyDiaryCostTypeDTO;

import java.util.Objects;
import java.util.function.Function;

public class JourneyDiaryCostTypeDTOAdapter implements Function<JourneyDiaryCostType, JourneyDiaryCostTypeDTO> {

    @Override
    public JourneyDiaryCostTypeDTO apply(JourneyDiaryCostType journeyDiaryCostType) {

        if (Objects.isNull(journeyDiaryCostType)) {
            return null;
        }

        return JourneyDiaryCostTypeDTO.builder()
                .id(journeyDiaryCostType.getId())
                .journeyDiaryId(journeyDiaryCostType.getJourneyDiary().getId())
                .costTypeId(journeyDiaryCostType.getCostType().getId())
                .value(journeyDiaryCostType.getValue())
                .imageCostLink(journeyDiaryCostType.getImageCostLink())
                .build();
    }

}
