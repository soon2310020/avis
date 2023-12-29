package vn.com.twendie.avis.tracking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import vn.com.twendie.avis.tracking.model.payload.Credential;

@Data
@Configuration
@ConfigurationProperties(prefix = "tracking.gps")
public class TrackingGpsConfig {

    private String baseUrl;

    private String customerCode;

    private String key;

    public Credential getCredential() {
        return new Credential(customerCode, key);
    }

}
