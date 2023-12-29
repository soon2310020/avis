package vn.com.twendie.avis.api.controller;

import org.apache.commons.compress.utils.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.com.twendie.avis.api.rest.model.ApiResponse;
import vn.com.twendie.avis.api.service.WorkingCalendarService;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@RestController
@RequestMapping("/working-calendar")
public class WorkingCalendarController {

    private final WorkingCalendarService workingCalendarService;

    public WorkingCalendarController(WorkingCalendarService workingCalendarService){
        this.workingCalendarService = workingCalendarService;
    }

    @PostMapping("/import/{year}")
    public ApiResponse<String> importWC(@PathVariable("year") int year){
        workingCalendarService.importWorkingCalendar(year);
        return ApiResponse.success("Success");
    }

    @PostMapping("/fix/{year}")
    public ApiResponse<String> fixWorkingCalendar(@PathVariable("year") int year){
        workingCalendarService.fixWorkingCalendar(year);
        return ApiResponse.success("Success");
    }

    @PostMapping("/fix-journey-diary-daily")
    public ApiResponse<String> fixJourneyDiaryDailyWeekendMonToSat(@RequestParam(required = false) Integer type){
        workingCalendarService.fixJourneyDiaryDailyWeekend(type);
        return ApiResponse.success("Successfully");
    }
    @PostMapping("/fix-journey-diary-daily-missing-working-calendar")
    public ApiResponse<String> fixJourneyDiaryDailyMissingWorkingCalendar(@RequestParam Integer year,@RequestParam(required = false) String from
            ,@RequestParam(required = false) String to){
        new Thread(() -> {
        workingCalendarService.fixJourneyDiaryDailyMissingWorkingCalendar(year,from,to);
        }).start();
        return ApiResponse.success("Successfully");
    }
    @GetMapping("/holiday-import-template")
    public void exportHolidayImportTemplate(HttpServletResponse response){
        try {
            OutputStream outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=holiday-calendar.xlsx");
            final InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader()
                    .getResourceAsStream("templates/excel/holiday-calendar.xlsx"));
            outputStream.write(IOUtils.toByteArray(inputStream));
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/import-holiday")
    public ApiResponse<String> importHoliday(@RequestParam("holidayFile") MultipartFile holidayFile, @RequestParam(required = false) Integer year) {
        try {
            workingCalendarService.importHoliday(holidayFile, year);
            return ApiResponse.success("Successfully");
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

}
