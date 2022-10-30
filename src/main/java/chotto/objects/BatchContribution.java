package chotto.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;

public class BatchContribution {

  private final List<Contribution> contributions;

  @JsonCreator
  public BatchContribution(@JsonProperty("contributions") final List<Contribution> contributions) {
    this.contributions = contributions;
  }

  public List<Contribution> getContributions() {
    return contributions;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final BatchContribution that = (BatchContribution) o;
    return Objects.equals(contributions, that.contributions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contributions);
  }
}
