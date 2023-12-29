package saleson.api.report;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.emoldino.api.common.resource.base.option.util.OptionUtils;

import saleson.api.mold.MoldRepository;
import saleson.api.mold.MoldService;
import saleson.api.part.PartService;
import saleson.api.report.payload.ProductivitySearchPayload;
import saleson.api.statistics.StatisticsWutService;
import saleson.common.enumeration.ConfigCategory;
import saleson.common.enumeration.CycleTimeStatus;
import saleson.common.enumeration.DataRangeType;
import saleson.common.enumeration.productivity.CompareType;
import saleson.common.enumeration.productivity.ProductivityVariable;
import saleson.common.util.DataUtils;
import saleson.common.util.DateUtils;
import saleson.common.util.ListUtils;
import saleson.dto.Item;
import saleson.dto.RestDataList;
import saleson.model.Mold;
import saleson.model.data.ChartData;
import saleson.model.data.PartProductionData;
import saleson.model.data.cycleTime.CycleTimeOverviewData;
import saleson.model.data.cycleTime.CycleTimeOverviewDetailData;
import saleson.model.data.cycleTime.ToolingCycleTimeData;
import saleson.model.data.cycleTime.ToolingCycleTimeDataLite;
import saleson.model.data.productivity.ProductivityOverviewData;
import saleson.model.data.productivity.ToolingProductivityData;
import saleson.model.data.supplierReport.SupplierProductionData;
import saleson.model.data.supplierReport.SupplierProductionOverviewData;
import saleson.service.util.DateTimeUtils;

@Service
public class ReportService {
    @Autowired
    PartService partService;

    @Lazy
    @Autowired
    MoldService moldService;

    @Autowired
    StatisticsWutService statisticsWutService;
    @Autowired
    MoldRepository moldRepository;

    public ProductivityOverviewData getOverviewProductivity(ProductivitySearchPayload payload, Pageable pageable){
        ProductivityOverviewData result = ProductivityOverviewData.builder().build();

        List<Long> moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);

        ProductivityOverviewData producedPartQuantity = partService.findProductivityOverviewData(moldIds, payload);
        result.setTotalProductivity(producedPartQuantity.getTotalProductivity());

        ProductivityOverviewData maxProductivity = moldService.findMaxProductivity(moldIds, payload);

        ProductivitySearchPayload payload1 = DataUtils.mapper.map(payload,ProductivitySearchPayload.class);
        payload1.setVariable(ProductivityVariable.AVAILABLE_PRODUCTIVITY);
        List<ToolingProductivityData>  toolingDetailsListProductivityDataList= moldService.findToolingDetailsListProductivity(moldIds, payload1, pageable);
        Integer availableTotal =0;
        for(int i=0;i<toolingDetailsListProductivityDataList.size();i++){
            availableTotal+=toolingDetailsListProductivityDataList.get(i).getMaxCapacity();
        }
        result.setAvailableProductivity(availableTotal);

        Integer totalQuantity = result.getTotalProductivity() + result.getAvailableProductivity();
        if (totalQuantity == 0) {
            result.setTotalProductivityPercent(0D);
            result.setAvailableProductivityPercent(0D);
        } else {
            result.setTotalProductivityPercent(((double) result.getTotalProductivity() / totalQuantity) * 100);
            result.setAvailableProductivityPercent(((double) result.getAvailableProductivity() / totalQuantity) * 100);
        }

//        result.setAvailableProductivity(maxProductivity.getTotalProductivity() - producedPartQuantity.getTotalProductivity());
        if((maxProductivity.getTotalProductivity() * maxProductivity.getMoldCount()) == 0) result.setAvgProductivity(0.0);
        else result.setAvgProductivity(Double.valueOf(producedPartQuantity.getTotalProductivity()) * 100 / maxProductivity.getTotalProductivity());
/*
        Double avgProductivity=0.0;
        Integer maxProductivityValue=producedPartQuantity.getTotalProductivity()+availableTotal;
        if(maxProductivityValue!=0){
            avgProductivity=Double.valueOf(producedPartQuantity.getTotalProductivity()) * 100/maxProductivityValue;
        }
        result.setAvgProductivity(avgProductivity);
*/

        if(payload.getDuration() != null){

            ProductivitySearchPayload subPayload = DataUtils.deepCopy(payload,ProductivitySearchPayload.class);
            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2));
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1));
/*

            ProductivitySearchPayload subPayload = ProductivitySearchPayload.builder()
                    .partId(payload.getPartId())
                    .startDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2))
                    .endDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1))
                    .compareBy(payload.getCompareBy())
                    .build();
*/
            ProductivityOverviewData currentProductivity = producedPartQuantity;
//            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()));
            ProductivityOverviewData lastTDaysProductivity = partService.findProductivityOverviewData(moldIds, subPayload);
//            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2));
//            ProductivityOverviewData last2TDaysProductivity = partService.findProductivityOverviewData(moldIds, subPayload);

            if(currentProductivity != null && lastTDaysProductivity != null) {
                Integer dividedBy = lastTDaysProductivity.getTotalProductivity();
                if (dividedBy != 0) {
                    Double trend = (currentProductivity.getTotalProductivity() - lastTDaysProductivity.getTotalProductivity()) * 100.0 / dividedBy;
                    result.setTrend(trend);
                }
            }
        }

        RestDataList<ToolingProductivityData> top5Data=getTop5ToolingProductivity(moldIds, payload, pageable,true);
        result.setTop5Tooling(top5Data.getDataList());
        result.setTotalElements(top5Data.getTotal());

        return result;
    }

    public RestDataList<ToolingProductivityData> getTop5ToolingProductivity(List<Long> moldIds, ProductivitySearchPayload payload, Pageable pageable, boolean hasCount){
		if (moldIds == null) {
			moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);
		}
        return moldService.findToolingProductivity(moldIds, payload, pageable, hasCount);
    }

    public List<ToolingProductivityData> getToolingProductivityDetailsList(ProductivitySearchPayload payload, Pageable pageable){
        List<Long> moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);
        List<ToolingProductivityData>  toolingDetailsListProductivityDataList= moldService.findToolingDetailsListProductivity(moldIds, payload, pageable);
        return toolingDetailsListProductivityDataList;
    }


    public CycleTimeOverviewData getOverviewCycleTime(ProductivitySearchPayload payload, Pageable pageable){
        CycleTimeOverviewData result = CycleTimeOverviewData.builder().build();
        result.setTrendMolds(new ArrayList<>());

        List<Long> moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);
        List<CycleTimeOverviewDetailData> complianceMolds = moldService.getReportCycleTimeInRange(moldIds, payload, DataRangeType.WITHIN);
        List<CycleTimeOverviewDetailData> belowApproved =  moldService.getReportCycleTimeInRange(moldIds, payload, DataRangeType.BELOW);
        List<CycleTimeOverviewDetailData> aboveApproved =  moldService.getReportCycleTimeInRange(moldIds, payload, DataRangeType.ABOVE);
        Map<Long,Item> mapCompany =  new HashMap<>();
        Map<Long,Set<Long>> companyMolds=new HashMap<>();
        if(Arrays.asList(CompareType.SUPPLIER,CompareType.TOOLMAKER).contains(payload.getCompareBy())){
            calculatorValueComplianceCycleTimeForCompaniesOverview(result, complianceMolds, belowApproved, aboveApproved, mapCompany, companyMolds);
        }else {
            result.setComplianceList(complianceMolds);
            result.setBelowList(belowApproved);
            result.setAboveList(aboveApproved);
        }
        result.setComplianceMoldCount(complianceMolds.size());
        result.setBelowMoldCount(belowApproved.size());
        result.setAboveMoldCount(aboveApproved.size());

        //calculate percent
        Integer moldCountForAll = result.getAboveMoldCount() + result.getBelowMoldCount() + result.getComplianceMoldCount();
        result.setComplianceMoldCountPercent(moldCountForAll == 0 ? 0D : ((double) result.getComplianceMoldCount() / moldCountForAll) * 100);
        result.setBelowMoldCountPercent(moldCountForAll == 0 ? 0D : ((double) result.getBelowMoldCount() / moldCountForAll) * 100);
        result.setAboveMoldCountPercent(moldCountForAll == 0 ? 0D : ((double) result.getAboveMoldCount() / moldCountForAll) * 100);

        if(payload.getDuration() != null){
            List<ToolingCycleTimeData> ptComplianceData = moldService.findToolingCycleTimeData(moldIds, payload, null, false);
            ProductivitySearchPayload subPayload = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2));
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1));

/*
            ProductivitySearchPayload subPayload = ProductivitySearchPayload.builder()
                    .startDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2))
                    .endDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1))
                    .compareBy(payload.getCompareBy())
                    .build();
*/
            List<ToolingCycleTimeData> pptComplianceData = moldService.findToolingCycleTimeData(moldIds, subPayload, null, false);

            List<ToolingCycleTimeData> trendMolds = new ArrayList<>();
            if(Arrays.asList(CompareType.SUPPLIER,CompareType.TOOLMAKER).contains(payload.getCompareBy())){
                mapCompany.keySet().forEach(key->{
                    Item cpn = mapCompany.get(key);
                    trendMolds.add(ToolingCycleTimeData.builder()
                            .id(key)
                            .code(cpn.getCode())
                            .name(cpn.getName())
                            .build());
                });
            }else{

                complianceMolds.forEach(x -> trendMolds.add(ToolingCycleTimeData.builder()
                        .moldId(x.getMoldId())
                        .id(x.getMoldId())
                        .moldCode(x.getMoldCode())
                        .build()));
                belowApproved.forEach(x -> trendMolds.add(ToolingCycleTimeData.builder()
                        .moldId(x.getMoldId())
                        .id(x.getMoldId())
                        .moldCode(x.getMoldCode())
                        .build()));
                aboveApproved.forEach(x -> trendMolds.add(ToolingCycleTimeData.builder()
                        .moldId(x.getMoldId())
                        .id(x.getMoldId())
                        .moldCode(x.getMoldCode())
                        .build()));
            }
            // Calculate trend for each item
            trendMolds.forEach(data -> {
                ToolingCycleTimeData firstData = pptComplianceData.stream().filter(x -> x.getId()!=null && x.getId().equals(data.getId())).findAny().orElse(null);
                Double firstCompliance = firstData != null ? firstData.getPercentageCompliance() : 0.0;

                ToolingCycleTimeData secondData = ptComplianceData.stream().filter(x -> x.getId()!=null &&  x.getId().equals(data.getId())).findAny().orElse(null);
                Double secondCompliance = secondData != null ? secondData.getPercentageCompliance() : 0.0;

                data.setTrend(secondCompliance - firstCompliance);

            });
            result.setTrendMolds(trendMolds);
            // Calculate overall trend
            if(trendMolds.size()>0){
                Double overallTrend = (trendMolds.stream().map(x -> x.getTrend()).reduce(0d, Double::sum)) / trendMolds.size();
                result.setTrend(overallTrend);
            }else result.setTrend(100.0);
        }
        //set field for cal top5
        result.setLimit1MoldCount(belowApproved.size());
        result.setLimit2MoldCount(aboveApproved.size());

        RestDataList<ToolingCycleTimeData> top5Data=getTop5ToolingCycleTime(moldIds, payload, pageable,result,companyMolds,true);
        result.setTop5Tooling(top5Data.getDataList());
        result.setTotalElements(top5Data.getTotal());
        return result;
    }

    private void calculatorValueComplianceCycleTimeForCompanies(CycleTimeOverviewData result, List<ToolingCycleTimeData> complianceMolds, List<ToolingCycleTimeData> limit1Molds, List<ToolingCycleTimeData> limit2Molds, Map<Long,Item> mapCompany, Map<Long,Set<Long>> companyMolds){
        Map<Long,Long> numCompliance=new HashMap<>();
        Map<Long,Long> numLimit1=new HashMap<>();
        Map<Long,Long> numLimit2=new HashMap<>();
        complianceMolds.forEach(c -> {
            if (c.getId() != null && mapCompany.get(c.getId()) == null) {
                mapCompany.put(c.getId(), new Item(c.getCode(), c.getName()));
            }
            if (numCompliance.get(c.getId()) == null) {
                numCompliance.put(c.getId(), 1l);
            } else numCompliance.put(c.getId(), numCompliance.get(c.getId()) + 1);

            if(companyMolds.get(c.getId())==null)companyMolds.put(c.getId(),new HashSet<>());
            companyMolds.get(c.getId()).add(c.getMoldId());
        });

        limit1Molds.forEach(c -> {
            if (c.getId() != null && mapCompany.get(c.getId()) == null) {
                mapCompany.put(c.getId(), new Item(c.getCode(), c.getName()));
            }
            if (numLimit1.get(c.getId()) == null) {
                numLimit1.put(c.getId(), 1l);
            } else numLimit1.put(c.getId(), numLimit1.get(c.getId()) + 1);

            if(companyMolds.get(c.getId())==null)companyMolds.put(c.getId(),new HashSet<>());
            companyMolds.get(c.getId()).add(c.getMoldId());
        });

        limit2Molds.forEach(c -> {
            if (c.getId() != null && mapCompany.get(c.getId()) == null) {
                mapCompany.put(c.getId(), new Item(c.getCode(), c.getName()));
            }
            if (numLimit2.get(c.getId()) == null) {
                numLimit2.put(c.getId(), 1l);
            } else numLimit2.put(c.getId(), numLimit2.get(c.getId()) + 1);

            if(companyMolds.get(c.getId())==null)companyMolds.put(c.getId(),new HashSet<>());
            companyMolds.get(c.getId()).add(c.getMoldId());
        });
        List<ToolingCycleTimeData> complianceCompanies= new ArrayList<>();
        List<ToolingCycleTimeData> limit1Companies= new ArrayList<>();
        List<ToolingCycleTimeData> limit2Companies= new ArrayList<>();
        numCompliance.keySet().forEach(key->{
            complianceCompanies.add(ToolingCycleTimeData.builder()
                    .id(key)
                    .code(mapCompany.get(key).getCode())
                    .name(mapCompany.get(key).getName())
                    .numMolds(numCompliance.get(key))
                    .totalMolds(Long.valueOf(companyMolds.get(key).size()))
                    .build());

        });
        numLimit1.keySet().forEach(key->{
            limit1Companies.add(ToolingCycleTimeData.builder()
                    .id(key)
                    .code(mapCompany.get(key).getCode())
                    .name(mapCompany.get(key).getName())
                    .numMolds(numLimit1.get(key))
                    .totalMolds(Long.valueOf(companyMolds.get(key).size()))
                    .build());

        });
        numLimit2.keySet().forEach(key->{
            limit2Companies.add(ToolingCycleTimeData.builder()
                    .id(key)
                    .code(mapCompany.get(key).getCode())
                    .name(mapCompany.get(key).getName())
                    .numMolds(numLimit2.get(key))
                    .totalMolds(Long.valueOf(companyMolds.get(key).size()))
                    .build());

        });
        result.setComplianceMolds(complianceCompanies);
        result.setLimit1Molds(limit1Companies);
        result.setLimit2Molds(limit2Companies);
    }

    private void calculatorValueComplianceCycleTimeForCompaniesOverview(CycleTimeOverviewData result
            , List<CycleTimeOverviewDetailData> complianceMolds, List<CycleTimeOverviewDetailData> belowApproved
            , List<CycleTimeOverviewDetailData> aboveApproved, Map<Long,Item> mapCompany, Map<Long,Set<Long>> companyMolds){
        Map<Long,Long> numCompliance=new HashMap<>();
        Map<Long,Long> numLimit1=new HashMap<>();
        Map<Long,Long> numLimit2=new HashMap<>();
        complianceMolds.forEach(c -> {
            if (c.getId() != null && mapCompany.get(c.getId()) == null) {
                mapCompany.put(c.getId(), new Item(c.getCode(), c.getName()));
            }
            if (numCompliance.get(c.getId()) == null) {
                numCompliance.put(c.getId(), 1l);
            } else numCompliance.put(c.getId(), numCompliance.get(c.getId()) + 1);

            if(companyMolds.get(c.getId())==null)companyMolds.put(c.getId(),new HashSet<>());
            companyMolds.get(c.getId()).add(c.getMoldId());
        });

        belowApproved.forEach(c -> {
            if (c.getId() != null && mapCompany.get(c.getId()) == null) {
                mapCompany.put(c.getId(), new Item(c.getCode(), c.getName()));
            }
            if (numLimit1.get(c.getId()) == null) {
                numLimit1.put(c.getId(), 1l);
            } else numLimit1.put(c.getId(), numLimit1.get(c.getId()) + 1);

            if(companyMolds.get(c.getId())==null)companyMolds.put(c.getId(),new HashSet<>());
            companyMolds.get(c.getId()).add(c.getMoldId());
        });

        aboveApproved.forEach(c -> {
            if (c.getId() != null && mapCompany.get(c.getId()) == null) {
                mapCompany.put(c.getId(), new Item(c.getCode(), c.getName()));
            }
            if (numLimit2.get(c.getId()) == null) {
                numLimit2.put(c.getId(), 1l);
            } else numLimit2.put(c.getId(), numLimit2.get(c.getId()) + 1);

            if(companyMolds.get(c.getId())==null)companyMolds.put(c.getId(),new HashSet<>());
            companyMolds.get(c.getId()).add(c.getMoldId());
        });
        List<CycleTimeOverviewDetailData> complianceCompanies= new ArrayList<>();
        List<CycleTimeOverviewDetailData> belowCompanies = new ArrayList<>();
        List<CycleTimeOverviewDetailData> aboveCompanies = new ArrayList<>();
        numCompliance.keySet().forEach(key->{
            complianceCompanies.add(CycleTimeOverviewDetailData.builder()
                    .id(key)
                    .code(mapCompany.get(key).getCode())
                    .name(mapCompany.get(key).getName())
                    .numMolds(numCompliance.get(key))
                    .totalMolds(Long.valueOf(companyMolds.get(key).size()))
                    .build());

        });
        numLimit1.keySet().forEach(key->{
            belowCompanies.add(CycleTimeOverviewDetailData.builder()
                    .id(key)
                    .code(mapCompany.get(key).getCode())
                    .name(mapCompany.get(key).getName())
                    .numMolds(numLimit1.get(key))
                    .totalMolds(Long.valueOf(companyMolds.get(key).size()))
                    .build());

        });
        numLimit2.keySet().forEach(key->{
            aboveCompanies.add(CycleTimeOverviewDetailData.builder()
                    .id(key)
                    .code(mapCompany.get(key).getCode())
                    .name(mapCompany.get(key).getName())
                    .numMolds(numLimit2.get(key))
                    .totalMolds(Long.valueOf(companyMolds.get(key).size()))
                    .build());

        });
        result.setComplianceList(complianceCompanies);
        result.setBelowList(belowCompanies);
        result.setAboveList(aboveCompanies);
    }

    public RestDataList<ToolingCycleTimeData> getTop5ToolingCycleTime(List<Long> moldIds, ProductivitySearchPayload payload
            , Pageable pageable,CycleTimeOverviewData overviewData,final Map<Long,Set<Long>> companyMolds,boolean hasCount){
		if (moldIds == null) {
			moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);
		}
        List<ToolingCycleTimeData> result = moldService.findToolingCycleTimeData(moldIds, payload, pageable, true);
        Long total=0l;
        if(hasCount)
            total = moldService.countToolingCycleTimeData(moldIds, payload, pageable, true);


        if (!Arrays.asList(CompareType.SUPPLIER, CompareType.TOOLMAKER).contains(payload.getCompareBy())) {
            List<Long> top5MoldIds = result.stream().map(x -> x.getMoldId()).collect(Collectors.toList());
            List<Mold> top5Molds = moldService.findByIdIn(top5MoldIds);
            //check compliant status
            List<ToolingCycleTimeData> complianceMolds = overviewData != null && overviewData.getComplianceMolds() != null ? overviewData.getComplianceMolds() :
                    moldService.getAvgCycleTimeMoldInRange(moldIds, payload, CycleTimeStatus.WITHIN_TOLERANCE);

            result.forEach(data -> {
                Mold mold = top5Molds.stream().filter(x -> x.getId()!=null &&  x.getId().equals(data.getMoldId())).findAny().orElse(null);
                data.setMold(mold);
                if(complianceMolds.stream().map(c->c.getId()).collect(Collectors.toList()).contains(data.getMoldId())){
                    data.setComplianceValue("Complied");
                }else
                    data.setComplianceValue("Not Complied");
                data.getDetails().add(new ToolingCycleTimeDataLite(data));
            });

        }else {
            //add info compliance
//            companyMolds = companyMolds == null ? new HashMap<>() : companyMolds;
            int totalMolds=0;
            if( overviewData == null){
                overviewData = CycleTimeOverviewData.builder().build();
                List<ToolingCycleTimeData> complianceMolds = moldService.getAvgCycleTimeMoldInRange(moldIds, payload, CycleTimeStatus.WITHIN_TOLERANCE);
                List<ToolingCycleTimeData> limit1Molds =  moldService.getAvgCycleTimeMoldInRange(moldIds, payload, CycleTimeStatus.OUTSIDE_L1);
                List<ToolingCycleTimeData> limit2Molds =  moldService.getAvgCycleTimeMoldInRange(moldIds, payload, CycleTimeStatus.OUTSIDE_L2);
                Map<Long,Item> mapCompany =  new HashMap<>();
                calculatorValueComplianceCycleTimeForCompanies(overviewData,complianceMolds,limit1Molds,limit2Molds,mapCompany, companyMolds);
                totalMolds=complianceMolds.size()+limit1Molds.size()+limit2Molds.size();
            }else {
                //load data for new flow detail
                List<ToolingCycleTimeData> complianceCompanies = overviewData != null && overviewData.getComplianceMolds() != null ? overviewData.getComplianceMolds() :
                        moldService.getAvgCycleTimeMoldInRange(moldIds, payload, CycleTimeStatus.WITHIN_TOLERANCE);
                if(overviewData.getComplianceList()!=null){
                    overviewData.getComplianceList().stream().forEach(c->{
                        if(c.getId()!=null){
                            ToolingCycleTimeData tcd= complianceCompanies.stream().filter(cp->c.getId().equals(cp.getId())).findFirst().orElse(null);
                            if(tcd!=null){
                                tcd.setNumMolds(c.getNumMolds());
                                tcd.setTotalMolds(c.getTotalMolds());
                            }
                        }
                    });
                }
                overviewData.setComplianceMolds(complianceCompanies);
                totalMolds = overviewData.getComplianceMoldCount() + overviewData.getLimit1MoldCount() + overviewData.getLimit2MoldCount();
            }
            int totalMoldsFinal = totalMolds;

            List<ToolingCycleTimeData> complianceCompanies = overviewData.getComplianceMolds();
//            List<ToolingCycleTimeData> complianceCompanies = overviewData != null && overviewData.getComplianceMolds() != null ? overviewData.getComplianceMolds() :
//                    moldService.getAvgCycleTimeMoldInRange(moldIds, payload, CycleTimeStatus.WITHIN_TOLERANCE);
            if(complianceCompanies!=null){

                List<Long> finalMoldIds = moldIds;
                result.forEach(data -> {
                ToolingCycleTimeData compliance = complianceCompanies.stream().filter(c->c.getId()!=null && c.getId().equals(data.getId())).findFirst().orElse(null);
                if(compliance!=null){
                    data.setNumMolds(compliance.getNumMolds());
                    data.setTotalMolds(compliance.getTotalMolds());
                }else {
                    data.setNumMolds(0l);
                    if (companyMolds.containsKey(data.getId()))
                        data.setTotalMolds(Integer.valueOf(companyMolds.get(data.getId()).size()).longValue());
                }
                ProductivitySearchPayload litePayload = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
                litePayload.setForTooltip(true);
                litePayload.setCompareBy(CompareType.TOOL);
                if (payload.getCompareBy().equals(CompareType.SUPPLIER)){
                    litePayload.setCompareBySupplier(true);
                } else if (payload.getCompareBy().equals(CompareType.TOOLMAKER)){
                    litePayload.setCompareByToolMaker(true);
                }
                litePayload.setCompanyId(data.getId());
                List<ToolingCycleTimeData> toolingDetails = moldService.findToolingCycleTimeData(finalMoldIds, litePayload, null, true);
                toolingDetails.forEach(detail -> {
                    data.getDetails().add(new ToolingCycleTimeDataLite(detail));
                });
            });
            }
        }
        // part production
        List<Long> toolingIdList= moldIds;
        List<Long> companyIds= null;
        if (Arrays.asList(CompareType.TOOL).contains(payload.getCompareBy())) {
            toolingIdList= result.stream().map(r->r.getMoldId()).collect(Collectors.toList());
        }else {
            companyIds = result.stream().map(r->r.getId()).collect(Collectors.toList());
        }
        List<PartProductionData> partProductionDataList=moldRepository.findPartProduction(toolingIdList,payload.getPartId(),
                payload.getStartDate(),payload.getEndDate(), payload.getCompareBy(),companyIds,null);
        Map<Long,List<PartProductionData>> mapPartProduction=partProductionDataList.stream().collect(
                Collectors.groupingBy(PartProductionData::getGroupId, HashMap::new, Collectors.toCollection(ArrayList::new))
        );
        result.stream().forEach(tooling->{
            if(mapPartProduction.containsKey(tooling.getId())){
                tooling.setPartProductionList(mapPartProduction.get(tooling.getId()));
                tooling.setNumberPart(mapPartProduction.get(tooling.getId())!=null?mapPartProduction.get(tooling.getId()).size():null);
            }
        });


        if(payload.getDuration() != null){
            List<String> titlesList=new ArrayList<>();
            titlesList.add(DateTimeUtils.getTitleRangDate(Instant.now().minus(payload.getDuration() * 4, ChronoUnit.DAYS)
                    , Instant.now().minus(payload.getDuration() * 3 + 1, ChronoUnit.DAYS)));
            titlesList.add(DateTimeUtils.getTitleRangDate(Instant.now().minus(payload.getDuration()*3, ChronoUnit.DAYS)
                    , Instant.now().minus(payload.getDuration() * 2 + 1, ChronoUnit.DAYS)));
            titlesList.add(DateTimeUtils.getTitleRangDate(Instant.now().minus(payload.getDuration()*2, ChronoUnit.DAYS)
                    , Instant.now().minus(payload.getDuration() + 1, ChronoUnit.DAYS)));
            titlesList.add(DateTimeUtils.getTitleRangDate(Instant.now().minus(payload.getDuration(), ChronoUnit.DAYS)
                    , Instant.now().minus(1, ChronoUnit.DAYS)));

            ProductivitySearchPayload subPayload = DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2));
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1));
/*
            ProductivitySearchPayload subPayload = ProductivitySearchPayload.builder()
                    .startDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2))
                    .endDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1))
                    .compareBy(payload.getCompareBy())
                    .build();
*/

            List<ToolingCycleTimeData> fifth = result;
            List<ToolingCycleTimeData> forth = moldService.findToolingCycleTimeData(moldIds, subPayload, null, true);

//            List<ToolingCycleTimeData> fifth = moldService.getAvgCycleTimeMoldInRange(top5MoldIds, payload, null);
//            List<ToolingCycleTimeData> forth = moldService.getAvgCycleTimeMoldInRange(top5MoldIds, subPayload, null);
//
            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 3));
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2 + 1));
//            List<ToolingCycleTimeData> third = moldService.getAvgCycleTimeMoldInRange(top5MoldIds, subPayload, null);
            List<ToolingCycleTimeData> third = moldService.findToolingCycleTimeData(moldIds, subPayload, null, true);

            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 4));
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 3 + 1));
//            List<ToolingCycleTimeData> second = moldService.getAvgCycleTimeMoldInRange(top5MoldIds, subPayload, null);
            List<ToolingCycleTimeData> second = moldService.findToolingCycleTimeData(moldIds, subPayload, null, true);

            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 5));
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 4 + 1));
//            List<ToolingCycleTimeData> first = moldService.getAvgCycleTimeMoldInRange(top5MoldIds, payload, null);
//            List<ToolingCycleTimeData> first = moldService.getComplianceShotCountInRange(moldIds, subPayload);
/*
            List<ToolingCycleTimeData> first = moldService.findToolingCycleTimeData(moldIds, subPayload, null, false);
*/

            result.forEach(data -> {
                List<ChartData> chartData = new ArrayList<>();
                Integer i = 0;
/*
                ToolingCycleTimeData firstData = first.stream().filter(x -> x.getId()!=null && x.getId().equals(data.getId())).findAny().orElse(null);
                Double firstCompliance = firstData != null ? firstData.getPercentageCompliance() : 0.0;
*/
                ToolingCycleTimeData secondData = second.stream().filter(x -> x.getId()!=null && x.getId().equals(data.getId())).findAny().orElse(null);
                Double secondCompliance = secondData != null ? secondData.getPercentageCompliance() : 0.0;
                Double secondL1 = secondData != null ? secondData.getPercentageL1() : 0.0;
                Double secondL2 = secondData != null ? secondData.getPercentageL2() : 0.0;
                ToolingCycleTimeData thirdData = third.stream().filter(x -> x.getId()!=null && x.getId().equals(data.getId())).findAny().orElse(null);
                Double thirdCompliance = thirdData != null ? thirdData.getPercentageCompliance() : 0.0;
                Double thirdL1 = thirdData != null ? thirdData.getPercentageL1() : 0.0;
                Double thirdL2 = thirdData != null ? thirdData.getPercentageL2() : 0.0;
                ToolingCycleTimeData forthData = forth.stream().filter(x -> x.getId()!=null && x.getId().equals(data.getId())).findAny().orElse(null);
                Double forthCompliance = forthData != null ? forthData.getPercentageCompliance() : 0.0;
                Double forthL1 = forthData != null ? forthData.getPercentageL1() : 0.0;
                Double forthL2 = forthData != null ? forthData.getPercentageL2() : 0.0;
                ToolingCycleTimeData fifthData = fifth.stream().filter(x -> x.getId()!=null && x.getId().equals(data.getId())).findAny().orElse(null);
                Double fifthCompliance = fifthData != null ? fifthData.getPercentageCompliance() : 0.0;
                Double fifthL1 = fifthData != null ? fifthData.getPercentageL1() : 0.0;
                Double fifthL2 = fifthData != null ? fifthData.getPercentageL2() : 0.0;

                chartData.add(ChartData.builder()
//                        .title((++i).toString())
                        .title(titlesList.get(i++))
                        .cycleTime(secondCompliance)
                        .cycleTimeL1(secondL1)
                        .cycleTimeL2(secondL2)
                        .build());

                chartData.add(ChartData.builder()
//                        .title((++i).toString())
                        .title(titlesList.get(i++))
                        .cycleTime(thirdCompliance)
                                .cycleTimeL1(thirdL1)
                                .cycleTimeL2(thirdL2)
                        .build());

                chartData.add(ChartData.builder()
//                        .title((++i).toString())
                        .title(titlesList.get(i++))
                        .cycleTime(forthCompliance)
                        .cycleTimeL1(forthL1)
                        .cycleTimeL2(forthL2)
                        .build());

                chartData.add(ChartData.builder()
//                        .title((++i).toString())
                        .title(titlesList.get(i++))
                        .cycleTime(fifthCompliance)
                        .cycleTimeL1(fifthL1)
                        .cycleTimeL2(fifthL2)
                        .build());
//                ToolingCycleTimeData firstItem = first.stream().filter(x -> x.getMoldId().equals(data.getMoldId())).findAny().orElse(null);
//                ToolingCycleTimeData secondItem = second.stream().filter(x -> x.getMoldId().equals(data.getMoldId())).findAny().orElse(null);
//                ToolingCycleTimeData thirdItem = third.stream().filter(x -> x.getMoldId().equals(data.getMoldId())).findAny().orElse(null);
//                ToolingCycleTimeData forthItem = forth.stream().filter(x -> x.getMoldId().equals(data.getMoldId())).findAny().orElse(null);
//                ToolingCycleTimeData fifthItem = fifth.stream().filter(x -> x.getMoldId().equals(data.getMoldId())).findAny().orElse(null);
//
//                if(firstItem != null){
//                    i++;
//                    chartData.add(ChartData.builder().title(i.toString()).cycleTime(firstItem.getCycleTime()).uptime(0L).build());
//                }
//                if(secondItem != null){
//                    i++;
//                    chartData.add(ChartData.builder().title(i.toString()).cycleTime(secondItem.getCycleTime()).uptime(0L).build());
//                }
//                if(thirdItem != null){
//                    i++;
//                    chartData.add(ChartData.builder().title(i.toString()).cycleTime(thirdItem.getCycleTime()).uptime(0L).build());
//                }
//                if(forthItem != null){
//                    i++;
//                    chartData.add(ChartData.builder().title(i.toString()).cycleTime(forthItem.getCycleTime()).uptime(0L).build());
//                }
//                if(fifthItem != null){
//                    i++;
//                    chartData.add(ChartData.builder().title(i.toString()).cycleTime(fifthItem.getCycleTime()).uptime(0L).build());
//                }
                chartData.stream().forEach(d->{
                    d.setCycleTime(DataUtils.roundDouble(d.getCycleTime(),2));
                    d.setCycleTimeL1(DataUtils.roundDouble(d.getCycleTimeL1(),2));
                    d.setCycleTimeL2(DataUtils.roundDouble(d.getCycleTimeL2(),2));
                });
                data.setChartData(chartData);

                //cal trend
                // Calculate trend for each item
                data.setTrend(fifthCompliance - forthCompliance);

            });
        }

        if(pageable != null ) {
            Sort.Direction[] directions = {Sort.Direction.DESC};
            pageable.getSort().forEach(order -> {
                directions[0] = order.getDirection();
            });
            if (directions[0].isAscending())
                result = result.stream().sorted(Comparator.nullsFirst(Comparator.comparing(ToolingCycleTimeData::getPercentageCompliance, Comparator.nullsFirst(Comparator.naturalOrder())))).collect(Collectors.toList());
            else
                result = result.stream().sorted(Comparator.nullsLast(Comparator.comparing(ToolingCycleTimeData::getPercentageCompliance, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())).collect(Collectors.toList());
        }

        return new RestDataList<>(total,result);
    }

    public SupplierProductionOverviewData getOverviewSupplierProduction(ProductivitySearchPayload payload, Pageable pageable){
        payload.setCheckForNewCounter(true);
        SupplierProductionOverviewData result =new SupplierProductionOverviewData();

        List<Long> moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);

        payload.setGroupBySuppliers(false);
//        List<SupplierProductionData> supplierProductionDataList = statisticsWutService.findProductionOverviewData(moldIds, payload);
//        Integer totalShot = supplierProductionDataList.isEmpty() ? 0 : supplierProductionDataList.get(0).getTotalShot();

        List<SupplierProductionData> supplierProductionWUTDataList = statisticsWutService.findProductionWUTQuantity(moldIds, payload, pageable);
        if (!supplierProductionWUTDataList.isEmpty()) {
            SupplierProductionData data = supplierProductionWUTDataList.get(0);
            Integer totalShot=data.getTotalShot();
            result = new SupplierProductionOverviewData(totalShot, data.getTotalNormalProduction(), data.getTotalWarmUpProduction(), data.getTotalCoolDownProduction(), data.getTotalAbnormalProduction());

            if (totalShot > 0) {
                if(result.getTotalWarmUpProduction()!=null)
                result.setWarmUpProductionPercent(result.getTotalWarmUpProduction() * 100 / Double.valueOf(totalShot));
                if(result.getTotalNormalProduction()!=null)
                result.setNormalProductionPercent(result.getTotalNormalProduction() * 100 / Double.valueOf(totalShot));
                if(result.getTotalCoolDownProduction() !=null)
                result.setCoolDownProductionPercent(result.getTotalCoolDownProduction() * 100 / Double.valueOf(totalShot));
                if(result.getTotalAbnormalProduction()!=null)
                result.setAbnormalProductionPercent(result.getTotalAbnormalProduction() * 100 / Double.valueOf(totalShot));
            }
            result.setTotalShot(totalShot);
        }
        //top 5
        RestDataList<SupplierProductionData> restDataList = getTop5SupplierProduction(moldIds, payload, pageable);

        result.setTop5Supplier(restDataList.getDataList());
        result.setTotalElements(restDataList.getTotal());

        return result;
    }


    public List<SupplierProductionData> getSupplierProductionList(List<Long> moldIds, ProductivitySearchPayload payload){
        payload.setCheckForNewCounter(true);
		if (moldIds == null) {
			moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);
		}

        payload.setGroupBySuppliers(true);
/*
        List<SupplierProductionData> supplierProductionDataList = statisticsWutService.findProductionOverviewData(moldIds, payload);
        Map<Long,SupplierProductionData> supplierProductionDataMap = new HashMap<>();
        supplierProductionDataList.stream().forEach(s->{
            supplierProductionDataMap.put(s.getId(),s);
        });
*/
        List<SupplierProductionData> supplierProductionWUTDataList = statisticsWutService.findProductionWUTQuantity(moldIds, payload, null);

        for (SupplierProductionData s : supplierProductionWUTDataList) {
//            SupplierProductionData dataTotal = supplierProductionDataMap.get(s.getId());
            SupplierProductionData dataTotal = s;
            s.setAbnormalProductionPercent(0.0);
            s.setCoolDownProductionPercent(0.0);
            s.setNormalProductionPercent(0.0);
            s.setWarmUpProductionPercent(0.0);
            if (dataTotal != null && dataTotal.getTotalShot() != null && dataTotal.getTotalShot() > 0) {
                s.setAbnormalProductionPercent(s.getTotalAbnormalProduction() * 100 / Double.valueOf(dataTotal.getTotalShot()));
                s.setCoolDownProductionPercent(s.getTotalCoolDownProduction() * 100 / Double.valueOf(dataTotal.getTotalShot()));
                s.setNormalProductionPercent(s.getTotalNormalProduction() * 100 / Double.valueOf(dataTotal.getTotalShot()));
                s.setWarmUpProductionPercent(s.getTotalWarmUpProduction() * 100 / Double.valueOf(dataTotal.getTotalShot()));
            }
        }
        return supplierProductionWUTDataList;
    }

    public RestDataList<SupplierProductionData> getTop5SupplierProduction(List<Long> moldIds, ProductivitySearchPayload payload
            , Pageable pageable){
        payload.setCheckForNewCounter(true);
		if (moldIds == null) {
			moldIds = moldService.findMoldIdsFromProductivitySearchPayload(payload);
		}
//        moldIds = moldIds != null ? moldIds : moldService.findMoldIdsFromProductivitySearchPayload(payload);
        List<SupplierProductionData> dataList = new ArrayList<>();
//        return moldService.findToolingProductivity(moldIds, payload, pageable);
        List<SupplierProductionData> supplierProductionDataList = getSupplierProductionList(moldIds,payload);
        Long total=Long.valueOf(supplierProductionDataList.size());

        if(pageable != null) {
            Sort.Direction[] directions = {Sort.Direction.DESC};
            pageable.getSort().forEach(order -> {
                directions[0] = order.getDirection();
            });
            if (directions[0].equals(Sort.Direction.ASC)) {
                supplierProductionDataList = supplierProductionDataList.stream().sorted(Comparator.comparing(SupplierProductionData::getNormalProductionPercent)).collect(Collectors.toList());
            } else {
                supplierProductionDataList = supplierProductionDataList.stream().sorted(Comparator.comparing(SupplierProductionData::getNormalProductionPercent).reversed()).collect(Collectors.toList());
            }
        }
//        dataList = pageable.getPageSize() >= supplierProductionDataList.size() ? supplierProductionDataList : new ArrayList<>(supplierProductionDataList.subList(0, pageable.getPageSize()));
        dataList = ListUtils.subList(supplierProductionDataList,pageable.getPageNumber(),pageable.getPageSize());
        // part production
        List<Long> toolingIdList= moldIds;
        List<Long> companyIds= null;
        if (Arrays.asList(CompareType.TOOL).contains(payload.getCompareBy())) {
            toolingIdList= dataList.stream().map(r->r.getId()).collect(Collectors.toList());
        }else {
            companyIds = dataList.stream().map(r->r.getId()).collect(Collectors.toList());
        }
        List<PartProductionData> partProductionDataList=moldRepository.findPartProduction(toolingIdList,payload.getPartId(),
                payload.getStartDate(),payload.getEndDate(), payload.getCompareBy(),companyIds,null);
        Map<Long,List<PartProductionData>> mapPartProduction=partProductionDataList.stream().collect(
                Collectors.groupingBy(PartProductionData::getGroupId, HashMap::new, Collectors.toCollection(ArrayList::new))
        );
        dataList.stream().forEach(tooling->{
            if(mapPartProduction.containsKey(tooling.getId())){
                tooling.setPartProductionList(mapPartProduction.get(tooling.getId()));
                tooling.setNumberPart(mapPartProduction.get(tooling.getId())!=null?mapPartProduction.get(tooling.getId()).size():null);
            }
        });


        //get for detail
        if(payload.getDuration() != null){

            List<SupplierProductionData>[] listDataPerPeriodic= new ArrayList[4];
            listDataPerPeriodic[0]=dataList;
            for (int lstT=1;lstT<listDataPerPeriodic.length;lstT++){
                ProductivitySearchPayload subPayload =  DataUtils.deepCopy(payload, ProductivitySearchPayload.class);
                subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * (lstT+1))+"000000");
                subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()*lstT + 1)+"000000");
/*
                ProductivitySearchPayload subPayload = ProductivitySearchPayload.builder()
                        .compareBy(payload.getCompareBy())
                        .startDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * (lstT+1))+"000000")
                        .endDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()*lstT + 1)+"000000")
                        .compareBy(payload.getCompareBy())
                        .build();
*/
                listDataPerPeriodic[lstT] = getSupplierProductionList(moldIds, subPayload);
            }
            /*
            ProductivitySearchPayload subPayload = ProductivitySearchPayload.builder()
                    .startDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2)+"000000")
                    .endDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1)+"000000")
                    .build();

//            List<SupplierProductionData> fifth = moldService.getComplianceShotCountInRange(moldIds, payload);
            List<SupplierProductionData> s2 = getSupplierProductionList(moldIds, subPayload);

            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 3)+"000000");
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 2 + 1)+"000000");
            List<SupplierProductionData> s3 = getSupplierProductionList(moldIds, subPayload);

            subPayload.setStartDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 4)+"000000");
            subPayload.setEndDate(DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() * 3 + 1)+"000000");
            List<SupplierProductionData> s4 = getSupplierProductionList(moldIds, subPayload);
*/
            dataList.forEach(data -> {
//                List<ChartData> chartData = new ArrayList<>();
//                Integer i = 0;
/*
                SupplierProductionData s4Data = s4.stream().filter(x -> x.getId().equals(data.getId())).findAny().orElse(null);
                SupplierProductionData s3Data = s3.stream().filter(x -> x.getId().equals(data.getId())).findAny().orElse(null);
                SupplierProductionData s2Data = s2.stream().filter(x -> x.getId().equals(data.getId())).findAny().orElse(null);
                List<SupplierProductionData> dataSupplierList = Arrays.asList(s4Data,s3Data,s2Data,data);
                List<String> dataTitleList= Arrays.asList(
                        (DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()*2 + 1)+"000000"),
                        (DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration() + 1)+"000000"),
                        (DateUtils.getPreviousDay("yyyyMMdd",  1))+"000000");
*/
                List<SupplierProductionData> dataSupplierList = new ArrayList<>();
                List<String> dataTitleList=new ArrayList<>();
                for(int i= listDataPerPeriodic.length;i>0;i--){
                    SupplierProductionData sData = listDataPerPeriodic[i-1].stream().filter(x -> x.getId().equals(data.getId())).findAny().orElse(null);
                    dataSupplierList.add(sData);
                    dataTitleList.add((DateUtils.getPreviousDay("yyyyMMdd", payload.getDuration()*(i-1) + 1)+"000000"));
                }
//                dataSupplierList.add(data);

                for (int i=0;i< dataSupplierList.size();i++) {
                    SupplierProductionData sp=dataSupplierList.get(i);
//                    SupplierProductionData spNext=dataSupplierList.get(i+1);
                    Double abnormalEvaluationPercent = null;
                    Double coolDownEvaluationPercent = null;
                    Double normalEvaluationPercent = null;
                    Double warmUpEvaluationPercent = null;

                    if (sp != null) {
/*
                        abnormalEvaluationPercent=calEvaluationPercent(sp.getTotalAbnormalProduction(),spNext.getTotalAbnormalProduction());
                        coolDownEvaluationPercent=calEvaluationPercent(sp.getTotalCoolDownProduction(),spNext.getTotalCoolDownProduction());
                        normalEvaluationPercent=calEvaluationPercent(sp.getTotalNormalProduction(),spNext.getTotalNormalProduction());
                        warmUpEvaluationPercent=calEvaluationPercent(sp.getTotalWarmUpProduction(),spNext.getTotalWarmUpProduction());
*/
/*
                        abnormalEvaluationPercent=subVal(sp.getAbnormalProductionPercent(),spNext.getAbnormalProductionPercent());
                        coolDownEvaluationPercent=subVal(sp.getCoolDownProductionPercent(),spNext.getCoolDownProductionPercent());
                        normalEvaluationPercent=subVal(sp.getNormalProductionPercent(),spNext.getNormalProductionPercent());
                        warmUpEvaluationPercent=subVal(sp.getWarmUpProductionPercent(),spNext.getWarmUpProductionPercent());
*/

                        abnormalEvaluationPercent= sp.getAbnormalProductionPercent();
                        coolDownEvaluationPercent= sp.getCoolDownProductionPercent();
                        normalEvaluationPercent= sp.getNormalProductionPercent();
                        warmUpEvaluationPercent= sp.getWarmUpProductionPercent();
                    }
//                    String title = getTitleByDue(dataTitleList.get(i), payload.getDuration());
                    String title = DateTimeUtils.getTitleRangDate(DateUtils.getInstant(dataTitleList.get(i), DateUtils.DEFAULT_DATE_FORMAT).minus(payload.getDuration() - 1, ChronoUnit.DAYS)
                            , DateUtils.getInstant(dataTitleList.get(i), DateUtils.DEFAULT_DATE_FORMAT));
                    data.getChartAbnormalData().add(ChartData.builder().title(title).trend(abnormalEvaluationPercent).build());
                    data.getChartCoolDownData().add(ChartData.builder().title(title).trend(coolDownEvaluationPercent).build());
                    data.getChartNormalData().add(ChartData.builder().title(title).trend(normalEvaluationPercent).build());
                    data.getChartWarmUpData().add(ChartData.builder().title(title).trend(warmUpEvaluationPercent).build());

                    if (i == dataSupplierList.size() - 2) {
                        SupplierProductionData spNext = dataSupplierList.get(i + 1);
                        if (sp != null && spNext != null) {
                            abnormalEvaluationPercent = subVal(sp.getAbnormalProductionPercent(), spNext.getAbnormalProductionPercent());
                            coolDownEvaluationPercent = subVal(sp.getCoolDownProductionPercent(), spNext.getCoolDownProductionPercent());
                            normalEvaluationPercent = subVal(sp.getNormalProductionPercent(), spNext.getNormalProductionPercent());
                            warmUpEvaluationPercent = subVal(sp.getWarmUpProductionPercent(), spNext.getWarmUpProductionPercent());

                            data.setAbnormalEvaluationPercent(abnormalEvaluationPercent);
                            data.setCoolDownEvaluationPercent(coolDownEvaluationPercent);
                            data.setNormalEvaluationPercent(normalEvaluationPercent);
                            data.setWarmUpEvaluationPercent(warmUpEvaluationPercent);
                        }
                    }
                }

            });
        }


        return new RestDataList<>(total,dataList);
    }
    private Double calEvaluationPercent(Integer val,Integer valNext){
        valNext = valNext!=null?valNext:0;
        if(val!=null && val!=0){
            return (valNext-val) * 100/Double.valueOf(val);
        }

        return  null;
    }
    private Double subVal(Double val,Double valNext){
        if(val!=null && valNext!=null){
            return valNext -  val;
        }
        return null;
    }

    public String getTitleByDue(String date, int due) {
        if (due == 7) {
            return "W" + DateUtils.getWeekOfYear(date);
        }
        if (due == 30) {
            return DateUtils.getDate(DateUtils.getInstant(date, DateUtils.DEFAULT_DATE_FORMAT), "MM");
        }
        if (due == 90) {
            return "Q" + ((Long.valueOf(DateUtils.getDate(DateUtils.getInstant(date, DateUtils.DEFAULT_DATE_FORMAT), "MM")) - 1) / 3 + 1);
        }
        if (due == 180) {
            return "H" + ((Long.valueOf(DateUtils.getDate(DateUtils.getInstant(date, DateUtils.DEFAULT_DATE_FORMAT), "MM")) - 1) / 6 + 1);
        }
        return date;
    }


}
