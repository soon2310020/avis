package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.*;
import saleson.api.user.support.UserAlertSerializer;
import saleson.common.enumeration.AlertType;
import saleson.common.enumeration.PeriodType;
import saleson.common.enumeration.SpecialAlertType;
import saleson.common.hibernate.converter.BooleanYnConverter;
import saleson.model.support.UserDateAudit;

import javax.persistence.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder @EqualsAndHashCode(of = "id", callSuper=false)
@Table(indexes = {
		@Index(name = "UK_USER_ALERT", columnList = "USER_ID, alertType", unique = true)
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "createdAt", "updatedAt"})
public class UserAlert extends UserDateAudit {

	@Id
	@GeneratedValue
	private Long id;

	@Enumerated(EnumType.STRING)
	private AlertType alertType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID")
	@JsonSerialize(using = UserAlertSerializer.class)
	private User user;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean email;

	@Enumerated(EnumType.STRING)
	private PeriodType periodType;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean sms;

	@Column(length = 1)
	@Convert(converter = BooleanYnConverter.class)
	private Boolean alertOn;

	@Enumerated(EnumType.STRING)
	private SpecialAlertType specialAlertType;

	public UserAlert(AlertType alertType, User user, Boolean email, PeriodType periodType){
		this.alertType = alertType;
		this.user = user;
		this.email = email;
		this.periodType = periodType;
	}

	public UserAlert(Boolean alertOn, SpecialAlertType specialAlertType,AlertType alertType, User user, Boolean email, PeriodType periodType){
		this.alertOn = alertOn;
		this.specialAlertType = specialAlertType;
		this.alertType = alertType;
		this.user = user;
		this.email = email;
		this.periodType = periodType;
	}

	public UserAlert(AlertType alertType, Boolean email){
		this.alertType = alertType;
		this.email = email;
	}
}
