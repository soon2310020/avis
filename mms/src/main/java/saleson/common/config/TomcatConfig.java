package saleson.common.config;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.AbstractProtocol;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

	//HTTP port
	@Value("${http.port}")
	private int httpPort;

	@Bean
	public ConfigurableServletWebServerFactory servletContainer() {

		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
			setSendReasonParse(connector);
		});

		// Let's configure additional connector to enable support for both HTTP and HTTPS
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		return tomcat;
	}

	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(httpPort);

		setSendReasonParse(connector);
		return connector;
	}

	private void setSendReasonParse(Connector connector) {
		ProtocolHandler protocolHandler = connector.getProtocolHandler();
		if (protocolHandler != null && protocolHandler instanceof AbstractHttp11Protocol) {
			AbstractProtocol protocol = (AbstractProtocol) protocolHandler;
			// skc 2018-08-13 톰캣 8.5에서 status 코드의 사유를 보내주지 않는 부분 수정
			// Status Code: 200  ==> Status Code: 200 OK
			protocol.setSendReasonPhrase(true);
		}
	}
}
