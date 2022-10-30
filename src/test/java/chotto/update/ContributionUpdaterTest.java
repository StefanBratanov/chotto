package chotto.update;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.serialization.ChottoObjectMapper;
import chotto.verification.ContributionVerification;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContributionUpdaterTest {

  private final ContributionVerification contributionVerification =
      new ContributionVerification(ChottoObjectMapper.getInstance());

  @Test
  public void updatesPowersOfTau() {
    final BatchContribution batchContribution = TestUtil.getInitialBatchContribution();

    batchContribution
        .getContributions()
        .forEach(
            contribution -> {
              final int hashCodeBeforeUpdate = contribution.getPowersOfTau().hashCode();
              ContributionUpdater.updatePowersOfTau(
                  contribution, TestUtil.generateRandomSecret().toUInt256());
              // check PowersOfTau is updated
              assertThat(contribution.getPowersOfTau().hashCode())
                  .isNotEqualTo(hashCodeBeforeUpdate);
            });

    // contribution validity check
    assertThat(contributionVerification.subgroupChecks(batchContribution)).isTrue();
  }
}
