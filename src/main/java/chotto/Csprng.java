package chotto;

import chotto.objects.Secret;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Csprng {

  private static final Logger LOG = LoggerFactory.getLogger(Csprng.class);

  private final byte[] entropyEntry;

  private final Set<Secret> generatedSecrets = new HashSet<>();

  public Csprng(final String entropyEntry) {
    this.entropyEntry = entropyEntry.getBytes();
  }

  public Secret generateSecret() {
    final byte[] seed = Arrays.copyOf(entropyEntry, 64);
    // replace half or more entries with random bytes to increase entropy
    final int leftBytesToFill = Math.max(seed.length - entropyEntry.length, 32);
    final byte[] randomBytes = new byte[leftBytesToFill];
    new Random().nextBytes(randomBytes);
    System.arraycopy(randomBytes, 0, seed, 64 - leftBytesToFill, leftBytesToFill);
    final Secret secret = Secret.fromSeed(seed);
    if (generatedSecrets.contains(secret)) {
      LOG.warn("The generated secret has already been generated before. Will generate a new one.");
      return generateSecret();
    }
    generatedSecrets.add(secret);
    return secret;
  }

  public void destroySecrets() {
    generatedSecrets.clear();
  }
}
