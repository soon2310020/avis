package saleson.common.notification;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import saleson.common.enumeration.AlertType;

import java.util.Arrays;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MailServiceTest {
	@Autowired
	MailService mailService;


	@Test
	public void sendMail() {
		Message message = AlertMailMessage.builder()
				.receivers(Arrays.asList("skc@onlinepowers.com", "tlsrmrckd@naver.com"))
				.alertType(AlertType.MISCONFIGURE)
				.toolingId("MOLD_34234")
				.build();

		mailService.send(message);
	}

	@Test
	public void sendMailBatch() {
		mailService.sendMail();
	}
}