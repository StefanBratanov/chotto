package chotto.secret;

import chotto.objects.Secret;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class CsprngStub implements Csprng {

  private final Supplier<Secret> fixedSecrets;

  public static Csprng fromFixedSecrets(final List<Secret> secrets) {
    final AtomicInteger currentIndex = new AtomicInteger(0);
    final Supplier<Secret> fixedSecrets = () -> secrets.get(currentIndex.getAndIncrement());
    return new CsprngStub(fixedSecrets);
  }

  private CsprngStub(final Supplier<Secret> fixedSecrets) {
    this.fixedSecrets = fixedSecrets;
  }

  @Override
  public Secret generateSecret() {
    return fixedSecrets.get();
  }
}
