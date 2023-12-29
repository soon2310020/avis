package com.emoldino.api.supplychain.resource.base.product.repository.partorder;

//@Entity
////@DynamicUpdate
//@Data
//@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
//@NoArgsConstructor
//public class PartOrder {
//	public PartOrder(Long productId, Long partId, Long supplierId, String periodType, String periodValue, Long quantity) {
//		this.productId = productId;
//		this.partId = partId;
//		this.supplierId = supplierId;
//		this.periodType = periodType;
//		this.periodValue = periodValue;
//		this.quantity = quantity;
//	}
//
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//
//	private Long productId;
//	@JsonIgnore
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "productId", insertable = false, updatable = false)
//	private Category product;
//
//	private Long partId;
//	@JsonIgnore
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "partId", insertable = false, updatable = false)
//	private Part part;
//
//	private Long supplierId;
//	@JsonIgnore
//	@ManyToOne(fetch = FetchType.LAZY)
//	@JoinColumn(name = "supplierId", insertable = false, updatable = false)
//	private Company supplier;
//
//	private String periodType;
//	private String periodValue;
//	private Long quantity;
//
//	@CreatedBy
//	@JsonIgnore
//	private Long createdBy;
//	@CreatedDate
//	private Instant createdAt;
//	@LastModifiedBy
//	@JsonIgnore
//	private Long updatedBy;
//	@LastModifiedDate
//	private Instant updatedAt;
//}
