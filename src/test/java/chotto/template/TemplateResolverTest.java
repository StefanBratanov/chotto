package chotto.template;

import static org.assertj.core.api.Assertions.assertThat;

import chotto.Constants;
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

  @Test
  public void createsSignContributionHtml() {
    final String html =
        templateEngine.createSignContributionHtml(
            "0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad", "{}", Constants.ECDSA_SIGN_CALLBACK_PATH);

    assertThat(html).contains("const typedData = \"{}\"");
    assertThat(html).contains("const ethAddress = \"0x33b187514f5Ea150a007651bEBc82eaaBF4da5ad\"");
    assertThat(html).contains("const callbackPath = \"/sign/ecdsa/callback\"");
  }
}
