/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.persistence;

import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.AddResponse;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.DeleteResponse;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.Result;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.ext.MergeOperation;
import org.ldaptive.ext.MergeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of an ldap entry manager. Uses an {@link LdapEntryMapper} to convert objects to entries, then
 * invokes LDAP operations with those objects. By default all attributes are requested using both the '*' and '+'
 * syntaxes. For attributes that must be requested by name, use {@link #DefaultLdapEntryManager(LdapEntryMapper,
 * ConnectionFactory, String[])}.
 *
 * @param  <T>  type of object to manage
 *
 * @author  Middleware Services
 */
public class DefaultLdapEntryManager<T> implements LdapEntryManager<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Mapper for converting ldap entry to object type. */
  private final LdapEntryMapper<T> ldapEntryMapper;

  /** Connection factory for LDAP communication. */
  private final ConnectionFactory connectionFactory;

  /** Additional attributes to include in searches. */
  private final String[] returnAttributes;


  /**
   * Creates a new default ldap entry manager.
   *
   * @param  mapper  for object conversion
   * @param  factory  for LDAP communication
   */
  public DefaultLdapEntryManager(final LdapEntryMapper<T> mapper, final ConnectionFactory factory)
  {
    this(mapper, factory, null);
  }


  /**
   * Creates a new default ldap entry manager. Use of attrs is for cases where a directory does not support either the
   * '*' or '+' syntaxes for returning all attributes of a given type.
   *
   * @param  mapper  for object conversion
   * @param  factory  for LDAP communication
   * @param  attrs  additional return attributes
   */
  public DefaultLdapEntryManager(final LdapEntryMapper<T> mapper, final ConnectionFactory factory, final String[] attrs)
  {
    ldapEntryMapper = mapper;
    connectionFactory = factory;
    returnAttributes = attrs;
  }


  /**
   * Returns the ldap entry mapper.
   *
   * @return  ldap entry mapper
   */
  public LdapEntryMapper<T> getLdapEntryMapper()
  {
    return ldapEntryMapper;
  }


  /**
   * Returns the connection factory.
   *
   * @return  connection factory
   */
  public ConnectionFactory getConnectionFactory()
  {
    return connectionFactory;
  }


  /**
   * Returns the return attributes.
   *
   * @return  additional attributes to include in searches.
   */
  public String[] getReturnAttributes()
  {
    return returnAttributes;
  }


  @Override
  public T find(final T object)
    throws LdapException
  {
    final String dn = getLdapEntryMapper().mapDn(object);
    String[] attrs = ReturnAttributes.ALL.value();
    if (returnAttributes != null) {
      attrs = LdapUtils.concatArrays(attrs, returnAttributes);
    }
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(dn, attrs);
    final SearchOperation search = new SearchOperation(connectionFactory);
    final SearchResponse response = search.execute(request);
    if (response.entrySize() == 0) {
      throw new IllegalArgumentException(
        String.format("Unable to find ldap entry %s, no entries returned: %s", dn, response));
    }
    if (response.entrySize() > 1) {
      throw new IllegalArgumentException(
        String.format("Unable to find ldap entry %s, multiple entries returned: %s", dn, response));
    }
    getLdapEntryMapper().map(response.getEntry(), object);
    return object;
  }


  @Override
  public AddResponse add(final T object)
    throws LdapException
  {
    final LdapEntry entry = new LdapEntry();
    getLdapEntryMapper().map(object, entry);
    return AddOperation.execute(connectionFactory, new AddRequest(entry.getDn(), entry.getAttributes()));
  }


  @Override
  public Result merge(final T object)
    throws LdapException
  {
    final LdapEntry entry = new LdapEntry();
    getLdapEntryMapper().map(object, entry);

    final MergeOperation merge = new MergeOperation(connectionFactory);
    return merge.execute(new MergeRequest(entry));
  }


  @Override
  public DeleteResponse delete(final T object)
    throws LdapException
  {
    final String dn = getLdapEntryMapper().mapDn(object);
    return DeleteOperation.execute(connectionFactory, new DeleteRequest(dn));
  }
}
