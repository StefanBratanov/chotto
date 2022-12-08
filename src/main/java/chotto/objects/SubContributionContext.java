package chotto.objects;

import java.util.Objects;
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

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SubContributionContext that = (SubContributionContext) o;
    return Objects.equals(secret, that.secret)
        && Objects.equals(blsSignatureMaybe, that.blsSignatureMaybe)
        && Objects.equals(potPubkey, that.potPubkey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(secret, blsSignatureMaybe, potPubkey);
  }
}
