package saleson.api.chart;

import static saleson.common.util.DateUtils.DEFAULT_DATE_FORMAT;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.emoldino.api.common.resource.composite.dsh.service.DshService;
import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import saleson.api.category.CategoryRepository;
import saleson.api.category.CategoryService;
import saleson.api.chart.payload.DashboardFilterPayload;
import saleson.api.company.CompanyService;
import saleson.api.counter.CounterRepository;
import saleson.api.location.LocationService;
import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.mold.payload.MoldPayload;
import saleson.api.part.PartService;
import saleson.api.statistics.StatisticsService;
import saleson.api.terminal.TerminalRepository;
import saleson.common.enumeration.EquipmentType;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.JsonUtils;
import saleson.common.util.StringUtils;
import saleson.dto.productionPatternAnalysis.InputCdataDTO;
import saleson.dto.productionPatternAnalysis.InputCdataListDTO;
import saleson.dto.productionPatternAnalysis.ProductionPatternDTO;
import saleson.model.Category;
import saleson.model.Cdata;
import saleson.model.Mold;
import saleson.model.MoldPart;
import saleson.model.data.CategoryDetailsData;
import saleson.model.data.CategoryPartDetailsData;
import saleson.model.data.CategoryProjectDetailsData;
import saleson.model.data.DashboardChartData;
import saleson.model.data.MiniComponentData;
import saleson.model.data.dashboard.ImplementationStatusData;
import saleson.service.rest.RestfulAPIService;
import saleson.service.transfer.CdataRepository;

@Service
@Slf4j
public class ChartService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Lazy
    @Autowired
    private MoldService moldService;

    @Lazy
    @Autowired
    private PartService partService;

    @Autowired
    private MoldRepository moldRepository;

    @Autowired
    private CounterRepository counterRepository;

    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private CdataRepository cdataRepository;

    @Autowired
    private RestfulAPIService restfulAPIService;

    @Value("${python.wut.server}")
    private String pythonWUTServer;
    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private CompanyService companyService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StatisticsService statisticsService;

    public CategoryDetailsData getCategoryDetails(Long categoryId, DashboardFilterPayload payload){
        CategoryDetailsData result = new CategoryDetailsData();
        Category category = categoryRepository.findById(categoryId).orElse(null);
        List<Long> partIdsFiltered = partService.findAllMiniDataByGeneralFilter()
                .stream()
                .map(MiniComponentData::getId)
                .collect(Collectors.toList());
        if(category == null) return result;

        result.setCategoryId(category.getId());
        result.setCategoryName(category.getName());
        if(category.getChildren() == null || category.getChildren().size() == 0) return result;

        List<CategoryProjectDetailsData> projectDetailsData = new ArrayList<>();
        List<Category> projects = category.getChildren();
        Integer[] partMoldCount = {0, 0};
        projects.forEach(project -> {
            CategoryProjectDetailsData projectDetails = new CategoryProjectDetailsData();
            projectDetails.setProjectId(project.getId());
            projectDetails.setProjectName(project.getName());

            MoldPayload moldPayload = new MoldPayload();
            moldPayload.setProjectId(project.getId());
            List<MoldPart> moldParts = moldService.findMoldPartAll(moldPayload, payload);
            List<CategoryPartDetailsData> categoryPartDetailsData = new ArrayList<>();
            Set<Mold> moldSet = new HashSet<>();
            if(moldParts != null && moldParts.size() > 0){
                Map<Long, List<MoldPart>> map = new HashMap<>();
                moldParts.forEach(moldPart -> {
                    if (partIdsFiltered.contains(moldPart.getPartId())) {
                        if(!map.containsKey(moldPart.getPartId()))
                            map.put(moldPart.getPartId(), new ArrayList<>());
                        map.get(moldPart.getPartId()).add(moldPart);
                        moldSet.add(moldPart.getMold());
                    }
                });

                map.forEach((k, v) -> {
                    CategoryPartDetailsData partDetails = new CategoryPartDetailsData();
                    partDetails.setPartId(k);
                    partDetails.setPartCode(v.get(0).getPartCode());
                    partDetails.setMoldCode(v.stream().map(x -> x.getMold().getEquipmentCode()).collect(Collectors.toList()));
                    partDetails.setMoldCount(partDetails.getMoldCode().size());
                    categoryPartDetailsData.add(partDetails);
                });
            }
            projectDetails.setPartDetailsData(categoryPartDetailsData);
            projectDetails.setPartCount(categoryPartDetailsData.size());
            projectDetails.setMoldCount(moldSet.size());

            projectDetailsData.add(projectDetails);
            partMoldCount[0] += categoryPartDetailsData.size();
            partMoldCount[1] += moldSet.size();
        });
        result.setProjectDetailsData(projectDetailsData);
        result.setProjectCount(projectDetailsData.size());
        result.setPartCount(partMoldCount[0]);
        result.setMoldCount(partMoldCount[1]);
        return result;
    }

    public ImplementationStatusData findImplementationStatus(DashboardFilterPayload payload) {
        log.info("findImplementationStatus payload: {}", payload);
        ImplementationStatusData result = ImplementationStatusData.builder().build();
        if (payload.getEquipmentType() != null) {
            List<DashboardChartData> chartData = new ArrayList<>();
            EquipmentType equipmentType = payload.getEquipmentType();
            switch (equipmentType) {
                case MOLD:
                    chartData = moldRepository.findImplementationStatus(payload);
                    break;
                case COUNTER:
                    chartData = counterRepository.findImplementationStatus(payload);
                    break;
                case TERMINAL:
                    chartData = terminalRepository.findImplementationStatus(payload);
                    break;
                default:
                    log.warn("invalid equipmentType", payload.getEquipmentType());
                    break;
            }
            Long total = chartData.stream().map(x -> x.getData()).reduce(0L, Long::sum);
            result.setTotal(total);
            result.setCompanies(chartData);
        }
        return result;
    }

    public List<ProductionPatternDTO> productionPatternAnalysis(Long moldId,String week){
        List<ProductionPatternDTO> resList= new ArrayList<>();
        if(moldId==null || StringUtils.isEmpty(week)) return resList;
        int year =Integer.valueOf(week.substring(0,4));
        int weekValue =Integer.valueOf(week.substring(4,6));
        Calendar calendar = DateUtils.getCalendar();
        calendar.setWeekDate(year,weekValue,Calendar.MONDAY);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.add(Calendar.HOUR,-1);
        String fromTime= DateUtils.getDate(calendar.toInstant(),DEFAULT_DATE_FORMAT);
        calendar.add(Calendar.HOUR,1);
        String dayFrom= DateUtils.getDate(calendar.toInstant(),DEFAULT_DATE_FORMAT);
        calendar.add(Calendar.DATE,7);
        String toTime= DateUtils.getDate(calendar.toInstant(),DEFAULT_DATE_FORMAT);

        List<Cdata> cdataList= cdataRepository.findAllByMoldIdAndRtBetweenAndTempNotNullOrderByRtAscLstAsc(moldId,fromTime,toTime);
        int numBefore=cdataList.stream().filter(c-> c.getRt().compareTo(dayFrom)<0).collect(Collectors.toList()).size();
        if(numBefore>1){
            cdataList=cdataList.subList(numBefore-1,cdataList.size());
        }
        List<InputCdataDTO> inputCdataDTOS = cdataList.stream().map(c-> DataUtils.mapper.map(c,InputCdataDTO.class))
                .collect(Collectors.toMap(InputCdataDTO::useRtAsKey, Function.identity(), (a, b) -> a))
                .values().stream().unordered().collect(Collectors.toList());
//                .collect(Collectors.toList());

//        Map<String, Object> parameterValue = new HashMap<>();
//        parameterValue.put("data", DataUtils.gson.fromJson(DataUtils.gson.toJson(inputCdataDTOS),JSONArray.class));
//        String bodyStr=DataUtils.gson.toJson(new InputCdataListDTO(inputCdataDTOS));
        if (!CollectionUtils.isEmpty(inputCdataDTOS)) {
            JSONObject response = null;
            try {
                String bodyStr = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(new InputCdataListDTO(inputCdataDTOS));
                response = restfulAPIService.sendWithBodyString(pythonWUTServer + "/warmup-time", null, bodyStr);

                if (!CollectionUtils.isEmpty(inputCdataDTOS) && response == null) {
                    log.error("Error from Python Service \nTooling: {}, week: {}, Counter: {} with body: \n{}", cdataList.get(0).getMoldCode()
                            , week, cdataList.get(0).getCi(), bodyStr);
                }

                if (response != null && response.has("data")) {
                    resList = JsonUtils.toCollection(response.getString("data"), List.class, ProductionPatternDTO.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LocalDate start = DateUtils.getLocalDate(dayFrom.substring(0,8));
        LocalDate end = start.plusDays(7);
        return getListDataAfterFillingBlankHours(start, end, resList);
    }

    private List<ProductionPatternDTO> getListDataAfterFillingBlankHours(LocalDate start, LocalDate end, List<ProductionPatternDTO> realList)
    {
        List<ProductionPatternDTO> allList = new ArrayList<>();
        DateTimeFormatter formatterOfPythonServer = DateTimeFormatter.ofPattern("yyyyMMdd")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM")
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault());

        while (start.compareTo(end) < 0) {
            String formattedDate = formatterOfPythonServer.format(start);
            for (int i = 0; i < 24; i++) {
                String time = i < 10 ? "0" + i : String.valueOf(i);
                allList.add(new ProductionPatternDTO((long)i, time, formattedDate));
            }
            start = start.plusDays(1);
        }

        List<ProductionPatternDTO> newList = new ArrayList<>();
        allList.forEach(p -> {
            boolean isMatched = false;
            for (ProductionPatternDTO rp : realList) {
                if (p.getDate().equals(rp.getDate()) && p.getTime().equals(rp.getTime())){
                    newList.add(rp);
                    isMatched = true;
                    break;
                }
            }

            if (!isMatched)
            {
                newList.add(p);
            }
        });

        newList.forEach(p -> {
            LocalDate date = DateUtils.getLocalDate("yyyyMMdd", p.getDate());
            p.setDate(formatter.format(date));
        });

        return newList;
    }
    public Map<String, List<MiniComponentData>> getListSearch(boolean isAll){
        List<MiniComponentData> suppliers = companyService.findAllMiniDataFiltered(null, isAll);
        List<MiniComponentData> toolMakers = companyService.findAllMiniDataFiltered("ToolMaker", isAll);
        List<MiniComponentData> locations = locationService.findAllMiniDataFiltered(isAll);
        long[] i = {1};
        List<MiniComponentData> contents = BeanUtils.get(DshService.class).getContent().stream().map(item -> new MiniComponentData(i[0]++, null, item.getName(), null)).collect(Collectors.toList());
        List<MiniComponentData> categories = categoryService.findAllMiniData();
        List<MiniComponentData> projects = statisticsService.findAllMiniData();
        List<MiniComponentData> parts = partService.findAllMiniDataFiltered(isAll);
        List<MiniComponentData> molds = moldService.findAllMiniData();

        Map<String, List<MiniComponentData>> listContent = new HashMap<>();
        listContent.put("Supplier", suppliers);
        listContent.put("ToolMaker", toolMakers);
        listContent.put("Location", locations);
        listContent.put("Content", contents);
        listContent.put("Category", categories);
        listContent.put("Project", projects);
        listContent.put("Part", parts);
        listContent.put("Tooling", molds);
        return listContent;
    }
}
