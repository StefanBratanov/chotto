package chotto.cli;

import chotto.objects.CeremonyStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class AsciiArtPrinter {

  public static void printBanner() {
    System.out.printf("%n%s%n", readResource("banner.txt"));
  }

  public static void printCeremonyStatus(final CeremonyStatus ceremonyStatus) {
    final String statusText =
        String.format(
            "lobby size = %d, contributions = %d, sequencer address = %s",
            ceremonyStatus.getLobbySize(),
            ceremonyStatus.getNumContributions(),
            ceremonyStatus.getSequencerAddress());
    printCeremonyCandlesWithText(statusText);
  }

  public static void printCeremonySummoning(final String nickname) {
    final String summoningText =
        String.format(
            "You (%s) have been summoned to take part in the Ethereum KZG ceremony", nickname);
    printCeremonyCandlesWithText(summoningText);
  }

  public static void printThankYou() {
    printCeremonyCandlesWithText("Thank you for your contribution!!!");
  }

  private static void printCeremonyCandlesWithText(final String text) {
    System.out.printf("%n" + readResource("ceremony.txt") + "%n%n", text);
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
