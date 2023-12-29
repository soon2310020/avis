package saleson.api.statistics;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.common.enumeration.WUTType;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.model.Mold;
import saleson.model.StatisticsWut;
import saleson.model.data.supplierReport.SupplierProductionData;
import saleson.model.data.wut.WUTFullData;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class StatisticsWutService {
    @Lazy
    @Autowired
    StatisticsWutRepository statisticsWutRepository;

    public List<StatisticsWut> saveList(Mold mold, List<WUTFullData> wutFullDataList,boolean valData) {
        List<StatisticsWut> listSave = new ArrayList<>();
        if (mold != null && wutFullDataList != null) {

            for (WUTFullData wutFullData : wutFullDataList) {
                List<StatisticsWut> oldData = statisticsWutRepository.findAllByMoldIdAndStartedAtAndValData(mold.getId(), wutFullData.getStartedAt(),valData);
                if (StringUtils.isEmpty(wutFullData.getEndAt()) && !StringUtils.isEmpty(wutFullData.getStartedAt()) && wutFullData.getHour() != null) {
                    Instant end = DateUtils.getInstant(wutFullData.getStartedAt(), DateUtils.DEFAULT_DATE_FORMAT).plus(Double.valueOf(wutFullData.getHour() * 3600).intValue(), ChronoUnit.SECONDS);
                    wutFullData.setEndAt(DateUtils.getDate(end, DateUtils.DEFAULT_DATE_FORMAT));
                }
                StatisticsWut statisticsWut = new StatisticsWut(mold.getId(), mold.getEquipmentCode(), wutFullData.getTitle(), wutFullData.getHour(), wutFullData.getShotCount(), wutFullData.getStartedAt(),wutFullData.getEndAt(),valData);
                //update
                if (!oldData.isEmpty()) {
                    statisticsWut = oldData.get(0);
                    statisticsWut.setMoldCode(mold.getEquipmentCode());
                    statisticsWut.setWutType(WUTType.getEnumByTitle(wutFullData.getTitle()));
                    statisticsWut.setHourValue(wutFullData.getHour());
                    statisticsWut.setShotCount(wutFullData.getShotCount());
                    statisticsWut.setEndAt(wutFullData.getEndAt());
                }
                listSave.add(statisticsWut);

            }
            statisticsWutRepository.saveAll(listSave);
        }
        return listSave;
    }

/*
    public List<SupplierProductionData>  findProductionOverviewData(List<Long> moldIds, ProductivitySearchPayload payload){
        return statisticsWutRepository.findProductionQuantity(moldIds, payload);
    }
*/
    public List<SupplierProductionData>  findProductionWUTQuantity(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable){
        return statisticsWutRepository.findProductionWUTQuantity(moldIds, payload, pageable);
    }

}
