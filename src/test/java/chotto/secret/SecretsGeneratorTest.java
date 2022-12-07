package chotto.secret;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.CsprngStub;
import chotto.TestUtil;
import chotto.objects.Secret;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SecretsGeneratorTest {

  private final List<Secret> secrets = TestUtil.getTestSecrets();

  private final Csprng csprng = CsprngStub.fromPredefinedSecrets(secrets);

  private final SecretsGenerator secretsGenerator = new SecretsGenerator(csprng);

  @Test
  public void throwsIfTriesToGetBeforeGenerating() {
    final IllegalStateException exception =
        Assertions.assertThrows(IllegalStateException.class, secretsGenerator::getSecrets);
    assertThat(exception).hasMessage("Expected 4 secrets to have been generated but it was 0");
  }

  @Test
  public void generatesAndGetsSecrets() {
    secretsGenerator.generateSecrets();
    final List<Secret> secrets = secretsGenerator.getSecrets();
    assertThat(secrets).hasSize(4);
    assertThat(secrets).isEqualTo(secrets);
  }
}
