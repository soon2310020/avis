package com.stg.service.dto.quotation.mbal;

import com.stg.service3rd.mbal.dto.resp.ICDataResp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ICDataRespDto {

	private String code;

	private String fullName;

	private String phoneNumber;

	public static ICDataRespDto of(ICDataResp info) {
		
		if (info == null) {
            return new ICDataRespDto();
        }
		
		ICDataRespDto dto = new ICDataRespDto();
		dto.setCode(info.getCode());
		dto.setFullName(info.getName());
		dto.setPhoneNumber(info.getPhoneNumber());
		return dto;
	}
}
