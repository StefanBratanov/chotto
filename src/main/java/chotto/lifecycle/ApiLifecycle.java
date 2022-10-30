package chotto.lifecycle;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import chotto.Csprng;
import chotto.cli.AsciiArtPrinter;
import chotto.contribution.Contributor;
import chotto.objects.BatchContribution;
import chotto.sequencer.Receipt;
import chotto.sequencer.SequencerClient;
import chotto.sequencer.SessionInfo;
import com.pivovarit.function.ThrowingRunnable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiLifecycle {

  private static final Logger LOG = LoggerFactory.getLogger(Csprng.class);

  private final SessionInfo sessionInfo;
  private final SequencerClient sequencerClient;
  private final int contributionAttemptPeriod;
  private final Contributor contributor;

  public ApiLifecycle(
      final SessionInfo sessionInfo,
      final SequencerClient sequencerClient,
      final int contributionAttemptPeriod,
      final Contributor contributor) {
    this.sessionInfo = sessionInfo;
    this.sequencerClient = sequencerClient;
    this.contributionAttemptPeriod = contributionAttemptPeriod;
    this.contributor = contributor;
  }

  public void runLifecycle() {

    final String sessionId = sessionInfo.getSessionId();
    final String nickname = sessionInfo.getNickname();

    LOG.info("Trying to contribute");

    Optional<BatchContribution> maybeContribution = sequencerClient.tryContribute(sessionId);

    while (maybeContribution.isEmpty()) {
      LOG.info("Will try to contribute again in {} seconds", contributionAttemptPeriod);
      ThrowingRunnable.unchecked(() -> TimeUnit.SECONDS.sleep(contributionAttemptPeriod)).run();
      maybeContribution = sequencerClient.tryContribute(sessionId);
    }

    final BatchContribution batchContribution = maybeContribution.get();

    AsciiArtPrinter.printCeremonySummoning(nickname);

    LOG.info("Beginning contribution");

    final BatchContribution updatedBatchContribution;
    try {
      updatedBatchContribution = contributor.contribute(batchContribution);
    } catch (Throwable ex) {
      sequencerClient.abortContribution(sessionId);
      throw new LifecycleException("There was an error during contribution.", ex);
    }

    LOG.info("Finished contribution");

    LOG.info("Uploading contribution");

    final Receipt receipt = sequencerClient.contribute(updatedBatchContribution, sessionId);

    LOG.info("Contribution uploading was successful");

    LOG.info("Received receipt with signature {}", receipt.getSignature());

    saveReceipt(receipt.getReceipt(), nickname);
  }

  private void saveReceipt(final String receipt, final String nickname) {
    final Path receiptPath = Paths.get("receipt-" + nickname + ".txt");
    try {
      Files.writeString(receiptPath, receipt, CREATE, TRUNCATE_EXISTING);
      LOG.info("Saved receipt to {}", receiptPath);
    } catch (IOException __) {
      LOG.warn("Couldn't save receipt to {}. Will log it instead below.", receiptPath);
      LOG.info(receipt);
    }
  }
}
