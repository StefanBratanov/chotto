package chotto.lifecycle;

import chotto.objects.BatchContribution;
import chotto.sequencer.SequencerClient;
import com.pivovarit.function.ThrowingRunnable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContributeTrier {

  private static final Logger LOG = LoggerFactory.getLogger(ContributeTrier.class);

  private final SequencerClient sequencerClient;
  private final int contributionAttemptPeriod;

  public ContributeTrier(
      final SequencerClient sequencerClient, final int contributionAttemptPeriod) {
    this.sequencerClient = sequencerClient;
    this.contributionAttemptPeriod = contributionAttemptPeriod;
  }

  public BatchContribution tryContributeUntilSuccess(final String sessionId) {

    Optional<BatchContribution> maybeContribution = sequencerClient.tryContribute(sessionId);

    while (maybeContribution.isEmpty()) {
      LOG.info("Will try to contribute again in {} second(s)", contributionAttemptPeriod);
      ThrowingRunnable.unchecked(() -> TimeUnit.SECONDS.sleep(contributionAttemptPeriod)).run();
      maybeContribution = sequencerClient.tryContribute(sessionId);
    }

    return maybeContribution.get();
  }
}
