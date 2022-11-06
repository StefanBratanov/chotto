package chotto.sign;

import chotto.Constants;
import chotto.objects.BlsSignature;
import chotto.objects.Secret;
import supranational.blst.P1;

public class BlsSigner {

  public static BlsSignature sign(final Secret secret, final String identity) {
    final P1 p1 = new P1();
    p1.hash_to(identity.getBytes(), Constants.IRTF_BLS_CYPHERSUITE, new byte[0])
        .sign_with(secret.getKey());
    return new BlsSignature(p1.to_affine());
  }
}
