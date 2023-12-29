package saleson.api.workOrder;

import com.emoldino.framework.repository.Q;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import saleson.api.workOrder.payload.SearchPayload;
import saleson.common.enumeration.ObjectType;
import saleson.common.enumeration.PriorityType;
import saleson.common.enumeration.WorkOrderStatus;
import saleson.common.enumeration.WorkOrderType;
import saleson.dto.WorkOrderDTO;
import saleson.model.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class WorkOrderRepositoryImpl  extends QuerydslRepositorySupport implements WorkOrderRepositoryCustom {
    public WorkOrderRepositoryImpl() {
        super(WorkOrder.class);
    }

    @Override
    public List<WorkOrderDTO> searchWorkOrder(Predicate predicate, Pageable pageable) {
        JPQLQuery query = getSearchQuery(predicate, pageable);
        query.limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        return query.fetch();
    }

    @Override
    public List<WorkOrderDTO> searchWorkOrderWithAssetIds(Predicate predicate, Pageable pageable, List<Long> assetIds) {
        JPQLQuery query = getSearchQueryWithAssetIds(predicate, pageable, assetIds);
        query.limit(pageable.getPageSize())
                .offset(pageable.getOffset());
        return query.fetch();
    }

    @Override
    public long countByPredicate(Predicate predicate) {
        JPQLQuery query = getSearchQuery(predicate, null);
        return query.fetchCount();
    }

    @Override
    public long countByPredicateAndAssetIdsIn(Predicate predicate, List<Long> assetIds) {
        JPQLQuery query = getSearchQueryWithAssetIds(predicate, null, assetIds);
        return query.fetchCount();


    }

    @Override
    public Optional<WorkOrder> findFirstByStatusAndAssetIdOrderByCompletedOnDesc(WorkOrderStatus status, Long moldId) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(workOrder.status.eq(status)).and(workOrderAsset.assetId.eq(moldId));
        builder.and(workOrder.requestChange.isNull().or(workOrder.requestChange.isFalse()));

        JPQLQuery query = from(workOrder)
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .where(builder);
        query.orderBy(workOrder.completedOn.desc());
        query.limit(1);

        return Optional.ofNullable(query.fetchOne() == null ? null : (WorkOrder) query.fetchOne());
    }

    @Override
    public Optional<WorkOrder> findFirstByStatusNotInAndAssetIdAndOrderType(List<WorkOrderStatus> statuses, Long moldId, WorkOrderType type) {
        if (moldId == null) {
            return Optional.empty();
        }
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(workOrder.status.notIn(statuses)).and(workOrderAsset.assetId.eq(moldId));
        builder.and(workOrder.requestChange.isNull().or(workOrder.requestChange.isFalse()));
        builder.and(workOrder.orderType.eq(type));

        JPQLQuery query = from(workOrder)
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .where(builder);
        query.orderBy(workOrder.createdAt.desc());
        query.limit(1);

        return Optional.ofNullable(query.fetchOne() == null ? null : (WorkOrder) query.fetchOne());
    }

    @Override
    public Optional<WorkOrder> findFirstByStatusAndAssetIdAndOrderType(WorkOrderStatus status, Long moldId, WorkOrderType type) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(workOrder.status.eq(status)).and(workOrderAsset.assetId.eq(moldId));
        builder.and(workOrder.orderType.eq(type));

        JPQLQuery query = from(workOrder)
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .where(builder);
        query.orderBy(workOrder.createdAt.desc());
        query.limit(1);

        return Optional.ofNullable(query.fetchOne() == null ? null : (WorkOrder) query.fetchOne());
    }

    @Override
    public Long countByStatusInAndAssetId(List<WorkOrderStatus> statuses, Long moldId) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(workOrder.status.in(statuses)).and(workOrderAsset.assetId.eq(moldId));
        builder.and(workOrder.requestChange.isNull().or(workOrder.requestChange.isFalse()));

        JPQLQuery query = from(workOrder)
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .where(builder);
        return (long) query.select(workOrder.id.countDistinct()).fetchOne();
    }

    @Override
    public Long countByOrderTypeAndAssetIdAndAssetType(WorkOrderType workOrderType, Long assetId, ObjectType assetType) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(workOrder.orderType.eq(workOrderType))
                .and(workOrderAsset.assetId.eq(assetId))
                .and(workOrderAsset.type.eq(assetType));
        builder.and(workOrder.status.notIn(Arrays.asList(WorkOrderStatus.COMPLETED, WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)));

        JPQLQuery query = from(workOrder)
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .where(builder);
        return (long) query.select(workOrder.id.countDistinct()).fetchOne();
    }

    @Override
    public Optional<WorkOrderDTO> findOptionalById(Long id) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QUser createdBy = new QUser("createdBy");
        QMoldMaintenance moldMaintenance = QMoldMaintenance.moldMaintenance;
//        DateTemplate<Instant> endDate = Expressions.dateTemplate(Instant.class, " ADDDATE({0},{1}) ", workOrder.start,moldMaintenance.dueDate);
//        DateTimeExpression<Instant> dateExpression = new CaseBuilder().when(moldMaintenance.isNull().and(moldMaintenance.dueDate.isNull()))
//                .then(workOrder.end).otherwise(endDate);
        JPQLQuery<WorkOrderDTO> query = from(workOrder).select(Projections.constructor(WorkOrderDTO.class,workOrder,createdBy))
                .leftJoin(moldMaintenance).on(workOrder.moldMaintenanceId.eq(moldMaintenance.id))
                .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                .where(workOrder.id.eq(id));
        return  Optional.ofNullable(query.fetchFirst());
    }

    private JPQLQuery getSearchQuery(Predicate predicate, Pageable pageable) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QUser createdBy = new QUser("createdBy");
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        QUser user = new QUser("user");

        OrderSpecifier orderBy = workOrder.updatedAt.desc();

        if (pageable != null && pageable.getSort().isSorted()) {
            String property  = pageable.getSort().get().findFirst().get().getProperty();
            Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
            if ("workOrderId".equals(property)) {
                StringExpression expression = workOrder.workOrderId;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("orderType".equals(property)) {
                EnumPath<WorkOrderType> expression = workOrder.orderType;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("priority".equals(property)) {
                NumberExpression expression = new CaseBuilder().when(workOrder.priority.eq(PriorityType.HIGH)).then(1)
                        .when(workOrder.priority.eq(PriorityType.MEDIUM)).then(2)
                        .otherwise(3);
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("status".equals(property)) {
                EnumPath<WorkOrderStatus> expression = workOrder.status;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("createdBy".equals(property)) {
                StringExpression expression = createdBy.name;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("start".equals(property)) {
                DateTimePath<Instant> expression = workOrder.start;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("end".equals(property)) {
                DateTimePath<Instant> expression = workOrder.end;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("workOrderUsers".equals(property)) {
                StringExpression expression = user.name;
                orderBy = isAsc ? expression.asc() : expression.desc();
            }

        }

        JPQLQuery<WorkOrderDTO> query = from(workOrder)
                .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                .leftJoin(workOrderUser).on(workOrder.id.eq(workOrderUser.workOrderId)
                        .and(workOrderUser.id.eq(JPAExpressions.select(workOrderUser.id.max()).from(workOrderUser).where(workOrderUser.workOrderId.eq(workOrder.id)))))
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .leftJoin(user).on(user.id.eq(workOrderUser.userId))
                .select(Projections.constructor(WorkOrderDTO.class, workOrder, createdBy))
                .where(predicate).orderBy(orderBy);
        return query;
    }

    private JPQLQuery getSearchQueryWithAssetIds(Predicate predicate, Pageable pageable, List<Long> assetIds) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QUser createdBy = new QUser("createdBy");
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        QUser user = new QUser("user");

        OrderSpecifier orderBy = workOrder.updatedAt.desc();
//        DateTemplate<Instant> endDate = Expressions.dateTemplate(Instant.class, " ADDDATE({0},{1}) ", workOrder.start, QMoldMaintenance.moldMaintenance.dueDate);
//        DateTimeExpression<Instant> dateExpression = new CaseBuilder().when(QMoldMaintenance.moldMaintenance.isNull().and(QMoldMaintenance.moldMaintenance.dueDate.isNull()))
//                .then(workOrder.end).otherwise(endDate);

        if (pageable != null && pageable.getSort().isSorted()) {
            String property  = pageable.getSort().get().findFirst().get().getProperty();
            Boolean isAsc = pageable.getSort().getOrderFor(property).isAscending();
            if ("workOrderId".equals(property)) {
                StringExpression expression = workOrder.workOrderId;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("orderType".equals(property)) {
                EnumPath<WorkOrderType> expression = workOrder.orderType;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("priority".equals(property)) {
                NumberExpression expression = new CaseBuilder().when(workOrder.priority.eq(PriorityType.HIGH)).then(1)
                        .when(workOrder.priority.eq(PriorityType.MEDIUM)).then(2)
                        .otherwise(3);
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("status".equals(property)) {
                EnumPath<WorkOrderStatus> expression = workOrder.status;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("createdBy".equals(property)) {
                StringExpression expression = createdBy.name;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("start".equals(property)) {
                DateTimePath<Instant> expression = workOrder.start;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("end".equals(property)) {
                DateTimePath<Instant> expression = workOrder.end;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("workOrderUsers".equals(property)) {
                StringExpression expression = user.name;
                orderBy = isAsc ? expression.asc() : expression.desc();
            } else if ("workOrderAssets".equals(property)) {
                NumberExpression expression = workOrder.workOrderAssets.size();
                orderBy = isAsc ? expression.asc() : expression.desc();
            }

        }
        JPQLQuery<WorkOrderDTO> query;
        if (CollectionUtils.isNotEmpty(assetIds)) {
            query = from(workOrder)
                    .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                    .leftJoin(workOrderUser).on(workOrder.id.eq(workOrderUser.workOrderId)
                            .and(workOrderUser.id.eq(JPAExpressions.select(workOrderUser.id.max()).from(workOrderUser).where(workOrderUser.workOrderId.eq(workOrder.id)))))
                    .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId).and(workOrderAsset.assetId.in(assetIds)))
                    .leftJoin(QMoldMaintenance.moldMaintenance).on(workOrder.moldMaintenanceId.eq(QMoldMaintenance.moldMaintenance.id))
                    .leftJoin(user).on(user.id.eq(workOrderUser.userId))
                    .select(Projections.constructor(WorkOrderDTO.class, workOrder, createdBy))
                    .where(predicate).orderBy(orderBy);
        } else {
            query = from(workOrder)
                    .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                    .leftJoin(workOrderUser).on(workOrder.id.eq(workOrderUser.workOrderId)
                            .and(workOrderUser.id.eq(JPAExpressions.select(workOrderUser.id.max()).from(workOrderUser).where(workOrderUser.workOrderId.eq(workOrder.id)))))
                    .leftJoin(user).on(user.id.eq(workOrderUser.userId))
                    .leftJoin(QMoldMaintenance.moldMaintenance).on(workOrder.moldMaintenanceId.eq(QMoldMaintenance.moldMaintenance.id))
                    .select(Projections.constructor(WorkOrderDTO.class, workOrder, createdBy))
                    .where(predicate).orderBy(orderBy);
        }


        return query;
    }

    public List<WorkOrderDTO> getWorkOrderListForExport(Predicate predicate) {
        QWorkOrder workOrder = QWorkOrder.workOrder;
        QUser createdBy = new QUser("createdBy");
        QWorkOrderUser workOrderUser = QWorkOrderUser.workOrderUser;
        QWorkOrderAsset workOrderAsset = QWorkOrderAsset.workOrderAsset;
        QUser user = new QUser("user");
        JPQLQuery<WorkOrderDTO> query = from(workOrder)
                .innerJoin(createdBy).on(workOrder.createdBy.eq(createdBy.id))
                .leftJoin(workOrderUser).on(workOrder.id.eq(workOrderUser.workOrderId)
                        .and(workOrderUser.id.eq(JPAExpressions.select(workOrderUser.id.max()).from(workOrderUser).where(workOrderUser.workOrderId.eq(workOrder.id)))))
                .leftJoin(user).on(user.id.eq(workOrderUser.userId))
                .leftJoin(workOrderAsset).on(workOrder.id.eq(workOrderAsset.workOrderId))
                .select(Projections.constructor(WorkOrderDTO.class, workOrder, createdBy))
                .where(predicate);
        return query.fetch();
    }
}
