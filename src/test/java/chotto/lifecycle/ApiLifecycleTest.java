package chotto.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import chotto.auth.Provider;
import chotto.auth.SessionInfo;
import chotto.contribution.Contributor;
import chotto.objects.BatchContribution;
import chotto.objects.Receipt;
import chotto.sequencer.SequencerClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ApiLifecycleTest {

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
    when(sequencerClient.tryContribute("123"))
        .thenReturn(Optional.empty())
        .thenReturn(Optional.of(receivedContribution));

    apiLifecycle =
        new ApiLifecycle(sessionInfo, sequencerClient, 1, contributor, objectMapper, tempDir);
  }

  @Test
  public void testLifecycle() throws JsonProcessingException {
    when(contributor.contribute(receivedContribution)).thenReturn(updatedContribution);

    final Receipt receipt = new Receipt("receipt123", "12345");

    when(sequencerClient.contribute(updatedContribution, "123")).thenReturn(receipt);

    when(objectMapper.writeValueAsString(updatedContribution)).thenReturn("contribution123");

    apiLifecycle.runLifecycle();

    assertThat(tempDir).isNotEmptyDirectory();

    assertThat(tempDir.resolve("receipt-foobar.txt")).exists().hasContent("receipt123");

    assertThat(tempDir.resolve("contribution-foobar.json")).exists().hasContent("contribution123");
  }

  @Test
  public void testAbortingContribution() {

    doThrow(new IllegalStateException("oopsy")).when(contributor).contribute(receivedContribution);

    final IllegalStateException exception =
        Assertions.assertThrows(IllegalStateException.class, apiLifecycle::runLifecycle);

    assertThat(exception).hasMessage("There was an error during contribution");

    verify(sequencerClient).abortContribution("123");
    verify(sequencerClient, times(0)).contribute(any(), any());

    assertThat(tempDir).isEmptyDirectory();
  }
}
