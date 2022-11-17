package chotto;

import static chotto.Constants.CURVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

import chotto.objects.Secret;
import java.util.HashSet;
import java.util.Set;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

class CsprngTest {

  private final Csprng csprng = new Csprng("Danksharding");

  @Test
  void generatesRandomSecrets() {
    final Set<Secret> secrets = new HashSet<>();
    for (int i = 0; i < 1_000_000; i++) {
      secrets.add(csprng.generateSecret());
    }
    assertThat(secrets)
        .hasSize(1_000_000)
        .allSatisfy(
            secret -> assertThat(secret.toUInt256()).isStrictlyBetween(UInt256.ZERO, CURVE_ORDER));
  }
}
