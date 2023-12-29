package com.stg.service.dto.combo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.stg.constant.ComboCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteComboRequest {

	private String type;
	private List<Long> ids;
}
