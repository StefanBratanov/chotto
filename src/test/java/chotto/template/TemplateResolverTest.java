package chotto.template;

import chotto.TestUtil;
import chotto.objects.BatchContribution;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class TemplateResolverTest {

  private final TemplateResolver templateEngine = new TemplateResolver();

  @Test
  public void createsTypedDataFromContribution() throws JSONException {
    final BatchContribution batchContribution =
        TestUtil.getBatchContribution("updatedContribution.json");

    final String typedData = templateEngine.createTypedData(batchContribution);

    JSONAssert.assertEquals(
        TestUtil.readResource("template/expectedTypedData.json"), typedData, true);
  }
}
