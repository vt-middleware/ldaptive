/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.transport.AbstractMessageFunctionalEntryHandler;

/**
 * This recursively searches based on a supplied attribute and merges those results into the original entry. For the
 * following LDIF:
 *
 * <pre>
   dn: uugid=group1,ou=groups,dc=ldaptive,dc=org
   uugid: group1
   member: uugid=group2,ou=groups,dc=ldaptive,dc=org

   dn: uugid=group2,ou=groups,dc=ldaptive,dc=org
   uugid: group2
 * </pre>
 *
 * <p>With the following code:</p>
 *
 * <pre>
   RecursiveResultHandler reh = new RecursiveResultHandler("member", "uugid");
 * </pre>
 *
 * <p>Will produce this result for the query (uugid=group1):</p>
 *
 * <pre>
   dn: uugid=group1,ou=groups,dc=ldaptive,dc=org
   uugid: group1
   uugid: group2
   member: uugid=group2,ou=groups,dc=ldaptive,dc=org
 * </pre>
 *
 * This handler should only be used with the {@link org.ldaptive.SearchOperation#execute()} method since it leverages
 * the connection to make further searches.
 *
 * @author  Middleware Services
 */
public class RecursiveResultHandler extends AbstractMessageFunctionalEntryHandler<SearchResponse>
  implements SearchResultHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 829;

  /** Attribute to recursively search on. */
  private String searchAttribute;

  /** Attribute(s) to merge. */
  private String[] mergeAttributes;

  /** Attributes to return when searching, mergeAttributes + searchAttribute. */
  private String[] retAttrs;


  /** Default constructor. */
  public RecursiveResultHandler() {}


  /**
   * Creates a new recursive entry handler.
   *
   * @param  searchAttr  attribute to search on
   * @param  mergeAttrs  attribute names to merge
   */
  public RecursiveResultHandler(final String searchAttr, final String... mergeAttrs)
  {
    searchAttribute = searchAttr;
    mergeAttributes = mergeAttrs;
    initializeReturnAttributes();
  }


  /**
   * Returns the attribute name that will be recursively searched on.
   *
   * @return  attribute name
   */
  public final String getSearchAttribute()
  {
    return searchAttribute;
  }


  /**
   * Sets the attribute name that will be recursively searched on.
   *
   * @param  name  of the search attribute
   */
  public final void setSearchAttribute(final String name)
  {
    assertMutable();
    searchAttribute = name;
    initializeReturnAttributes();
  }


  /**
   * Returns the attribute names that will be merged by the recursive search.
   *
   * @return  attribute names
   */
  public final String[] getMergeAttributes()
  {
    return LdapUtils.copyArray(mergeAttributes);
  }


  /**
   * Sets the attribute name that will be merged by the recursive search.
   *
   * @param  mergeAttrs  attribute names to merge
   */
  public final void setMergeAttributes(final String... mergeAttrs)
  {
    assertMutable();
    mergeAttributes = LdapUtils.copyArray(mergeAttrs);
    initializeReturnAttributes();
  }


  /**
   * Initializes the return attributes array. Must be called after both searchAttribute and mergeAttributes have been
   * set.
   */
  protected void initializeReturnAttributes()
  {
    if (mergeAttributes != null && searchAttribute != null) {
      // return attributes must include the search attribute
      retAttrs = new String[mergeAttributes.length + 1];
      System.arraycopy(mergeAttributes, 0, retAttrs, 0, mergeAttributes.length);
      retAttrs[retAttrs.length - 1] = searchAttribute;
    }
  }


  @Override
  public SearchResponse apply(final SearchResponse response)
  {
    response.getEntries().forEach(this::handleEntry);
    return response;
  }


  @Override
  public void handleEntry(final LdapEntry entry)
  {
    // Recursively searches a list of attributes and merges those results with
    // the existing entry.
    final List<String> searchedDns = new ArrayList<>();
    try {
      if (entry.getAttribute(searchAttribute) != null) {
        searchedDns.add(entry.getDn());
        readSearchAttribute(entry, searchedDns);
      } else {
        recursiveSearch(entry.getDn(), entry, searchedDns);
      }
    } catch (LdapException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Reads the values of {@link #searchAttribute} from the supplied attributes and calls {@link #recursiveSearch} for
   * each.
   *
   * @param  entry  to read
   * @param  searchedDns  list of DNs whose attributes have been read
   *
   * @throws  LdapException  if an error occurs performing a search
   */
  private void readSearchAttribute(final LdapEntry entry, final List<String> searchedDns)
    throws LdapException
  {
    if (entry != null) {
      final LdapAttribute attr = entry.getAttribute(searchAttribute);
      if (attr != null && !attr.isBinary()) {
        final Set<String> values = new HashSet<>(attr.getStringValues());
        for (String s : values) {
          recursiveSearch(s, entry, searchedDns);
        }
      }
    }
  }


  /**
   * Recursively gets the attribute(s) {@link #mergeAttributes} for the supplied dn and adds the values to the supplied
   * attributes.
   *
   * @param  dn  to get attribute(s) for
   * @param  entry  to merge with
   * @param  searchedDns  list of DNs that have been searched for
   *
   * @throws  LdapException  if an error occurs performing a search
   */
  private void recursiveSearch(final String dn, final LdapEntry entry, final List<String> searchedDns)
    throws LdapException
  {
    if (!searchedDns.contains(dn)) {

      LdapEntry newEntry = null;
      final SearchResponse result = performSearch(dn, retAttrs);
      if (result.isSuccess() && result.entrySize() == 1) {
        newEntry = LdapEntry.copy(result.getEntry());
      }
      searchedDns.add(dn);

      if (newEntry != null) {
        // recursively search new attributes
        readSearchAttribute(newEntry, searchedDns);

        // merge new attribute values
        for (String s : mergeAttributes) {
          final LdapAttribute newAttr = newEntry.getAttribute(s);
          if (newAttr != null) {
            final LdapAttribute oldAttr = entry.getAttribute(s);
            if (oldAttr == null) {
              entry.addAttributes(LdapAttribute.copy(newAttr));
            } else {
              if (newAttr.isBinary()) {
                newAttr.getBinaryValues().forEach(oldAttr::addBinaryValues);
              } else {
                newAttr.getStringValues().forEach(oldAttr::addStringValues);
              }
            }
          }
        }
      }
    }
  }


  /**
   * Perform an object scope search on the supplied baseDN and retrieve the supplied return attributes.
   *
   * @param  baseDn  to search on
   * @param  attrs  attributes to return
   *
   * @return  search response
   *
   * @throws  LdapException  if the search operation fails
   */
  protected SearchResponse performSearch(final String baseDn, final String[] attrs)
    throws LdapException
  {
    return getConnection().operation(SearchRequest.objectScopeSearchRequest(baseDn, attrs)).execute();
  }


  @Override
  public RecursiveResultHandler newInstance()
  {
    return new RecursiveResultHandler(searchAttribute, mergeAttributes);
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof RecursiveResultHandler) {
      final RecursiveResultHandler v = (RecursiveResultHandler) o;
      return LdapUtils.areEqual(mergeAttributes, v.mergeAttributes) &&
             LdapUtils.areEqual(retAttrs, v.retAttrs) &&
             LdapUtils.areEqual(searchAttribute, v.searchAttribute);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, mergeAttributes, retAttrs, searchAttribute);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "searchAttribute=" + searchAttribute + ", " +
      "mergeAttributes=" + Arrays.toString(mergeAttributes) + ", " +
      "retAttrs=" + Arrays.toString(retAttrs) + "]";
  }
}
