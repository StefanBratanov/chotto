package chotto;

import java.math.BigInteger;
import org.apache.tuweni.units.bigints.UInt256;

public class Constants {

  private Constants() {}

  public static final int NUMBER_OF_SECRETS = 4;

  public static final String GITHUB_REPO = "https://github.com/StefanBratanov/chotto";

  public static final String AUTH_CALLBACK_PATH = "/auth/callback";

  public static final String ECDSA_SIGN_CALLBACK_PATH = "/sign/ecdsa/callback";

  public static final UInt256 CURVE_ORDER =
      UInt256.valueOf(
          new BigInteger(
              "52435875175126190479447740508185965837690552500527637822603658699938581184513"));

  public static final String IRTF_BLS_CYPHERSUITE = "BLS_SIG_BLS12381G1_XMD:SHA-256_SSWU_RO_POP_";
}
