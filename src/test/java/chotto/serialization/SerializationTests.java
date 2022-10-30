package chotto.serialization;

import chotto.TestUtil;
import chotto.objects.Contribution;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class SerializationTests {

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  @Test
  public void deserializesAndSerializesContributions() throws IOException {
    final InputStream contributionIs =
        TestUtil.readResourceAsInputStream("initialContribution.json");

    final JsonNode contributions = OBJECT_MAPPER.readTree(contributionIs).get("contributions");

    contributions.forEach(
        contributionJson -> {
          try {
            final Contribution contribution = TestUtil.deserializeContribution(contributionJson);
            final String serializedContribution = OBJECT_MAPPER.writeValueAsString(contribution);
            JSONAssert.assertEquals(contributionJson.toString(), serializedContribution, true);
          } catch (IOException | JSONException ex) {
            Assertions.fail(ex);
          }
        });
  }
}
