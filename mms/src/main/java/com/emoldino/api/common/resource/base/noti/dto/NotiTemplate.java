package com.emoldino.api.common.resource.base.noti.dto;

import com.emoldino.api.common.resource.base.noti.enumeration.NotiLinkType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
//@Entity
//@EntityListeners(AuditingEntityListener.class)
//@Table(indexes = @Index(name = "UX_NOTI_TEMPLATE", columnList = "notiCode,language", unique = true))
//@DynamicUpdate
public class NotiTemplate {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;

//	private NotiCode notiCode;
//	private Language language;

	private boolean contentByUser;

	private String title;
	private String content;

	private String emailTitle;
	private String emailSubtype;
	private String emailContent;

//	@CreatedBy
//	@Column(updatable = false)
//	private Long createdBy;
//	@CreatedDate
//	@Column(updatable = false)
//	private Instant createdAt;
//	@LastModifiedBy
//	private Long updatedBy;
//	@LastModifiedDate
//	private Instant updatedAt;
}
