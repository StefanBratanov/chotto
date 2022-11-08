package chotto.sign;

import chotto.Store;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EcdsaSignCallback implements Handler {

  private static final Logger LOG = LoggerFactory.getLogger(EcdsaSignCallback.class);

  private final Store store;

  public EcdsaSignCallback(final Store store) {
    this.store = store;
  }

  @Override
  public void handle(final Context ctx) {
    final HttpServletRequest request = ctx.req();
    final String signature =
        Objects.requireNonNull(request.getParameter("signature"), "signature must not be null");
    store.setEcdsaSignature(signature);
    LOG.info("Received an ECDSA signature for the batch contribution: {}", signature);
    ctx.result(
        "Thank you for your signature. You can return to the Chotto logs to witness the remainder of the ceremony.");
  }
}
