package chotto.contribution;

import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion.VersionFlag;
import java.io.InputStream;

public class SchemaLoader {

  private static final JsonSchemaFactory FACTORY =
      JsonSchemaFactory.getInstance(VersionFlag.V202012);

  public static JsonSchema loadContributionSchema() {
    return FACTORY.getSchema(getResource("contributionSchema.json"));
  }

  private static InputStream getResource(final String resource) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
  }
}
