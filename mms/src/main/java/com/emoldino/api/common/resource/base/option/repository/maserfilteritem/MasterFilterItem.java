package com.emoldino.api.common.resource.base.option.repository.maserfilteritem;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.emoldino.api.common.resource.base.option.enumeration.MasterFilterResourceType;

import lombok.Data;
import lombok.NoArgsConstructor;
import saleson.common.hibernate.converter.BooleanYnConverter;

@Data
@NoArgsConstructor
@Entity
@Table(indexes = @Index(name = "UX_MASTER_FILTER_ITEM", columnList = "filterCode,userId,resourceType,resourceId", unique = true))
public class MasterFilterItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String filterCode;
	private Long userId;
	@Enumerated(EnumType.STRING)
	private MasterFilterResourceType resourceType;
	private Long resourceId;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean selected;
	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean temporal;
}
