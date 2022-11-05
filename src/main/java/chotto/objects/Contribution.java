package chotto.objects;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;

public class Contribution {

  private final int numG1Powers;
  private final int numG2Powers;
  private final PowersOfTau powersOfTau;
  private Optional<G2Point> potPubkey;
  private BlsSignature blsSignature;

  @JsonCreator
  public Contribution(
      @JsonProperty("numG1Powers") final int numG1Powers,
      @JsonProperty("numG2Powers") final int numG2Powers,
      @JsonProperty("powersOfTau") final PowersOfTau powersOfTau,
      @JsonProperty("potPubkey") final Optional<G2Point> potPubkey,
      @JsonProperty("bls_signature") @JsonAlias("blsSignature") final BlsSignature blsSignature) {
    this.numG1Powers = numG1Powers;
    this.numG2Powers = numG2Powers;
    this.powersOfTau = powersOfTau;
    this.potPubkey = potPubkey;
    this.blsSignature = blsSignature;
  }

  public int getNumG1Powers() {
    return numG1Powers;
  }

  public int getNumG2Powers() {
    return numG2Powers;
  }

  public PowersOfTau getPowersOfTau() {
    return powersOfTau;
  }

  public Optional<G2Point> getPotPubkey() {
    return potPubkey;
  }

  public BlsSignature getBlsSignature() {
    return blsSignature;
  }

  public void setPotPubkey(final Optional<G2Point> potPubkey) {
    this.potPubkey = potPubkey;
  }

  public void setBlsSignature(final BlsSignature blsSignature) {
    this.blsSignature = blsSignature;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final Contribution that = (Contribution) o;
    return Objects.equals(numG1Powers, that.numG1Powers)
        && Objects.equals(numG2Powers, that.numG2Powers)
        && Objects.equals(powersOfTau, that.powersOfTau)
        && Objects.equals(potPubkey, that.potPubkey)
        && Objects.equals(blsSignature, that.blsSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(numG1Powers, numG2Powers, powersOfTau, potPubkey, blsSignature);
  }
}
