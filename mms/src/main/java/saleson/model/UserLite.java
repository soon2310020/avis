package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "USER")
@Data
public class UserLite {
	@Id
	private Long id;
	private String name;
	private String department;
	private String position;
	private String mobileNumber;
	private String email;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name="COMPANY_ID")
	private CompanyLite companyLite;

	public String getCompanyName() {
		CompanyLite companylite=getCompanyLite();
		return companylite == null ? null : companylite.getName();
	}
}
