package vn.com.twendie.avis.data.service.impl;

import org.springframework.stereotype.Service;
import vn.com.twendie.avis.data.model.TokenFirebase;
import vn.com.twendie.avis.data.repository.TokenFirebaseRepo;
import vn.com.twendie.avis.data.service.TokenFirebaseService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenFirebaseServiceImpl implements TokenFirebaseService {

    private final TokenFirebaseRepo tokenFirebaseRepo;

    public TokenFirebaseServiceImpl(TokenFirebaseRepo tokenFirebaseRepo) {
        this.tokenFirebaseRepo = tokenFirebaseRepo;
    }

    @Override
    public List<TokenFirebase> findAllByUserId(long userId) {
        return tokenFirebaseRepo.findAllByUserId((int) userId);
    }

    @Override
    public Set<String> findAllTokenByUSerId(long userId) {
        return findAllByUserId(userId)
                .stream()
                .map(TokenFirebase::getToken)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<String> findAllValidTokenByUSerId(long userId) {
        return findAllTokenByUSerId(userId).stream()
                .filter(this::checkValidUuid)
                .collect(Collectors.toSet());
    }

    @Override
    public void save(long userId, String token) {
        TokenFirebase tokenFirebase = TokenFirebase.builder()
                .userId((int) userId)
                .token(token)
                .build();
        tokenFirebaseRepo.save(tokenFirebase);
    }

    @Override
    public void saveIfNotExist(long userId, String token, boolean validate) {
        if (!validate || checkValidUuid(token)) {
            if (!exists(userId, token)) {
                save(userId, token);
            }
        }
    }

    @Override
    public boolean exists(long userId, String token) {
        return tokenFirebaseRepo.existsByUserIdAndToken((int) userId, token);
    }

    @Override
    public void deleteAllByToken(String token) {
        tokenFirebaseRepo.deleteAllByToken(token);
    }

    @Override
    public boolean checkValidUuid(String token) {
        try {
            UUID.fromString(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
