package chotto;

import chotto.objects.BatchContribution;
import chotto.objects.Contribution;
import chotto.objects.Secret;
import chotto.serialization.ChottoObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class TestUtil {

  private static final Csprng CSPRNG = new Csprng(UUID.randomUUID().toString());

  public static String readResource(final String resource) {
    try {
      return new String(readResourceAsInputStream(resource).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }

  public static InputStream readResourceAsInputStream(final String resource) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
  }

  public static BatchContribution getInitialBatchContribution() {
    final InputStream contributionIs = readResourceAsInputStream("initialContribution.json");

    try {
      return ChottoObjectMapper.getInstance().readValue(contributionIs, BatchContribution.class);
    } catch (IOException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }

  public static Secret generateRandomSecret() {
    return CSPRNG.generateSecret();
  }

  public static Contribution deserializeContribution(final JsonNode contributionJson) {
    try {
      return ChottoObjectMapper.getInstance().treeToValue(contributionJson, Contribution.class);
    } catch (JsonProcessingException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }
}
