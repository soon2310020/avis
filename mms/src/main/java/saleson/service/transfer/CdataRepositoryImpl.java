package saleson.service.transfer;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import saleson.model.Cdata;

public class CdataRepositoryImpl extends QuerydslRepositorySupport implements CdataRepositoryCustom {

	public CdataRepositoryImpl() {
		super(Cdata.class);
	}

}
