package saleson.api.workOrder.payload;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPAExpressions;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import saleson.api.mold.payload.ExportPayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.enumeration.WorkOrderType;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.model.QWorkOrder;
import saleson.model.QWorkOrderAsset;
import saleson.model.QWorkOrderUser;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class ExportWorkOrderPayload extends ExportPayload {
    private List<WorkOrderType> typeList = Lists.newArrayList();

    private List<Long> moldIds = new ArrayList<>();

    private List<Long> machineIds = new ArrayList<>();

    private List<Long> terminalIds = new ArrayList<>();

    private List<Long> counterIds = new ArrayList<>();

    private Instant startTime;

    private Instant endTime;


    public Predicate getPredicateExport() {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(commonFilter());

        if (!SecurityUtils.isAdmin()) {
            builder.and(workOrder.id.in(JPAExpressions
                            .select(workOrderUser.workOrderId)
                            .from(workOrderUser)
                            .where(workOrderUser.userId.eq(SecurityUtils.getUserId())))
                    .or(workOrder.createdBy.eq(SecurityUtils.getUserId()))
            );
        }


        if(CollectionUtils.isNotEmpty(moldIds)) {
            builder.and(workOrderAsset.assetId.in(moldIds)).and(workOrderAsset.type.eq(ObjectType.TOOLING));
        }

        if(CollectionUtils.isNotEmpty(machineIds)) {
            builder.and(workOrderAsset.assetId.in(machineIds)).and(workOrderAsset.type.eq(ObjectType.MACHINE));
        }

        if(CollectionUtils.isNotEmpty(terminalIds)) {
            builder.and(workOrderAsset.assetId.in(terminalIds)).and(workOrderAsset.type.eq(ObjectType.TERMINAL));
        }

        if(CollectionUtils.isNotEmpty(counterIds)) {
            builder.and(workOrderAsset.assetId.in(counterIds)).and(workOrderAsset.type.eq(ObjectType.COUNTER));
        }

        if (CollectionUtils.isNotEmpty(typeList) && typeList.size() != WorkOrderType.values().length) {
            builder.and(workOrder.orderType.in(typeList));
        }

        builder.and(workOrder.start.between(startTime, endTime)
                .or(workOrder.end.between(startTime, endTime))
                .or(workOrder.start.loe(startTime).and(workOrder.end.goe(startTime))));


        return builder;
    }

    private BooleanBuilder commonFilter(){
        QWorkOrder workOrder = QWorkOrder.workOrder;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(workOrder.requestChange.isNull().or(workOrder.requestChange.isFalse()))
                .and(workOrder.status.ne(WorkOrderStatus.APPROVAL_REQUESTED))
                .and(workOrder.status.ne(WorkOrderStatus.REQUESTED_NOT_FINISHED));
        return builder;
    }

    public String getDisplayTime() {
        String displayTime;
        switch (getRangeType()) {
            case CUSTOM_RANGE: {
                if (getToDate().equals(getFromDate())) {
                    displayTime = getFromDate();
                } else {
                    displayTime =  getFromDate()+" to "+getToDate();
                }
                break;
            }

            case MONTHLY: {
                String time = getTime();
                Instant date = DateUtils.getInstant(time + "01000000", DateUtils.DEFAULT_DATE_FORMAT);
                displayTime = DateUtils.getDate(date, DateUtils.MMMM) +"_"+time.substring(0, 4);
                break;
            }
            case WEEKLY: {
                String time = getTime();
                displayTime = "W"+time.substring(0,1)+"_"+time.substring(0, 4);
                break;
            }
            case YEARLY:
            default:
                displayTime = getTime();
                break;
        }
        return displayTime;
    }

    public String getDisplayFullTime() {
        String displayTime;
        switch (getRangeType()) {
            case CUSTOM_RANGE: {
                if (getToDate().equals(getFromDate())) {
                    displayTime =  DateUtils.convertStringDateFormat(getFromDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils.YYYY_MM_dd);
                } else {
                    String fromDateString = DateUtils.convertStringDateFormat(getFromDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils.YYYY_MM_dd);
                    String toDateString = DateUtils.convertStringDateFormat(getToDate(), DateUtils.YYYY_MM_DD_DATE_FORMAT, DateUtils.YYYY_MM_dd);
                    displayTime = fromDateString+" to "+ toDateString;
                }
                break;
            }

            case MONTHLY: {
                String time = getTime();
                Instant date = DateUtils.getInstant(time + "01000000", DateUtils.DEFAULT_DATE_FORMAT);
                displayTime = DateUtils.getDate(date, DateUtils.MMMM) +" "+time.substring(0, 4);
                break;
            }
            case WEEKLY: {
                String time = getTime();
                displayTime = "Week "+time.substring(4,6)+" "+time.substring(0, 4);
                break;
            }
            case YEARLY:
            default:
                displayTime = getTime();
                break;
        }
        return displayTime;
    }

    public boolean isAllTypes() {
        return typeList == null || typeList.size() == WorkOrderType.values().length;
    }

    public ExportWorkOrderPayload cloneSearchObject() {
        ExportWorkOrderPayload exportWorkOrderPayload = new ExportWorkOrderPayload();
        exportWorkOrderPayload.setDataTypes(getDataTypes());
        exportWorkOrderPayload.setFrequency(getFrequency());
        exportWorkOrderPayload.setFromDate(getFromDate());
        exportWorkOrderPayload.setToDate(getToDate());
        exportWorkOrderPayload.setRangeType(getRangeType());
        exportWorkOrderPayload.setTime(getTime());
        exportWorkOrderPayload.setTimezoneOffsetClient(getTimezoneOffsetClient());
        exportWorkOrderPayload.setStartTime(getStartTime());
        exportWorkOrderPayload.setEndTime(getEndTime());

        return exportWorkOrderPayload;
    }

    public boolean isSingleFile() {
        List<Long> allIds = Lists.newArrayList();
        allIds.addAll(moldIds);
        allIds.addAll(machineIds);
        allIds.addAll(terminalIds);
        allIds.addAll(counterIds);
        return CollectionUtils.isNotEmpty(allIds) && allIds.size() == 1;
    }
}
