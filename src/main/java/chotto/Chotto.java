package chotto;

import static chotto.Constants.AUTH_CALLBACK_PATH;
import static chotto.Constants.ECDSA_SIGN_CALLBACK_PATH;

import chotto.auth.AuthCallback;
import chotto.auth.Provider;
import chotto.auth.SessionInfo;
import chotto.cli.AsciiArtHelper;
import chotto.cli.CliInstructor;
import chotto.cli.PropertiesVersionProvider;
import chotto.contribution.Contributor;
import chotto.contribution.SubContributionManager;
import chotto.identity.IdentityRetriever;
import chotto.lifecycle.ApiLifecycle;
import chotto.lifecycle.ContributeTrier;
import chotto.objects.BatchTranscript;
import chotto.objects.CeremonyStatus;
import chotto.secret.Csprng;
import chotto.secret.SecretsManager;
import chotto.sequencer.SequencerClient;
import chotto.serialization.ChottoObjectMapper;
import chotto.sign.BlsSigner;
import chotto.sign.EcdsaSignCallback;
import chotto.sign.EcdsaSigner;
import chotto.template.TemplateResolver;
import chotto.verification.ContributionVerification;
import chotto.verification.TranscriptVerification;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivovarit.function.ThrowingRunnable;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.Spec;

@Command(
    name = "chotto",
    mixinStandardHelpOptions = true,
    versionProvider = PropertiesVersionProvider.class,
    header = {
      "  _____ _           _   _        ",
      " / ____| |         | | | |       ",
      "| |    | |__   ___ | |_| |_ ___  ",
      "| |    | '_ \\ / _ \\| __| __/ _ \\ ",
      "| |____| | | | (_) | |_| || (_) |",
      " \\_____|_| |_|\\___/ \\__|\\__\\___/ "
    },
    description = "Ethereum's Power of Tau client implementation written in Java")
public class Chotto implements Callable<Integer> {

  private static final Logger LOG = LoggerFactory.getLogger(Chotto.class);

  @Spec CommandSpec spec;

  @Option(
      names = {"--sequencer"},
      description = "The URL of the sequencer which would be used for the ceremony",
      required = true)
  private URI sequencer;

  private String entropyEntry;

  @Option(
      names = {"--entropy-entry"},
      description =
          "A text which would be used as a seed to generate random secrets in the background. There will be several layers of randomness on top of this text, so there is no need to worry about its uniqueness or keeping it a secret.",
      required = true)
  public void setEntropyEntry(final String value) {
    if (value.length() <= 5) {
      throw new ParameterException(
          spec.commandLine(),
          String.format(
              "Invalid value '%s' for option '--entropy-entry': "
                  + "the text should be more than 5 characters.",
              value));
    }
    entropyEntry = value;
  }

  @Option(
      names = {"--server-port"},
      description = "The port on which to start the local server",
      showDefaultValue = Visibility.ALWAYS)
  private int serverPort = 8080;

  @Option(
      names = {"--authentication"},
      description =
          "The authentication provider which will be used for logging in. Valid values: ${COMPLETION-CANDIDATES}",
      showDefaultValue = Visibility.ALWAYS)
  private Provider provider = Provider.ETHEREUM;

  private int contributionAttemptPeriod = 15;

  @Option(
      names = {"--verify-transcript"},
      description = "Whether to verify the sequencer transcript or not.",
      showDefaultValue = Visibility.ALWAYS)
  private boolean verifyTranscript = false;

  @Option(
      names = {"--contribution-attempt-period"},
      description =
          "How often (in seconds) to attempt contribution once authenticated. This value could change dynamically based on responses from the sequencer.",
      defaultValue = "15",
      showDefaultValue = Visibility.ALWAYS)
  public void setContributionAttemptPeriod(final int value) {
    if (value < 1) {
      throw new ParameterException(
          spec.commandLine(),
          String.format(
              "Invalid value '%d' for option '--contribution-attempt-period': "
                  + "value should be bigger than 0.",
              value));
    }
    contributionAttemptPeriod = value;
  }

  @Option(
      names = {"--bls-sign-sub-contributions"},
      description = "Sign your sub-contributions using your identity. Doing so is RECOMMENDED.",
      showDefaultValue = Visibility.ALWAYS)
  private boolean blsSignSubContributions = true;

  @Option(
      names = {"--ecdsa-sign-contribution"},
      description =
          "Sign your contribution using the Ethereum address you logged in with. Doing so is RECOMMENDED. This value is only applicable when you have authenticated with Ethereum.",
      showDefaultValue = Visibility.ALWAYS)
  private boolean ecdsaSignContribution = true;

  @Option(
      names = {"--callback-endpoint"},
      description =
          "The URL of the server which is started by this process. Specify this option ONLY if you decide to login and sign from a browser on a different computer. Make sure the URL is accessible from that browser.")
  private Optional<URI> callbackEndpoint = Optional.empty();

  @Option(
      names = {"--output-directory"},
      description = "The directory where the outputs of the ceremony will be saved",
      showDefaultValue = Visibility.ALWAYS)
  private Path outputDirectory =
      Paths.get(System.getProperty("user.home") + File.separator + "kzg-ceremony");

  @Override
  public Integer call() {
    try {
      runSafely();
      return 0;
    } catch (final Throwable ex) {
      LOG.error(
          "There was an error during the ceremony. You can restart Chotto to try to contribute again.",
          ex);
      return 1;
    }
  }

  private void runSafely() {
    createOutputDirectoryIfNeeded();

    AsciiArtHelper.printBannerOnStartup();

    final Store store = new Store();

    final AuthCallback authCallback = new AuthCallback(store);
    final EcdsaSignCallback ecdsaSignCallback = new EcdsaSignCallback(store);

    final Javalin app =
        Javalin.create()
            .addHandler(HandlerType.GET, AUTH_CALLBACK_PATH, authCallback)
            .addHandler(HandlerType.GET, ECDSA_SIGN_CALLBACK_PATH, ecdsaSignCallback)
            .start(serverPort);

    LOG.info("Started server on port {}", serverPort);

    final HttpClient httpClient = HttpClient.newBuilder().build();
    final ObjectMapper objectMapper = ChottoObjectMapper.getInstance();

    final TranscriptVerification transcriptVerification = new TranscriptVerification(objectMapper);
    final ContributionVerification contributionVerification =
        new ContributionVerification(objectMapper);

    final SequencerClient sequencerClient =
        new SequencerClient(
            httpClient, sequencer, objectMapper, transcriptVerification, contributionVerification);

    final CeremonyStatus ceremonyStatus = sequencerClient.getCeremonyStatus();
    AsciiArtHelper.printCeremonyStatus(ceremonyStatus);

    final Csprng csprng = new Csprng(entropyEntry);
    final SecretsManager secretsManager = new SecretsManager(csprng);

    secretsManager.generateSecrets();

    final BlsSigner blsSigner = new BlsSigner();

    final String host =
        callbackEndpoint.map(URI::toString).orElse("http://localhost:" + serverPort);

    final String loginLink = sequencerClient.getLoginLink(provider, host + AUTH_CALLBACK_PATH);

    final boolean callbackEndpointIsDefined = callbackEndpoint.isPresent();

    final TemplateResolver templateResolver = new TemplateResolver();

    CliInstructor.instructUserToLogin(loginLink, callbackEndpointIsDefined);

    while (store.getSessionInfo().isEmpty()) {
      LOG.info("Waiting for user login...");
      ThrowingRunnable.unchecked(() -> TimeUnit.SECONDS.sleep(5)).run();
    }

    final SessionInfo sessionInfo = store.getSessionInfo().get();

    final IdentityRetriever identityRetriever =
        IdentityRetriever.create(sessionInfo.getProvider(), httpClient, objectMapper);

    final String nickname = sessionInfo.getNickname();

    final String identity = identityRetriever.getIdentity(nickname);

    LOG.info("Your identity is {}", identity);

    final SubContributionManager subContributionManager =
        new SubContributionManager(secretsManager, blsSigner, identity, blsSignSubContributions);

    subContributionManager.generateContexts();

    final EcdsaSigner ecdsaSigner =
        new EcdsaSigner(
            app, templateResolver, host, callbackEndpointIsDefined, subContributionManager, store);

    final Optional<BatchTranscript> verifiedBatchTranscript =
        verifyTranscript ? Optional.of(sequencerClient.getTranscript(true)) : Optional.empty();

    final Optional<String> ecdsaSignatureMaybe;
    if (sessionInfo.getProvider().equals(Provider.ETHEREUM) && ecdsaSignContribution) {
      final BatchTranscript batchTranscript =
          verifiedBatchTranscript.orElseGet(() -> sequencerClient.getTranscript(false));
      final String ecdsaSignature = ecdsaSigner.sign(nickname, batchTranscript);
      ecdsaSignatureMaybe = Optional.of(ecdsaSignature);
    } else {
      ecdsaSignatureMaybe = Optional.empty();
    }

    final Contributor contributor = new Contributor(subContributionManager, ecdsaSignatureMaybe);

    final ContributeTrier contributeTrier =
        new ContributeTrier(sequencerClient, TimeUnit.SECONDS, contributionAttemptPeriod);

    final ApiLifecycle apiLifecycle =
        new ApiLifecycle(
            sessionInfo,
            contributeTrier,
            sequencerClient,
            contributor,
            objectMapper,
            outputDirectory);

    apiLifecycle.runLifecycle();

    AsciiArtHelper.printThankYou();

    CliInstructor.instructUserToShareOnTwitter(identity);
  }

  private void createOutputDirectoryIfNeeded() {
    try {
      Files.createDirectories(outputDirectory);
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void main(String[] args) {
    System.setProperty("picocli.ansi", "false");
    final int exitCode =
        new CommandLine(new Chotto()).setCaseInsensitiveEnumValuesAllowed(true).execute(args);
    System.exit(exitCode);
  }
}
