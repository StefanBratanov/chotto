package chotto.verification;

import chotto.objects.BatchTranscript;
import chotto.objects.G2Point;
import chotto.objects.Receipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiptValidator {

  private static final Logger LOG = LoggerFactory.getLogger(ReceiptValidator.class);

  private final ObjectMapper objectMapper;

  public ReceiptValidator(final ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public void validate(final Receipt receipt, final BatchTranscript batchTranscript) {
    try {
      final JsonNode receiptJsonNode = objectMapper.readTree(receipt.getReceipt());

      final ArrayNode witnessNode = (ArrayNode) receiptJsonNode.get("witness");
      final List<G2Point> witnesses =
          StreamSupport.stream(witnessNode.spliterator(), false)
              .map(JsonNode::asText)
              .map(G2Point::fromHexString)
              .collect(Collectors.toList());

      witnesses.forEach(
          witness -> {
            boolean witnessHasBeenAdded =
                batchTranscript.getTranscripts().stream()
                    .anyMatch(
                        transcript -> transcript.getWitness().getPotPubkeys().contains(witness));
            if (!witnessHasBeenAdded) {
              throw new ReceiptValidationException(
                  String.format(
                      "%s is not equal to any of the PoT Pubkeys", witness.toHexString()));
            }
          });

      LOG.info("Checked that witnesses are equal to PoT Pubkeys");

    } catch (final JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
