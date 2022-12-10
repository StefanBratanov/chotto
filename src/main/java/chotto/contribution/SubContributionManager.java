package chotto.contribution;

import chotto.objects.BlsSignature;
import chotto.objects.G2Point;
import chotto.objects.SubContributionContext;
import chotto.secret.SecretsManager;
import chotto.sign.BlsSigner;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SubContributionManager {

  private final List<SubContributionContext> contexts = new LinkedList<>();

  private final SecretsManager secretsManager;
  private final BlsSigner blsSigner;
  private final String identity;
  private final boolean blsSignSubContributions;

  public SubContributionManager(
      final SecretsManager secretsManager,
      final BlsSigner blsSigner,
      final String identity,
      final boolean blsSignSubContributions) {
    this.secretsManager = secretsManager;
    this.blsSigner = blsSigner;
    this.identity = identity;
    this.blsSignSubContributions = blsSignSubContributions;
  }

  public void generateContexts() {
    secretsManager
        .getSecrets()
        .forEach(
            secret -> {
              final G2Point potPubKey = G2Point.generator().mul(secret.toUInt256());
              final Optional<BlsSignature> blsSignatureMaybe =
                  blsSignSubContributions
                      ? Optional.of(blsSigner.sign(secret, identity))
                      : Optional.empty();
              final SubContributionContext context =
                  new SubContributionContext(secret, blsSignatureMaybe, potPubKey);
              contexts.add(context);
            });
  }

  public List<SubContributionContext> getContexts() {
    if (contexts.isEmpty()) {
      throw new IllegalStateException("Sub-contribution contexts haven't been generated.");
    }
    return contexts;
  }
}
