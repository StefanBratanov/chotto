package chotto.secret;

import static chotto.Constants.CURVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

import chotto.objects.Secret;
import java.util.HashMap;
import java.util.Map;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

class CsprngTest {

  private final Csprng csprng = new Csprng("Danksharding");

  @Test
  void generatesRandomSecrets() {
    final Map<Secret, Long> performanceTimes = new HashMap<>();

    final int numberOfIterations = 1_000_000;

    for (int i = 0; i < numberOfIterations; i++) {
      final long start = getCurrentTimeInMicroseconds();
      final Secret secret = csprng.generateSecret();
      final long end = getCurrentTimeInMicroseconds();
      assertThat(secret.toUInt256()).isStrictlyBetween(UInt256.ZERO, CURVE_ORDER);
      performanceTimes.put(secret, end - start);
    }

    assertThat(performanceTimes).hasSize(numberOfIterations);
    // performance check
    final double averageTime =
        performanceTimes.values().stream().mapToLong(Long::longValue).average().orElseThrow();
    assertThat(averageTime).isBetween(0.0, 20.0);
  }

  private long getCurrentTimeInMicroseconds() {
    return System.nanoTime() / 1000;
  }
}
