package chotto.secret;

import chotto.objects.Secret;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Csprng {

  private static final Logger LOG = LoggerFactory.getLogger(Csprng.class);

  private static final int SEED_LENGTH = 256;

  private final byte[] entropyEntry;

  private final Set<Secret> generatedSecrets = new HashSet<>();

  public Csprng(final String entropyEntry) {
    this.entropyEntry = entropyEntry.getBytes();
  }

  public Secret generateSecret() {
    final byte[] seed = Arrays.copyOf(entropyEntry, SEED_LENGTH);
    // replace half or more entries with random bytes to increase entropy
    final int leftBytesToFill = Math.max(SEED_LENGTH - entropyEntry.length, SEED_LENGTH / 2);
    final byte[] randomBytes = new byte[leftBytesToFill];
    new Random().nextBytes(randomBytes);
    System.arraycopy(randomBytes, 0, seed, SEED_LENGTH - leftBytesToFill, leftBytesToFill);
    // fromSeed is using the BLS KeyGen function to generate a secret
    final Secret secret = Secret.fromSeed(seed);
    if (generatedSecrets.contains(secret)) {
      LOG.warn("The generated secret has already been generated before. Will generate a new one.");
      return generateSecret();
    }
    generatedSecrets.add(secret);
    return secret;
  }
}
