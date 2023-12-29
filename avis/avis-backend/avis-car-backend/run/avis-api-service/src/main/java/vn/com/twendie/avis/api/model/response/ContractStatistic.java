package vn.com.twendie.avis.api.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ContractStatistic implements Serializable {

    @JsonProperty("waiting_assign_car")
    private int waitingAssignCar;

    @JsonProperty("assigned_car")
    private int assignedCar;

    @JsonProperty("in_progress")
    private int inProgress;

    @JsonProperty("finished")
    private int finished;

    @JsonProperty("canceled")
    private int canceled;

    @JsonProperty("lending_car")
    private int lendingCar;

    @JsonProperty("lending_driver")
    private int lendingDriver;

    @JsonProperty("lending_car_and_driver")
    private int lendingCarAndDriver;

}
