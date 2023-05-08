/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.Arrays;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Provides the ability to modify the case of search entry DNs, attribute names, and attribute values.
 *
 * @author  Middleware Services
 */
public class CaseChangeEntryHandler extends AbstractEntryHandler<LdapEntry> implements LdapEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 821;

  /** Enum to define the type of case change. */
  public enum CaseChange {

    /** no case change. */
    NONE,

    /** lower case. */
    LOWER,

    /** upper case. */
    UPPER;


    /**
     * This changes the supplied string based on the supplied case change.
     *
     * @param  cc  case change to perform
     * @param  string  to modify
     *
     * @return  string that has been changed
     */
    public static String perform(final CaseChange cc, final String string)
    {
      String s = null;
      if (CaseChange.LOWER == cc) {
        s = string.toLowerCase();
      } else if (CaseChange.UPPER == cc) {
        s = string.toUpperCase();
      } else if (CaseChange.NONE == cc) {
        s = string;
      }
      return s;
    }
  }

  /** Type of case modification to make to the entry DN. */
  private CaseChange dnCaseChange = CaseChange.NONE;

  /** Type of case modification to make to the attribute names. */
  private CaseChange attributeNameCaseChange = CaseChange.NONE;

  /** Type of case modification to make to the attributes values. */
  private CaseChange attributeValueCaseChange = CaseChange.NONE;

  /** Attribute names to modify. */
  private String[] attributeNames;


  /**
   * Returns the DN case change.
   *
   * @return  case change
   */
  public CaseChange getDnCaseChange()
  {
    return dnCaseChange;
  }


  /**
   * Sets the DN case change.
   *
   * @param  cc  case change
   */
  public void setDnCaseChange(final CaseChange cc)
  {
    dnCaseChange = cc;
  }


  /**
   * Returns the attribute name case change.
   *
   * @return  case change
   */
  public CaseChange getAttributeNameCaseChange()
  {
    return attributeNameCaseChange;
  }


  /**
   * Sets the attribute name case change.
   *
   * @param  cc  case change
   */
  public void setAttributeNameCaseChange(final CaseChange cc)
  {
    attributeNameCaseChange = cc;
  }


  /**
   * Returns the attribute value case change.
   *
   * @return  case change
   */
  public CaseChange getAttributeValueCaseChange()
  {
    return attributeValueCaseChange;
  }


  /**
   * Sets the attribute value case change.
   *
   * @param  cc  case change
   */
  public void setAttributeValueCaseChange(final CaseChange cc)
  {
    attributeValueCaseChange = cc;
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
  public LdapEntry apply(final LdapEntry entry)
  {
    handleEntry(entry);
    return entry;
  }


  @Override
  protected String handleDn(final LdapEntry entry)
  {
    return CaseChange.perform(dnCaseChange, entry.getDn());
  }


  @Override
  protected void handleAttributes(final LdapEntry entry)
  {
    if (attributeNames == null) {
      super.handleAttributes(entry);
    } else {
      for (String s : attributeNames) {
        final LdapAttribute la = entry.getAttribute(s);
        if (la != null) {
          handleAttribute(la);
        }
      }
    }
  }


  @Override
  protected String handleAttributeName(final String name)
  {
    return CaseChange.perform(attributeNameCaseChange, name);
  }


  @Override
  protected String handleAttributeValue(final String value)
  {
    return CaseChange.perform(attributeValueCaseChange, value);
  }


  @Override
  protected byte[] handleAttributeValue(final byte[] value)
  {
    return value;
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof CaseChangeEntryHandler) {
      final CaseChangeEntryHandler v = (CaseChangeEntryHandler) o;
      return LdapUtils.areEqual(dnCaseChange, v.dnCaseChange) &&
             LdapUtils.areEqual(attributeNameCaseChange, v.attributeNameCaseChange) &&
             LdapUtils.areEqual(attributeValueCaseChange, v.attributeValueCaseChange) &&
             LdapUtils.areEqual(attributeNames, v.attributeNames);
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        dnCaseChange,
        attributeNameCaseChange,
        attributeValueCaseChange,
        attributeNames);
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "dnCaseChange=" + dnCaseChange + ", " +
      "attributeNameCaseChange=" + attributeNameCaseChange + ", " +
      "attributeValueCaseChange=" + attributeValueCaseChange + ", " +
      "attributeNames=" + Arrays.toString(attributeNames) + "]";
  }
}
