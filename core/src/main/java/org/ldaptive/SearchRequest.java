/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import org.ldaptive.asn1.ApplicationDERTag;
import org.ldaptive.asn1.BooleanType;
import org.ldaptive.asn1.ConstructedDEREncoder;
import org.ldaptive.asn1.DEREncoder;
import org.ldaptive.asn1.IntegerType;
import org.ldaptive.asn1.OctetStringType;
import org.ldaptive.asn1.UniversalDERTag;
import org.ldaptive.filter.Filter;
import org.ldaptive.filter.FilterParser;

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
  private String baseDn = "";

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
  private Filter searchFilter;

  /** Return attributes. */
  private String[] returnAttributes = ReturnAttributes.ALL_USER.value();

  /**
   * Binary attribute names used to convey attributes that should be treated as binary when a response is received for
   * this request. This property is not part of the request specification. See {@link LdapAttribute#isBinary()}.
   */
  private String[] binaryAttributes;


  /**
   * Default constructor.
   */
  public SearchRequest() {}


  /**
   * Creates a new search request.
   *
   * @param  dn  base DN
   */
  public SearchRequest(final String dn)
  {
    setBaseDn(dn);
  }


  /**
   * Creates a new search request.
   *
   * @param  dn  base DN
   * @param  filter  search filter
   */
  public SearchRequest(
    final String dn,
    final String filter)
  {
    setBaseDn(dn);
    setFilter(filter);
  }


  /**
   * Creates a new search request.
   *
   * @param  dn  base DN
   * @param  filter  search filter
   * @param  attributes  return attributes
   */
  public SearchRequest(
    final String dn,
    final String filter,
    final String... attributes)
  {
    setBaseDn(dn);
    setFilter(filter);
    setReturnAttributes(attributes);
  }


  /**
   * Creates a new search request.
   *
   * @param  dn  base DN
   * @param  template  filter template
   * @param  attributes  return attributes
   */
  public SearchRequest(
    final String dn,
    final FilterTemplate template,
    final String... attributes)
  {
    setBaseDn(dn);
    setFilter(template);
    setReturnAttributes(attributes);
  }


  /**
   * Creates a new search request.
   *
   * @param  dn  base DN
   * @param  filter  search filter
   * @param  attributes  return attributes
   */
  public SearchRequest(
    final String dn,
    final Filter filter,
    final String... attributes)
  {
    setBaseDn(dn);
    setFilter(filter);
    setReturnAttributes(attributes);
  }


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
    final Filter filter,
    final String... attributes)
  {
    setBaseDn(dn);
    setSearchScope(scope);
    setDerefAliases(aliases);
    setSizeLimit(size);
    setTimeLimit(time);
    setTypesOnly(types);
    setFilter(filter);
    setReturnAttributes(attributes);
  }
  // CheckStyle:ParameterNumber ON


  /**
   * Returns the base DN.
   *
   * @return  base DN
   */
  public String getBaseDn()
  {
    return baseDn;
  }


  /**
   * Sets the base DN.
   *
   * @param  dn  base DN
   */
  public void setBaseDn(final String dn)
  {
    logger.trace("setting baseDn: {}", dn);
    baseDn = dn;
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
    if (scope == null) {
      throw new IllegalArgumentException("Scope cannot be null");
    }
    logger.trace("setting searchScope: {}", scope);
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
    if (aliases == null) {
      throw new IllegalArgumentException("Aliases cannot be null");
    }
    logger.trace("setting derefAliases: {}", aliases);
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
    logger.trace("setting sizeLimit: {}", limit);
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
    logger.trace("setting timeLimit: {}", limit);
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
    logger.trace("setting typesOnly: {}", types);
    typesOnly = types;
  }


  /**
   * Returns the search filter.
   *
   * @return  search filter
   */
  public Filter getFilter()
  {
    return searchFilter;
  }


  /**
   * Sets the search filter.
   *
   * @param  filter  search filter
   */
  public void setFilter(final Filter filter)
  {
    logger.trace("setting filter: {}", filter);
    searchFilter = filter;
  }


  /**
   * Sets the search filter. See {@link FilterParser#parse(String)}.
   *
   * @param  filter  search filter
   */
  public void setFilter(final String filter)
  {
    logger.trace("setting filter: {}", filter);
    searchFilter = FilterParser.parse(filter);
  }


  /**
   * Sets the search filter. See {@link FilterTemplate} and {@link FilterParser#parse(String)}.
   *
   * @param  template  filter template
   */
  public void setFilter(final FilterTemplate template)
  {
    logger.trace("setting filter: {}", template);
    searchFilter = FilterParser.parse(template.format());
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
  public void setReturnAttributes(final String... attributes)
  {
    logger.trace("setting returnAttributes: {}", Arrays.toString(attributes));
    returnAttributes = ReturnAttributes.parse(attributes);
  }


  /**
   * Returns names of binary attributes.
   *
   * @return  binary attribute names
   */
  public String[] getBinaryAttributes()
  {
    return binaryAttributes;
  }


  /**
   * Sets names of binary attributes.
   *
   * @param  attrs  binary attribute names
   */
  public void setBinaryAttributes(final String... attrs)
  {
    logger.trace("setting binaryAttributes: {}", Arrays.toString(attrs));
    binaryAttributes = attrs;
  }


  @Override
  protected DEREncoder[] getRequestEncoders(final int id)
  {
    return new DEREncoder[] {
      new IntegerType(id),
      new ConstructedDEREncoder(
        new ApplicationDERTag(PROTOCOL_OP, true),
        new OctetStringType(baseDn),
        new IntegerType(UniversalDERTag.ENUM, searchScope.ordinal()),
        new IntegerType(UniversalDERTag.ENUM, derefAliases.ordinal()),
        new IntegerType(sizeLimit),
        new IntegerType((int) timeLimit.getSeconds()),
        new BooleanType(typesOnly),
        searchFilter.getEncoder(),
        new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          Stream.of(returnAttributes).map(OctetStringType::new).toArray(DEREncoder[]::new))),
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
      return LdapUtils.areEqual(baseDn, v.baseDn) &&
        LdapUtils.areEqual(searchScope, v.searchScope) &&
        LdapUtils.areEqual(derefAliases, v.derefAliases) &&
        LdapUtils.areEqual(sizeLimit, v.sizeLimit) &&
        LdapUtils.areEqual(timeLimit, v.timeLimit) &&
        LdapUtils.areEqual(typesOnly, v.typesOnly) &&
        LdapUtils.areEqual(searchFilter, v.searchFilter) &&
        LdapUtils.areEqual(returnAttributes, v.returnAttributes) &&
        LdapUtils.areEqual(binaryAttributes, v.binaryAttributes) &&
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
        baseDn,
        searchScope,
        derefAliases,
        sizeLimit,
        timeLimit,
        typesOnly,
        searchFilter,
        returnAttributes,
        binaryAttributes,
        getControls());
  }


  @Override
  public String toString()
  {
    return new StringBuilder(super.toString()).append(", ")
      .append("dn=").append(baseDn).append(", ")
      .append("scope=").append(searchScope).append(", ")
      .append("aliases=").append(derefAliases).append(", ")
      .append("sizeLimit=").append(sizeLimit).append(", ")
      .append("timeLimit=").append(timeLimit).append(", ")
      .append("typesOnly=").append(typesOnly).append(", ")
      .append("filter=").append(searchFilter).append(", ")
      .append("returnAttributes=").append(Arrays.toString(returnAttributes)).append(", ")
      .append("binaryAttributes=").append(Arrays.toString(binaryAttributes)).toString();
  }


  /**
   * Returns a search request initialized for use with an object level search scope.
   *
   * @param  dn  of an ldap entry
   *
   * @return  search request
   */
  public static SearchRequest objectScopeSearchRequest(final String dn)
  {
    return objectScopeSearchRequest(dn, null);
  }


  /**
   * Returns a search request initialized for use with an object level search scope.
   *
   * @param  dn  of an ldap entry
   * @param  attrs  to return
   *
   * @return  search request
   */
  public static SearchRequest objectScopeSearchRequest(final String dn, final String[] attrs)
  {
    return objectScopeSearchRequest(dn, attrs, FilterParser.parse("(objectClass=*)"));
  }


  /**
   * Returns a search request initialized for use with an object level search scope.
   *
   * @param  dn  of an ldap entry
   * @param  attrs  to return
   * @param  filter  to execute on the ldap entry
   *
   * @return  search request
   */
  public static SearchRequest objectScopeSearchRequest(
    final String dn,
    final String[] attrs,
    final String filter)
  {
    return objectScopeSearchRequest(dn, attrs, FilterParser.parse(filter));
  }


  /**
   * Returns a search request initialized for use with an object level search scope.
   *
   * @param  dn  of an ldap entry
   * @param  attrs  to return
   * @param  template  to execute on the ldap entry
   *
   * @return  search request
   */
  public static SearchRequest objectScopeSearchRequest(
    final String dn,
    final String[] attrs,
    final FilterTemplate template)
  {
    return objectScopeSearchRequest(dn, attrs, FilterParser.parse(template.format()));
  }


  /**
   * Returns a search request initialized for use with an object level search scope.
   *
   * @param  dn  of an ldap entry
   * @param  attrs  to return
   * @param  filter  to execute on the ldap entry
   *
   * @return  search request
   */
  public static SearchRequest objectScopeSearchRequest(
    final String dn,
    final String[] attrs,
    final Filter filter)
  {
    final SearchRequest request = new SearchRequest();
    request.setBaseDn(dn);
    request.setFilter(filter);
    request.setReturnAttributes(attrs);
    request.setSearchScope(SearchScope.OBJECT);
    return request;
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
    sr.setBaseDn(request.getBaseDn());
    sr.setSearchScope(request.getSearchScope());
    sr.setDerefAliases(request.getDerefAliases());
    sr.setSizeLimit(request.getSizeLimit());
    sr.setTimeLimit(request.getTimeLimit());
    sr.setTypesOnly(request.isTypesOnly());
    sr.setFilter(request.getFilter());
    sr.setReturnAttributes(request.getReturnAttributes());
    sr.setBinaryAttributes(request.getBinaryAttributes());
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
      object.setBaseDn(dn);
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
    public Builder filter(final Filter filter)
    {
      object.setFilter(filter);
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
      object.setFilter(filter);
      return self();
    }


    /**
     * Sets the search filter.
     *
     * @param  template  filter template
     *
     * @return  this builder
     */
    public Builder filter(final FilterTemplate template)
    {
      object.setFilter(template);
      return self();
    }


    /**
     * Sets the return attributes.
     *
     * @param  attributes  return attributes
     *
     * @return  this builder
     */
    public Builder returnAttributes(final String... attributes)
    {
      object.setReturnAttributes(attributes);
      return self();
    }


    /**
     * Sets the return attributes.
     *
     * @param  attributes  return attributes
     *
     * @return  this builder
     */
    public Builder returnAttributes(final Collection<String> attributes)
    {
      object.setReturnAttributes(attributes.toArray(String[]::new));
      return self();
    }


    /**
     * Sets the binary attributes.
     *
     * @param  attributes  binary attributes
     *
     * @return  this builder
     */
    public Builder binaryAttributes(final String... attributes)
    {
      object.setBinaryAttributes(attributes);
      return self();
    }


    /**
     * Sets the binary attributes.
     *
     * @param  attributes  binary attributes
     *
     * @return  this builder
     */
    public Builder binaryAttributes(final Collection<String> attributes)
    {
      object.setBinaryAttributes(attributes.toArray(String[]::new));
      return self();
    }
  }
}
