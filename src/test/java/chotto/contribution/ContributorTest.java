package chotto.contribution;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import chotto.Csprng;
import chotto.CsprngStub;
import chotto.TestUtil;
import chotto.objects.BatchContribution;
import chotto.objects.Secret;
import chotto.serialization.ChottoObjectMapper;
import chotto.verification.ContributionVerification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.assertj.core.api.Assertions.assertThat;

class ContributorTest {

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  private final List<Secret> secrets =
      List.of(
          Secret.fromText("foo"),
          Secret.fromText("bar"),
          Secret.fromText("danksharding"),
          Secret.fromText("devcon"));

  private final Csprng csprng = CsprngStub.fromPredefinedSecrets(secrets);

  private final ContributionVerification contributionVerification =
      new ContributionVerification(OBJECT_MAPPER);

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void checkContributionFlow(final boolean withIdentity) throws IOException, JSONException {
    final Contributor contributor;
    if (withIdentity) {
      contributor = new Contributor(csprng, Optional.of("git|14827647|@StefanBratanov"));
    } else {
      contributor = new Contributor(csprng, Optional.empty());
    }
    final BatchContribution initialBatchContribution = TestUtil.getInitialBatchContribution();

    final BatchContribution updatedContribution = contributor.contribute(initialBatchContribution);

    final String actualContribution = OBJECT_MAPPER.writeValueAsString(updatedContribution);
    final String expectedContribution =
        TestUtil.readResource(
            withIdentity ? "updatedContribution.json" : "updatedContributionNoSignatures.json");

    JSONAssert.assertEquals(expectedContribution, actualContribution, true);

    final boolean validJson = contributionVerification.schemaCheck(actualContribution);
    final boolean validContribution = contributionVerification.subgroupChecks(updatedContribution);

    assertThat(validJson).isTrue();
    assertThat(validContribution).isTrue();
  }
}
