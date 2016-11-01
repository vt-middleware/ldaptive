/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Simple bean representing an ldap search result. Contains a map of entry DN to ldap entry.
 *
 * @author  Middleware Services
 */
public class SearchResult extends AbstractLdapBean
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 337;

  /** serial version uid. */
  private static final long serialVersionUID = -4686725717997623766L;

  /** Entries contained in this result. */
  private final Map<String, LdapEntry> resultEntries;

  /** References contained in this result. */
  private final Collection<SearchReference> searchReferences;


  /** Default constructor. */
  public SearchResult()
  {
    this(SortBehavior.getDefaultSortBehavior());
  }


  /**
   * Creates a new search result.
   *
   * @param  sb  sort behavior of the results
   */
  public SearchResult(final SortBehavior sb)
  {
    super(sb);
    if (SortBehavior.UNORDERED == sb) {
      resultEntries = new HashMap<>();
      searchReferences = new HashSet<>();
    } else if (SortBehavior.ORDERED == sb) {
      resultEntries = new LinkedHashMap<>();
      searchReferences = new LinkedHashSet<>();
    } else if (SortBehavior.SORTED == sb) {
      resultEntries = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
      searchReferences = new TreeSet<>(
        new Comparator<SearchReference>() {
          @Override
          public int compare(final SearchReference ref1, final SearchReference ref2)
          {
            return Arrays.toString(ref1.getReferralUrls()).compareTo(Arrays.toString(ref2.getReferralUrls()));
          }
        });
    } else {
      throw new IllegalArgumentException("Unknown sort behavior: " + sb);
    }
  }


  /**
   * Creates a new search result.
   *
   * @param  entry  ldap entry
   */
  public SearchResult(final LdapEntry... entry)
  {
    this();
    for (LdapEntry e : entry) {
      addEntry(e);
    }
  }


  /**
   * Creates a new search result.
   *
   * @param  entries  collection of ldap entries
   */
  public SearchResult(final Collection<LdapEntry> entries)
  {
    this();
    addEntries(entries);
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
   * @return  single entry
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
   * @return  ldap entry
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
  public String[] getEntryDns()
  {
    return resultEntries.keySet().toArray(new String[resultEntries.keySet().size()]);
  }


  /**
   * Adds an entry to this search result.
   *
   * @param  entry  entry to add
   */
  public void addEntry(final LdapEntry... entry)
  {
    for (LdapEntry e : entry) {
      resultEntries.put(e.getDn().toLowerCase(), e);
    }
  }


  /**
   * Adds entry(s) to this search result.
   *
   * @param  entries  collection of entries to add
   */
  public void addEntries(final Collection<LdapEntry> entries)
  {
    for (LdapEntry e : entries) {
      addEntry(e);
    }
  }


  /**
   * Removes an entry from this search result.
   *
   * @param  entry  entry to remove
   */
  public void removeEntry(final LdapEntry... entry)
  {
    for (LdapEntry e : entry) {
      resultEntries.remove(e.getDn().toLowerCase());
    }
  }


  /**
   * Removes the entry with the supplied dn from this search result.
   *
   * @param  dn  of entry to remove
   */
  public void removeEntry(final String dn)
  {
    resultEntries.remove(dn.toLowerCase());
  }


  /**
   * Removes the entry(s) from this search result.
   *
   * @param  entries  collection of ldap entries to remove
   */
  public void removeEntries(final Collection<LdapEntry> entries)
  {
    for (LdapEntry le : entries) {
      removeEntry(le);
    }
  }


  /**
   * Returns a collection of ldap entry.
   *
   * @return  collection of ldap entry
   */
  public Collection<SearchReference> getReferences()
  {
    return searchReferences;
  }


  /**
   * Returns a single search reference of this result. If multiple references exist the first references returned by the
   * underlying iterator is used. If no references exist null is returned.
   *
   * @return  single search references
   */
  public SearchReference getReference()
  {
    if (searchReferences.isEmpty()) {
      return null;
    }
    return searchReferences.iterator().next();
  }


  /**
   * Adds a reference to this search result.
   *
   * @param  reference  reference to add
   */
  public void addReference(final SearchReference... reference)
  {
    Collections.addAll(searchReferences, reference);
  }


  /**
   * Adds references(s) to this search result.
   *
   * @param  references  collection of references to add
   */
  public void addReferences(final Collection<SearchReference> references)
  {
    for (SearchReference r : references) {
      addReference(r);
    }
  }


  /**
   * Removes a reference from this search result.
   *
   * @param  reference  reference to remove
   */
  public void removeReference(final SearchReference... reference)
  {
    for (SearchReference r : reference) {
      searchReferences.remove(r);
    }
  }


  /**
   * Removes the references(s) from this search result.
   *
   * @param  references  collection of search references to remove
   */
  public void removeReferences(final Collection<SearchReference> references)
  {
    for (SearchReference r : references) {
      removeReference(r);
    }
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
  public SearchResult subResult(final int fromIndex, final int toIndex)
  {
    if (fromIndex < 0 || toIndex > resultEntries.size() || fromIndex > toIndex) {
      throw new IndexOutOfBoundsException("Illegal index value");
    }

    final SearchResult result = new SearchResult(getSortBehavior());
    if (resultEntries.isEmpty() || fromIndex == toIndex) {
      return result;
    }

    int i = 0;
    for (Map.Entry<String, LdapEntry> e : resultEntries.entrySet()) {
      if (i >= fromIndex && i < toIndex) {
        result.addEntry(e.getValue());
      }
      i++;
    }
    return result;
  }


  /**
   * Returns the number of entries in this search result.
   *
   * @return  number of entries in this search result
   */
  public int size()
  {
    return resultEntries.size();
  }


  /** Removes all the entries in this search result. */
  public void clear()
  {
    resultEntries.clear();
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
             LdapUtils.areEqual(searchReferences, v.searchReferences);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, resultEntries.values(), searchReferences);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::entries=%s, references=%s]",
        getClass().getName(),
        hashCode(),
        resultEntries.values(),
        searchReferences);
  }


  /**
   * Merges the entries in the supplied result into a single entry. This method always returns a search result of size
   * zero or one.
   *
   * @param  result  search result containing entries to merge
   *
   * @return  search result containing a single merged entry
   */
  public static SearchResult mergeEntries(final SearchResult result)
  {
    LdapEntry mergedEntry = null;
    if (result != null) {
      for (LdapEntry le : result.getEntries()) {
        if (mergedEntry == null) {
          mergedEntry = le;
        } else {
          for (LdapAttribute la : le.getAttributes()) {
            final LdapAttribute oldAttr = mergedEntry.getAttribute(la.getName());
            if (oldAttr == null) {
              mergedEntry.addAttribute(la);
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
    return mergedEntry != null ? new SearchResult(mergedEntry) : new SearchResult();
  }
}
