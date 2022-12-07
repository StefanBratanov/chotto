package chotto;

import chotto.objects.BatchContribution;
import chotto.objects.BatchTranscript;
import chotto.objects.Secret;
import chotto.secret.Csprng;
import chotto.serialization.ChottoObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class TestUtil {

  private static final Csprng CSPRNG = new Csprng(UUID.randomUUID().toString());

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
    final InputStream contributionIs = readResourceAsInputStream(resource);

    try {
      return ChottoObjectMapper.getInstance().readValue(contributionIs, BatchContribution.class);
    } catch (final IOException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }

  public static BatchTranscript getBatchTranscript(final String resource) {
    final InputStream transcriptIs = readResourceAsInputStream(resource);

    try {
      return ChottoObjectMapper.getInstance().readValue(transcriptIs, BatchTranscript.class);
    } catch (final IOException ioex) {
      throw new UncheckedIOException(ioex);
    }
  }

  public static Path findSavedTranscriptFile(final Path outputDirectory) {
    try (final Stream<Path> outputDirectoryFiles = Files.list(outputDirectory)) {
      return outputDirectoryFiles
          .filter(path -> path.getFileName().toString().startsWith("transcript-"))
          .findFirst()
          .orElseThrow(
              () -> new IllegalStateException("Transcript file not found in " + outputDirectory));
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
