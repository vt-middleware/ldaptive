/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.LdapEntry;
import org.ldaptive.control.SyncDoneControl;
import org.ldaptive.control.SyncStateControl;
import org.ldaptive.extended.SyncInfoMessage;

/**
 * Contains data returned when using the sync repl search control.
 *
 * @author  Middleware Services
 */
public class SyncReplItem
{

  /** Entry contained in this sync repl item. */
  private final Entry syncReplEntry;

  /** Message contained in this sync repl item. */
  private final SyncInfoMessage syncInfoMessage;

  /** Result contained in this sync repl item. */
  private final Result syncReplResult;

  /** Exception thrown by the search operation. */
  private final Exception syncReplException;


  /**
   * Creates a new sync repl item.
   *
   * @param  entry  that represents this item
   */
  public SyncReplItem(final Entry entry)
  {
    syncReplEntry = entry;
    syncInfoMessage = null;
    syncReplResult = null;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  message  that represents this item
   */
  public SyncReplItem(final SyncInfoMessage message)
  {
    syncReplEntry = null;
    syncInfoMessage = message;
    syncReplResult = null;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  result  that represents this item
   */
  public SyncReplItem(final Result result)
  {
    syncReplEntry = null;
    syncInfoMessage = null;
    syncReplResult = result;
    syncReplException = null;
  }


  /**
   * Creates a new sync repl item.
   *
   * @param  exception  that represents this item
   */
  public SyncReplItem(final Exception exception)
  {
    syncReplEntry = null;
    syncInfoMessage = null;
    syncReplResult = null;
    syncReplException = exception;
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
   * Returns the entry contained in this item or null if this item does not contain an entry.
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
   * Returns the intermediate message contained in this item or null if this item does not contain a message.
   *
   * @return  sync info message
   */
  public SyncInfoMessage getMessage()
  {
    return syncInfoMessage;
  }


  /**
   * Returns whether this item represents a result.
   *
   * @return  whether this item represents a result
   */
  public boolean isResult()
  {
    return syncReplResult != null;
  }


  /**
   * Returns the result contained in this item or null if this item does not contain a result.
   *
   * @return  result
   */
  public Result getResult()
  {
    return syncReplResult;
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
   * Returns the exception contained in this item or null if this item does not contain an exception.
   *
   * @return  exception
   */
  public Exception getException()
  {
    return syncReplException;
  }


  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder("[").append(getClass().getName()).append("@").append(hashCode());
    if (isEntry()) {
      sb.append("::syncReplEntry=").append(syncReplEntry).append("]");
    } else if (isMessage()) {
      sb.append("::syncInfoMessage=").append(syncInfoMessage).append("]");
    } else if (isResult()) {
      sb.append("::syncReplResult=").append(syncReplResult).append("]");
    } else if (isException()) {
      sb.append("::syncReplException=").append(syncReplException).append("]");
    } else {
      sb.append("]");
    }
    return sb.toString();
  }


  /** Wrapper class that provides easy access to the {@link SyncStateControl} contained in a search entry. */
  public static class Entry
  {

    /** Search entry that this class wraps. */
    private final LdapEntry searchEntry;

    /** Control to search the entry for. */
    private final SyncStateControl syncStateControl;


    /**
     * Creates a new entry. If the supplied search entry contains a {@link SyncStateControl} it is made available via
     * {@link #getSyncStateControl()}.
     *
     * @param  entry  to search for sync state control in
     */
    public Entry(final LdapEntry entry)
    {
      searchEntry = entry;
      syncStateControl = (SyncStateControl) entry.getControl(SyncStateControl.OID);
    }


    /**
     * Returns the underlying search entry.
     *
     * @return  underlying search entry
     */
    public LdapEntry getSearchEntry()
    {
      return searchEntry;
    }


    /**
     * Returns the sync state control or null if no such control exists in the search entry.
     *
     * @return  sync state control
     */
    public SyncStateControl getSyncStateControl()
    {
      return syncStateControl;
    }


    @Override
    public String toString()
    {
      return new StringBuilder("[").append(
        getClass().getName()).append("@").append(hashCode()).append("::")
        .append("searchEntry=").append(searchEntry).append("]").toString();
    }
  }


  /** Wrapper class that provides easy access to the {@link SyncDoneControl} contained in a result. */
  public static class Result
  {

    /** Result that this class wraps. */
    private final org.ldaptive.Result result;

    /** Control to search the response for. */
    private final SyncDoneControl syncDoneControl;


    /**
     * Creates a new response. If the supplied response contains a {@link SyncDoneControl} it is made available via
     * {@link #getSyncDoneControl()}.
     *
     * @param  res  to search for sync done control in
     */
    public Result(final org.ldaptive.Result res)
    {
      result = res;
      syncDoneControl = (SyncDoneControl) result.getControl(SyncDoneControl.OID);
    }


    /**
     * Returns the underlying result.
     *
     * @return  underlying result
     */
    public org.ldaptive.Result getResult()
    {
      return result;
    }


    /**
     * Returns the sync done control or null if no such control exists in the response.
     *
     * @return  sync done control
     */
    public SyncDoneControl getSyncDoneControl()
    {
      return syncDoneControl;
    }


    @Override
    public String toString()
    {
      return new StringBuilder("[").append(
        getClass().getName()).append("@").append(hashCode()).append("::")
        .append("result=").append(result).append("]").toString();
    }
  }
}
