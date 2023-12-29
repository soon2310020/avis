package unused.resource.workorder;

//@Getter
//@Setter
//@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = true)
//public class WorkOrderStatusOut extends PageImpl<WorkOrderStatusOut.WorkOrderStatusItem> {
//	public WorkOrderStatusOut(List<WorkOrderStatusItem> content, Pageable pageable, long total) {
//		super(content, pageable, total);
//	}
//
//	@Data
//	@NoArgsConstructor
//	@Builder
//	@JsonInclude(JsonInclude.Include.NON_NULL)
//	@AllArgsConstructor
//	public static class WorkOrderStatusItem {
//		private Long supplierId;
//		private String supplierCode;
//		private String supplierName;
//		private double overdueRate;
//		private Long overdue;
//		private Long totalWorkOrder;
//
//		public WorkOrderStatusItem(Long supplierId, String supplierCode, String supplierName, Long overdue, Long totalWorkOrder) {
//			this.supplierId = supplierId;
//			this.supplierCode = supplierCode;
//			this.supplierName = supplierName;
//			this.overdue = overdue;
//			this.totalWorkOrder = totalWorkOrder;
//			if (totalWorkOrder != null && totalWorkOrder > 0 && overdue != null) {
//				this.overdueRate = Math.round(this.overdue.doubleValue() * 100 * 100 / this.totalWorkOrder) / 100d;
//			}
//		}
//
//	}
//}
