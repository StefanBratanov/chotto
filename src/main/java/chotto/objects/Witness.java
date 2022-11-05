package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Witness {

  private final List<G1Point> runningProducts;
  private final List<G2Point> potPubkeys;
  private final List<BlsSignature> blsSignatures;

  @JsonCreator
  public Witness(
      @JsonProperty("runningProducts") final List<G1Point> runningProducts,
      @JsonProperty("potPubkeys") final List<G2Point> potPubkeys,
      @JsonProperty("blsSignatures") final List<BlsSignature> blsSignatures) {
    this.runningProducts = runningProducts;
    this.potPubkeys = potPubkeys;
    this.blsSignatures = blsSignatures;
  }

  public List<G1Point> getRunningProducts() {
    return runningProducts;
  }

  public List<G2Point> getPotPubkeys() {
    return potPubkeys;
  }

  public List<BlsSignature> getBlsSignatures() {
    return blsSignatures;
  }
}
