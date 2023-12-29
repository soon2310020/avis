package vn.com.twendie.avis.api.core.config;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@ComponentScan({ "vn.com.twendie.avis.api.core" })
@Configuration
public class ModuleConfiguration {

    @Value("${okhttp.timeout.connection:30}")
    private long httpClientConnectionTimeout;

    @Value("${okhttp.timeout.read:30}")
    private long httpClientReadTimeout;

    @Value("${okhttp.timeout.write:30}")
    private long httpClientWriteTimeout;

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(httpClientConnectionTimeout, TimeUnit.SECONDS)
                .readTimeout(httpClientReadTimeout, TimeUnit.SECONDS)
                .writeTimeout(httpClientWriteTimeout, TimeUnit.SECONDS)
                .build();
    }

}
