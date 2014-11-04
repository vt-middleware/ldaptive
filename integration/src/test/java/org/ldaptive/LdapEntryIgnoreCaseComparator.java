/*
  $Id: LdapEntryIgnoreCaseComparator.java 2379 2012-05-04 16:15:59Z dfisher $

  Copyright (C) 2003-2012 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2379 $
  Updated: $Date: 2012-05-04 12:15:59 -0400 (Fri, 04 May 2012) $
*/
package org.ldaptive;

import java.util.Comparator;

/**
 * Compares two ldap entries, ignoring the case of supplied attribute values.
 *
 * @author  Middleware Services
 * @version  $Revision: 2379 $ $Date: 2012-05-04 12:15:59 -0400 (Fri, 04 May 2012) $
 */
public class LdapEntryIgnoreCaseComparator implements Comparator<LdapEntry>
{

  /** names of attributes to ignore the case of. */
  private final String[] attributeNames;


  /**
   * Creates a new ldap entry ignore case comparator that will ignore the
   * case of all attribute values.
   */
  public LdapEntryIgnoreCaseComparator()
  {
    this((String[]) null);
  }


  /**
   * Creates a new ldap entry ignore case comparator with the supplied names. If
   * names is null all attributes will be case ignored.
   *
   * @param  names  of attributes whose case should be ignored
   */
  public LdapEntryIgnoreCaseComparator(final String... names)
  {
    attributeNames = names;
  }


  /**
   * Compares two ldap entries.
   *
   * @param  a  first entry for the comparison
   * @param  b  second entry for the comparison
   *
   * @return  a negative integer, zero, or a positive integer as the first
   * argument is less than, equal to, or greater than the second.
   */
  public int compare(final LdapEntry a, final LdapEntry b)
  {
    return lowerCaseEntry(a, attributeNames).hashCode() -
           lowerCaseEntry(b, attributeNames).hashCode();
  }


  /**
   * Returns a new ldap entry whose attribute values have been lower cased as
   * configured.
   *
   * @param  le  entry to copy values from
   * @param  names  of attributes whose case should be ignored
   *
   * @return  ldap entry with lower cased attribute values
   *
   * @throws  IllegalArgumentException  if a binary attribute is lower cased
   */
  public static LdapEntry lowerCaseEntry(
    final LdapEntry le, final String... names)
  {
    final LdapEntry lowerCase = new LdapEntry(le.getSortBehavior());
    lowerCase.setDn(le.getDn());
    for (LdapAttribute la : le.getAttributes()) {
      if (names != null) {
        boolean setAttr = false;
        for (String name : names) {
          if (name.equalsIgnoreCase(la.getName())) {
            lowerCase.addAttribute(
              LdapAttributeIgnoreCaseComparator.lowerCaseAttribute(la));
            setAttr = true;
            break;
          }
        }
        if (!setAttr) {
          lowerCase.addAttribute(la);
        }
      } else {
        lowerCase.addAttribute(
          LdapAttributeIgnoreCaseComparator.lowerCaseAttribute(la));
      }
    }
    return lowerCase;
  }
}
