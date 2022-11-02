package chotto.lifecycle;

import chotto.objects.BatchContribution;
import chotto.objects.SequencerError;
import chotto.sequencer.SequencerClient;
import chotto.sequencer.TryContributeResponse;
import com.pivovarit.function.ThrowingRunnable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContributeTrier {

  private static final Logger LOG = LoggerFactory.getLogger(ContributeTrier.class);

  private final SequencerClient sequencerClient;
  private final TimeUnit attemptTimeUnit;
  private final int attemptPeriod;

  private int rateLimitingAttemptPeriod;

  public ContributeTrier(
      final SequencerClient sequencerClient,
      final TimeUnit attemptTimeUnit,
      final int attemptPeriod) {
    this.sequencerClient = sequencerClient;
    this.attemptTimeUnit = attemptTimeUnit;
    this.attemptPeriod = attemptPeriod;
    rateLimitingAttemptPeriod = attemptPeriod;
  }

  public BatchContribution tryContributeUntilSuccess(final String sessionId) {

    TryContributeResponse tryContributeResponse = sequencerClient.tryContribute(sessionId);

    while (tryContributeResponse.getBatchContribution().isEmpty()) {
      final Optional<SequencerError> maybeSequencerError =
          tryContributeResponse.getSequencerError();
      final int nextAttemptPeriod = getNextAttemptPeriod(maybeSequencerError);
      LOG.info(
          "Will try to contribute again in {} {}",
          nextAttemptPeriod,
          attemptTimeUnit.name().toLowerCase());
      sleep(nextAttemptPeriod);
      tryContributeResponse = sequencerClient.tryContribute(sessionId);
    }

    return tryContributeResponse.getBatchContribution().get();
  }

  private int getNextAttemptPeriod(final Optional<SequencerError> maybeSequencerError) {
    if (maybeSequencerError.isPresent()) {
      final SequencerError sequencerError = maybeSequencerError.get();
      if (errorIsRateLimiting(sequencerError)) {
        LOG.info(
            "Rate limiting error was received. Will increase period for the next contribution attempt.");
        rateLimitingAttemptPeriod *= 2;
        return rateLimitingAttemptPeriod;
      } else {
        resetRateLimitingAttemptPeriod();
      }
    }
    resetRateLimitingAttemptPeriod();
    return attemptPeriod;
  }

  private void resetRateLimitingAttemptPeriod() {
    rateLimitingAttemptPeriod = attemptPeriod;
  }

  private boolean errorIsRateLimiting(final SequencerError sequencerError) {
    return sequencerError.getCode().contains("RateLimited");
  }

  private void sleep(final int period) {
    ThrowingRunnable.unchecked(() -> attemptTimeUnit.sleep(period)).run();
  }
}
