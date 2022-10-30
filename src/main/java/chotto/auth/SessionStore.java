package chotto.auth;

import chotto.sequencer.SessionInfo;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SessionStore {

  private final AtomicReference<SessionInfo> currentSessionInfo = new AtomicReference<>();

  public Optional<SessionInfo> getSessionInfo() {
    return Optional.ofNullable(currentSessionInfo.get());
  }

  public void setSessionInfo(final SessionInfo sessionInfo) {
    currentSessionInfo.set(sessionInfo);
  }
}
