package chotto.contribution;

import static chotto.auth.Provider.ETHEREUM;

import chotto.Csprng;
import chotto.auth.SessionInfo;
import chotto.objects.BatchContribution;
import chotto.objects.BlsSignature;
import chotto.objects.Contribution;
import chotto.objects.Secret;
import chotto.sign.BlsSigner;
import chotto.sign.EcdsaSigner;
import java.util.List;
import org.apache.tuweni.units.bigints.UInt256;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Contributor {

  private static final Logger LOG = LoggerFactory.getLogger(Contributor.class);

  private final Csprng csprng;
  private final BlsSigner blsSigner;
  private final EcdsaSigner ecdsaSigner;
  private final SessionInfo sessionInfo;
  private final String identity;
  private final boolean blsSignContributions;
  private final boolean ecdsaSignBatchContribution;

  public Contributor(
      final Csprng csprng,
      final BlsSigner blsSigner,
      final EcdsaSigner ecdsaSigner,
      final SessionInfo sessionInfo,
      final String identity,
      final boolean blsSignContributions,
      final boolean ecdsaSignBatchContribution) {
    this.csprng = csprng;
    this.blsSigner = blsSigner;
    this.ecdsaSigner = ecdsaSigner;
    this.sessionInfo = sessionInfo;
    this.identity = identity;
    this.blsSignContributions = blsSignContributions;
    this.ecdsaSignBatchContribution = ecdsaSignBatchContribution;
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
      if (blsSignContributions) {
        final BlsSignature signature = blsSigner.sign(secret, identity);
        contribution.setBlsSignature(signature);
        LOG.info("BLS-Signed the contribution with your identity");
      } else {
        contribution.setBlsSignature(null);
        LOG.info("Skipped BLS-Signing the contribution");
      }
    }
    if (sessionInfo.getProvider().equals(ETHEREUM) && ecdsaSignBatchContribution) {
      final String ecdsaSignature = ecdsaSigner.sign(sessionInfo.getNickname(), batchContribution);
      batchContribution.setEcdsaSignature(ecdsaSignature);
      LOG.info("Signed the batch contribution with your ECDSA Signature");
    } else {
      batchContribution.setEcdsaSignature(null);
      LOG.info("Skipped signing the batch contribution with an ECDSA Signature");
    }

    return batchContribution;
  }
}
