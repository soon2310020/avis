package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRWithoutDriverDiary {

    @JsonProperty("date")
    private String date;

    @JsonIgnore
    private Timestamp startDate;

    @JsonProperty("km_start")
    private BigDecimal kmStart;

    @JsonProperty("km_end")
    private BigDecimal kmEnd;

    @JsonProperty("used_km")
    private BigDecimal usedKm;

    @JsonProperty("over_km")
    private BigDecimal overKm;

    @JsonProperty("driver_name")
    private List<String> driverName;

    @JsonProperty("vehicle_number_plate")
    private List<String> vehicleNumberPlate;

    @JsonProperty("note")
    private List<String> note;
}
