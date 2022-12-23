package chotto.sequencer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import chotto.TestUtil;
import chotto.auth.Provider;
import chotto.contribution.ContributionVerification;
import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import chotto.objects.CeremonyStatus;
import chotto.objects.Receipt;
import chotto.objects.Transcript;
import chotto.objects.Witness;
import chotto.serialization.ChottoObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.slf4j.event.Level;

class SequencerClientTest {

  private final String sessionId = "123";

  private final ObjectMapper objectMapper = ChottoObjectMapper.getInstance();

  private ClientAndServer mockServer;

  private final ContributionVerification contributionVerification =
      mock(ContributionVerification.class);

  private SequencerClient sequencerClient;

  @BeforeEach
  public void setUp() {
    mockServer =
        ClientAndServer.startClientAndServer(Configuration.configuration().logLevel(Level.WARN));
    sequencerClient =
        new SequencerClient(
            HttpClient.newBuilder().build(),
            URI.create("http://localhost:" + mockServer.getPort()),
            objectMapper,
            contributionVerification);
    when(contributionVerification.schemaCheck(anyString())).thenReturn(true);
    when(contributionVerification.subgroupChecks(any())).thenReturn(true);
  }

  @AfterEach
  public void cleanUp() {
    mockServer.stop();
  }

  @Test
  public void testGettingCeremonyStatus() {
    mockServer
        .when(request().withMethod("GET").withPath("/info/status"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(
                    "{\"lobby_size\":1,\"num_contributions\":16,\"sequencer_address\":\"string\"}"));

    final CeremonyStatus ceremonyStatus = sequencerClient.getCeremonyStatus();

    assertThat(ceremonyStatus.getLobbySize()).isEqualTo(1);
    assertThat(ceremonyStatus.getNumContributions()).isEqualTo(16);
    assertThat(ceremonyStatus.getSequencerAddress()).isEqualTo("string");
  }

  @Test
  public void testGettingCurrentTranscript() {
    mockServer
        .when(request().withMethod("GET").withPath("/info/current_state"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(TestUtil.readResource("initialTranscript.json")));

    final BatchTranscript batchTranscript = sequencerClient.getTranscript();
    final List<Transcript> transcripts = batchTranscript.getTranscripts();

    assertThat(transcripts).hasSize(4);
    assertThat(batchTranscript.getParticipantIds()).hasSize(1);
    assertThat(batchTranscript.getParticipantEcdsaSignatures()).hasSize(1);

    transcripts.forEach(
        transcript -> {
          assertThat(transcript.getPowersOfTau()).isNotNull();
          final Witness witness = transcript.getWitness();
          assertThat(witness.getRunningProducts()).isNotEmpty();
          assertThat(witness.getPotPubkeys()).isNotEmpty();
          assertThat(witness.getBlsSignatures()).isNotEmpty();
          assertThat(transcript.getNumG1Powers()).isGreaterThan(0);
          assertThat(transcript.getNumG2Powers()).isGreaterThan(0);
        });
  }

  @Test
  public void testGettingLoginLinks() {
    final String redirectTo = "http://localhost:8080";

    mockServer
        .when(
            request()
                .withMethod("GET")
                .withQueryStringParameter("redirect_to", redirectTo)
                .withPath("/auth/request_link"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(
                    "{\"eth_auth_url\":\"https://ethfoo.bar\",\"github_auth_url\":\"https://githubfoo.bar\"}"));

    final String ethLink = sequencerClient.getLoginLink(Provider.ETHEREUM, redirectTo);
    final String gitHubLink = sequencerClient.getLoginLink(Provider.GITHUB, redirectTo);

    assertThat(ethLink).isEqualTo("https://ethfoo.bar");
    assertThat(gitHubLink).isEqualTo("https://githubfoo.bar");
  }

  @Test
  public void testContributionNotAvailable() {
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/lobby/try_contribute"))
        .respond(
            response()
                .withStatusCode(400)
                .withBody(
                    "{\"code\":\"TryContributeError::RateLimited\",\"error\":\"call came too early. rate limited\"}"));

    final TryContributeResponse response = sequencerClient.tryContribute(sessionId);

    assertThat(response.getBatchContribution()).isEmpty();
    assertThat(response.getSequencerError())
        .hasValueSatisfying(
            sequencerError -> {
              assertThat(sequencerError.getCode()).isEqualTo("TryContributeError::RateLimited");
              assertThat(sequencerError.getMessage())
                  .isEqualTo("call came too early. rate limited");
            });
  }

  @Test
  public void testContributionReturnsErrorMessage() {
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/lobby/try_contribute"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(
                    "{\"code\":\"TryContributeError::AnotherContributionInProgress\",\"message\":\"another contribution in progress\"}"));

    final TryContributeResponse response = sequencerClient.tryContribute(sessionId);

    assertThat(response.getBatchContribution()).isEmpty();
    assertThat(response.getSequencerError())
        .hasValueSatisfying(
            sequencerError -> {
              assertThat(sequencerError.getCode())
                  .isEqualTo("TryContributeError::AnotherContributionInProgress");
              assertThat(sequencerError.getMessage()).isEqualTo("another contribution in progress");
            });
  }

  @Test
  public void testContributionDoesNotPassSchemaCheck() {
    setupContributionResponse();

    when(contributionVerification.schemaCheck(anyString())).thenReturn(false);

    final IllegalStateException exception =
        Assertions.assertThrows(
            IllegalStateException.class, () -> sequencerClient.tryContribute(sessionId));

    assertThat(exception)
        .hasMessage(
            "The received contribution does not match the defined contribution json schema");
  }

  @Test
  public void testContributionDoesNotPassPointChecks() {
    setupContributionResponse();

    when(contributionVerification.subgroupChecks(any())).thenReturn(false);

    final IllegalStateException exception =
        Assertions.assertThrows(
            IllegalStateException.class, () -> sequencerClient.tryContribute(sessionId));

    assertThat(exception).hasMessage("The received contribution does not pass the point checks");
  }

  @Test
  public void testContributionIsAvailable() {
    setupContributionResponse();

    final TryContributeResponse response = sequencerClient.tryContribute(sessionId);
    assertThat(response.getBatchContribution()).hasValue(TestUtil.getInitialBatchContribution());
    assertThat(response.getSequencerError()).isEmpty();
  }

  @Test
  public void testContributionSuccess() throws JSONException {
    final BatchContribution batchContribution = TestUtil.getInitialBatchContribution();

    final HttpRequest requestDefinition =
        request()
            .withMethod("POST")
            .withHeader("Authorization", "Bearer " + sessionId)
            .withPath("/contribute");

    mockServer
        .when(requestDefinition)
        .respond(
            response()
                .withStatusCode(200)
                .withBody("{\"receipt\":\"string\",\"signature\":\"string\"}"));

    final Receipt receipt = sequencerClient.contribute(batchContribution, sessionId);

    assertThat(receipt.getReceipt()).isEqualTo("string");
    assertThat(receipt.getSignature()).isEqualTo("string");

    final HttpRequest[] recordedRequest = mockServer.retrieveRecordedRequests(requestDefinition);

    assertThat(recordedRequest).hasSize(1);

    JSONAssert.assertEquals(
        TestUtil.readResource("initialContribution.json"),
        recordedRequest[0].getBodyAsString(),
        JSONCompareMode.STRICT_ORDER);
  }

  @Test
  public void testContributionFailure() {
    final BatchContribution batchContribution = TestUtil.getInitialBatchContribution();

    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/contribute"))
        .respond(
            response()
                .withStatusCode(400)
                .withBody(
                    "{\"code\":\"SessionError::InvalidSessionId\",\"error\":\"invalid Bearer token\"}"));

    final SequencerClientException exception =
        Assertions.assertThrows(
            SequencerClientException.class,
            () -> sequencerClient.contribute(batchContribution, sessionId));

    assertThat(exception)
        .hasMessage(
            "Failed to upload contribution (status: 400, message: {\"code\":\"SessionError::InvalidSessionId\",\"error\":\"invalid Bearer token\"})");
  }

  @Test
  public void testAbortingContribution() {
    final HttpRequest requestDefinition =
        request()
            .withMethod("POST")
            .withHeader("Authorization", "Bearer " + sessionId)
            .withPath("/contribution/abort");

    mockServer.when(requestDefinition).respond(response().withStatusCode(200).withBody("{}"));

    sequencerClient.abortContribution(sessionId);

    assertThat(mockServer.retrieveRecordedRequests(requestDefinition)).hasSize(1);
  }

  private void setupContributionResponse() {
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/lobby/try_contribute"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(TestUtil.readResource("initialContribution.json")));
  }
}
