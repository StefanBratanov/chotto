package chotto.cli;

import chotto.objects.CeremonyStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class AsciiArtHelper {

  private static final AtomicReference<String> CACHED_BANNER = new AtomicReference<>();
  private static final AtomicReference<String> CACHED_CANDLE = new AtomicReference<>();

  public static String getBanner() {
    return Optional.ofNullable(CACHED_BANNER.get())
        .orElseGet(
            () -> {
              final String banner = readResource("banner.txt");
              CACHED_BANNER.set(banner);
              return banner;
            });
  }

  public static void printBannerOnStartup() {
    System.out.printf(
        "%n%s%n%n%s%n%n",
        getBanner(), "Ethereum's Power of Tau client implementation written in Java");
  }

  public static void printCeremonyStatus(final CeremonyStatus ceremonyStatus) {
    final String statusText =
        String.format(
            "lobby size = %d, contributions = %d, sequencer address = %s",
            ceremonyStatus.getLobbySize(),
            ceremonyStatus.getNumContributions(),
            ceremonyStatus.getSequencerAddress());
    printCeremonyCandleWithText(statusText);
  }

  public static void printCeremonySummoning(final String nickname) {
    final String summoningText =
        String.format(
            "You (%s) have been summoned to take part in the Ethereum KZG ceremony", nickname);
    printCeremonyCandleWithText(summoningText);
  }

  public static void printThankYou() {
    printCeremonyCandleWithText("Thank you for your contribution!!!");
  }

  private static void printCeremonyCandleWithText(final String text) {
    System.out.printf("%n" + getCeremonyCandle() + "%n%n", text);
  }

  private static String getCeremonyCandle() {
    return Optional.ofNullable(CACHED_CANDLE.get())
        .orElseGet(
            () -> {
              final String candle = readResource("ceremony.txt");
              CACHED_CANDLE.set(candle);
              return candle;
            });
  }

  private static String readResource(final String resource) {
    final InputStream inputStream =
        Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    try {
      return new String(Objects.requireNonNull(inputStream).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
