package saleson.model.integration;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

public class InfMessageRepositoryImpl extends QuerydslRepositorySupport {
	public InfMessageRepositoryImpl() {
		super(InfMessage.class);
	}
}
