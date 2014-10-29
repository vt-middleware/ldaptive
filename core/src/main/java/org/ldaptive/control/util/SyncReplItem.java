/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.control.util;

import org.ldaptive.SearchEntry;
import org.ldaptive.SearchResult;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.control.SyncDoneControl;
import org.ldaptive.control.SyncStateControl;
import org.ldaptive.intermediate.SyncInfoMessage;

/**
 * Contains data returned when using the sync repl search control.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
 */
public class SyncReplItem
{

  /** Async request from the search operation. */
  private final AsyncRequest asyncRequest;

  /** Entry contained in this sync repl item. */
  private final Entry syncReplEntry;

  /** Message contained in this sync repl item. */
  private final SyncInfoMessage syncInfoMessage;

  /** Response contained in this sync repl item. */
  private final Response syncReplResponse;

  /** Exception thrown by the search operation. */
  private final Exception syncReplException;


  /**
   * Creates a new sync repl item.
   *
   * @param  request  that represents this item
   */
  public SyncReplItem(final AsyncRequest request)
  {
    asyncRequest = request;
    syncReplEntry = null;
    syncInfoMessage = null;
    syncReplResponse = null;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  entry  that represents this item
   */
  public SyncReplItem(final Entry entry)
  {
    asyncRequest = null;
    syncReplEntry = entry;
    syncInfoMessage = null;
    syncReplResponse = null;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  message  that represents this item
   */
  public SyncReplItem(final SyncInfoMessage message)
  {
    asyncRequest = null;
    syncReplEntry = null;
    syncInfoMessage = message;
    syncReplResponse = null;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  response  that represents this item
   */
  public SyncReplItem(final Response response)
  {
    asyncRequest = null;
    syncReplEntry = null;
    syncInfoMessage = null;
    syncReplResponse = response;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  exception  that represents this item
   */
  public SyncReplItem(final Exception exception)
  {
    asyncRequest = null;
    syncReplEntry = null;
    syncInfoMessage = null;
    syncReplResponse = null;
    syncReplException = exception;
  }


  /**
   * Returns whether this item represents an async request.
   *
   * @return  whether this item represents an async request
   */
  public boolean isAsyncRequest()
  {
    return asyncRequest != null;
  }


  /**
   * Returns the async request contained in this item or null if this item does
   * not contain an async request.
   *
   * @return  async request
   */
  public AsyncRequest getAsyncRequest()
  {
    return asyncRequest;
  }


  /**
   * Returns whether this item represents a search entry.
   *
   * @return  whether this item represents a search entry
   */
  public boolean isEntry()
  {
    return syncReplEntry != null;
  }


  /**
   * Returns the entry contained in this item or null if this item does not
   * contain an entry.
   *
   * @return  sync repl entry
   */
  public Entry getEntry()
  {
    return syncReplEntry;
  }


  /**
   * Returns whether this item represents an intermediate message.
   *
   * @return  whether this item represents an intermediate message
   */
  public boolean isMessage()
  {
    return syncInfoMessage != null;
  }


  /**
   * Returns the intermediate message contained in this item or null if this
   * item does not contain a message.
   *
   * @return  sync info message
   */
  public SyncInfoMessage getMessage()
  {
    return syncInfoMessage;
  }


  /**
   * Returns whether this item represents a response.
   *
   * @return  whether this item represents a response
   */
  public boolean isResponse()
  {
    return syncReplResponse != null;
  }


  /**
   * Returns the response contained in this item or null if this item does not
   * contain a response.
   *
   * @return  response
   */
  public Response getResponse()
  {
    return syncReplResponse;
  }


  /**
   * Returns whether this item represents an exception.
   *
   * @return  whether this item represents an exception
   */
  public boolean isException()
  {
    return syncReplException != null;
  }


  /**
   * Returns the exception contained in this item or null if this item does not
   * contain an exception.
   *
   * @return  exception
   */
  public Exception getException()
  {
    return syncReplException;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    String s;
    if (isAsyncRequest()) {
      s = String.format(
        "[%s@%d::asyncRequest=%s]",
        getClass().getName(),
        hashCode(),
        asyncRequest);
    } else if (isEntry()) {
      s = String.format(
        "[%s@%d::syncReplEntry=%s]",
        getClass().getName(),
        hashCode(),
        syncReplEntry);
    } else if (isMessage()) {
      s = String.format(
        "[%s@%d::syncInfoMessage=%s]",
        getClass().getName(),
        hashCode(),
        syncInfoMessage);
    } else if (isResponse()) {
      s = String.format(
        "[%s@%d::syncReplResponse=%s]",
        getClass().getName(),
        hashCode(),
        syncReplResponse);
    } else if (isException()) {
      s = String.format(
        "[%s@%d::syncReplException=%s]",
        getClass().getName(),
        hashCode(),
        syncReplException);
    } else {
      s = String.format("[%s@%d]", getClass().getName(), hashCode());
    }
    return s;
  }


  /**
   * Wrapper class that provides easy access to the {@link SyncStateControl}
   * contained in a search entry.
   */
  public static class Entry
  {

    /** Search entry that this class wraps. */
    private final SearchEntry searchEntry;

    /** Control to search the entry for. */
    private final SyncStateControl syncStateControl;


    /**
     * Creates a new entry. If the supplied search entry contains a {@link
     * SyncStateControl} it is made available via {@link
     * #getSyncStateControl()}.
     *
     * @param  entry  to search for sync state control in
     */
    public Entry(final SearchEntry entry)
    {
      searchEntry = entry;
      syncStateControl = (SyncStateControl) entry.getControl(
        SyncStateControl.OID);
    }


    /**
     * Returns the underlying search entry.
     *
     * @return  underlying search entry
     */
    public SearchEntry getSearchEntry()
    {
      return searchEntry;
    }


    /**
     * Returns the sync state control or null if no such control exists in the
     * search entry.
     *
     * @return  sync state control
     */
    public SyncStateControl getSyncStateControl()
    {
      return syncStateControl;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::searchEntry=%s]",
          getClass().getName(),
          hashCode(),
          searchEntry);
    }
  }


  /**
   * Wrapper class that provides easy access to the {@link SyncDoneControl}
   * contained in a response.
   */
  public static class Response
  {

    /** Response that this class wraps. */
    private final org.ldaptive.Response<SearchResult> response;

    /** Control to search the response for. */
    private final SyncDoneControl syncDoneControl;


    /**
     * Creates a new response. If the supplied response contains a {@link
     * SyncDoneControl} it is made available via {@link #getSyncDoneControl()}.
     *
     * @param  res  to search for sync done control in
     */
    public Response(final org.ldaptive.Response<SearchResult> res)
    {
      response = res;
      syncDoneControl = (SyncDoneControl) response.getControl(
        SyncDoneControl.OID);
    }


    /**
     * Returns the underlying response.
     *
     * @return  underlying response
     */
    public org.ldaptive.Response<SearchResult> getResponse()
    {
      return response;
    }


    /**
     * Returns the sync done control or null if no such control exists in the
     * response.
     *
     * @return  sync done control
     */
    public SyncDoneControl getSyncDoneControl()
    {
      return syncDoneControl;
    }


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
      return
        String.format(
          "[%s@%d::response=%s]",
          getClass().getName(),
          hashCode(),
          response);
    }
  }
}
