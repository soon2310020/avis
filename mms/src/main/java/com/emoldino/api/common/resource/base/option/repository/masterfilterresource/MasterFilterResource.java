package com.emoldino.api.common.resource.base.option.repository.masterfilterresource;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterMode;
import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(indexes = @Index(name = "UX_MASTER_FILTER_RESOURCE", columnList = "filterCode,userId,resourceType", unique = true))
@DynamicUpdate
public class MasterFilterResource {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String filterCode;
	private Long userId;
	@Enumerated(EnumType.STRING)
	private MasterFilterResourceType resourceType;

	private Integer position;
	@Enumerated(EnumType.STRING)
	private MasterFilterMode mode;
}
