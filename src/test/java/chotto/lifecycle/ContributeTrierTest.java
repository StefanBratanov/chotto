package chotto.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import chotto.objects.BatchContribution;
import chotto.sequencer.SequencerClient;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ContributeTrierTest {

  private final SequencerClient sequencerClient = mock(SequencerClient.class);

  private final BatchContribution receivedContribution = mock(BatchContribution.class);

  private final ContributeTrier contributeTrier = new ContributeTrier(sequencerClient, 1);

  @Test
  public void testContributingUntilSuccess() {
    final String sessionId = "123";
    when(sequencerClient.tryContribute(sessionId))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(receivedContribution));

    final BatchContribution result = contributeTrier.tryContributeUntilSuccess(sessionId);

    assertThat(result).isEqualTo(receivedContribution);

    verify(sequencerClient, times(2)).tryContribute(sessionId);
  }
}
