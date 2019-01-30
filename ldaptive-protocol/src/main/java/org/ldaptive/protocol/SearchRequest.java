/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.protocol;

import java.util.Arrays;
import java.util.stream.Stream;
import org.ldaptive.DerefAliases;
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

  /** Base DN. */
  private String baseDN;

  /** Search scope. */
  private SearchScope searchScope;

  /** Deref aliases. */
  private DerefAliases derefAliases;

  /** Size limit. */
  private int sizeLimit;

  /** Time limit. */
  private int timeLimit;

  /** Types only. */
  private boolean typesOnly;

  /** Search filter. */
  private SearchFilter searchFilter;

  /** Return attributes. */
  private String[] returnAttributes;


  /**
   * Default constructor.
   */
  private SearchRequest() {}


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
    final int time,
    final boolean types,
    final SearchFilter filter,
    final String... attributes)
  {
    baseDN = dn;
    searchScope = scope;
    derefAliases = aliases;
    if (size < 0) {
      throw new IllegalArgumentException("Size limit must be >= 0");
    }
    sizeLimit = size;
    if (time < 0) {
      throw new IllegalArgumentException("Time limit must be >= 0");
    }
    timeLimit = time;
    typesOnly = types;
    searchFilter = filter;
    returnAttributes = attributes;
  }
  // CheckStyle:ParameterNumber ON


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
        new IntegerType(timeLimit),
        new BooleanType(typesOnly),
        searchFilter.getEncoder(),
        new ConstructedDEREncoder(
          UniversalDERTag.SEQ,
          Stream.of(returnAttributes).map(v -> new OctetStringType(v)).toArray(DEREncoder[]::new))),
    };
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
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
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
      object.baseDN = dn;
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
      object.searchScope = scope;
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
      object.derefAliases = aliases;
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
      object.sizeLimit = size;
      return self();
    }


    /**
     * Sets the time limit.
     *
     * @param  time  time limit
     *
     * @return  this builder
     */
    public Builder timeLimit(final int time)
    {
      object.timeLimit = time;
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
      object.typesOnly = types;
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
      object.searchFilter = filter;
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
      object.searchFilter = SearchFilterParser.parse(filter);
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
      object.returnAttributes = attributes;
      return self();
    }
  }
}
