package chotto.lifecycle;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import chotto.auth.SessionInfo;
import chotto.cli.AsciiArtPrinter;
import chotto.contribution.Contributor;
import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import chotto.objects.Receipt;
import chotto.sequencer.SequencerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiLifecycle {

  private static final Logger LOG = LoggerFactory.getLogger(ApiLifecycle.class);

  private static final DateTimeFormatter FILE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final SessionInfo sessionInfo;
  private final ContributeTrier contributeTrier;
  private final SequencerClient sequencerClient;
  private final Contributor contributor;
  private final ObjectMapper objectMapper;
  private final Path outputDirectory;

  public ApiLifecycle(
      final SessionInfo sessionInfo,
      final ContributeTrier contributeTrier,
      final SequencerClient sequencerClient,
      final Contributor contributor,
      final ObjectMapper objectMapper,
      final Path outputDirectory) {
    this.sessionInfo = sessionInfo;
    this.contributeTrier = contributeTrier;
    this.sequencerClient = sequencerClient;
    this.contributor = contributor;
    this.objectMapper = objectMapper;
    this.outputDirectory = outputDirectory;
  }

  public void runLifecycle() {

    final String sessionId = sessionInfo.getSessionId();
    final String nickname = sessionInfo.getNickname();

    LOG.info("Trying to contribute");

    final BatchContribution batchContribution =
        contributeTrier.tryContributeUntilSuccess(sessionId);

    AsciiArtPrinter.printCeremonySummoning(nickname);

    LOG.info("Beginning contribution");

    final BatchContribution updatedBatchContribution;
    try {
      updatedBatchContribution = contributor.contribute(batchContribution);
    } catch (final Exception ex) {
      sequencerClient.abortContribution(sessionId);
      throw new IllegalStateException("There was an error during contribution", ex);
    }

    LOG.info("Finished contribution");

    LOG.info("Uploading contribution");

    final Receipt receipt = sequencerClient.contribute(updatedBatchContribution, sessionId);

    LOG.info("Contribution uploading was successful");

    LOG.info("Received receipt with signature {}", receipt.getSignature());

    saveContribution(updatedBatchContribution, nickname);
    saveReceipt(receipt, nickname);
    requestAndSaveTranscript();
  }

  private void saveContribution(final BatchContribution contribution, final String nickname) {
    final Path contributionPath = outputDirectory.resolve("contribution-" + nickname + ".json");
    try {
      final String contributionJson = objectMapper.writeValueAsString(contribution);
      Files.writeString(contributionPath, contributionJson, CREATE, TRUNCATE_EXISTING);
      LOG.info("Saved contribution to {}", contributionPath);
    } catch (final IOException __) {
      LOG.warn("Couldn't save contribution to {}", contributionPath);
    }
  }

  private void saveReceipt(final Receipt receipt, final String nickname) {
    final Path receiptPath = outputDirectory.resolve("receipt-" + nickname + ".txt");
    try {
      Files.writeString(receiptPath, receipt.getReceipt(), CREATE, TRUNCATE_EXISTING);
      LOG.info("Saved receipt to {}", receiptPath);
    } catch (final IOException __) {
      LOG.warn("Couldn't save receipt to {}. Will log it instead below.", receiptPath);
      LOG.info(receipt.getReceipt());
    }
  }

  private void requestAndSaveTranscript() {
    final Path transcriptPath =
        outputDirectory.resolve(
            "transcript-" + FILE_TIME_FORMATTER.format(LocalDateTime.now()) + ".json");
    try {
      final BatchTranscript transcript = sequencerClient.getTranscript();
      final String transcriptJson = objectMapper.writeValueAsString(transcript);
      Files.writeString(transcriptPath, transcriptJson, CREATE);
      LOG.info("Saved transcript to {}", transcriptPath);
    } catch (final Exception ex) {
      LOG.warn("Couldn't save transcript to " + transcriptPath, ex);
    }
  }
}
