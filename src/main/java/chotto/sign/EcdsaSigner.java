package chotto.sign;

import chotto.Constants;
import chotto.Store;
import chotto.cli.CliInstructor;
import chotto.contribution.SubContributionManager;
import chotto.objects.BatchTranscript;
import chotto.objects.SubContributionContext;
import chotto.template.TemplateResolver;
import com.pivovarit.function.ThrowingRunnable;
import io.javalin.Javalin;
import java.util.List;
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
  private final SubContributionManager subContributionManager;
  private final Store store;

  public EcdsaSigner(
      final Javalin app,
      final TemplateResolver templateResolver,
      final String host,
      final boolean callbackEndpointIsDefined,
      final SubContributionManager subContributionManager,
      final Store store) {
    this.app = app;
    this.templateResolver = templateResolver;
    this.host = host;
    this.callbackEndpointIsDefined = callbackEndpointIsDefined;
    this.subContributionManager = subContributionManager;
    this.store = store;
  }

  public String sign(final String ethAddress, final BatchTranscript batchTranscript) {

    final List<SubContributionContext> contributionContexts = subContributionManager.getContexts();

    final String typedData =
        templateResolver.createTypedData(batchTranscript, contributionContexts);

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
