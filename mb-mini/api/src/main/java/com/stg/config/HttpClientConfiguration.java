package com.stg.config;

import com.stg.config.properties.ApacheComponentsProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class HttpClientConfiguration {

    private final ApacheComponentsProperties.HttpClientProperties httpClientProperties;
    private final ApacheComponentsProperties.ConnectionPoolingProperties connectionPoolingProperties;

    @Bean(name = "pool-ableHttpClient")
    public HttpClient getPoolableHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(
                connectionPoolingProperties.getTimeToLiveInSeconds(), TimeUnit.SECONDS);
        connectionManager.setMaxTotal(connectionPoolingProperties.getMaxConnection());
        connectionManager.setDefaultMaxPerRoute(connectionPoolingProperties.getMaxConnectionPerRoute());

        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(
                        RequestConfig.copy(RequestConfig.DEFAULT)
                                .setConnectTimeout(httpClientProperties.getConnectionTimeout())
                                .setConnectionRequestTimeout(httpClientProperties.getConnectionRequestTimeout())
                                .setSocketTimeout(httpClientProperties.getSocketTimeout())
                                .build()
                ).build();

        log.info("[event=http_client_created, httpClientProperties={}, connectionPoolProperties={}][Http client created]",
                httpClientProperties.toString(), connectionPoolingProperties);

        return httpClient;
    }
}
