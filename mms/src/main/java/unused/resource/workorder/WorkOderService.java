package unused.resource.workorder;

//@Service
//public class WorkOderService {
//
//    public WorkOrderStatusOut getWorkOrderStatus(Pageable pageable) {
//
//        BooleanBuilder builder = new BooleanBuilder();
//
//        NumberExpression<Long> totalCount = Q.workOrder.countDistinct();
//        NumberExpression<Long> overdueCount =
//            new CaseBuilder().when(Q.workOrder.end.before(Instant.now())
//                .and(Q.workOrder.status.notIn(Arrays.asList(WorkOrderStatus.DECLINED,
//                    WorkOrderStatus.CANCELLED,
//                    WorkOrderStatus.COMPLETED,
//                    WorkOrderStatus.REQUESTED_NOT_FINISHED,
//                    WorkOrderStatus.CM_APPROVAL_REQUESTED,
//                    WorkOrderStatus.PENDING_APPROVAL))
//                )).then(Q.workOrder.id).otherwise(Expressions.nullExpression()).countDistinct().as("overdueCount");
////        NumberExpression<Long> overdueRate = (overdueCount.multiply(100).divide(totalCount));// got error with count divide count
//
//        JPAQuery<WorkOrderStatusOut.WorkOrderStatusItem> query = BeanUtils.get(JPAQueryFactory.class)//
//            .select(Projections.constructor(WorkOrderStatusOut.WorkOrderStatusItem.class, //
//                Q.company.id, Q.company.name, Q.company.companyCode, //
//                overdueCount,
//                totalCount
//            ))//
//            .from(Q.workOrder)
//            .join(Q.workOrderAsset).on(Q.workOrder.id.eq(Q.workOrderAsset.workOrderId))
//            .leftJoin(Q.mold).on(Q.mold.id.eq(Q.workOrderAsset.assetId).and(Q.workOrderAsset.type.eq(ObjectType.TOOLING)))
//            .leftJoin(Q.machine).on(Q.machine.id.eq(Q.workOrderAsset.assetId).and(Q.workOrderAsset.type.eq(ObjectType.MACHINE)))
//            .innerJoin(Q.company).on(Q.mold.companyId.eq(Q.company.id).or(Q.machine.companyId.eq(Q.company.id)))
//            ;
//
//        builder.and(Q.workOrder.requestChange.isNull().or(Q.workOrder.requestChange.isFalse()))
//            .and(Q.workOrder.status.ne(WorkOrderStatus.CM_APPROVAL_REQUESTED))
//            .and(Q.workOrder.status.ne(WorkOrderStatus.REQUESTED_NOT_FINISHED));
//
//        Set<EntityPathBase<?>> join = new HashSet<>();
//        join.add(Q.company);
//        join.add(Q.mold);
//        join.add(Q.machine);
//        QueryUtils.applyCompanyFilter(query, join, "COMMON",null);
//
//        query.where(builder);
//        query.groupBy(Q.company);
////        query.orderBy(overdueRate.desc());
//
//        Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
////            .put("overdueCount", overdueCount)//
//            .build();
//
////        pageable = pageable!=null?pageable: PageRequest.of(0,1000);
//        QueryUtils.applyPagination(query,PageRequest.of(0,1000));
////        QueryUtils.applyPagination(query, pageable, fieldMap,totalCount.desc(), Q.mold.equipmentCode.asc(), Q.company.name.asc());
//        QueryResults<WorkOrderStatusOut.WorkOrderStatusItem> results = query.fetchResults();
//        List<WorkOrderStatusOut.WorkOrderStatusItem> results1= results.getResults();
//        Collections.sort(results1,Comparator.comparingDouble(WorkOrderStatusOut.WorkOrderStatusItem::getOverdueRate).reversed());
//        return new WorkOrderStatusOut(ListUtils.subList(results1, pageable.getPageNumber(), pageable.getPageSize()), pageable, results.getTotal());
//    }
//
//    public PMComplianceOut getPmCompliance(Pageable pageable) {
//
//
//        BooleanBuilder builder = new BooleanBuilder();
//
//        NumberExpression<Long> totalCount = Q.workOrder.countDistinct();
//        NumberExpression<Long> completedCount =
//            new CaseBuilder().when(Q.workOrder.end.before(Instant.now())
//                .and(Q.workOrder.status.eq(WorkOrderStatus.COMPLETED)
//                )).then(Q.workOrder.id).otherwise(Expressions.nullExpression()).countDistinct().as("overdueCount");
////        NumberExpression<Long> overdueRate = (overdueCount.multiply(100).divide(totalCount));// got error with count divide count
//
//        JPAQuery<PMComplianceOut.PMComplianceItem> query = BeanUtils.get(JPAQueryFactory.class)//
//            .select(Projections.constructor(PMComplianceOut.PMComplianceItem.class, //
//                Q.company.id, Q.company.name, Q.company.companyCode, //
//                completedCount,
//                totalCount
//            ))//
//            .from(Q.workOrder)
//            .join(Q.workOrderAsset).on(Q.workOrder.id.eq(Q.workOrderAsset.workOrderId))
//            .leftJoin(Q.mold).on(Q.mold.id.eq(Q.workOrderAsset.assetId).and(Q.workOrderAsset.type.eq(ObjectType.TOOLING)))
//            .leftJoin(Q.machine).on(Q.machine.id.eq(Q.workOrderAsset.assetId).and(Q.workOrderAsset.type.eq(ObjectType.MACHINE)))
//            .innerJoin(Q.company).on(Q.mold.companyId.eq(Q.company.id).or(Q.machine.companyId.eq(Q.company.id)))
//            ;
//
//        builder.and(Q.workOrder.orderType.eq(WorkOrderType.PREVENTATIVE_MAINTENANCE));
//        builder.and(Q.workOrder.status.notIn(Arrays.asList( WorkOrderStatus.CANCELLED, WorkOrderStatus.DECLINED)));
//
//        Set<EntityPathBase<?>> join = new HashSet<>();
//        join.add(Q.company);
//        join.add(Q.mold);
//        join.add(Q.machine);
//        QueryUtils.applyCompanyFilter(query, join, "COMMON",null);
//
//        query.where(builder);
//        query.groupBy(Q.company);
////        query.orderBy(overdueRate.desc());
//
//        Map<String, ComparableExpressionBase<?>> fieldMap = new MapBuilder<String, ComparableExpressionBase<?>>()//
////            .put("overdueCount", overdueCount)//
//            .build();
//
//        QueryUtils.applyPagination(query,PageRequest.of(0,1000));
//        QueryResults<PMComplianceOut.PMComplianceItem> results = query.fetchResults();
//        List<PMComplianceOut.PMComplianceItem> results1= results.getResults();
//        Collections.sort(results1,Comparator.comparingDouble(PMComplianceOut.PMComplianceItem::getComplianceRate));
//        return new PMComplianceOut(ListUtils.subList(results1, pageable.getPageNumber(), pageable.getPageSize()),pageable,results.getTotal());
//    }
//}
