package saleson.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import javax.persistence.*;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Table(indexes = {
		@Index(name="IDX_AUTHORITY", columnList = "authority")
})
public class MoldAuthority {
	@Id
	@GeneratedValue
	private Long id;

	private Long moldId;

	private String authority;


	public MoldAuthority() {}

	public MoldAuthority(String authority) {
		//this.moldId = moldId;
		this.authority = authority;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMoldId() {
		return moldId;
	}

	public void setMoldId(Long moldId) {
		this.moldId = moldId;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}
}
