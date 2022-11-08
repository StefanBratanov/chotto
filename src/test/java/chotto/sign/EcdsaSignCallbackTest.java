package chotto.sign;

import static chotto.Constants.ECDSA_SIGN_CALLBACK_PATH;
import static org.assertj.core.api.Assertions.assertThat;

import chotto.Store;
import io.javalin.http.HandlerType;
import io.javalin.testtools.JavalinTest;
import java.util.Objects;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class EcdsaSignCallbackTest {

  private final Store store = new Store();

  private final EcdsaSignCallback ecdsaSignCallback = new EcdsaSignCallback(store);

  @Test
  public void testCallback() {

    final String signature =
        "0x1949e68bfab53a3f921ace3c83d562e36fa5fe82d6f603394e58627a2fa4a31553aca183c6adbb1dad2ac032358b863d2c2137fe2b046e822041037fb97758251c";

    JavalinTest.test(
        ((server, client) -> {
          server.addHandler(HandlerType.GET, ECDSA_SIGN_CALLBACK_PATH, ecdsaSignCallback);

          final Response response =
              client.get(ECDSA_SIGN_CALLBACK_PATH + "?signature=" + signature);

          assertThat(response.code()).isEqualTo(200);
          assertThat(Objects.requireNonNull(response.body()).string())
              .isEqualTo(
                  "Thank you for your signature. You can return to the Chotto logs to witness the remainder of the ceremony.");

          assertThat(store.getEcdsaSignature()).hasValue(signature);
        }));
  }
}
