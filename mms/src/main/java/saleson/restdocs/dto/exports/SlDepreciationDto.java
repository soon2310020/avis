package saleson.restdocs.dto.exports;

import com.emoldino.framework.util.DateUtils2;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import saleson.model.Mold;
import saleson.service.util.DateTimeUtils;

import static saleson.common.service.ExportService.getNumberWithCurrency;

@Data
public class SlDepreciationDto {
    private String slCurrentBookValue;
    private String slDepreciation;
    private String slDepreciationTerm;
    private String slYearlyDepreciation;
    private String slLatestDepreciationPoint;

    public SlDepreciationDto(Mold mold, Integer offsetClient) {
        this.slCurrentBookValue = getNumberWithCurrency(mold.getSlCurrentBookValue(), mold.getCostCurrencyType());
        this.slDepreciation = mold.getSlDepreciationTitle();
        this.slDepreciationTerm = String.valueOf(mold.getSlDepreciationTerm());
        this.slYearlyDepreciation = getNumberWithCurrency(mold.getSlYearlyDepreciation() != null ? mold.getSlYearlyDepreciation() : 0, mold.getCostCurrencyType());
        this.slLatestDepreciationPoint = DateTimeUtils.formatDateTimeWithTimeZone(mold.getSlLatestDepreciationPoint(), offsetClient, DateUtils2.DatePattern.yyyy_MM_dd);
    }
}
