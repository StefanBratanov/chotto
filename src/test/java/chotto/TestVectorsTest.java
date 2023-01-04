package chotto;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

/**
 * <a href="https://github.com/jsign/kzg-ceremony-test-vectors">Ethereum KZG Powers of Tau Test
 * Vectors</a>
 */
public class TestVectorsTest {

  private static final Csprng FIXED_CSPRNG;

  static {
    FIXED_CSPRNG =
        CsprngStub.fromFixedSecrets(
            List.of(
                Secret.fromHexString("0x111100"),
                Secret.fromHexString("0x221100"),
                Secret.fromHexString("0x331100"),
                Secret.fromHexString("0x441100")));
  }

  private final ObjectMapper objectMapper = ChottoObjectMapper.getInstance();

  @Test
  public void validateImplementation() throws IOException, JSONException {
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

    final SecretsManager secretsManager = new SecretsManager(FIXED_CSPRNG);
    secretsManager.generateSecrets();

    final SubContributionManager subContributionManager =
        new SubContributionManager(
            secretsManager,
            new BlsSigner(),
            "eth|0x33b187514f5ea150a007651bebc82eaabf4da5ad",
            true);
    subContributionManager.generateContexts();

    final Contributor contributor = new Contributor(subContributionManager, Optional.empty());

    final BatchContribution updatedBatchContribution =
        contributor.contribute(initialBatchContribution);
    assertThat(updatedBatchContribution.getEcdsaSignature()).isNull();
    // the expected json uses an empty ecdsaSignature in the json which is equivalent to not
    // including it at all
    updatedBatchContribution.setEcdsaSignature("");

    // ignoring bls signatures
    final JSONComparator comparator =
        new CustomComparator(
            JSONCompareMode.STRICT_ORDER,
            new Customization("contributions[*].blsSignature", (__, ___) -> true));

    JSONAssert.assertEquals(
        expectedBatchContribution,
        objectMapper.writeValueAsString(updatedBatchContribution),
        comparator);
  }
}
