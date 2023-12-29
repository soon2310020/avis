package saleson.service.version;

import org.springframework.data.jpa.repository.JpaRepository;
import saleson.model.Version;

import java.util.List;

public interface VersionRepository extends JpaRepository<Version, Integer> {
	/**
	 * 터미널 ID로 버전 데이터 조회
	 * @param ti
	 * @return
	 */
	List<Version> findAllByTi(String ti);

}
