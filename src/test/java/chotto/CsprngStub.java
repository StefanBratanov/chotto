package chotto;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import chotto.objects.Secret;

public class CsprngStub extends Csprng {

  private final Supplier<Secret> predefinedSecrets;

  public static Csprng fromPredefinedSecrets(final List<Secret> secrets) {
    final AtomicInteger currentIndex = new AtomicInteger(0);
    final Supplier<Secret> predefinedSecrets = () -> secrets.get(currentIndex.getAndIncrement());
    return new CsprngStub(predefinedSecrets);
  }

  private CsprngStub(final Supplier<Secret> predefinedSecrets) {
    super("foobar");
    this.predefinedSecrets = predefinedSecrets;
  }

  @Override
  public Secret generateSecret() {
    return predefinedSecrets.get();
  }
}
