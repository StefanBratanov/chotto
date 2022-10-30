package chotto.serialization;

import chotto.objects.BlsSignature;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class BlsSignatureSerializer extends JsonSerializer<BlsSignature> {

  @Override
  public void serialize(
      final BlsSignature value, final JsonGenerator gen, final SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.toHexString());
  }
}
