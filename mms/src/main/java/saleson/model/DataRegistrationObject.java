package saleson.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.ObjectType;
import saleson.model.support.DateAudit;

import javax.persistence.*;

@Getter
@Setter
@Entity
@AllArgsConstructor @NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DataRegistrationObject extends DateAudit {

	@Id
	@GeneratedValue
	private Long id;

	@Column(name = "DATA_REGISTRATION_ID", insertable = false, updatable = false)
	private Long dataRegistrationId;

	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "DATA_REGISTRATION_ID")
	private DataRegistration dataRegistration;

	@Enumerated(EnumType.STRING)
	private ObjectType objectType;
	private Long objectId;

}
