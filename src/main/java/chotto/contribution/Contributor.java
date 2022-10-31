package chotto.contribution;

import chotto.Csprng;
import chotto.objects.BatchContribution;
import chotto.objects.BlsSignature;
import chotto.objects.Contribution;
import chotto.objects.Secret;
import chotto.signing.Signer;
import java.util.List;
import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Contributor {

  private static final Logger LOG = LoggerFactory.getLogger(Contributor.class);

  private final Csprng csprng;
  private final String identity;
  private final boolean signContributions;

  public Contributor(final Csprng csprng, final String identity, final boolean signContributions) {
    this.csprng = csprng;
    this.identity = identity;
    this.signContributions = signContributions;
  }

  public BatchContribution contribute(final BatchContribution batchContribution) {
    int index = 0;
    final List<Contribution> contributions = batchContribution.getContributions();
    for (Contribution contribution : contributions) {
      final Secret secret = csprng.generateSecret();
      final UInt256 secretNumber = secret.toUInt256();
      LOG.info("Updating contribution {}/{}", ++index, contributions.size());
      ContributionUpdater.updatePowersOfTau(contribution, secretNumber);
      LOG.info("Updated Powers of Tau");
      ContributionUpdater.updateWitness(contribution, secretNumber);
      LOG.info("Updated Witness");
      if (signContributions) {
        final BlsSignature signature = Signer.blsSign(secret, identity);
        contribution.setBlsSignature(signature);
        LOG.info("Signed contribution");
      } else {
        contribution.setBlsSignature(null);
        LOG.info("Skipped signing contribution");
      }
    }
    return batchContribution;
  }
}
