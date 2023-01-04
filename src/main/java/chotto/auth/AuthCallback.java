package chotto.auth;

import chotto.Store;
import chotto.cli.AsciiArtHelper;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthCallback implements Handler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthCallback.class);

  private final Store store;

  public AuthCallback(final Store store) {
    this.store = store;
  }

  @Override
  public void handle(final Context ctx) {
    final HttpServletRequest request = ctx.req();
    final String error = request.getParameter("error");
    if (error != null) {
      final String code = request.getParameter("code");
      final String exceptionMessage =
          String.format(
              "Error while logging in (code: %s, error: %s). You can restart Chotto to try to contribute again.",
              code, error);
      LOG.error(exceptionMessage);
      ctx.status(500);
      ctx.result(exceptionMessage);
      return;
    }
    final String sessionId =
        Objects.requireNonNull(request.getParameter("session_id"), "session_id must not be null");
    final String nickname =
        Objects.requireNonNull(request.getParameter("nickname"), "nickname must not be null");
    final Provider provider = Provider.fromProviderName(request.getParameter("provider"));
    final SessionInfo sessionInfo = new SessionInfo(provider, nickname, sessionId);
    store.setSessionInfo(sessionInfo);
    LOG.info("Successfully logged in with {} ({})", provider, nickname);
    ctx.html(
        "<html><pre>"
            + AsciiArtHelper.getBanner()
            + "</pre><div>"
            + String.format(
                "Successfully logged in with %s. You can go back to the Chotto logs to witness your ceremony contribution.",
                provider)
            + "</div></html>");
  }
}
