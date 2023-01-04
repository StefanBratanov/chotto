package chotto;

import chotto.contribution.Contributor;
import chotto.contribution.SubContributionManager;
import chotto.objects.BatchContribution;
import chotto.objects.Secret;
import chotto.secret.Csprng;
import chotto.secret.CsprngStub;
import chotto.secret.SecretsManager;
import chotto.serialization.ChottoObjectMapper;
import chotto.sign.BlsSigner;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class ReferenceTests {

  private final ObjectMapper objectMapper = ChottoObjectMapper.getInstance();

  /**
   * <a href="https://github.com/jsign/kzg-ceremony-test-vectors">Ethereum KZG Powers of Tau Test
   * Vectors</a>
   */
  @Test
  public void validateImplementationAgainstJsignTestVectors() throws IOException, JSONException {
    final BatchContribution initialBatchContribution =
        objectMapper.readValue(
            new URL(
                "https://raw.githubusercontent.com/jsign/kzg-ceremony-test-vectors/main/initialContribution.json"),
            BatchContribution.class);

    final String expectedBatchContribution =
        objectMapper
            .readTree(
                new URL(
                    "https://raw.githubusercontent.com/jsign/kzg-ceremony-test-vectors/main/updatedContribution.json"))
            .toString();

    final Csprng fixedCsprng =
        CsprngStub.fromFixedSecrets(
            List.of(
                Secret.fromHexString("0x111100"),
                Secret.fromHexString("0x221100"),
                Secret.fromHexString("0x331100"),
                Secret.fromHexString("0x441100")));

    final SecretsManager secretsManager = new SecretsManager(fixedCsprng);
    secretsManager.generateSecrets();

    // reference implementation uses an empty string as an identity
    final SubContributionManager subContributionManager =
        new SubContributionManager(secretsManager, new BlsSigner(), "", true);
    subContributionManager.generateContexts();

    final Contributor contributor = new Contributor(subContributionManager, Optional.empty());

    final BatchContribution updatedBatchContribution =
        contributor.contribute(initialBatchContribution);

    JSONAssert.assertEquals(
        expectedBatchContribution, objectMapper.writeValueAsString(updatedBatchContribution), true);
  }
}
