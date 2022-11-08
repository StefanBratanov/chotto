package chotto.cli;

import static com.pivovarit.function.ThrowingRunnable.unchecked;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class CliInstructor {

  public static void instructUserToLogin(
      final String loginLink, final boolean callbackEndpointIsDefined) {
    if (canOpenBrowserOnThisMachine()) {
      System.out.printf(
          "A login window will open on your browser in a second. Follow the instructions to login. If you accidentally close the browser window before logging in, observe an error or you want to login from a different machine, you can use the link below.%n%n%s%n%n",
          loginLink);
      openLinkInBrowser(loginLink);
    } else {
      if (!callbackEndpointIsDefined) {
        throwCallbackEndpointMustBeDefinedException();
      }
      System.out.printf(
          "Browsing is not supported on your machine. Copy the link below in browser of your choice on another machine and follow the login instructions.%n%n%s%n%n",
          loginLink);
    }
  }

  public static void instructUserToSignContribution(
      final String signContributionLink, final boolean callbackEndpointIsDefined) {
    if (canOpenBrowserOnThisMachine()) {
      System.out.printf(
          "A sign window will open on your browser in a second. Follow the instructions to sign your contribution. If you accidentally close the browser window before signing, observe an error or you want to sign from a different machine, you can use the link below.%n%n%s%n%n",
          signContributionLink);
      openLinkInBrowser(signContributionLink);
    } else {
      if (!callbackEndpointIsDefined) {
        throwCallbackEndpointMustBeDefinedException();
      }
      System.out.printf(
          "Browsing is not supported on your machine. Copy the link below in browser of your choice on another machine and follow the sign instructions.%n%n%s%n%n",
          signContributionLink);
    }
  }

  private static boolean canOpenBrowserOnThisMachine() {
    return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE);
  }

  private static void openLinkInBrowser(final String link) {
    unchecked(
            () -> {
              TimeUnit.SECONDS.sleep(1);
              final Desktop desktop = Desktop.getDesktop();
              desktop.browse(new URI(link));
            })
        .run();
  }

  private static void throwCallbackEndpointMustBeDefinedException() {
    throw new IllegalStateException(
        "--callback-endpoint must be defined when a local browser is not supported");
  }
}
