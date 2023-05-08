/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.ldaptive.AttributeModification;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;

/**
 * Contains the data required to perform a merge operation.
 *
 * @author  Middleware Services
 */
public class MergeRequest
{

  /** Ldap entry to merge. */
  private LdapEntry ldapEntry;

  /** Whether to delete the entry. */
  private boolean deleteEntry;

  /** Attribute names to include in the search. */
  private String[] searchAttrs;

  /** Attribute names to include when performing a merge. */
  private String[] includeAttrs;

  /** Attribute names to exclude when performing a merge. */
  private String[] excludeAttrs;

  /** Whether to use replace or add/delete for attribute modifications. */
  private boolean useReplace = true;

  /** Handler for attribute modifications. */
  private AttributeModificationsHandler[] attributeModificationsHandlers;


  /** Default constructor. */
  public MergeRequest() {}


  /**
   * Creates a new merge request.
   *
   * @param  entry  to merge into the LDAP
   */
  public MergeRequest(final LdapEntry entry)
  {
    setEntry(entry);
  }


  /**
   * Creates a new merge request.
   *
   * @param  entry  to merge into the LDAP
   * @param  delete  whether the supplied entry should be deleted
   */
  public MergeRequest(final LdapEntry entry, final boolean delete)
  {
    setEntry(entry);
    setDeleteEntry(delete);
  }


  /**
   * Returns the ldap entry to merge.
   *
   * @return  ldap entry to merge
   */
  public LdapEntry getEntry()
  {
    return ldapEntry;
  }


  /**
   * Sets the ldap entry to merge into the LDAP.
   *
   * @param  entry  to merge
   */
  public void setEntry(final LdapEntry entry)
  {
    ldapEntry = entry;
  }


  /**
   * Returns whether to delete the entry.
   *
   * @return  whether to delete the entry
   */
  public boolean getDeleteEntry()
  {
    return deleteEntry;
  }


  /**
   * Sets whether to delete the entry.
   *
   * @param  b  whether to delete the entry
   */
  public void setDeleteEntry(final boolean b)
  {
    deleteEntry = b;
  }


  /**
   * Returns the names of attributes that are used when searching for the entry.
   *
   * @return  attribute names to return when searching
   */
  public String[] getSearchAttributes()
  {
    return searchAttrs;
  }


  /**
   * Sets the list of attribute names that are used when searching for the entry.
   *
   * @param  attrs  names to return when searching
   */
  public void setSearchAttributes(final String... attrs)
  {
    searchAttrs = attrs;
  }


  /**
   * Returns the names of attributes that are included when performing a modify.
   *
   * @return  attribute names to include
   */
  public String[] getIncludeAttributes()
  {
    return includeAttrs;
  }


  /**
   * Sets the list of attribute names to include when performing modify.
   *
   * @param  attrs  names to include
   */
  public void setIncludeAttributes(final String... attrs)
  {
    includeAttrs = attrs;
  }


  /**
   * Returns the names of attributes that are excluded when performing a modify.
   *
   * @return  attribute names to exclude
   */
  public String[] getExcludeAttributes()
  {
    return excludeAttrs;
  }


  /**
   * Sets the list of attribute names to exclude when performing a modify.
   *
   * @param  attrs  names to exclude
   */
  public void setExcludeAttributes(final String... attrs)
  {
    excludeAttrs = attrs;
  }


  /**
   * Returns whether replace should be used for attribute modifications.
   *
   * @return  whether replace should be used for attribute modifications
   */
  public boolean isUseReplace()
  {
    return useReplace;
  }


  /**
   * Sets whether replace should be used for attribute modifications.
   *
   * @param  replace  whether replace should be used for attribute modifications
   */
  public void setUseReplace(final boolean replace)
  {
    useReplace = replace;
  }


  /**
   * Returns the attribute modifications handlers.
   *
   * @return  attribute modifications handlers
   */
  public AttributeModificationsHandler[] getAttributeModificationsHandlers()
  {
    return attributeModificationsHandlers;
  }


  /**
   * Sets the attribute value processors.
   *
   * @param  handlers  attribute modifications handlers
   */
  public void setAttributeModificationsHandlers(final AttributeModificationsHandler... handlers)
  {
    attributeModificationsHandlers = handlers;
  }


  @Override
  public String toString()
  {
    return "[" +
      getClass().getName() + "@" + hashCode() + "::" +
      "ldapEntry=" + ldapEntry + ", " +
      "deleteEntry=" + deleteEntry + ", " +
      "searchAttributes=" + Arrays.toString(searchAttrs) + ", " +
      "includeAttributes=" + Arrays.toString(includeAttrs) + ", " +
      "excludeAttributes=" + Arrays.toString(excludeAttrs) + ", " +
      "useReplace=" + useReplace + ", " +
      "attributeModificationProcessor=" +
      Arrays.toString(attributeModificationsHandlers) + "]";
  }


  /**
   * Marker interface for an attribute modifications handler. The complexity of this interface stems from the
   * requirement to support batching. A modify operation is executed for each element in the outer list. Attribute
   * modifications in the inner list may be mutated as needed to produce modification lists of the desired size and
   * complexity.
   *
   * @author  Middleware Services
   */
  public interface AttributeModificationsHandler
    extends Function<List<List<AttributeModification>>, List<List<AttributeModification>>> {}

  /**
   * Processes attribute modifications to enforce the maximum number of attribute values in any single attribute. For
   * attribute values that exceed this limit, a new attribute is created to contain the excess values.
   */
  public static class MaxSizeAttributeValueHandler implements AttributeModificationsHandler
  {

    /** Maximum number of attribute values allowed in a single attribute. */
    private final int maxSize;


    /**
     * Creates a new max attribute value size processor.
     *
     * @param  size  maximum number of attribute values to allow
     */
    public MaxSizeAttributeValueHandler(final int size)
    {
      maxSize = size;
    }


    @Override
    public List<List<AttributeModification>> apply(final List<List<AttributeModification>> modifications)
    {
      final List<List<AttributeModification>> attrValuesModifications = new ArrayList<>(new ArrayList<>());
      for (List<AttributeModification> mods : modifications) {
        final List<AttributeModification> attrMods = new ArrayList<>(mods.size());
        for (AttributeModification am : mods) {
          if (am.getAttribute().size() > maxSize) {
            divideList(
              new ArrayList<>(am.getAttribute().getBinaryValues()),
              maxSize,
              values -> attrMods.add(
                new AttributeModification(
                  am.getOperation(),
                  LdapAttribute.builder().name(am.getAttribute().getName()).binaryValues(values).build())));
          } else {
            attrMods.add(am);
          }
        }
        attrValuesModifications.add(attrMods);
      }
      return attrValuesModifications;
    }
  }


  /**
   * Processes attribute modifications so that any list of attribute modifications does not exceed the configured batch
   * size. For a provided matrix of 1x10 with batch size of 5, would result in a matrix of 2x5. This would result in the
   * merge operation performing two modifies, each with five attribute modifications.
   */
  public static class BatchHandler implements AttributeModificationsHandler
  {

    /** Batch size to enforce. */
    private final int batchSize;


    /**
     * Creates a new batch processor.
     *
     * @param  size  batch size
     */
    public BatchHandler(final int size)
    {
      batchSize = size;
    }


    @Override
    public List<List<AttributeModification>> apply(final List<List<AttributeModification>> modifications)
    {
      final List<List<AttributeModification>> batchModifications = new ArrayList<>(modifications.size());
      for (List<AttributeModification> mods : modifications) {
        if (mods.size() > batchSize) {
          divideList(mods, batchSize, batchModifications::add);
        } else {
          batchModifications.add(mods);
        }
      }
      return batchModifications;
    }
  }


  /**
   * Divides the supplied list into sub lists by the supplied divisor and passes each sub list to the consumer.
   *
   * @param  <T>  type of list element
   * @param  list  to divide
   * @param  divisor  to divide list by
   * @param  consumer  to process each sub list
   */
  private static <T> void divideList(final List<T> list, final int divisor, final Consumer<List<T>> consumer)
  {
    for (int i = 0; i < list.size() / divisor; i++) {
      final int start = i * divisor;
      final int end = (i + 1) * divisor;
      consumer.accept(list.subList(start, end > list.size() ? list.size() : end));
    }
  }
}
