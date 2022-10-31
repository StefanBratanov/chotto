package chotto.sequencer;

import chotto.auth.Provider;
import java.util.Objects;

public class SessionInfo {

  private final Provider provider;
  private final String nickname;
  private final String sessionId;

  public SessionInfo(final Provider provider, final String nickname, final String sessionId) {
    this.provider = provider;
    this.nickname = nickname;
    this.sessionId = sessionId;
  }

  public Provider getProvider() {
    return provider;
  }

  public String getNickname() {
    return nickname;
  }

  public String getSessionId() {
    return sessionId;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final SessionInfo that = (SessionInfo) o;
    return Objects.equals(provider, that.provider)
        && Objects.equals(nickname, that.nickname)
        && Objects.equals(sessionId, that.sessionId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(provider, nickname, sessionId);
  }
}
