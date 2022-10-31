package chotto.cli;

import java.net.URL;
import java.util.Properties;
import picocli.CommandLine.IVersionProvider;

/**
 * {@link IVersionProvider} implementation that returns version information from a {@code
 * /version.txt} file in the classpath. Taken from
 * https://github.com/remkop/picocli/blob/main/picocli-examples/src/main/java/picocli/examples/VersionProviderDemo1.java
 */
public class PropertiesVersionProvider implements IVersionProvider {
  public String[] getVersion() throws Exception {
    final URL url = getClass().getResource("/version.txt");
    if (url == null) {
      return new String[] {"No version.txt file found in the classpath."};
    }
    final Properties properties = new Properties();
    properties.load(url.openStream());
    return new String[] {properties.getProperty("Version")};
  }
}
