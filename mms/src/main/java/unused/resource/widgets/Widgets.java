package unused.resource.widgets;

//public class Widgets {
//
//	public List<TolDistOut> getTolDist() {
//		List<TolDistOut> result = BeanUtils.get(DshRepository.class).findAllDistributions(new TolAdtGetIn());
//		result.forEach(t -> {
//			TolAdtGetIn summaryInput = new TolAdtGetIn();
//			summaryInput.setLocationId(t.getLocationId());
//			TolAdtGetOut.TolAdtStatusSummary summary = BeanUtils.get(TolAdtRepository.class).findAllStatusSummary(summaryInput);
//
//			t.setInProduction(summary.getInProduction());
//			t.setInactive(summary.getInactive());
//			t.setIdle(summary.getIdle());
//			t.setSensorOffline(summary.getSensorOffline());
//			t.setSensorDetached(summary.getSensorDetached());
//			t.setNoSensor(summary.getNoSensor());
//			t.setOnStandby(summary.getOnStandby());
//		});
//
//		return result;
//	}
//
//	public TolAdtGetOut.TolAdtUtilizationSummary getOvrUtl() {
//		return BeanUtils.get(TolAdtRepository.class).findAllUtilizationSummary(new TolAdtGetIn());
//	}
//
//	public DemCplTrkOut getDemCplTrk(Pageable pageable) {
//		Page<FltCompany> page = BeanUtils.get(FltService.class).getSuppliers(new FltIn(), pageable);
//		List<DemCplTrkOut.DemCplTrkItem> content = page.getContent().stream().map(fltCompany -> {
//			Page<DemCplTrkDetailOut.DemCplTrkDetailItem> bySupplier = BeanUtils.get(DemCplService.class).getSupplierParts(new DemCplPartsGetIn(), fltCompany.getId(), null);
//			long numHighCompliance = bySupplier.getContent().stream().filter(b -> PriorityType.HIGH.equals(b.getDemandCompliance())).count();
//			long numMediumCompliance = bySupplier.getContent().stream().filter(b -> PriorityType.MEDIUM.equals(b.getDemandCompliance())).count();
//			long numLowCompliance = bySupplier.getContent().stream().filter(b -> PriorityType.LOW.equals(b.getDemandCompliance())).count();
//			return new DemCplTrkOut.DemCplTrkItem(fltCompany.getId(), fltCompany.getCode(), fltCompany.getName(), numHighCompliance, numMediumCompliance, numLowCompliance);
//		}).collect(Collectors.toList());
//		return new DemCplTrkOut(content, pageable, page.getTotalElements());
//	}
//
//	public DemCplTrkDetailOut getDemCplTrkDetail(Long supplierId, Pageable pageable) {
//		FltSupplier supplier = FltUtils.getSupplierById(supplierId);
//		Page<DemCplTrkDetailOut.DemCplTrkDetailItem> page = BeanUtils.get(DemCplService.class).getSupplierParts(new DemCplPartsGetIn(), supplierId, pageable);
//		return new DemCplTrkDetailOut(page.getContent(), supplier);
//	}
//
//}
