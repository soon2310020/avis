package saleson.service.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import saleson.model.Transfer;
import saleson.model.Version;
import saleson.service.transfer.TransferRepository;

import java.util.List;

@Service
public class VersionService {

	@Value("${app.mms.firmware.upurl}")
	private String FIRMWARE_DEFAULT_UPURL;

	@Autowired
	VersionRepository versionRepository;

	@Autowired
	TransferRepository transferRepository;

	/**
	 * 터미널 ID로 uprul을 조회한다.
	 * @param transfer
	 * @return
	 */
	public String getUpurlBy(Transfer transfer) {
		// 1. 전송 데이터 저장
		transferRepository.save(transfer);

		// 2. upurl 조회
		List<Version> versions = versionRepository.findAllByTi(transfer.getTi());

		// 3.
		if (versions == null || versions.size() == 0) {
			return FIRMWARE_DEFAULT_UPURL;
		}

		return versions.get(0).getUpurl();
	}
}
