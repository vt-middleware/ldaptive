/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.Response;
import org.ldaptive.SearchEntry;
import org.ldaptive.async.AsyncRequest;
import org.ldaptive.control.EntryChangeNotificationControl;

/**
 * Contains data returned when using the persistent search request control.
 *
 * @author  Middleware Services
 */
public class PersistentSearchItem
{

  /** Async request from the search operation. */
  private final AsyncRequest asyncRequest;

  /** Entry contained in this persistent search item. */
  private final Entry persistentSearchEntry;

  /** Response contained in this persistent search item. */
  private final Response persistentSearchResponse;

  /** Exception thrown by the search operation. */
  private final Exception persistentSearchException;


  /**
   * Creates a new persistent search item.
   *
   * @param  request  that represents this item
   */
  public PersistentSearchItem(final AsyncRequest request)
  {
    asyncRequest = request;
    persistentSearchEntry = null;
    persistentSearchResponse = null;
    persistentSearchException = null;
  }


  /**
   * Creates a new persistent search item.
   *
   * @param  entry  that represents this item
   */
  public PersistentSearchItem(final Entry entry)
  {
    asyncRequest = null;
    persistentSearchEntry = entry;
    persistentSearchResponse = null;
    persistentSearchException = null;
  }


  /**
   * Creates a new persistent search item.
   *
   * @param  response  that represents this item
   */
  public PersistentSearchItem(final Response response)
  {
    asyncRequest = null;
    persistentSearchEntry = null;
    persistentSearchResponse = response;
    persistentSearchException = null;
  }


  /**
   * Creates a new persistent search item.
   *
   * @param  exception  that represents this item
   */
  public PersistentSearchItem(final Exception exception)
  {
    asyncRequest = null;
    persistentSearchEntry = null;
    persistentSearchResponse = null;
    persistentSearchException = exception;
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
    return persistentSearchEntry != null;
  }


  /**
   * Returns the entry contained in this item or null if this item does not
   * contain an entry.
   *
   * @return  search entry
   */
  public Entry getEntry()
  {
    return persistentSearchEntry;
  }


  /**
   * Returns whether this item represents a response.
   *
   * @return  whether this item represents a response
   */
  public boolean isResponse()
  {
    return persistentSearchResponse != null;
  }


  /**
   * Returns the response contained in this item or null if this item does not
   * contain a response.
   *
   * @return  response
   */
  public Response getResponse()
  {
    return persistentSearchResponse;
  }


  /**
   * Returns whether this item represents an exception.
   *
   * @return  whether this item represents an exception
   */
  public boolean isException()
  {
    return persistentSearchException != null;
  }


  /**
   * Returns the exception contained in this item or null if this item does not
   * contain an exception.
   *
   * @return  exception
   */
  public Exception getException()
  {
    return persistentSearchException;
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
        "[%s@%d::persistentSearchEntry=%s]",
        getClass().getName(),
        hashCode(),
        persistentSearchEntry);
    } else if (isResponse()) {
      s = String.format(
        "[%s@%d::persistentSearchResponse=%s]",
        getClass().getName(),
        hashCode(),
        persistentSearchResponse);
    } else if (isException()) {
      s = String.format(
        "[%s@%d::persistentSearchException=%s]",
        getClass().getName(),
        hashCode(),
        persistentSearchException);
    } else {
      s = String.format("[%s@%d]", getClass().getName(), hashCode());
    }
    return s;
  }


  /**
   * Wrapper class that provides easy access to the {@link
   * EntryChangeNotificationControl} contained in a search entry.
   */
  public static class Entry
  {

    /** Search entry that this class wraps. */
    private final SearchEntry searchEntry;

    /** Control to search the entry for. */
    private final EntryChangeNotificationControl entryChangeNotificationControl;


    /**
     * Creates a new entry. If the supplied search entry contains a {@link
     * EntryChangeNotificationControl} it is made available via {@link
     * #getEntryChangeNotificationControl()}.
     *
     * @param  entry  to search for entry change notification control in
     */
    public Entry(final SearchEntry entry)
    {
      searchEntry = entry;
      entryChangeNotificationControl = (EntryChangeNotificationControl)
        entry.getControl(EntryChangeNotificationControl.OID);
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
     * Returns the entry change notification control or null if no such control
     * exists in the search entry.
     *
     * @return  entry change notification control
     */
    public EntryChangeNotificationControl getEntryChangeNotificationControl()
    {
      return entryChangeNotificationControl;
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
}
