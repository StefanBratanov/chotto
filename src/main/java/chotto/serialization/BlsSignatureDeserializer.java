package chotto.serialization;

import chotto.objects.BlsSignature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;

public class BlsSignatureDeserializer extends JsonDeserializer<BlsSignature> {

  @Override
  public BlsSignature deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException {
    return BlsSignature.fromHexString(p.getValueAsString());
  }
}
