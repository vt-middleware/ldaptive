/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.Operation;
import org.ldaptive.Request;
import org.ldaptive.Response;
import org.ldaptive.ResultCode;
import org.ldaptive.handler.HandlerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation of referral handling.
 *
 * @param  <Q>  type of ldap request
 * @param  <S>  type of ldap response
 *
 * @author  Middleware Services
 */
public abstract class AbstractReferralHandler<Q extends Request, S> implements ReferralHandler<Q, S>
{

  /** Default referral limit. Value is {@value}. */
  protected static final int DEFAULT_REFERRAL_LIMIT = 10;


  /** Default referral connection factory. Uses {@link DefaultConnectionFactory}. */
  protected static final ReferralConnectionFactory DEFAULT_CONNECTION_FACTORY = new ReferralConnectionFactory() {
    @Override
    public ConnectionFactory getConnectionFactory(final ConnectionConfig config, final String ldapUrl)
    {
      final ConnectionConfig cc = ConnectionConfig.newConnectionConfig(config);
      cc.setLdapUrl(ldapUrl);
      return new DefaultConnectionFactory(cc);
    }
  };

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Referral limit. */
  private final int referralLimit;

  /** Referral depth. */
  private final int referralDepth;

  /** Referral connection factory. */
  private final ReferralConnectionFactory connectionFactory;


  /**
   * Creates a new abstract referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  public AbstractReferralHandler(final int limit, final int depth, final ReferralConnectionFactory factory)
  {
    referralLimit = limit;
    referralDepth = depth;
    connectionFactory = factory;
  }


  /**
   * Returns the maximum number of referrals to follow.
   *
   * @return  referral limit
   */
  public int getReferralLimit()
  {
    return referralLimit;
  }


  /**
   * Returns the referral depth of this handler.
   *
   * @return  referral depth
   */
  public int getReferralDepth()
  {
    return referralDepth;
  }


  /**
   * Returns the referral connection factory.
   *
   * @return  referral connection factory
   */
  public ReferralConnectionFactory getReferralConnectionFactory()
  {
    return connectionFactory;
  }


  /**
   * Creates a new request for this type of referral.
   *
   * @param  request  of the original operation
   * @param  url  of the referral
   *
   * @return  new request
   */
  protected abstract Q createReferralRequest(final Q request, final LdapURL url);


  /**
   * Creates an operation for this type of referral.
   *
   * @param  conn  to execute the operation on
   *
   * @return  new operation
   */
  protected abstract Operation<Q, S> createReferralOperation(final Connection conn);


  /**
   * Follows the supplied referral URLs in order until a SUCCESS or REFERRAL_LIMIT_EXCEEDED occurs. If neither of those
   * conditions occurs this method returns null.
   *
   * @param  conn  the original operation occurred on
   * @param  request  of the operation that produced a referral
   * @param  referralUrls  produced by the request
   *
   * @return  referral response
   *
   * @throws  LdapException  if a REFERRAL_LIMIT_EXCEEDED in encountered
   */
  protected Response<S> followReferral(final Connection conn, final Q request, final String[] referralUrls)
    throws LdapException
  {
    Response<S> referralResponse = null;
    final List<String> urls = Arrays.asList(referralUrls);
    Collections.shuffle(urls);
    logger.debug("Following referral with URLs: {}", urls);
    for (String url : urls) {
      final LdapURL ldapUrl = new LdapURL(url);
      if (ldapUrl.getEntry().getHostname() == null) {
        continue;
      }

      final ConnectionFactory cf = connectionFactory.getConnectionFactory(
        conn.getConnectionConfig(),
        ldapUrl.getEntry().getHostnameWithSchemeAndPort());
      try (Connection referralConn = cf.getConnection()) {
        referralConn.open();

        final Q referralRequest = createReferralRequest(request, ldapUrl);
        final Operation<Q, S> op = createReferralOperation(referralConn);
        referralResponse = op.execute(referralRequest);
      } catch (LdapException e) {
        logger.warn("Could not follow referral to " + url, e);
        if (e.getResultCode() == ResultCode.REFERRAL_LIMIT_EXCEEDED) {
          throw e;
        }
      }
      if (referralResponse != null && referralResponse.getResultCode() == ResultCode.SUCCESS) {
        break;
      }
    }
    return referralResponse;
  }


  @Override
  public HandlerResult<Response<S>> handle(final Connection conn, final Q request, final Response<S> response)
    throws LdapException
  {
    HandlerResult<Response<S>> result;
    if (referralDepth > referralLimit) {
      result = new HandlerResult<>(
        new Response<>(
          response.getResult(),
          ResultCode.REFERRAL_LIMIT_EXCEEDED,
          response.getMessage(),
          response.getMatchedDn(),
          response.getControls(),
          response.getReferralURLs(),
          response.getMessageId()));
    } else {
      final Response<S> referralResponse = followReferral(conn, request, response.getReferralURLs());
      if (referralResponse != null) {
        result = new HandlerResult<>(referralResponse);
      } else {
        result = new HandlerResult<>(response);
      }
    }
    return result;
  }


  /**
   * Implementation that does not require the response.
   *
   * @param  conn  the operation occurred on
   * @param  request  the operation executed
   * @param  referralUrls  encountered in the operation
   *
   * @return  handler result
   *
   * @throws  LdapException  if an error occurs following referrals
   */
  public HandlerResult<Response<S>> handle(final Connection conn, final Q request, final String[] referralUrls)
    throws LdapException
  {
    HandlerResult<Response<S>> result;
    if (referralDepth > referralLimit) {
      result = new HandlerResult<>(
        new Response<>((S) null, ResultCode.REFERRAL_LIMIT_EXCEEDED, null, null, null, null, -1));
    } else {
      final Response<S> referralResponse = followReferral(conn, request, referralUrls);
      if (referralResponse != null) {
        result = new HandlerResult<>(referralResponse);
      } else {
        result = new HandlerResult<>(null);
      }
    }
    return result;
  }


  @Override
  public void initializeRequest(final Q request) {}
}
