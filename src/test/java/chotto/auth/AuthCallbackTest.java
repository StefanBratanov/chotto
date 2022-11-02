package chotto.auth;

import static chotto.Constants.AUTH_CALLBACK_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import io.javalin.http.HandlerType;
import io.javalin.testtools.JavalinTest;
import java.util.Objects;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class AuthCallbackTest {

  private final SessionStore sessionStore = new SessionStore();

  private final AuthCallback authCallback = new AuthCallback(sessionStore);

  @Test
  public void testCallback() {

    JavalinTest.test(
        ((server, client) -> {
          server.addHandler(HandlerType.GET, AUTH_CALLBACK_PATH, authCallback);

          final Response response =
              client.get(
                  AUTH_CALLBACK_PATH
                      + "?session_id=a6d8bd3b-3154-4d29-bdd7-d28669b0a4a5&sub=eth+%7C+0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad&nickname=0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad&provider=Ethereum&exp=18446744073709551645");

          assertThat(response.code()).isEqualTo(200);
          assertThat(Objects.requireNonNull(response.body()).string())
              .isEqualTo(
                  "Successfully logged in with Ethereum. You can go back to the Chotto logs to witness your ceremony contribution.");

          assertThat(sessionStore.getSessionInfo())
              .hasValueSatisfying(
                  sessionInfo -> {
                    assertThat(sessionInfo.getProvider()).isEqualTo(Provider.ETHEREUM);
                    assertThat(sessionInfo.getNickname())
                        .isEqualTo("0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad");
                    assertThat(sessionInfo.getSessionId())
                        .isEqualTo("a6d8bd3b-3154-4d29-bdd7-d28669b0a4a5");
                  });
        }));
  }
}
