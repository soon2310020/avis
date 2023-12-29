package saleson.service.transfer;

import org.springframework.stereotype.Component;

import saleson.model.Adata;
import saleson.model.Cdata;
import saleson.model.Tdata;

@Component
public class VersionChecker {
	public static final String TMS = "TMS"; // R1버전 터미널 prefix
	public static final String CMS = "CMS"; // R1버전 카운터 prefix
	public static final String VERSION_R1 = "R1";
	public static final String VERSION_R2 = "R2";

	public String determine(Cdata cdata) {
		return determineFromCi(cdata.getCi());
	}

	public String determine(Adata adata) {
		return determineFromCi(adata.getCi());
	}

	public String determine(Tdata tdata) {
		return determineFromTi(tdata.getTi());
	}

	public String determineFromCi(String ci) {
		return ci.toUpperCase().startsWith(CMS) ? VERSION_R1 : VERSION_R2;
	}

	public String determineFromTi(String ti) {
		return ti.toUpperCase().startsWith(TMS) ? VERSION_R1 : VERSION_R2;
	}
}
