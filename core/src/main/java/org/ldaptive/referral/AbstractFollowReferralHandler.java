/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.referral;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.LdapException;
import org.ldaptive.LdapURL;
import org.ldaptive.LdapUtils;
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

  /** Whether to throw an exception if a referral cannot be chased. */
  private final boolean throwOnFailure;


  /**
   * Creates a new abstract referral handler.
   *
   * @param  limit  number of referrals to follow
   * @param  depth  number of referrals followed
   * @param  factory  referral connection factory
   * @param  tf  whether to throw on failure to chase referral
   */
  public AbstractFollowReferralHandler(
    final int limit, final int depth, final ReferralConnectionFactory factory, final boolean tf)
  {
    referralLimit = limit;
    referralDepth = depth;
    connectionFactory = factory;
    throwOnFailure = tf;
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
   * Returns whether to throw on failure to chase referrals.
   *
   * @return  whether to throw on failure to chase referrals
   */
  public boolean getThrowOnFailure()
  {
    return throwOnFailure;
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
   * Returns the result codes that indicate a successful referral was followed.
   *
   * @return  success result codes
   */
  protected Set<ResultCode> getSuccessResultCodes()
  {
    return Set.of(ResultCode.SUCCESS);
  }


  /**
   * Follows the supplied referral URLs in random order until a SUCCESS or REFERRAL_LIMIT_EXCEEDED occurs. If neither of
   * those conditions occurs this method returns null.
   *
   * @param  referralUrls  produced by the request
   *
   * @return  referral response
   *
   * @throws  LdapException  if the referral limit is exceeded
   */
  protected S followReferral(final String[] referralUrls)
    throws LdapException
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
        logger.debug("Created referral request {} for {}", referralRequest, op);
        referralResult = op.execute(referralRequest);
      } catch (LdapException e) {
        if (throwOnFailure || e.getResultCode() == ResultCode.REFERRAL_LIMIT_EXCEEDED) {
          throw e;
        }
        logger.warn("Could not follow referral to {}", url, e);
      } catch (Exception e) {
        if (throwOnFailure) {
          throw e;
        }
        logger.warn("Could not follow referral to {}", url, e);
      }
      if (referralResult != null &&
          (referralResult.getResultCode() == ResultCode.SUCCESS ||
           referralResult.getResultCode() == ResultCode.REFERRAL_LIMIT_EXCEEDED)) {
        break;
      }
    }
    logger.debug("Received referral result {}", referralResult);
    return referralResult;
  }


  @Override
  public S apply(final S result)
  {
    LdapUtils.assertNotNullArg(result, "Response cannot be null");
    if (!ResultCode.REFERRAL.equals(result.getResultCode()) ||
        result.getReferralURLs() == null ||
        result.getReferralURLs().length == 0)
    {
      return result;
    }
    if (referralDepth > referralLimit) {
      throw new RuntimeException(
        new LdapException(ResultCode.REFERRAL_LIMIT_EXCEEDED, "Referral limit of " + referralLimit + " exceeded"));
    }
    S referralResult = result;
    try {
      final S r = followReferral(result.getReferralURLs());
      // if referral fails, return the original result
      if (r != null || referralDepth > 1) {
        referralResult = r;
      }
    } catch (LdapException e) {
      throw new RuntimeException(e);
    }
    if (referralDepth == 1 &&
         (referralResult == null || !getSuccessResultCodes().contains(referralResult.getResultCode())))
    {
      // referral chasing failed, throw or use the original result
      if (getThrowOnFailure()) {
        throw new RuntimeException(
          new LdapException(ResultCode.LOCAL_ERROR, "Could not follow referral " + referralResult));
      }
      referralResult = result;
    }
    return referralResult;
  }
}
