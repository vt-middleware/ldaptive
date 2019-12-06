/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.asn1.DERBuffer;
import org.ldaptive.asn1.DERParser;

/**
 * Response that encapsulates the result elements of a search request. This class formally decodes the SearchResultDone
 * LDAP message defined as:
 *
 * <pre>
   SearchResultDone ::= [APPLICATION 5] LDAPResult
 * </pre>
 *
 * @author  Middleware Services
 */
public class SearchResponse extends AbstractResult
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 5;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 10301;

  /** Entries contained in this result. */
  private final Map<String, LdapEntry> resultEntries = new LinkedHashMap<>();

  /** Search result references contained in this result. */
  private final Set<SearchResultReference> resultReferences = new LinkedHashSet<>();


  /**
   * Default constructor.
   */
  public SearchResponse() {}


  /**
   * Creates a new search result done.
   *
   * @param  buffer  to decode
   */
  public SearchResponse(final DERBuffer buffer)
  {
    final DERParser parser = new DERParser();
    parser.registerHandler(MessageIDHandler.PATH, new MessageIDHandler(this));
    parser.registerHandler("/SEQ/APP(5)/ENUM[0]", new ResultCodeHandler(this));
    parser.registerHandler("/SEQ/APP(5)/OCTSTR[1]", new MatchedDNHandler(this));
    parser.registerHandler("/SEQ/APP(5)/OCTSTR[2]", new DiagnosticMessageHandler(this));
    parser.registerHandler("/SEQ/APP(5)/CTX(3)/OCTSTR[0]", new ReferralHandler(this));
    parser.registerHandler(ControlsHandler.PATH, new ControlsHandler(this));
    parser.parse(buffer);
  }


  /**
   * Copies the values of the supplied search result done to this synthetic result.
   *
   * @param  result  of values to copy
   */
  public void initialize(final SearchResponse result)
  {
    copyValues(result);
  }


  /**
   * Returns a collection of ldap entry.
   *
   * @return  collection of ldap entry
   */
  public Collection<LdapEntry> getEntries()
  {
    return resultEntries.values();
  }


  /**
   * Returns a single entry of this result. If multiple entries exist the first entry returned by the underlying
   * iterator is used. If no entries exist null is returned.
   *
   * @return  search result entry
   */
  public LdapEntry getEntry()
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
  public LdapEntry getEntry(final String dn)
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
  public void addEntries(final LdapEntry... entry)
  {
    Stream.of(entry).forEach(e -> resultEntries.put(e.getDn().toLowerCase(), e));
  }


  /**
   * Adds entry(s) to this search result.
   *
   * @param  entries  collection of entries to add
   */
  public void addEntries(final Collection<LdapEntry> entries)
  {
    entries.forEach(e -> resultEntries.put(e.getDn().toLowerCase(), e));
  }


  /**
   * Returns the number of entries in this search result.
   *
   * @return  number of entries in this search result
   */
  public int entrySize()
  {
    return resultEntries.size();
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
  public void addReferences(final SearchResultReference... reference)
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
    references.forEach(resultReferences::add);
  }


  /**
   * Returns the number of references in this search result.
   *
   * @return  number of references in this search result
   */
  public int referenceSize()
  {
    return resultReferences.size();
  }


  /**
   * Returns a portion of this result between the specified fromIndex, inclusive, and toIndex, exclusive. If fromIndex
   * and toIndex are equal, the return result is empty. The result of this method is undefined for unordered results.
   *
   * @param  fromIndex  low endpoint of the search result (inclusive)
   * @param  toIndex  high endpoint of the search result (exclusive)
   *
   * @return  portion of this search result
   *
   * @throws  IndexOutOfBoundsException  for illegal index values
   */
  public SearchResponse subResult(final int fromIndex, final int toIndex)
  {
    if (fromIndex < 0 || toIndex > resultEntries.size() || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException("Illegal index value");
    }

    final SearchResponse result = new SearchResponse();
    if (resultEntries.isEmpty() || fromIndex == toIndex) {
      return result;
    }

    int i = 0;
    for (Map.Entry<String, LdapEntry> e : resultEntries.entrySet()) {
      if (i >= fromIndex && i < toIndex) {
        result.addEntries(e.getValue());
      }
      i++;
    }
    return result;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SearchResponse && super.equals(o)) {
      final SearchResponse v = (SearchResponse) o;
      return LdapUtils.areEqual(resultEntries, v.resultEntries) &&
        LdapUtils.areEqual(resultReferences, v.resultReferences);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        getMessageID(),
        getControls(),
        getResultCode(),
        getMatchedDN(),
        getDiagnosticMessage(),
        getReferralURLs(),
        resultEntries.values(),
        resultReferences);
  }


  @Override
  public String toString()
  {
    return new StringBuilder(
      super.toString()).append(", ")
      .append("entries=").append(resultEntries.values()).append(", ")
      .append("references=").append(resultReferences).toString();
  }


  /**
   * Returns a new response whose entries are sorted naturally by DN. Each attribute and each attribute value are also
   * sorted. See {@link LdapEntry#sort(LdapEntry)} and {@link LdapAttribute#sort(LdapAttribute)}.
   *
   * @param  sr  response to sort
   *
   * @return  sorted response
   */
  public static SearchResponse sort(final SearchResponse sr)
  {
    final SearchResponse sorted = new SearchResponse();
    sorted.copyValues(sr);
    sorted.addEntries(sr.getEntries().stream()
      .map(LdapEntry::sort)
      .sorted(Comparator.comparing(LdapEntry::getDn, String.CASE_INSENSITIVE_ORDER))
      .collect(Collectors.toCollection(LinkedHashSet::new)));
    sorted.addReferences(sr.getReferences().stream()
      .map(SearchResultReference::sort)
      .sorted(Comparator.comparing(SearchResultReference::hashCode))
      .collect(Collectors.toCollection(LinkedHashSet::new)));
    return sorted;
  }


  /**
   * Merges the entries in the supplied result into a single entry. This method always returns a search result of size
   * zero or one.
   *
   * @param  result  search result containing entries to merge
   *
   * @return  search result containing a single merged entry
   */
  public static SearchResponse merge(final SearchResponse result)
  {
    LdapEntry mergedEntry = null;
    if (result != null) {
      for (LdapEntry entry : result.getEntries()) {
        if (mergedEntry == null) {
          mergedEntry = entry;
        } else {
          for (LdapAttribute la : entry.getAttributes()) {
            final LdapAttribute oldAttr = mergedEntry.getAttribute(la.getName());
            if (oldAttr == null) {
              mergedEntry.addAttributes(la);
            } else {
              if (oldAttr.isBinary()) {
                oldAttr.addBinaryValues(la.getBinaryValues());
              } else {
                oldAttr.addStringValues(la.getStringValues());
              }
            }
          }
        }
      }
    }
    return mergedEntry != null ?
      builder()
        .entry(
          LdapEntry.builder().dn(mergedEntry.getDn()).attributes(mergedEntry.getAttributes()).build())
        .build() :
      new SearchResponse();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder extends AbstractResult.AbstractBuilder<Builder, SearchResponse>
  {


    protected Builder()
    {
      super(new SearchResponse());
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    public Builder entry(final LdapEntry... e)
    {
      object.addEntries(e);
      return this;
    }


    public Builder reference(final SearchResultReference... r)
    {
      object.addReferences(r);
      return this;
    }
  }
  // CheckStyle:ON
}
