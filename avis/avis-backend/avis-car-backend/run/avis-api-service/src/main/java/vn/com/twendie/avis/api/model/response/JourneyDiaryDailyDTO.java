package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import vn.com.twendie.avis.api.core.ApplicationContextProvider;
import vn.com.twendie.avis.data.model.CodeValueModel;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static vn.com.twendie.avis.data.enumtype.JourneyDiaryCostTypeEnum.BREAKDOWN_FEE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JourneyDiaryDailyDTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("journey_diary_id")
    private Long journeyDiaryId;

    @JsonProperty("contract_id")
    private Long contractId;

    @JsonProperty("parent_id")
    private Long parentId;

    @JsonProperty("date")
    private Timestamp date;

    @JsonProperty("month")
    private Timestamp month;

    private String customerNameUsed;

    @JsonProperty("customer_name_used")
    public List<String> getCustomerNameUsed() {
        if (CollectionUtils.isEmpty(children)) {
            return Objects.isNull(customerNameUsed) ? Collections.emptyList() : Collections.singletonList(customerNameUsed);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getCustomerNameUsed)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private String customerDepartment;

    @JsonProperty("customer_department")
    public List<String> getCustomerDepartment() {
        if (CollectionUtils.isEmpty(children)) {
            return Objects.isNull(customerDepartment) ? Collections.emptyList() : Collections.singletonList(customerDepartment);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getCustomerDepartment)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private String tripItinerary;

    @JsonProperty("trip_itinerary")
    public List<String> getTripItinerary() {
        if (CollectionUtils.isEmpty(children)) {
            return Objects.isNull(tripItinerary) ? Collections.emptyList() : Collections.singletonList(tripItinerary);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getTripItinerary)
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    @JsonProperty("km_start")
    private BigDecimal kmStart;

    @JsonProperty("km_customer_get_in")
    private BigDecimal kmCustomerGetIn;

    @JsonProperty("km_customer_get_out")
    private BigDecimal kmCustomerGetOut;

    @JsonProperty("km_end")
    private BigDecimal kmEnd;

    @JsonProperty("total_km")
    private BigDecimal totalKm;

    @JsonProperty("empty_km")
    private BigDecimal emptyKm;

    @JsonProperty("used_km")
    private BigDecimal usedKm;

    @JsonProperty("used_km_self_drive")
    private BigDecimal usedKmSelfDrive;

    @JsonProperty("working_time_app_from")
    private Time workingTimeAppFrom;

    @JsonProperty("working_time_app_to")
    private Time workingTimeAppTo;

    @JsonProperty("working_time_gps_from")
    private Time workingTimeGpsFrom;

    @JsonProperty("working_time_gps_to")
    private Time workingTimeGpsTo;

    @JsonProperty("over_time")
    private Integer overTime;

    @JsonProperty("over_km")
    private BigDecimal overKm;

    @JsonProperty("over_km_self_drive")
    private BigDecimal overKmSelfDrive;

    @JsonProperty("overnight")
    private Integer overnight;

    @Builder.Default
    @JsonProperty("is_holiday")
    private Boolean isHoliday = false;

    @Builder.Default
    @JsonProperty("is_weekend")
    private Boolean isWeekend = false;

    @Builder.Default
    @JsonIgnore
    private Boolean isSelfDrive = false;

    @JsonProperty("is_self_drive")
    public Boolean getBoolSelfDrive() {
        return Objects.nonNull(usedKmSelfDrive);
    }

    @JsonProperty("is_with_driver")
    public Boolean getBoolWithDriver() {
        return Objects.nonNull(usedKm);
    }

    private String driverName;

    @JsonProperty("driver_name")
    public List<String> getDriverName() {
        if (CollectionUtils.isEmpty(children)) {
            return Objects.nonNull(driver) ? Collections.singletonList(driver.getName()) :
                    Objects.nonNull(driverName) ? Collections.singletonList(driverName) : Collections.emptyList();
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getDriverName)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    private String vehicleNumberPlate;

    @JsonProperty("vehicle_number_plate")
    public List<String> getVehicleNumberPlate() {
        if (CollectionUtils.isEmpty(children)) {
            return Objects.nonNull(vehicle) ? Collections.singletonList(vehicle.getNumberPlate()) :
                    Objects.nonNull(vehicleNumberPlate) ? Collections.singletonList(vehicleNumberPlate) : Collections.emptyList();
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getVehicleNumberPlate)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @JsonProperty("image_cost_links")
    public List<String> getImageCostLinks() {
        if (CollectionUtils.isEmpty(children)) {
            return journeyDiaryDailyCostTypes.stream()
                    .filter(item -> !BREAKDOWN_FEE.code().equals(item.getCode()))
                    .map(CodeValueModel::getLink)
                    .filter(Objects::nonNull)
                    .map(this::createImageLink)
                    .collect(Collectors.toList());
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getImageCostLinks)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    @JsonProperty("image_breakdown_link")
    public List<String> getImageBreakdownLink() {
        if (CollectionUtils.isEmpty(children)) {
            return journeyDiaryDailyCostTypes.stream()
                    .filter(item -> BREAKDOWN_FEE.code().equals(item.getCode()))
                    .map(CodeValueModel::getLink)
                    .filter(Objects::nonNull)
                    .map(this::createImageLink)
                    .collect(Collectors.toList());
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getImageBreakdownLink)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private String imageOdoLinks;

    @JsonProperty("image_odo_links")
    public List<String> getImageOdoLinks() {
        if (CollectionUtils.isEmpty(children)) {
            return createImageLinks(imageOdoLinks);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getImageOdoLinks)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private String imageCustomerGetInLink;

    @JsonProperty("image_customer_get_in_link")
    public List<String> getImageCustomerGetInLink() {
        if (CollectionUtils.isEmpty(children)) {
            return createImageLinks(imageCustomerGetInLink);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getImageCustomerGetInLink)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private String imageCustomerGetOutLink;

    @JsonProperty("image_customer_get_out_link")
    public List<String> getImageCustomerGetOutLink() {
        if (CollectionUtils.isEmpty(children)) {
            return createImageLinks(imageCustomerGetOutLink);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getImageCustomerGetOutLink)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private String stationFeeImages;

    @JsonProperty("station_fee_images")
    public List<String> getStationFeeImages() {
        if (CollectionUtils.isEmpty(children)) {
            return createImageLinks(stationFeeImages);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getStationFeeImages)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    private String confirmationScreenshot;

    @JsonProperty("confirmation_screenshot")
    public List<String> getConfirmationScreenshot() {
        if (CollectionUtils.isEmpty(getJourneyDiaryStationFees())) {
            return Collections.emptyList();
        } else {
            if (CollectionUtils.isEmpty(children)) {
                return createImageLinks(confirmationScreenshot);
            } else {
                return children.stream()
                        .map(JourneyDiaryDailyDTO::getConfirmationScreenshot)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toList());
            }
        }
    }

    @Builder.Default
    private Boolean flagOdoRecognitionFailed = false;

    @JsonProperty("flag_odo_recognition_failed")
    public Boolean getFlagOdoRecognitionFailed() {
        return !CollectionUtils.isEmpty(getImageOdoLinks());
    }

    @Builder.Default
    private Boolean flagMultiDate = false;

    @JsonProperty("flag_multi_date")
    private Boolean getFlagMultiDate() {
        if (CollectionUtils.isEmpty(children)) {
            return flagMultiDate;
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getFlagMultiDate)
                    .filter(Objects::nonNull)
                    .anyMatch(b -> b);
        }
    }

    @Builder.Default
    @JsonProperty("flag_unavailable_vehicle")
    private Boolean flagUnavailableVehicle = false;

    @Builder.Default
    private Boolean flagChangedVehicle = false;

    @JsonProperty("flag_changed_vehicle")
    public Boolean getFlagChangedVehicle() {
        if (CollectionUtils.isEmpty(children)) {
            return flagChangedVehicle;
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getFlagChangedVehicle)
                    .filter(Objects::nonNull)
                    .anyMatch(b -> b);
        }
    }

    @Builder.Default
    private Boolean flagChangedKmNorm = false;

    @JsonProperty("flag_changed_km_norm")
    public Boolean getFlagChangedKmNorm() {
        if (CollectionUtils.isEmpty(children)) {
            return flagChangedKmNorm;
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getFlagChangedKmNorm)
                    .filter(Objects::nonNull)
                    .anyMatch(b -> b);
        }
    }

    @Builder.Default
    private Boolean flagChangedWorkingDay = false;

    @JsonProperty("flag_changed_working_day")
    public Boolean getFlagChangedWorkingDay() {
        if (CollectionUtils.isEmpty(children)) {
            return flagChangedWorkingDay;
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getFlagChangedWorkingDay)
                    .filter(Objects::nonNull)
                    .anyMatch(b -> b);
        }
    }

    @Builder.Default
    @JsonProperty("flag_created_manually")
    private Boolean flagCreatedManually = false;

    @JsonProperty("flag_finished_modify")
    private Boolean flagFinishedModify;

    private String note;

    @JsonProperty("note")
    public List<String> getNote() {
        if (CollectionUtils.isEmpty(children)) {
            return Objects.isNull(note) ? Collections.emptyList() : Collections.singletonList(note);
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getNote)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    @JsonIgnore
    private UserDTO driver;

    @JsonIgnore
    private VehicleDTO vehicle;

    @JsonProperty("children")
    private List<JourneyDiaryDailyDTO> children;

    @JsonProperty("costs")
    private List<CodeValueModel> journeyDiaryDailyCostTypes;

    private Set<JourneyDiaryStationFeeDTO> journeyDiaryStationFees;

    @JsonProperty("journey_diary_station_fees")
    public List<JourneyDiaryStationFeeDTO> getJourneyDiaryStationFees() {
        if (CollectionUtils.isEmpty(children)) {
            return CollectionUtils.emptyIfNull(journeyDiaryStationFees)
                    .stream()
                    .sorted(Comparator.comparingLong(JourneyDiaryStationFeeDTO::getId))
                    .collect(Collectors.toList());
        } else {
            return children.stream()
                    .map(JourneyDiaryDailyDTO::getJourneyDiaryStationFees)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        }
    }

    @JsonIgnore
    private String dayOfWeek;

    private String imageBaseUrl;

    private String getImageBaseUrl() {
        if (Objects.isNull(imageBaseUrl)) {
            Environment env = ApplicationContextProvider.getApplicationContext().getBean(Environment.class);
            imageBaseUrl = env.getProperty("app.base-url");
        }
        return imageBaseUrl;
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

    private List<String> createImageLinks(String link) {
        return Arrays.stream(StringUtils.split(StringUtils.defaultIfBlank(link, EMPTY), "\n"))
                .map(this::createImageLink)
                .collect(Collectors.toList());
    }

    @JsonProperty("signature_image_url")
    private List<String> signatureImageUrl;
    public void setSignatureImageUrl(List<String> signatureImageUrl){
        if(signatureImageUrl == null){
            return;
        }
        this.signatureImageUrl = signatureImageUrl.stream()
                .map(this::createImageLink).collect(Collectors.toList());
    }

    @JsonProperty("signature_status")
    private Boolean signatureStatus;

    @JsonProperty("signature_comment")
    private List<String> signatureComment;

    @JsonProperty("name_finds")
    private String nameFinds;
}
