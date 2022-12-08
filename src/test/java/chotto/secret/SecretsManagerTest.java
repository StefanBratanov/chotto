package chotto.secret;

import static chotto.Constants.NUMBER_OF_SECRETS;
import static org.assertj.core.api.Assertions.assertThat;

import chotto.objects.Secret;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SecretsManagerTest {

  private final Csprng csprng = new Csprng("Danksharding");

  private final SecretsManager secretsManager = new SecretsManager(csprng);

  @Test
  public void throwsIfTriesToGetBeforeGenerating() {
    final IllegalStateException exception =
        Assertions.assertThrows(IllegalStateException.class, secretsManager::getSecrets);
    assertThat(exception).hasMessage("Expected 4 secrets to have been generated but it was 0");
  }

  @Test
  public void generatesAndGetsSecrets() {
    secretsManager.generateSecrets();
    final List<Secret> secrets = secretsManager.getSecrets();
    assertThat(secrets).hasSize(NUMBER_OF_SECRETS);
    assertThat(secrets).allMatch(Objects::nonNull).doesNotHaveDuplicates();
  }
}
