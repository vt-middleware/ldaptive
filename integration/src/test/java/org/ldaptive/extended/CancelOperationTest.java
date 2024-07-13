/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.extended;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.ldaptive.AbstractTest;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchOperationHandle;
import org.ldaptive.SearchRequest;
import org.ldaptive.SingleConnectionFactory;
import org.ldaptive.TestControl;
import org.ldaptive.control.SyncRequestControl;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for cancelling a search operation.
 *
 * @author  Middleware Services
 */
public class CancelOperationTest extends AbstractTest
{


  /**
   * @param  dn  to search on.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("cancelDn")
  @Test(groups = "extended")
  public void cancel(final String dn)
    throws Exception
  {
    // AD server returns UNAVAILABLE_CRITICAL_EXTENSION
    if (TestControl.isActiveDirectory()) {
      return;
    }

    final SingleConnectionFactory cf = createSingleConnectionFactory();
    try {
      final SearchOperation search = new SearchOperation(cf);
      final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn);
      request.setControls(new SyncRequestControl(SyncRequestControl.Mode.REFRESH_AND_PERSIST, true));
      search.setRequest(request);
      final CountDownLatch latch = new CountDownLatch(1);
      search.setEntryHandlers(ldapEntry -> {
        latch.countDown();
        return ldapEntry;
      });
      final Result[] result = new Result[1];
      search.setResultHandlers(response -> result[0] = response);

      final SearchOperationHandle searchHandle = search.send();
      latch.await(10, TimeUnit.SECONDS);

      final ExtendedResponse cancelResult = searchHandle.cancel().execute();
      assertThat(result[0].getResultCode()).isEqualTo(ResultCode.CANCELED);
      assertThat(cancelResult.getResultCode()).isEqualTo(ResultCode.SUCCESS);
    } finally {
      cf.close();
    }
  }
}
