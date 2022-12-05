package chotto.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import chotto.objects.BatchContribution;
import chotto.objects.CeremonyStatus;
import chotto.objects.SequencerError;
import chotto.sequencer.SequencerClient;
import chotto.sequencer.TryContributeResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ContributeTrierTest {

  private final String sessionId = "123";

  private final SequencerClient sequencerClient = mock(SequencerClient.class);

  private final BatchContribution receivedContribution = mock(BatchContribution.class);

  private final TryContributeResponse successResponse =
      new TryContributeResponse(Optional.of(receivedContribution), Optional.empty());

  private final TryContributeResponse emptyResponse =
      new TryContributeResponse(Optional.empty(), Optional.empty());

  private final TryContributeResponse rateLimitingResponse =
      new TryContributeResponse(
          Optional.empty(),
          Optional.of(
              new SequencerError(
                  "TryContributeError::RateLimited", "call came too early. rate limited")));

  private final TryContributeResponse anotherContributionInProgressResponse =
      new TryContributeResponse(
          Optional.empty(),
          Optional.of(
              new SequencerError(
                  "TryContributeError::AnotherContributionInProgress",
                  "another contribution in progress")));

  private final ContributeTrier contributeTrier =
      new ContributeTrier(sequencerClient, TimeUnit.MILLISECONDS, 100);

  @Test
  public void testContributingUntilSuccess() {

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(emptyResponse)
        .thenReturn(emptyResponse)
        .thenReturn(emptyResponse)
        .thenReturn(successResponse);

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient, times(4)).tryContribute(sessionId);
  }

  @Test
  public void testAnotherContributionInProgressError() {
    when(sequencerClient.getCeremonyStatus()).thenReturn(new CeremonyStatus(1, 10, "string"));

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(anotherContributionInProgressResponse)
        .thenReturn(successResponse);

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient).getCeremonyStatus();
    verify(sequencerClient, times(2)).tryContribute(sessionId);
  }

  @Test
  public void testQueryingCeremonyStatusFailureDoesNotStopTryingToContribute() {
    doThrow(new IllegalStateException("oopsy")).when(sequencerClient).getCeremonyStatus();

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(anotherContributionInProgressResponse)
        .thenReturn(successResponse);

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient).getCeremonyStatus();
    verify(sequencerClient, times(2)).tryContribute(sessionId);
  }

  @Test
  public void testContributionDoublesUpNextPeriodWhenRateLimitingErrorOccurs() {

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(rateLimitingResponse)
        .thenReturn(emptyResponse)
        .thenReturn(successResponse);

    long startCall = System.currentTimeMillis();

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    long endCall = System.currentTimeMillis();

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient, times(3)).tryContribute(sessionId);

    // 200 (2nd call) + 100 (3rd call)
    assertThat(endCall - startCall).isGreaterThanOrEqualTo(300);
  }

  @Test
  public void testContributionDoublesUpAttemptPeriodTwiceIfMoreThanOneRateLimitingErrorInARow() {

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(rateLimitingResponse)
        .thenReturn(rateLimitingResponse)
        .thenReturn(emptyResponse)
        .thenReturn(successResponse);

    long startCall = System.currentTimeMillis();

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    long endCall = System.currentTimeMillis();

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient, times(4)).tryContribute(sessionId);

    // 200 (2nd call) + 400 (2nd call) + 100 (3rd call)
    assertThat(endCall - startCall).isGreaterThanOrEqualTo(700);
  }

  @Test
  public void testContributionDoesNotDoublesUpAttemptPeriodTwiceIfRateLimitingErrorsAreNotInARow() {

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(rateLimitingResponse)
        .thenReturn(emptyResponse)
        .thenReturn(rateLimitingResponse)
        .thenReturn(successResponse);

    long startCall = System.currentTimeMillis();

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    long endCall = System.currentTimeMillis();

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient, times(4)).tryContribute(sessionId);

    // 200 (2nd call) + 100 (2nd call) + 200 (3rd call)
    assertThat(endCall - startCall).isGreaterThanOrEqualTo(500);
    assertThat(endCall - startCall).isLessThan(700);
  }

  @Test
  public void testContributionFailsIfThereIsUnknownSessionIdError() {
    final TryContributeResponse unknownSessionIdResponse =
        new TryContributeResponse(
            Optional.empty(),
            Optional.of(
                new SequencerError("TryContributeError::UnknownSessionId", "unknown session id")));

    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(emptyResponse)
        .thenReturn(unknownSessionIdResponse);

    final IllegalStateException exception =
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> contributeTrier.tryContributeUntilSuccess(sessionId));

    assertThat(exception)
        .hasMessage(
            "Unknown session id error was received from the sequencer. Try restarting the client and authenticating again.");

    verify(sequencerClient, times(2)).tryContribute(sessionId);
  }
}
