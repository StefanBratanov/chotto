package chotto.secret;

import chotto.objects.Secret;
import java.util.Arrays;

public class StdioCsprng implements Csprng {

  private static final int SEED_LENGTH = 256;

  private final byte[] entropy;

  public StdioCsprng(final String entropy) {
    this.entropy = entropy.getBytes();
  }

  @Override
  public Secret generateSecret() {
    final byte[] seed = Arrays.copyOf(entropy, SEED_LENGTH);
    // replace half or more entries with random bytes to increase entropy
    final int leftBytesToFill = Math.max(SEED_LENGTH - entropy.length, SEED_LENGTH / 2);
    final byte[] randomBytes = new byte[leftBytesToFill];
    SECURE_RANDOM.nextBytes(randomBytes);
    System.arraycopy(randomBytes, 0, seed, SEED_LENGTH - leftBytesToFill, leftBytesToFill);
    // fromSeed is using the BLS KeyGen function to generate a secret
    return Secret.fromSeed(seed);
  }
}
