/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.nio.charset.StandardCharsets;
import org.ldaptive.BindResponse;
import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
import org.ldaptive.sasl.Mechanism;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link ScramSaslClient}.
 *
 * @author  Middleware Services
 */
public class ScramSaslClientTest
{


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport")
  public void clientFirstMessage()
    throws Exception
  {
    final ScramSaslClient.ClientFirstMessage clientFirstMsg = new ScramSaslClient.ClientFirstMessage(
      "test@ldaptive.org", null);
    Assert.assertEquals(clientFirstMsg.encode(), "n,,n=test@ldaptive.org,r=" + clientFirstMsg.getNonce());
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = "transport")
  public void roundTripBind()
    throws Exception
  {
    final String saslCreds =
      "r=tBn5tno7IaCEAX28UCdeYA==rZymji+nFf6/+3nvnH7oRVzqhYBrXfNo,s=GrrkPG1cm6PsF/3Lq9xAtL5xUz0=,i=4096";

    final ScramSaslClient.ClientFirstMessage clientFirstMsg = new ScramSaslClient.ClientFirstMessage(
      "test3@vt.edu", LdapUtils.base64Decode("tBn5tno7IaCEAX28UCdeYA=="));

    final ScramSaslClient.ServerFirstMessage serverFirstMsg = new ScramSaslClient.ServerFirstMessage(
      clientFirstMsg,
      BindResponse.builder()
        .resultCode(ResultCode.SASL_BIND_IN_PROGRESS)
        .serverSaslCreds(saslCreds.getBytes(StandardCharsets.UTF_8))
        .build());

    Assert.assertEquals(serverFirstMsg.getMessage(), saslCreds);
    Assert.assertEquals(
      serverFirstMsg.getCombinedNonce(), "tBn5tno7IaCEAX28UCdeYA==rZymji+nFf6/+3nvnH7oRVzqhYBrXfNo");
    Assert.assertEquals(serverFirstMsg.getSalt(), LdapUtils.base64Decode("GrrkPG1cm6PsF/3Lq9xAtL5xUz0="));
    Assert.assertEquals(serverFirstMsg.getIterations(), 4096);

    final ScramSaslClient.ClientFinalMessage clientFinalMsg = new ScramSaslClient.ClientFinalMessage(
      Mechanism.SCRAM_SHA_1, "password", clientFirstMsg, serverFirstMsg);

    Assert.assertEquals(
      clientFinalMsg.encode(),
      "c=biws,r=tBn5tno7IaCEAX28UCdeYA==rZymji+nFf6/+3nvnH7oRVzqhYBrXfNo,p=jtvyGs0+ZYhxqtejptDPilF89Y4=");

    final ScramSaslClient.ServerFinalMessage serverFinalMessage = new ScramSaslClient.ServerFinalMessage(
      Mechanism.SCRAM_SHA_1,
      clientFinalMsg,
      BindResponse.builder()
        .resultCode(ResultCode.SUCCESS)
        .serverSaslCreds("v=p42sN66CmsScQGfZJppVyhuTX+g=".getBytes(StandardCharsets.UTF_8))
        .build());

    Assert.assertTrue(serverFinalMessage.isVerified());
  }
}
