package com.emoldino.api.common.resource.base.accesscontrol.repository.userlite;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.emoldino.framework.util.ValueUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USER")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLite {
	@Id
	private Long id;
	private String name;
	private String loginId;

	@Transient
	public String getLabel() {
		return ValueUtils.toLabel(true, name, loginId);
	}
}
