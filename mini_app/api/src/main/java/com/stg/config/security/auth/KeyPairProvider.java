package com.stg.config.security.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@Slf4j
public class KeyPairProvider {

    private final KeyPair keyPair;

    public KeyPairProvider(@Value("${spring.oauth.keyStorePath}") String keyStorePath,
                           @Value("${spring.oauth.keyStoreAlias}") String keyStoreAlias,
                           @Value("${spring.oauth.keyStorePassword}") String keyStorePassword) {
        try {
            File file = ResourceUtils.getFile(keyStorePath);
            Resource cpr = new FileUrlResource(file.getPath());
            keyPair = new KeyStoreKeyFactory(cpr, keyStorePassword.toCharArray()).getKeyPair(keyStoreAlias);
        } catch (IOException e) {
            log.error("[Tool]--Invalid keyPair {}", e.getMessage());
            throw new IllegalArgumentException("Invalid keystore", e);
        }
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
