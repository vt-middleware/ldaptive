/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.sasl;

import java.util.function.Function;
import org.ldaptive.AnonymousBindRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ConnectionInitializer;
import org.ldaptive.LdapException;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.extended.WhoAmIRequest;
import org.ldaptive.extended.WhoAmIResponseParser;
import org.ldaptive.handler.ResultPredicate;

/**
 * Provides search functionality where the operation needs to be performed by an ID that is known at runtime. After a
 * connection is returned from the connection factory a SASL external bind is attempted using an authorization ID
 * provided by the idResolver function. Once the search is completed as this identity the connection is returned to its
 * original state using any {@link ConnectionInitializer} configured on the connection factory. If no connection
 * initializers are configured, an anonymous bind is performed.
 *
 * @author  Middleware Services
 */
public class SaslProxyAuthzSearchOperation
{

  /** Connection factory. */
  private final ConnectionFactory connectionFactory;


  /**
   * Creates a new SASL proxy authz search operation.
   *
   * @param  factory  connection factory
   */
  public SaslProxyAuthzSearchOperation(final ConnectionFactory factory)
  {
    connectionFactory = factory;
  }


  /**
   * Performs a SASL external proxy bind before executing a search. The connection is returned to it's original state by
   * using {@link ConnectionConfig#getConnectionInitializers()} or by performing an anonymous bind if no initializers
   * are configured.
   *
   * @param  idResolver  to resolve the SASL authz ID
   * @param  searchRequest  to perform
   *
   * @return  search response for the the search request
   *
   * @throws  LdapException  if the ID resolution fails, the search fails or the connection cannot be returned to it's
   * original state
   */
  public SearchResponse execute(final Function<Connection, String> idResolver, final SearchRequest searchRequest)
    throws LdapException
  {
    try (Connection conn = connectionFactory.getConnection()) {
      conn.open();
      try {
        saslAuthzBind(conn, idResolver.apply(conn));
        return conn.operation(searchRequest).execute();
      } catch (Exception e) {
        throw new LdapException(e);
      } finally {
        final ConnectionInitializer[] initializers =
          connectionFactory.getConnectionConfig().getConnectionInitializers();
        if (initializers != null && initializers.length > 0) {
          for (ConnectionInitializer initializer : initializers) {
            final Result result = initializer.initialize(conn);
            if (!result.isSuccess()) {
              throw new LdapException(
                ResultCode.PARAM_ERROR,
                "Connection initializer " + initializer + " returned response: " + result);
            }
          }
        } else {
          conn.operation(new AnonymousBindRequest()).execute();
        }
      }
    }
  }


  /**
   * Attempts a SASL external bind with the supplied authorizationId. A {@link WhoAmIRequest} is then sent to confirm
   * the authorizationId was successful.
   *
   * @param  conn  to bind on
   * @param  authzId  to authz as
   *
   * @throws  LdapException  if the bind is not successful or the WhoAmI operation fails
   */
  protected void saslAuthzBind(final Connection conn, final String authzId)
    throws LdapException
  {
    conn.operation(SaslBindRequest.builder()
      .mechanism(Mechanism.EXTERNAL.mechanism())
      .credentials(authzId)
      .build())
      .throwIf(ResultPredicate.NOT_SUCCESS)
      .execute();
    final String whoami = WhoAmIResponseParser.parse(conn.operation(new WhoAmIRequest())
      .throwIf(ResultPredicate.NOT_SUCCESS)
      .execute());
    if (!whoami.equals(authzId)) {
      throw new LdapException("SASL bind did not produce the expected subject '" + authzId +
        "'. Found '" + whoami + "' instead.");
    }
  }


  /**
   * Attempts to resolve an entry DN with a configured search request.
   */
  public static class SearchIdResolver implements Function<Connection, String>
  {

    /** Search request for entry resolution. */
    private final SearchRequest searchRequest;


    /**
     * Creates a new search id resolver.
     *
     * @param  request  to resolve a single entry
     */
    public SearchIdResolver(final SearchRequest request)
    {
      searchRequest = request;
    }


    @Override
    public String apply(final Connection connection)
    {
      try {
        final SearchResponse response = connection.operation(searchRequest)
          .throwIf(ResultPredicate.NOT_SUCCESS)
          .execute();
        if (response.entrySize() == 0) {
          throw new LdapException("No entries found attempting to resolve DN with " + searchRequest);
        }
        if (response.entrySize() > 1) {
          throw new LdapException("More than one entry found attempting to resolve DN with " + searchRequest);
        }
        return "dn:" + response.getEntry().getDn();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
