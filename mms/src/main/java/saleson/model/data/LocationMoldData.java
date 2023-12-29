package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import saleson.common.enumeration.ContinentName;

import java.util.Locale;

@Data
public class LocationMoldData {
    private String countryCode;
    private ContinentName continent;
    private Long toolingCount;
    private Double percentage;
    private String countryName;

    @QueryProjection
    public LocationMoldData(String countryCode, ContinentName continent, Long toolingCount){
        this.countryCode = countryCode;
        this.continent = continent;
        this.toolingCount = toolingCount;
        Locale locale = new Locale("", countryCode);
        this.countryName = locale.getDisplayCountry(new Locale("en"));
    }
}
