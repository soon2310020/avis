package saleson.api.endLifeCycle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import saleson.api.chart.payload.ChartPayload;
import saleson.api.endLifeCycle.payload.EndLifeCyclePayload;
import saleson.api.preset.PresetService;
import saleson.api.topic.TopicService;
import saleson.api.topic.payload.CorrespondencePayload;
import saleson.api.topic.payload.TopicPayload;
import saleson.common.domain.MultipartFormData;
import saleson.common.enumeration.EndOfLifeCycleStatus;
import saleson.common.enumeration.TopicType;
import saleson.common.payload.ApiResponse;
import saleson.model.Correspondence;
import saleson.model.MoldEndLifeCycle;
import saleson.model.Topic;
import saleson.model.data.MoldEndLifeCycleChartData;
import saleson.model.data.MoldEndLifeCycleChartResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/end-life-cycle")
public class MoldEndLifeCycleController {
    @Autowired
    MoldEndLifeCycleService moldEndLifeCycleService;

    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    TopicService topicService;

    @Autowired
    PresetService presetService;

    @GetMapping
    public ResponseEntity<Page<MoldEndLifeCycle>> moldEndLifeCycles(EndLifeCyclePayload payload, Pageable pageable) {
        payload.setCheckWorking(true);
        Page<MoldEndLifeCycle> pageContent = moldEndLifeCycleService.findAll(payload.getPredicate(), pageable, payload.getAccumulatedShotFilter());

        return new ResponseEntity<>(pageContent, HttpStatus.OK);
    }

    @PostMapping("/{id}/change-status")
    public ApiResponse changeStatus(@PathVariable(value = "id", required = true) Long id,
                                    @RequestParam(value = "status", required = true) EndOfLifeCycleStatus status) {
        try {
            MoldEndLifeCycle moldEndLifeCycle = moldEndLifeCycleService.findById(id);
            moldEndLifeCycle.setStatus(status);
            moldEndLifeCycleService.save(moldEndLifeCycle, null);
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse(false, "Fail!");
        }
        return new ApiResponse(true, "OK!!");
    }

    @GetMapping("/{moldEndLifeCycleId}/graph-mold-end-life-cycle")
    @Deprecated
    public List<MoldEndLifeCycleChartData> getMoldEndLifeCycleChartData(@PathVariable(value = "moldEndLifeCycleId", required = true) Long id,ChartPayload payload) {
       return moldEndLifeCycleService.getMoldEndLifeCycleChartData(id,payload);
    }

    @PostMapping("/{moldEndLifeCycleId}/create-correspondence")
    public ResponseEntity<Correspondence> createCorrespondence(@PathVariable(value = "moldEndLifeCycleId", required = true) Long id,MultipartFormData formData) {
//        Role role = payload.getModel();

        try {
            MoldEndLifeCycle moldEndLifeCycle = moldEndLifeCycleService.findById(id);
            if (moldEndLifeCycle == null){
                return new ResponseEntity<>(null);
//                return new ApiResponse(false, "End of Life Cycle not found!");
            }
            Topic topic = null;
            if (moldEndLifeCycle.getTopicId() != null) {
                topic =  topicService.findById(moldEndLifeCycle.getTopicId());
            }
            if(topic == null){
                topic = new Topic();
                topic.setTopicType(TopicType.END_lIFE_CYCLE);
                //new payload for create new topic
                topicService.save(topic,new TopicPayload());
                moldEndLifeCycle.setTopicId(topic.getId());
                moldEndLifeCycleService.save(moldEndLifeCycle,null);
            }

            CorrespondencePayload payload = objectMapper.readValue(formData.getPayload(), CorrespondencePayload.class);
            payload.setFiles(formData.getFiles());
            payload.setTopicId(topic.getId());
            Correspondence correspondence = topicService.saveCorrespondence(payload.getModel(), payload);

            //topic
            topicService.updateTopicWhenCreateCorrespondence(topic,correspondence);

//            topicService.sendMail(topic, correspondence);
            return new ResponseEntity<>(correspondence, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null);
        }
    }
    @GetMapping("/exportExcel")
    public void printExcel(HttpServletResponse response,
                           @RequestParam(required = false) List<Long> ids,
                           @RequestParam(required = false) Integer timezoneOffsetClient,
                           @RequestParam(name = "fileName", defaultValue = "tooling-detail", required = false) String fileName
            , Pageable pageable){
        try {
            OutputStream outputStream = response.getOutputStream();
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=tooling-report-"+new Date().getTime()+".xlsx");
            outputStream.write(moldEndLifeCycleService.exportExcelDataMolds(ids,timezoneOffsetClient, pageable).toByteArray());
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//	@GetMapping("/job-init-refurbishment")
//	public ApiResponse jobInitRefurbishment() {
//		try {
//			moldEndLifeCycleService.procAll();
//			return ApiResponse.success();
//		} catch (Exception e) {
//			return ApiResponse.error();
//		}
//	}

    @GetMapping("/job-init-statistics-accumulating-shot")
    public ApiResponse jobInitStatisticsAccumulatingShot( @RequestParam(value = "allTime", required = false) boolean allTime,
                                                          @RequestParam(value = "fromDay", required = false) String fromDay) {
        try {
            moldEndLifeCycleService.initStatisticsAccumulatingShot(allTime, fromDay);
            return ApiResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }

    @GetMapping("/{moldEndLifeCycleId}/graph-mold-end-life-cycle-full")
    public MoldEndLifeCycleChartResponse getMoldEndLifeCycleChartDataFull(@PathVariable(value = "moldEndLifeCycleId", required = true) Long id) {
        return moldEndLifeCycleService.getMoldEndLifeCycleChartDataFull(id);
    }
    @GetMapping("/job-cal-missing-day-preset")
    public ApiResponse jobCalMissingDay() {
        try {
            presetService.jobCalMissingDay();
            return ApiResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            return ApiResponse.error(e.getMessage());
        }
    }


}
