package chotto;

import java.util.HashSet;
import java.util.Set;

import chotto.objects.Secret;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

import static chotto.Constants.CURVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

class CsprngTest {

  private final Csprng csprng = new Csprng("Danksharding");

  @Test
  void generatesRandomSecrets() {
    final Set<Secret> secrets = new HashSet<>();
    for (int i = 0; i < 1000000; i++) {
      secrets.add(csprng.generateSecret());
    }
    assertThat(secrets)
        .hasSize(1000000)
        .allSatisfy(
            secret -> assertThat(secret.toUInt256()).isStrictlyBetween(UInt256.ZERO, CURVE_ORDER));
  }
}
