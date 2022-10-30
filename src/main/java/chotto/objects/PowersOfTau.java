package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

public class PowersOfTau {

  private final G1Point[] g1Powers;
  private final G2Point[] g2Powers;

  @JsonCreator
  public PowersOfTau(
      @JsonProperty("G1Powers") final G1Point[] g1Powers,
      @JsonProperty("G2Powers") final G2Point[] g2Powers) {
    this.g1Powers = g1Powers;
    this.g2Powers = g2Powers;
  }

  @JsonProperty("G1Powers")
  public G1Point[] getG1Powers() {
    return g1Powers;
  }

  @JsonProperty("G2Powers")
  public G2Point[] getG2Powers() {
    return g2Powers;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final PowersOfTau that = (PowersOfTau) o;
    return Arrays.equals(g1Powers, that.g1Powers) && Arrays.equals(g2Powers, that.g2Powers);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(g1Powers);
    result = 31 * result + Arrays.hashCode(g2Powers);
    return result;
  }
}
