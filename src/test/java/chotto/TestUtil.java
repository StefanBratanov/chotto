package chotto;

import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import chotto.objects.Receipt;
import chotto.objects.Secret;
import chotto.secret.Csprng;
import chotto.secret.StdioCsprng;
import chotto.serialization.ChottoObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class TestUtil {

  private static final Csprng CSPRNG = new StdioCsprng(UUID.randomUUID().toString());

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  public static String readResource(final String resource) {
    try (final InputStream resourceIs = readResourceAsInputStream(resource)) {
      return new String(resourceIs.readAllBytes(), StandardCharsets.UTF_8);
    } catch (final IOException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }

  public static InputStream readResourceAsInputStream(final String resource) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
  }

  public static BatchContribution getInitialBatchContribution() {
    return getBatchContribution("initialContribution.json");
  }

  public static BatchContribution getBatchContribution(final String resource) {
    return getObjectFromResource(resource, BatchContribution.class);
  }

  public static BatchTranscript getBatchTranscript(final String resource) {
    return getObjectFromResource(resource, BatchTranscript.class);
  }

  public static Receipt getReceipt(final String resource) {
    return getObjectFromResource(resource, Receipt.class);
  }

  private static <T> T getObjectFromResource(final String resource, final Class<T> objectClass) {
    try (final InputStream inputStream = readResourceAsInputStream(resource)) {
      return OBJECT_MAPPER.readValue(inputStream, objectClass);
    } catch (final IOException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }

  public static List<Secret> getTestSecrets() {
    return List.of(
        Secret.fromText("foo"),
        Secret.fromText("bar"),
        Secret.fromText("danksharding"),
        Secret.fromText("devcon"));
  }

  public static Secret generateRandomSecret() {
    return CSPRNG.generateSecret();
  }
}
