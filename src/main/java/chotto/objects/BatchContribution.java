package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public class BatchContribution {

  private final List<Contribution> contributions;
  private String ecdsaSignature;

  @JsonCreator
  public BatchContribution(
      @JsonProperty("contributions") final List<Contribution> contributions,
      @JsonProperty("ecdsaSignature") final String ecdsaSignature) {
    this.contributions = contributions;
    this.ecdsaSignature = ecdsaSignature;
  }

  public List<Contribution> getContributions() {
    return contributions;
  }

  public String getEcdsaSignature() {
    return ecdsaSignature;
  }

  public void setEcdsaSignature(final String ecdsaSignature) {
    this.ecdsaSignature = ecdsaSignature;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final BatchContribution that = (BatchContribution) o;
    return Objects.equals(contributions, that.contributions)
        && Objects.equals(ecdsaSignature, that.ecdsaSignature);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contributions, ecdsaSignature);
  }
}
