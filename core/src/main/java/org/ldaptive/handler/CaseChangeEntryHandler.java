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
public class CaseChangeEntryHandler extends AbstractEntryHandler implements LdapEntryHandler
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


  /** Default constructor. */
  public CaseChangeEntryHandler() {}


  /**
   * Creates a new case change entry handler.
   *
   * @param  dnChange  to apply to entry DN
   * @param  attrNameChange  to apply to attribute names
   * @param  attrValueChange  to apply to attribute values
   * @param  attrNames  to apply these changes to
   */
  public CaseChangeEntryHandler(
    final CaseChange dnChange,
    final CaseChange attrNameChange,
    final CaseChange attrValueChange,
    final String... attrNames)
  {
    dnCaseChange = dnChange;
    attributeNameCaseChange = attrNameChange;
    attributeValueCaseChange = attrValueChange;
    attributeNames = attrNames;
  }


  /**
   * Returns the DN case change.
   *
   * @return  case change
   */
  public final CaseChange getDnCaseChange()
  {
    return dnCaseChange;
  }


  /**
   * Sets the DN case change.
   *
   * @param  cc  case change
   */
  public final void setDnCaseChange(final CaseChange cc)
  {
    assertMutable();
    dnCaseChange = cc;
  }


  /**
   * Returns the attribute name case change.
   *
   * @return  case change
   */
  public final CaseChange getAttributeNameCaseChange()
  {
    return attributeNameCaseChange;
  }


  /**
   * Sets the attribute name case change.
   *
   * @param  cc  case change
   */
  public final void setAttributeNameCaseChange(final CaseChange cc)
  {
    assertMutable();
    attributeNameCaseChange = cc;
  }


  /**
   * Returns the attribute value case change.
   *
   * @return  case change
   */
  public final CaseChange getAttributeValueCaseChange()
  {
    return attributeValueCaseChange;
  }


  /**
   * Sets the attribute value case change.
   *
   * @param  cc  case change
   */
  public final void setAttributeValueCaseChange(final CaseChange cc)
  {
    assertMutable();
    attributeValueCaseChange = cc;
  }


  /**
   * Returns the attribute names.
   *
   * @return  attribute names
   */
  public final String[] getAttributeNames()
  {
    return LdapUtils.copyArray(attributeNames);
  }


  /**
   * Sets the attribute names.
   *
   * @param  names  of the attributes
   */
  public final void setAttributeNames(final String... names)
  {
    assertMutable();
    attributeNames = LdapUtils.copyArray(names);
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


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  /** Case change entry handler builder. */
  public static final class Builder
  {

    /** Case change entry handler to build. */
    private final CaseChangeEntryHandler object = new CaseChangeEntryHandler();


    /**
     * Default constructor.
     */
    private Builder() {}


    /**
     * Makes this instance immutable.
     *
     * @return  this builder
     */
    public Builder freeze()
    {
      object.freeze();
      return this;
    }


    /**
     * Sets the DN case change.
     *
     * @param  cc  DN case change
     *
     * @return  this builder
     */
    public Builder dnCaseChange(final CaseChange cc)
    {
      object.setDnCaseChange(cc);
      return this;
    }


    /**
     * Sets the attribute name case change.
     *
     * @param  cc  attribute name case change
     *
     * @return  this builder
     */
    public Builder attributeNameCaseChange(final CaseChange cc)
    {
      object.setAttributeNameCaseChange(cc);
      return this;
    }


    /**
     * Sets the attribute value case change.
     *
     * @param  cc  attribute value case change
     *
     * @return  this builder
     */
    public Builder attributeValueCaseChange(final CaseChange cc)
    {
      object.setAttributeValueCaseChange(cc);
      return this;
    }


    /**
     * Sets the attribute names.
     *
     * @param  names  attribute names
     *
     * @return  this builder
     */
    public Builder attributeNames(final String... names)
    {
      object.setAttributeNames(names);
      return this;
    }


    /**
     * Returns the case change entry handler.
     *
     * @return  case change entry handler
     */
    public CaseChangeEntryHandler build()
    {
      return object;
    }
  }
}
