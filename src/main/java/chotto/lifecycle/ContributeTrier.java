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
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContributeTrier {

  private static final Logger LOG = LoggerFactory.getLogger(ContributeTrier.class);

  private final SequencerClient sequencerClient;
  private final TimeUnit attemptTimeUnit;
  private final int attemptPeriod;

  public ContributeTrier(
      final SequencerClient sequencerClient,
      final TimeUnit attemptTimeUnit,
      final int attemptPeriod) {
    this.sequencerClient = sequencerClient;
    this.attemptTimeUnit = attemptTimeUnit;
    this.attemptPeriod = attemptPeriod;
  }

  public BatchContribution tryContributeUntilSuccess(final String sessionId) {

    TryContributeResponse tryContributeResponse = sequencerClient.tryContribute(sessionId);

    while (tryContributeResponse.getBatchContribution().isEmpty()) {
      tryContributeResponse.getSequencerError().ifPresent(this::handleError);
      LOG.info(
          "Will try to contribute again in {} {}",
          attemptPeriod,
          attemptTimeUnit.name().toLowerCase());
      sleep(attemptPeriod);
      tryContributeResponse = sequencerClient.tryContribute(sessionId);
    }

    return tryContributeResponse.getBatchContribution().get();
  }

  private void handleError(final SequencerError sequencerError) {
    if (errorIsAnotherContributionInProgress(sequencerError)) {
      tryLogLobbySize();
    } else if (errorIsRateLimiting(sequencerError)) {
      throw new IllegalStateException("Rate limiting error was received from the sequencer");
    } else if (errorIsUnknownSessionId(sequencerError)) {
      throw new IllegalStateException("Unknown session id error was received from the sequencer");
    }
  }

  private boolean errorIsAnotherContributionInProgress(final SequencerError sequencerError) {
    return sequencerError.getCode().contains(ANOTHER_CONTRIBUTION_IN_PROGRESS_ERROR);
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
