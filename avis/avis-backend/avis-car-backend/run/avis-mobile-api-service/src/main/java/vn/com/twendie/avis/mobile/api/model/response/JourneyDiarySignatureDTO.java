package vn.com.twendie.avis.mobile.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.core.env.Environment;
import vn.com.twendie.avis.api.core.ApplicationContextProvider;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Data
public class JourneyDiarySignatureDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("time_start")
    private Timestamp timeStart;

    @JsonProperty("time_end")
    private Timestamp timeEnd;

    @JsonProperty("status")
    private boolean status;

    @JsonProperty("trip_itinerary")
    private String tripItinerary;

    @JsonProperty("total_km")
    private Integer totalKm;

    @JsonProperty("cost")
    private Long cost;

    @JsonProperty("license_plate")
    private String licensePlate;

    @JsonProperty("driver")
    private String driver;

    @JsonProperty("contract_code")
    private String contractCode;

    @JsonProperty("signature_image_url")
    private String signatureImageUrl;

    @JsonProperty("used_km_self_drive")
    private Long usedKmSelfDrive;

    @Column(name = "used_km")
    private Long usedKm;

    public void setSignatureImageUrl(String signatureImageUrl){
        this.signatureImageUrl = createImageLink(signatureImageUrl);
    }

    private String createImageLink(String link) {
        if (Objects.nonNull(link)) {
            String[] parts = link.split("/");
            String fileName = parts[parts.length - 1];
            return String.format("%s/images/%s", getImageBaseUrl(), fileName);
        } else {
            return null;
        }
    }

    private String getImageBaseUrl() {
        if (Objects.isNull(imageBaseUrl)) {
            Environment env = ApplicationContextProvider.getApplicationContext().getBean(Environment.class);
            imageBaseUrl = env.getProperty("app.base-url");
        }
        return imageBaseUrl;
    }

    private String imageBaseUrl;

    private String comment;

}
