package chotto.sign;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import chotto.Constants;
import chotto.Store;
import chotto.objects.BatchContribution;
import chotto.template.TemplateResolver;
import io.javalin.Javalin;
import io.javalin.testtools.JavalinTest;
import java.util.Objects;
import java.util.Optional;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

class EcdsaSignerTest {

  private final Javalin app = Javalin.create();

  private final TemplateResolver templateResolver = mock(TemplateResolver.class);

  private final Store store = mock(Store.class);

  private final EcdsaSigner ecdsaSigner =
      new EcdsaSigner(app, templateResolver, "https://ethfoo.bar", true, store);

  @Test
  public void testEcdsaSigning() {
    final String ethAddress = "0xC4b1c53aB4a4636e4DF2283B04e71aa022B7Aae3";
    final BatchContribution batchContribution = mock(BatchContribution.class);

    final String expectedSignature =
        "0x1949e68bfab53a3f921ace3c83d562e36fa5fe82d6f603394e58627a2fa4a31553aca183c6adbb1dad2ac032358b863d2c2137fe2b046e822041037fb97758251c";

    when(templateResolver.createTypedData(batchContribution)).thenReturn("{}");
    when(templateResolver.createSignContributionHtml(
            ethAddress, "{}", Constants.ECDSA_SIGN_CALLBACK_PATH))
        .thenReturn("<html></html>");
    when(store.getEcdsaSignature()).thenReturn(Optional.of(expectedSignature));

    final String signature = ecdsaSigner.sign(ethAddress, batchContribution);

    assertThat(signature).isEqualTo(expectedSignature);

    // test the new sign endpoint is set up
    JavalinTest.test(
        app,
        (server, client) -> {
          final Response response = client.get(EcdsaSigner.SIGN_PATH);
          assertThat(response.code()).isEqualTo(200);
          assertThat(response.headers().get("Content-Type")).isEqualTo("text/html");
          assertThat(Objects.requireNonNull(response.body()).string()).isEqualTo("<html></html>");
        });
  }
}
