package saleson.service.transfer;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import saleson.model.LogTransfer;

import java.util.List;

@Slf4j
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransferControllerTest {
	@Autowired
	LogTransferRepository logTransferRepository;

	@Test
	@DisplayName("LogTransfer 데이터로 CDATA -> STATISTICS 등록")
	void createCdata() {
		String url = "http://dev.emoldino.com/mms/transfer/data";

		List<LogTransfer> transfers = logTransferRepository.findAllByCiOrderById("NCM2015A01102");	// NCM2015A01101,

		for (int i = 0; i < transfers.size(); i++) {


			MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
			params.add("q", transfers.get(i).getEs());

			RestTemplate restTemplate = new RestTemplateBuilder().build();
			String response = restTemplate.postForObject(url, params, String.class);
			log.debug("TRANS {} : {}", i, response);
		}

	}

}