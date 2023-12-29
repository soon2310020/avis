package unused.resource.demcpltrk;

//public class DemCplTrkBak {
//
//	public Page<DemCplTrkDetailOut.DemCplTrkDetailItem> getSupplierParts(DemCplPartsGetIn input, Long supplierId, Pageable pageable) {
//		LogicUtils.assertNotEmpty(supplierId, "supplierId");
//		List<Long> supplierIds = Arrays.asList(supplierId);
//		pageable = pageable == null ? PageRequest.of(0, 1000) : pageable;
//		Page<FltPart> page = BeanUtils.get(FltService.class).getPartsWithSupplierIds(ValueUtils.map(input, FltIn.class), supplierIds, pageable);
//
//		String week = !ObjectUtils.isEmpty(input.getTimeValue()) ? input.getTimeValue() : DateUtils2.format(DateUtils2.getInstant(), DatePattern.YYYYww, Zone.GMT);
//		List<DemCplTrkDetailOut.DemCplTrkDetailItem> list = page.getContent().stream()//
//				.map(part -> {
//					long demand = ValueUtils.toLong(//
//							BeanUtils.get(JPAQueryFactory.class)//
//									.select(Q.partPlan.quantity.sum())//
//									.from(Q.partPlan)//
//									.where(new BooleanBuilder()//
//											.and(Q.partPlan.partId.eq(part.getId()))//
//											.and(Q.partPlan.periodType.eq("WEEKLY"))//
//											.and(Q.partPlan.periodValue.eq(week)).and(Q.partPlan.supplierId.in(supplierIds)))//
//									.fetchOne(), //
//							0L);
//					long produced;
//					{
//						long[] _produced = { 0L };
//						BeanUtils.get(PartStatRepository.class).findAllWeeklyByProduct(week, part.getCategoryId(), part.getId(), supplierIds, null)//
//								.forEach(stat -> {
//									_produced[0] += stat.getProducedVal();
//								});
//						produced = _produced[0];
//					}
//					long capacity = ProductUtils.getWeeklyCapa(week, part.getCategoryId(), part.getId(), supplierIds, produced);
//					long predicted = ProductUtils.toPredicted(week, produced, capacity);
//					PriorityType demandCompliance = ProductUtils.toDemandCompliance(demand, produced, predicted, capacity);
////				double demandComplianceRate = ProductUtils.toDemandComplianceRate(demand, produced, predicted, capacity);
//					return new DemCplTrkDetailOut.DemCplTrkDetailItem(part.getId(), part.getName(), part.getPartCode(), demand, produced, demandCompliance);
//				})//
//				.collect(Collectors.toList());
//
//		return new PageImpl<>(list, pageable, page.getTotalElements());
//	}
//
//}
