package com.stg.service.dto.combo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpStatus;

import com.stg.constant.ComboCode;
import com.stg.constant.Gender;
import com.stg.errors.ApplicationException;
import com.stg.errors.dto.ErrorDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ComboSuggestionDto {

//	@NotNull
//	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
//	private LocalDate dob;

	private String dob;

//	@NotBlank
	private Gender gender;

//	@NotBlank
	private BigDecimal inputAmount;

//	@NotBlank
	private ComboCode comboCode;

	private int age;

	public void validate() {
		if(inputAmount.compareTo(BigDecimal.valueOf(10000000)) < 0 || inputAmount.remainder(BigDecimal.valueOf(1000000)).intValue() != 0){
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Số tiền bảo hiểm >= 10 triệu VNĐ và phải là bội số của 1 triệu VNĐ"));
		}

		if(StringUtils.isBlank(dob)){
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Ngày sinh bắt buộc nhập"));
		}

		LocalDate curDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate localDateDob = LocalDate.parse(dob, formatter);

		if(localDateDob.plusDays(15).isAfter(curDate)){
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Tuổi của người mua bảo hiểm phải đủ từ 15 ngày tuổi"));
		}

		age = curDate.getYear() - localDateDob.getYear();
		if(localDateDob.getDayOfYear() > curDate.getDayOfYear()){
			age = age - 1;
		}
		if(this.getAge() > 65){
			throw new ApplicationException("", new ErrorDto(HttpStatus.BAD_REQUEST, "Độ tuổi tham gia bảo hiểm phải từ 15 ngày tuổi đến 65 tuổi."));
		}
	}

	public int getAge() {
		return age;
	}
}
