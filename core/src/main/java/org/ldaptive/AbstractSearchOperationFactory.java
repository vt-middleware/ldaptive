/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.handler.LdapEntryHandler;
import org.ldaptive.handler.SearchReferenceHandler;
import org.ldaptive.handler.SearchResultHandler;

/**
 * Base class for classes that need to configure a search operation.
 *
 * @author  Middleware Services
 */
public abstract class AbstractSearchOperationFactory extends AbstractOperationFactory<SearchRequest, SearchResponse>
{

  /** Functions to handle entries. */
  private LdapEntryHandler[] entryHandlers;

  /** Functions to handle response references. */
  private SearchReferenceHandler[] referenceHandlers;

  /** Functions to handle search response results. */
  private SearchResultHandler[] searchResultHandlers;


  @Override
  public void freeze()
  {
    super.freeze();
    freeze(entryHandlers);
    freeze(referenceHandlers);
    freeze(searchResultHandlers);
  }


  /**
   * Returns the search entry handlers.
   *
   * @return  search entry handlers
   */
  public LdapEntryHandler[] getEntryHandlers()
  {
    return LdapUtils.copyArray(entryHandlers);
  }


  /**
   * Sets the search entry handlers.
   *
   * @param  handlers  search entry handlers
   */
  public void setEntryHandlers(final LdapEntryHandler... handlers)
  {
    assertMutable();
    entryHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the search reference handlers.
   *
   * @return  search reference handlers
   */
  public SearchReferenceHandler[] getReferenceHandlers()
  {
    return LdapUtils.copyArray(referenceHandlers);
  }


  /**
   * Sets the search reference handlers.
   *
   * @param  handlers  search reference handlers
   */
  public void setReferenceHandlers(final SearchReferenceHandler... handlers)
  {
    assertMutable();
    referenceHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Returns the search result handlers.
   *
   * @return  search result handlers
   */
  public SearchResultHandler[] getSearchResultHandlers()
  {
    return LdapUtils.copyArray(searchResultHandlers);
  }


  /**
   * Sets the search result handlers.
   *
   * @param  handlers  search result handlers
   */
  public void setSearchResultHandlers(final SearchResultHandler... handlers)
  {
    assertMutable();
    searchResultHandlers = LdapUtils.copyArray(handlers);
  }


  /**
   * Creates a new search operation configured with the properties on this factory.
   *
   * @return  search operation
   */
  protected SearchOperation createSearchOperation()
  {
    return createSearchOperation(getConnectionFactory());
  }


  /**
   * Creates a new search operation configured with the properties on this factory.
   *
   * @param  cf  connection factory to set on the search operation
   *
   * @return  search operation
   */
  protected SearchOperation createSearchOperation(final ConnectionFactory cf)
  {
    final SearchOperation op = new SearchOperation(cf);
    initializeOperation(op);
    if (entryHandlers != null) {
      op.setEntryHandlers(entryHandlers);
    }
    if (referenceHandlers != null) {
      op.setReferenceHandlers(referenceHandlers);
    }
    if (searchResultHandlers != null) {
      op.setSearchResultHandlers(searchResultHandlers);
    }
    return op;
  }


  @Override
  public String toString()
  {
    return super.toString() + ", " +
      (entryHandlers != null ? "entryHandlers=" + Arrays.toString(entryHandlers) + ", " : "") +
      (referenceHandlers != null ? "referenceHandlers=" + Arrays.toString(referenceHandlers) + ", " : "") +
      (searchResultHandlers != null ? "searchResultHandlers=" + Arrays.toString(searchResultHandlers) : "");
  }
}
