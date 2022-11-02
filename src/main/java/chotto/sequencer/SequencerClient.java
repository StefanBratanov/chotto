package chotto.sequencer;

import static com.pivovarit.function.ThrowingSupplier.unchecked;

import chotto.auth.Provider;
import chotto.contribution.ContributionVerification;
import chotto.objects.BatchContribution;
import chotto.objects.CeremonyStatus;
import chotto.objects.Receipt;
import chotto.objects.SequencerError;
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

  private static final Logger LOG = LoggerFactory.getLogger(SequencerClient.class);

  private final HttpClient httpClient;
  private final URI sequencerEndpoint;
  private final ObjectMapper objectMapper;
  private final ContributionVerification contributionVerification;

  public SequencerClient(
      final HttpClient httpClient,
      final URI sequencerEndpoint,
      final ObjectMapper objectMapper,
      final ContributionVerification contributionVerification) {
    this.httpClient = httpClient;
    this.sequencerEndpoint = sequencerEndpoint;
    this.objectMapper = objectMapper;
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
      LOG.error(getFailureMessage(response, "Contribution is not available"));
      return new TryContributeResponse(Optional.empty(), getMaybeSequencerError(response.body()));
    }

    final String contributionJson = response.body();

    final Optional<SequencerError> maybeSequencerError = getMaybeSequencerError(contributionJson);

    if (maybeSequencerError.isPresent()) {
      LOG.info(getFailureMessage(response, "Contribution is not available"));
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

    if (!contributionVerification.subgroupChecks(batchContribution)) {
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
      LOG.error(getFailureMessage(response, "Failed to abort contribution"));
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
    throw new SequencerClientException(getFailureMessage(response, errorPrefix));
  }

  private String getFailureMessage(final HttpResponse<String> response, final String errorPrefix) {
    return String.format(
        "%s (status: %s%s)",
        errorPrefix,
        response.statusCode(),
        Optional.ofNullable(response.body())
            .filter(body -> !body.isBlank())
            .map(body -> ", message: " + body)
            .orElse(""));
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
