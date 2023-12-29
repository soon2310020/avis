package com.stg.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.stg.entity.PavTable;
import com.stg.errors.ApplicationException;
import com.stg.service.dto.insurance.PavTableDto;
import org.apache.http.client.HttpClient;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class AppConfiguration {

    private static final String RSA_INSTANCE = "RSA";

    @Value("${external.baas-key.private_key_signature_path}")
    private String signaturePath;

    @Value("${external.mbal-key.client_id_ul3}")
    private String clientIdUl3;

    @Value("${external.mbal-key.client_secret_ul3}")
    private String clientSecretUl3;

    @Autowired
    public HttpClient httpClient;

    @Bean
    @DependsOn({"pool-ableHttpClient"})
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        return new RestTemplate(requestFactory);
    }

    @Bean
    public ObjectMapper mapper() {
        return new ObjectMapper()
                .configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
                .configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true);
    }

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(PavTable.class, PavTableDto.class);
        modelMapper.getConfiguration().setAmbiguityIgnored(true); // skip field unknown
        return modelMapper;
    }

    @Bean
    public PrivateKey getPrivateKey() {
        try (InputStream ioStream = new FileInputStream(signaturePath)) {
            byte[] bytes = ioStream.readAllBytes();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
            KeyFactory kf = KeyFactory.getInstance(RSA_INSTANCE);
            return kf.generatePrivate(spec);
        } catch (Exception e) {
            throw new ApplicationException("Error when get private key");
        }
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public HttpHeaders genMbalBasicAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientIdUl3, clientSecretUl3);
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
