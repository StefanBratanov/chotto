package chotto.contribution;

import chotto.objects.BatchContribution;
import chotto.objects.Contribution;
import chotto.objects.G1Point;
import chotto.objects.G2Point;
import chotto.objects.PowersOfTau;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import java.io.UncheckedIOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContributionVerification {

  private static final Logger LOG = LoggerFactory.getLogger(ContributionVerification.class);

  private final JsonSchema contributionSchema;
  private final ObjectMapper objectMapper;

  public ContributionVerification(final ObjectMapper objectMapper) {
    this.contributionSchema = SchemaLoader.loadSchema();
    this.objectMapper = objectMapper;
  }

  public boolean schemaCheck(final String contributionJson) {
    final Set<ValidationMessage> validationMessages;
    try {
      validationMessages = contributionSchema.validate(objectMapper.readTree(contributionJson));
    } catch (final JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
    if (!validationMessages.isEmpty()) {
      LOG.error(
          "Error(s) when verifying the received contribution against the schema: "
              + validationMessages);
      return false;
    }
    return true;
  }

  public boolean subgroupChecks(final BatchContribution batchContribution) {
    return batchContribution.getContributions().stream()
        .map(Contribution::getPowersOfTau)
        .allMatch(this::verifyPowersOfTau);
  }

  private boolean verifyPowersOfTau(final PowersOfTau powersOfTau) {
    for (final G1Point g1Power : powersOfTau.getG1Powers()) {
      if (!g1Power.isInPrimeSubgroup()) {
        return false;
      }
    }
    for (final G2Point g2Power : powersOfTau.getG2Powers()) {
      if (!g2Power.isInPrimeSubgroup()) {
        return false;
      }
    }
    return true;
  }
}
