package com.stg.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DefaultKeyPairProvider implements KeyPairProvider {

    private final KeyStore keyStore;
    private final Map<String, KeyPair> keyPairs;

    public DefaultKeyPairProvider(@Value("${spring.oauth.keyStorePath}") String keyStorePath,
                                  @Value("${spring.oauth.keyStorePassword}") String keyStorePassword) {

        try {
            InputStream inputStream = new FileInputStream(keyStorePath);
            this.keyStore = KeyStore.getInstance("JKS");
            keyStore.load(inputStream, keyStorePassword.toCharArray());
            this.keyPairs = new HashMap<>();
            File file = ResourceUtils.getFile(keyStorePath);
            Resource cpr = new FileUrlResource(file.getPath());
            Enumeration<String> enumeration = keyStore.aliases();

            while (enumeration.hasMoreElements()) {
                String alias = enumeration.nextElement();
                keyPairs.put(alias, new KeyStoreKeyFactory(cpr, keyStorePassword.toCharArray()).getKeyPair(alias));
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            log.error("[MINI]--Invalid keystore {}", e.getMessage());
            throw new IllegalArgumentException("Invalid keystore", e);
        }
    }

    @Override
    public Map<String, KeyPair> getKeyPairs() {
        return keyPairs;
    }

    @Override
    public KeyPair getKeyPair(String alias) {
        return keyPairs.get(alias);
    }
}
