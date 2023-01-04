package chotto.verification;

import chotto.objects.BatchTranscript;
import chotto.objects.G1Point;
import chotto.objects.Transcript;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.ValidationMessage;
import java.io.UncheckedIOException;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TranscriptVerification {

  private static final Logger LOG = LoggerFactory.getLogger(TranscriptVerification.class);

  private final JsonSchema transcriptSchema;
  private final ObjectMapper objectMapper;

  public TranscriptVerification(final ObjectMapper objectMapper) {
    this.transcriptSchema = SchemaLoader.loadTranscriptSchema();
    this.objectMapper = objectMapper;
  }

  public boolean schemaCheck(final String transcriptJson) {
    final Set<ValidationMessage> validationMessages;
    try {
      validationMessages = transcriptSchema.validate(objectMapper.readTree(transcriptJson));
    } catch (final JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
    if (!validationMessages.isEmpty()) {
      LOG.error(
          "Error(s) when verifying the received transcript against the schema: "
              + validationMessages);
      return false;
    }
    return true;
  }

  public boolean pointChecks(final BatchTranscript batchTranscript) {
    return batchTranscript.getTranscripts().stream()
        .map(Transcript::getWitness)
        .flatMap(witness -> witness.getRunningProducts().stream())
        .allMatch(G1Point::isInPrimeSubgroup);
  }
}
