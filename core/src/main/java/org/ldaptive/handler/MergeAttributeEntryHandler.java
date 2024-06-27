/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.handler;

import java.util.Arrays;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapUtils;

/**
 * Merges the values of one or more attributes into a single attribute. The merged attribute may or may not already
 * exist on the entry. If it does exist its existing values will remain intact.
 *
 * @author  Middleware Services
 */
public class MergeAttributeEntryHandler extends AbstractEntryHandler implements LdapEntryHandler
{

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 827;

  /** Attribute name to add merge values into. */
  private String mergeAttributeName;

  /** Attribute names to read values from. */
  private String[] attributeNames;


  /** Default constructor. */
  public MergeAttributeEntryHandler() {}


  /**
   * Creates a ew merge attribute entry handler.
   *
   * @param  mergeName  attribute name to merge values into
   * @param  attrNames  attribute names to read values from
   */
  public MergeAttributeEntryHandler(final String mergeName, final String[] attrNames)
  {
    mergeAttributeName = mergeName;
    attributeNames = attrNames;
  }


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
    assertMutable();
    mergeAttributeName = name;
  }


  /**
   * Returns the attribute names.
   *
   * @return  attribute names
   */
  public String[] getAttributeNames()
  {
    return LdapUtils.copyArray(attributeNames);
  }


  /**
   * Sets the attribute names.
   *
   * @param  names  of the attributes
   */
  public void setAttributeNames(final String... names)
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
  protected void handleAttributes(final LdapEntry entry)
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
      entry.addAttributes(mergedAttribute);
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
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "mergeAttributeName=" + mergeAttributeName + ", " +
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


  /** Merge attribute entry handler builder. */
  public static final class Builder
  {

    /** Merge attribute entry handler to build. */
    private final MergeAttributeEntryHandler object = new MergeAttributeEntryHandler();


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
     * Sets the merge attribute name.
     *
     * @param  name  merge attribute name
     *
     * @return  this builder
     */
    public Builder mergeAttributeName(final String name)
    {
      object.setMergeAttributeName(name);
      return this;
    }


    /**
     * Returns the merge attribute entry handler.
     *
     * @return  merge attribute entry handler
     */
    public MergeAttributeEntryHandler build()
    {
      return object;
    }
  }
}
