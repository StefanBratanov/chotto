package chotto.lifecycle;

import static chotto.sequencer.SequencerClient.ANOTHER_CONTRIBUTION_IN_PROGRESS_ERROR;
import static chotto.sequencer.SequencerClient.RATE_LIMITED_ERROR;
import static chotto.sequencer.SequencerClient.UNKNOWN_SESSION_ID_ERROR;

import chotto.objects.BatchContribution;
import chotto.objects.CeremonyStatus;
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
      if (errorIsAnotherContributionInProgress(maybeSequencerError)) {
        tryLogLobbySize();
      }
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
            "Rate limiting error was received from the sequencer. Will increase period for the next contribution attempt.");
        rateLimitingAttemptPeriod = (int) Math.floor(rateLimitingAttemptPeriod * 1.1);
        return rateLimitingAttemptPeriod;
      } else if (errorIsUnknownSessionId(sequencerError)) {
        throw new IllegalStateException("Unknown session id error was received from the sequencer");
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

  private boolean errorIsAnotherContributionInProgress(
      final Optional<SequencerError> maybeSequencerError) {
    return maybeSequencerError
        .map(
            sequencerError ->
                sequencerError.getCode().contains(ANOTHER_CONTRIBUTION_IN_PROGRESS_ERROR))
        .orElse(false);
  }

  private boolean errorIsRateLimiting(final SequencerError sequencerError) {
    return sequencerError.getCode().contains(RATE_LIMITED_ERROR);
  }

  private boolean errorIsUnknownSessionId(final SequencerError sequencerError) {
    return sequencerError.getCode().contains(UNKNOWN_SESSION_ID_ERROR);
  }

  private void sleep(final int period) {
    ThrowingRunnable.unchecked(() -> attemptTimeUnit.sleep(period)).run();
  }

  private void tryLogLobbySize() {
    try {
      final CeremonyStatus ceremonyStatus = sequencerClient.getCeremonyStatus();
      LOG.info("Current lobby size: {}", ceremonyStatus.getLobbySize());
    } catch (final Exception __) {
      LOG.warn("Error while querying the current lobby size. Will not stop trying to contribute.");
    }
  }
}
