package chotto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import chotto.contribution.ContributionVerification;
import chotto.objects.BatchContribution;
import chotto.serialization.ChottoObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.slf4j.event.Level;
import picocli.CommandLine;

class ChottoIntegrationTest {

  private final CommandLine cmd = new CommandLine(new Chotto());

  private final LogCaptor logCaptor = LogCaptor.forRoot();

  private final ObjectMapper objectMapper = ChottoObjectMapper.getInstance();

  private final String sessionId = "a6d8bd3b-3154-4d29-bdd7-d28669b0a4a5";

  private final String ethAddress = "0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad";

  private final String ecdsaSignature =
      "0x1949e68bfab53a3f921ace3c83d562e36fa5fe82d6f603394e58627a2fa4a31553aca183c6adbb1dad2ac032358b863d2c2137fe2b046e822041037fb97758251c";

  private final ContributionVerification contributionVerification =
      new ContributionVerification(objectMapper);

  private ClientAndServer mockServer;

  private int serverPort;

  @TempDir Path tempDir;

  @BeforeEach
  public void setUp() {
    mockServer =
        ClientAndServer.startClientAndServer(Configuration.configuration().logLevel(Level.WARN));
    serverPort = getFreePort();
    mockCeremonyStatusResponse();
    mockLoginLinksResponse();
  }

  @AfterEach
  public void cleanUp() {
    mockServer.stop();
  }

  @Test
  public void testSuccessfulContribution() throws IOException, InterruptedException {

    mockTryContributeResponse();
    mockUploadingContributionResponse();
    mockGetTranscriptResponse();

    final CompletableFuture<Integer> exitCode = runChottoCommand();

    await().until(() -> logCaptor.getInfoLogs().contains("Waiting for user login..."));

    triggerAuthCallbackManually();

    await()
        .atMost(Duration.ofMinutes(1))
        .until(
            () ->
                logCaptor
                    .getInfoLogs()
                    .contains("Waiting for an ECDSA signature for the batch contribution..."));

    triggerEcdsaSignCallbackManually();

    await().atMost(Duration.ofMinutes(1)).until(exitCode::isDone);

    assertThat(exitCode).isCompletedWithValue(0);

    // verify contribution
    final Path savedContribution = tempDir.resolve("contribution-" + ethAddress + ".json");
    assertThat(savedContribution)
        .exists()
        .content()
        .satisfies(
            contributionJson -> {
              assertThat(contributionVerification.schemaCheck(contributionJson)).isTrue();
              final BatchContribution batchContribution =
                  objectMapper.readValue(contributionJson, BatchContribution.class);
              assertThat(contributionVerification.subgroupChecks(batchContribution)).isTrue();
              assertThat(batchContribution.getEcdsaSignature()).isEqualTo(ecdsaSignature);
            });
    // verify receipt is saved
    assertThat(tempDir.resolve("receipt-" + ethAddress + ".txt")).exists().isNotEmptyFile();
    assertThat(TestUtil.findSavedTranscriptFile(tempDir)).isNotEmptyFile();
  }

  @Test
  public void testProcessFailWhenUnknownSessionIdFromSequencer()
      throws IOException, InterruptedException {

    mockTryContributeUnknownSessionIdResponse();

    final CompletableFuture<Integer> exitCode = runChottoCommand();

    await().until(() -> logCaptor.getInfoLogs().contains("Waiting for user login..."));

    triggerAuthCallbackManually();

    await().atMost(Duration.ofMinutes(1)).until(exitCode::isDone);

    assertThat(exitCode).isCompletedWithValue(1);

    assertThat(logCaptor.getErrorLogs())
        .contains(
            "There was an error during the ceremony. You can restart Chotto to try to contribute again.");
    assertThat(tempDir).isEmptyDirectory();
  }

  private CompletableFuture<Integer> runChottoCommand() {
    return CompletableFuture.supplyAsync(
        () ->
            cmd.execute(
                "--sequencer=" + "http://localhost:" + mockServer.getPort(),
                "--entropy-entry=Danksharding",
                "--server-port=" + serverPort,
                "--callback-endpoint=" + getLocalServerHost(),
                "--output-directory=" + tempDir));
  }

  private void mockCeremonyStatusResponse() {
    mockServer
        .when(request().withMethod("GET").withPath("/info/status"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(
                    "{\"lobby_size\":0,\"num_contributions\":31,\"sequencer_address\":\"0x960A650EF9625aA9E8B747ab3cBbbE23f78F0b66\"}"));
  }

  private void mockLoginLinksResponse() {
    mockServer
        .when(
            request()
                .withMethod("GET")
                .withQueryStringParameter(
                    "redirect_to", getLocalServerHost() + Constants.AUTH_CALLBACK_PATH)
                .withPath("/auth/request_link"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(
                    "{\"eth_auth_url\":\"https://ethfoo.bar\",\"github_auth_url\":\"https://githubfoo.bar\"}"));
  }

  private void mockTryContributeResponse() {
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/lobby/try_contribute"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(TestUtil.readResource("integration/contribution.json")));
  }

  private void mockTryContributeUnknownSessionIdResponse() {
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/lobby/try_contribute"))
        .respond(
            response()
                .withStatusCode(401)
                .withBody(
                    "{\"code\":\"TryContributeError::UnknownSessionId\",\"error\":\"unknown session id\"}"));
  }

  private void mockUploadingContributionResponse() {
    mockServer
        .when(
            request()
                .withMethod("POST")
                .withHeader("Authorization", "Bearer " + sessionId)
                .withPath("/contribute"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(TestUtil.readResource("integration/receipt.json")));
  }

  private void mockGetTranscriptResponse() {
    mockServer
        .when(request().withMethod("GET").withPath("/info/current_state"))
        .respond(
            response()
                .withStatusCode(200)
                .withBody(TestUtil.readResource("integration/transcript.json")));
  }

  private void triggerAuthCallbackManually() throws IOException, InterruptedException {
    final HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(
                URI.create(getLocalServerHost())
                    .resolve(Constants.AUTH_CALLBACK_PATH + getAuthCallbackQueryParams()))
            .GET()
            .build();
    final HttpClient httpClient = HttpClient.newBuilder().build();
    httpClient.send(httpRequest, BodyHandlers.discarding());
  }

  private void triggerEcdsaSignCallbackManually() throws IOException, InterruptedException {
    final HttpRequest httpRequest =
        HttpRequest.newBuilder()
            .uri(
                URI.create(getLocalServerHost())
                    .resolve(Constants.ECDSA_SIGN_CALLBACK_PATH + "?signature=" + ecdsaSignature))
            .GET()
            .build();
    final HttpClient httpClient = HttpClient.newBuilder().build();
    httpClient.send(httpRequest, BodyHandlers.discarding());
  }

  private String getAuthCallbackQueryParams() {
    return "?session_id="
        + sessionId
        + "&sub=eth+%7C+"
        + ethAddress
        + "&nickname="
        + ethAddress
        + "&provider=Ethereum&exp=18446744073709551645";
  }

  private String getLocalServerHost() {
    return "http://localhost:" + serverPort;
  }

  private int getFreePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (final IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
