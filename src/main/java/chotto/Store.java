package chotto;

import chotto.auth.SessionInfo;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class Store {

  private final AtomicReference<String> authError = new AtomicReference<>();
  private final AtomicReference<SessionInfo> sessionInfo = new AtomicReference<>();
  private final AtomicReference<String> ecdsaSignature = new AtomicReference<>();

  public Optional<String> getAuthError() {
    return Optional.ofNullable(authError.get());
  }

  public void setAuthError(final String authError) {
    this.authError.set(authError);
  }

  public Optional<SessionInfo> getSessionInfo() {
    return Optional.ofNullable(sessionInfo.get());
  }

  public void setSessionInfo(final SessionInfo sessionInfo) {
    this.sessionInfo.set(sessionInfo);
  }

  public Optional<String> getEcdsaSignature() {
    return Optional.ofNullable(ecdsaSignature.get());
  }

  public void setEcdsaSignature(final String ecdsaSignature) {
    this.ecdsaSignature.set(ecdsaSignature);
  }
}
