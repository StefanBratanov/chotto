package chotto.contribution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import chotto.Csprng;
import chotto.CsprngStub;
import chotto.TestUtil;
import chotto.auth.Provider;
import chotto.auth.SessionInfo;
import chotto.objects.BatchContribution;
import chotto.objects.Secret;
import chotto.serialization.ChottoObjectMapper;
import chotto.sign.BlsSigner;
import chotto.sign.EcdsaSigner;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;
import org.json.JSONException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

class ContributorTest {

  private static final ObjectMapper OBJECT_MAPPER = ChottoObjectMapper.getInstance();

  private final List<Secret> secrets =
      List.of(
          Secret.fromText("foo"),
          Secret.fromText("bar"),
          Secret.fromText("danksharding"),
          Secret.fromText("devcon"));

  private final Csprng csprng = CsprngStub.fromPredefinedSecrets(secrets);

  private final BlsSigner blsSigner = new BlsSigner();

  private final EcdsaSigner ecdsaSigner = mock(EcdsaSigner.class);

  private final ContributionVerification contributionVerification =
      new ContributionVerification(OBJECT_MAPPER);

  @ParameterizedTest(name = "{3}")
  @MethodSource("provideContributorInput")
  public void checkContributionFlow(
      final SessionInfo sessionInfo,
      final boolean blsSignContribution,
      final boolean ecdsaSignContribution,
      final String expectedContributionResource)
      throws IOException, JSONException {

    final String identity = "git|14827647|@StefanBratanov";

    final String ecdsaSignature =
        "0x1949e68bfab53a3f921ace3c83d562e36fa5fe82d6f603394e58627a2fa4a31553aca183c6adbb1dad2ac032358b863d2c2137fe2b046e822041037fb97758251c";

    final Contributor contributor =
        new Contributor(
            csprng,
            blsSigner,
            ecdsaSigner,
            sessionInfo,
            identity,
            blsSignContribution,
            ecdsaSignContribution);

    when(ecdsaSigner.sign(eq(sessionInfo.getNickname()), notNull())).thenReturn(ecdsaSignature);

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
    final SessionInfo ethereumSessionInfo =
        new SessionInfo(Provider.ETHEREUM, "0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad", null);
    final SessionInfo githubSessionInfo = new SessionInfo(Provider.GITHUB, "StefanBratanov", null);
    return Stream.of(
        Arguments.of(ethereumSessionInfo, true, true, "updatedContribution.json"),
        Arguments.of(githubSessionInfo, true, false, "updatedContributionNoEcdsa.json"),
        Arguments.of(ethereumSessionInfo, false, false, "updatedContributionNoBlsNoEcdsa.json"),
        Arguments.of(githubSessionInfo, true, true, "updatedContributionNoEcdsa.json"));
  }
}
