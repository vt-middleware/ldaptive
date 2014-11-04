/*
  $Id: RetrySearchOperation.java 2619 2013-02-12 21:52:33Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2619 $
  Updated: $Date: 2013-02-12 16:52:33 -0500 (Tue, 12 Feb 2013) $
*/
package org.ldaptive;

import org.ldaptive.handler.OperationExceptionHandler;

/**
 * Provides a wrapper class for testing {@link OperationExceptionHandler}.
 *
 * @author  Middleware Services
 * @version  $Revision: 2619 $ $Date: 2013-02-12 16:52:33 -0500 (Tue, 12 Feb 2013) $
 */
public class RetrySearchOperation extends SearchOperation
{

  /** serial version uid. */
  private static final long serialVersionUID = 4247614583961731974L;

  /** exception to rethrow. */
  private final LdapException ex;

  /** whether to perform retries. */
  private boolean allowRetry = true;

  /** retry counter. */
  private int retryCount;

  /** run time counter. */
  private long runTime;

  /** stop counter. */
  private int stopCount;


  /**
   * Creates a new retry search operation.
   *
   * @param  c  connection
   * @param  e  ldap exception
   */
  public RetrySearchOperation(final Connection c, final LdapException e)
  {
    super(c);
    ex = e;
    setOperationExceptionHandler(new RetryExceptionHandler());
  }


  /**
   * Sets whether to allow retry.
   *
   * @param  b  whether to allow retry
   */
  public void setAllowRetry(final boolean b)
  {
    allowRetry = b;
  }


  /**
   * Returns the retry count.
   *
   * @return  retry count
   */
  public int getRetryCount()
  {
    return retryCount;
  }


  /**
   * Returns the run time counter.
   *
   * @return  run time
   */
  public long getRunTime()
  {
    return runTime;
  }


  /**
   * Sets the count at which to stop retries.
   *
   * @param  i  stop count
   */
  public void setStopCount(final int i)
  {
    stopCount = i;
  }


  /** Resets all the counters. */
  public void reset()
  {
    retryCount = 0;
    runTime = 0;
    stopCount = 0;
  }


  /**
   * See {@link ReopenOperationExceptionHandler#setRetry(int)}.
   *
   * @param  i  to set
   */
  public void setReopenRetry(final int i)
  {
    ((ReopenOperationExceptionHandler)
      getOperationExceptionHandler()).setRetry(i);
  }


  /**
   * See {@link ReopenOperationExceptionHandler#setRetryWait(long)}.
   *
   * @param  l  to set
   */
  public void setReopenRetryWait(final long l)
  {
    ((ReopenOperationExceptionHandler)
      getOperationExceptionHandler()).setRetryWait(l);
  }


  /**
   * See {@link ReopenOperationExceptionHandler#setRetryBackoff(int)}.
   *
   * @param  i  to set
   */
  public void setReopenRetryBackoff(final int i)
  {
    ((ReopenOperationExceptionHandler)
      getOperationExceptionHandler()).setRetryBackoff(i);
  }


  /**
   * Calculates the execution time of {@link ReopenOperationExceptionHandler}.
   */
  public class RetryExceptionHandler extends ReopenOperationExceptionHandler
  {


    /** {@inheritDoc} */
    @Override
    public void handleInternal(
      final Connection conn,
      final SearchRequest request,
      final Response<SearchResult> response)
      throws LdapException
    {
      if (!allowRetry) {
        return;
      }
      final long t = System.currentTimeMillis();
      super.handleInternal(conn, request, response);
      runTime += System.currentTimeMillis() - t;
      throw ex;
    }


    /** {@inheritDoc} */
    @Override
    protected boolean retry(final int count)
    {
      if (stopCount > 0 && retryCount == stopCount) {
        return false;
      }
      boolean b = super.retry(count);
      if (b) {
        retryCount++;
      }
      return b;
    }
  }
}
