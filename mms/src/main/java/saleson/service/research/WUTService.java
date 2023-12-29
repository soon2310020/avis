package saleson.service.research;

import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.model.data.wut.*;
import saleson.service.rest.RestfulAPIService;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WUTService {
    @Autowired
    RestfulAPIService restfulAPIService;

    @Value("${python.wut.server}")
    String pythonWUTServer;

    public BasedWUTData calculateWUTByCtt(List<MoldCttTempData> rawData){
        BasedWUTData result = BasedWUTData.builder().build();
        Long[] warmUpTime = {0L};
        for(int index = 0; index < rawData.size(); index++){
            MoldCttTempData data = rawData.get(index);
            warmUpTime[0] ++; // increase 1 hour each time
            if(data.getCtt() != null) {
                String[] splitCtt = data.getCtt().split("/");
                if (splitCtt.length > 0) {
                    Map<Double, Integer> cttMap = new HashMap<>(); // key: ctt, value: shot count
                    for (int i = 0; i < splitCtt.length - 1; i += 2) {
                        if (splitCtt[i] == null || splitCtt[i].trim().equals(""))
                            break;
                        cttMap.put(Double.valueOf(splitCtt[i]), Integer.valueOf(splitCtt[i + 1]));
                    }
                    if (cttMap.size() > 0) {
                        Double[] total = {0.0};
                        Integer[] totalShot = {0};
                        cttMap.forEach((k, v) -> {
                            total[0] += Math.abs(data.getCt() - k) * v;
                            totalShot[0] += v;
                        });
                        if (totalShot[0] != 0) {
                            Double value = (total[0] / 10) / totalShot[0]; // convert 100ms to second
                            if (value < 0.5 || Math.abs(value - 0.5) < 0.05) {
                                result.setIndex(index);
                                result.setWut((double) warmUpTime[0] * 3600); // convert to seconds
                                break;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public Map<Integer, BasedWUTData> calculateWUTByTemp(Map<Integer, List<MoldCttTempData>> rawData) throws JSONException {
        Map<Integer, BasedWUTData> result = new HashMap<>();

        List<Object> elements = new ArrayList<>();
        rawData.forEach((k, v) -> {
            Map<String, Object> element = new HashMap<>();
            element.put("index", k);
            List<Double> temperature = new ArrayList<>();
            v.forEach(tempData -> {
                if(tempData.getTemp() != null) {
                    String[] temp = tempData.getTemp().split("/");
                    for (int i = 0; i < temp.length; i++) {
                        if (i != 5) temperature.add(Double.valueOf(temp[i]) / 10.0);
                    }
                }
            });
            if(temperature.size() != 0) {
                element.put("temp", temperature);
                elements.add(element);
            }
        });

        Map<String, Object> parameterValue = new HashMap<>();
        parameterValue.put("data", elements);
        JSONObject response = restfulAPIService.send(pythonWUTServer + "/calculator", parameterValue);
        Gson gson = new Gson();
        WUTTempBasedFullData listData = gson.fromJson(response.toString(), WUTTempBasedFullData.class);
        listData.getData().forEach(data -> {
            result.put(data.getIndex(), data);
        });
        return result;
    }

    public List<WUTFullData> convertData(List<WUTData> wutData, Map<Integer, List<MoldCttTempData>> rawData){
        List<WUTFullData> results = new ArrayList<>();
        wutData.forEach(data -> {

            if(data.getWarmUpTime() != null){
                WUTFullData fullData = WUTFullData.builder().build();
                fullData.setTitle("Warm-up time");
                fullData.setHour(data.getWarmUpTime().getHour());
                fullData.setShotCount(data.getWarmUpTime().getShotCount());
                fullData.setStartedAt(data.getWarmUpTime().getStartedAt());
                fullData.setEndAt(DateUtils.getAddedTime(fullData.getStartedAt(), "yyyyMMddHHmmss", (long) (fullData.getHour() * 3600), ChronoUnit.SECONDS));
                results.add(fullData);
            }
            if(data.getNormalTime() != null){
                List<MoldCttTempData> listRawData = rawData.get(data.getIndex());

                if (data.getIndexStartNormal() >=  listRawData.size() - 1) { // skip when error
                    return;
                }
                List<MoldCttTempData> normalListRawData = listRawData.subList(data.getIndexStartNormal(), listRawData.size() - 1); // Cool-down time is not counted as normal time
                List<Integer> changeInNormalIndexes = new ArrayList<>();
                normalListRawData.forEach(normalRawData -> {
                    if(normalRawData.getCtt() != null) {
                        String[] splitCtt = normalRawData.getCtt().split("/");
                        if (splitCtt.length > 0) {
                            Map<Double, Integer> cttMap = new HashMap<>();
                            for (int i = 0; i < splitCtt.length - 1; i += 2) {
                                if (splitCtt[i] == null || splitCtt[i].trim().equals(""))
                                    break;
                                cttMap.put(Double.valueOf(splitCtt[i]), Integer.valueOf(splitCtt[i + 1]));
                            }
                            if (cttMap.size() > 0) {
                                Double[] total = {0.0};
                                Integer[] totalShot = {0};
                                cttMap.forEach((k, v) -> {
                                    total[0] += Math.abs(normalRawData.getCt() - k) * v;
                                    totalShot[0] += v;
                                });
                                if (totalShot[0] != 0) {
                                    Double value = (total[0] / 10) / totalShot[0]; // convert 100ms to second
                                    if (value >= 0.5) {
                                        changeInNormalIndexes.add(normalListRawData.indexOf(normalRawData));
                                    }
                                }
                            }
                        }
                    }
                });
                if(changeInNormalIndexes.size() == 0 && normalListRawData != null && normalListRawData.size() > 0){
                    WUTFullData fullData = WUTFullData.builder().build();
                    fullData.setTitle("Normal time");
                    fullData.setHour(data.getNormalTime().getHour());
                    fullData.setShotCount(data.getNormalTime().getShotCount());
                    fullData.setStartedAt(data.getNormalTime().getStartedAt());
                    fullData.setEndAt(normalListRawData.get(normalListRawData.size() - 1).getLst());
                    results.add(fullData);
                }else if(normalListRawData != null && normalListRawData.size() > 0){
                    Double normalTime = data.getFirstNormalTimePeriod();
                    String startedAt = data.getNormalTime().getStartedAt();
                    Integer shotCount = data.getFirstNormalTimeShotCount();
                    Double changeInNormalTime = 0.0;
                    String endAt = normalListRawData.get(0).getLst();
                    for(int i = 0; i < normalListRawData.size(); i++){
                        if(!changeInNormalIndexes.contains(i) && !changeInNormalIndexes.contains(i - 1)){
                            normalTime += (double) getUptimeSecondsByCtt(normalListRawData.get(i)) / 3600;
                            shotCount += normalListRawData.get(i).getShotCount();
                            endAt = normalListRawData.get(i).getLst();
                            if(i == normalListRawData.size() - 1){
                                WUTFullData fullData = WUTFullData.builder().build();
                                fullData.setTitle("Normal time");
                                fullData.setHour(normalTime);
//                                fullData.setShotCount((int) (data.getNormalTime().getShotCount() * normalTime / data.getNormalTime().getHour()));
                                fullData.setShotCount(shotCount);
                                fullData.setStartedAt(startedAt);
                                fullData.setEndAt(endAt);
                                results.add(fullData);
                            }
                        }else if(changeInNormalIndexes.contains(i) && !changeInNormalIndexes.contains(i - 1) && normalTime > 0){
                            WUTFullData fullData = WUTFullData.builder().build();
                            fullData.setTitle("Normal time");
                            fullData.setHour(normalTime);
                            fullData.setShotCount(shotCount);
                            fullData.setStartedAt(startedAt);
                            fullData.setEndAt(endAt);
                            results.add(fullData);
                            changeInNormalTime = (double) getUptimeSecondsByCtt(normalListRawData.get(i)) / 3600;
                            startedAt = endAt;
                            endAt = normalListRawData.get(i).getLst();
                            shotCount = normalListRawData.get(i).getShotCount();
                            if(i == normalListRawData.size() - 1){
                                fullData = WUTFullData.builder().build();
                                fullData.setTitle("Change in normal time");
                                fullData.setHour(changeInNormalTime);
                                fullData.setShotCount(shotCount);
                                fullData.setStartedAt(startedAt);
                                fullData.setEndAt(endAt);
                                results.add(fullData);
                            }
                        }else if(changeInNormalIndexes.contains(i) && changeInNormalIndexes.contains(i - 1)){
                            shotCount += normalListRawData.get(i).getShotCount();
                            changeInNormalTime += (double) getUptimeSecondsByCtt(normalListRawData.get(i)) / 3600;
                            endAt = normalListRawData.get(i).getLst();
                            if(i == normalListRawData.size() - 1){
                                WUTFullData fullData = WUTFullData.builder().build();
                                fullData.setTitle("Change in normal time");
                                fullData.setHour(changeInNormalTime);
                                fullData.setShotCount(shotCount);
                                fullData.setStartedAt(startedAt);
                                fullData.setEndAt(endAt);
                                results.add(fullData);
                            }
                        }else if(!changeInNormalIndexes.contains(i) && changeInNormalIndexes.contains(i - 1) && changeInNormalTime > 0){
                            WUTFullData fullData = WUTFullData.builder().build();
                            fullData.setTitle("Change in normal time");
                            fullData.setHour(changeInNormalTime);
                            fullData.setShotCount(shotCount);
                            fullData.setStartedAt(startedAt);
                            fullData.setEndAt(endAt);
                            results.add(fullData);
                            normalTime = (double) getUptimeSecondsByCtt(normalListRawData.get(i)) / 3600;
                            shotCount = normalListRawData.get(i).getShotCount();
                            startedAt = endAt;
                            endAt = normalListRawData.get(i).getLst();
                            if(i == normalListRawData.size() - 1){
                                fullData = WUTFullData.builder().build();
                                fullData.setTitle("Normal time");
                                fullData.setHour(normalTime);
                                fullData.setShotCount(shotCount);
                                fullData.setStartedAt(startedAt);
                                fullData.setEndAt(endAt);
                                results.add(fullData);
                            }
                        }
                    }
                }

            }
            if(data.getCoolDownTime() != null){
                WUTFullData fullData = WUTFullData.builder().build();
                fullData.setTitle("Cool down time");
                fullData.setHour(data.getCoolDownTime().getHour());
                fullData.setShotCount(data.getCoolDownTime().getShotCount());
                fullData.setStartedAt(data.getCoolDownTime().getStartedAt());
                fullData.setEndAt(data.getEndSection());
                results.add(fullData);
            }
            if(data.getAbnormalData() != null){
                WUTFullData fullData = WUTFullData.builder().build();
                fullData.setTitle("Abnormal time");
                fullData.setHour(data.getAbnormalData().getHour());
                fullData.setShotCount(data.getAbnormalData().getShotCount());
                fullData.setStartedAt(data.getAbnormalData().getStartedAt());
                fullData.setEndAt(data.getEndSection());
                results.add(fullData);
            }
            if(data.getDownTime() != null){
                WUTFullData fullData = WUTFullData.builder().build();
                fullData.setTitle("Down time");
                fullData.setHour(data.getDownTime().getHour());
                fullData.setStartedAt(data.getDownTime().getStartedAt());
                fullData.setEndAt(DateUtils.getAddedTime(fullData.getStartedAt(), "yyyyMMddHHmmss", (long) (fullData.getHour() * 3600), ChronoUnit.SECONDS));
                results.add(fullData);
            }
        });
        return results;
    }

    public Long getUptimeSecondsByCtt(MoldCttTempData data){
        if(data.getCtt() != null) {
            String[] splitCtt = data.getCtt().split("/");
            if (splitCtt.length > 0) {
                Map<Double, Integer> cttMap = new HashMap<>(); // key: ctt, value: shot count
                for (int i = 0; i < splitCtt.length - 1; i += 2) {
                    if (splitCtt[i] == null || splitCtt[i].trim().equals(""))
                        break;
                    cttMap.put(Double.valueOf(splitCtt[i]), Integer.valueOf(splitCtt[i + 1]));
                }
                if (cttMap.size() > 0) {
                    Double[] totalUptimeSecond = {0.0};
                    cttMap.forEach((k, v) -> {
                        totalUptimeSecond[0] += k * v / 10; // convert 100ms to second
                    });
                    return totalUptimeSecond[0].longValue();
                }
            }
        }else if(data.getCt() != null){
            Double totalUptimeSecond = (data.getCt() / 10) * (data.getShotCount() != null ? data.getShotCount() : 0);
            return totalUptimeSecond.longValue();
        }
        return 0L;
    }
}
