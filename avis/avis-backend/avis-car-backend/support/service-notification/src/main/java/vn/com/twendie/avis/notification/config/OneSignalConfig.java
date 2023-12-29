package vn.com.twendie.avis.notification.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "notification.onesignal")
public class OneSignalConfig {

    private String url;

    private String appId;

    private String apiKey;

}
