package chotto.cli;

import chotto.objects.CeremonyStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AsciiArtHelper {

  private static final String BANNER = readResource("banner.txt");
  private static final String CEREMONY_CANDLE = readResource("ceremony.txt");

  public static String getBanner() {
    return BANNER;
  }

  public static void printBannerOnStartup() {
    System.out.printf(
        "%n%s%n%n%s%n%n", BANNER, "Ethereum's Power of Tau client implementation written in Java");
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
    System.out.printf("%n" + CEREMONY_CANDLE + "%n%n", text);
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
