package chotto.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;

public class CustomNullSerializer extends JsonSerializer<Object> {

  @Override
  public void serialize(
      final Object value, final JsonGenerator gen, final SerializerProvider serializers)
      throws IOException {
    gen.writeString("");
  }
}
