package chotto.contribution;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.CsprngStub;
import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.objects.Secret;
import chotto.secret.Csprng;
import chotto.secret.SecretsGenerator;
import chotto.serialization.ChottoObjectMapper;
import chotto.sign.BlsSigner;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

class ContributorTest {

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  private static final SecretsGenerator SECRET_MANAGER;

  static {
    final List<Secret> secrets = TestUtil.getTestSecrets();
    final Csprng csprng = CsprngStub.fromPredefinedSecrets(secrets);
    SECRET_MANAGER = new SecretsGenerator(csprng);
    SECRET_MANAGER.generateSecrets();
  }

  private final ContributionVerification contributionVerification =
      new ContributionVerification(OBJECT_MAPPER);

  @ParameterizedTest(name = "{2}")
  @MethodSource("provideContributorInput")
  public void checkContributionFlow(
      final boolean blsSignSubContributions,
      final boolean ecdsaSignContribution,
      final String expectedContributionResource)
      throws IOException, JSONException {

    final String identity = "git|14827647|@StefanBratanov";

    final SubContributionManager subContributionManager =
        new SubContributionManager(
            SECRET_MANAGER, new BlsSigner(), identity, blsSignSubContributions);

    subContributionManager.generateContexts();

    Optional<String> ecdsaSignatureMaybe = Optional.empty();
    if (ecdsaSignContribution) {
      ecdsaSignatureMaybe =
          Optional.of(
              "0x1949e68bfab53a3f921ace3c83d562e36fa5fe82d6f603394e58627a2fa4a31553aca183c6adbb1dad2ac032358b863d2c2137fe2b046e822041037fb97758251c");
    }

    final Contributor contributor = new Contributor(subContributionManager, ecdsaSignatureMaybe);

    final BatchContribution initialBatchContribution = TestUtil.getInitialBatchContribution();

    final BatchContribution updatedContribution = contributor.contribute(initialBatchContribution);

    final String actualContribution = OBJECT_MAPPER.writeValueAsString(updatedContribution);

    final String expectedContribution = TestUtil.readResource(expectedContributionResource);

    JSONAssert.assertEquals(expectedContribution, actualContribution, true);

    final boolean validJson = contributionVerification.schemaCheck(actualContribution);
    final boolean validContribution = contributionVerification.subgroupChecks(updatedContribution);

    assertThat(validJson).isTrue();
    assertThat(validContribution).isTrue();
  }

  private static Stream<Arguments> provideContributorInput() {
    return Stream.of(
        Arguments.of(true, true, "updatedContribution.json"),
        Arguments.of(true, false, "updatedContributionNoEcdsa.json"),
        Arguments.of(false, false, "updatedContributionNoBlsNoEcdsa.json"));
  }
}
