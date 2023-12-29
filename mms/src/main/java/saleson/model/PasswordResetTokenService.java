package saleson.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetTokenService {

  @Autowired
  private PasswordResetTokenRepository repository;

  public PasswordResetToken findByToken(String token) {
    return repository.findByToken(token).orElse(null);
  }

  public PasswordResetToken save(PasswordResetToken token) {
    return repository.save(token);
  }

  public void delete(PasswordResetToken token) {
    repository.delete(token);
  }

}
