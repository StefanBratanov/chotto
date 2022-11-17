package chotto;

import static chotto.Constants.CURVE_ORDER;
import static org.assertj.core.api.Assertions.assertThat;

import chotto.objects.Secret;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.LongStream;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Test;

class CsprngTest {

  private final Csprng csprng = new Csprng("Danksharding");

  @Test
  void generatesRandomSecrets() {
    final Set<Secret> secrets = new HashSet<>();
    final LongStream.Builder performanceTimes = LongStream.builder();
    for (int i = 0; i < 1_000_000; i++) {
      final long start = getCurrentTimeInMicroseconds();
      final Secret secret = csprng.generateSecret();
      final long end = getCurrentTimeInMicroseconds();
      secrets.add(secret);
      performanceTimes.add(end - start);
    }
    assertThat(secrets)
        .hasSize(1_000_000)
        .allSatisfy(
            secret -> assertThat(secret.toUInt256()).isStrictlyBetween(UInt256.ZERO, CURVE_ORDER));
    // performance check
    final double averageTime = performanceTimes.build().average().orElseThrow();
    assertThat(averageTime).isBetween(0.0, 50.0);
  }

  private long getCurrentTimeInMicroseconds() {
    return System.nanoTime() / 1000;
  }
}
