/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;

/**
 * Merges the values of one or more attributes into a single attribute. The merged attribute may or may not already
 * exist on the entry. If it does exist it's existing values will remain intact.
 *
 * @author  Middleware Services
 */
public class MergeAttributeEntryHandler extends AbstractSearchEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 827;

  /** Attribute name to add merge values into. */
  private String mergeAttributeName;

  /** Attribute names to read values from. */
  private String[] attributeNames;


  /**
   * Returns the merge attribute name.
   *
   * @return  merge attribute name
   */
  public String getMergeAttributeName()
  {
    return mergeAttributeName;
  }


  /**
   * Sets the merge attribute name.
   *
   * @param  name  of the merge attribute
   */
  public void setMergeAttributeName(final String name)
  {
    mergeAttributeName = name;
  }


  /**
   * Returns the attribute names.
   *
   * @return  attribute names
   */
  public String[] getAttributeNames()
  {
    return attributeNames;
  }


  /**
   * Sets the attribute names.
   *
   * @param  names  of the attributes
   */
  public void setAttributeNames(final String... names)
  {
    attributeNames = names;
  }


  @Override
  protected void handleAttributes(final Connection conn, final SearchRequest request, final SearchEntry entry)
    throws LdapException
  {
    boolean newAttribute = false;
    LdapAttribute mergedAttribute = entry.getAttribute(mergeAttributeName);
    if (mergedAttribute == null) {
      mergedAttribute = new LdapAttribute(mergeAttributeName);
      newAttribute = true;
    }
    for (String s : attributeNames) {
      final LdapAttribute la = entry.getAttribute(s);
      if (la != null) {
        if (la.isBinary()) {
          mergedAttribute.addBinaryValues(la.getBinaryValues());
        } else {
          mergedAttribute.addStringValues(la.getStringValues());
        }
      }
    }

    if (mergedAttribute.size() > 0 && newAttribute) {
      entry.addAttribute(mergedAttribute);
    }
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof MergeAttributeEntryHandler) {
      final MergeAttributeEntryHandler v = (MergeAttributeEntryHandler) o;
      return LdapUtils.areEqual(attributeNames, v.attributeNames) &&
             LdapUtils.areEqual(mergeAttributeName, v.mergeAttributeName);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return LdapUtils.computeHashCode(HASH_CODE_SEED, attributeNames, mergeAttributeName);
  }


  @Override
  public String toString()
  {
    return
      String.format(
        "[%s@%d::mergeAttributeName=%s, attributeNames=%s]",
        getClass().getName(),
        hashCode(),
        mergeAttributeName,
        Arrays.toString(attributeNames));
  }
}
