package chotto.sign;

import chotto.Constants;
import chotto.Store;
import chotto.cli.CliInstructor;
import chotto.objects.BatchContribution;
import chotto.template.TemplateResolver;
import com.pivovarit.function.ThrowingRunnable;
import io.javalin.Javalin;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcdsaSigner {

  private static final Logger LOG = LoggerFactory.getLogger(EcdsaSigner.class);

  static final String SIGN_PATH = "/sign/ecdsa";

  private final Javalin app;
  private final TemplateResolver templateResolver;
  private final String host;
  private final boolean callbackEndpointIsDefined;
  private final Store store;

  public EcdsaSigner(
      final Javalin app,
      final TemplateResolver templateResolver,
      final String host,
      final boolean callbackEndpointIsDefined,
      final Store store) {
    this.app = app;
    this.templateResolver = templateResolver;
    this.host = host;
    this.callbackEndpointIsDefined = callbackEndpointIsDefined;
    this.store = store;
  }

  public String sign(final String ethAddress, final BatchContribution batchContribution) {

    final String typedData = templateResolver.createTypedData(batchContribution);

    final String signContributionHtml =
        templateResolver.createSignContributionHtml(
            ethAddress, typedData, Constants.ECDSA_SIGN_CALLBACK_PATH);

    app.get(SIGN_PATH, ctx -> ctx.html(signContributionHtml));

    CliInstructor.instructUserToSignContribution(host + SIGN_PATH, callbackEndpointIsDefined);

    while (store.getEcdsaSignature().isEmpty()) {
      LOG.info("Waiting for an ECDSA signature for the contribution...");
      ThrowingRunnable.unchecked(() -> TimeUnit.SECONDS.sleep(5)).run();
    }

    return store.getEcdsaSignature().get();
  }
}
