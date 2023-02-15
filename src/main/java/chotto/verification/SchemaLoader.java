package chotto.verification;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import java.io.InputStream;

public class SchemaLoader {

  private SchemaLoader() {}

  private static final JsonSchemaFactory FACTORY =
      JsonSchemaFactory.getInstance(VersionFlag.V202012);

  public static JsonSchema loadContributionSchema() {
    return FACTORY.getSchema(getResource("contributionSchema.json"));
  }

  public static JsonSchema loadTranscriptSchema() {
    return FACTORY.getSchema(getResource("transcriptSchema.json"));
  }

  private static InputStream getResource(final String resource) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
  }
}
