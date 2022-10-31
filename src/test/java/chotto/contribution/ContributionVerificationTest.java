package chotto.contribution;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.serialization.ChottoObjectMapper;
import org.junit.jupiter.api.Test;

class ContributionVerificationTest {

  private final ContributionVerification contributionVerification =
      new ContributionVerification(ChottoObjectMapper.getInstance());

  @Test
  public void checksSchema() {
    final String contribution = TestUtil.readResource("initialContribution.json");

    assertThat(contributionVerification.schemaCheck(contribution)).isTrue();
  }

  @Test
  public void checksSubgroups() {
    final BatchContribution batchContribution = TestUtil.getInitialBatchContribution();

    assertThat(contributionVerification.subgroupChecks(batchContribution)).isTrue();
  }
}
