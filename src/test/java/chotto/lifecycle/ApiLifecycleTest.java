package chotto.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import chotto.auth.Provider;
import chotto.auth.SessionInfo;
import chotto.contribution.Contributor;
import chotto.objects.BatchContribution;
import chotto.objects.Receipt;
import chotto.sequencer.SequencerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApiLifecycleTest {

  private final ContributeTrier contributeTrier = mock(ContributeTrier.class);

  private final SequencerClient sequencerClient = mock(SequencerClient.class);

  private final Contributor contributor = mock(Contributor.class);

  private final BatchContribution receivedContribution = mock(BatchContribution.class);

  private final BatchContribution updatedContribution = mock(BatchContribution.class);

  private final ObjectMapper objectMapper = mock(ObjectMapper.class);

  private final SessionInfo sessionInfo = new SessionInfo(Provider.ETHEREUM, "foobar", "123");

  @TempDir Path tempDir;

  private ApiLifecycle apiLifecycle;

  @BeforeEach
  public void setup() {
    when(contributeTrier.tryContributeUntilSuccess("123")).thenReturn(receivedContribution);

    apiLifecycle =
        new ApiLifecycle(
            sessionInfo, contributeTrier, sequencerClient, contributor, objectMapper, tempDir);
  }

  @Test
  public void testLifecycle() throws IOException {
    when(contributor.contribute(receivedContribution)).thenReturn(updatedContribution);

    final Receipt receipt = new Receipt("receipt", "12345");

    when(sequencerClient.contribute(updatedContribution, "123")).thenReturn(receipt);

    when(objectMapper.writeValueAsString(updatedContribution)).thenReturn("contribution123");
    when(objectMapper.writeValueAsString(receipt)).thenReturn("receipt12345");

    final Receipt result = apiLifecycle.runLifecycle();

    assertThat(tempDir).isNotEmptyDirectory();

    assertThat(tempDir.resolve("receipt-foobar.txt")).exists().hasContent("receipt12345");
    assertThat(tempDir.resolve("contribution-foobar.json")).exists().hasContent("contribution123");

    assertThat(result).isEqualTo(receipt);
  }

  @Test
  public void testAbortingContribution() {

    doThrow(new IllegalStateException("oopsy")).when(contributor).contribute(receivedContribution);

    final IllegalStateException exception =
        Assertions.assertThrows(IllegalStateException.class, apiLifecycle::runLifecycle);

    assertThat(exception).hasMessage("There was an error during contribution");

    verify(sequencerClient).abortContribution("123");

    verifyNoMoreInteractions(sequencerClient);

    assertThat(tempDir).isEmptyDirectory();
  }
}
