/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;

/**
 * Synthetic result that encapsulates all the data that can be returned in a search request.
 *
 * @author  Middleware Services
 */
public class SearchResult extends AbstractResult
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10331;

  /** Entries contained in this result. */
  private final Map<String, SearchResultEntry> resultEntries = new LinkedHashMap<>();

  /** Search result references contained in this result. */
  private final Set<SearchResultReference> resultReferences = new LinkedHashSet<>();


  /**
   * Copies the values of the supplied search result done to this synthetic result.
   *
   * @param  result  of values to copy
   */
  public void initialize(final SearchResultDone result)
  {
    setMessageID(result.getMessageID());
    addControls(result.getControls());
    setResultCode(result.getResultCode());
    setMatchedDN(result.getMatchedDN());
    setDiagnosticMessage(result.getDiagnosticMessage());
    addReferralURLs(result.getReferralURLs());
  }


  /**
   * Returns a collection of ldap entry.
   *
   * @return  collection of ldap entry
   */
  public Collection<SearchResultEntry> getEntries()
  {
    return resultEntries.values();
  }


  /**
   * Returns a single entry of this result. If multiple entries exist the first entry returned by the underlying
   * iterator is used. If no entries exist null is returned.
   *
   * @return  search result entry
   */
  public SearchResultEntry getEntry()
  {
    if (resultEntries.isEmpty()) {
      return null;
    }
    return resultEntries.values().iterator().next();
  }


  /**
   * Returns the ldap in this result with the supplied DN.
   *
   * @param  dn  of the entry to return
   *
   * @return  search result entry
   */
  public SearchResultEntry getEntry(final String dn)
  {
    return resultEntries.get(dn.toLowerCase());
  }


  /**
   * Returns the entry DNs in this result.
   *
   * @return  string array of entry DNs
   */
  public Set<String> getEntryDns()
  {
    return resultEntries.keySet();
  }


  /**
   * Adds an entry to this search result.
   *
   * @param  entry  entry to add
   */
  public void addEntry(final SearchResultEntry... entry)
  {
    Stream.of(entry).forEach(e -> resultEntries.put(e.getLdapDN().toLowerCase(), e));
  }


  /**
   * Adds entry(s) to this search result.
   *
   * @param  entries  collection of entries to add
   */
  public void addEntries(final Collection<SearchResultEntry> entries)
  {
    entries.forEach(this::addEntry);
  }


  /**
   * Returns a collection of ldap entry.
   *
   * @return  collection of ldap entry
   */
  public Collection<SearchResultReference> getReferences()
  {
    return resultReferences;
  }


  /**
   * Returns a single search reference of this result. If multiple references exist the first references returned by the
   * underlying iterator is used. If no references exist null is returned.
   *
   * @return  search result references
   */
  public SearchResultReference getReference()
  {
    if (resultReferences.isEmpty()) {
      return null;
    }
    return resultReferences.iterator().next();
  }


  /**
   * Adds a reference to this search result.
   *
   * @param  reference  reference to add
   */
  public void addReference(final SearchResultReference... reference)
  {
    Collections.addAll(resultReferences, reference);
  }


  /**
   * Adds references(s) to this search result.
   *
   * @param  references  collection of references to add
   */
  public void addReferences(final Collection<SearchResultReference> references)
  {
    references.forEach(this::addReference);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SearchResult) {
      final SearchResult v = (SearchResult) o;
      return LdapUtils.areEqual(resultEntries, v.resultEntries) &&
        LdapUtils.areEqual(resultReferences, v.resultReferences);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, resultEntries.values(), resultReferences);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("entries=").append(resultEntries.values()).append(", ")
      .append("references=").append(resultReferences).toString();
  }


  // CheckStyle:OFF
  protected static class Builder extends AbstractResult.AbstractBuilder<Builder, SearchResult>
  {


    public Builder()
    {
      super(new SearchResult());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder entry(final SearchResultEntry e)
    {
      object.addEntry(e);
      return this;
    }


    public Builder reference(final SearchResultReference r)
    {
      object.addReference(r);
      return this;
    }
  }
  // CheckStyle:ON
}
