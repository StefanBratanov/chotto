package chotto.signing;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.Constants;
import chotto.TestUtil;
import chotto.objects.BlsSignature;
import chotto.objects.Secret;
import org.junit.jupiter.api.Test;
import supranational.blst.BLST_ERROR;
import supranational.blst.P2;

class SignerTest {

  @Test
  public void testBlsSigning() {
    final Secret secret = TestUtil.generateRandomSecret();
    final String identity = "git|12345678|@username";

    final BlsSignature signature = Signer.blsSign(secret, identity);

    assertThat(signature.toBytesCompressed().size()).isEqualTo(48);

    // signature verification
    final BLST_ERROR result =
        signature
            .getEcPoint()
            .core_verify(
                new P2(secret.getKey()).to_affine(),
                true,
                identity.getBytes(),
                Constants.IRTF_BLS_CYPHERSUITE);

    assertThat(result).isEqualTo(BLST_ERROR.BLST_SUCCESS);
  }
}
