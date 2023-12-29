package com.stg.service.dto.potentialcustomer;

import com.stg.constant.lead.MbalType;
import com.stg.constant.lead.MicType;
import com.stg.constant.lead.SupMbalType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CaredProductDto {
	private List<MbalType> mbal;
	private List<SupMbalType> supMbal;
	private List<MicType> mic;
}
