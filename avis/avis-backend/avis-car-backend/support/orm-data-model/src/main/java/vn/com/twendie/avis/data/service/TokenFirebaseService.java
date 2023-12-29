package vn.com.twendie.avis.data.service;

import vn.com.twendie.avis.data.model.TokenFirebase;

import java.util.List;
import java.util.Set;

public interface TokenFirebaseService {

    List<TokenFirebase> findAllByUserId(long userId);

    Set<String> findAllTokenByUSerId(long userId);

    Set<String> findAllValidTokenByUSerId(long userId);

    void save(long userId, String token);

    void saveIfNotExist(long userId, String token, boolean validate);

    boolean exists(long userId, String token);

    void deleteAllByToken(String token);

    boolean checkValidUuid(String token);

}