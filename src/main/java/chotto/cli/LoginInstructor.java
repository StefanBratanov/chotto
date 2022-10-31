package chotto.cli;

import static com.pivovarit.function.ThrowingRunnable.unchecked;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.net.URI;
import java.util.concurrent.TimeUnit;

public class LoginInstructor {

  public static void instructUserToLogin(final String loginLink, final URI authCallbackEndpoint) {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
      System.out.printf(
          "A login window will open on your browser in a second. Follow the instructions to login. If you accidentally close the browser window before logging in, observe an error or you want to login from a different machine, you can use the link below.%n%n%s%n%n",
          loginLink);
      unchecked(
              () -> {
                TimeUnit.SECONDS.sleep(2);
                final Desktop desktop = Desktop.getDesktop();
                desktop.browse(new URI(loginLink));
              })
          .run();
    } else {
      if (authCallbackEndpoint == null) {
        throw new IllegalArgumentException(
            "--auth-callback-endpoint must be defined when a local browser is not supported");
      }
      System.out.printf(
          "Browsing is not supported on your machine. Copy the link below in browser of your choice on another machine and follow the login instructions.%n%n%s%n%n",
          loginLink);
    }
  }
}
