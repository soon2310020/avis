package saleson.model;

import java.time.Instant;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import saleson.common.enumeration.EndOfLifeCycleStatus;
import saleson.common.enumeration.PriorityType;
import saleson.model.support.EndLifeCycleBase;

@Getter
@Setter
@NoArgsConstructor
@Entity
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class MoldEndLifeCycle extends EndLifeCycleBase {
	@Id
	@GeneratedValue
	private Long id;

	public MoldEndLifeCycle(Mold mold, Instant issueDate, PriorityType priority, EndOfLifeCycleStatus status) {
		super(mold, issueDate, priority, status);
	}
}
