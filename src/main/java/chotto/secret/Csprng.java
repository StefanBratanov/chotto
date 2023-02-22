package chotto.secret;

import chotto.objects.Secret;
import java.security.SecureRandom;

public interface Csprng {

  SecureRandom SECURE_RANDOM = new SecureRandom();

  Secret generateSecret();
}
