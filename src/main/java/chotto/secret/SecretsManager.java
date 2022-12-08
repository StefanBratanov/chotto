package chotto.secret;

import static chotto.Constants.NUMBER_OF_SECRETS;

import chotto.objects.Secret;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public class SecretsManager {

  private final List<Secret> secrets = new LinkedList<>();

  private final Csprng csprng;

  public SecretsManager(final Csprng csprng) {
    this.csprng = csprng;
  }

  public void generateSecrets() {
    IntStream.range(0, NUMBER_OF_SECRETS).forEach(__ -> secrets.add(csprng.generateSecret()));
  }

  public List<Secret> getSecrets() {
    if (secrets.size() != NUMBER_OF_SECRETS) {
      throw new IllegalStateException(
          String.format(
              "Expected %d secrets to have been generated but it was %d",
              NUMBER_OF_SECRETS, secrets.size()));
    }
    return secrets;
  }
}
