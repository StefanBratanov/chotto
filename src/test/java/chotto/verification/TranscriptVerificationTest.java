package chotto.verification;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.TestUtil;
import chotto.objects.BatchTranscript;
import chotto.serialization.ChottoObjectMapper;
import org.junit.jupiter.api.Test;

class TranscriptVerificationTest {

  private final TranscriptVerification transcriptVerification =
      new TranscriptVerification(ChottoObjectMapper.getInstance());

  @Test
  public void checksSchema() {
    final String transcript = TestUtil.readResource("initialTranscript.json");

    assertThat(transcriptVerification.schemaCheck(transcript)).isTrue();
  }

  @Test
  public void checksSubgroups() {
    final BatchTranscript batchTranscript = TestUtil.getBatchTranscript("initialTranscript.json");

    assertThat(transcriptVerification.pointChecks(batchTranscript)).isTrue();
  }
}
