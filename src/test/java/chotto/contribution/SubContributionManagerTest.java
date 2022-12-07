package chotto.contribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import chotto.TestUtil;
import chotto.objects.BlsSignature;
import chotto.objects.Secret;
import chotto.secret.SecretsManager;
import chotto.sign.BlsSigner;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SubContributionManagerTest {

  private final SecretsManager secretsManager = mock(SecretsManager.class);

  private final BlsSigner blsSigner = mock(BlsSigner.class);

  private final String identity = "git|14827647|@StefanBratanov";

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void generatesAndGetsContexts(final boolean blsSignSubContributions) {
    final SubContributionManager subContributionManager =
        new SubContributionManager(secretsManager, blsSigner, identity, blsSignSubContributions);

    final List<Secret> secrets = TestUtil.getTestSecrets();

    when(secretsManager.getSecrets()).thenReturn(secrets);

    final BlsSignature blsSignature =
        BlsSignature.fromHexString(
            "0x9411ddc2e12e81ba46b74b0090e237483de76dfb5bb30a11cc19e1bad7d07eb5bbaeb5f7eb2db46538706cdb8b07bf1c");

    when(blsSigner.sign(any(), eq(identity))).thenReturn(blsSignature);

    subContributionManager.generateContexts();

    final List<SubContributionContext> contexts = subContributionManager.getContexts();

    assertThat(contexts).hasSize(4);
    assertThat(contexts.stream().map(SubContributionContext::getSecret)).hasSameElementsAs(secrets);
    assertThat(contexts.stream().map(SubContributionContext::getPotPubkey))
        .allMatch(Objects::nonNull);
    assertThat(contexts.stream().map(SubContributionContext::getBlsSignatureMaybe))
        .allSatisfy(
            blsSignatureMaybe -> {
              if (blsSignSubContributions) {
                assertThat(blsSignatureMaybe).hasValue(blsSignature);
              } else {
                assertThat(blsSignatureMaybe).isEmpty();
              }
            });
  }
}
