package saleson.api.part;

import saleson.model.data.StatisticsPartData;

import java.util.List;

public interface PartProjectProducedRepositoryCustom {

	Long getProjectProduced(Long projectId);

	List<StatisticsPartData> getPartProducedByProjectId(Long projectId, List<Long> partIds);

}
