package chotto.contribution;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.objects.G2Point;
import chotto.objects.Secret;
import chotto.serialization.ChottoObjectMapper;
import org.junit.jupiter.api.Test;

class ContributionUpdaterTest {

  private final ContributionVerification contributionVerification =
      new ContributionVerification(ChottoObjectMapper.getInstance());

  @Test
  public void updatesContribution() {
    final BatchContribution batchContribution = TestUtil.getInitialBatchContribution();

    batchContribution
        .getContributions()
        .forEach(
            contribution -> {
              final Secret secret = TestUtil.generateRandomSecret();
              final int powersOfTauHashCodeBeforeUpdate = contribution.getPowersOfTau().hashCode();
              final G2Point potPubkeyBeforeUpdate = contribution.getPotPubkey();
              ContributionUpdater.updatePowersOfTau(contribution, secret.toUInt256());
              // check PowersOfTau is updated
              assertThat(contribution.getPowersOfTau().hashCode())
                  .isNotEqualTo(powersOfTauHashCodeBeforeUpdate);
              ContributionUpdater.updateWitness(contribution, secret.toUInt256());
              // check potPubkey is updated
              assertThat(contribution.getPotPubkey()).isNotEqualTo(potPubkeyBeforeUpdate);
            });

    // contribution validity check
    assertThat(contributionVerification.subgroupChecks(batchContribution)).isTrue();
  }
}
