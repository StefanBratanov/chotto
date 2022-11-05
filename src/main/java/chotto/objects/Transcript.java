package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Transcript {

  private final int numG1Powers;
  private final int numG2Powers;
  private final PowersOfTau powersOfTau;
  private final Witness witness;

  @JsonCreator
  public Transcript(
      @JsonProperty("numG1Powers") final int numG1Powers,
      @JsonProperty("numG2Powers") final int numG2Powers,
      @JsonProperty("powersOfTau") final PowersOfTau powersOfTau,
      @JsonProperty("witness") final Witness witness) {
    this.numG1Powers = numG1Powers;
    this.numG2Powers = numG2Powers;
    this.powersOfTau = powersOfTau;
    this.witness = witness;
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

  public Witness getWitness() {
    return witness;
  }
}
