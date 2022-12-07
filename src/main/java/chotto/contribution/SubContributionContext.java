package chotto.contribution;

import chotto.objects.BlsSignature;
import chotto.objects.G2Point;
import chotto.objects.Secret;
import java.util.Optional;

public class SubContributionContext {

  private final Secret secret;
  private final Optional<BlsSignature> blsSignatureMaybe;
  private final G2Point potPubkey;

  public SubContributionContext(
      final Secret secret,
      final Optional<BlsSignature> blsSignatureMaybe,
      final G2Point potPubkey) {
    this.secret = secret;
    this.blsSignatureMaybe = blsSignatureMaybe;
    this.potPubkey = potPubkey;
  }

  public Secret getSecret() {
    return secret;
  }

  public Optional<BlsSignature> getBlsSignatureMaybe() {
    return blsSignatureMaybe;
  }

  public G2Point getPotPubkey() {
    return potPubkey;
  }
}
