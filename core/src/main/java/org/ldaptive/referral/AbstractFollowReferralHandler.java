/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.Operation;
import org.ldaptive.Request;
import org.ldaptive.Result;
import org.ldaptive.ResultCode;
import org.ldaptive.transport.MessageFunctional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation of referral handling.
 *
 * @param  <Q>  type of request
 * @param  <S>  type of result
 *
 * @author  Middleware Services
 */
public abstract class AbstractFollowReferralHandler<Q extends Request, S extends Result>
  extends MessageFunctional.Function<Q, S, S, S>
{

  /** Default referral limit. Value is {@value}. */
  protected static final int DEFAULT_REFERRAL_LIMIT = 10;

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** Referral limit. */
  protected final int referralLimit;

  /** Referral depth. */
  protected final int referralDepth;

  /** Referral connection factory. */
  private final ReferralConnectionFactory connectionFactory;


  /**
   * Creates a new abstract referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   */
  public AbstractFollowReferralHandler(final int limit, final int depth, final ReferralConnectionFactory factory)
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
   * @param  url  of the referral
   *
   * @return  new request
   */
  protected abstract Q createReferralRequest(LdapURL url);


  /**
   * Creates an operation for this type of referral.
   *
   * @param  factory  to get a connection with
   *
   * @return  new operation
   */
  protected abstract Operation<Q, S> createReferralOperation(ConnectionFactory factory);


  /**
   * Follows the supplied referral URLs in order until a SUCCESS or REFERRAL_LIMIT_EXCEEDED occurs. If neither of those
   * conditions occurs this method returns null.
   *
   * @param  referralUrls  produced by the request
   *
   * @return  referral response
   */
  protected S followReferral(final String[] referralUrls)
  {
    S referralResult = null;
    final List<String> urls = Arrays.asList(referralUrls);
    Collections.shuffle(urls);
    logger.debug("Following referral with URLs: {}", urls);
    for (String url : urls) {
      final LdapURL ldapUrl = new LdapURL(url);
      if (ldapUrl.getHostname() == null) {
        continue;
      }

      final ConnectionFactory cf = connectionFactory.getConnectionFactory(ldapUrl.getHostnameWithSchemeAndPort());
      try {
        final Q referralRequest = createReferralRequest(ldapUrl);
        final Operation<Q, S> op = createReferralOperation(cf);
        referralResult = op.execute(referralRequest);
      } catch (LdapException e) {
        logger.warn("Could not follow referral to " + url, e);
      }
      if (referralResult != null &&
          (referralResult.getResultCode() == ResultCode.SUCCESS ||
           referralResult.getResultCode() == ResultCode.REFERRAL_LIMIT_EXCEEDED)) {
        break;
      }
    }
    return referralResult;
  }


  @Override
  public S apply(final S result)
  {
    if (result.getReferralURLs() == null || result.getReferralURLs().length == 0) {
      return result;
    }
    S referralResult = result;
    if (referralDepth <= referralLimit) {
      final S r = followReferral(result.getReferralURLs());
      if (r != null) {
        referralResult = r;
      }
    }
    return referralResult;
  }
}
