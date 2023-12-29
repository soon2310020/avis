package saleson.api.workOrder.payload;

import com.emoldino.framework.util.BeanUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import saleson.api.company.CompanyService;
import saleson.api.company.payload.CompanyPayload;
import saleson.common.enumeration.*;
import saleson.common.util.DateUtils;
import saleson.common.util.SecurityUtils;
import saleson.common.util.StringUtils;
import saleson.model.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchPayload {
    private List<WorkOrderType> typeList = Lists.newArrayList();
    private List<PriorityType> priorityTypeList = Lists.newArrayList();
    private List<Long> companyIdList = Lists.newArrayList();
    private List<WorkOrderType> orderType;
    private Boolean isPmWorkOrder;

    private Boolean assignedToMe;

    private Boolean createdByMe;

    private Boolean isHistory;

    private Boolean isAllCompany;

    private String status;

    private List<Long> assetIds;

    private Long start;
    private Long end;

    private List<Long> childCompanyIdList = Lists.newArrayList();

    public Predicate getPredicate() {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(commonFilter());

        if (!SecurityUtils.isAdmin()) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.user.companyId.in(childCompanyIdList))));
        }
        if (CompanyType.IN_HOUSE.equals(SecurityUtils.getCompanyType()) && CollectionUtils.isEmpty(companyIdList)&&isAllCompany!=null&&isAllCompany) {
            isAllCompany = false;
            companyIdList = BeanUtils.get(CompanyService.class).findAll(CompanyPayload.builder().build().getPredicate(), PageRequest.of(0, 9999)).stream().map(Company::getId).collect(Collectors.toList());
        } else if (CompanyType.IN_HOUSE.equals(SecurityUtils.getCompanyType())&&isAllCompany!=null&&isAllCompany) {
            isAllCompany = false;
        }

        if (CollectionUtils.isNotEmpty(typeList)) {
            builder.and(workOrder.orderType.in(typeList));
        }
        if (Boolean.TRUE.equals(isPmWorkOrder)){
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        }
        if (CollectionUtils.isNotEmpty(priorityTypeList)) {
            builder.and(workOrder.priority.in(priorityTypeList));
        }
        if (isHistory != null) {
            if (isHistory) {
                builder.and(workOrder.status.in(Arrays.asList(WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED,WorkOrderStatus.COMPLETED, WorkOrderStatus.CHANGE_REQUESTED)));
                if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end)) {
                    builder.and(workOrder.completedOn.between(getStartInstant(), getEndInstant()));
                }
            } else {
                builder.and(workOrder.status.notIn(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED, WorkOrderStatus.CHANGE_REQUESTED)));
            }
        }

        if (CollectionUtils.isNotEmpty(orderType)){
            builder.and(workOrder.orderType.in(orderType));
            if (orderType.size() == 1 && orderType.get(0).equals(WorkOrderType.EMERGENCY)) {
                if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end)) {
                    builder.and(workOrder.start.between(getStartInstant(), getEndInstant()));
                }
            }
        }

//        if (!StringUtils.isEmpty(end) && (isHistory != null && !isHistory))
//            builder.and(workOrder.completedOn.isNull().or(workOrder.completedOn.after(getEndInstant())));

        if (!StringUtils.isEmpty(status) && "open".equals(status)) {
            if (!StringUtils.isEmpty(end))
                builder.and(workOrder.status.in(Arrays.asList(WorkOrderStatus.REQUESTED, WorkOrderStatus.ACCEPTED)).or(workOrder.status.eq(WorkOrderStatus.PENDING_APPROVAL).and(workOrder.completedOn.isNull().or(workOrder.completedOn.after(getEndInstant())))));
            else
                builder.and(workOrder.status.in(Arrays.asList(WorkOrderStatus.REQUESTED, WorkOrderStatus.ACCEPTED, WorkOrderStatus.PENDING_APPROVAL)));
        }

        if (!StringUtils.isEmpty(status) && "overdue".equals(status)) {
            if (!StringUtils.isEmpty(end)) {
                builder.and(workOrder.end.before(getEndInstant()));
                builder.and(workOrder.status.in(Arrays.asList(WorkOrderStatus.REQUESTED, WorkOrderStatus.ACCEPTED)).or(workOrder.status.eq(WorkOrderStatus.PENDING_APPROVAL).and(workOrder.completedOn.isNull().or(workOrder.completedOn.after(getEndInstant())))));
            } else {
                builder.and(workOrder.status.in(Arrays.asList(WorkOrderStatus.REQUESTED, WorkOrderStatus.ACCEPTED, WorkOrderStatus.PENDING_APPROVAL)));
            }
        }
        if (!StringUtils.isEmpty(status) && "completed".equals(status)) {
            builder.and(workOrder.status.eq(WorkOrderStatus.COMPLETED));
            if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end))
                builder.and(workOrder.completedOn.between(getStartInstant(), getEndInstant()));
        }
        if (CollectionUtils.isNotEmpty(assetIds)) {
            builder.and(workOrderAsset.assetId.in(assetIds));
        }
//        if (!StringUtils.isEmpty(status) && "overdue".equals(status)) {
//            NumberExpression<Integer> millisecond = Expressions.numberTemplate(Integer.class, "UNIX_TIMESTAMP({0}) ", workOrder.start);
//            builder.and(moldMaintenance.isNull().or(moldMaintenance.dueDate.isNull()).or(millisecond.add(moldMaintenance.dueDate.multiply(24 * 3600L)).loe(getEndInstant().getEpochSecond())));
//        }
        setMyWorkOrderPredicate(builder, workOrder, workOrderUser);
        setCompanyPredicate(builder, workOrder);

        return builder;
    }

    public Predicate getPredicateTotal() {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(commonFilter());
        BooleanBuilder subBuilder = new BooleanBuilder();
        subBuilder.and(workOrder.status.notIn(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)).and(workOrder.completedOn.isNull().or(workOrder.completedOn.after(getEndInstant()))))
                .or(workOrder.status.in(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)).and(workOrder.completedOn.between(getStartInstant(), getEndInstant())));
        builder.and(subBuilder);
        if (!SecurityUtils.isAdmin()) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.user.companyId.in(childCompanyIdList))));
        }
        if (CollectionUtils.isNotEmpty(assetIds)) {
            builder.and(workOrderAsset.assetId.in(assetIds));
        }
        if (CollectionUtils.isNotEmpty(orderType))
            builder.and(workOrder.orderType.in(orderType));
        if (Boolean.TRUE.equals(isPmWorkOrder)){
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        }
        if (CollectionUtils.isNotEmpty(priorityTypeList))
            builder.and(workOrder.priority.in(priorityTypeList));
        setCompanyPredicate(builder, workOrder);
        return builder;
    }

    public Predicate getPredicateTotalOngoing() {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(commonFilter());
        builder.and(workOrder.status.notIn(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED, WorkOrderStatus.CHANGE_REQUESTED)));
//        if (!StringUtils.isEmpty(end))
//            builder.and(workOrder.completedOn.isNull().or(workOrder.completedOn.after(getEndInstant())));
        if (!StringUtils.isEmpty(status) && "overdue".equals(status)) {
//            NumberExpression<Integer> millisecond = Expressions.numberTemplate(Integer.class, "UNIX_TIMESTAMP({0}) ", workOrder.start);
//            builder.and(QMoldMaintenance.moldMaintenance.isNull().or(QMoldMaintenance.moldMaintenance.dueDate.isNull()).or(millisecond.add(QMoldMaintenance.moldMaintenance.dueDate.multiply(24 * 3600L)).loe(getEndInstant().getEpochSecond())));
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
            builder.and(workOrder.end.lt(Instant.now())).and(workOrder.status.ne(WorkOrderStatus.PENDING_APPROVAL));
            if (!StringUtils.isEmpty(end)) {
                builder.and(workOrder.end.before(getEndInstant()));
            }

        }
        if (!SecurityUtils.isAdmin()) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.user.companyId.in(childCompanyIdList))));
        }
        if (CollectionUtils.isNotEmpty(typeList))
            builder.and(workOrder.orderType.in(typeList));
        if (Boolean.TRUE.equals(isPmWorkOrder)){
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        }
        if (CollectionUtils.isNotEmpty(priorityTypeList))
            builder.and(workOrder.priority.in(priorityTypeList));
        if (CollectionUtils.isNotEmpty(assetIds)) {
            builder.and(workOrderAsset.assetId.in(assetIds));
        }
        setMyWorkOrderPredicate(builder, workOrder, workOrderUser);
        setCompanyPredicate(builder, workOrder);
        return builder;
    }

    private void setMyWorkOrderPredicate( BooleanBuilder builder, QWorkOrder workOrder, QWorkOrderUser workOrderUser) {
        if (createdByMe != null && createdByMe) {
            builder.and(workOrder.createdBy.eq(SecurityUtils.getUserId()));
            if (CollectionUtils.isEmpty(companyIdList))
                companyIdList = BeanUtils.get(CompanyService.class).findAll(CompanyPayload.builder().build().getPredicate(), PageRequest.of(0, 9999)).stream().map(Company::getId).collect(Collectors.toList());
            isAllCompany = false;
        }

        if (assignedToMe != null && assignedToMe) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.userId.eq(SecurityUtils.getUserId())))
            );
            if (CollectionUtils.isEmpty(companyIdList))
                companyIdList = BeanUtils.get(CompanyService.class).findAll(CompanyPayload.builder().build().getPredicate(), PageRequest.of(0, 9999)).stream().map(Company::getId).collect(Collectors.toList());
            isAllCompany = false;
        }
    }

    public Predicate getPredicateTotalHistory() {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(commonFilter());
        builder.and(workOrder.status.in(Arrays.asList(WorkOrderStatus. CANCELLED, WorkOrderStatus.DECLINED,WorkOrderStatus.COMPLETED, WorkOrderStatus.CHANGE_REQUESTED)));
        if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end)) {
            builder.and(workOrder.completedOn.between(getStartInstant(), getEndInstant()));
        }

        if (!SecurityUtils.isAdmin()) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.user.companyId.in(childCompanyIdList))));
        }
        if (!StringUtils.isEmpty(status) && "completed".equals(status)) {
            builder.and(workOrder.status.eq(WorkOrderStatus.COMPLETED));
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        }
        if (CollectionUtils.isNotEmpty(typeList))
            builder.and(workOrder.orderType.in(typeList));
        if (Boolean.TRUE.equals(isPmWorkOrder)){
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        }
        if (CollectionUtils.isNotEmpty(priorityTypeList))
            builder.and(workOrder.priority.in(priorityTypeList));
        if (CollectionUtils.isNotEmpty(assetIds)) {
            builder.and(workOrderAsset.assetId.in(assetIds));
        }
        setMyWorkOrderPredicate(builder, workOrder, workOrderUser);
        setCompanyPredicate(builder, workOrder);
        return builder;
    }

    private void setCompanyPredicate( BooleanBuilder builder, QWorkOrder workOrder) {
        QMold mold = QMold.mold;
        QMachine machine = QMachine.machine;
        QTerminal terminal = QTerminal.terminal;
        QCounter counter = QCounter.counter;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        if ((isAllCompany != null && !isAllCompany) || (companyIdList != null && companyIdList.size() == 1)) {
//            List<Long> companyIdAllTierHierarchy = new ArrayList<>();
//            companyIdList.forEach(c -> addAllAccessibleCompanyIds(companyIdAllTierHierarchy,c));

            BooleanBuilder assetBuilder = new BooleanBuilder();
            assetBuilder.or(workOrder.id.eq(JPAExpressions
                    .select(workOrderAsset.workOrderId).distinct()
                    .from(workOrderAsset).innerJoin(mold).on(mold.id.eq(workOrderAsset.assetId).and(workOrderAsset.type.eq(ObjectType.TOOLING)))
                    .where(workOrderAsset.workOrderId.eq(workOrder.id).and(mold.companyId.in(companyIdList))))
            );
            assetBuilder.or(workOrder.id.eq(JPAExpressions
                    .select(workOrderAsset.workOrderId).distinct()
                    .from(workOrderAsset).innerJoin(machine).on(machine.id.eq(workOrderAsset.assetId).and(workOrderAsset.type.eq(ObjectType.MACHINE)))
                    .where(workOrderAsset.workOrderId.eq(workOrder.id).and(machine.companyId.in(companyIdList))))
            );
            assetBuilder.or(workOrder.id.eq(JPAExpressions
                    .select(workOrderAsset.workOrderId).distinct()
                    .from(workOrderAsset).innerJoin(terminal).on(terminal.id.eq(workOrderAsset.assetId).and(workOrderAsset.type.eq(ObjectType.TERMINAL)))
                    .where(workOrderAsset.workOrderId.eq(workOrder.id).and(terminal.companyId.in(companyIdList))))
            );
            assetBuilder.or(workOrder.id.eq(JPAExpressions
                    .select(workOrderAsset.workOrderId).distinct()
                    .from(workOrderAsset).innerJoin(counter).on(counter.id.eq(workOrderAsset.assetId).and(workOrderAsset.type.eq(ObjectType.COUNTER)))
                    .where(workOrderAsset.workOrderId.eq(workOrder.id).and(counter.companyId.in(companyIdList))))
            );
            builder.and(assetBuilder);
            builder.and(commonFilter());
        }
    }

    public Predicate getPredicateOverdue() {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        BooleanBuilder builder = new BooleanBuilder();
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        builder.and(commonFilter());
        if (!SecurityUtils.isAdmin()) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.user.companyId.in(childCompanyIdList))));
        }
        builder.and(workOrder.status.notIn(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.DECLINED, WorkOrderStatus.CANCELLED, WorkOrderStatus.CHANGE_REQUESTED)))
                .and(workOrder.end.lt(Instant.now()));
        if (createdByMe != null && createdByMe) {
            builder.and(workOrder.createdBy.eq(SecurityUtils.getUserId()));
        }

        if (assignedToMe != null && assignedToMe) {
            builder.and(workOrder.id.in(JPAExpressions
                    .select(workOrderUser.workOrderId)
                    .from(workOrderUser)
                    .where(workOrderUser.userId.eq(SecurityUtils.getUserId())))
            );
        }
        if (CollectionUtils.isNotEmpty(typeList))
            builder.and(workOrder.orderType.in(typeList));
        if (Boolean.TRUE.equals(isPmWorkOrder)){
            builder.and(workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
        }
        if (CollectionUtils.isNotEmpty(priorityTypeList))
            builder.and(workOrder.priority.in(priorityTypeList));

        if (!StringUtils.isEmpty(end)) {
            builder.and(workOrder.end.before(getEndInstant()));
        }
        if (CollectionUtils.isNotEmpty(assetIds)) {
            builder.and(workOrderAsset.assetId.in(assetIds));
        }
        setMyWorkOrderPredicate(builder, workOrder, workOrderUser);
        setCompanyPredicate(builder, workOrder);
        return builder;
    }

    private BooleanBuilder commonFilter(){
        QWorkOrder workOrder = QWorkOrder.workOrder;
        BooleanBuilder builder = new BooleanBuilder();
        //filter by month
        if (!StringUtils.isEmpty(start) && !StringUtils.isEmpty(end)) {
            if (isHistory != null && !isHistory) {
                builder.and(workOrder.start.before(getEndInstant()));
            }
        }
        builder.and(workOrder.requestChange.isNull().or(workOrder.requestChange.isFalse()))
                .and(workOrder.status.ne(WorkOrderStatus.APPROVAL_REQUESTED))
                .and(workOrder.status.ne(WorkOrderStatus.REQUESTED_NOT_FINISHED));
        return builder;
    }

    private Instant getStartInstant() {
        return !StringUtils.isEmpty(start) ? Instant.ofEpochSecond(start) : Instant.now();
    }

    private Instant getEndInstant() {
        return !StringUtils.isEmpty(end) ? (Instant.ofEpochSecond(end).plus(1, ChronoUnit.DAYS).compareTo(Instant.now()) > 0 ? Instant.now() : Instant.ofEpochSecond(end).plus(1, ChronoUnit.DAYS)) : Instant.now();
    }
}
