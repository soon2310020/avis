package vn.com.twendie.avis.api.model.projection;

public interface OvertimeInfo {

    Long getContractId();

    Long getRealWorkingDay();

    Long getOvertime();

    Long getOverNight();

    Long getWeekend();

    Long getHoliday();

}
