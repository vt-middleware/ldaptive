/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.provider;

import org.ldaptive.SearchEntry;
import org.ldaptive.SearchReference;
import org.ldaptive.intermediate.IntermediateResponse;

/**
 * Contains the data returned from a search request.
 *
 * @author  Middleware Services
 */
public class SearchItem
{

  /** search entry. */
  private final SearchEntry searchEntry;

  /** search reference. */
  private final SearchReference searchReference;

  /** intermediate response. */
  private final IntermediateResponse intermediateResponse;


  /**
   * Creates a new search item.
   *
   * @param  se  ldap entry
   */
  public SearchItem(final SearchEntry se)
  {
    searchEntry = se;
    searchReference = null;
    intermediateResponse = null;
  }


  /**
   * Creates a new search item.
   *
   * @param  sr  searchReference
   */
  public SearchItem(final SearchReference sr)
  {
    searchEntry = null;
    searchReference = sr;
    intermediateResponse = null;
  }


  /**
   * Creates a new search item.
   *
   * @param  ir  intermediate response
   */
  public SearchItem(final IntermediateResponse ir)
  {
    searchEntry = null;
    searchReference = null;
    intermediateResponse = ir;
  }


  /**
   * Returns whether this search item contains a search entry.
   *
   * @return  whether this search item contains a search entry
   */
  public boolean isSearchEntry()
  {
    return searchEntry != null;
  }


  /**
   * Returns the search entry in this search item or null if this search item
   * does not contain a search entry.
   *
   * @return  search entry
   */
  public SearchEntry getSearchEntry()
  {
    return searchEntry;
  }


  /**
   * Returns whether this search item contains a search reference.
   *
   * @return  whether this search item contains a search reference
   */
  public boolean isSearchReference()
  {
    return searchReference != null;
  }


  /**
   * Returns the search reference in this search item or null if this search
   * item does not contain a search reference.
   *
   * @return  searchReference
   */
  public SearchReference getSearchReference()
  {
    return searchReference;
  }


  /**
   * Returns whether this search item contains an intermediate response.
   *
   * @return  whether this search item contains an intermediate response
   */
  public boolean isIntermediateResponse()
  {
    return intermediateResponse != null;
  }


  /**
   * Returns the intermediate response in this search item or null if this
   * search item does not contain an intermediate response.
   *
   * @return  intermediate response
   */
  public IntermediateResponse getIntermediateResponse()
  {
    return intermediateResponse;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    String s;
    if (isSearchEntry()) {
      s = String.format(
        "[%s@%d::searchEntry=%s]",
        getClass().getName(),
        hashCode(),
        searchEntry);
    } else if (isSearchReference()) {
      s = String.format(
        "[%s@%d::searchReference=%s]",
        getClass().getName(),
        hashCode(),
        searchReference);
    } else if (isIntermediateResponse()) {
      s = String.format(
        "[%s@%d::intermediateResponse=%s]",
        getClass().getName(),
        hashCode(),
        intermediateResponse);
    } else {
      s = String.format("[%s@%d]", getClass().getName(), hashCode());
    }
    return s;
  }
}
