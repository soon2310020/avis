package vn.com.twendie.avis.mobile.api.config;


import lombok.Data;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.util.Pool;
import org.springframework.integration.util.SimplePool;
import vn.com.twendie.avis.mobile.api.pool.FTPClientPoolItemCallBack;

@Data
@Configuration
@ConfigurationProperties(prefix = "ftp.client")
public class FtpConfig {

    private String host;

    private int port;

    private String username;

    private String password;

    private int poolSize;

    @Bean
    public Pool<FTPClient> FTPClientPool() {
        return new SimplePool<>(poolSize, new FTPClientPoolItemCallBack(host, port, username, password));
    }

}
