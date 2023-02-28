package chotto.secret;

import java.util.Arrays;

import chotto.objects.Secret;

public class StdioCsprng implements Csprng {

  private static final int IKM_LENGTH = 256;

  private final byte[] entropy;

  public StdioCsprng(final String entropy) {
    this.entropy = entropy.getBytes();
  }

  @Override
  public Secret generateSecret() {
    final byte[] ikm = Arrays.copyOf(entropy, IKM_LENGTH);
    // replace half or more entries with random bytes to increase entropy
    final int leftBytesToFill = Math.max(IKM_LENGTH - entropy.length, IKM_LENGTH / 2);
    final byte[] randomBytes = new byte[leftBytesToFill];
    SECURE_RANDOM.nextBytes(randomBytes);
    System.arraycopy(randomBytes, 0, ikm, IKM_LENGTH - leftBytesToFill, leftBytesToFill);
    // fromIkm is using a BLS KeyGen(IKM) function to generate a secret
    return Secret.fromIkm(ikm);
  }
}
