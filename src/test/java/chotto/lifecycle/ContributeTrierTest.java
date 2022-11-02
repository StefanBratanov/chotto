package chotto.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import chotto.objects.BatchContribution;
import chotto.objects.SequencerError;
import chotto.sequencer.SequencerClient;
import chotto.sequencer.TryContributeResponse;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ContributeTrierTest {

  private final String sessionId = "123";

  private final SequencerClient sequencerClient = mock(SequencerClient.class);

  private final BatchContribution receivedContribution = mock(BatchContribution.class);

  private final TryContributeResponse successResponse =
      new TryContributeResponse(Optional.of(receivedContribution), Optional.empty());

  final TryContributeResponse emptyResponse =
      new TryContributeResponse(Optional.empty(), Optional.empty());

  final TryContributeResponse rateLimitingResponse =
      new TryContributeResponse(
          Optional.empty(),
          Optional.of(
              new SequencerError(
                  "TryContributeError::RateLimited", "call came too early. rate limited")));

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
}
