package com.stg.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "apache-components")
public class ApacheComponentsProperties {
    private HttpClientProperties httpClientProperties;
    private ConnectionPoolingProperties connectionPoolingProperties;

    @Data
    @Component
    @ConfigurationProperties(prefix = "apache-components.http-client")
    public static class HttpClientProperties {
        private int socketTimeout;
        private int connectionRequestTimeout;
        private int connectionTimeout;
    }

    @Data
    @Component
    @ConfigurationProperties(prefix = "apache-components.connection-pool")
    public static class ConnectionPoolingProperties {
        private int maxConnectionPerRoute;
        private int maxConnection;
        private int timeToLiveInSeconds;
    }
}
