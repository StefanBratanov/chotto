package chotto.serialization;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class SerializationTests {

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  @Test
  public void deserializesAndSerializesContributions() throws IOException, JSONException {
    final String initialContributionJson = TestUtil.readResource("initialContribution.json");

    final BatchContribution batchContribution =
        OBJECT_MAPPER.readValue(initialContributionJson, BatchContribution.class);

    final String serializedBatchContribution = OBJECT_MAPPER.writeValueAsString(batchContribution);

    JSONAssert.assertEquals(
        initialContributionJson, serializedBatchContribution, JSONCompareMode.STRICT_ORDER);
  }

  @Test
  public void deserializesContributionFromLocalSequencer() throws JsonProcessingException {
    final String localSequencerContributionJson =
        TestUtil.readResource("contributionLocalSequencer.json");

    final BatchContribution batchContribution =
        OBJECT_MAPPER.readValue(localSequencerContributionJson, BatchContribution.class);

    batchContribution
        .getContributions()
        .forEach(contribution -> assertThat(contribution.getBlsSignature()).isNull());
  }

  @Test
  public void deserializesAndSerializesTranscript() throws JsonProcessingException, JSONException {
    final String transcriptJson = TestUtil.readResource("initialTranscript.json");

    final BatchTranscript batchTranscript =
        OBJECT_MAPPER.readValue(transcriptJson, BatchTranscript.class);

    final String serializedTranscript = OBJECT_MAPPER.writeValueAsString(batchTranscript);

    JSONAssert.assertEquals(transcriptJson, serializedTranscript, false);
  }
}
