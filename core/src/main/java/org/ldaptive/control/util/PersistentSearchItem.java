/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.control.util;

import org.ldaptive.LdapEntry;
import org.ldaptive.Result;
import org.ldaptive.control.EntryChangeNotificationControl;

/**
 * Contains data returned when using the persistent search request control.
 *
 * @author  Middleware Services
 */
public class PersistentSearchItem
{

  /** Entry contained in this persistent search item. */
  private final Entry persistentSearchEntry;

  /** Result contained in this persistent search item. */
  private final Result persistentSearchResult;

  /** Exception thrown by the search operation. */
  private final Exception persistentSearchException;


  /**
   * Creates a new persistent search item.
   *
   * @param  entry  that represents this item
   */
  public PersistentSearchItem(final Entry entry)
  {
    persistentSearchEntry = entry;
    persistentSearchResult = null;
    persistentSearchException = null;
  }


  /**
   * Creates a new persistent search item.
   *
   * @param  result  that represents this item
   */
  public PersistentSearchItem(final Result result)
  {
    persistentSearchEntry = null;
    persistentSearchResult = result;
    persistentSearchException = null;
  }


  /**
   * Creates a new persistent search item.
   *
   * @param  exception  that represents this item
   */
  public PersistentSearchItem(final Exception exception)
  {
    persistentSearchEntry = null;
    persistentSearchResult = null;
    persistentSearchException = exception;
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
   * Returns the entry contained in this item or null if this item does not contain an entry.
   *
   * @return  search entry
   */
  public Entry getEntry()
  {
    return persistentSearchEntry;
  }


  /**
   * Returns whether this item represents a result.
   *
   * @return  whether this item represents a result
   */
  public boolean isResult()
  {
    return persistentSearchResult != null;
  }


  /**
   * Returns the result contained in this item or null if this item does not contain a result.
   *
   * @return  result
   */
  public Result getResult()
  {
    return persistentSearchResult;
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
   * Returns the exception contained in this item or null if this item does not contain an exception.
   *
   * @return  exception
   */
  public Exception getException()
  {
    return persistentSearchException;
  }


  @Override
  public String toString()
  {
    final StringBuilder sb = new StringBuilder("[").append(getClass().getName()).append("@").append(hashCode());
    if (isEntry()) {
      sb.append("::persistentSearchEntry=").append(persistentSearchEntry).append("]");
    } else if (isResult()) {
      sb.append("::persistentSearchResult=").append(persistentSearchResult).append("]");
    } else if (isException()) {
      sb.append("::persistentSearchException=").append(persistentSearchException).append("]");
    } else {
      sb.append("]");
    }
    return sb.toString();
  }


  /**
   * Wrapper class that provides easy access to the {@link EntryChangeNotificationControl} contained in a search entry.
   */
  public static class Entry
  {

    /** Search entry that this class wraps. */
    private final LdapEntry searchEntry;

    /** Control to search the entry for. */
    private final EntryChangeNotificationControl entryChangeNotificationControl;


    /**
     * Creates a new entry. If the supplied search entry contains a {@link EntryChangeNotificationControl} it is made
     * available via {@link #getEntryChangeNotificationControl()}.
     *
     * @param  entry  to search for entry change notification control in
     */
    public Entry(final LdapEntry entry)
    {
      searchEntry = entry;
      entryChangeNotificationControl = (EntryChangeNotificationControl) entry.getControl(
        EntryChangeNotificationControl.OID);
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
     * Returns the entry change notification control or null if no such control exists in the search entry.
     *
     * @return  entry change notification control
     */
    public EntryChangeNotificationControl getEntryChangeNotificationControl()
    {
      return entryChangeNotificationControl;
    }


    @Override
    public String toString()
    {
      return new StringBuilder("[").append(
        getClass().getName()).append("@").append(hashCode()).append("::")
        .append("searchEntry=").append(searchEntry).append("]").toString();
    }
  }
}
