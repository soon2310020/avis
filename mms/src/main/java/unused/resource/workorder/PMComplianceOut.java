package unused.resource.workorder;

//@Getter
//@Setter
//@ToString(callSuper = true)
//@EqualsAndHashCode(callSuper = true)
//public class PMComplianceOut extends PageImpl<PMComplianceOut.PMComplianceItem> {
//    public PMComplianceOut(List<PMComplianceItem> content, Pageable pageable, long total) {
//        super(content, pageable, total);
//    }
//
//    @Data
//    @NoArgsConstructor
//    @Builder
//    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @AllArgsConstructor
//    public static class PMComplianceItem {
//        private Long supplierId;
//        private String supplierCode;
//        private String supplierName;
//        private Double complianceRate;
//        private PriorityType complianceRateLevel;
//        private Long completed;
//        private Long totalWorkOrder;
//
//        public PMComplianceItem(Long completed, Long totalWorkOrder) {
//            this.completed = completed;
//            this.totalWorkOrder = totalWorkOrder;
//        }
//
//        public PMComplianceItem(Long supplierId, String supplierCode, String supplierName
//            , Long completed, Long totalWorkOrder) {
//            this.supplierId = supplierId;
//            this.supplierCode = supplierCode;
//            this.supplierName = supplierName;
//            this.completed = completed;
//            this.totalWorkOrder = totalWorkOrder;
//            if (totalWorkOrder != null && completed != null && totalWorkOrder > 0) {
//                this.complianceRate = Math.round(this.completed.doubleValue() * 100 * 100 / this.totalWorkOrder) / 100d;
//            }
//
//            if (complianceRate != null) {
//                if (complianceRate < 40) {
//                    this.complianceRateLevel = PriorityType.LOW;
//                } else if (complianceRate < 80) {
//                    this.complianceRateLevel = PriorityType.MEDIUM;
//                } else {
//                    this.complianceRateLevel = PriorityType.HIGH;
//                }
//            }
//        }
//    }
//}
