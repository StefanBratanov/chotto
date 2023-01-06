package chotto.sequencer;

import static com.pivovarit.function.ThrowingSupplier.unchecked;

import chotto.auth.Provider;
import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import chotto.objects.CeremonyStatus;
import chotto.objects.Receipt;
import chotto.objects.SequencerError;
import chotto.verification.ContributionVerification;
import chotto.verification.TranscriptVerification;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivovarit.function.ThrowingSupplier;
import io.javalin.http.ContentType;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequencerClient {

  public static final String RATE_LIMITED_ERROR = "RateLimited";
  public static final String UNKNOWN_SESSION_ID_ERROR = "UnknownSessionId";
  public static final String ANOTHER_CONTRIBUTION_IN_PROGRESS_ERROR =
      "AnotherContributionInProgress";

  private static final Logger LOG = LoggerFactory.getLogger(SequencerClient.class);

  private final HttpClient httpClient;
  private final URI sequencerEndpoint;
  private final ObjectMapper objectMapper;
  private final TranscriptVerification transcriptVerification;
  private final ContributionVerification contributionVerification;

  public SequencerClient(
      final HttpClient httpClient,
      final URI sequencerEndpoint,
      final ObjectMapper objectMapper,
      final TranscriptVerification transcriptVerification,
      final ContributionVerification contributionVerification) {
    this.httpClient = httpClient;
    this.sequencerEndpoint = sequencerEndpoint;
    this.objectMapper = objectMapper;
    this.transcriptVerification = transcriptVerification;
    this.contributionVerification = contributionVerification;
  }

  public CeremonyStatus getCeremonyStatus() {
    final HttpRequest request = buildGetRequest("/info/status").build();
    final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throwException(response, "Failed to get ceremony status");
    }

    return unchecked(() -> objectMapper.readValue(response.body(), CeremonyStatus.class)).get();
  }

  public BatchTranscript getTranscript(final boolean verifyTranscript) {
    LOG.info("Requesting ceremony transcript...");

    final HttpRequest request = buildGetRequest("/info/current_state").build();
    final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throwException(response, "Failed to get transcript");
    }

    LOG.info("A transcript was received" + (verifyTranscript ? " .Verifying it." : ""));

    final String transcriptJson = response.body();

    if (verifyTranscript) {

      if (!transcriptVerification.schemaCheck(transcriptJson)) {
        throw new IllegalStateException(
            "The received transcript does not match the defined transcript json schema");
      }

      LOG.info("Transcript passes schema check");

      final BatchTranscript batchTranscript =
          unchecked(() -> objectMapper.readValue(transcriptJson, BatchTranscript.class)).get();

      if (!transcriptVerification.pointChecks(batchTranscript)) {
        throw new IllegalStateException("The received transcript does not pass the point checks");
      }

      LOG.info("Transcript passes point checks");

      return batchTranscript;
    }

    return unchecked(() -> objectMapper.readValue(transcriptJson, BatchTranscript.class)).get();
  }

  public String getLoginLink(final Provider provider, final String redirectTo) {
    final HttpRequest request =
        buildGetRequest("/auth/request_link?redirect_to=" + redirectTo).build();

    final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throwException(response, "Failed to get login link");
    }

    final JsonNode responseJson = unchecked(() -> objectMapper.readTree(response.body())).get();

    if (provider.equals(Provider.ETHEREUM)) {
      return responseJson.get("eth_auth_url").asText();
    } else if (provider.equals(Provider.GITHUB)) {
      return responseJson.get("github_auth_url").asText();
    } else {
      throw new IllegalArgumentException(provider + " is not supported for logging in");
    }
  }

  public TryContributeResponse tryContribute(final String sessionId) {
    final HttpRequest request =
        buildPostRequest("/lobby/try_contribute", BodyPublishers.noBody())
            .header("Authorization", "Bearer " + sessionId)
            .build();

    final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      LOG.warn(createExceptionMessage(response, "Contribution is not available"));
      return new TryContributeResponse(Optional.empty(), getMaybeSequencerError(response.body()));
    }

    final String contributionJson = response.body();

    final Optional<SequencerError> maybeSequencerError = getMaybeSequencerError(contributionJson);

    if (maybeSequencerError.isPresent()) {
      final SequencerError sequencerError = maybeSequencerError.get();
      if (sequencerError.getCode().contains(ANOTHER_CONTRIBUTION_IN_PROGRESS_ERROR)) {
        LOG.info("Contribution is not available. Another contribution is in progress.");
      } else {
        LOG.info(createExceptionMessage(response, "Contribution is not available"));
      }
      return new TryContributeResponse(Optional.empty(), maybeSequencerError);
    }

    LOG.info("A contribution was received. Verifying it.");

    if (!contributionVerification.schemaCheck(contributionJson)) {
      throw new IllegalStateException(
          "The received contribution does not match the defined contribution json schema");
    }

    LOG.info("Contribution passes schema check");

    final BatchContribution batchContribution =
        unchecked(() -> objectMapper.readValue(contributionJson, BatchContribution.class)).get();

    if (!contributionVerification.pointChecks(batchContribution)) {
      throw new IllegalStateException("The received contribution does not pass the point checks");
    }

    LOG.info("Contribution passes point checks");

    return new TryContributeResponse(Optional.of(batchContribution), Optional.empty());
  }

  public Receipt contribute(final BatchContribution batchContribution, final String sessionId) {

    final HttpRequest request =
        buildPostRequest(
                "/contribute",
                BodyPublishers.ofByteArray(
                    ThrowingSupplier.unchecked(
                            () -> objectMapper.writeValueAsBytes(batchContribution))
                        .get()))
            .header("Authorization", "Bearer " + sessionId)
            .header("Content-Type", ContentType.JSON)
            .build();

    final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      throwException(response, "Failed to upload contribution");
    }

    return unchecked(() -> objectMapper.readValue(response.body(), Receipt.class)).get();
  }

  public void abortContribution(final String sessionId) {
    final HttpRequest request =
        buildPostRequest("/contribution/abort", BodyPublishers.noBody())
            .header("Authorization", "Bearer " + sessionId)
            .build();

    final HttpResponse<String> response = sendRequest(request, BodyHandlers.ofString());

    if (response.statusCode() != 200) {
      LOG.error(createExceptionMessage(response, "Failed to abort contribution"));
      return;
    }

    LOG.info("Aborted contribution");
  }

  private HttpRequest.Builder buildGetRequest(final String path) {
    return buildRequest(path, "GET", BodyPublishers.noBody());
  }

  private HttpRequest.Builder buildPostRequest(
      final String path, final BodyPublisher bodyPublisher) {
    return buildRequest(path, "POST", bodyPublisher);
  }

  private HttpRequest.Builder buildRequest(
      final String path, final String method, final BodyPublisher bodyPublisher) {
    return HttpRequest.newBuilder(sequencerEndpoint.resolve(path)).method(method, bodyPublisher);
  }

  private <T> HttpResponse<T> sendRequest(
      final HttpRequest request, final BodyHandler<T> responseBodyHandler) {
    try {
      return httpClient.send(request, responseBodyHandler);
    } catch (final IOException | InterruptedException ex) {
      throw new SequencerClientException("Error when sending request to " + request.uri(), ex);
    }
  }

  private void throwException(final HttpResponse<String> response, final String errorPrefix) {
    throw new SequencerClientException(createExceptionMessage(response, errorPrefix));
  }

  private String createExceptionMessage(
      final HttpResponse<String> response, final String errorPrefix) {
    final String failureMessage =
        getFailureMessage(response).map(message -> ", message: " + message).orElse("");
    return String.format("%s (status: %s%s)", errorPrefix, response.statusCode(), failureMessage);
  }

  private Optional<String> getFailureMessage(final HttpResponse<String> response) {
    return Optional.ofNullable(response.body()).filter(body -> !body.isBlank());
  }

  private Optional<SequencerError> getMaybeSequencerError(final String json) {
    try {
      final SequencerError sequencerError = objectMapper.readValue(json, SequencerError.class);
      return Optional.of(sequencerError);
    } catch (final Exception __) {
      return Optional.empty();
    }
  }
}
