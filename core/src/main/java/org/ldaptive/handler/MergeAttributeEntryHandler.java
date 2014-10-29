/*
  $Id$

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision$
  Updated: $Date$
*/
package org.ldaptive.handler;

import java.util.Arrays;
import org.ldaptive.Connection;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.SearchEntry;
import org.ldaptive.SearchRequest;

/**
 * Merges the values of one or more attributes into a single attribute. The
 * merged attribute may or may not already exist on the entry. If it does exist
 * it's existing values will remain intact.
 *
 * @author  Middleware Services
 * @version  $Revision: 2885 $ $Date: 2014-02-05 16:28:49 -0500 (Wed, 05 Feb 2014) $
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


  /** {@inheritDoc} */
  @Override
  protected void handleAttributes(
    final Connection conn,
    final SearchRequest request,
    final SearchEntry entry)
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


  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        attributeNames,
        mergeAttributeName);
  }


  /** {@inheritDoc} */
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
