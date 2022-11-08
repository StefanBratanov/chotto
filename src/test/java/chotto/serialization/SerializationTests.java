package chotto.serialization;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class SerializationTests {

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  @ParameterizedTest
  @ValueSource(
      strings = {
        "initialContribution.json",
        "integration/contribution.json",
        "contributionLocalSequencer.json",
        "updatedContribution.json",
        "updatedContributionNoBlsNoEcdsa.json",
        "updatedContributionNoEcdsa.json"
      })
  public void deserializesAndSerializesContributions(final String contributionResource)
      throws IOException, JSONException {
    final String initialContributionJson = TestUtil.readResource(contributionResource);

    final BatchContribution batchContribution =
        OBJECT_MAPPER.readValue(initialContributionJson, BatchContribution.class);

    final String serializedBatchContribution = OBJECT_MAPPER.writeValueAsString(batchContribution);

    JSONAssert.assertEquals(
        initialContributionJson, serializedBatchContribution, JSONCompareMode.STRICT_ORDER);
  }

  @ParameterizedTest
  @ValueSource(strings = {"initialTranscript.json", "integration/transcript.json"})
  public void deserializesAndSerializesTranscript(final String transcriptResource)
      throws JsonProcessingException, JSONException {
    final String transcriptJson = TestUtil.readResource(transcriptResource);

    final BatchTranscript batchTranscript =
        OBJECT_MAPPER.readValue(transcriptJson, BatchTranscript.class);

    final String serializedTranscript = OBJECT_MAPPER.writeValueAsString(batchTranscript);

    JSONAssert.assertEquals(transcriptJson, serializedTranscript, true);
  }
}
