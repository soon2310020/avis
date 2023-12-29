package saleson.model.data;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.Setter;
import saleson.common.enumeration.mapper.CodeMapperType;

@Getter @Setter
public class CompanyChartData implements Comparable<CompanyChartData> {
	private String title;
	private Long data = 0L;


	@QueryProjection
	public CompanyChartData(String title, Long data){
		this.title = title;
		this.data = data;
	}


	@QueryProjection
	public CompanyChartData(CodeMapperType codeMapperType, Long data){
		this.title = codeMapperType.getTitle();
		this.data = data;
	}

	@Override
	public int compareTo(CompanyChartData o) {
		return Integer.compare(Integer.parseInt(title), Integer.parseInt(o.getTitle()));

	}

}
