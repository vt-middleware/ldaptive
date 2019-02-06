/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.time.Duration;
import java.util.Arrays;
import java.util.stream.Stream;
import org.ldaptive.DerefAliases;
import org.ldaptive.LdapUtils;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchScope;
import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.protocol.filter.SearchFilterParser;

/**
 * LDAP search request defined as:
 *
 * <pre>
   SearchRequest ::= [APPLICATION 3] SEQUENCE {
     baseObject      LDAPDN,
     scope           ENUMERATED {
       baseObject              (0),
       singleLevel             (1),
       wholeSubtree            (2),
       ...  },
     aliases    ENUMERATED {
       neverDerefAliases       (0),
       derefInSearching        (1),
       derefFindingBaseObj     (2),
       derefAlways             (3) },
     sizeLimit       INTEGER (0 ..  maxInt),
     timeLimit       INTEGER (0 ..  maxInt),
     typesOnly       BOOLEAN,
     filter          Filter,
     attributes      AttributeSelection }
 * </pre>
 *
 * @author  Middleware Services
 */
public class SearchRequest extends AbstractRequestMessage
{

  /** BER protocol number. */
  public static final int PROTOCOL_OP = 3;

  /** hash code seed. */
  private static final int HASH_CODE_SEED = 307;

  /** Base DN. */
  private String baseDN = "";

  /** Search scope. */
  private SearchScope searchScope = SearchScope.SUBTREE;

  /** Deref aliases. */
  private DerefAliases derefAliases = DerefAliases.NEVER;

  /** Size limit. */
  private int sizeLimit;

  /** Time limit. */
  private Duration timeLimit = Duration.ZERO;

  /** Types only. */
  private boolean typesOnly;

  /** Search filter. */
  private SearchFilter searchFilter;

  /** Return attributes. */
  private String[] returnAttributes = ReturnAttributes.ALL_USER.value();


  /**
   * Default constructor.
   */
  public SearchRequest() {}


  /**
   * Creates a new search request.
   *
   * @param  dn  base DN
   * @param  scope  search scope
   * @param  aliases  deref aliases
   * @param  size  size limit
   * @param  time  time limit
   * @param  types  types only
   * @param  filter  search filter
   * @param  attributes  return attributes
   */
  // CheckStyle:ParameterNumber OFF
  public SearchRequest(
    final String dn,
    final SearchScope scope,
    final DerefAliases aliases,
    final int size,
    final Duration time,
    final boolean types,
    final SearchFilter filter,
    final String... attributes)
  {
    setBaseDN(dn);
    setSearchScope(scope);
    setDerefAliases(aliases);
    setSizeLimit(size);
    setTimeLimit(time);
    setTypesOnly(types);
    setSearchFilter(filter);
    setReturnAttributes(attributes);
  }
  // CheckStyle:ParameterNumber ON


  /**
   * Returns the base DN.
   *
   * @return  base DN
   */
  public String getBaseDN()
  {
    return baseDN;
  }


  /**
   * Sets the base DN.
   *
   * @param  dn  base DN
   */
  public void setBaseDN(final String dn)
  {
    baseDN = dn;
  }


  /**
   * Gets the search scope.
   *
   * @return  search scope
   */
  public SearchScope getSearchScope()
  {
    return searchScope;
  }


  /**
   * Sets the search scope.
   *
   * @param  scope  search scope
   */
  public void setSearchScope(final SearchScope scope)
  {
    searchScope = scope;
  }


  /**
   * Returns how to dereference aliases.
   *
   * @return  how to dereference aliases
   */
  public DerefAliases getDerefAliases()
  {
    return derefAliases;
  }


  /**
   * Sets how to dereference aliases.
   *
   * @param  aliases  how to dereference aliases
   */
  public void setDerefAliases(final DerefAliases aliases)
  {
    derefAliases = aliases;
  }


  /**
   * Returns the size limit.
   *
   * @return  size limit
   */
  public int getSizeLimit()
  {
    return sizeLimit;
  }


  /**
   * Sets the size limit.
   *
   * @param  limit  size limit
   *
   * @throws  IllegalArgumentException  if limit is negative
   */
  public void setSizeLimit(final int limit)
  {
    if (limit < 0) {
      throw new IllegalArgumentException("Size limit cannot be negative");
    }
    sizeLimit = limit;
  }


  /**
   * Returns the time limit.
   *
   * @return  time limit
   */
  public Duration getTimeLimit()
  {
    return timeLimit;
  }


  /**
   * Sets the time limit.
   *
   * @param  limit  time limit
   *
   * @throws  IllegalArgumentException  if limit is null or negative
   */
  public void setTimeLimit(final Duration limit)
  {
    if (limit == null || limit.isNegative()) {
      throw new IllegalArgumentException("Time limit cannot be null or negative");
    }
    timeLimit = limit;
  }


  /**
   * Returns whether to return only attribute types.
   *
   * @return  whether to return only attribute types
   */
  public boolean isTypesOnly()
  {
    return typesOnly;
  }


  /**
   * Sets whether to return only attribute types.
   *
   * @param  types  whether to return only attribute types
   */
  public void setTypesOnly(final boolean types)
  {
    typesOnly = types;
  }


  /**
   * Returns the search filter.
   *
   * @return  search filter
   */
  public SearchFilter getSearchFilter()
  {
    return searchFilter;
  }


  /**
   * Sets the search filter.
   *
   * @param  filter  search filter
   */
  public void setSearchFilter(final SearchFilter filter)
  {
    searchFilter = filter;
  }


  /**
   * Sets the search filter. See {@link SearchFilterParser#parse(String)}.
   *
   * @param  filter  search filter
   */
  public void setSearchFilter(final String filter)
  {
    searchFilter = SearchFilterParser.parse(filter);
  }


  /**
   * Returns the search return attributes.
   *
   * @return  search return attributes
   */
  public String[] getReturnAttributes()
  {
    return returnAttributes;
  }


  /**
   * Sets the search return attributes.
   *
   * @param  attributes  search return attributes
   */
  public void setReturnAttributes(final String[] attributes)
  {
    returnAttributes = attributes;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new OctetStringType(baseDN),
        new IntegerType(UniversalDERTag.ENUM, searchScope.ordinal()),
        new IntegerType(UniversalDERTag.ENUM, derefAliases.ordinal()),
        new IntegerType(sizeLimit),
        new IntegerType((int) timeLimit.getSeconds()),
        new BooleanType(typesOnly),
        searchFilter.getEncoder(),
        new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          Stream.of(returnAttributes).map(v -> new OctetStringType(v)).toArray(DEREncoder[]::new))),
    };
  }


  @Override
  public boolean equals(final Object o)
  {
    if (o == this) {
      return true;
    }
    if (o instanceof SearchRequest) {
      final SearchRequest v = (SearchRequest) o;
      return LdapUtils.areEqual(baseDN, v.baseDN) &&
        LdapUtils.areEqual(searchScope, v.searchScope) &&
        LdapUtils.areEqual(derefAliases, v.derefAliases) &&
        LdapUtils.areEqual(sizeLimit, v.sizeLimit) &&
        LdapUtils.areEqual(timeLimit, v.timeLimit) &&
        LdapUtils.areEqual(typesOnly, v.typesOnly) &&
        LdapUtils.areEqual(searchFilter, v.searchFilter) &&
        LdapUtils.areEqual(returnAttributes, v.returnAttributes) &&
        LdapUtils.areEqual(getControls(), v.getControls());
    }
    return false;
  }


  @Override
  public int hashCode()
  {
    return
      LdapUtils.computeHashCode(
        HASH_CODE_SEED,
        baseDN,
        searchScope,
        derefAliases,
        sizeLimit,
        timeLimit,
        typesOnly,
        searchFilter,
        returnAttributes,
        getControls());
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(baseDN).append(", ")
      .append("scope=").append(searchScope).append(", ")
      .append("aliases=").append(derefAliases).append(", ")
      .append("sizeLimit=").append(sizeLimit).append(", ")
      .append("timeLimit=").append(timeLimit).append(", ")
      .append("typesOnly=").append(typesOnly).append(", ")
      .append("filter=").append(searchFilter).append(", ")
      .append("attributes=").append(Arrays.toString(returnAttributes)).toString();
  }


  /**
   * Returns a new search request with the same properties as the supplied request.
   *
   * @param  request  to copy
   *
   * @return  copy of the supplied search request
   */
  public static SearchRequest copy(final SearchRequest request)
  {
    final SearchRequest sr = new SearchRequest();
    sr.setBaseDN(request.getBaseDN());
    sr.setSearchScope(request.getSearchScope());
    sr.setDerefAliases(request.getDerefAliases());
    sr.setSizeLimit(request.getSizeLimit());
    sr.setTimeLimit(request.getTimeLimit());
    sr.setTypesOnly(request.isTypesOnly());
    sr.setSearchFilter(request.getSearchFilter());
    sr.setReturnAttributes(request.getReturnAttributes());
    sr.setControls(request.getControls());
    return sr;
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


  /**
   * Creates a builder for this class.
   *
   * @param  request  search request to initialize the builder with
   *
   * @return  new builder
   */
  public static Builder builder(final SearchRequest request)
  {
    return new Builder(request);
  }


  /** Search request builder. */
  public static class Builder extends AbstractRequestMessage.AbstractBuilder<SearchRequest.Builder, SearchRequest>
  {


    /**
     * Default constructor.
     */
    protected Builder()
    {
      super(new SearchRequest());
    }


    /**
     * Creates a new builder.
     *
     * @param  req  search request to build
     */
    protected Builder(final SearchRequest req)
    {
      super(req);
    }


    @Override
    protected Builder self()
    {
      return this;
    }


    /**
     * Sets the base DN.
     *
     * @param  dn  base DN
     *
     * @return  this builder
     */
    public Builder dn(final String dn)
    {
      object.setBaseDN(dn);
      return self();
    }


    /**
     * Sets the search scope.
     *
     * @param  scope  search scope
     *
     * @return  this builder
     */
    public Builder scope(final SearchScope scope)
    {
      object.setSearchScope(scope);
      return self();
    }


    /**
     * Sets the deref aliases flag.
     *
     * @param  aliases  deref aliases
     *
     * @return  this builder
     */
    public Builder aliases(final DerefAliases aliases)
    {
      object.setDerefAliases(aliases);
      return self();
    }


    /**
     * Sets the size limit.
     *
     * @param  size  size limit
     *
     * @return  this builder
     */
    public Builder sizeLimit(final int size)
    {
      object.setSizeLimit(size);
      return self();
    }


    /**
     * Sets the time limit.
     *
     * @param  time  time limit
     *
     * @return  this builder
     */
    public Builder timeLimit(final Duration time)
    {
      object.setTimeLimit(time);
      return self();
    }


    /**
     * Sets the types only.
     *
     * @param  types  whether to return only types
     *
     * @return  this builder
     */
    public Builder typesOnly(final boolean types)
    {
      object.setTypesOnly(types);
      return self();
    }


    /**
     * Sets the search filter.
     *
     * @param  filter  search filter
     *
     * @return  this builder
     */
    public Builder filter(final SearchFilter filter)
    {
      object.setSearchFilter(filter);
      return self();
    }


    /**
     * Sets the search filter.
     *
     * @param  filter  search filter
     *
     * @return  this builder
     */
    public Builder filter(final String filter)
    {
      object.setSearchFilter(filter);
      return self();
    }


    /**
     * Sets the return attributes.
     *
     * @param  attributes  return attributes
     *
     * @return  this builder
     */
    public Builder attributes(final String... attributes)
    {
      object.setReturnAttributes(attributes);
      return self();
    }
  }
}
