/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ext;

import java.util.Arrays;
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

  /** Number of attribute modifications to batch together. */
  private int modificationBatchSize;

  /** Number of attribute values to batch together. */
  private int attributeValuesBatchSize;


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
   * Returns the number of modifications that a modify operation should contain.
   *
   * @return  number of modifications that a modify operation should contain
   */
  public int getModificationBatchSize()
  {
    return modificationBatchSize;
  }


  /**
   * Sets the number of modifications that a modify operation should contain.
   *
   * @param  size  number of modifications that a modify operation should contain
   */
  public void setModificationBatchSize(final int size)
  {
    modificationBatchSize = size;
  }


  /**
   * Returns the number of attribute values that any single attribute modification should contain.
   *
   * @return  number of attribute values that any single attribute modification should contain
   */
  public int getAttributeValuesBatchSize()
  {
    return attributeValuesBatchSize;
  }


  /**
   * Sets the number of attribute values that any single attribute modification should contain.
   *
   * @param  size  number of attribute values that any single attribute modification should contain
   */
  public void setAttributeValuesBatchSize(final int size)
  {
    attributeValuesBatchSize = size;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("ldapEntry=").append(ldapEntry).append(", ")
      .append("deleteEntry=").append(deleteEntry).append(", ")
      .append("searchAttributes=").append(Arrays.toString(searchAttrs)).append(", ")
      .append("includeAttributes=").append(Arrays.toString(includeAttrs)).append(", ")
      .append("excludeAttributes=").append(Arrays.toString(excludeAttrs)).append(", ")
      .append("useReplace=").append(useReplace).append(", ")
      .append("modificationBatchSize=").append(modificationBatchSize).append(", ")
      .append("excludeAttributes=").append(attributeValuesBatchSize).append("]").toString();
  }
}
