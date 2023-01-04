package chotto.contribution;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.objects.Secret;
import chotto.serialization.ChottoObjectMapper;
import chotto.verification.ContributionVerification;
import org.junit.jupiter.api.Test;

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
              final Secret secret = TestUtil.generateRandomSecret();
              final int powersOfTauHashCodeBeforeUpdate = contribution.getPowersOfTau().hashCode();
              ContributionUpdater.updatePowersOfTau(contribution, secret.toUInt256());
              // check PowersOfTau is updated
              assertThat(contribution.getPowersOfTau().hashCode())
                  .isNotEqualTo(powersOfTauHashCodeBeforeUpdate);
            });

    // contribution validity check
    assertThat(contributionVerification.pointChecks(batchContribution)).isTrue();
  }
}
