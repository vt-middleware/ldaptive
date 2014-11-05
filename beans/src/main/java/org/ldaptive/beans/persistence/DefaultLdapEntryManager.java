/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.persistence;

import org.ldaptive.AddOperation;
import org.ldaptive.AddRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.DeleteRequest;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.ext.MergeOperation;
import org.ldaptive.ext.MergeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of an ldap entry manager. Uses an {@link
 * LdapEntryMapper} to convert objects to entries, then invokes LDAP operations
 * with those objects.
 *
 * @param  <T>  type of object to manage
 *
 * @author  Middleware Services
 * @version  $Revision: 3065 $ $Date: 2014-10-01 13:40:26 -0400 (Wed, 01 Oct 2014) $
 */
public class DefaultLdapEntryManager<T> implements LdapEntryManager<T>
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Mapper for converting ldap entry to object type. */
  private final LdapEntryMapper<T> ldapEntryMapper;

  /** Connection factory for LDAP communication. */
  private final ConnectionFactory connectionFactory;


  /**
   * Creates a new default ldap entry manager.
   *
   * @param  mapper  for object conversion
   * @param  factory  for LDAP communication
   */
  public DefaultLdapEntryManager(
    final LdapEntryMapper<T> mapper,
    final ConnectionFactory factory)
  {
    ldapEntryMapper = mapper;
    connectionFactory = factory;
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


  /** {@inheritDoc} */
  @Override
  public T find(final T object)
    throws LdapException
  {
    final String dn = getLdapEntryMapper().mapDn(object);
    final SearchRequest request = SearchRequest.newObjectScopeSearchRequest(dn);
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();

      final SearchOperation search = new SearchOperation(conn);
      final Response<SearchResult> response = search.execute(request);
      if (response.getResult().size() == 0) {
        throw new IllegalArgumentException(
          String.format(
            "Unable to find ldap entry %s, no entries returned: %s",
            dn,
            response));
      }
      if (response.getResult().size() > 1) {
        throw new IllegalArgumentException(
          String.format(
            "Unable to find ldap entry %s, multiple entries returned: %s",
            dn,
            response));
      }
      getLdapEntryMapper().map(object, response.getResult().getEntry());
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
    return object;
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> add(final T object)
    throws LdapException
  {
    final LdapEntry entry = new LdapEntry();
    getLdapEntryMapper().map(object, entry);

    final AddRequest request = new AddRequest(
      entry.getDn(),
      entry.getAttributes());
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();

      final AddOperation add = new AddOperation(conn);
      return add.execute(request);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> merge(final T object)
    throws LdapException
  {
    final LdapEntry entry = new LdapEntry();
    getLdapEntryMapper().map(object, entry);

    final MergeRequest request = new MergeRequest(entry);
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();

      final MergeOperation merge = new MergeOperation(conn);
      return merge.execute(request);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }


  /** {@inheritDoc} */
  @Override
  public Response<Void> delete(final T object)
    throws LdapException
  {
    final String dn = getLdapEntryMapper().mapDn(object);
    final DeleteRequest request = new DeleteRequest(dn);
    final Connection conn = getConnectionFactory().getConnection();
    try {
      conn.open();

      final DeleteOperation delete = new DeleteOperation(conn);
      return delete.execute(request);
    } finally {
      if (conn != null) {
        conn.close();
      }
    }
  }
}
