package com.stg.config.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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
                httpClientProperties, connectionPoolingProperties);

        return httpClient;
    }


    @Bean
    @DependsOn({"pool-ableHttpClient"})
    public RestTemplate restTemplate(@Autowired HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);
    }
}
