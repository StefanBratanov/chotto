package chotto.contribution;

import chotto.objects.BatchContribution;
import chotto.objects.Contribution;
import chotto.objects.SubContributionContext;
import java.util.List;
import java.util.Optional;
import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Contributor {

  private static final Logger LOG = LoggerFactory.getLogger(Contributor.class);

  private final SubContributionManager subContributionManager;
  private final Optional<String> ecdsaSignatureMaybe;

  public Contributor(
      final SubContributionManager subContributionManager,
      final Optional<String> ecdsaSignatureMaybe) {
    this.subContributionManager = subContributionManager;
    this.ecdsaSignatureMaybe = ecdsaSignatureMaybe;
  }

  public BatchContribution contribute(final BatchContribution batchContribution) {
    int index = 0;
    final List<SubContributionContext> subContributionContexts =
        subContributionManager.getContexts();
    final List<Contribution> contributions = batchContribution.getContributions();
    for (final Contribution contribution : contributions) {
      final SubContributionContext subContributionContext = subContributionContexts.get(index);
      final UInt256 secret = subContributionContext.getSecret().toUInt256();
      LOG.info("Updating sub-contribution {}/{}", ++index, contributions.size());
      ContributionUpdater.updatePowersOfTau(contribution, secret);
      LOG.info("Updated Powers of Tau");
      contribution.setPotPubkey(subContributionContext.getPotPubkey());
      LOG.info("Updated Witness");
      subContributionContext
          .getBlsSignatureMaybe()
          .ifPresentOrElse(
              blsSignature -> {
                contribution.setBlsSignature(blsSignature);
                LOG.info("Signed the sub-contribution with a BLS signature");
              },
              () -> {
                contribution.setBlsSignature(null);
                LOG.info("Skipped signing the sub-contribution with a BLS signature");
              });
    }
    ecdsaSignatureMaybe.ifPresentOrElse(
        ecdsaSignature -> {
          batchContribution.setEcdsaSignature(ecdsaSignature);
          LOG.info("Signed the contribution with an ECDSA Signature");
        },
        () -> {
          batchContribution.setEcdsaSignature(null);
          LOG.info("Skipped signing the contribution with an ECDSA Signature");
        });

    return batchContribution;
  }
}
