package chotto.verification;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.TestUtil;
import chotto.objects.BatchTranscript;
import chotto.objects.Receipt;
import chotto.serialization.ChottoObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ReceiptValidatorTest {

  private final ReceiptValidator receiptValidator =
      new ReceiptValidator(ChottoObjectMapper.getInstance());

  @Test
  public void testSuccessfulValidationOfReceipt() {
    final Receipt receipt = TestUtil.getReceipt("integration/receipt.json");
    final BatchTranscript batchTranscript =
        TestUtil.getBatchTranscript("integration/transcript.json");

    receiptValidator.validate(receipt, batchTranscript);
  }

  @Test
  public void testWitnessDoesNotEqualPoTPubkeys() {
    final Receipt receipt = TestUtil.getReceipt("integration/receipt.json");
    final BatchTranscript batchTranscript =
        TestUtil.getBatchTranscript("integration/otherTranscript.json");

    final ReceiptValidationException exception =
        Assertions.assertThrows(
            ReceiptValidationException.class,
            () -> receiptValidator.validate(receipt, batchTranscript));

    assertThat(exception)
        .hasMessage(
            "0xa22bf92bc5c371461278815a75676915f777c8332b60e15078c80122e9d93408fc790fe0a6070a1e373db6cc36985c3b0350024f29087a629df7a9998408aeb0b3e8d151acdd1a76219a18ac88af3e8f2dbb895f588537dbb565b2f324c6fa58 is not equal to any of the PoT Pubkeys");
  }
}
