package com.stg.config;

import java.security.KeyPair;
import java.util.Map;

public interface KeyPairProvider {
    Map<String, KeyPair> getKeyPairs();

    KeyPair getKeyPair(String alias);
}
