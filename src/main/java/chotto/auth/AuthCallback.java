package chotto.auth;

import chotto.sequencer.SessionInfo;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthCallback implements Handler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthCallback.class);

  private final SessionStore sessionStore;

  public AuthCallback(final SessionStore sessionStore) {
    this.sessionStore = sessionStore;
  }

  @Override
  public void handle(final Context ctx) {
    final HttpServletRequest request = ctx.req();
    final String sessionId = request.getParameter("session_id");
    final String nickname = request.getParameter("nickname");
    final Provider provider = Provider.fromProviderName(request.getParameter("provider"));
    final SessionInfo sessionInfo = new SessionInfo(provider, nickname, sessionId);
    sessionStore.setSessionInfo(sessionInfo);
    LOG.info("Successfully logged in with {} ({})", provider, nickname);
    ctx.result(
        String.format(
            "Successfully logged in with %s. You can go back to the Chotto logs to witness your ceremony contribution.",
            provider));
  }
}
