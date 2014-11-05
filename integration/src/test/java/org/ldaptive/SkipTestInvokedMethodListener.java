/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * Marks a test as skipped if it's results indicate such.
 *
 * @author  Middleware Services
 * @version  $Revision: 2198 $ $Date: 2012-01-04 16:02:09 -0500 (Wed, 04 Jan 2012) $
 */
public class SkipTestInvokedMethodListener implements IInvokedMethodListener
{


  /** {@inheritDoc} */
  @Override
  public void afterInvocation(
    final IInvokedMethod method, final ITestResult testResult)
  {
    final Throwable t = testResult.getThrowable();
    final String msg = t != null ? t.getMessage() : null;
    if (t instanceof UnsupportedOperationException) {
      testResult.setStatus(ITestResult.SKIP);
    } else if (msg != null &&
               msg.startsWith(UnsupportedOperationException.class.getName())) {
      testResult.setStatus(ITestResult.SKIP);
    }
  }


  /** {@inheritDoc} */
  @Override
  public void beforeInvocation(
    final IInvokedMethod method, final ITestResult testResult) {}
}
