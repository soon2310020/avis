package saleson.restdocs.dto.exports;

import com.emoldino.framework.util.DateUtils2;
import lombok.Data;
import saleson.model.Mold;
import saleson.service.util.DateTimeUtils;

import static saleson.common.service.ExportService.getNumberWithCurrency;

@Data
public class UpDepreciationDto {
    private String upCurrentBookValue;
    private String depreciationPercentage;
    private String upDepreciationTerm;
    private String upDepreciationPerShot;
    private String upLatestDepreciationPoint;

    public UpDepreciationDto(Mold mold, Integer offsetClient) {
        this.upCurrentBookValue = getNumberWithCurrency(mold.getUpCurrentBookValue(), mold.getCostCurrencyType());
        this.depreciationPercentage =  mold.getDepreciationPercentageTitle();
        this.upDepreciationTerm = mold.getUpDepreciationTerm()+" Shots";
        this.upDepreciationPerShot = getNumberWithCurrency(mold.getUpDepreciationPerShot(), mold.getCostCurrencyType());
        this.upLatestDepreciationPoint = DateTimeUtils.formatDateTimeWithTimeZone(mold.getUpLatestDepreciationPoint(), offsetClient, DateUtils2.DatePattern.yyyy_MM_dd);
    }
}
